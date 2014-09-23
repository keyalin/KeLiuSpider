/**
 * 该文件定义了子节点和主节点交互的两个类DNodeListenTask， DHostListenTask
 */
package spider;

import java.net.*;
import java.util.concurrent.*;
import java.io.*;
import javax.swing.*;


/**
 * 定义listen任务模板, 它有两个继承类：
 * 子节点监视线程的任务类DNodeListenTask， 总机节点监视线程的任务类DHostListenTask
 * 注意定义一个定期检查的任务
 * @author keyalin
 *
 */
public abstract class ListenTask implements Runnable{
	protected MainFrame mainframe;
	protected String ip;
	protected int port;
	protected ServerSocket server;
	protected ListenTask(String ip, int port, MainFrame mainframe){
		this.mainframe = mainframe;
		this.ip = ip;
		this.port = port;//这里存储本机的ip地址和监视端口
	}
	protected void showMessage(String message){
		JOptionPane.showMessageDialog(null, message);
	}
	public abstract void run();
	public abstract void dataSave();
	public void stopListen(){
		try{
			server.close();
		}catch(Exception e){}
	}
}






