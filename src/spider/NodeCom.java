
/**
 * �������ӽڵ�ͨ�����ݽṹ��ģ����
 */
package spider;

import java.io.Serializable;

/**
 * 
 * �����ӽڵ��ͨ�����ݽṹ��
 * @author keyalin
 *
 */
public class NodeCom extends DSBDataCommunication{
	
	private static final long serialVersionUID = 7431847818742296944L;
	/**
	 * ��������ͷ����������ͨ����Щ�ֶ��ж���������
	 */
	private ReqType reqType = null;
	enum ReqType{JOIN, QUIT, HOST, RESADDNODE, CHECK};
	
	/**
	 * ����NodeComʵ������װ�����ͣ� ��û�б���ת����null
	 * @return
	 */
	public ReqType getReqType(){
		return reqType;
	}
	
	//��������ͷ����, ����ͨ����Щ������ȡ������������
	private JoinReq joinReq = null;
	private QuitReq quitReq = null;
	private HostReq hostReq = null;
	private ResAddNodeReq resAddNodeReq = null;
	private CheckReq checkReq = null;
	
	/**
	 *  ����JoinReqʵ���� ��NodeComʵ��û�з�װΪAddNodeReq�� ����һ��nullֵ
	 * @return
	 */
	public JoinReq getJoinReq(){
		return joinReq;
	}
	
	/**
	 *  ����QuitReqʵ���� ��NodeComʵ��û�з�װΪQuitReq�� ����һ��nullֵ
	 * @return
	 */
	public QuitReq getQuitReq(){
		return quitReq;
	}
	
	/**
	 *  ����HostReqʵ���� ��NodeComʵ��û�з�װΪHostReq�� ����һ��nullֵ
	 * @return
	 */
	public HostReq getHostReq(){
		return hostReq;
	}
	
	/**
	 *  ����ResAddNodeReqʵ���� ��NodeComʵ��û�з�װΪResAddNodeReq�� ����һ��nullֵ
	 * @return
	 */
	public ResAddNodeReq getResAddNodeReq(){
		return resAddNodeReq;
	}
	
	/*
	 * ��NodeCom�����װΪһ���ض����������
	 * ע������α��װ
	 * ��װ����ÿ��NodeCom����ֻ�ܵ���һ�Ρ�
	 * ���� setCount������֤ÿ����װ����ֻ����һ�Ρ�
	 * �����ص��ã� ���׳��쳣NotCallException
	 */
	private int setCount = 1;
	
