/**
 * 该文件定义了爬虫软件所有的事件类
 */
package spider;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;

import javax.swing.*;

import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import javax.swing.table.*;

import spider.Pic.NextEvent;
import spider.Pic.PreviousEvent;


/**
 * @author keyalin
 * 192.168.1.102
 */
public class MainFrame extends SpiderMainFrame{
	
	private static final long serialVersionUID = 2699253242912665802L;

	public MainFrame(){
		this.setTitle("keliuspider");
		this.setVisible(true);
		this.hostPanel.setVisible(false);
		this.nodePanel.setVisible(false);
		this.nodeTypeSelectPanel.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocation(400, 400);
		this.setSize(800, 600);
		eventsAdd();
	}
	
	private void eventsAdd(){
		stopButton.addActionListener(new StopButtonEvent());
		pauseButton.addActionListener(new PauseButtonEvent());
		startButton.addActionListener(new StartButtonEvent());
		imageThreadCount.addItemListener(new ImageSelectEvent());
		connTimeout.addItemListener(new TimeoutSelectEvent());
		savePeriod.addItemListener(new SavePeriodEvent());//change
		pageThreadCount.addItemListener(new LinksSelectEvent());
		saveButton.addActionListener(new DatabaseSettingSaveEvent());
		queryButton.addActionListener(new QueryEvent());
		displayButton.addActionListener(new DisplayEvent());
		typeSelect.addActionListener(new DTypeSelectEvent());
		addNodeButton.addActionListener(new AddNodeEvent());
		deleteButton.addActionListener(new DeleteNodeEvent());
		removeButton.addActionListener(new RemoveNodeEvent());
		abortButton.addActionListener(new AbortEvent());
		backButton1.addActionListener(new BackButton1Event());
		joinButton.addActionListener(new JoinEvent());
		quitButton.addActionListener(new QuitEvent());
		backButton2.addActionListener(new BackButton2Event());
		sendButton.addActionListener(new SendButtonEvent());
	}  
	   
   //自定义数据
   	private MainFrame mainframe = this;
   	private Manager taskManager = null;
   	//自定义数据
   
   /*
    * 修改mainframe里面的数据接口
    */
   
   //修改显示数据
   	public void setSpeedLabelText(String text){
   		this.speedLabel.setText(text);
   	}
   	
   	public void setValidUrlsLabelText(String text){
   		this.validUrlsLabel.setText(text);
   	}
   	
   	public void setInvalidUrlsLabelText(String text){
   		this.invalidUrlsLabel.setText(text);
   	}
   	
	public void setValidImagesLabelText(String text){
   		this.validImagesLabel.setText(text);
   	}
   	
   	public void setInvalidImagesLabelText(String text){
   		this.invalidImagesLabel.setText(text);
   	}
   	
   	public void setTimeCostLabelText(String text){
   		this.timeCostLabel.setText(text);
   	}
   	
	public void setMemoryUsageLabelText(String text){
   		this.memoryUsageLabel.setText(text);
   	}
   	
   	public void setTotalSizeText(String text){
   		this.totalSizeLabel.setText(text);
   	}
   	
   	public void setSeedsText(String text){
   		this.seedsText.setText(text);
   	}
   
   	//提供修改capturetable数据的接口
   	//修改单个选项
   	public  synchronized void setValueAtCaptureTable(int row, int column, Object value){
   		this.captureTable.setValueAt(value, row, column);
   	}
   	
//	   	//保留方法， 以后用到
//	   	public synchronized void repaintCaptureTable(Object[][] data){
//	   		captureTableDefaultTableModel.setDataVector(data, new String [] {
//		               "threadname", "url", "capturetype", "size", "status"
//	        });
//	   	}
   	
   	public synchronized Object getValueAtCaptureTable(int row, int column){
   		return captureTable.getValueAt(row, column);
   	}
   	
   	
   	/**
   	 * 若分布式爬行已结束， 那么就显示nodetypeselectpanel
   	 */
   	public void dRunAgain(){
   		this.hostPanel.setVisible(false);
   		this.nodePanel.setVisible(false);
   		this.nodeTypeSelectPanel.setVisible(true);
   	}
   /*
    * 修改mainframe里面的数据接口上
    */
   	
   	
   /*
    * 事件处理代码区域, 其他地方不要修改下
    */

