/**
 * 该文件定义了分布式爬虫系统抓取网页的任务类
 */

package spider;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import spider.DownloadTask.status;

/**
 * @author keyalin
 *
 */
public class NodePageDownloadTask extends DownloadTask{
	
	/**
	 * @param id 任务的标识，用于在capturetable上正确定位
	 * @param taskManager
	 */
	protected NodePageDownloadTask(int id, Manager taskManager) {
		super(id, taskManager);
	}

	/**
	 * 定义了下载一个网页及其相关处理的过程
	 * 先到visitedhref里面读取href。
	 * 然后检查href是否在visitedhref里面， host是否在othervisitedhost里面
	 * 然后抓取html。
	 * 检查html是否超时， 超时则检查host是否在ownvisited里面， 若不在， othervisitedhost添加host， 
	 * 并且将url发送给主机
	 * 若不超时， 解析网页， 将上面的hrefs添加到todohrefs里面， 将images添加到todoImages里面
	 * 然后将数据保存到相关数据库。
	 * 然后将href添加到visitedHrefs里面。 同时检查host是否在ownerhost里面， 若不在添加ownerhost里里面
	 */
	public void run() {
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
			String host = Utility.getHost(href);
			if(DSBData.othersVisitedHost.contains(host)) continue;//查看host是否已经被其他节点抓取过
			if(Manager.visitedHrefs.contains(href)) continue;			
			state = status.FETCH;
			this.showStatusOnMainframe(state);
			this.showDownloadSizeOnMainframe(0);
			this.showUrlOnMainframe(href);
			int timeout = GetSettingValue.getTimeout();
			String html = GetUrlContents.getHtml(href, timeout);
			if(null == html){
				taskManager.invalidHrefsIncrease(1);
				continue;
			}
			if(html.equals("NULL")){//超时处理
				if(DSBData.ownVisitedHost.contains(host)){//若超时但是host在本地
					taskManager.invalidHrefsIncrease(1);
					continue;
				}
				else{
					DSBData.othersVisitedHost.offer(host);//若超时且不在本地就发送给主机
					NodeCom req = new NodeCom();
					try {
						req.setHostReq(DSBData.getNodeIP(), host, false, DSBData.
								getNodePort(), href);
					} catch (NotCallException e) {
						e.printStackTrace();
					}
					Utility.sendRequest(req, DSBData.getHostIP(), DSBData.getHostPort());
					req = null;
					taskManager.invalidHrefsIncrease(1);
					continue;
				}				
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
				statement.setString(2, host);
				statement.setString(3, html);
				statement.setString(4, time);
				statement.execute();
				Manager.visitedHrefs.offer(href.intern());
				if(!DSBData.ownVisitedHost.contains(host)){//若host不在ownervisited表里面， 发送给主机
					NodeCom req = new NodeCom();
					try {
						req.setHostReq(DSBData.getNodeIP(), host, true, DSBData.
								getNodePort(), null);
						Utility.sendRequest(req, DSBData.getHostIP(), DSBData.getHostPort());
						DSBData.ownVisitedHost.offer(host.intern());
						req = null;
					} catch (NotCallException e) {
						e.printStackTrace();
					}					
				}
				taskManager.validHrefsIncrease(1); 
				href = null;
				html = null;
				time = null;
				host = null;
			}
			catch(SQLException e){
				System.out.println(e);
			}
			finally{
				try {
					statement.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
