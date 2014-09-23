/**
 * 该文件定义有关分布式通信的节点和总机爬取记录的数据结构, 全都采用类似于hash链表的数据结构
 * DHostTable 主机存储各个子节点的host抓取数据
 * VisitedHost 子节点用来存储自己已经抓取的但是失败的host表。
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
 * 主机用来记录每个节点的相关数据信息， ip， port， seed，active， 以及各自的host表
 * 同时定义了修改这些数据的方法
 * @author keyalin
 *
 */
class DHostTable extends DSBDataStructures{

	private static final long serialVersionUID = 8303743655101712675L;
	
	/*
	 * 定义表头
	 */
	private DSubNode rootNode;
	
	/**
	 * 定义hostTable中每个字节点的数据结构
	 * ip 子节点的ip
	 * port 子节点的监视port
	 * rootHost 每个子节点已抓取host表的表头
	 * active 用以标识该节点是否活跃。 子节点退出时， 只用标识为false就可，这样子节点数据就不用被删除， 而保留在表中。
	 * next 指向表中下一个子节点
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
	 * 子节点的host表的表选项数据结构
	 * value host值
	 * next指向下一个DHost实例
	 * @author keyalin
	 *
	 */
	class DHost implements Serializable{
		private static final long serialVersionUID = -6608508236692726112L;
		String value;
		DHost next;
	}
	
	/**
	 * 创建一个新的node节点， active默认为true
	 * @param ip 子节点的ip地址
	 * @param port 子节点的监视端口
	 * @return 返回一个子节点实例
	 */
	private DSubNode createNewDHost(String ip, int port){
		DSubNode newNode = new DSubNode();
		newNode.ip = ip;
		newNode.port = port;
		newNode.active = true;
		newNode.rootHost = new DHost();//仅仅只是创建一个空的节点host表头
		newNode.rootHost.value = null;
		newNode.rootHost.next = null;
		newNode.next = null;
		return newNode;
	}
	
	/**
	 * 初始化rootnode, 它的作用仅仅是创建一个subnodetable的表头，指向第一个subnode节点。
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
	 * 若节点是新增节点，在subnode表尾直接添加中新节点
	 * @param ip 新增节点的ip地址
	 * @param port 新增节点的通信端口
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
	 * 当新加入的节点以前参与过， 则只需修改相应的active值即可
	 * @param ip 重新加入的节点ip地址
	 * @param port 重新加入的节点的port地址
	 * @return 0表示该节点在表中不存在， 1表示修改成功
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
	 * 在表中移除一个节点， 注意此时节点数据不被删除
	 * @param ip 要移除节点的ip
	 * @return 0表示该节点不存在， 1表示删除成功
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
	 * 在表中移除一个节点， 同时节点数据被彻底删除
	 * @param ip 指定节点的ip地址
	 * @return 0表示节点不存在， 1表示删除成功
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
	 * 清空表所有的数据
	 * @return 1 表示成功了
	 */
	public synchronized int clearSubNodesTable(){
		rootNode.next = null;//这种方法是否妥当， 还是将标识为设置为false比较妥当，会不会造成内存泄露
		return 1;
	}
	
	/**
	 * 查询host是否在subnode表中
	 * @param host 指定的host
	 * @return  返回包括host的ip地址，如果表中不存在该host， 返回null
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
	 * 将host直接添加到添加到指定ip的host表的尾部
	 * @param ip 该host所归属的ip地址
	 * @param host host值
	 * @return 1表示成功， 0表示失败
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
	 * 删除指定ip节点的host值
	 * @param host host值
	 * @param ip  指定节点的ip
	 * @return 1表示成功， 0 表示host不存在
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
	 * 当子节点ip、port发生变化， 就需要更新这个子节点的ip， port。注意新的ip不要和某个节点的ip重复
	 * 所调用这个函数之前，先要调用containNode函数以确保ip的唯一性。
	 * @param oldIp 修改之前的子节点ip地址
	 * @param newIp	修改之后的子节点ip地址
	 * @param port	修改之后的子节点port端口
	 * @return 1表示修改成功， 0表示不存在oldip的节点。
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
	 * 查找指定ip的node是否在subnodetable表中
	 * @param ip
	 * @return 0表示不存在， 1表示存在， -1表示存在但是不活跃
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
	 * 将dhosttable中所有节点的节点编号，ip， port， active值封装在object[40][4]里面返回， 供快速查询
	 * 若子节点超过40个， 出错
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
 * 定义子节点存放自己抓取失败的host，和 主节点要求其不抓取的host 的数据结构
 * @author keyalin
 *
 * @param <T>
 */
class DVisitedHost<T> extends LinkedBlockingQueue<T>{
	private static final long serialVersionUID = -3607593989901140772L;
}




