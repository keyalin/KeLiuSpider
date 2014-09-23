/**
 * ���ļ����������ڵ���������ݽṹ
 */
package spider;

import java.io.Serializable;

/**
 * �����ܻ���������ݽṹ
 * @author keyalin
 *
 */
public class MainUnitCom extends DSBDataCommunication{	
	private static final long serialVersionUID = -7665425908443572986L;
	
	/**
	 * ��������ͷ��nodeͨ��ReqType�ֶ��ж���������
	 */
	private ReqType reqType;
	enum ReqType{ADDNODE, RESJOIN, DELETENODE, CHECK, HOST}
	
	/**
	 * ��������ͷ��nodeͨ������ͷ�ж��������ͣ� �Ӷ���MainUnitComת��Ϊ��Ӧtype������ʵ����
	 * ע�⣺����Ϊת��
	 * @return ReqType ʵ��
	 */
	public ReqType getReqType(){
		return reqType;
	}
	
	/*
	 * �����������Ͷ���nodeͨ����Щ�����ȡָ����������ͷ�����Ӧ����������
	 */
	private AddNodeReq addNodeReq = null;
	private ResJoinReq resJoinReq = null;
	private HostReq hostReq = null;
	private DeleteNodeReq deleteNodeReq = null;
	private CheckNodeReq checkNodeReq = null;
	
	/**
	 * ����AddNodeReqʵ���� ��MainUnitComʵ��û�з�װΪAddNodeReq�� ����һ��nullֵ
	 * @return
	 */
	public AddNodeReq getAddNodeReq(){
		return addNodeReq;
	}
	
	/**
	 * ����ResJoinReqʵ���� ��MainUnitComʵ��û�з�װΪResJoinReq�� ����һ��nullֵ
	 * @return
	 */
	public ResJoinReq getResJoinReq(){
		return resJoinReq;
	}
	
	/**
	 * ����HostReqʵ���� ��MainUnitComʵ��û�з�װΪHostReq�� ����һ��nullֵ
	 * @return
	 */
	public HostReq getHostReq(){
		return hostReq;
	}
	
	/**
	 * ����DeleteNodeReqʵ���� ��MainUnitComʵ��û�з�װΪDeleteNodeReq�� ����һ��nullֵ
	 * @return
	 */
	public DeleteNodeReq getDeleteNodeReq(){
		return deleteNodeReq;
	}
	
	/**
	 * ����CheckNodeReqʵ���� ��MainUnitComʵ��û�з�װΪCheckNodeReq�� ����һ��nullֵ
	 * @return
	 */
	public CheckNodeReq getCheckNodeReq(){
		return checkNodeReq;
	}
	
	/*
	 * ��MainUnitCom�����װΪһ���ض����������
	 * ע������α��װ
	 * ��װ����ÿ��MainUnitCom����ֻ�ܵ�������һ�η�װ������
	 * ���� setCount������֤ÿ����װ����ֻ����һ�Ρ�
	 * ��ǰ����setCountΪ1ʱ���ܵ��÷�װ����
	 * �����ص��ã� ���׳��쳣NotCallException
	 */
	private int setCount = 1;
	
	/**
	 * ��mainunitcomʵ����װΪaddnode����
	 * @param ip �ܻ���ip��ַ
	 * @param port �ܻ���port��ַ
	 * @param seed �ܻ�������ӽڵ��ץȡ��㣬 null��ʾ�ӽڵ�ץȡ���Ϊhistory
	 * @throws NotCallException ��mainunitcomʵ���Ѿ�����װ�� ���׳����쳣
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
	 * ��mainunitcom��װΪΪResJoinReq����
	 * @param ip ������ip��ַ
	 * @param port	�����Ķ˿ڵ�ַ
	 * @param connect true��ʾ�ܻ��Ƿ�ͬ�����ӽڵ�����
	 * @param ipConflict true��ʾ�ӽڵ��newIp��ַ�������ڵ��г�ͻ
	 * @param seedConfict true��ʾ�ӽڵ��seed�Ѿ��������ڵ�ץȡ��
	 * @throws NotCallException ��mainunitcomʵ���Ѿ�����װ�� ���׳����쳣
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
	 * ��mainunitcomʵ�� ��װΪhostreqʵ��
	 * @param host ��captureORdeleteΪfalseʱ�� ���ֶα�ʾ��host�Ѿ�����Ľڵ�ץȡ��������ʱ��Ϊnull 
	 * @param captureORdelete 
	 * @param url ��captureORdeleteΪtrueʱ�� �ӽڵ�Ὣ��url��ӵ�todeHref����
	 * @throws NotCallException ��mainunitcomʵ���Ѿ�����װ�� ���׳����쳣
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
	 * ��mainunitcomʵ�� ��װΪdeletereqʵ��
	 * @param ip ������ip��ַ
	 * @param clear true��ʾnodeɾ���������ݣ� false��ʾֹͣ���м���
	 * @throws NotCallException ��mainunitcomʵ���Ѿ�����װ�� ���׳����쳣
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
	 * ��mainunitcomʵ�� ��װΪdeletereqʵ��
	 * @throws NotCallException ��mainunitcomʵ���Ѿ�����װ�� ���׳����쳣
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
	 * ��������ӽڵ�����������ݽṹ�� ���������ֶΣ�
	 * ip ����ip�� ע����������ip
	 * port �������Ӷ˿� ע���������ļ��Ӷ˿�
	 * seed seed��Ϊnull�� ���ʾ�ӽڵ���ȡ���history�������ӽڵ㽫��seed��ʼץȡ��ҳ
	 * @author keyalin 
	 */
	class AddNodeReq implements Serializable{
		private static final long serialVersionUID = 6831233722365833418L;
		String ip;
		int port;
		String seed;//seedΪnull��ʾ�ӽڵ���ȡ���history
	}
	
	/**
	 * ���ӽڵ㷢��host��������ݽṹ�� ���������ֶ�
	 * host ��Ҫ���в�����host
	 * captureORdelete true��ʾ֪ͨ�ӽڵ�ȥץȡurl�� false��ʾ�ӽڵ�ɾ���������host��ص���Ϣ
	 * url ��Ҫ���в�����url
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
	 * ��Ӧ�ӽڵ��join����, ���������ֶΣ�
	 * ip ���ڵ��ip��ַ
	 * port ���ڵ�Ķ˿ڵ�ַ
	 * connect true��ʾ���ӳɹ��� false��ʾ����ʧ��
	 * ipConfilct connectΪfalseʱ��Ч�� true��ʾ����ip��ͻ
	 * seedConflict  connectΪfalseʱ��Ч�� true��ʾ����seed��ͻ
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
	 * ����ɾ���ڵ���������ݽṹ�����������ֶΣ�
	 * ip ����ip
	 * clear true��ʾnodeɾ���������ݣ� false��ʾֹͣ���м���
	 * @author keyalin
	 *
	 */
	class DeleteNodeReq implements Serializable{
		private static final long serialVersionUID = 2983615495860096696L;
		String ip;
		boolean clear;
	}
	
	/**
	 * ������ڵ��Ƿ���ڵ����� ��ֹ�ӽڵ��������������Ŀ��ƣ�Ŀǰ���ֶ�
	 * @author keyalin
	 *
	 */
	class CheckNodeReq implements Serializable{
		private static final long serialVersionUID = -7033418191154954152L;		
	}
}