   	/**
   	 * 定义下载按钮处理
   	 * 先跟据distribute的选择确定taskmanager的类型
   	 * 再根据history的选择确定抓取起点。
   	 * 然后让histoy按钮不可编辑
   	 * @keyalin
   	 */
   	class StartButtonEvent implements ActionListener{
   		public void actionPerformed(ActionEvent e){
   			startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
   			try{
   				int i = JOptionPane.showConfirmDialog(null, "Please check your setting, are " +
   					"you sure?", "hey", JOptionPane.YES_NO_OPTION);
   				if(JOptionPane.NO_OPTION == i) return;
   				if(null != taskManager){//任务已启动
   					JOptionPane.showMessageDialog(null, "Capturing has been already started!");
   					return;
   				}
   				if(dsbNo.isSelected()){//选择taskmanager的类型
   					taskManager = new SingleTaskManager(mainframe);
   				}
   				else if(dsbYes.isSelected()){
   					taskManager = new DistributedTaskManager(mainframe);
   				}
   				else {
   					JOptionPane.showMessageDialog(null, "Please select the mode single " +
   						"or distributed?");
   					return;
   				}
   				if(seed.isSelected()){
   					String[] seeds = seedTextField.getText().trim().split(";");
   					if(0 == taskManager.startFromSeeds(seeds)){
   						taskManager = null;
   						Utility.showMessage("start failed");
   						return;
   					}
   					//让相关按钮不能按seed, history
   					seed.setEnabled(false);
   					history.setEnabled(false);
   					return;
   				}
   				else if(history.isSelected()){
   					if(0 == taskManager.startFromHistory()){
   						taskManager = null;
   						Utility.showMessage("start failed");
   						return;
   					}
   					//让相关按钮不能按seed, history
   					seed.setEnabled(false);
   					history.setEnabled(false);
   					return;
   				}
   				else{
   					taskManager = null;
   					JOptionPane.showMessageDialog(null, "Please select where to start, " +
						"history or seeds?");
   					return;
   				}   			
   			}
   			finally{
   				startButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}
   		}
   	}
   	
   	/**
   	 * 定义stop按钮监视器
   	 * 先检查任务启动没有
   	 * 释放history等button
   	 * @author keyalin
   	 *
   	 */
   	class StopButtonEvent implements ActionListener{
   		public void actionPerformed(ActionEvent e){
   			stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
   			try{
   				if(null == taskManager){//任务还没有启动
   				JOptionPane.showMessageDialog(null, "Task has not started yet!");
   				stopButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   				return;
	   			}
	   			taskManager.stop();
	   			taskManager = null;
	   			seed.setEnabled(true);//释放按钮
	   			history.setEnabled(true);
	   			return;	
   			}finally{
   				stopButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}   			
   		}
   	}
   	
   	/**
   	 * 定义暂停按钮的监视器
   	 * 先要检查task是否已经启动
   	 * @author keyalin
   	 *
   	 */
   	class PauseButtonEvent implements ActionListener{
   		public void actionPerformed(ActionEvent e){
   			pauseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
   			try{
   				if(null == taskManager){
   					JOptionPane.showMessageDialog(null, "Task has not started yet!");
   					return;
   				}

   				JToggleButton button = (JToggleButton)e.getSource();
   				boolean selected = button.isSelected();
   				if(selected){
   					taskManager.suspend();
   				}
   				else{
   					taskManager.restart();
   				}
   				return;
   			}
   			finally{
   				pauseButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}
   		}
   	}

   	/**
     * 定义links JComboBox , imags JComboBox, JComboBox timeout,
     * JComboBox savePeriod 监视器类， 以获得用户设置的参数. 然后将保存的数据存放
     * 在getsettingvalue里面
     */
    
    abstract class SelectEvent implements ItemListener{
 	   public abstract void itemStateChanged(ItemEvent e);
    }
    
    /**
     * 当用户修改pageThreadcount触发此事件。
     * 注意在主界面的动态显示
     * @author keyalin
     *
     */
    class LinksSelectEvent extends SelectEvent{
 	   public void itemStateChanged(ItemEvent e) {
 		   String str = e.getItem().toString(); 		   
 		   GetSettingValue.setPageThreadCount(Utility.convertNumberStringToInt(str));
 		   str = null;
 		   if(null != taskManager){
 			   taskManager.changeUsage();//杀死或者创建一些线程， 同时动态显示
 			   return;
 		   }
 	   }	   
    }
    
    /**
     * 当用户修改imageThreadcount触发此事件。
     * 注意在主界面的动态显示
     * @author keyalin
     *
     */
    class ImageSelectEvent extends SelectEvent{
 	   public void itemStateChanged(ItemEvent e) {
 		   String str = e.getItem().toString(); 		   
		   GetSettingValue.setImageThreadCount(Utility.convertNumberStringToInt(str));
		   str = null;
		   if(null != taskManager){
			   taskManager.changeUsage();//杀死一些线程， 同时动态显示
			   return;
		   }
 	   }
    }
    
    /**
     * 修改网页抓取的等待时间
     * @author keyalin
     *
     */
    class TimeoutSelectEvent extends SelectEvent{
 	   public void itemStateChanged(ItemEvent e) {
 		   String str = e.getItem().toString(); 
 		   GetSettingValue.setTimeout(Utility.convertNumberStringToInt(str) * 10);
 		   str = null;
 	   }
    }
    
