/**
 * ���ļ���������ץȡͼƬ��������
 */
package spider;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author keyalin
 *
 */
public class ImageDownloadTask extends DownloadTask{
	public ImageDownloadTask(int id, Manager taskManager){
		super(id, taskManager);
	}
	
	
	/**
	 * ����������һ��ͼƬ������ش���Ĺ���
	 * �ȵ�visitedImages�����ȡimage��
	 * Ȼ����image�Ƿ���visitedImages����
	 * Ȼ��ץȡͼƬ��������
	 * ����������Ƿ�Ϊnull�� null ��continue�� 
 	 * ���ͼƬ�ĵĸ�ʽ��host
	 * Ȼ�����ݱ��浽������ݿ⡣
	 * Ȼ��image��ӵ�visitedImages���档
	 */
	public void run(){
		this.showThreadNameOnMainframe(Thread.currentThread().getName());
		this.showDownloadTypeOnMainframe("image");		
		while(!Thread.currentThread().isInterrupted()){
			waitToStart();
			String image = Manager.todoImages.poll();
			if(null == image){//û������ʱ���߳���Ϣ
				state = status.SLEEP;
				this.showStatusOnMainframe(state);
				try{
					TimeUnit.SECONDS.sleep(2);
				}catch(InterruptedException e){
					state = status.ABORT;
					this.showStatusOnMainframe(state);
				}
				continue;
			}
			if(Manager.visitedImages.contains(image)) continue;
			String format = Utility.getImagFormat(image);
			if(null == format) {
				taskManager.invalidImagesIncrease(1);
				continue;
			}
			state = status.FETCH;
			this.showStatusOnMainframe(state);
			this.showUrlOnMainframe(image);
			this.showDownloadSizeOnMainframe(0);
			int timeout = GetSettingValue.getTimeout();
			byte[] bytes = GetUrlContents.getBytes(image, timeout);
			if(null == bytes || 0 == bytes.length){
				taskManager.invalidImagesIncrease(1);
				continue;
			}
			taskManager.totalSizeIncrease(bytes.length);
			this.showDownloadSizeOnMainframe(bytes.length);
			state = status.SAVE;//��������
			String host = Utility.getHost(image);
			String timestamp = Utility.getFormatTime(Calendar.getInstance());
			PreparedStatement  statement = null;
			try{	
				String sql = "insert into images (url, contents, host, " +
						"format, download_date) values(?, ?, ?, ?, ?)";
				statement = Manager.conn.prepareStatement(sql);
				statement.setString(1, image);
				statement.setBytes(2, bytes);
				statement.setString(3, host);
				statement.setString(4, format);
				statement.setString(5, timestamp);
				statement.execute();
				Manager.visitedImages.offer(image.intern());
				taskManager.validImagesIncrease(1);
				image = null;
				bytes = null;
				host = null;
				format = null;
				timestamp = null;
			}catch(SQLException e){
				e.printStackTrace();
			}finally{
				try{
					if(null != statement) statement.close();
				}catch(Exception e){}
			}
		}
	}
}
