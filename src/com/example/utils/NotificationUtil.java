package com.example.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.downloaddemo.MainActivity;
import com.example.downloaddemo.R;
import com.example.entities.FileInfo;
import com.example.services.DownloadService;

/**
 * ֪ͨ������
 * @author Administrator
 *
 */
public class NotificationUtil {

	private NotificationManager mNotificationManager = null;
	private Map<Integer, Notification> mNotifications = null;
	private Context mContext = null;
	
	public NotificationUtil(Context context) {
		mContext = context;
		//���֪ͨϵͳ����
		mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		//����֪ͨ�ļ���
		mNotifications = new HashMap<Integer, Notification>();
	}
	
	/**
	 * ��ʾ֪ͨ
	 * @param fileInfo
	 */
	public void showNotification(FileInfo fileInfo) {
		//�ж�֪ͨ�Ƿ��Ѿ���ʾ��
		if (!mNotifications.containsKey(fileInfo.getId())) {
			//����֪ͨ����
			Notification notification = new Notification();
			//���ù�������
			notification.tickerText = fileInfo.getFileName() + "��ʼ����";
			//������ʾʱ��
			notification.when = System.currentTimeMillis();
			//����ͼ��
			notification.icon = R.drawable.ic_launcher;
			//����֪ͨ����
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			//���õ��֪ͨ���Ĳ���
			Intent intent = new Intent(mContext, MainActivity.class);
			PendingIntent pintent = PendingIntent.getActivity(mContext, 0, intent, 0);
			notification.contentIntent = pintent;
			//����RemoteViews����
			RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification);
			//���ÿ�ʼ��ť����
			Intent intentStart = new Intent(mContext, DownloadService.class);
			intentStart.setAction(DownloadService.ACTION_START);
			intentStart.putExtra("fileInfo", fileInfo);
			PendingIntent piStart = PendingIntent.getService(mContext, 0, intentStart, 0);
			remoteViews.setOnClickPendingIntent(R.id.btStart, piStart);
			//���ý�����ť����
			Intent intentStop = new Intent(mContext, DownloadService.class);
			intentStop.setAction(DownloadService.ACTION_STOP);
			intentStop.putExtra("fileInfo", fileInfo);
			PendingIntent piStop = PendingIntent.getService(mContext, 0, intentStop, 0);
			remoteViews.setOnClickPendingIntent(R.id.btStop, piStop);
			//����TextView
			remoteViews.setTextViewText(R.id.tvFile, fileInfo.getFileName());
			//����Notification����ͼ
			notification.contentView = remoteViews;
			//����֪ͨ�㲥
			mNotificationManager.notify(fileInfo.getId(), notification);
			//��֪ͨ�ӵ�������
			mNotifications.put(fileInfo.getId(), notification);
		}
	}
	
	/**
	 * ȡ��֪ͨ
	 * @param id
	 */
	public void cancelNotification(int id) {
		mNotificationManager.cancel(id);
		mNotifications.remove(id);
	}
	
	/**
	 * ���½�����
	 * @param id
	 * @param progress
	 */
	public void updateNotification(int id, int progress) {
		Notification notification = mNotifications.get(id);
		if (notification != null) {
			//�޸Ľ�����
			notification.contentView.setProgressBar(R.id.pbProgress, 100, progress, false);
			mNotificationManager.notify(id, notification);
		}
	}
	
}
