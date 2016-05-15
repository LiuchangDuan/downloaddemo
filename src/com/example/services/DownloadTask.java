package com.example.services;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	private int mThreadCount = 1; // �߳�����
	private List<DownloadThread> mThreadList = null; // �̼߳���
	public static ExecutorService sExecutorService = Executors.newCachedThreadPool(); // �̳߳�
	
	public DownloadTask(Context mContext, FileInfo mFileInfo, int mThreadCount) {
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadCount = mThreadCount;
		mDao = new ThreadDAOImpl(mContext);
	}
	
	public void download() {
		//��ȡ���ݿ���߳���Ϣ �����ݿ������ؽ���
		List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
		if (threadInfos.size() == 0) {
			//���ÿ���߳����صĳ���
			int length = mFileInfo.getLength() / mThreadCount;
			for (int i = 0; i < mThreadCount; i++) {
				//�����߳���Ϣ
				ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), 
						length * i, (i + 1) * length - 1, 0);
				if (i == mThreadCount - 1) {
					threadInfo.setEnd(mFileInfo.getLength());
				}
				//��ӵ��߳���Ϣ������
				threadInfos.add(threadInfo);
				//�����ݿ����һ���߳���Ϣ
				mDao.insertThread(threadInfo);
			}
		}
		mThreadList = new ArrayList<DownloadThread>();
		//��������߳̽�������
		for (ThreadInfo info : threadInfos) {
			DownloadThread thread = new DownloadThread(info);
//			thread.start();
			DownloadTask.sExecutorService.execute(thread);
			//����̵߳������У��������
			mThreadList.add(thread);
		}
		
//		ThreadInfo threadInfo = null;
//		if (threadInfos.size() == 0) {
//			//��ʼ���߳���Ϣ����
//			threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
//		} else {
//			threadInfo = threadInfos.get(0);
//		}
//		//�������߳̽�������
//		new DownloadThread(threadInfo).start();
	}
	
	/**
	 * �ж��Ƿ������̶߳�ִ�����
	 */
	private synchronized void checkAllThreadsFinished() {
		boolean allFinished = true;
		//�����̼߳��ϣ��ж��߳��Ƿ�ִ�����
		for (DownloadThread thread : mThreadList) {
			if (!thread.isFinished) {
				allFinished = false;
				break;
			}
		}
		if (allFinished) {
			//ɾ���߳���Ϣ
			mDao.deleteThread(mFileInfo.getUrl());
			//���͹㲥֪ͨUI�����������
			Intent intent = new Intent(DownloadService.ACTION_FINISH);
			intent.putExtra("fileInfo", mFileInfo);
			mContext.sendBroadcast(intent);
		}
	}
	
	/**
	 * �����߳�
	 */
	class DownloadThread extends Thread {
		private ThreadInfo mThreadInfo = null;
		public boolean isFinished = false; // �߳��Ƿ�ִ�����

		public DownloadThread(ThreadInfo mThreadInfo) {
			this.mThreadInfo = mThreadInfo;
		}
		
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream input = null;
			try {
				//������
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
				mFinished += mThreadInfo.getFinished();
				//��ʼ����
				if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					//��ȡ����
					int len = 0;
					byte[] buffer = new byte[1024];
					Intent intent = new Intent(DownloadService.ACTION_UPDATE);
					long time = System.currentTimeMillis();
					input = conn.getInputStream();
					while ((len = input.read(buffer)) != -1) {
						//д���ļ�
						raf.write(buffer, 0, len);
						//�ۼ������ļ���ɽ���
						mFinished += len;
						//�ۼ�ÿ���߳���ɵĽ���
						mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
						//���1000�������һ�ν���
						if (System.currentTimeMillis() - time > 1000) {
							time = System.currentTimeMillis();
							//���ͽ��ȵ�Activity
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							intent.putExtra("id", mFileInfo.getId());
							mContext.sendBroadcast(intent);
						}
						if (isPause) {
							//��������ͣʱ���������ؽ���
							mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
							return;
						}
					}
				}
				//��ʶ�߳�ִ�����
				isFinished = true;
				
				//������������Ƿ�ִ�����
				checkAllThreadsFinished();
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
