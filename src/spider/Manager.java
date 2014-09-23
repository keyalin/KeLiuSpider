/**
 * ���ļ������˿�������ϵͳ���е�ģ����
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
	protected DownloadTask[] tasks = new DownloadTask[40];//�ǲ������ﱣ����̫������ã� �����ڴ����Ĺ���ע��������
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
	 * ������ץȡ��ҳ�����seeds�� ����ô˺���
	 * @param seeds
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
	 */
	public abstract int startFromSeeds(String[] seeds);
	
	/**
	 * ������ץȡ��ҳ�����history�� ����ô˺���
	 * @param seeds
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
	 */
	public abstract int startFromHistory();

	/**
	 * ��ͣ���е�����
	 */
	
	public void suspend(){
		for(DownloadTask task : tasks){
			if(null != task) task.suspend();
		}
	}
	
	/**
	 * ��������ָ�ִ��
	 */
	public void restart(){
		for(DownloadTask task : tasks){
			if(null != task) task.restart();
		}
	}
	
	/**
	 * ��ֹ�����ִ�У� ���ұ���������ݣ� todohrefs, todoImages, visitedHrefs, visitedImages
	 * @return 0��ʾ���ݱ���ʧ�ܣ� 1��ʾ���ݱ���ɹ�
	 */
	public int stop(){
		for(Future future : futures){
			if(null != future){
				future.cancel(true);
				future = null;
			}
		}
		for(DownloadTask task : tasks){
			if(null != task)task = null;//��һ���Ƿ��Ҫ�� ���ͷ��ڴ棬����Ҫ�᲻��Ӱ������ִ��
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
	 * ��ʼ��ץȡ���Ϊseed��taskManager�������е�����
	 * @return  0��ʾʧ�ܣ� 1��ʾ�ɹ�
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
	 * ��ʼ��ץȡ���Ϊhistory��taskManager������
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
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
	 * ��ʼ��todoHrefs
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
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
	 * ��ʼ��todoImages
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
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
	 * ��ʼ��visitedhrefs
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
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
	 * ��ʼ��visitedImages
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
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
	 * ����todoHrefs, todoImages, visitedHrefs, visitedImages
	 * @return 0��ʾ�ɹ��� 1��ʾʧ��
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
	 * ����todoHrefs, ���ļ�ǰ�� ���������д�� ������֮��������ֻ��
	 * @return 0��ʾ����ʧ�ܣ� 1��ʾ����ɹ�
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
	 * ����todoImages, ���ļ�ǰ�� ���������д�� ������֮��������ֻ��
	 * @return 0��ʾ����ʧ�ܣ� 1��ʾ����ɹ�
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
	 * ����visitedHrefs ���ļ�ǰ�� ���������д�� ������֮��������ֻ��
	 * @return 0��ʾ����ʧ�ܣ� 1��ʾ����ɹ�
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
	 * ���嶯̬��������
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
	 * �ṩ�޸�totalsize�Ľӿ�
	 * @param i totalsize������
	 */
	public synchronized void totalSizeIncrease(int i){
		totalSize += i;
	}
	
	/**
	 * �ṩ�޸�validHrefs�Ľӿ�
	 * @param i validHrefs������
	 */
	public synchronized void validHrefsIncrease(int i){
		validHrefs += i;
	}
	
	/**
	 * �ṩ�޸�invalidHrefs�Ľӿ�
	 * @param i invalidHrefs������
	 */
	public synchronized void invalidHrefsIncrease(int i){
		invalidHrefs += i;
	}
	
	/**
	 * �ṩ�޸�validImages�Ľӿ�
	 * @param i validImages������
	 */
	public synchronized void validImagesIncrease(int i){
		validImages += i;
	}
	
	/**
	 * �ṩ�޸�invalidImages�Ľӿ�
	 * @param i invalidImages������
	 */
	public synchronized void invalidImagesIncrease(int i){
		invalidImages += i;
	}
	
	/**
	 * �����ص�һЩ����ָ������ÿ��5��չʾ��mainframe�ϡ� 
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
	 * �����޸�mainframe capturetable��ѡ��Ľӿ�
	 * @param row ��ѡ�����ڵ���
	 * @param column ��ѡ�����ڵĵ���
	 * @param value ��ѡ�Ҫ���޸ĳɵ�����
	 */
	public void setValueAtMainframe(int row, int column, String value){
		mainframe.setValueAtCaptureTable(row, column, value);
	}
	
	/**
	 * ���˺���������ʱ��imagecount��pagecountֻ�п���һ�������仯
	 * ע�⴦����� Ҫ�޸�imagecount��pagecount��ֵ
	 */
	public abstract  void changeUsage();

}
