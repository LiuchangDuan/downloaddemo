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
 * 下载任务类
 * @author Administrator
 *
 */
public class DownloadTask {
	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinished = 0;
	public boolean isPause = false;
	private int mThreadCount = 1; // 线程数量
	private List<DownloadThread> mThreadList = null; // 线程集合
	public static ExecutorService sExecutorService = Executors.newCachedThreadPool(); // 线程池
	
	public DownloadTask(Context mContext, FileInfo mFileInfo, int mThreadCount) {
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadCount = mThreadCount;
		mDao = new ThreadDAOImpl(mContext);
	}
	
	public void download() {
		//读取数据库的线程信息 从数据库获得下载进度
		List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
		if (threadInfos.size() == 0) {
			//获得每个线程下载的长度
			int length = mFileInfo.getLength() / mThreadCount;
			for (int i = 0; i < mThreadCount; i++) {
				//创建线程信息
				ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), 
						length * i, (i + 1) * length - 1, 0);
				if (i == mThreadCount - 1) {
					threadInfo.setEnd(mFileInfo.getLength());
				}
				//添加到线程信息集合中
				threadInfos.add(threadInfo);
				//向数据库插入一条线程信息
				mDao.insertThread(threadInfo);
			}
		}
		mThreadList = new ArrayList<DownloadThread>();
		//启动多个线程进行下载
		for (ThreadInfo info : threadInfos) {
			DownloadThread thread = new DownloadThread(info);
//			thread.start();
			DownloadTask.sExecutorService.execute(thread);
			//添加线程到集合中（方便管理）
			mThreadList.add(thread);
		}
		
//		ThreadInfo threadInfo = null;
//		if (threadInfos.size() == 0) {
//			//初始化线程信息对象
//			threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
//		} else {
//			threadInfo = threadInfos.get(0);
//		}
//		//创建子线程进行下载
//		new DownloadThread(threadInfo).start();
	}
	
	/**
	 * 判断是否所有线程都执行完毕
	 */
	private synchronized void checkAllThreadsFinished() {
		boolean allFinished = true;
		//遍历线程集合，判断线程是否都执行完毕
		for (DownloadThread thread : mThreadList) {
			if (!thread.isFinished) {
				allFinished = false;
				break;
			}
		}
		if (allFinished) {
			//删除线程信息
			mDao.deleteThread(mFileInfo.getUrl());
			//发送广播通知UI下载任务结束
			Intent intent = new Intent(DownloadService.ACTION_FINISH);
			intent.putExtra("fileInfo", mFileInfo);
			mContext.sendBroadcast(intent);
		}
	}
	
	/**
	 * 下载线程
	 */
	class DownloadThread extends Thread {
		private ThreadInfo mThreadInfo = null;
		public boolean isFinished = false; // 线程是否执行完毕

		public DownloadThread(ThreadInfo mThreadInfo) {
			this.mThreadInfo = mThreadInfo;
		}
		
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream input = null;
			try {
				//打开连接
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				//设置下载位置
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
				//设置文件写入位置
				File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				/*
				 * seek()方法：在读写的时候跳过设置好的字节数，从下一个字节数开始读写
				 * 例如：seek(100),则跳过100个字节，从第101个字节开始读写
				 */
				raf.seek(start);
				mFinished += mThreadInfo.getFinished();
				//开始下载
				if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					//读取数据
					int len = 0;
					byte[] buffer = new byte[1024];
					Intent intent = new Intent(DownloadService.ACTION_UPDATE);
					long time = System.currentTimeMillis();
					input = conn.getInputStream();
					while ((len = input.read(buffer)) != -1) {
						//写入文件
						raf.write(buffer, 0, len);
						//累加整个文件完成进度
						mFinished += len;
						//累加每个线程完成的进度
						mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
						//间隔1000毫秒更新一次进度
						if (System.currentTimeMillis() - time > 1000) {
							time = System.currentTimeMillis();
							//发送进度到Activity
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							intent.putExtra("id", mFileInfo.getId());
							mContext.sendBroadcast(intent);
						}
						if (isPause) {
							//在下载暂停时，保存下载进度
							mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
							return;
						}
					}
				}
				//标识线程执行完毕
				isFinished = true;
				
				//检查下载任务是否执行完毕
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
