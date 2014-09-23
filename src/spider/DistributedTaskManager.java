/**
 * ���ļ�������keliuspdierѡ���˷ֲ�ʽģʽ���̹߳�����
 */

package spider;

import java.util.concurrent.Executors;

/**
 * @author keyalin
 *
 */
public class DistributedTaskManager extends Manager{
	DistributedTaskManager(MainFrame mainframe){
		super(mainframe);
	}

	
	/**
	 * ������ץȡ��ҳ�����history�� ����ô˺���
	 * @param seeds
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
	 */
	public int startFromHistory() {
		conn = DatabaseManager.databaseConnection(databaseName, user, password);
		if(null == conn){
			Utility.showMessage("connect to the database failed");
			return 0;
		}
		if(1 != seedDataIntial()) return 0;
		exec = Executors.newCachedThreadPool();
		for(int i = 0; i < pagecount; i++){
			NodePageDownloadTask task = new NodePageDownloadTask(i, this);
			futures[i] = exec.submit(task);//this�᲻�ᵼ���ڴ�й¶
			tasks[i] = task;
		}
		for(int i = 0; i < imagecount; i ++){
			NodePageDownloadTask task = new NodePageDownloadTask(i + pagecount, this);
			futures[i + pagecount]= exec.submit(task);//this�᲻�ᵼ���ڴ�й¶
			tasks[i + pagecount] = task;
		}
		exec.execute(new DisplayDataOnMainFrame());
		exec.execute(new SavePeriodTask());
		return 1;
	}
	
	/**
	 * ������ץȡ��ҳ�����seeds�� ����ô˺���
	 * @param seeds
	 * @return 0��ʾʧ�ܣ� 1��ʾ�ɹ�
	 */
	public int startFromSeeds(String[] seeds) {
		conn = DatabaseManager.databaseConnection(databaseName, user, password);
		if(null == conn){
			Utility.showMessage("connect to the database failed");
			return 0;
		}
		if(1 != historyDataIntial()) return  0;
		exec = Executors.newCachedThreadPool();
		for(String str : seeds){
			if(null != str){
				todoHrefs.offer(str);
			}
		}
		for(int i = 0; i < pagecount; i++){
			NodePageDownloadTask task = new NodePageDownloadTask(i, this);
			futures[i] = exec.submit(task);//this�᲻�ᵼ���ڴ�й¶
			tasks[i] = task;
		}
		for(int i = 0; i < imagecount; i ++){
			NodeImageDownloadTask task = new NodeImageDownloadTask(i + pagecount, this);
			futures[i + pagecount]= exec.submit(task);//this�᲻�ᵼ���ڴ�й¶
			tasks[i + pagecount] = task;
		}
		exec.execute(new DisplayDataOnMainFrame());
		exec.execute(new SavePeriodTask());
		return 1;
	}

	/**
	 * ���˺���������ʱ��imagecount��pagecountֻ�п���һ�������仯
	 * ע�⴦����� Ҫ�޸�imagecount��pagecount��ֵ
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
	 * ��pagethread���������ı䣬 ϵͳ��Ҫ���еĹ���
	 * @param minux �ñ����ľ���ֵ
	 * @param add ���������٣� fasle�� �������ӣ� true
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
				tasks[i] = new NodePageDownloadTask(i, this);
				futures[i] = exec.submit(tasks[i]);
			}
			
		}
	}

	/**
	 * ��imagethread���������ı䣬 ϵͳ��Ҫ���еĹ���
	 * @param minux �ñ����ľ���ֵ
	 * @param add ���������٣� fasle�� �������ӣ� true
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
				tasks[i] = new NodeImageDownloadTask(i, this);
				futures[i] = exec.submit(tasks[i]);
			}
		}
	}
}
