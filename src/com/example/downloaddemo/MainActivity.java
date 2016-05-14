package com.example.downloaddemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


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
    }
}
