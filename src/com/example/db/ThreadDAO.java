package com.example.db;

import java.util.List;

import com.example.entities.ThreadInfo;

/**
 * ���ݷ��ʽӿ�
 * @author Administrator
 *
 */
public interface ThreadDAO {
	//�����߳���Ϣ
	public void insertThread(ThreadInfo threadInfo);
	//ɾ���߳�
	public void deleteThread(String url);
	//�����߳����ؽ���
	public void updateThread(String url, int thread_id, int finished);
	//��ѯ�ļ����߳���Ϣ
	public List<ThreadInfo> getThreads(String url);
	//�߳���Ϣ�Ƿ����
	public boolean isExists(String url, int thread_id);
}
