package com.example.services;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import com.example.entities.FileInfo;

public class DownloadService extends Service {
	
	//����·��
	public static final String DOWNLOAD_PATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + 
			"/downloads/";
	//��ʼ��������
	public static final String ACTION_START = "ACTION_START";
	//ֹͣ��������
	public static final String ACTION_STOP = "ACTION_STOP";
	//������������
	public static final String ACTION_FINISH = "ACTION_FINISH";
	//����UI����
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	//��ʼ����ʶ
	public static final int MSG_INIT = 0x1;
	private InitThread mInitThread = null;
	//��������ļ���
	private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<Integer, DownloadTask>(); 
//	private DownloadTask mTask = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//���Activity�����Ĳ���
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
//			Log.i("test", "Start : " + fileInfo.toString());
			//�ӵ��������������ʼ���߳�
			mInitThread = new InitThread(fileInfo);
			DownloadTask.sExecutorService.execute(mInitThread);
//			new InitThread(fileInfo).start();
		} else if (ACTION_STOP.equals(intent.getAction())) {
//			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
//			Log.i("test", "Stop" + fileInfo.toString());
			//��ͣ����
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			//�Ӽ�����ȡ����������
			DownloadTask task = mTasks.get(fileInfo.getId());
			if (task != null) {
				//ֹͣ��������
				task.isPause = true;
			}
//			if (mTask != null) {
//				mTask.isPause = true;
//			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				//��ó�ʼ���Ľ��
				FileInfo fileInfo = (FileInfo) msg.obj;
//				Log.i("test", "Init : " + fileInfo);
				//������������
				DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 3);
				task.download();
				//������������ӵ�������
				mTasks.put(fileInfo.getId(), task);
//				mTask = new DownloadTask(DownloadService.this, fileInfo);
//				mTask.download();
				break;

			default:
				break;
			}
		}
	};
	
	/**
	 * ��ʼ�����߳�
	 */
	class InitThread extends Thread {
		private FileInfo mFileInfo = null;

		public InitThread(FileInfo mFileInfo) {
			this.mFileInfo = mFileInfo;
		}
		
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			try {
				//���������ļ�
				URL url = new URL(mFileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				/*
				 * ��Ϊ�����Ǵӷ����������ļ�
				 * ��������ѡ��GET��ʽ����ȡ����
				 * �������ز������⣬��POST
				 */
				conn.setRequestMethod("GET");
				int length = -1;
				if (conn.getResponseCode() == HttpStatus.SC_OK) {
					//����ļ�����
					length = conn.getContentLength();
				}
				if (length <= 0) {
					return;
				}
				File dir = new File(DOWNLOAD_PATH);
				if (!dir.exists()) {
					dir.mkdir();
				}
				//�ڱ��ش����ļ�
				File file = new File(dir, mFileInfo.getFileName());
				/*
				 * r---Read---ָ��ȡȨ��
				 * w---Write---ָд��Ȩ��
				 * d---Delete---ָɾ��Ȩ��
				 */
				raf = new RandomAccessFile(file, "rwd");
				//�����ļ�����
				raf.setLength(length);
				mFileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					raf.close();
					conn.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
