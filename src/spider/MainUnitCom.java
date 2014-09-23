/**
 * 该文件定义了主节点请求的数据结构
 */
package spider;

import java.io.Serializable;

/**
 * 定义总机请求的数据结构
 * @author keyalin
 *
 */
public class MainUnitCom extends DSBDataCommunication{	
	private static final long serialVersionUID = -7665425908443572986L;
	
	/**
	 * 定义请求头，node通过ReqType字段判断请求类型
	 */
	private ReqType reqType;
	enum ReqType{ADDNODE, RESJOIN, DELETENODE, CHECK, HOST}
	
	/**
	 * 返回请求头，node通过请求头判断请求类型， 从而将MainUnitCom转换为对应type的请求实例。
	 * 注意：这是为转换
	 * @return ReqType 实例
	 */
	public ReqType getReqType(){
		return reqType;
	}
	
	/*
	 * 创建请求类型对象，node通过这些对象读取指定类型请求头的相对应的数据内容
	 */
	private AddNodeReq addNodeReq = null;
	private ResJoinReq resJoinReq = null;
	private HostReq hostReq = null;
	private DeleteNodeReq deleteNodeReq = null;
	private CheckNodeReq checkNodeReq = null;
	
	/**
	 * 返回AddNodeReq实例， 若MainUnitCom实例没有封装为AddNodeReq， 返回一个null值
	 * @return
	 */
	public AddNodeReq getAddNodeReq(){
		return addNodeReq;
	}
	
	/**
	 * 返回ResJoinReq实例， 若MainUnitCom实例没有封装为ResJoinReq， 返回一个null值
	 * @return
	 */
	public ResJoinReq getResJoinReq(){
		return resJoinReq;
	}
	
	/**
	 * 返回HostReq实例， 若MainUnitCom实例没有封装为HostReq， 返回一个null值
	 * @return
	 */
	public HostReq getHostReq(){
		return hostReq;
	}
	
	/**
	 * 返回DeleteNodeReq实例， 若MainUnitCom实例没有封装为DeleteNodeReq， 返回一个null值
	 * @return
	 */
	public DeleteNodeReq getDeleteNodeReq(){
		return deleteNodeReq;
	}
	
	/**
	 * 返回CheckNodeReq实例， 若MainUnitCom实例没有封装为CheckNodeReq， 返回一个null值
	 * @return
	 */
	public CheckNodeReq getCheckNodeReq(){
		return checkNodeReq;
	}
	
	/*
	 * 将MainUnitCom对象封装为一个特定的请求对象。
	 * 注意这是伪封装
	 * 封装函数每个MainUnitCom对象只能调用任意一次封装函数。
	 * 变量 setCount用来保证每个封装函数只调用一次。
	 * 当前仅当setCount为1时才能调用封装函数
	 * 若多重调用， 这抛出异常NotCallException
	 */
	private int setCount = 1;
	
