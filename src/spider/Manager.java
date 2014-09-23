/**
 * 该文件定义了控制爬虫系统运行的模板类
 */
package spider;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.*;

/**
 * @author keyalin
 *
 */
public abstract class Manager {
	public static LinksQueue<String> todoHrefs = null;
	public static ImagesQueue<String> todoImages = null;
	public static VisitedQueue<String> visitedHrefs = null;
	public static VisitedQueue<String> visitedImages = null;
	protected MainFrame mainframe = null;
	protected int pagecount;
	protected int imagecount;
	protected Future[] futures = new Future[40];
	protected ExecutorService exec = null;
	protected DownloadTask[] tasks = new DownloadTask[40];//是不是这里保存了太多的引用， 所以内存消耗过大，注意任务复制
	public static Connection conn = null;
	protected String databaseName;
	protected String user;
	protected String password;
	
	public Manager(MainFrame mainframe){
		this.mainframe = mainframe;
		pagecount = GetSettingValue.getPageThreadCount();
		imagecount = GetSettingValue.getImageThreadCount();
		databaseName = GetSettingValue.getDatabaseName();
		user = GetSettingValue.getUserName();
		password = GetSettingValue.getPassword();
	}
	
	/**
	 * 若爬虫抓取网页起点是seeds， 则调用此函数
	 * @param seeds
	 * @return 0表示失败， 1表示成功
	 */
	public abstract int startFromSeeds(String[] seeds);
	
	/**
	 * 若爬虫抓取网页起点是history， 则调用此函数
	 * @param seeds
	 * @return 0表示失败， 1表示成功
	 */
	public abstract int startFromHistory();

	/**
	 * 暂停所有的任务
	 */
	
	public void suspend(){
		for(DownloadTask task : tasks){
			if(null != task) task.suspend();
		}
	}
	
	/**
	 * 所有任务恢复执行
	 */
	public void restart(){
		for(DownloadTask task : tasks){
			if(null != task) task.restart();
		}
	}
	
	/**
	 * 终止任务的执行， 并且保存相关数据： todohrefs, todoImages, visitedHrefs, visitedImages
	 * @return 0表示数据保存失败， 1表示数据保存成功
	 */
	public int stop(){
		for(Future future : futures){
			if(null != future){
				future.cancel(true);
				future = null;
			}
		}
		for(DownloadTask task : tasks){
			if(null != task)task = null;//这一步是否必要， 以释放内存，若必要会不会影响程序的执行
		}
		exec.shutdownNow();
		try {
			conn.close();
		} catch (SQLException e) {
			Utility.showMessage("Database connection close fialed");
			e.printStackTrace();
		}
		
		if(0 == dataSave()) return 0;
		else return 1;
	}	
	
	
	/**
	 * 初始化抓取起点为seed的taskManager里面所有的数据
	 * @return  0表示失败， 1表示成功
	 */
	protected int seedDataIntial(){						
		todoHrefs = new LinksQueue<String>();
		todoImages = new ImagesQueue<String>();
		visitedHrefs = new VisitedQueue<String>();
		visitedImages = new VisitedQueue<String>();
		pagecount = GetSettingValue.getPageThreadCount();
		imagecount = GetSettingValue.getImageThreadCount();
		return 1;
	}
	
