package com.example.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.entities.FileInfo;

public class DownloadService extends Service {
	
	public static final String DOWNLOAD_PATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + 
			"/downloads/";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final int MSG_INIT = 0;
	private DownloadTask mTask = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//���Activity�����Ĳ���
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.i("test", "Start : " + fileInfo.toString());
			//������ʼ���߳�
			new InitThread(fileInfo).start();
		} else if (ACTION_STOP.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.i("test", "Stop" + fileInfo.toString());
			if (mTask != null) {
				mTask.isPause = true;
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo = (FileInfo) msg.obj;
				Log.i("test", "Init : " + fileInfo);
				//������������
				mTask = new DownloadTask(DownloadService.this, fileInfo);
				mTask.download();
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