	/**
	 * ��NodeComʵ����װΪһ��join����
	 * @param oldIp ���ӽڵ����µĽڵ㣬 ���ֶ�Ϊnull�� ����Ϊ���һ�β�������ʱ��ip
	 * @param newIp �ӽڵ����ڵ�ip
	 * @param port 	�ӽڵ����ڵļ��Ӷ˿�
	 * @param seed	�ӽڵ��������ʼ�㣬 null��ʾ������ʼ��Ϊ��ʷ���ӽڵ������ǰ��������񣩡�
	 * @throws NotCallException NodeComʵ���Ѿ�����װ���׳����쳣
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
	 * ��NodeComʵ����װΪһ��QuitReq����
	 * @param ip  �ӽڵ��ip
	 * @param clear true֪ͨ�ܻ��Ƿ���ոýڵ���������ݣ�һ������Ϊfalse��
	 * @throws NotCallException NodeComʵ���Ѿ�����װ���׳����쳣
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
	 * ��NodeComʵ����װΪһ��HostReq����
	 * @param ip �ӽڵ��ip��ַ
	 * @param host �ӽڵ㷢�͸�������host
	 * @param addORwait true��ʾ��host��ӵ������У� false��ʾ���󽫸�host���͸������ڵ�����
	 * @param port �ӽڵ�Ķ˿�
	 * @param url ��addORwaitΪfalse�� ���ֶ���Ч�� ����Ϊnull
	 * @throws NotCallException NodeComʵ���Ѿ�����װ���׳����쳣
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
	 * ��NodeComʵ����װΪResAddNodeReq����
	 * @param ip �ӽڵ��ip��ַ
	 * @param port �ӽڵ�ļ��Ӷ˿�
	 * @param agree true��ʾ�ӽڵ�ͬ������ܻ�
	 * @param history false��ʾ�Ǹ������ڵ�
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
	 * ��NodeComʵ����װΪcheckreq
	 * @throws NotCallException ����ʵ���Ѿ�����װ�� �׳����쳣
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
	 * �ӽڵ���������join�����ݽṹ�� ���������ֶΣ�
	 * oldIp ���ӽڵ���ǰ������ܻ�����������ip��ַ�����仯��Ӧ��֪ͨ������ǰ��ip��ַ. ���������ڵ㣬���ֶ�Ϊnull
	 * newIp �ӽڵ����ڵ�ip��ַ
	 * port �ӽڵ����ڵļ��Ӷ˿�
	 * seed �����ֶ�Ϊnull�����ӽڵ���ǰ������ܻ�,ͬʱ��ʾ�ӽڵ�ץȡ���Ϊhistory�����ӽڵ��������ڵ㣬��ô���ֶβ���Ϊ��
	 * ��Ϊ����Ҫ�ж��ӽڵ����ȡ����Ƿ��������ڵ��ͻ����ֹ�ӽڵ������
	 * @author keyalin
	 *
	 */
	class JoinReq implements Serializable{
		private static final long serialVersionUID = 8895172356928357683L;
		String oldIp;// 
		String newIp;
		int port;
		String seed;//seedΪnull��ʾ��ȡ���Ϊhistory
	}
	
	/**
	 * �˳��������ݽṹ�����������ֶ�
	 * ip �ӽڵ��ip��ַ
	 * clear ��Ϊtrue�� �ܻ���ɾ�������Ѹýڵ���ص����ݡ� һ�㽨�齫���ֶ�����Ϊfalse
	 * @author keyalin
	 *
	 */
	class QuitReq implements Serializable{
		private static final long serialVersionUID = -1000715165452404865L;
		String ip;
		boolean clear;
	}
	
	 /**
	  * host�������ݽṹ�� ���������ֶ�
	  * addORwait true��ʾ���ܻ����host��false��ʾ���ǽ�url��������Ľڵ�ץȡ
	  * ip �ӽڵ��ip��ַ
	  * url ��Ҫ���в�����urlֵ
	  * host ��Ҫ���в�����hostֵ
	  * port �ӽڵ�ļ��Ӷ˿�
	  * @author keyalin
	  *
	  */
	class HostReq implements Serializable{
		private static final long serialVersionUID = -1117257574097624124L;
		boolean addORwait;//��ʾ�����host���ǽ���host��������Ľڵ�����
		String ip;
		String url;
		String host;
		int port;
	}
	
	/**
	 * ��Ӧ�ܻ���AddNodeReq��������ݽṹ�����������ֶ�
	 * ip���ӽڵ��ip
	 * port �ӽڵ�ļ��Ӷ˿�
	 * history true��ʾ�˽ڵ��Ƿ������ڵ㡣
	 * agree true��ʾ�ӽڵ�ͬ������ܻ�����Ϊ�����ֶΣ� ��Ϊ�ڸ������У������ӽڵ㲻ͬ������ܻ���addnode���� 
	 * �ͻᶪ��addnode��������ظ���
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
	 * �����࣬ �Ժ�����õ�
	 */
	class ResDeleteReq implements Serializable{
		private static final long serialVersionUID = 7008108855893592245L;
	}
	
	/*
	 * �����࣬ �Ժ�����õ�
	 * ��Ӧ�ܻ���check����
	 */
	class ResCheck implements Serializable{
		private static final long serialVersionUID = -2554816934445584826L;		
	}
	
	/**
	 * ���ڼ�������Ƿ����
	 * @author keyalin
	 *
	 */
	class CheckReq implements Serializable{
		private static final long serialVersionUID = -4821703454768096140L;	
	}
}
