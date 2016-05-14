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
        //��ʼ�����
        mTvFileName = (TextView) findViewById(R.id.tvFileName);
        mPbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        mBtStop = (Button) findViewById(R.id.btStop);
        mBtStart = (Button) findViewById(R.id.btStart);
        mPbProgress.setMax(100);
        //�����ļ���Ϣ����
        final FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/imooc.apk", 
        		"imooc.apk", 0, 0);
        //����¼�����
        mBtStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//ͨ��Intent���ݲ�����Service
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
        //ע��㲥������
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(mReceiver);
    };
    
    /**
     * ����UI�Ĺ㲥������
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
