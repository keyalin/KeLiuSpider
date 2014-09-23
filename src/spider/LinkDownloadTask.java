/**
 * ���ļ������˵���ģʽ������ץȡ��ҳ��������
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
	 * ����������һ����ҳ������ش���Ĺ���
	 * �ȵ�visitedhref�����ȡhref��
	 * Ȼ����href�Ƿ���visitedhref����
	 * Ȼ��ץȡhtml��
	 * ���html�Ƿ�Ϊnull�� null ��continue�� 
 	 * ������ҳ�� �������hrefs��ӵ�todohrefs���棬 ��images��ӵ�todoImages����
	 * Ȼ�����ݱ��浽������ݿ⡣
	 * Ȼ��href��ӵ�visitedHrefs���档
	 */
	public void run(){
		this.showThreadNameOnMainframe(Thread.currentThread().getName());
		this.showDownloadTypeOnMainframe("page");		
		while(!Thread.currentThread().isInterrupted()){
			waitToStart();
			String href = Manager.todoHrefs.poll();
			if(null == href){//û������ʱ���߳���Ϣ
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
			RegexParser.imagesExtreact(html, href);//�Ѿ���images��ӵ�todoImages�����ˡ�
			state = status.SAVE;//���浽���ݿ�
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
