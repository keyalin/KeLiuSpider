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
 * 定义总机的监视任务， 它负责完成以下人任务：
 * 1. 读取总机的dhosttable数据，创建子节点基本信息的快捷访问表。
 * 2. 循环监视固定端口，读取子节点发来的请求. 子节点的请求有几种类型：
 * 	  joinReq， quitReq， host请求， resAddnode请求
 * 3. 根据不同请求的类型， 做出相应的处理。
 * 4. 定期检查子节点是否存在
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
		if(0 == dataIntial()) {//预读取数据
			mainframe.abortListenTask();
			return;
		}
		Object[][] nodesTable = DSBData.dHostTable.getNodeData();//将nodes的信息显示在nodestable上面
		mainframe.repaintNodesTable(nodesTable);
		
		Thread datasave = new Thread(new DataSaveTask());//启动定期保存数据任务
		datasave.setDaemon(true);
		datasave.start();
		
		Thread nodesCheck = new Thread(new CheckTask());//启动定期保存检查子节点是否存在任务
		nodesCheck.setDaemon(true);
		nodesCheck.start();
		
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			showMessage("someting wrong happen, " + e.toString());
			mainframe.abortListenTask();
			return;
		}
		//接受任务并且做出处理
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
	 * 初始化主机的distribute数据
	 * @return 0表示初始化失败， 1表示初始化成功
	 */
	private int dataIntial(){
		if(0 == readHostTableData()){
			return 0;
		}
		else return 1;
	}
	
	/**
	 * 预读取主机的相关数据： dhosttable
	 * @return 0表示读取成功， 1表示读取失败
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
	 * RecieveTask用来处理单个socket任务
	 * 处理子节点的join请求, quit请求, host请求， resAdd请求
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
				//处理join请求
			if(NodeCom.ReqType.JOIN == nodeReq.getReqType()){
				NodeCom.JoinReq joinReq = nodeReq.getJoinReq();
				if(0 == processJoinReq(joinReq)) return;
				showMessage("the node: " + joinReq.newIp + " join in");
				addNode(joinReq.oldIp, joinReq.newIp, joinReq.port);
				Object[][] data = DSBData.dHostTable.getNodeData();
				mainframe.repaintNodesTable(data);//重新绘制nodestable
				data = null;
				return;
			}
				//处理quit请求
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
				//处理host请求
			else if(NodeCom.ReqType.HOST == nodeReq.getReqType()){
				NodeCom.HostReq hostReq = nodeReq.getHostReq();
				processHostReq(hostReq);
				hostReq = null;
				return;
			}
				//处理resAdd请求
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
	 * 主机处理子节点的quit请求过程。 若DSBData.dHostTable无对应的ip子节点， 返回0， 否则返回1.
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
	 * 总机处理子节点的HostReq请求。addORwait为 HostReq的一个字段,true表示当前子节点抓取这个host成功。
	 * 若addORwait为true， 则检查此host是否已经被其他节点抓取过， 若true， 通知此节点不要再抓取此节点
	 * 若addORwait为true为false， 先检查是否被其他节点抓取过，若否则将此host请求发送给所有的ip节点，让他们抓取此host
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
				//通知子节点这个host已经被其他节点遍历
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
				//向所有的子节点发送url请求
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
	 * 处理子节点的Join请求, 向子节点发送ResJoinReq请求。若返回为1，表示总机同意添加此节点
	 * @param nodeReq 请求对象
	 * @return 0表示处理失败， 1表示回应成功
	 */
	private int processJoinReq(NodeCom.JoinReq joinReq){
		String oldIp = joinReq.oldIp;
		String newIp = joinReq.newIp;					
		String seed = joinReq.seed;
		int port = joinReq.port;
		MainUnitCom resJoinReq = new MainUnitCom();
			//join 节点是一个新的节点
		if(null == oldIp){
				//检查newip是否冲突
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
				//newip不冲突
			else {
					//seed冲突
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
					//seed不冲突
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
			//以前的节点要求重新加入
		else{
			if(0 != DSBData.dHostTable.containNode(oldIp)){//该节点存在但是不活跃
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
				try{//没有发现join请求申明的old节点
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
	 * 总机将MainUnitCom请求发送给指定ip， port的子节点
	 * @param request 请求对象
	 * @param ip 子节点的ip
	 * @param port 子节点的listen端口
	 * @return 1表示成功， 0表示失败
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
	 * 在DHostTabletable中添加一个新的子节点
	 * @param oldIp 子节点之前的ip地址， 若为null， 表示该节点是一个新加入的节点
	 * @param newIp 子节点现在ip地址
	 * @param port 子节点的监视端口
	 */
	private void addNode(String oldIp, String newIp, int port){
		if(1 == DSBData.dHostTable.containNode(newIp)) return;
		if(null == oldIp){
			DSBData.dHostTable.addNode(newIp, port);
			return;
		}
		else{//恢复旧节点的活跃性
			DSBData.dHostTable.updateIpPort(oldIp, newIp, port);
			DSBData.dHostTable.recoveryNode(newIp, port);
			return;
		}
	}
	
	/**
	 * 总机处理子节点的resAddNodeReq请求
	 * @param resAddNodeReq
	 * @return 1表示同意子节点加入， 0表示处理失败， 应当通知主机管理员
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
			if(true != history){//主机的add请求， 若子节点ip地址发生变化， 那么必须由子节点发出join请求。再定义一个功能
				DSBData.dHostTable.addNode(ip, port);
			}
			else{
				DSBData.dHostTable.recoveryNode(ip, port);
			}
			return 1;	
		}	
	}
	
	/**
	 * 定期保存数据类
	 * 在run里面驱动， 注意该任务的终止
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
	 * 保存与host有关的数据
	 */
	public void dataSave(){
		saveDHostTable();
	}
	
	/**
	 * 保存dHostTable
	 * @return 1表示保存成功， 0表示保存失败
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
	 * 定期检查子节点是否存在， 检查时间间隔为60秒 
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