    /**
     * 修改定期保存的时间
     * @author keyalin
     *
     */
    class SavePeriodEvent extends SelectEvent{
    	public void itemStateChanged(ItemEvent e) {
    		String str = e.getItem().toString();   		
  		   	GetSettingValue.setSavePeriod(Utility.convertNumberStringToInt(str) * 3600000);
  		   	str = null;
  	   	}
    }
   
   /**
    * 检查用户设置的database参数是否合理， 若合理， 就将用户设置database参数保存到GetSettingValue里面
    * 该类事件添加在databasepanel里面的saveButton监视器里面
    * @keyalin 
    */
   class DatabaseSettingSaveEvent implements ActionListener{
	   public void actionPerformed(ActionEvent e){
		   saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		   try{
			   String databaseName = databaseText.getText().trim();
			   String userName = userText.getText().trim();
			   String password = Utility.ConvertCharArrayToString(passwordText.getPassword());
			   if(databaseName.equals("")) {
				   JOptionPane.showMessageDialog(null, "the databaseName can't be null");
				   return;
			   }
			   if(userName.equals("")){
				   JOptionPane.showMessageDialog(null, "the userName can't be null");
				   return;
			   }
			   if(password.equals("")){
				   JOptionPane.showMessageDialog(null, "the password is null, are you sure?");
			   //here no return, attention!
			   }
			   GetSettingValue.setDatabaseName(databaseName);
			   GetSettingValue.setUserName(userName);
			   GetSettingValue.setPassword(password);
			   Utility.showMessage("Database setting succeed！");
			   databaseName = null;
			   userName = null;
			   password = null;
			   return;
		   }
		   	finally{
   				saveButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}
	   }
   }
   
   /* 1. 查询某个图片url所对应的图片。 image 和 display按钮
    * 2. 查询某个网页url所对应的网页正文内容。 page 和 display按钮。
    * 3. 统计某个host下图片数据。 image 和 query 按钮
    * 4. 同体积某个host下网页数据。 page 和 query 按钮。
    */
   
   /**
    * 定义display按钮的监视器
    * 显示相同host的图片
    */
   class DisplayEvent implements ActionListener{
   		public void actionPerformed(ActionEvent e){
   			displayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
   			try{
   				Connection conn = DatabaseManager.databaseConnection(databaseText.getText().trim(),userText.
   						getText().trim(), Utility.ConvertCharArrayToString(passwordText.getPassword()));
   				if(conn == null) {
   						displayTextArea.setText("Connect to the database failed");
   						return;
   				}
   				if(image.isSelected()){//批量显示图片
   					String sql = "select contents, format from images where host = ?";
   					PreparedStatement statement = null;
   					try {
   						statement = conn.prepareStatement(sql);
   						statement.setString(1, urlText.getText().trim());
   					} catch (SQLException e1) {
   						displayTextArea.setText(e1.toString());
   						return;
   					}
   					ResultSet set = null;
   					try {
   						set = statement.executeQuery();
   					} catch (SQLException e1) {
   						displayTextArea.setText(e1.toString());
   						return;
   					}  					
   					new Pic(set).setVisible(true);//显示图形查看   					
   				}
   				else if(page.isSelected()){//显示网页正文和关键内容
   					String sql = "select contents from pages where url = ?";
   					PreparedStatement statement = null;
   					try {
   						statement = conn.prepareStatement(sql);
   						statement.setString(1, urlText.getText().trim());
   					} catch (SQLException e1) {
   						displayTextArea.setText(e1.toString());
   					}
   					ResultSet set = null;
   					try {
   						set = statement.executeQuery();
   					} catch (SQLException e1) {
   						displayTextArea.setText("operate data failed!");
   					}
   					try {
   						set.next();
   						String html = set.getString(1);
   						displayTextArea.append("网页正文关键部分：\n");
   						displayTextArea.append(DomParser.getMainText(html) + "\n\n");
   						System.out.println(html);
   						System.out.println(DomParser.getTotalText(html));
   						displayTextArea.append("网页正文全部：\n");
   						displayTextArea.append(DomParser.getTotalText(html) + "\n\n");
   						set.close();					
   					} catch (SQLException e1) {
   						try {
							set.close();
						} catch (SQLException e2) {
							e2.printStackTrace();
						}
   						displayTextArea.setText(e1.toString());
   					}catch(NullPointerException e2){
   						displayTextArea.setText("没有相关数据在数据库中");
   					}
   				}
   			} finally{
   				displayButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}
   		}
   }
   
