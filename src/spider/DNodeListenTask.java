package spider;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.swing.JOptionPane;

/**
 * �����ӽڵ�ļ���������
 * @author keyalin
 *
 */
class DNodeListenTask extends ListenTask{
	Thread checkThread = null;//�ӽڵ㶨�ڼ�����ڵ��Ƿ���ڣ����ӽڵ����ϵͳ��������
	DNodeListenTask(String ip, int port, MainFrame mainframe){
		super(ip, port, mainframe);
		StringBuilder sb = new StringBuilder();
		sb.append("Type: node ");
		sb.append("IP: ");
		sb.append(ip);
		sb.append(" port: ");
		sb.append(port);
		this.mainframe.setTitle(sb.toString().intern());
	}
	public void run(){
			//Ԥ��ȡ����
		if(0 == dataIntial()) {
			showMessage("data intial failed");
			mainframe.abortListenTask();//��ֹ����
			return;
		}
		Thread datasave = new Thread(new DataSaveTask());//�������ڱ�����������
		datasave.setDaemon(true);
		datasave.start();
		ExecutorService exec = Executors.newCachedThreadPool();
		try{
			server = new ServerSocket(port);
		}catch(Exception e){
			e.printStackTrace();
			showMessage("something wrong " + e.toString());
			mainframe.abortListenTask();//��ֹ����
			return;
		}
		while(!server.isClosed()){
			Socket socket = null;
			try{
				socket = server.accept();
				exec.execute(new socketTask(socket));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * �����ӽڵ㴦��������������
	 */
	class socketTask implements Runnable{
		Socket socket;
		socketTask(Socket socket){
			this.socket = socket;
		}
		public void run(){
			ObjectInputStream in = null;
			try{
				in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				MainUnitCom mainUnitReq = (MainUnitCom)in.readObject();
				
				if(MainUnitCom.ReqType.ADDNODE == mainUnitReq.getReqType()){
					MainUnitCom.AddNodeReq addNodeReq = mainUnitReq.getAddNodeReq();
					if(0 == processAddNodeReq(addNodeReq)){
						showMessage("Join the host" + addNodeReq.ip + " failed");
						return;
					}
					else{
						showMessage("Join the host" + addNodeReq.ip+ " succeed!");
						checkThread = new Thread(new CheckHost());//�������ڼ�������Ƿ���ڵ�����
						checkThread.setDaemon(true);
						checkThread.start();
						return;
					}
				}
				else if(MainUnitCom.ReqType.DELETENODE == mainUnitReq.getReqType()){
					MainUnitCom.DeleteNodeReq deleteReq = mainUnitReq.getDeleteNodeReq();
					processDeleteReq(deleteReq);
				}
				else if(MainUnitCom.ReqType.CHECK == mainUnitReq.getReqType()) {
					/*
					 * �򵥶����� ��������
					 */
					return;
				}
				else if(MainUnitCom.ReqType.HOST == mainUnitReq.getReqType()){
					MainUnitCom.HostReq hostReq = mainUnitReq.getHostReq();
					processHostReq(hostReq);
					return;
				}
				else if(MainUnitCom.ReqType.RESJOIN == mainUnitReq.getReqType()){
					MainUnitCom.ResJoinReq resJoinReq = mainUnitReq.getResJoinReq();
					/*
					 * ��ʾ�û����ӳɹ�����
					 */
					processResJoinReq(resJoinReq);
				}
				else return;
				
			}catch(Exception e){
				e.printStackTrace();
			}
			finally{
				try{
					in.close();
					socket.close();
				}
				catch(Exception e){}
			}
		}
	}
	
	/**
	 * ��ʼ��dnodelistentask������
	 * @return ��ʼ���ɹ�����1�� ʧ�ܷ���0
	 */
	private int dataIntial(){
		if(1 == readOwnVisitedHostData() && 1 == readOthersVisitedHostData()){
			return 1;
		}
		else return 0;
	}
	/**
	 * ��ȡ����ownVisitedHost
	 * @return 1��ʾ�ɹ��� 0��ʾʧ��
	 */
	private int readOwnVisitedHostData(){
		File ownVisitedHost = new File("ownVisitedHost");
		if(!ownVisitedHost.exists()){
			try {
				ownVisitedHost.createNewFile();	
				ownVisitedHost.setReadOnly();
			} catch (IOException e) {
				showMessage("create file ownVisitedHost failed");
				return 0;
			}
			DSBData.ownVisitedHost = new VisitedQueue<String>();
			return 1;
		}
		if(0 == ownVisitedHost.length()){
			DSBData.ownVisitedHost = new VisitedQueue<String>();
			return 1;
		}
		ObjectInputStream in = null;
		try{
			ownVisitedHost.setExecutable(true);
			ownVisitedHost.setReadable(true);
			ownVisitedHost.setWritable(true);
			in = new ObjectInputStream(new FileInputStream(ownVisitedHost));
			DSBData.ownVisitedHost = (VisitedQueue<String>) in.readObject();
			return 1;
		}catch(IOException e){
			showMessage("read file visitedHost failed");
			return 0;
		}catch(ClassNotFoundException e){
			showMessage("the file visitedHost has been dirty");
			return 0;
		}
		finally {
			try{
				ownVisitedHost.setReadOnly();
				in.close();
			}
			catch(Exception e){}
		}
	}
	
	/**
	 * ��ȡ����othersVisitedHost
	 * @return 1��ʾ�ɹ��� 0��ʾʧ��
	 */
	private int readOthersVisitedHostData(){
		File othersVisitedHost = new File("othersVisitedHost");
		if(!othersVisitedHost.exists()){
			try {
				othersVisitedHost.createNewFile();	
				othersVisitedHost.setReadOnly();
			} catch (IOException e) {
				showMessage("create file othersVisitedHost failed");
				return 0;
			}
			DSBData.othersVisitedHost = new VisitedQueue<String>();
			return 1;
		}
		if(0 == othersVisitedHost.length()) {
			DSBData.othersVisitedHost= new VisitedQueue<String>();
			return 1;
		}
		ObjectInputStream in = null;
		try{
			othersVisitedHost.setExecutable(true);
			othersVisitedHost.setReadable(true);
			othersVisitedHost.setWritable(true);
			in = new ObjectInputStream(new FileInputStream(othersVisitedHost));
			DSBData.othersVisitedHost = (VisitedQueue<String>) in.readObject();
			return 1;
		}catch(IOException e){
			showMessage("read file othersVisitedHost failed");
			return 0;
		}catch(ClassNotFoundException e){
			showMessage("the file othersVisitedHost has been dirty");
			return 0;
		}
		finally {
			try{
				othersVisitedHost.setReadOnly();
				in.close();
			}
			catch(Exception e){}
		}
	}
	
	/**
	 * �ӽڵ㴦���ܻ�����addNode����, �ظ�һ��ResAddNodeReq
	 * @param addNodeReq �յ���addNode����
	 * @return 1��ʾ�ظ��ɹ��� 0��ʾʧ��
	 */
	public int processAddNodeReq(MainUnitCom.AddNodeReq addNodeReq){
		String ip = addNodeReq.ip;
		int port = addNodeReq.port;
		String seed = addNodeReq.seed;
		String message = "the host " + ip + " want us to join in, do you agree?";
		int i = JOptionPane.showConfirmDialog(null, message, "hey!", JOptionPane.YES_NO_OPTION);
		boolean history;
		boolean agree;
		if(JOptionPane.NO_OPTION == i) {
			agree = false;//�û���ͬ��join in
		}
		else {
			agree = true;
		}
		if(null == seed) {
			history = true;
		}
		else {
			history = false;
		}
		NodeCom resAddNodeReq = new NodeCom();
		
		try{
			resAddNodeReq.setResAddNodeReq(this.ip, this.port, agree, history);//��ip������ip�����Լ���ip
		}catch(NotCallException e){
			return 0;
		}
		if(0 == Utility.sendRequest(resAddNodeReq, ip, port)){
			return 0;
		}
		else {
			if(null != seed){
				mainframe.seedTextField.setText(seed);
			}
			DSBData.setHostIP(ip);
			DSBData.setHostPort(port);//��������ip�Ͷ˿�
			return 1;
		}
	}
	
	/**
	 * �ӽڵ㴦�������ܻ���host����
	 * ��captureORdeleteΪtrue�� �ӽڵ���url����todohref��
	 * ��captureORdeleteΪfalse�� �ӽڵ���host�����Ѿ��������ڵ����е�visitedhost����
	 * @param hostReq
	 */
	public void processHostReq(MainUnitCom.HostReq hostReq){
		boolean captureORdelete = hostReq.captureORdelete;
		String host = hostReq.host;
		String url = hostReq.url;
		if(true == captureORdelete){
			String[] seeds = url.split(";");
			for(String str : seeds){
				Manager.todoHrefs.offer(str.intern());
			}
		}
		else {
			DSBData.ownVisitedHost.offer(host.intern());
		}
	}
	
	/**
	 * �ӽڵ㴦���ܻ���resJoinReq
	 * @param resJoinReq
	 * @return 1��ʾ�ܻ�ͬ�����ӣ� 0��ʾ�ܻ���ͬ������
	 */
	public int processResJoinReq(MainUnitCom.ResJoinReq resJoinReq){
		boolean connect = resJoinReq.connect;
		boolean ipConflict = resJoinReq.ipConfilct;
		boolean seedConfilct = resJoinReq.seedConflict;
		if(true == connect){
			JOptionPane.showMessageDialog(null, "connect succeed!");
			DSBData.setHostIP(resJoinReq.ip);
			DSBData.setHostPort(resJoinReq.port);//��������ip�Ͷ˿�
			return 1;
		}
		else if(true == ipConflict) {
			JOptionPane.showMessageDialog(null, "ipConflict! please check your ip");
			return 0;
		}
		else if(true == seedConfilct){
			JOptionPane.showMessageDialog(null, "seedConflict! please change your seed");
			return 0;
		}
		else {
			JOptionPane.showMessageDialog(null, "connect failed!");
			return 0;
		}
	}
	
	/**
	 * ����������delete���� �������ݣ� ֹͣץȡ�� ֹͣ���ӣ� ��ʾ�û�
	 * @param deleteReq
	 */
	private void processDeleteReq(MainUnitCom.DeleteNodeReq deleteReq){
		if(!deleteReq.clear){
			dataSave();
		}
		else{
			File own = new File("ownVisitedHost"),
				other = new File("othersVisitedHost");
			own.delete();
			other.delete();
		}
		mainframe.abortListenTask();	
		mainframe.dTaskStop();	
		showMessage("The host delete our node");		
	}
	
	/**
	 * ����dnode������
	 */
	public void dataSave(){
		saveOwnVisitedHost();
		saveOtherVisitedHost();
	}
	
	/**
	 * ����ownVisitedHost
	 */
	private void saveOwnVisitedHost(){
		File file = new File("ownVisitedHost");
		if(!file.exists()) {
			try{
				file.createNewFile();
				file.setReadOnly();
			}catch(IOException e){
				showMessage("create file ownVisitedHost failed, the data ownVisitedHost is lost");
				return;
			}
		}
		file.setExecutable(true);
		file.setReadable(true);
		file.setWritable(true);
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(DSBData.ownVisitedHost);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			showMessage("save data ownVisitedHost failed");
			return;
		}
		finally{
			try{
				file.setReadOnly();
				out.close();
			}catch(Exception e){}
		}
	}
	
	/**
	 * ����othersVisitedHost
	 */
	private void saveOtherVisitedHost(){
		File file = new File("othersVisitedHost");
		if(!file.exists()) {
			try{
				file.createNewFile();
				file.setReadOnly();
			}catch(IOException e){
				showMessage("create file othersVisitedHost failed, the data othersVisitedHost is lost");
				return;
			}
		}
		ObjectOutputStream out = null;
		try {
			file.setExecutable(true);
			file.setReadable(true);
			file.setWritable(true);
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(DSBData.othersVisitedHost);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			showMessage("save data othersVisitedHost failed");
			return;
		}
		finally{
			try{
				file.setReadOnly();
				out.close();
			}catch(Exception e){}
		}
	}
	
	/**
	 * ���嶨�ڱ������ݵ���
	 * @author keyalin
	 *
	 */
	class DataSaveTask implements Runnable{
		public void run(){
			while(!Thread.currentThread().isInterrupted())
			try{
				long saveTime = GetSettingValue.getSavePeriod();
				TimeUnit.MILLISECONDS.sleep(saveTime);
				dataSave();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	 
	/**
	 * �����ӽڵ㶨�ڼ�����ڵ��Ƿ���ڵ�������
	 * @author keyalin
	 *
	 */
	class CheckHost implements Runnable{
		public void run(){
			while(!Thread.currentThread().isInterrupted()){
				try {
					TimeUnit.SECONDS.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				NodeCom checkReq = new NodeCom();
				try {
					checkReq.setCheckReq();
				} catch (NotCallException e) {
					e.printStackTrace();
					continue;
				}
				if(0 == Utility.sendRequest(checkReq, DSBData.getHostIP(), DSBData.getHostPort())){
					Utility.showMessage("Can't connect to the host");
					mainframe.abortListenTask();
					mainframe.dTaskStop();
				}
				else{
					continue;
				}
			}
		}
	}
}
