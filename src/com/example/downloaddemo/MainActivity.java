package com.example.downloaddemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.entities.FileInfo;
import com.example.services.DownloadService;


public class MainActivity extends Activity {
	
	private TextView mTvFileName = null;
	
	private ProgressBar mPbProgress = null;
	
	private Button mBtStop = null;
	
	private Button mBtStart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化组件
        mTvFileName = (TextView) findViewById(R.id.tvFileName);
        mPbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        mBtStop = (Button) findViewById(R.id.btStop);
        mBtStart = (Button) findViewById(R.id.btStart);
        mPbProgress.setMax(100);
        //创建文件信息对象
        final FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/imooc.apk", 
        		"imooc.apk", 0, 0);
        //添加事件监听
        mBtStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//通过Intent传递参数给Service
				Intent intent = new Intent(MainActivity.this, DownloadService.class);
				intent.setAction(DownloadService.ACTION_START);
				intent.putExtra("fileInfo", fileInfo);
				startService(intent);
			}
		});
        mBtStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, DownloadService.class);
				intent.setAction(DownloadService.ACTION_STOP);
				intent.putExtra("fileInfo", fileInfo);
				startService(intent);
			}
		});
        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(mReceiver);
    };
    
    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
				int finished = intent.getIntExtra("finished", 0);
				mPbProgress.setProgress(finished);
			}
		}
    	
    };
}
