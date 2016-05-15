package com.example.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 * @author Administrator
 *
 */
public class DBHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "download.db";
	private static DBHelper sHelper = null; // 静态对象引用
	private static final int VERSION = 1;
	//创建保存线程信息表
	private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement, " +
			"thread_id integer, url text, start integer, end integer, finished integer)";
	private static final String SQL_DROP = "drop table if exists thread_info";

	//私有化构造方法
	private DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}
	
	/**
	 * 获得类的对象
	 * 使用单例模式保证线程安全
	 * 静态引用对象
	 */
	public static DBHelper getInstance(Context context) {
		if (sHelper == null) {
			sHelper = new DBHelper(context);
		}
		return sHelper;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL(SQL_DROP);
		db.execSQL(SQL_CREATE);
	}

}