   /**
    * 定义querybutton的监视类
    * 它主要完成统计每个主机host的urls总数， imags总数， 并且在界面上展示。 
    */
   class QueryEvent implements ActionListener{
	   public void actionPerformed(ActionEvent e){
		   queryButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		   try{
	   			queryTextArea.setText(null);
	   			Connection conn = DatabaseManager.databaseConnection(mainframe.databaseText.
	   								getText().trim(), userText.getText().trim(), Utility.
	   								ConvertCharArrayToString(passwordText.getPassword()));
	   			if(conn == null) {
	   				queryTextArea.setText("Connect to the database failed");
	   				return;
	   			}
	   			if(page.isSelected()){
	   				String sql = "select url from pages where host = ?";
	   				PreparedStatement statement = null;
	   				ResultSet set = null;
	   				try{
	   					statement = conn.prepareStatement(sql);
	   					statement.setString(1, urlText.getText().trim());
	   					set = statement.executeQuery();
	   					set.last();
	   					int i = set.getRow();
	   					queryTextArea.append("总共查询到" + i + "条数据:\n");
	   					set.beforeFirst();
	   					while(set.next()){
	   						queryTextArea.append(set.getString(1) + "\n");
	   					}
	   				}catch(SQLException e1){
	   					queryTextArea.setText("操作失败： " + e1);
	   				}catch(NullPointerException e2){
	   					queryTextArea.setText("没有数据在数据库中");
	   				}
	   				finally {
	   					try{
	   						set.close();
	   						statement.close();
	   						conn.close();
	   					}catch(SQLException e1){
	   						queryTextArea.setText("the database failed to close , please check");
	   					}
	   				}
	   			}else if(image.isSelected()){
	   				String sql = "select url from images where host = ?";
	   				PreparedStatement statement = null;
	   				ResultSet set = null;
	   				try{
	   					statement = conn.prepareStatement(sql);
	   					statement.setString(1, urlText.getText().trim());
	   					set = statement.executeQuery();
	   					set.last();
	   					int i = set.getRow();
	   					queryTextArea.append("总共查询到" + i + "条数据:\n");
	   					set.beforeFirst();
	   					while(set.next()){
	   						queryTextArea.append(set.getString(1) + "\n");
	   					}
	   				}catch(SQLException e1){
	   					queryTextArea.setText("操作失败： " + e1);
	   				}catch(NullPointerException e2){
	   					queryTextArea.setText("没有数据在数据库中");
	   				}
	   				finally {
	   					try{
	   						set.close();
	   						statement.close();
	   						conn.close();
	   					}catch(SQLException e1){
	   						queryTextArea.setText("the database failed to close , please check");
	   					}
	   				}
	   			}
		   }finally{
   				queryButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}
   		}
   }
	   
	   //分布式代码从这里开始
	   /*
	    *数据 
	    */
   	private ListenTask listenTask = null;
	private Future future = null;
	private ExecutorService exec = Executors.newCachedThreadPool();
	
	//
	/**
	 * 提供修改nodestable的接口
	 */
   	public synchronized void repaintNodesTable(Object[][] data){
   		DefaultTableModel tableModel = new DefaultTableModel(data, new String [] {
                "linenumber", "node ip", "node port", "active"
   	    });
		nodesTable.setModel(tableModel);
	}
   	
   	/**
   	 * 提供停止listen任务的接口
   	 */
   	public synchronized void abortListenTask(){
   		if(null != future && !future.isCancelled()){
   			listenTask.stopListen();
   			future.cancel(true);
//   			exec.shutdownNow();
   			exec.shutdownNow();
   			listenTask = null;
   			future = null;
   		}
   	}
	
 	/**
 	 * 为分布模式提供停止抓取任务的接口
 	 */
   	public void dTaskStop(){
   		if(null == taskManager){//任务还没有启动， 或者已经停止
			return;
		}
		taskManager.stop();
		taskManager = null;
		seed.setEnabled(true);//释放按钮
		history.setEnabled(true);
		this.dsbNo.setEnabled(true);
		this.dsbYes.setEnabled(true);
   	} 
   	
