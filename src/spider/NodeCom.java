
/**
 * 定义了子节点通信数据结构的模板类
 */
package spider;

import java.io.Serializable;

/**
 * 
 * 定义子节点的通信数据结构类
 * @author keyalin
 *
 */
public class NodeCom extends DSBDataCommunication{
	
	private static final long serialVersionUID = 7431847818742296944L;
	/**
	 * 定义请求头变量，主机通过这些字段判断请求类型
	 */
	private ReqType reqType = null;
	enum ReqType{JOIN, QUIT, HOST, RESADDNODE, CHECK};
	
	/**
	 * 返回NodeCom实例被封装的类型， 若没有被封转返回null
	 * @return
	 */
	public ReqType getReqType(){
		return reqType;
	}
	
	//定义请求头变量, 主机通过这些变量读取请求数据内容
	private JoinReq joinReq = null;
	private QuitReq quitReq = null;
	private HostReq hostReq = null;
	private ResAddNodeReq resAddNodeReq = null;
	private CheckReq checkReq = null;
	
	/**
	 *  返回JoinReq实例， 若NodeCom实例没有封装为AddNodeReq， 返回一个null值
	 * @return
	 */
	public JoinReq getJoinReq(){
		return joinReq;
	}
	
	/**
	 *  返回QuitReq实例， 若NodeCom实例没有封装为QuitReq， 返回一个null值
	 * @return
	 */
	public QuitReq getQuitReq(){
		return quitReq;
	}
	
	/**
	 *  返回HostReq实例， 若NodeCom实例没有封装为HostReq， 返回一个null值
	 * @return
	 */
	public HostReq getHostReq(){
		return hostReq;
	}
	
	/**
	 *  返回ResAddNodeReq实例， 若NodeCom实例没有封装为ResAddNodeReq， 返回一个null值
	 * @return
	 */
	public ResAddNodeReq getResAddNodeReq(){
		return resAddNodeReq;
	}
	
	/*
	 * 将NodeCom对象封装为一个特定的请求对象。
	 * 注意这是伪封装
	 * 封装函数每个NodeCom对象只能调用一次。
	 * 变量 setCount用来保证每个封装函数只调用一次。
	 * 若多重调用， 这抛出异常NotCallException
	 */
	private int setCount = 1;
	
