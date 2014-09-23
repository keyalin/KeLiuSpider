package test;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import spider.Utility;

/**
 * @author keyalin
 *
 */
public class WAN extends Fangzheng{
	WAN(){
		this.setTitle("WAN");
		this.jLabel6.setText("节点带宽");
		this.setVisible(true);
		this.setSize(350, 500);
		this.broad.setText("5");
		this.start.addActionListener(new StartEvent());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	class StartEvent implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			mainframe.result.setText(null);
			int m = Utility.convertNumberStringToInt(mainframe.nodecount.getText().trim());
			int n = Utility.convertNumberStringToInt(mainframe.webnumber.getText().trim());
			int total = Utility.convertNumberStringToInt(mainframe.filecount.getText().trim());
			int timeout = Utility.convertNumberStringToInt(mainframe.timeout.getText().trim());
			int change = Utility.convertNumberStringToInt(mainframe.change.getText().trim());
			int broad = Utility.convertNumberStringToInt(mainframe.broad.getText().trim());
			int intial = Utility.convertNumberStringToInt(mainframe.intialhost.getText().trim());
			int punish = Utility.convertNumberStringToInt(mainframe.punish.getText().trim());
			
			/*
			 * 创建输入文件
			 */
			File file = new File("d:\\\\WAN.doc");
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new FileOutputStream(file, true));
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
				return;
			}
			pw.append("nodesnumber: " + m + " nodebroad: " + broad + " change: " + 
						change + "% timeout: " + timeout + "\n");
			pw.flush();
			pw.checkError();
			
			double A1, A2, A3, A4, A5;
			 A1 = A2 = A3 = A4 = A5 = 0.0;			
			for(int count = 0; count < 10; count++){
				exec = Executors.newCachedThreadPool();
			
				//初始化超时表
				Random ran = new Random(47);
				mainframe.a = new int[m][n];
				for(int[] b : mainframe.a){
					for(int i = 0; i < n; i++){
						b[i] = (ran.nextInt(10)+1);
					}
				}
				
				//初始化主节点时延表
				mainframe.b = new int[m];
				for(int i = 0; i < m; i++){
					b[i] = (ran.nextInt(10)+1);
				}
				
				//初始化各个节点
				mainframe.nodes = new Node[m];
				for(int i = 0; i < m; i++){
					mainframe.nodes[i] = new Node(i, timeout, broad);
					for(int j = i * intial; j < (i +1) * intial; j++){//初始化初始节点
						mainframe.nodes[i].todo.offer(j);
					}
				}
				
				//初始化每个web划分
				mainframe.webs = new Web[n];
				for(int i = 0; i < n; i++){
					mainframe.webs[i] = new Web();
					mainframe.webs[i].id = i;
					mainframe.webs[i].number = total/n;
				}
				
				//初始化统计数据
				mainframe.hostnumber = n;
				mainframe.nodenumber = m;
				mainframe.swap = 100/change;
				mainframe.filenumber = total;
				mainframe.punishment = punish;
				mainframe.totalsize = 0;
				mainframe.totaltime = 0;
				mainframe.validfile = 0;
				mainframe.duplicatefile = 0;//
				mainframe.comunication = 0;
				
				//开始运行任务
				for(Node node : nodes){
					exec.execute(node);
				}
				try {
					TimeUnit.SECONDS.sleep(Utility.convertNumberStringToInt
							(mainframe.runtime.getText().trim())/10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				exec.shutdownNow();
				StringBuilder sb = new StringBuilder();
				double time = mainframe.totaltime/mainframe.nodenumber;
				sb.append(count + 1 + "\n");
				sb.append("validFile: ");
				sb.append(mainframe.validfile);
				sb.append("\nduplicatefile: ");
				sb.append(mainframe.duplicatefile);
				sb.append("\ntime: ");
				sb.append(Utility.format(time));
				sb.append("\ntotalsize: ");
				sb.append(mainframe.totalsize);
				sb.append("\nmainunit comunication: ");
				sb.append(Utility.format(10000* mainframe.comunication/time));
				mainframe.result.append(sb.toString()+ "\n\n");
				
				//在文件上输出结果
//				StringBuilder result = new StringBuilder();
//				result.append("  nodesnumber  hostnumber  totalfiles  validfiles  " +
//						"duplicatefiles  downloadsize  timecost  communication\n");
//				pw.append(result.toString());
//				pw.flush();
//				pw.append(String.format("%13d%12d%12d%12d%16f%14d%10.1f%15d\n", m, n, total,
//								validfile, duplicatefile, totalsize, time, comunication));

//				pw.flush();
				double speed = totalsize/time;//time = totaltime/m
				double duplicate = mainframe.duplicatefile/mainframe.validfile;
				double srcuse = speed/(mainframe.nodenumber + 1);
				double override = 1.0 * mainframe.validfile/mainframe.filenumber;
				double communicate = 1.0 * mainframe.comunication / time;
				A1 += speed;
				A2 += duplicate;
				A3 += srcuse;
				A4 += override;
				A5 += communicate;
			}
			pw.append("     speed     duplicate     srcuse     fileoverride     comunicate\n");
			pw.append(String.format("%10.5f%14.5f%11.5f%17.5f%15.5f\n\n", A1/10, 
					A2/10, A3/10, A4/10, A5/10));	
			pw.flush();
			pw.close();
		}		
	}
	
	public static void main(String[] args) throws IOException{
		new WAN();
		Utility.showMessage("nihao");
		new JDialog().setVisible(true);
		System.out.println(Utility.linesTotal("src/spider"));
	}
}