	/**
	 * 初始化抓取起点为history的taskManager的数据
	 * @return 0表示失败， 1表示成功
	 */
	protected int historyDataIntial(){
		if(1 == readTodoHrefs() && 1 == readTodoImages() && 
				1 == readVisitedHrefs() && 1 == readVisitedImages()){
			pagecount = GetSettingValue.getPageThreadCount();
			imagecount = GetSettingValue.getImageThreadCount();
			return 1;
		}
		else {
			Utility.showMessage("dataitial failed");
			return 0;
		}
	}

	
	/**
	 * 初始化todoHrefs
	 * @return 0表示失败， 1表示成功
	 */
	protected int readTodoHrefs(){
		File todoHrefsFile = new File("todoHrefs");
		ObjectInputStream inTodoHrefs = null;
		if(todoHrefsFile.exists()){
			try {
				inTodoHrefs = new ObjectInputStream(new FileInputStream(todoHrefsFile));
				try {
					todoHrefs = (LinksQueue<String>) inTodoHrefs.readObject();
				} catch (ClassNotFoundException e) {
					return 0;
				}
			} 
			catch (FileNotFoundException e) {
				Utility.showMessage("error");
				return 0;
			} 
			catch (EOFException e) {
				todoHrefs = new LinksQueue<String>();
				return 1;
			}
			catch(IOException e){
				return 0;
			}finally{
				try {
					inTodoHrefs.close();
				} catch (Exception e) {}
			} 			
		}
		else {
			try {
				todoHrefsFile.createNewFile();
			} catch (IOException e) {
				Utility.showMessage("create file todoHrefs failed");
				return 0;
			}
			todoHrefs = new LinksQueue<String>();
		}
		return 1;
	}
		
	
	/**
	 * 初始化todoImages
	 * @return 0表示失败， 1表示成功
	 */
	protected int readTodoImages(){
		File todoImagesFile = new File("todoImages");
		ObjectInputStream inTodoImages = null;
		if(todoImagesFile.exists()){
			try {
				inTodoImages = new ObjectInputStream(new FileInputStream(todoImagesFile));
				try {
					todoImages = (ImagesQueue<String>) inTodoImages.readObject();
				} catch (ClassNotFoundException e) {
					return 0;
				}
			} 
			catch (FileNotFoundException e) {
				Utility.showMessage("error");
				return 0;
			} 
			catch (EOFException e) {
				todoImages = new ImagesQueue<String>();				
			}
			catch(IOException e){
				return 0;
			}finally{
				try {
					inTodoImages.close();
				} catch (Exception e) {}
			} 			
		}
		else {
			try {
				todoImagesFile.createNewFile();
			} catch (IOException e) {
				Utility.showMessage("create file todoImages failed");
				return 0;
			}
			todoImages = new ImagesQueue<String>();
		}
		return 1;
	}
	
	
	/**
	 * 初始化visitedhrefs
	 * @return 0表示失败， 1表示成功
	 */	
	protected int readVisitedHrefs(){
		File visitedHrefsFile = new File("visitedHrefs");
		ObjectInputStream inVisitedUrls = null;
		if(visitedHrefsFile.exists()){
			try {
				inVisitedUrls = new ObjectInputStream(new FileInputStream(visitedHrefsFile));
				try {
					visitedHrefs = (VisitedQueue<String>) inVisitedUrls.readObject();
					return 1;
				} catch (ClassNotFoundException e) {
					return 0;
				}
			} 
			catch (FileNotFoundException e) {
				Utility.showMessage("error");
				return 0;
			} 
			catch (EOFException e) {
				visitedHrefs = new VisitedQueue<String>();	
				return 1;
			}
			catch(IOException e){
				return 0;
			}finally{
				try {
					inVisitedUrls.close();
				} catch (Exception e) {}
			} 			
		}
		else {
			try {
				visitedHrefsFile.createNewFile();
			} catch (IOException e) {
				Utility.showMessage("create file todoHrefs failed");
				return 0;
			}
			visitedHrefs = new VisitedQueue<String>();
			return 1;
		}		
	}
	
	
	/**
	 * 初始化visitedImages
	 * @return 0表示失败， 1表示成功
	 */
	protected int readVisitedImages(){
		File visitedImagesFile = new File("visitedImages");
		ObjectInputStream inVisitedImages = null;
		if(visitedImagesFile.exists()){
			try {
				inVisitedImages = new ObjectInputStream(new FileInputStream(visitedImagesFile));
				try {
					visitedImages = (VisitedQueue<String>) inVisitedImages.readObject();
					return 1;
				} catch (ClassNotFoundException e) {
					return 0;
				}
			} 
			catch (FileNotFoundException e) {
				Utility.showMessage("error");
				return 0;
			} 
			catch (EOFException e) {
				visitedImages = new VisitedQueue<String>();	
				return 1;
			}
			catch(IOException e){
				return 0;
			}finally{
				try {
					inVisitedImages.close();
				} catch (Exception e) {}
			} 			
		}
		else {
			try {
				visitedImagesFile.createNewFile();
			} catch (IOException e) {
				Utility.showMessage("create file visitedImages failed");
				return 0;
			}
			visitedImages = new VisitedQueue<String>();
			return 1;
		}
	}

