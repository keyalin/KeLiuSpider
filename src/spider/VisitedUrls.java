/**
 * ����ӽڵ��viditedUrls����������hash����Ľṹ����ͬhost��urls���õ��������һ��֧���� ����ͬ��host���һ������
 * �����ݽṹ��һ��ȱ�㣬 ���洢����ƫ��� ��ز��룬 ��ѯ�� ɾ����Խ��Խ������Ϊÿ�ζ�Ҫ�ӱ�ͷ��ʼ������
 * ע�����������ÿ��������ͬ��
 * @author keyalin
 *
 */
package spider;

import java.io.*;

public class VisitedUrls implements Serializable{
	
	private static final long serialVersionUID = -3142637791717180190L;
	
	private Host hostRoot;
	
	/**
	 * ����ͷ��ʼ������ͷhostRoot��ÿ���ֶζ�Ϊnull
	 */
	public VisitedUrls(){
		hostRoot = new Host();
		hostRoot.urlRoot = null;
		hostRoot.next = null;
		hostRoot.value = null;
	}
	
	/**
	 * �ж�ָ����host�ڲ��ڱ���
	 * @param ָ����host
	 * @return 0 ��ʾ�����ڣ� 1��ʾ����
	 */
	public synchronized int containHost(String host){
		Host  hostQuery = hostRoot.next;
		while(null != hostQuery){
			if(host.equals(hostQuery.value)){
				return 1;
			}
			hostQuery = hostQuery.next;
		}
		return 0;
	}
	
	/**
	 * �ж�ָ����url�ڲ��ڱ���
	 * @param ָ����url
	 * @return 0 ��ʾ�����ڣ� 1��ʾ����
	 */
	public synchronized int containUrl(String url){
		String host = Utility.getHost(url);
		if(host == null) return 0;
		Host hostQuery = hostRoot.next;
		while(null != hostQuery){
			if(host.equals(hostQuery.value)){
				break;
			}
			hostQuery = hostQuery.next;
		}
		if(null == hostQuery){
			return 0;
		}
		else{
			Url urlQuery = hostQuery.urlRoot.next;
			while(null != urlQuery){
				if(url.equals(urlQuery.value) ){
					return 1;
				}
				urlQuery = urlQuery.next;
			}
			return 0;
		}
	}
	
	//����url��ַ����Url����
	private Url createUrl(String value){
		Url newUrl = new Url();
		newUrl.value = value;
		newUrl.next = null;
		return newUrl;
	}
	
	/**
	 * �ڱ������ָ��value��url
	 * @param value ָ��url
	 */
	public synchronized void addUrl(String value){
		String host = Utility.getHost(value);
		if(null == host) return;
		Host hostQuery = hostRoot.next;
		while(null != hostQuery){
			if(host.equals(hostQuery.value)){
				break;
			}
			hostQuery = hostQuery.next;
		}
		if(null == hostQuery){
			addHost(host);
			addUrl(value);//�������δ��̫��׾��
		}else{
			Url urlQuery = hostQuery.urlRoot;
			while(null != urlQuery.next) urlQuery = urlQuery.next;
			Url url = createUrl(value);
			urlQuery.next = url;
		}
	}
	
	//����һ���µ�host����
	private Host createHost(String value){
		Host newHost = new Host();
		newHost.value = value;
		newHost.urlRoot = new Url();
		newHost.urlRoot.value = null;
		newHost.urlRoot.next = null;
		newHost.next = null;
		return newHost;
	}
	
	/**
	 * ��ָ��value��Host������ӵ���β
	 * @param value hostֵ
	 */
	public synchronized void addHost(String value){
		Host newHost = createHost(value);
		Host hostQuery = hostRoot;
		while(null != hostQuery.next) hostQuery = hostQuery.next;
		hostQuery.next = newHost;
	}
	
	/**
	 * ��ָ��url�ӱ���ɾ��
	 * @param value ָ����urlֵ
	 */
	public synchronized void removeUrl(String value){
		String host = Utility.getHost(value);
		if(null == host) return;
		Host hostQuery = hostRoot.next;
		while(null != hostQuery){
			if(value.equals(hostQuery.value) ){
				break;
			}
			hostQuery = hostQuery.next;
		}
		if(null == hostQuery){
			return;
		}else{
			Url previous = hostQuery.urlRoot;
			Url next = previous.next;
			while(null != next){
				if(value == next.value) {
					previous = next.next;
					next = null;
					return;
				}
				previous = next;
				next = next.next;
			}
			return;
		}
	}
	
	/**
	 * �ڱ���ɾ������hostΪvalue��urls
	 * @param value ָ��value��hostֵ
	 */
	public synchronized void removeHost(String value){
		Host previous = hostRoot;
		Host next = hostRoot.next;
		while(null != next){
			if(value == next.value){
				previous.next = next.next;
				next = null;
				return;
			}
			previous = next;
			next = next.next;
		}
		return;
	}
	
	/**
	 * ����host����url�ڵ�����ݽṹ
	 */
	class Url implements Serializable{
		private static final long serialVersionUID = -7588148472854216481L;
		public String value;
		public Url next;
	}
	
	/**
	 * host���������ݽṹ
	 */
	class Host implements Serializable{
		private static final long serialVersionUID = 3661324492879389236L;
		public String value;
		public Url urlRoot;
		public Host next;
	}
}



