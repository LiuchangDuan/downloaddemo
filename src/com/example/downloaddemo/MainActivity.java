package com.example.downloaddemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.entities.FileInfo;
import com.example.services.DownloadService;


public class MainActivity extends Activity {
	
	private ListView mLvFile = null;
	private List<FileInfo> mFileList = null;
	private FileListAdapter mAdapter = null;
	
//	private TextView mTvFileName = null;
//	
//	private ProgressBar mPbProgress = null;
//	
//	private Button mBtStop = null;
//	
//	private Button mBtStart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //��ʼ�����
        mLvFile = (ListView) findViewById(R.id.lvFile);
        //�����ļ�����
        mFileList = new ArrayList<FileInfo>();
//        mTvFileName = (TextView) findViewById(R.id.tvFileName);
//        mPbProgress = (ProgressBar) findViewById(R.id.pbProgress);
//        mBtStop = (Button) findViewById(R.id.btStop);
//        mBtStart = (Button) findViewById(R.id.btStart);
//        mPbProgress.setMax(100);
        //�����ļ���Ϣ����
        FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/imooc.apk", 
        		"imooc.apk", 0, 0);
        FileInfo fileInfo1 = new FileInfo(1, "http://www.imooc.com/download/Activator.exe", 
        		"Activator.exe", 0, 0);
        FileInfo fileInfo2 = new FileInfo(2, "http://www.imooc.com/download/iTunes64Setup.exe", 
        		"iTunes64Setup.exe", 0, 0);
        FileInfo fileInfo3 = new FileInfo(3, "http://www.imooc.com/download/SkyGameInstall99602.exe", 
        		"SkyGameInstall99602.exe", 0, 0);
        mFileList.add(fileInfo);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        //����������
        mAdapter = new FileListAdapter(this, mFileList);
        //����ListView
        mLvFile.setAdapter(mAdapter);
        //����¼�����
//        mBtStart.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				//ͨ��Intent���ݲ�����Service
//				Intent intent = new Intent(MainActivity.this, DownloadService.class);
//				intent.setAction(DownloadService.ACTION_START);
//				intent.putExtra("fileInfo", fileInfo);
//				startService(intent);
//			}
//		});
//        mBtStop.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MainActivity.this, DownloadService.class);
//				intent.setAction(DownloadService.ACTION_STOP);
//				intent.putExtra("fileInfo", fileInfo);
//				startService(intent);
//			}
//		});
        //ע��㲥������
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISH);
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
				//���½�����
				int finished = intent.getIntExtra("finished", 0);
				int id = intent.getIntExtra("id", 0);
				mAdapter.updateProgress(id, finished);
//				mPbProgress.setProgress(finished);
			} else if (DownloadService.ACTION_FINISH.equals(intent.getAction())) {
				//���ؽ���
				FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
				//���½���Ϊ0
				mAdapter.updateProgress(fileInfo.getId(), 0);
				Toast.makeText(MainActivity.this, mFileList.get(fileInfo.getId()).getFileName() + "�������", Toast.LENGTH_SHORT).show();
			}
		}
    	
    };
}
