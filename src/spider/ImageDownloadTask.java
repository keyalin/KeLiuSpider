/**
 * 该文件定义爬虫抓取图片的任务类
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
	 * 定义了下载一个图片及其相关处理的过程
	 * 先到visitedImages里面读取image。
	 * 然后检查image是否在visitedImages里面
	 * 然后抓取图片数据流。
	 * 检查数据流是否为null， null 就continue， 
 	 * 获得图片的的格式和host
	 * 然后将数据保存到相关数据库。
	 * 然后将image添加到visitedImages里面。
	 */
	public void run(){
		this.showThreadNameOnMainframe(Thread.currentThread().getName());
		this.showDownloadTypeOnMainframe("image");		
		while(!Thread.currentThread().isInterrupted()){
			waitToStart();
			String image = Manager.todoImages.poll();
			if(null == image){//没有任务时，线程休息
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
			state = status.SAVE;//保存数据
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
