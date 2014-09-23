/**
 * 该文件定义爬虫下载web资源的模板类DownloadTask
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
	protected boolean wait;//用以控制任务的暂停和恢复
	protected status state;//反映线程的状态
	protected enum status {SLEEP, SAVE, FETCH, ABORT}
	
	protected DownloadTask(int id, Manager taskManager){
		this.id = id;
		this.taskManager = taskManager;
		wait = false;
		state = status.SLEEP;
	}
	
	/**
	 * 修改id的值
	 * @param id
	 */
	public void setID(int id){
		this.id = id;
	}
	
	/**
	 * 提供修改mainframe上表选项数Status据显示的方法
	 * @param row 表选项的所在的行
	 * @param column 表选项所在的列
	 * @param value 将要修改成的值
	 */
	protected synchronized void showStatusOnMainframe(status state){
		taskManager.setValueAtMainframe(id, 4, state.toString());
	}
	
	/**
	 * 提供修改mainframe上表选项ize数据显示的方法
	 * @param row 表选项的所在的行
	 * @param column 表选项所在的列
	 * @param value 将要修改成的值
	 */
	protected synchronized void showDownloadSizeOnMainframe(int size){
		taskManager.setValueAtMainframe(id, 3, Integer.toString(size) + "B");
	}
	
	/**
	 * 提供修改mainframe上表选项Url数据显示的方法
	 * @param row 表选项的所在的行
	 * @param column 表选项所在的列
	 * @param value 将要修改成的值
	 */
	protected synchronized void showUrlOnMainframe(String url){
		taskManager.setValueAtMainframe(id, 1, url);
	}
	
	/**
	 * 提供修改mainframe上表选项capturetype数据显示的方法
	 * @param row 表选项的所在的行
	 * @param column 表选项所在的列
	 * @param value 将要修改成的值
	 */
	protected synchronized void showDownloadTypeOnMainframe(String type){
		taskManager.setValueAtMainframe(id, 2, type);
	}
	
	/**
	 * 提供修改mainframe上表选项ThreadName数据显示的方法
	 * @param row 表选项的所在的行
	 * @param column 表选项所在的列
	 * @param value 将要修改成的值
	 */
	protected synchronized void showThreadNameOnMainframe(String name){
		taskManager.setValueAtMainframe(id, 0, name);
	}
	
	/**
	 * 暂停任务
	 */
	public synchronized void suspend(){
		setStart(false);
	}
	
	/**
	 * 恢复任务
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
			Thread.currentThread().interrupt();//如何中断当前线程有待研究
		}
	}
	
	public abstract void run();
}
