/**
 * 该文件定义了单机模式下爬虫抓取网页的任务类
 */
package spider;

import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author keyalin
 *
 */
public class LinkDownloadTask extends DownloadTask{
	public LinkDownloadTask(int id, Manager taskManager){
		super(id, taskManager);
	}	
	
	/**
	 * 定义了下载一个网页及其相关处理的过程
	 * 先到visitedhref里面读取href。
	 * 然后检查href是否在visitedhref里面
	 * 然后抓取html。
	 * 检查html是否为null， null 就continue， 
 	 * 解析网页， 将上面的hrefs添加到todohrefs里面， 将images添加到todoImages里面
	 * 然后将数据保存到相关数据库。
	 * 然后将href添加到visitedHrefs里面。
	 */
	public void run(){
		this.showThreadNameOnMainframe(Thread.currentThread().getName());
		this.showDownloadTypeOnMainframe("page");		
		while(!Thread.currentThread().isInterrupted()){
			waitToStart();
			String href = Manager.todoHrefs.poll();
			if(null == href){//没有任务时，线程休息
				state = status.SLEEP;
				this.showStatusOnMainframe(state);
				try{
					TimeUnit.SECONDS.sleep(2);
				}catch(InterruptedException e){
					state = status.ABORT;
					this.showStatusOnMainframe(state);
					return;
				}
				continue;
			}
			if(Manager.visitedHrefs.contains(href)) continue;
			state = status.FETCH;
			this.showStatusOnMainframe(state);
			this.showDownloadSizeOnMainframe(0);
			this.showUrlOnMainframe(href);
			int timeout = GetSettingValue.getTimeout();
			String html = GetUrlContents.getHtml(href, timeout);
			if(null == html || html.equals("NULL")){
				taskManager.invalidHrefsIncrease(1);
				continue;
			}
			int size = html.length() * 2;
			this.showDownloadSizeOnMainframe(size);
			taskManager.totalSizeIncrease(size);
			RegexParser.urlsExtreact(html, href);
			RegexParser.imagesExtreact(html, href);//已经将images添加到todoImages里面了。
			state = status.SAVE;//保存到数据库
			this.showStatusOnMainframe(state);
			String time = Utility.getFormatTime(Calendar.getInstance());
			PreparedStatement  statement = null;
			try{
				String sql = "insert into pages (url, host, contents, download_date) " +
								"values(?, ?, ?, ?)";
				statement = Manager.conn.prepareStatement(sql);
				statement.setString(1, href);
				statement.setString(2, Utility.getHost(href));
				statement.setString(3, html);
				statement.setString(4, time);
				statement.execute();
				Manager.visitedHrefs.offer(href.intern());
				taskManager.validHrefsIncrease(1); 
				href = null;
				html = null;
				time = null;
//				System.out.println("todohrefs: " + Manager.todoHrefs.size());
//				System.out.println("todoImages: " + Manager.todoImages.size());
			}
			catch(SQLException e){
				System.out.println(e);
			}
			finally{
				try {
					statement.close();
				} catch (Exception e) {
					return;
				}
			}
		}
	}
}
