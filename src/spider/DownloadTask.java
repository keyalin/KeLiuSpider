/**
 * ���ļ�������������web��Դ��ģ����DownloadTask
 */

package spider;

import java.sql.*;

/**
 * @author keyalin
 *
 */
public abstract class DownloadTask implements Runnable{
	protected int id;
	protected Manager taskManager;
	protected boolean wait;//���Կ����������ͣ�ͻָ�
	protected status state;//��ӳ�̵߳�״̬
	protected enum status {SLEEP, SAVE, FETCH, ABORT}
	
	protected DownloadTask(int id, Manager taskManager){
		this.id = id;
		this.taskManager = taskManager;
		wait = false;
		state = status.SLEEP;
	}
	
	/**
	 * �޸�id��ֵ
	 * @param id
	 */
	public void setID(int id){
		this.id = id;
	}
	
	/**
	 * �ṩ�޸�mainframe�ϱ�ѡ����Status����ʾ�ķ���
	 * @param row ��ѡ������ڵ���
	 * @param column ��ѡ�����ڵ���
	 * @param value ��Ҫ�޸ĳɵ�ֵ
	 */
	protected synchronized void showStatusOnMainframe(status state){
		taskManager.setValueAtMainframe(id, 4, state.toString());
	}
	
	/**
	 * �ṩ�޸�mainframe�ϱ�ѡ��ize������ʾ�ķ���
	 * @param row ��ѡ������ڵ���
	 * @param column ��ѡ�����ڵ���
	 * @param value ��Ҫ�޸ĳɵ�ֵ
	 */
	protected synchronized void showDownloadSizeOnMainframe(int size){
		taskManager.setValueAtMainframe(id, 3, Integer.toString(size) + "B");
	}
	
	/**
	 * �ṩ�޸�mainframe�ϱ�ѡ��Url������ʾ�ķ���
	 * @param row ��ѡ������ڵ���
	 * @param column ��ѡ�����ڵ���
	 * @param value ��Ҫ�޸ĳɵ�ֵ
	 */
	protected synchronized void showUrlOnMainframe(String url){
		taskManager.setValueAtMainframe(id, 1, url);
	}
	
	/**
	 * �ṩ�޸�mainframe�ϱ�ѡ��capturetype������ʾ�ķ���
	 * @param row ��ѡ������ڵ���
	 * @param column ��ѡ�����ڵ���
	 * @param value ��Ҫ�޸ĳɵ�ֵ
	 */
	protected synchronized void showDownloadTypeOnMainframe(String type){
		taskManager.setValueAtMainframe(id, 2, type);
	}
	
	/**
	 * �ṩ�޸�mainframe�ϱ�ѡ��ThreadName������ʾ�ķ���
	 * @param row ��ѡ������ڵ���
	 * @param column ��ѡ�����ڵ���
	 * @param value ��Ҫ�޸ĳɵ�ֵ
	 */
	protected synchronized void showThreadNameOnMainframe(String name){
		taskManager.setValueAtMainframe(id, 0, name);
	}
	
	/**
	 * ��ͣ����
	 */
	public synchronized void suspend(){
		setStart(false);
	}
	
	/**
	 * �ָ�����
	 */
	public synchronized void restart(){
		setStart(true);
	}
	
	public synchronized void setStart(boolean wait){
		this.wait = wait;
		notifyAll();
	}
	
	@SuppressWarnings("deprecation")
	public synchronized void waitToStart(){
		try{
			if(wait == false) return;
			state = status.SLEEP;
			showStatusOnMainframe(state);
			while(wait == true) wait();
		}catch(Exception e){
			state = status.ABORT;
			showStatusOnMainframe(state);
			Thread.currentThread().interrupt();//����жϵ�ǰ�߳��д��о�
		}
	}
	
	public abstract void run();
}