   /**
    * 定义该机器作为节点还是作为主机
    * 若setting里面distributed选择的是no或者没有被选择， 提示用户当前设置是选择distibuted模式
    * 若setting里面distributed选择的是yes：那么进行以下操作：
    * 若dHostButton被选择， 那么只显示dHostPanel, 若dNodeButton被选择， 那么只显示dNodePanel
    * 然后 确定当前的ip， 和监视端口， 开启后台监视进程。 同时预读取history相关抓取信息。
    * 主机预读取信息由：dhosttable， 子节点预读取visitedhost， visitedUrls, visitedimages, totoHrefs,
    * 以及todoImages。 这些信息的引用位于DSBdata中。相关信息的读取让监视进程取完成。
    * @author keyalin
    *
    */  
	class DTypeSelectEvent implements ActionListener{
		public void actionPerformed(ActionEvent e){
			typeSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null != future) return;
				if(null == dsbButtonGroup.getSelection()){
					JOptionPane.showMessageDialog(null, "please select whether to distribute" +
					   		" in settingpanel", "Hey", JOptionPane.ERROR_MESSAGE);
					   return;
				}
				if(dsbNo.isSelected()){
					JOptionPane.showMessageDialog(null, "you have choosen no distribution" ,
				   			"Hey", JOptionPane.ERROR_MESSAGE);
				   return;
				}
				if(null == nodeTypeButtonGroup.getSelection()){
					 JOptionPane.showMessageDialog(null, "please select the type of distributed" +
					 		"host or node" , "Hey", JOptionPane.ERROR_MESSAGE);
					 return;
				}
				if(node.isSelected()){
					IPandPort data = getIPandPort();
					if(null == data) return;
					else {
						String ip = data.ip;
						int port = data.port;
						String message = "your ip is: " + ip + " and port is: " + port +
	   										"the distributed type is node, are you sure?";
						int option = JOptionPane.showConfirmDialog(null, message, null, 
		   							JOptionPane.YES_NO_OPTION);
						if(option == JOptionPane.YES_OPTION){
							listenTask = new DNodeListenTask(ip.intern(), port, mainframe);
							exec = Executors.newCachedThreadPool();
							future = exec.submit(listenTask) ;						 
							DSBData.setNodeIP(ip.intern());
							DSBData.setNodePort(port);
							dsbYes.setEnabled(false);
							dsbNo.setEnabled(false);
							nodePanel.setVisible(true);
							hostPanel.setVisible(false);
							nodeTypeSelectPanel.setVisible(false);
							return;
						}else if(option == JOptionPane.NO_OPTION){
							return;
						}
					}
				}
				if(host.isSelected()){
					IPandPort data = getIPandPort();
					if(null == data) return;
					else {
						String ip = data.ip;
						int port = data.port;
						String message = "your ip is: " + ip + " and port is: " + port +
	   										" the distributed type is host, are you sure?";
						 int option = JOptionPane.showConfirmDialog(null, message, null, 
		   							JOptionPane.YES_NO_OPTION);
						 if(option == JOptionPane.YES_OPTION){
							 listenTask = new DHostListenTask(ip.intern(), port, mainframe);
							 exec = Executors.newCachedThreadPool();
							 future = exec.submit(listenTask) ;
							 DSBData.setHostIP(ip.intern());
							 DSBData.setHostPort(port);
							 dsbYes.setEnabled(false);
							 dsbNo.setEnabled(false);
							 nodePanel.setVisible(false);
							 hostPanel.setVisible(true);
							 nodeTypeSelectPanel.setVisible(false);
							 mainframe.startButton.setEnabled(false);
							 return;
						 }else if(option == JOptionPane.NO_OPTION){
							 return;
						 }
					}
				}
				return;
			}
			finally{
				typeSelect.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	/**
	 * 定义addnode事件
	 * 检查完ip不冲突后， 就向子节点发送addnode请求
	 * @author keyalin
	 *
	 */
	class AddNodeEvent implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			addNodeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null == future || future.isCancelled()){
					Utility.showMessage("Aborted already!");
					return;
				}
				String ip = mainframe.nodeIpText.getText().trim();
				int port = Utility.convertNumberStringToInt(mainframe.portText.getText().trim());
				String seeds = mainframe.seedsText.getText().trim();
				if(seeds.equals("")) {
					if(-1 != DSBData.dHostTable.containNode(ip)){
						Utility.showMessage("The node: " + ip + " has not join us before!");
						return;
					}
					seeds = null;
				}
				else{//检验分配的任务是否重复
					if(0 != DSBData.dHostTable.containNode(ip)){
						Utility.showMessage("the node: " + ip + " must be a new node");
						return;
					}
					for(String str : seeds.split("")){
						if(null != DSBData.dHostTable.containHost(Utility.getHost(str))){
							Utility.showMessage(str + " has been captured already!");
							return;
						}
					}
				}
				if(1 == DSBData.dHostTable.containNode(ip)) {
					Utility.showMessage("The node: " + ip + " have already existed!");
					return;
				}	
				MainUnitCom req = new MainUnitCom();
				try {
					req.setAddNodeReq(DSBData.getHostIP(), DSBData.getHostPort(), seeds);
				} catch (NotCallException e1) {
					Utility.showMessage("Request pack failed!");
					return;
				}
				if(0 == Utility.sendRequest(req, ip, port)){
					Utility.showMessage("Request send failed!");
					return;
				}
				else {
					Utility.showMessage("Request send succeed, please wait!");
					return;
				}
			}
			finally{
				addNodeButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		
	}
	
	/**
	 * 删除一个节点，发送请求成功后，清除与这个子节点相关的数据，
	 * @author keyalin
	 *
	 */
	class DeleteNodeEvent implements ActionListener{
		public void actionPerformed(ActionEvent e){
			deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null == future || future.isCancelled()){
					Utility.showMessage("Aborted already!");
					return;
				}
				String ip = mainframe.nodeIpText.getText().trim();
				int port = Utility.convertNumberStringToInt(mainframe.portText.getText().trim());
				boolean clear = true;
				int i = JOptionPane.showConfirmDialog(null, "The node: " + ip + " will be deleted, " +
						"and the data of the node will be clear, are you sure", "hey", 
						JOptionPane.YES_NO_OPTION);
				if(JOptionPane.NO_OPTION == i){
					return;
				}
				else{
					MainUnitCom req = new MainUnitCom();
					try {
						req.setDeleteReq(DSBData.getHostIP(), clear);
					} catch (NotCallException e1) {
						e1.printStackTrace();
						return;
					}
					if(0 == Utility.sendRequest(req, ip, port)){
						Utility.showMessage("Request send failed");
						return;
					}
					else{
						DSBData.dHostTable.deleteNode(ip);
						Object[][] data = DSBData.dHostTable.getNodeData();
						mainframe.repaintNodesTable(data);
						return;
					}
				}
			}finally{
				deleteButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	/**
	 * 移除一个节点， 但是不删除该节点的抓取数据
	 * @author keyalin
	 *
	 */
	class RemoveNodeEvent implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null == future || future.isCancelled()){
					Utility.showMessage("Aborted already!");
					return;
				}
				String ip = mainframe.nodeIpText.getText().trim();
				int port = Utility.convertNumberStringToInt(mainframe.portText.getText().trim());
				boolean clear = false;
				int i = JOptionPane.showConfirmDialog(null, "The node: " + ip + " will be removed, " +
						"and the data of the node will not be clear, are you sure", "hey", 
						JOptionPane.YES_NO_OPTION);
				if(JOptionPane.NO_OPTION == i){
					return;
				}
				else{
					MainUnitCom req = new MainUnitCom();
					try {
						req.setDeleteReq(DSBData.getHostIP(), clear);
					} catch (NotCallException e1) {
						e1.printStackTrace();
						return;
					}
					if(0 == Utility.sendRequest(req, ip, port)){
						Utility.showMessage("Request send failed");
						return;
					}
					else{
						DSBData.dHostTable.removeNode(ip);
						Object[][] data = DSBData.dHostTable.getNodeData();
						mainframe.repaintNodesTable(data);
						return;
					}
				}
			}finally{
				removeButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		
	}
	
	/**
	 * 停止主机的运行， 向所有的子节点发送removenode请求， 然后停止后台监视进程, 并且保存数据
	 * @author keyalin
	 *
	 */
	class AbortEvent implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			abortButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null == future || future.isCancelled()){
					Utility.showMessage("Aborted already!");
					return;
				}
				int i = JOptionPane.showConfirmDialog(null, "Are you sure to abort?", "hey", JOptionPane.YES_NO_OPTION);
				if(JOptionPane.NO_OPTION == i) return;
				Object[][] data = DSBData.dHostTable.getNodeData();
				for(Object[] a : data){
					if(null == a[1]) break;
					String ip = a[1].toString();
					int port = Utility.convertNumberStringToInt(a[2].toString());
					String active = a[3].toString();
					if(active.equals("false")) continue;
					MainUnitCom req = new MainUnitCom();
					try {
						req.setDeleteReq(DSBData.getHostIP(), false);
					} catch (NotCallException e1) {
						e1.printStackTrace();
					}
					if(0 == Utility.sendRequest(req, ip, port)){
						Utility.showMessage("Send the request to the node: " + ip + " failed");
					}
					DSBData.dHostTable.removeNode(ip);
				}
				listenTask.dataSave();
				data = DSBData.dHostTable.getNodeData();
				mainframe.repaintNodesTable(data);
				future.cancel(true);
