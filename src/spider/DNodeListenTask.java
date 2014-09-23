package spider;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.swing.JOptionPane;

/**
 * 定义子节点的监视任务类
 * @author keyalin
 *
 */
class DNodeListenTask extends ListenTask{
	Thread checkThread = null;//子节点定期检查主节点是否存在，在子节点加入系统中启动。
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
			//预读取数据
		if(0 == dataIntial()) {
			showMessage("data intial failed");
			mainframe.abortListenTask();//终止监听
			return;
		}
		Thread datasave = new Thread(new DataSaveTask());//启动定期保存数据任务
		datasave.setDaemon(true);
		datasave.start();
		ExecutorService exec = Executors.newCachedThreadPool();
		try{
			server = new ServerSocket(port);
		}catch(Exception e){
			e.printStackTrace();
			showMessage("something wrong " + e.toString());
			mainframe.abortListenTask();//终止监听
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
	 * 定义子节点处理单个请求任务类
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
						checkThread = new Thread(new CheckHost());//启动定期检查主机是否存在的任务
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
					 * 简单丢弃， 不做处理
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
					 * 提示用户连接成功即可
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
	 * 初始化dnodelistentask的数据
	 * @return 初始化成功返回1， 失败返回0
	 */
	private int dataIntial(){
		if(1 == readOwnVisitedHostData() && 1 == readOthersVisitedHostData()){
			return 1;
		}
		else return 0;
	}
	/**
	 * 读取数据ownVisitedHost
	 * @return 1表示成功， 0表示失败
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
	 * 读取数据othersVisitedHost
	 * @return 1表示成功， 0表示失败
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
	 * 子节点处理总机来的addNode请求, 回复一个ResAddNodeReq
	 * @param addNodeReq 收到的addNode请求
	 * @return 1表示回复成功， 0表示失败
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
			agree = false;//用户不同意join in
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
			resAddNodeReq.setResAddNodeReq(this.ip, this.port, agree, history);//此ip非主机ip，是自己的ip
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
			DSBData.setHostPort(port);//保存主机ip和端口
			return 1;
		}
	}
	
	/**
	 * 子节点处理来自总机的host请求
	 * 若captureORdelete为true， 子节点则将url放入todohref中
	 * 若captureORdelete为false， 子节点则将host放入已经被其他节点爬行的visitedhost表中
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
	 * 子节点处理总机的resJoinReq
	 * @param resJoinReq
	 * @return 1表示总机同意连接， 0表示总机不同意连接
	 */
	public int processResJoinReq(MainUnitCom.ResJoinReq resJoinReq){
		boolean connect = resJoinReq.connect;
		boolean ipConflict = resJoinReq.ipConfilct;
		boolean seedConfilct = resJoinReq.seedConflict;
		if(true == connect){
			JOptionPane.showMessageDialog(null, "connect succeed!");
			DSBData.setHostIP(resJoinReq.ip);
			DSBData.setHostPort(resJoinReq.port);//保存主机ip和端口
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
	 * 处理主机的delete请求。 保存数据， 停止抓取， 停止监视， 提示用户
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
	 * 保存dnode的数据
	 */
	public void dataSave(){
		saveOwnVisitedHost();
		saveOtherVisitedHost();
	}
	
	/**
	 * 保存ownVisitedHost
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
	 * 保存othersVisitedHost
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
	 * 定义定期保存数据的类
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
	 * 定义子节点定期检查主节点是否存在的任务类
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