	/**
	 * 将mainunitcom实例封装为addnode请求
	 * @param ip 总机的ip地址
	 * @param port 总机的port地址
	 * @param seed 总机分配给子节点的抓取起点， null表示子节点抓取起点为history
	 * @throws NotCallException 当mainunitcom实例已经被封装， 则抛出此异常
	 */
	public void setAddNodeReq(String ip, int port, String seed) throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		} else{
			reqType = ReqType.ADDNODE;
			addNodeReq = new AddNodeReq();
			addNodeReq.ip = ip;
			addNodeReq.port = port;
			addNodeReq.seed = seed;
			setCount --;
		}		
	}
	
	/**
	 * 将mainunitcom封装为为ResJoinReq请求
	 * @param ip 主机的ip地址
	 * @param port	主机的端口地址
	 * @param connect true表示总机是否同意与子节点连接
	 * @param ipConflict true表示子节点的newIp地址与其他节点有冲突
	 * @param seedConfict true表示子节点的seed已经被其他节点抓取过
	 * @throws NotCallException 当mainunitcom实例已经被封装， 则抛出此异常
	 */
	public void setResJoinReq(String ip, int port, boolean connect, boolean ipConflict, 
									boolean seedConfict) throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else{
			reqType = ReqType.RESJOIN;
			resJoinReq = new ResJoinReq();
			resJoinReq.connect = connect;
			resJoinReq.ipConfilct = ipConflict;
			resJoinReq.seedConflict = seedConfict;
			setCount --;
		}		
	}
	
	/**
	 * 将mainunitcom实例 封装为hostreq实例
	 * @param host 当captureORdelete为false时， 此字段表示该host已经被别的节点抓取过，其他时候为null 
	 * @param captureORdelete 
	 * @param url 当captureORdelete为true时， 子节点会将该url添加到todeHref表里
	 * @throws NotCallException 当mainunitcom实例已经被封装， 则抛出此异常
	 */
	public void setHostReq(String host, boolean captureORdelete, String url) throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else{
			reqType = ReqType.HOST;
			hostReq = new HostReq();
			hostReq.host = host;
			hostReq.url = url;
			hostReq.captureORdelete = captureORdelete;
			setCount --;
		}
	}
	
	/**
	 * 将mainunitcom实例 封装为deletereq实例
	 * @param ip 主机的ip地址
	 * @param clear true表示node删除下载数据， false表示停止运行即可
	 * @throws NotCallException 当mainunitcom实例已经被封装， 则抛出此异常
	 */
	public void setDeleteReq(String ip, boolean clear) throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else{
			reqType = ReqType.DELETENODE;
			deleteNodeReq = new DeleteNodeReq();
			deleteNodeReq.ip = ip;
			deleteNodeReq.clear = clear;
		}
	}
	
	/**
	 * 将mainunitcom实例 封装为deletereq实例
	 * @throws NotCallException 当mainunitcom实例已经被封装， 则抛出此异常
	 */
	public void setCheckReq() throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else{
			reqType = ReqType.CHECK;
			checkNodeReq = new CheckNodeReq();
		}
	}
	/**
	 * 定义添加子节点的请求类数据结构， 它有以下字段：
	 * ip 主机ip， 注意是主机的ip
	 * port 主机监视端口 注意是主机的监视端口
	 * seed seed若为null， 则表示子节点爬取起点history，否则子节点将从seed开始抓取网页
	 * @author keyalin 
	 */
	class AddNodeReq implements Serializable{
		private static final long serialVersionUID = 6831233722365833418L;
		String ip;
		int port;
		String seed;//seed为null表示子节点爬取起点history
	}
	
	/**
	 * 向子节点发送host请求的数据结构。 它有以下字段
	 * host 将要进行操作的host
	 * captureORdelete true表示通知子节点去抓取url， false表示子节点删除所有与该host相关的信息
	 * url 将要进行操作的url
	 * @author keyalin
	 *
	 */
	class HostReq implements Serializable{
		private static final long serialVersionUID = 587056620074013707L;
		String host;
		String url;
		boolean captureORdelete;
	}
	
	/**
	 * 回应子节点的join请求, 它有以下字段：
	 * ip 主节点的ip地址
	 * port 主节点的端口地址
	 * connect true表示连接成功， false表示连接失败
	 * ipConfilct connect为false时生效， true表示发生ip冲突
	 * seedConflict  connect为false时生效， true表示发生seed冲突
	 * @author keyalin
	 *
	 */
	class ResJoinReq implements Serializable{
		private static final long serialVersionUID = 4638903530300591056L;
		String ip;
		int port;
		boolean connect;
		boolean ipConfilct;
		boolean seedConflict;
	}
	
	/**
	 * 定义删除节点请求的数据结构，它有以下字段：
	 * ip 主机ip
	 * clear true表示node删除下载数据， false表示停止运行即可
	 * @author keyalin
	 *
	 */
	class DeleteNodeReq implements Serializable{
		private static final long serialVersionUID = 2983615495860096696L;
		String ip;
		boolean clear;
	}
	
	/**
	 * 定义检查节点是否存在的请求， 防止子节点意外脱离主机的控制，目前无字段
	 * @author keyalin
	 *
	 */
	class CheckNodeReq implements Serializable{
		private static final long serialVersionUID = -7033418191154954152L;		
	}
}