	/**
	 * 保存todoHrefs, todoImages, visitedHrefs, visitedImages
	 * @return 0表示成功， 1表示失败
	 */
	public synchronized int dataSave(){
		if(1 == saveTodoHrefs() && 1 == saveTodoImages()
				&& 1 == saveVisitedHrefs() && 1 == saveVisitedImages()){
			return 1;
		}
		else {
			Utility.showMessage("save data failed");
			return 0;
		}
	}

	/**
	 * 保存todoHrefs, 打开文件前， 先设置其可写。 保存完之后设置其只读
	 * @return 0表示保存失败， 1表示保存成功
	 */
	public synchronized int saveTodoHrefs(){
		ObjectOutputStream out = null;
		File file = new File("todoHrefs");
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException e){
				Utility.showMessage("create file todoHrefs failed");
				return 0;
			}
		}
		try{
			file.setExecutable(true);
			file.setWritable(true);
			out = new ObjectOutputStream(new FileOutputStream(file));
		}catch(IOException e){
			Utility.showMessage("save todoHrefs failed");
			return 0;
		}
		try{
			out.writeObject(todoHrefs);
			return 1;
		}catch(IOException e){
			Utility.showMessage("save todoHrefs failed");
			return 0;
		}finally{
			try{
				file.setReadOnly();
				out.close();
			}catch(Exception e){}
		}
	}
		
	
	/**
	 * 保存todoImages, 打开文件前， 先设置其可写。 保存完之后设置其只读
	 * @return 0表示保存失败， 1表示保存成功
	 */
	public synchronized int saveTodoImages(){
		File file = new File("todoImages");
		ObjectOutputStream out = null;
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException e){
				Utility.showMessage("create file todoImages failed");
				return 0;
			}
		}
		try{
			file.setExecutable(true);
			file.setWritable(true);
			out = new ObjectOutputStream(new FileOutputStream(file));
		}catch(IOException e){
			Utility.showMessage("save todoImages failed");
			return 0;
		}
		try{
			out.writeObject(todoImages);
			return 1;
		}catch(IOException e){
			Utility.showMessage("save todoImages failed");
			return 0;
		}finally{
			try{
				file.setReadOnly();
				out.close();
			}catch(Exception e){}
		}
	}

	/**
	 * 保存visitedHrefs 打开文件前， 先设置其可写。 保存完之后设置其只读
	 * @return 0表示保存失败， 1表示保存成功
	 */
	public synchronized int saveVisitedHrefs(){
		File file = new File("visitedHrefs");
		ObjectOutputStream out = null;
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException e){
				Utility.showMessage("create file visitedHrefs failed");
				return 0;
			}
		}
		try{
			file.setExecutable(true);
			file.setWritable(true);
			out = new ObjectOutputStream(new FileOutputStream(file));
		}catch(IOException e){
			Utility.showMessage("save visitedHrefs failed");
			return 0;
		}
		try{
			out.writeObject(visitedHrefs);
			return 1;
		}catch(IOException e){
			Utility.showMessage("save visitedHrefs failed");
			return 0;
		}finally{
			try{
				file.setReadOnly();
				out.close();
			}catch(Exception e){}
		}
	}
	
	public synchronized int saveVisitedImages(){
		File file = new File("visitedImages");
		ObjectOutputStream out = null;
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException e){
				Utility.showMessage("create file visitedImages failed");
				return 0;
			}
		}
		try{
			file.setExecutable(true);
			file.setWritable(true);
			out = new ObjectOutputStream(new FileOutputStream(file));
		}catch(IOException e){
			Utility.showMessage("save visitedImages failed");
			return 0;
		}
		try{
			out.writeObject(visitedImages);
			return 1;
		}catch(IOException e){
			Utility.showMessage("save visitedImages failed");
			return 0;
		}finally{
			try{
				file.setReadOnly();
				out.close();
			}catch(Exception e){}
		}
	}

	/**
	 * 定义动态保存任务
	 * @author keyalin
	 *
	 */
	class SavePeriodTask implements Runnable{
		public void run(){
			while(!Thread.currentThread().isInterrupted()){
				long savePeriod = GetSettingValue.getSavePeriod();
				try{
					TimeUnit.SECONDS.sleep(savePeriod);
					dataSave();
				}catch(InterruptedException e){
					return;
				}
			}
		}
	}
	
	
	protected double speed = 0.00;
	protected long totalSize = 0;
	protected String timeCost = "0dates0hours0minitus0seconds";
	protected int validHrefs = 0;
	protected int invalidHrefs = 0;
	protected int validImages = 0;
	protected int invalidImages = 0;
	protected double memoryUsage = 0.00;
	
	/**
	 * 提供修改totalsize的接口
	 * @param i totalsize的增量
	 */
	public synchronized void totalSizeIncrease(int i){
		totalSize += i;
	}
	
	/**
	 * 提供修改validHrefs的接口
	 * @param i validHrefs的增量
	 */
	public synchronized void validHrefsIncrease(int i){
		validHrefs += i;
	}
	
	/**
	 * 提供修改invalidHrefs的接口
	 * @param i invalidHrefs的增量
	 */
	public synchronized void invalidHrefsIncrease(int i){
		invalidHrefs += i;
	}
	
	/**
	 * 提供修改validImages的接口
	 * @param i validImages的增量
	 */
	public synchronized void validImagesIncrease(int i){
		validImages += i;
	}
	
	/**
	 * 提供修改invalidImages的接口
	 * @param i invalidImages的增量
	 */
	public synchronized void invalidImagesIncrease(int i){
		invalidImages += i;
	}
	
	/**
	 * 将下载的一些性能指标数据每隔5秒展示在mainframe上。 
	 * @author keyalin
	 *
	 */
	class DisplayDataOnMainFrame implements Runnable{
		public void run(){
			Calendar timestamp = Calendar.getInstance();
			long tempSize = 0;
			while(!Thread.currentThread().isInterrupted()){
				try{
					TimeUnit.SECONDS.sleep(5);
				}catch(InterruptedException e){
					return;
				}
				timeCost = Utility.getTimeInterval(timestamp, Calendar.getInstance());
				speed = 1.0 * (totalSize - tempSize)/5000;
				tempSize = totalSize;
				memoryUsage = 1.0 * (Runtime.getRuntime().totalMemory() - 
								Runtime.getRuntime().freeMemory())/1000000;
				mainframe.setTimeCostLabelText("TimeCost: " + timeCost);
				mainframe.setSpeedLabelText("Speed: " + Utility.format(speed) + "KB/S");
				mainframe.setTotalSizeText("TotalSize: " + Long.toString(totalSize/1000000) + "MB");
				mainframe.setMemoryUsageLabelText("MemoryUsage: " + Utility.format(memoryUsage) + "MB");
				mainframe.setValidImagesLabelText("ValidImages: " + Integer.toString(validImages));
				mainframe.setInvalidImagesLabelText("InvalidImages: " + Integer.toString(invalidImages));
				mainframe.setValidUrlsLabelText("ValidHrefs: " + Integer.toString(validHrefs));
				mainframe.setInvalidUrlsLabelText("InvalidHrefs: " + Integer.toString(invalidHrefs));
			}
		}
	}

	/**
	 * 定义修改mainframe capturetable表选项的接口
	 * @param row 表选项所在的行
	 * @param column 表选项所在的的列
	 * @param value 表选项将要被修改成的数据
	 */
	public void setValueAtMainframe(int row, int column, String value){
		mainframe.setValueAtCaptureTable(row, column, value);
	}
	
	/**
	 * 当此函数被调用时，imagecount和pagecount只有可能一个发生变化
	 * 注意处理完后， 要修改imagecount和pagecount的值
	 */
	public abstract  void changeUsage();

}
