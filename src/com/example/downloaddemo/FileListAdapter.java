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
 * 文件列表适配器
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
	 * getCount方法用于返回需要在ListView上显示的总数
	 * 要让文件集合中的文件显示在列表上，所以返回的是文件集合对象的size()方法
	 * 返回文件个数
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
			//加载视图
			view = LayoutInflater.from(mContext).inflate(R.layout.listitem, null);
			//获得布局中的控件
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
					//通知Service开始下载
					Intent intent = new Intent(mContext, DownloadService.class);
					intent.setAction(DownloadService.ACTION_START);
					intent.putExtra("fileInfo", fileInfo);
					mContext.startService(intent);
				}
			});
			holder.btStop.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//通知Service停止下载
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
		//设置视图中的控件
		holder.pbProgress.setProgress(fileInfo.getFinished());
		return view;
	}
	
	/**
	 * 更新列表项中的进度条
	 */
	public void updateProgress(int id, int progress) {
		FileInfo fileInfo = mFileList.get(id);
		fileInfo.setFinished(progress);
		/*
		 * 该方法可以在修改适配器绑定的数组后不用重新刷新Activity，通知Activity更新ListView(调用getView())
		 */
		notifyDataSetChanged();
	}
	
	/**
	 * ViewHolder就是一个临时的储存器，把每次getView方法中每次返回的View缓存起来，可以下次再用。
	 * 这样做的好处就是不必每次都到布局文件中来查找控件
	 */
	static class ViewHolder {
		TextView tvFileName;
		Button btStop, btStart;
		ProgressBar pbProgress;
	}

}
