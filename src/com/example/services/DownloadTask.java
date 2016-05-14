package com.example.services;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;

import com.example.db.ThreadDAO;
import com.example.db.ThreadDAOImpl;
import com.example.entities.FileInfo;
import com.example.entities.ThreadInfo;

/**
 * ����������
 * @author Administrator
 *
 */
public class DownloadTask {
	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinished = 0;
	public boolean isPause = false;
	
	public DownloadTask(Context mContext, FileInfo mFileInfo) {
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		mDao = new ThreadDAOImpl(mContext);
	}
	
	public void download() {
		//��ȡ���ݿ���߳���Ϣ
		List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		if (threadInfos.size() == 0) {
			//��ʼ���߳���Ϣ����
			threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
		} else {
			threadInfo = threadInfos.get(0);
		}
		//�������߳̽�������
		new DownloadThread(threadInfo).start();
	}
	
	/**
	 * �����߳�
	 */
	class DownloadThread extends Thread {
		private ThreadInfo mThreadInfo = null;

		public DownloadThread(ThreadInfo mThreadInfo) {
			this.mThreadInfo = mThreadInfo;
		}
		
		public void run() {
			//�����ݿ����һ���߳���Ϣ
			if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
				mDao.insertThread(mThreadInfo);
			}
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream input = null;
			try {
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				//��������λ��
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
				//�����ļ�д��λ��
				File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				/*
				 * seek()�������ڶ�д��ʱ���������úõ��ֽ���������һ���ֽ�����ʼ��д
				 * ���磺seek(100),������100���ֽڣ��ӵ�101���ֽڿ�ʼ��д
				 */
				raf.seek(start);
				Intent intent = new Intent(DownloadService.ACTION_UPDATE);
				mFinished += mThreadInfo.getFinished();
				//��ʼ����
				if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					//��ȡ����
					input = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4];
					int len = -1;
					long time = System.currentTimeMillis();
					while ((len = input.read(buffer)) != -1) {
						//д���ļ�
						raf.write(buffer, 0, len);
						//�����ؽ��ȷ��͹㲥��Activity
						mFinished += len;
						if (System.currentTimeMillis() - time > 500) {
							time = System.currentTimeMillis();
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							mContext.sendBroadcast(intent);
						}
						//��������ͣʱ���������ؽ���
						if (isPause) {
							mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
							return;
						}
					}
					//ɾ���߳���Ϣ
					mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					conn.disconnect();
					raf.close();
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
