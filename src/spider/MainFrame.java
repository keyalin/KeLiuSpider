/**
 * ���ļ�����������������е��¼���
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
	   
   //�Զ�������
   	private MainFrame mainframe = this;
   	private Manager taskManager = null;
   	//�Զ�������
   
   /*
    * �޸�mainframe��������ݽӿ�
    */
   
   //�޸���ʾ����
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
   
   	//�ṩ�޸�capturetable���ݵĽӿ�
   	//�޸ĵ���ѡ��
   	public  synchronized void setValueAtCaptureTable(int row, int column, Object value){
   		this.captureTable.setValueAt(value, row, column);
   	}
   	
//	   	//���������� �Ժ��õ�
//	   	public synchronized void repaintCaptureTable(Object[][] data){
//	   		captureTableDefaultTableModel.setDataVector(data, new String [] {
//		               "threadname", "url", "capturetype", "size", "status"
//	        });
//	   	}
   	
   	public synchronized Object getValueAtCaptureTable(int row, int column){
   		return captureTable.getValueAt(row, column);
   	}
   	
   	
   	/**
   	 * ���ֲ�ʽ�����ѽ����� ��ô����ʾnodetypeselectpanel
   	 */
   	public void dRunAgain(){
   		this.hostPanel.setVisible(false);
   		this.nodePanel.setVisible(false);
   		this.nodeTypeSelectPanel.setVisible(true);
   	}
   /*
    * �޸�mainframe��������ݽӿ���
    */
   	
   	
   /*
    * �¼������������, �����ط���Ҫ�޸���
    */

   	/**
   	 * �������ذ�ť����
   	 * �ȸ���distribute��ѡ��ȷ��taskmanager������
   	 * �ٸ���history��ѡ��ȷ��ץȡ��㡣
   	 * Ȼ����histoy��ť���ɱ༭
   	 * @keyalin
   	 */
   	class StartButtonEvent implements ActionListener{
   		public void actionPerformed(ActionEvent e){
   			startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
   			try{
   				int i = JOptionPane.showConfirmDialog(null, "Please check your setting, are " +
   					"you sure?", "hey", JOptionPane.YES_NO_OPTION);
   				if(JOptionPane.NO_OPTION == i) return;
   				if(null != taskManager){//����������
   					JOptionPane.showMessageDialog(null, "Capturing has been already started!");
   					return;
   				}
   				if(dsbNo.isSelected()){//ѡ��taskmanager������
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
   					//����ذ�ť���ܰ�seed, history
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
   					//����ذ�ť���ܰ�seed, history
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
   	 * ����stop��ť������
   	 * �ȼ����������û��
   	 * �ͷ�history��button
   	 * @author keyalin
   	 *
   	 */
   	class StopButtonEvent implements ActionListener{
   		public void actionPerformed(ActionEvent e){
   			stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
   			try{
   				if(null == taskManager){//����û������
   				JOptionPane.showMessageDialog(null, "Task has not started yet!");
   				stopButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   				return;
	   			}
	   			taskManager.stop();
	   			taskManager = null;
	   			seed.setEnabled(true);//�ͷŰ�ť
	   			history.setEnabled(true);
	   			return;	
   			}finally{
   				stopButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}   			
   		}
   	}
   	
   	/**
   	 * ������ͣ��ť�ļ�����
   	 * ��Ҫ���task�Ƿ��Ѿ�����
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
     * ����links JComboBox , imags JComboBox, JComboBox timeout,
     * JComboBox savePeriod �������࣬ �Ի���û����õĲ���. Ȼ�󽫱�������ݴ��
     * ��getsettingvalue����
     */
    
    abstract class SelectEvent implements ItemListener{
 	   public abstract void itemStateChanged(ItemEvent e);
    }
    
    /**
     * ���û��޸�pageThreadcount�������¼���
     * ע����������Ķ�̬��ʾ
     * @author keyalin
     *
     */
    class LinksSelectEvent extends SelectEvent{
 	   public void itemStateChanged(ItemEvent e) {
 		   String str = e.getItem().toString(); 		   
 		   GetSettingValue.setPageThreadCount(Utility.convertNumberStringToInt(str));
 		   str = null;
 		   if(null != taskManager){
 			   taskManager.changeUsage();//ɱ�����ߴ���һЩ�̣߳� ͬʱ��̬��ʾ
 			   return;
 		   }
 	   }	   
    }
    
    /**
     * ���û��޸�imageThreadcount�������¼���
     * ע����������Ķ�̬��ʾ
     * @author keyalin
     *
     */
    class ImageSelectEvent extends SelectEvent{
 	   public void itemStateChanged(ItemEvent e) {
 		   String str = e.getItem().toString(); 		   
		   GetSettingValue.setImageThreadCount(Utility.convertNumberStringToInt(str));
		   str = null;
		   if(null != taskManager){
			   taskManager.changeUsage();//ɱ��һЩ�̣߳� ͬʱ��̬��ʾ
			   return;
		   }
 	   }
    }
    
    /**
     * �޸���ҳץȡ�ĵȴ�ʱ��
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
     * �޸Ķ��ڱ����ʱ��
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
    * ����û����õ�database�����Ƿ���� ������ �ͽ��û�����database�������浽GetSettingValue����
    * �����¼������databasepanel�����saveButton����������
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
			   Utility.showMessage("Database setting succeed��");
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
   
   /* 1. ��ѯĳ��ͼƬurl����Ӧ��ͼƬ�� image �� display��ť
    * 2. ��ѯĳ����ҳurl����Ӧ����ҳ�������ݡ� page �� display��ť��
    * 3. ͳ��ĳ��host��ͼƬ���ݡ� image �� query ��ť
    * 4. ͬ���ĳ��host����ҳ���ݡ� page �� query ��ť��
    */
   
   /**
    * ����display��ť�ļ�����
    * ��ʾ��ͬhost��ͼƬ
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
   				if(image.isSelected()){//������ʾͼƬ
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
   					new Pic(set).setVisible(true);//��ʾͼ�β鿴   					
   				}
   				else if(page.isSelected()){//��ʾ��ҳ���ĺ͹ؼ�����
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
   						displayTextArea.append("��ҳ���Ĺؼ����֣�\n");
   						displayTextArea.append(DomParser.getMainText(html) + "\n\n");
   						System.out.println(html);
   						System.out.println(DomParser.getTotalText(html));
   						displayTextArea.append("��ҳ����ȫ����\n");
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
   						displayTextArea.setText("û��������������ݿ���");
   					}
   				}
   			} finally{
   				displayButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   			}
   		}
   }
   
   /**
    * ����querybutton�ļ�����
    * ����Ҫ���ͳ��ÿ������host��urls������ imags������ �����ڽ�����չʾ�� 
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
	   					queryTextArea.append("�ܹ���ѯ��" + i + "������:\n");
	   					set.beforeFirst();
	   					while(set.next()){
	   						queryTextArea.append(set.getString(1) + "\n");
	   					}
	   				}catch(SQLException e1){
	   					queryTextArea.setText("����ʧ�ܣ� " + e1);
	   				}catch(NullPointerException e2){
	   					queryTextArea.setText("û�����������ݿ���");
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
	   					queryTextArea.append("�ܹ���ѯ��" + i + "������:\n");
	   					set.beforeFirst();
	   					while(set.next()){
	   						queryTextArea.append(set.getString(1) + "\n");
	   					}
	   				}catch(SQLException e1){
	   					queryTextArea.setText("����ʧ�ܣ� " + e1);
	   				}catch(NullPointerException e2){
	   					queryTextArea.setText("û�����������ݿ���");
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
	   
	   //�ֲ�ʽ��������￪ʼ
	   /*
	    *���� 
	    */
   	private ListenTask listenTask = null;
	private Future future = null;
	private ExecutorService exec = Executors.newCachedThreadPool();
	
	//
	/**
	 * �ṩ�޸�nodestable�Ľӿ�
	 */
   	public synchronized void repaintNodesTable(Object[][] data){
   		DefaultTableModel tableModel = new DefaultTableModel(data, new String [] {
                "linenumber", "node ip", "node port", "active"
   	    });
		nodesTable.setModel(tableModel);
	}
   	
   	/**
   	 * �ṩֹͣlisten����Ľӿ�
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
 	 * Ϊ�ֲ�ģʽ�ṩֹͣץȡ����Ľӿ�
 	 */
   	public void dTaskStop(){
   		if(null == taskManager){//����û�������� �����Ѿ�ֹͣ
			return;
		}
		taskManager.stop();
		taskManager = null;
		seed.setEnabled(true);//�ͷŰ�ť
		history.setEnabled(true);
		this.dsbNo.setEnabled(true);
		this.dsbYes.setEnabled(true);
   	} 
   	
   /**
    * ����û�����Ϊ�ڵ㻹����Ϊ����
    * ��setting����distributedѡ�����no����û�б�ѡ�� ��ʾ�û���ǰ������ѡ��distibutedģʽ
    * ��setting����distributedѡ�����yes����ô�������²�����
    * ��dHostButton��ѡ�� ��ôֻ��ʾdHostPanel, ��dNodeButton��ѡ�� ��ôֻ��ʾdNodePanel
    * Ȼ�� ȷ����ǰ��ip�� �ͼ��Ӷ˿ڣ� ������̨���ӽ��̡� ͬʱԤ��ȡhistory���ץȡ��Ϣ��
    * ����Ԥ��ȡ��Ϣ�ɣ�dhosttable�� �ӽڵ�Ԥ��ȡvisitedhost�� visitedUrls, visitedimages, totoHrefs,
    * �Լ�todoImages�� ��Щ��Ϣ������λ��DSBdata�С������Ϣ�Ķ�ȡ�ü��ӽ���ȡ��ɡ�
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
	 * ����addnode�¼�
	 * �����ip����ͻ�� �����ӽڵ㷢��addnode����
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
				else{//�������������Ƿ��ظ�
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
	 * ɾ��һ���ڵ㣬��������ɹ������������ӽڵ���ص����ݣ�
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
	 * �Ƴ�һ���ڵ㣬 ���ǲ�ɾ���ýڵ��ץȡ����
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
	 * ֹͣ���������У� �����е��ӽڵ㷢��removenode���� Ȼ��ֹͣ��̨���ӽ���, ���ұ�������
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
	 * host����nodeTypeSelectPanelҳ�档
	 * ��Ҫ��������Ƿ��Ѿ���ֹ, Ȼ��listenTask��future������Ϊnull�����ͷ���ذ�ť
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
	 * ���ڵ����ӽڵ��������
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
	 * �����ӽڵ��join�����¼��� ���ӽڵ�Ϊ�����ڵ㣬 oldIpΪnull�� ��ץȡ���Ϊhistory�� seedsΪnull
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
				String ip = hostIpText.getText().trim();//ע������������ip
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
	 * �����ӽڵ��˳�host�������¼��ࡣ ����: ����Ƿ��Ѿ����������ӣ� �û�ȷ���Ƿ��˳��� ��ʾ�û��Ƿ�������ݣ�
	 * ����quit���� ����������ݣ�ȡ���������� ץȡ��ҳ�߳���ֹ
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
				String ip = hostIpText.getText().trim();//ע������������ip
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
					dTaskStop();//ֹͣץȡ	
				}
			}finally{
				quitButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}			
		}
	}

	/**
	 * �ӽڵ㷵�ص�nodeTypePanelҳ��
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
	 * ���һ��������serversocket�ļ��Ӷ˿ڣ� �ͱ���ip��ַ
	 * @return IPandPortʵ��
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
	 * ���ݽṹ�� һ��ip�ֶΣ� ��һ��port�ֶ�
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
 * ������һ��ͼƬ�鿴��
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