	/**
	 * 将NodeCom实例封装为一个join请求
	 * @param oldIp 若子节点是新的节点， 该字段为null。 否者为最后一次参与任务时的ip
	 * @param newIp 子节点现在的ip
	 * @param port 	子节点现在的监视端口
	 * @param seed	子节点的爬行起始点， null表示爬行起始点为历史（子节点必须以前参与过任务）。
	 * @throws NotCallException NodeCom实例已经被封装后抛出此异常
	 */
	public void setJoinReq(String oldIp, String newIp, int port, String seed) 
															throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else {
			reqType = ReqType.JOIN;
			joinReq = new JoinReq();
			joinReq.newIp = newIp;
			joinReq.oldIp = oldIp;
			joinReq.seed = seed;
			joinReq.port = port;
			setCount --;
		}
	}
	
	/**
	 * 将NodeCom实例封装为一个QuitReq请求
	 * @param ip  子节点的ip
	 * @param clear true通知总机是否清空该节点的所有数据，一般设置为false。
	 * @throws NotCallException NodeCom实例已经被封装后抛出此异常
	 */
	public void setQuitReq(String ip, boolean clear) throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else {
			reqType = ReqType.QUIT;
			quitReq = new QuitReq();
			quitReq.ip = ip;
			quitReq.clear = clear;
			setCount --;
		}
	}
	
	/**
	 * 将NodeCom实例封装为一个HostReq请求
	 * @param ip 子节点的ip地址
	 * @param host 子节点发送给主机的host
	 * @param addORwait true表示该host添加到主机中， false表示请求将该host发送给其他节点爬行
	 * @param port 子节点的端口
	 * @param url 若addORwait为false， 此字段生效， 否则为null
	 * @throws NotCallException NodeCom实例已经被封装了抛出此异常
	 */
	public void setHostReq(String ip, String host, boolean addORwait, int port, String url) 
														throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else {
			reqType = ReqType.HOST;
			hostReq = new HostReq();
			hostReq.ip = ip;
			hostReq.host = host;
			hostReq.addORwait = addORwait;
			hostReq.port = port;
			hostReq.url = url;
			setCount --;
		}
	}
	
	/**
	 * 将NodeCom实例封装为ResAddNodeReq请求
	 * @param ip 子节点的ip地址
	 * @param port 子节点的监视端口
	 * @param agree true表示子节点同意加入总机
	 * @param history false表示是个新增节点
	 * @throws NotCallException
	 */
	public void setResAddNodeReq(String ip, int port, boolean agree, boolean history) 
															throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}
		else {
			reqType = ReqType.RESADDNODE;
			resAddNodeReq = new ResAddNodeReq();
			resAddNodeReq.ip = ip;
			resAddNodeReq.port = port;
			resAddNodeReq.agree = agree;
			resAddNodeReq.history = history;
			setCount --;
		}
	}
	
	/**
	 * 将NodeCom实例封装为checkreq
	 * @throws NotCallException 若该实例已经被封装， 抛出此异常
	 *  
	 */
	public void setCheckReq() throws NotCallException{
		if(1 != setCount){
			throw new NotCallException();
		}else{
			reqType = ReqType.CHECK;
		}
	}
	
	/**
	 * 子节点请求加入的join类数据结构， 它有以下字段：
	 * oldIp 若子节点以前加入过总机，但是现在ip地址发生变化，应当通知主机以前的ip地址. 若是新增节点，此字段为null
	 * newIp 子节点现在的ip地址
	 * port 子节点现在的监视端口
	 * seed 若此字段为null，若子节点以前加入过总机,同时表示子节点抓取起点为history，若子节点是新增节点，那么此字段不能为空
	 * 因为主机要判断子节点的爬取起点是否与其他节点冲突，防止子节点饿死。
	 * @author keyalin
	 *
	 */
	class JoinReq implements Serializable{
		private static final long serialVersionUID = 8895172356928357683L;
		String oldIp;// 
		String newIp;
		int port;
		String seed;//seed为null表示爬取起点为history
	}
	
	/**
	 * 退出请求数据结构。它有以下字段
	 * ip 子节点的ip地址
	 * clear 若为true， 总机将删除所有已该节点相关的数据。 一般建议将该字段设置为false
	 * @author keyalin
	 *
	 */
	class QuitReq implements Serializable{
		private static final long serialVersionUID = -1000715165452404865L;
		String ip;
		boolean clear;
	}
	
	 /**
	  * host请求数据结构， 它有以下字段
	  * addORwait true表示是总机添加host，false表示还是将url交付给别的节点抓取
	  * ip 子节点的ip地址
	  * url 将要进行操作的url值
	  * host 将要进行操作的host值
	  * port 子节点的监视端口
	  * @author keyalin
	  *
	  */
	class HostReq implements Serializable{
		private static final long serialVersionUID = -1117257574097624124L;
		boolean addORwait;//表示是添加host还是将此host交付给别的节点爬行
		String ip;
		String url;
		String host;
		int port;
	}
	
	/**
	 * 回应总机的AddNodeReq请求的数据结构。它有以下字段
	 * ip：子节点的ip
	 * port 子节点的监视端口
	 * history true表示此节点是否新增节点。
	 * agree true表示子节点同意加入总机。此为保留字段， 因为在该爬虫中，若过子节点不同意加入总机的addnode请求， 
	 * 就会丢弃addnode请求而不回复。
	 * @author keyalin
	 *
	 */
	class ResAddNodeReq implements Serializable{
		private static final long serialVersionUID = 2451103624033913647L;
		String ip;
		int port;
		boolean history;
		boolean agree;
	}
	
	/*
	 * 保留类， 以后可能用到
	 */
	class ResDeleteReq implements Serializable{
		private static final long serialVersionUID = 7008108855893592245L;
	}
	
	/*
	 * 保留类， 以后可能用到
	 * 回应总机的check请求
	 */
	class ResCheck implements Serializable{
		private static final long serialVersionUID = -2554816934445584826L;		
	}
	
	/**
	 * 定期检查主机是否存在
	 * @author keyalin
	 *
	 */
	class CheckReq implements Serializable{
		private static final long serialVersionUID = -4821703454768096140L;	
	}
}
