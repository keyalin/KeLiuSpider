/**
 * ���ļ������йطֲ�ʽͨ�ŵĽڵ���ܻ���ȡ��¼�����ݽṹ, ȫ������������hash��������ݽṹ
 * DHostTable �����洢�����ӽڵ��hostץȡ����
 * VisitedHost �ӽڵ������洢�Լ��Ѿ�ץȡ�ĵ���ʧ�ܵ�host��
 * @author keyalin
 *
 */
package spider;

import java.util.concurrent.*;
import java.io.*;

public class DSBDataStructures implements Serializable{
	private static final long serialVersionUID = -3317612658528239799L;

}

/**
 * ����������¼ÿ���ڵ�����������Ϣ�� ip�� port�� seed��active�� �Լ����Ե�host��
 * ͬʱ�������޸���Щ���ݵķ���
 * @author keyalin
 *
 */
class DHostTable extends DSBDataStructures{

	private static final long serialVersionUID = 8303743655101712675L;
	
	/*
	 * �����ͷ
	 */
	private DSubNode rootNode;
	
	/**
	 * ����hostTable��ÿ���ֽڵ�����ݽṹ
	 * ip �ӽڵ��ip
	 * port �ӽڵ�ļ���port
	 * rootHost ÿ���ӽڵ���ץȡhost��ı�ͷ
	 * active ���Ա�ʶ�ýڵ��Ƿ��Ծ�� �ӽڵ��˳�ʱ�� ֻ�ñ�ʶΪfalse�Ϳɣ������ӽڵ����ݾͲ��ñ�ɾ���� �������ڱ��С�
	 * next ָ�������һ���ӽڵ�
	 * @author keyalin
	 *
	 */
	class DSubNode implements Serializable{
		private static final long serialVersionUID = -8395719091146985739L;
		String ip;
		int port;
		DHost rootHost;
		Boolean active;
		DSubNode next;
	}
	
	/**
	 * �ӽڵ��host��ı�ѡ�����ݽṹ
	 * value hostֵ
	 * nextָ����һ��DHostʵ��
	 * @author keyalin
	 *
	 */
	class DHost implements Serializable{
		private static final long serialVersionUID = -6608508236692726112L;
		String value;
		DHost next;
	}
	
	/**
	 * ����һ���µ�node�ڵ㣬 activeĬ��Ϊtrue
	 * @param ip �ӽڵ��ip��ַ
	 * @param port �ӽڵ�ļ��Ӷ˿�
	 * @return ����һ���ӽڵ�ʵ��
	 */
	private DSubNode createNewDHost(String ip, int port){
		DSubNode newNode = new DSubNode();
		newNode.ip = ip;
		newNode.port = port;
		newNode.active = true;
		newNode.rootHost = new DHost();//����ֻ�Ǵ���һ���յĽڵ�host��ͷ
		newNode.rootHost.value = null;
		newNode.rootHost.next = null;
		newNode.next = null;
		return newNode;
	}
	
	/**
	 * ��ʼ��rootnode, �������ý����Ǵ���һ��subnodetable�ı�ͷ��ָ���һ��subnode�ڵ㡣
	 */
	public DHostTable(){
		rootNode = new DSubNode();
		rootNode.ip = null;
		rootNode.port = -1;
		rootNode.rootHost = null;
		rootNode.active = null;
		rootNode.next = null;
	}
	

	/**
	 * ���ڵ��������ڵ㣬��subnode��βֱ��������½ڵ�
	 * @param ip �����ڵ��ip��ַ
	 * @param port �����ڵ��ͨ�Ŷ˿�
	 * @return 
	 */
	public synchronized void addNode(String ip, int port){		
		DSubNode previous = rootNode;
		while(null != previous.next){
			previous = previous.next;
		}
		DSubNode newNode = createNewDHost(ip, port);
		previous.next = newNode;
	}
	
	/**
	 * ���¼���Ľڵ���ǰ������� ��ֻ���޸���Ӧ��activeֵ����
	 * @param ip ���¼���Ľڵ�ip��ַ
	 * @param port ���¼���Ľڵ��port��ַ
	 * @return 0��ʾ�ýڵ��ڱ��в����ڣ� 1��ʾ�޸ĳɹ�
	 */
	public synchronized int recoveryNode(String ip, int port){
		DSubNode nodeQuery = rootNode.next;
		while(null != nodeQuery){
			if(ip.equals(nodeQuery.ip)){
				nodeQuery.port = port;
				nodeQuery.active = true;
				return 1;
			}
			nodeQuery = nodeQuery.next;
		}
		return 0;
	}
	
	/**
	 * �ڱ����Ƴ�һ���ڵ㣬 ע���ʱ�ڵ����ݲ���ɾ��
	 * @param ip Ҫ�Ƴ��ڵ��ip
	 * @return 0��ʾ�ýڵ㲻���ڣ� 1��ʾɾ���ɹ�
	 */
	public synchronized int removeNode(String ip){
		DSubNode nodeQuery = rootNode.next;
		while(null != nodeQuery){
			if(ip.equals(nodeQuery.ip)){
				nodeQuery.active = false;
				return 1;
			}
			nodeQuery = nodeQuery.next;
		}
		return 0;
	}
	
	/**
	 * �ڱ����Ƴ�һ���ڵ㣬 ͬʱ�ڵ����ݱ�����ɾ��
	 * @param ip ָ���ڵ��ip��ַ
	 * @return 0��ʾ�ڵ㲻���ڣ� 1��ʾɾ���ɹ�
	 */
	public synchronized int deleteNode(String ip){
		DSubNode previous = rootNode;
		DSubNode next = previous.next;
		while(null != next){
			if(ip.equals(next.ip)){
				previous.next = next.next;
				next = null;
				return 1;
			}
			previous = next;
			next = next.next;
		}
		return 0;
	}
	
