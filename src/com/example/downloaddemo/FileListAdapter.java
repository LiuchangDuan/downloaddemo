package com.example.downloaddemo;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.entities.FileInfo;
import com.example.services.DownloadService;

/**
 * �ļ��б�������
 */
public class FileListAdapter extends BaseAdapter {
	
	private Context mContext = null;
	private List<FileInfo> mFileList = null;
	
	public FileListAdapter(Context mContext, List<FileInfo> mFileList) {
		super();
		this.mContext = mContext;
		this.mFileList = mFileList;
	}

	/**
	 * getCount�������ڷ�����Ҫ��ListView����ʾ������
	 * Ҫ���ļ������е��ļ���ʾ���б��ϣ����Է��ص����ļ����϶����size()����
	 * �����ļ�����
	 */
	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		final FileInfo fileInfo = mFileList.get(position);
		ViewHolder holder = null;
		if (view == null) {
			//������ͼ
			view = LayoutInflater.from(mContext).inflate(R.layout.listitem, null);
			//��ò����еĿؼ�
			holder = new ViewHolder();
			holder.tvFileName = (TextView) view.findViewById(R.id.tvFileName);
			holder.btStop = (Button) view.findViewById(R.id.btStop);
			holder.btStart = (Button) view.findViewById(R.id.btStart);
			holder.pbProgress = (ProgressBar) view.findViewById(R.id.pbProgress);
			holder.tvFileName.setText(fileInfo.getFileName());
			holder.pbProgress.setMax(100);
			holder.btStart.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//֪ͨService��ʼ����
					Intent intent = new Intent(mContext, DownloadService.class);
					intent.setAction(DownloadService.ACTION_START);
					intent.putExtra("fileInfo", fileInfo);
					mContext.startService(intent);
				}
			});
			holder.btStop.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//֪ͨServiceֹͣ����
					Intent intent = new Intent(mContext, DownloadService.class);
					intent.setAction(DownloadService.ACTION_STOP);
					intent.putExtra("fileInfo", fileInfo);
					mContext.startService(intent);
				}
			});
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		//������ͼ�еĿؼ�
		holder.pbProgress.setProgress(fileInfo.getFinished());
		return view;
	}
	
	/**
	 * �����б����еĽ�����
	 */
	public void updateProgress(int id, int progress) {
		FileInfo fileInfo = mFileList.get(id);
		fileInfo.setFinished(progress);
		/*
		 * �÷����������޸��������󶨵������������ˢ��Activity��֪ͨActivity����ListView(����getView())
		 */
		notifyDataSetChanged();
	}
	
	/**
	 * ViewHolder����һ����ʱ�Ĵ���������ÿ��getView������ÿ�η��ص�View���������������´����á�
	 * �������ĺô����ǲ���ÿ�ζ��������ļ��������ҿؼ�
	 */
	static class ViewHolder {
		TextView tvFileName;
		Button btStop, btStart;
		ProgressBar pbProgress;
	}

}
