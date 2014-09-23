/**
 * ���ļ������˷ֲ�ʽ����ϵͳץȡ��ҳ��������
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
	 * @param id ����ı�ʶ��������capturetable����ȷ��λ
	 * @param taskManager
	 */
	protected NodePageDownloadTask(int id, Manager taskManager) {
		super(id, taskManager);
	}

	/**
	 * ����������һ����ҳ������ش���Ĺ���
	 * �ȵ�visitedhref�����ȡhref��
	 * Ȼ����href�Ƿ���visitedhref���棬 host�Ƿ���othervisitedhost����
	 * Ȼ��ץȡhtml��
	 * ���html�Ƿ�ʱ�� ��ʱ����host�Ƿ���ownvisited���棬 �����ڣ� othervisitedhost���host�� 
	 * ���ҽ�url���͸�����
	 * ������ʱ�� ������ҳ�� �������hrefs��ӵ�todohrefs���棬 ��images��ӵ�todoImages����
	 * Ȼ�����ݱ��浽������ݿ⡣
	 * Ȼ��href��ӵ�visitedHrefs���档 ͬʱ���host�Ƿ���ownerhost���棬 ���������ownerhost������
	 */
	public void run() {
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
			String host = Utility.getHost(href);
			if(DSBData.othersVisitedHost.contains(host)) continue;//�鿴host�Ƿ��Ѿ��������ڵ�ץȡ��
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
			if(html.equals("NULL")){//��ʱ����
				if(DSBData.ownVisitedHost.contains(host)){//����ʱ����host�ڱ���
					taskManager.invalidHrefsIncrease(1);
					continue;
				}
				else{
					DSBData.othersVisitedHost.offer(host);//����ʱ�Ҳ��ڱ��ؾͷ��͸�����
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
				statement.setString(2, host);
				statement.setString(3, html);
				statement.setString(4, time);
				statement.execute();
				Manager.visitedHrefs.offer(href.intern());
				if(!DSBData.ownVisitedHost.contains(host)){//��host����ownervisited�����棬 ���͸�����
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
