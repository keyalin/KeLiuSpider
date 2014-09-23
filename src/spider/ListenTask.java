/**
 * ���ļ��������ӽڵ�����ڵ㽻����������DNodeListenTask�� DHostListenTask
 */
package spider;

import java.net.*;
import java.util.concurrent.*;
import java.io.*;
import javax.swing.*;


/**
 * ����listen����ģ��, ���������̳��ࣺ
 * �ӽڵ�����̵߳�������DNodeListenTask�� �ܻ��ڵ�����̵߳�������DHostListenTask
 * ע�ⶨ��һ�����ڼ�������
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
		this.port = port;//����洢������ip��ַ�ͼ��Ӷ˿�
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