	/**
	 * ��ձ����е�����
	 * @return 1 ��ʾ�ɹ���
	 */
	public synchronized int clearSubNodesTable(){
		rootNode.next = null;//���ַ����Ƿ��׵��� ���ǽ���ʶΪ����Ϊfalse�Ƚ��׵����᲻������ڴ�й¶
		return 1;
	}
	
	/**
	 * ��ѯhost�Ƿ���subnode����
	 * @param host ָ����host
	 * @return  ���ذ���host��ip��ַ��������в����ڸ�host�� ����null
	 */
	public synchronized String containHost(String host){
		DSubNode nodeQuery = rootNode.next;
		while(null != nodeQuery){
			DHost hostQuery = nodeQuery.rootHost.next;
			while(null != hostQuery){
				if(host.equals(hostQuery.value)){
					return nodeQuery.ip.intern();
				}
				hostQuery = hostQuery.next;
			}
			nodeQuery = nodeQuery.next;
		}
		return null;
	}
	
	/**
	 * ��hostֱ����ӵ���ӵ�ָ��ip��host���β��
	 * @param ip ��host��������ip��ַ
	 * @param host hostֵ
	 * @return 1��ʾ�ɹ��� 0��ʾʧ��
	 */	 
	public synchronized int addHost(String ip, String host){
		DSubNode nodeQuery = rootNode.next;
		while(null != nodeQuery){
			if(ip.equals(nodeQuery.ip)){
				DHost hostQuery = nodeQuery.rootHost;
				while(null != hostQuery.next){
					hostQuery = hostQuery.next;
				}
				DHost newHost = new DHost();
				newHost.value = host;
				newHost.next = null;
				hostQuery.next = newHost;
				return 1;
			}
			nodeQuery = nodeQuery.next;
		}
		return 0;
	}
	
	/**
	 * ɾ��ָ��ip�ڵ��hostֵ
	 * @param host hostֵ
	 * @param ip  ָ���ڵ��ip
	 * @return 1��ʾ�ɹ��� 0 ��ʾhost������
	 */
	public synchronized int removeHost(String ip, String host){
		DSubNode nodeQuery = rootNode;
		while(null != nodeQuery){
			if(ip.equals(nodeQuery.ip)){
				DHost previous = nodeQuery.rootHost;
				DHost next = previous.next;
				while(null != next){
					if(host.equals(next.value)){
						break;
					}
					else{
						previous = next;
						next = next.next;
					}
				}
				if(null == next) return 0;
				else {
					previous.next = next.next;
					next = null;
					return 1;
				}
			}
			nodeQuery = nodeQuery.next;
		}
		return 0;
	}
	
	/**
	 * ���ӽڵ�ip��port�����仯�� ����Ҫ��������ӽڵ��ip�� port��ע���µ�ip��Ҫ��ĳ���ڵ��ip�ظ�
	 * �������������֮ǰ����Ҫ����containNode������ȷ��ip��Ψһ�ԡ�
	 * @param oldIp �޸�֮ǰ���ӽڵ�ip��ַ
	 * @param newIp	�޸�֮����ӽڵ�ip��ַ
	 * @param port	�޸�֮����ӽڵ�port�˿�
	 * @return 1��ʾ�޸ĳɹ��� 0��ʾ������oldip�Ľڵ㡣
	 */
	public synchronized int updateIpPort(String oldIp, String newIp, int newPort){
		DSubNode nodeQuery = rootNode.next;
		while(null != nodeQuery){
			if(oldIp.equals(nodeQuery.ip)){
				nodeQuery.ip = newIp;
				nodeQuery.port = newPort;
				return 1;
			}
			nodeQuery = nodeQuery.next;
		}
		return 0;
	}
	
	/**
	 * ����ָ��ip��node�Ƿ���subnodetable����
	 * @param ip
	 * @return 0��ʾ�����ڣ� 1��ʾ���ڣ� -1��ʾ���ڵ��ǲ���Ծ
	 */
	public synchronized int containNode(String ip){
		DSubNode nodeQuery = rootNode.next;
		while(null != nodeQuery){
			if(ip.equals(nodeQuery.ip)){
				if(true == nodeQuery.active) return 1;
				else return -1;
			}
			nodeQuery = nodeQuery.next;
		}
		return 0;
	}
	
	/**
	 * ��dhosttable�����нڵ�Ľڵ��ţ�ip�� port�� activeֵ��װ��object[40][4]���淵�أ� �����ٲ�ѯ
	 * ���ӽڵ㳬��40���� ����
	 * @return Object[40][3] 
	 */
	 public synchronized Object[][] getNodeData(){
		 Object[][] nodeData = new Object[40][4];
		 DSubNode nodeQuery = rootNode.next;
		 int i = 0;
		 while(null != nodeQuery){
			 nodeData[i][0] = i + 1;
			 nodeData[i][1] = nodeQuery.ip.intern();
			 nodeData[i][2] = nodeQuery.port;
			 nodeData[i][3] = nodeQuery.active;
			 i++;
			 nodeQuery = nodeQuery.next;
		 }
		 return nodeData;
	 }
}


/**
 * �����ӽڵ����Լ�ץȡʧ�ܵ�host���� ���ڵ�Ҫ���䲻ץȡ��host �����ݽṹ
 * @author keyalin
 *
 * @param <T>
 */
class DVisitedHost<T> extends LinkedBlockingQueue<T>{
	private static final long serialVersionUID = -3607593989901140772L;
}




