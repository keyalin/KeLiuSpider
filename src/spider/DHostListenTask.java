package spider;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

/**
 * �����ܻ��ļ������� �������������������
 * 1. ��ȡ�ܻ���dhosttable���ݣ������ӽڵ������Ϣ�Ŀ�ݷ��ʱ�
 * 2. ѭ�����ӹ̶��˿ڣ���ȡ�ӽڵ㷢��������. �ӽڵ�������м������ͣ�
 * 	  joinReq�� quitReq�� host���� resAddnode����
 * 3. ���ݲ�ͬ��������ͣ� ������Ӧ�Ĵ���
 * 4. ���ڼ���ӽڵ��Ƿ����
 * @author keyalin
 *
 */
class DHostListenTask extends ListenTask{
	DHostListenTask(String ip, int port, MainFrame mainframe){
		super(ip, port, mainframe);
		StringBuilder sb = new StringBuilder();
		sb.append("Type: host ");
		sb.append("IP: ");
		sb.append(ip);
		sb.append(" port: ");
		sb.append(port);
		this.mainframe.setTitle(sb.toString().intern());
	}
	
	public void run(){				
		if(0 == dataIntial()) {//Ԥ��ȡ����
			mainframe.abortListenTask();
			return;
		}
		Object[][] nodesTable = DSBData.dHostTable.getNodeData();//��nodes����Ϣ��ʾ��nodestable����
		mainframe.repaintNodesTable(nodesTable);
		
		Thread datasave = new Thread(new DataSaveTask());//�������ڱ�����������
		datasave.setDaemon(true);
		datasave.start();
		
		Thread nodesCheck = new Thread(new CheckTask());//�������ڱ������ӽڵ��Ƿ��������
		nodesCheck.setDaemon(true);
		nodesCheck.start();
		
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			showMessage("someting wrong happen, " + e.toString());
			mainframe.abortListenTask();
			return;
		}
		//������������������
		ExecutorService exec = Executors.newCachedThreadPool();
		while(!server.isClosed()){
			Socket socket = null;
			try {
				socket = server.accept();
				exec.execute(new RecieveTask(socket));
			} catch (IOException e) {
				showMessage("someting wrong happen, " + e.toString());
				mainframe.abortListenTask();
				return;
			}
		}
	}

	
	/**
	 * ��ʼ��������distribute����
	 * @return 0��ʾ��ʼ��ʧ�ܣ� 1��ʾ��ʼ���ɹ�
	 */
	private int dataIntial(){
		if(0 == readHostTableData()){
			return 0;
		}
		else return 1;
	}
	
	/**
	 * Ԥ��ȡ������������ݣ� dhosttable
	 * @return 0��ʾ��ȡ�ɹ��� 1��ʾ��ȡʧ��
	 */
	private int readHostTableData(){
		ObjectInputStream in = null;
		File file = new File("dHostTable");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				showMessage("Create file dHostTable failed!");
				return 0;
			}
		}
		try{
			file.setExecutable(true);
			file.setWritable(true);
			file.setReadable(true);
			in = new ObjectInputStream(new FileInputStream(file));
			try {
				DSBData.dHostTable = (DHostTable)in.readObject();
				return 1;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}catch (FileNotFoundException e) {
			Utility.showMessage("No file dHostTable!");
			return 0;
		} 
		catch (EOFException e) {
			DSBData.dHostTable = new DHostTable();
			return 1;
		}
		catch(IOException e){
			e.printStackTrace();
			showMessage("Read the data failed");
			return 0;
		}finally{
			try {
				file.setReadable(true);
				in.close();
			} catch (Exception e) {}
		}
		return 0; 
	}
	
	/**
	 * RecieveTask����������socket����
	 * �����ӽڵ��join����, quit����, host���� resAdd����
	 * @author keyalin
	 */
	class RecieveTask implements Runnable{
		private Socket socket;
		RecieveTask(Socket socket){
			this.socket = socket;
		}
		public void run(){
			ObjectInputStream in = null;
			NodeCom nodeReq = null;
			try{
				in = new ObjectInputStream(socket.getInputStream());
				nodeReq = (NodeCom)(in.readObject());
			}catch(IOException e){
				return;
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
			if(null == nodeReq) return;
				//����join����
			if(NodeCom.ReqType.JOIN == nodeReq.getReqType()){
				NodeCom.JoinReq joinReq = nodeReq.getJoinReq();
				if(0 == processJoinReq(joinReq)) return;
				showMessage("the node: " + joinReq.newIp + " join in");
				addNode(joinReq.oldIp, joinReq.newIp, joinReq.port);
				Object[][] data = DSBData.dHostTable.getNodeData();
				mainframe.repaintNodesTable(data);//���»���nodestable
				data = null;
				return;
			}
				//����quit����
			else if(NodeCom.ReqType.QUIT == nodeReq.getReqType()){				
				NodeCom.QuitReq  quitReq = nodeReq.getQuitReq();
				if(0 == processQuitReq(quitReq)) return;
				else{
					Object[][] data = DSBData.dHostTable.getNodeData();
					mainframe.repaintNodesTable(data);
					quitReq = null;
					data = null;
					return;
				}
			}
				//����host����
			else if(NodeCom.ReqType.HOST == nodeReq.getReqType()){
				NodeCom.HostReq hostReq = nodeReq.getHostReq();
				processHostReq(hostReq);
				hostReq = null;
				return;
			}
				//����resAdd����
			else if(NodeCom.ReqType.RESADDNODE == nodeReq.getReqType()){
				NodeCom.ResAddNodeReq resAddNodeReq = nodeReq.getResAddNodeReq();
				if(0 == processResAddNodeReq(resAddNodeReq)) {
					showMessage("Add the node: " + resAddNodeReq.ip + " failed!");
					resAddNodeReq = null;
					return;
				}
				else {
					Object[][] data = DSBData.dHostTable.getNodeData();
					mainframe.repaintNodesTable(data);
					showMessage("Add the node: " + resAddNodeReq.ip + " sccuceed!");
					resAddNodeReq = null;
				}
			}
			else if(NodeCom.ReqType.CHECK == nodeReq.getReqType()){
				nodeReq = null;
			}
			else return;
		}
	}

	/**
	 * ���������ӽڵ��quit������̡� ��DSBData.dHostTable�޶�Ӧ��ip�ӽڵ㣬 ����0�� ���򷵻�1.
	 * @param quitReq
	 * @return
	 */
	private int processQuitReq(NodeCom.QuitReq quitReq){
		if(false == quitReq.clear){			
			if(1 == DSBData.dHostTable.removeNode(quitReq.ip)){
				String message = "the node: " + quitReq.ip + " want to quit, " +
				"and the data of it will not be delete!";
				JOptionPane.showMessageDialog(null, message);
				return 1;
			}
			else return 0;
		}
		else {
			if(1 == DSBData.dHostTable.deleteNode(quitReq.ip)){
				String message = "the node: " + quitReq.ip + " want to quit, " +
				"and the data of it will be delete!";
				JOptionPane.showMessageDialog(null, message);
				return 1;
			}
			else return 0;
		}
	}
	
	/**
	 * �ܻ������ӽڵ��HostReq����addORwaitΪ HostReq��һ���ֶ�,true��ʾ��ǰ�ӽڵ�ץȡ���host�ɹ���
	 * ��addORwaitΪtrue�� �����host�Ƿ��Ѿ��������ڵ�ץȡ���� ��true�� ֪ͨ�˽ڵ㲻Ҫ��ץȡ�˽ڵ�
	 * ��addORwaitΪtrueΪfalse�� �ȼ���Ƿ������ڵ�ץȡ���������򽫴�host�����͸����е�ip�ڵ㣬������ץȡ��host
	 * @param hostReq
	 */
	private void processHostReq(NodeCom.HostReq hostReq){
		String ip = hostReq.ip;
		String host = hostReq.host;
		String url = hostReq.url;
		int port = hostReq.port;
		boolean addORwait = hostReq.addORwait;
		String existHostIp = DSBData.dHostTable.containHost(host);
		if(true == addORwait){
			if(null == existHostIp) {
				DSBData.dHostTable.addHost(ip, host);
			}
			else if(existHostIp == ip) return;
			else {
				//֪ͨ�ӽڵ����host�Ѿ��������ڵ����
				MainUnitCom mHostReq = new MainUnitCom();
				try {
					mHostReq.setHostReq(host, false, null);
				} catch (NotCallException e) {
					e.printStackTrace();
				}
				sendRequest(mHostReq, ip, port);
			}
		}
		else {	
				//�����е��ӽڵ㷢��url����
			if(null == existHostIp){				
				for(Object[] a : DSBData.dHostTable.getNodeData()){
					if(null == a[2].toString())break;
					if("false" == a[2].toString()) continue;
					MainUnitCom mHostReq = new MainUnitCom();
					try {
						mHostReq.setHostReq(host, true, url);
					} catch (NotCallException e) {
						e.printStackTrace();
					}
					sendRequest(mHostReq, a[0].toString(), Utility
									.convertNumberStringToInt(a[1].toString()));
				}
			}
			else return;
		}
	}
	
	/**
	 * �����ӽڵ��Join����, ���ӽڵ㷢��ResJoinReq����������Ϊ1����ʾ�ܻ�ͬ����Ӵ˽ڵ�
	 * @param nodeReq �������
	 * @return 0��ʾ����ʧ�ܣ� 1��ʾ��Ӧ�ɹ�
	 */
	private int processJoinReq(NodeCom.JoinReq joinReq){
		String oldIp = joinReq.oldIp;
		String newIp = joinReq.newIp;					
		String seed = joinReq.seed;
		int port = joinReq.port;
		MainUnitCom resJoinReq = new MainUnitCom();
			//join �ڵ���һ���µĽڵ�
		if(null == oldIp){
				//���newip�Ƿ��ͻ
			if(0 != DSBData.dHostTable.containNode(newIp)){				
				try{
					resJoinReq.setResJoinReq(DSBData.getHostIP(), DSBData.getHostPort(),false, 
							true, false);
				}catch(NotCallException e){
					e.printStackTrace();
					return 0;
				}
				sendRequest(resJoinReq, newIp, port);
				return 0;
			}
				//newip����ͻ
			else {
					//seed��ͻ
				if(null != DSBData.dHostTable.containHost(seed)){
					try{
						resJoinReq.setResJoinReq(DSBData.getHostIP(), DSBData.getHostPort(),
								false, false, true);
					}catch(NotCallException e){
						e.printStackTrace();
						return 0;
					}
					sendRequest(resJoinReq, newIp, port); 
					return 0;				
				}
					//seed����ͻ
				else {
					try{
						resJoinReq.setResJoinReq(DSBData.getHostIP(), DSBData.getHostPort(),true, 
								false, false);
					}catch(NotCallException e){
						e.printStackTrace();
						return 0;
					}
					if(0 == sendRequest(resJoinReq, newIp, port)) return 0;
					else {
						return 1;
					}
				}
			}
		}
			//��ǰ�Ľڵ�Ҫ�����¼���
		else{
			if(0 != DSBData.dHostTable.containNode(oldIp)){//�ýڵ���ڵ��ǲ���Ծ
				try{
					resJoinReq.setResJoinReq(DSBData.getHostIP(), DSBData.getHostPort(),true, 
							false, false);
				}catch(NotCallException e){
					e.printStackTrace();
					return 0;
				}
				if(0 == sendRequest(resJoinReq, newIp, port)) return 0;
				else return 1;
			}
			else {
				try{//û�з���join����������old�ڵ�
					resJoinReq.setResJoinReq(DSBData.getHostIP(), DSBData.getHostPort(),false, 
							false, false);
				}catch(NotCallException e){
					e.printStackTrace();
					return 0;
				}
				sendRequest(resJoinReq, newIp, port);
				return 0;
			}
		}
	}
	

	/**
	 * �ܻ���MainUnitCom�����͸�ָ��ip�� port���ӽڵ�
	 * @param request �������
	 * @param ip �ӽڵ��ip
	 * @param port �ӽڵ��listen�˿�
	 * @return 1��ʾ�ɹ��� 0��ʾʧ��
	 */
	private int sendRequest(MainUnitCom request, String ip, int port){
		Socket socket = null;
		ObjectOutputStream out = null;
		try{
			socket = new Socket(ip, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(request);
			out.close();
			socket.close();
			return 1;
		}catch(IOException e){
			return 0;
		}finally{
			try{
				out.close();
				socket.close();
			}catch(Exception e){}
		}
	}
	
	/**
	 * ��DHostTabletable�����һ���µ��ӽڵ�
	 * @param oldIp �ӽڵ�֮ǰ��ip��ַ�� ��Ϊnull�� ��ʾ�ýڵ���һ���¼���Ľڵ�
	 * @param newIp �ӽڵ�����ip��ַ
	 * @param port �ӽڵ�ļ��Ӷ˿�
	 */
	private void addNode(String oldIp, String newIp, int port){
		if(1 == DSBData.dHostTable.containNode(newIp)) return;
		if(null == oldIp){
			DSBData.dHostTable.addNode(newIp, port);
			return;
		}
		else{//�ָ��ɽڵ�Ļ�Ծ��
			DSBData.dHostTable.updateIpPort(oldIp, newIp, port);
			DSBData.dHostTable.recoveryNode(newIp, port);
			return;
		}
	}
	
	/**
	 * �ܻ������ӽڵ��resAddNodeReq����
	 * @param resAddNodeReq
	 * @return 1��ʾͬ���ӽڵ���룬 0��ʾ����ʧ�ܣ� Ӧ��֪ͨ��������Ա
	 */
	private int processResAddNodeReq(NodeCom.ResAddNodeReq resAddNodeReq){
		String ip = resAddNodeReq.ip;
		int port = resAddNodeReq.port;
		System.out.print("nihao");
		boolean history = resAddNodeReq.history;
		boolean agree = resAddNodeReq.agree;
		if(false == agree){
			return 0;
		}
		else{
			if(true != history){//������add���� ���ӽڵ�ip��ַ�����仯�� ��ô�������ӽڵ㷢��join�����ٶ���һ������
				DSBData.dHostTable.addNode(ip, port);
			}
			else{
				DSBData.dHostTable.recoveryNode(ip, port);
			}
			return 1;	
		}	
	}
	
	/**
	 * ���ڱ���������
	 * ��run���������� ע����������ֹ
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
	 * ������host�йص�����
	 */
	public void dataSave(){
		saveDHostTable();
	}
	
	/**
	 * ����dHostTable
	 * @return 1��ʾ����ɹ��� 0��ʾ����ʧ��
	 */
	private int saveDHostTable(){
		File file = new File("dHostTable");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				showMessage("Create file dHostTable failed");
				e.printStackTrace();
				return 0;
			}
		}
		ObjectOutputStream out = null;
		try{
			file.setExecutable(true);
			file.setWritable(true);
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(DSBData.dHostTable);
			return 1;
		}catch(IOException e){
			System.out.println(e);
			showMessage("Save file dHostTable failed");
			return 0;
		}
		finally{
			try{
				file.setReadOnly();
				out.close();
			}catch(Exception e){}
		}
	}
	
	/**
	 * ���ڼ���ӽڵ��Ƿ���ڣ� ���ʱ����Ϊ60�� 
	 * @author keyalin
	 *
	 */
	class CheckTask implements Runnable{
		public void run(){
			while(!Thread.currentThread().isInterrupted()){
				try {
					TimeUnit.SECONDS.sleep(60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Object[][] data = DSBData.dHostTable.getNodeData();
				for(Object[] a : data){
					if(null == a[1]) break;
					String ip = a[1].toString();
					int port = Utility.convertNumberStringToInt(a[2].toString());
					String active = a[3].toString();
					if(active.equals("false")) continue;
					MainUnitCom req = new MainUnitCom();
					try {
						req.setCheckReq();
					} catch (NotCallException e1) {
						e1.printStackTrace();
					}
					if(0 == Utility.sendRequest(req, ip, port)){
						Utility.showMessage("The node: " + ip + " do not exist");
						a[3] = false;
						mainframe.repaintNodesTable(data);
						DSBData.dHostTable.removeNode(ip);
					}
				}
			}
		}
	}
}
