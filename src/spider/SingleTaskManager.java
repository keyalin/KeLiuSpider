/**
 * 定义单机模式下控制爬虫运行的类
 */
package spider;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.*;

import spider.Manager.DisplayDataOnMainFrame;
import spider.Manager.SavePeriodTask;

/**
 * 要不要定义一个静态的conn？？
 * @author keyalin
 *
 */
public class SingleTaskManager extends Manager {
	SingleTaskManager(MainFrame mainframe){
		super(mainframe);
	}
	
	/**
	 * 若爬虫抓取网页起点是seeds， 则调用此函数
	 * @param seeds
	 * @return 0表示失败， 1表示成功
	 */
	public int startFromSeeds(String[] seeds){
		conn = DatabaseManager.databaseConnection(databaseName, user, password);
		if(null == conn){
			Utility.showMessage("connect to the database failed");
			return 0;
		}
		if(1 != seedDataIntial()) return  0;
		exec = Executors.newCachedThreadPool();
		for(String str : seeds){
			if(null != str){
				todoHrefs.offer(str);
			}
		}
		for(int i = 0; i < pagecount; i++){
			LinkDownloadTask task = new LinkDownloadTask(i, this);
			futures[i] = exec.submit(task);//this会不会导致内存泄露
			tasks[i] = task;
		}
		for(int i = 0; i < imagecount; i ++){
			ImageDownloadTask task = new ImageDownloadTask(i + pagecount, this);
			futures[i + pagecount]= exec.submit(task);//this会不会导致内存泄露
			tasks[i + pagecount] = task;
		}
		exec.execute(new DisplayDataOnMainFrame());
		exec.execute(new SavePeriodTask());
		return 1;
	}

	/**
	 * 若爬虫抓取网页起点是history， 则调用此函数
	 * @param seeds
	 * @return 0表示失败， 1表示成功
	 */
	public int startFromHistory(){
		conn = DatabaseManager.databaseConnection(databaseName, user, password);
		if(null == conn){
			Utility.showMessage("connect to the database failed");
			return 0;
		}
		if(1 != historyDataIntial()) return 0;
		exec = Executors.newCachedThreadPool();
		for(int i = 0; i < pagecount; i++){
			LinkDownloadTask task = new LinkDownloadTask(i, this);
			futures[i] = exec.submit(task);//this会不会导致内存泄露
			tasks[i] = task;
		}
		for(int i = 0; i < imagecount; i ++){
			ImageDownloadTask task = new ImageDownloadTask(i + pagecount, this);
			futures[i + pagecount]= exec.submit(task);//this会不会导致内存泄露
			tasks[i + pagecount] = task;
		}
		exec.execute(new DisplayDataOnMainFrame());
		exec.execute(new SavePeriodTask());
		return 1;
	}

	/**
	 * 当此函数被调用时，imagecount和pagecount只有可能一个发生变化
	 * 注意处理完后， 要修改imagecount和pagecount的值
	 */
	public synchronized void changeUsage(){
		int page = GetSettingValue.getPageThreadCount();
		int image = GetSettingValue.getImageThreadCount();
		if(pagecount == page && image == imagecount) return;
		if(image == imagecount){
			boolean add = page > pagecount ? true : false;
			changeCaptureTableWhenPagecountChange(page - pagecount, add);
			pagecount = page;
		}
		else{
			boolean add = image > imagecount ? true : false;
			changeCaptureTableWhenImagecountChange(image - imagecount, add);
			imagecount = image;
		}
	}
	
	/**
	 * 当pagethread数量发生改变， 系统所要进行的工作
	 * @param minux 该变量的绝对值
	 * @param add 若数量减少， fasle， 数量增加， true
	 */
	protected synchronized void changeCaptureTableWhenPagecountChange(int minux, boolean add){
		if(false == add){
			minux = - minux;
			for(int i = minux; i > 0; i--){
				futures[pagecount - i].cancel(true);
			}
			for(int i = pagecount - minux; i < pagecount + imagecount - minux; i++){
				for(int j = 0; j < 5; j++){
					mainframe.setValueAtCaptureTable(i, j, mainframe.
								getValueAtCaptureTable(i + minux, j));
				}
				futures[i] = futures[i + minux];
				tasks[i] = tasks[i + minux];
				tasks[i].setID(i);
			}
			for(int i = pagecount + imagecount - minux; i < pagecount + imagecount; i++){
				for(int j = 0; j < 5; j++){
					mainframe.setValueAtCaptureTable(i, j, null);					
				}
				tasks[i] = null;
				futures[i] = null;
			}
		}
		else {
			for(int i = pagecount + imagecount + minux - 1; i >  pagecount + minux - 1; i--){
				for(int j = 0; j < 5; j++){
					mainframe.setValueAtCaptureTable(i, j, mainframe.
								getValueAtCaptureTable(i - minux, j));
					futures[i] = futures[i - minux];
					tasks[i] = tasks[i - minux];
					tasks[i].setID(i);
				}
			}
			for(int i = pagecount; i < pagecount + minux; i++){
				for(int j = 0; j < 5; j++){
					mainframe.setValueAtCaptureTable(i, j, null);
				}
				tasks[i] = new LinkDownloadTask(i, this);
				futures[i] = exec.submit(tasks[i]);
			}
			
		}
		//
	}

	/**
	 * 当imagethread数量发生改变， 系统所要进行的工作
	 * @param minux 该变量的绝对值
	 * @param add 若数量减少， fasle， 数量增加， true
	 */
	protected synchronized void changeCaptureTableWhenImagecountChange(int minux, boolean add){
		if(false == add){
			minux = - minux;
			for(int i = pagecount + imagecount - minux; i < pagecount + imagecount; i++){
				for(int j = 0; j < 5; j++){
					mainframe.setValueAtCaptureTable(i, j, null);
				}
				futures[i].cancel(true);
				futures[i] = null;
				tasks[i] = null;
			}
		}
		else {
			for(int i = pagecount + imagecount; i < pagecount + imagecount + minux; i++){
				for(int j = 0; j < 5; j++){
					mainframe.setValueAtCaptureTable(i, j, null);
				}
				tasks[i] = new ImageDownloadTask(i, this);
				futures[i] = exec.submit(tasks[i]);
			}
		}
	}
}