//				exec.shutdownNow();
				return;
			}finally{
				abortButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
		}
		
	}

	/**
	 * host返回nodeTypeSelectPanel页面。
	 * 先要检查任务是否已经终止, 然后将listenTask和future都设置为null，在释放相关按钮
	 * @author keyalin
	 *
	 */
	class BackButton1Event implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			backButton1.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null != future && !future.isCancelled()){
					Utility.showMessage("Please abort first!");
					return;
				}
				listenTask = null;
				future = null;
				hostPanel.setVisible(false);
				nodePanel.setVisible(false);
				nodeTypeSelectPanel.setVisible(true);
				dsbYes.setEnabled(true);
				dsbNo.setEnabled(true);
				startButton.setEnabled(true);
			}finally{
				backButton1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}		
	}
	
	/**
	 * 主节点向子节点分配任务
	 * @author keyalin
	 *
	 */
	class SendButtonEvent implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				String ip = mainframe.nodeIpText.getText().trim().intern();
				int port = Utility.convertNumberStringToInt(mainframe.portText.getText().trim());
				String seeds = mainframe.seedsText.getText().trim();
				for(String str : seeds.split(";")){
					if(null != DSBData.dHostTable.containHost(Utility.getHost(str))){
						Utility.showMessage(str + " has been captured already!");
						return;
					}
				}
				MainUnitCom req = new MainUnitCom();
				try {
					req.setHostReq(null, true, seeds);
				} catch (NotCallException e1) {
					e1.printStackTrace();
				}
				if(0 == Utility.sendRequest(req, ip, port)){
					Utility.showMessage("Send message failed");
					return;
				}
				else {
					Utility.showMessage("Send message succeed!");
					return;
				}
			}finally{
				sendButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
		}
		
	}
	
	/**
	 * 定义子节点的join请求事件。 若子节点为新增节点， oldIp为null， 若抓取起点为history， seeds为null
	 * @author keyalin
	 *
	 */
	class JoinEvent implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			joinButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null == future || future.isCancelled()){
					Utility.showMessage("Abort already!");
					return;
				}
				String oldIp = oldIpText.getText().trim();
				String ip = hostIpText.getText().trim();//注意这是主机的ip
				int port = Utility.convertNumberStringToInt(hostPortText.getText().trim());
				String seeds = nodeSeedsText.getText().trim();
				if(oldIp.equals("")) oldIp = null;
				if(seeds.equals("")) seeds = null;
				NodeCom req = new NodeCom();
				try {
					req.setJoinReq(oldIp, DSBData.getNodeIP(), DSBData.getNodePort(), seeds);
				} catch (NotCallException e1) {
					Utility.showMessage("Request pack failed!");
					return;
				}
				if(0 == Utility.sendRequest(req, ip, port)){
					Utility.showMessage("Request send failed!");
					return;
				}
				else {
					Utility.showMessage("Request send succeed, please wait response");
					return;
				}
			}finally{
				joinButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		
	}

	/**
	 * 定义子节点退出host主机的事件类。 过程: 检查是否已经与主机连接， 用户确认是否退出， 提示用户是否清除数据，
	 * 发送quit请求， 保存相关数据，取消监视任务， 抓取网页线程终止
	 * @author keyalin
	 *
	 */
	class QuitEvent implements ActionListener{
		public void actionPerformed(ActionEvent e){
			quitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null == future || future.isCancelled()){
					Utility.showMessage("The node have already disconnected to the host!");
					return;
				}
				String ip = hostIpText.getText().trim();//注意这是主机的ip
				int port = Utility.convertNumberStringToInt(hostPortText.getText().trim());
				int i = JOptionPane.showConfirmDialog(null, "Are you sure to quit?", 
						"Hey", JOptionPane.YES_NO_OPTION);
				if(JOptionPane.NO_OPTION == i) return;
				else {
					boolean clear;
					int j = JOptionPane.showConfirmDialog(null, "Do you want the host to clear" +
							" your data", "Hey", JOptionPane.YES_NO_OPTION);
					if(JOptionPane.NO_OPTION == j) clear = false;
					else clear = true;
					NodeCom req = new NodeCom();
					try {
						req.setQuitReq(DSBData.getNodeIP(), clear);
					} catch (NotCallException e1) {
						e1.printStackTrace();
						Utility.showMessage("Operation failed");
						return;
					}
					if(0 == Utility.sendRequest(req, ip, port)){
						Utility.showMessage("Message send failed!");
					}
					listenTask.dataSave();
					future.cancel(true);
//					exec.shutdownNow();
					dTaskStop();//停止抓取	
				}
			}finally{
				quitButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}			
		}
	}

	/**
	 * 子节点返回到nodeTypePanel页面
	 * @author keyalin
	 *
	 */
	class BackButton2Event implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			backButton2.setCursor(new Cursor(Cursor.HAND_CURSOR));
			try{
				if(null != future && !future.isCancelled()){
					Utility.showMessage("Please abort first!");
					return;
				}
				listenTask = null;
				future = null;
				hostPanel.setVisible(false);
				nodePanel.setVisible(false);
				nodeTypeSelectPanel.setVisible(true);
				dsbYes.setEnabled(true);
				dsbNo.setEnabled(true);
			}finally{
				backButton2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}		
	}
	
	/**
	 * 获得一个可用于serversocket的监视端口， 和本地ip地址
	 * @return IPandPort实例
	 */
	private IPandPort getIPandPort(){
		Random ran = new Random(Calendar.getInstance().get(Calendar.SECOND));
		String ip = null;
		int port = -1;
		try{
			ip = InetAddress.getLocalHost().getHostAddress();
		}catch(UnknownHostException e2){
			JOptionPane.showMessageDialog(null, "can't obtain the local ip",
					   							"Hey", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		ServerSocket socket = null;
		int i = 0;
		while(true){
			if(5 == i){
				Utility.showMessage("can't get an port");
				return null;
			}
			port = ran.nextInt(500) + 500;
			try{
				socket = new ServerSocket(port);
				IPandPort data = new IPandPort();
				data.ip = ip;
				data.port = port;
				socket.close();
				return data;
			}catch(BindException e1){
				i++;
				continue;
			}
			catch(Exception e1){
				Utility.showMessage("can't get an port");
				return null;
			}
			finally{
				i++;
				try{
					socket.close();
				}catch(Exception e1){}				
			}
		}		
	}
	
	/**
	 * 数据结构， 一个ip字段， 和一个port字段
	 * ip string
	 * port int
	 * @author keyalin
	 *
	 */
	class IPandPort{
		String ip;
		int port;
	}
}

/**
 * 定义了一个图片查看器
 * @author keyalin
 *
 */
class Pic extends JDialog {
	private static final long serialVersionUID = 6083186083504879412L;
	ResultSet set = null;
	boolean exist = true;
	
    public Pic(ResultSet set) {
       initComponents();
       this.setVisible(false);
       this.setSize(400, 400);
       this.jLabel1.setText(null);
       this.previousbutton.addActionListener(new PreviousEvent());
       this.nextButton.addActionListener(new NextEvent()); 
       this.set = set;
       try {
    	   set.next();
       } catch (SQLException e) {
    	   e.printStackTrace();	
    	   exist = false;
    	   this.jLabel1.setText("NO DATA");
       }
       
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        previousbutton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.95;
        gridBagConstraints.weighty = 0.95;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jLabel1, gridBagConstraints);

        previousbutton.setText("previous");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(previousbutton, gridBagConstraints);

        nextButton.setText("next");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(nextButton, gridBagConstraints);

        pack();
    }// </editor-fold>

    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousbutton;
    
    public void dispose(){
    	try{
    		set.close();
    	}catch(Exception e){}
    	super.dispose();
    }
    // End of variables declaration
    class PreviousEvent implements ActionListener{
		public void actionPerformed(ActionEvent e){	
			BufferedInputStream in = null;
			BufferedOutputStream out = null;
			try{
				if(false == exist) return;			 
				else{
					if(set.isFirst()) {
						Utility.showMessage("Aready first"); 
						return;
					}
					jLabel1.setText(null);
					set.previous();
					Blob b = set.getBlob(1);
					in = new BufferedInputStream(b.getBinaryStream());
					byte[] bytes = Utility.readBufferToBytes(in);
					File file = new File("temp/temp." + set.getString(2));
					if(file.exists()) file.delete();
					out = new BufferedOutputStream(new FileOutputStream(file));
					out.write(bytes);
					out.flush();
					jLabel1.setIcon(null);
					jLabel1.setIcon(new ImageIcon("temp/temp." + set.getString(2)));
				}
			}catch(SQLException e1){
				e1.printStackTrace();
				try{
					set.close();
				}catch(Exception e2){}
			}
			catch(IOException e1){}	
			finally{
				try{
					in.close();
					out.close();
				}
				catch(Exception e2){}
			}
		}			  	
    }
    
    class NextEvent implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			BufferedInputStream in = null;
			BufferedOutputStream out = null;
			try{
				if(false == exist) return;			 
				 else{
					 if(set.isLast()) {
						 Utility.showMessage("Aready last"); 
						 return;
					 }
					 jLabel1.setText(null);
					 set.next();
					 Blob b = set.getBlob(1);
					 in = new BufferedInputStream(b.getBinaryStream());
					 byte[] bytes = Utility.readBufferToBytes(in);
					 File file = new File("temp/temp." + set.getString(2));
					 if(file.exists()) file.delete();
					 out = new BufferedOutputStream(new FileOutputStream(file));
					 out.write(bytes);
					 out.flush();
					 jLabel1.setIcon(null);
				     jLabel1.setIcon(new ImageIcon("temp/temp." + set.getString(2)));
				 }
			}catch(SQLException e1){
				e1.printStackTrace();
				try{
					set.close();
				}catch(Exception e2){}
			}
			catch(IOException e1){
				e1.printStackTrace();
			}	
			finally{
				try{
					in.close();
					out.close();
				}
				catch(Exception e2){}
			}
		}  
	}
}
