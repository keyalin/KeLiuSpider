/**
 * 存放子节点的viditedUrls采用类似于hash链表的结构。相同host的urls采用单链表组成一个支链， 而不同的host组成一个主链
 * 该数据结构有一个缺点， 当存储数量偏大后， 相关插入， 查询， 删除会越来越慢。因为每次都要从表头开始向后遍历
 * 注意让这里面的每个方法都同步
 * @author keyalin
 *
 */
package spider;

import java.io.*;

public class VisitedUrls implements Serializable{
	
	private static final long serialVersionUID = -3142637791717180190L;
	
	private Host hostRoot;
	
	/**
	 * 将表头初始化，表头hostRoot的每个字段都为null
	 */
	public VisitedUrls(){
		hostRoot = new Host();
		hostRoot.urlRoot = null;
		hostRoot.next = null;
		hostRoot.value = null;
	}
	
	/**
	 * 判断指定的host在不在表中
	 * @param 指定的host
	 * @return 0 表示不存在， 1表示存在
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
	 * 判断指定的url在不在表中
	 * @param 指定的url
	 * @return 0 表示不存在， 1表示存在
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
	
	//根据url网址创建Url对象
	private Url createUrl(String value){
		Url newUrl = new Url();
		newUrl.value = value;
		newUrl.next = null;
		return newUrl;
	}
	
	/**
	 * 在表中添加指定value的url
	 * @param value 指定url
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
			addUrl(value);//这个方法未免太笨拙了
		}else{
			Url urlQuery = hostQuery.urlRoot;
			while(null != urlQuery.next) urlQuery = urlQuery.next;
			Url url = createUrl(value);
			urlQuery.next = url;
		}
	}
	
	//创建一个新的host对象
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
	 * 将指定value的Host对象添加到表尾
	 * @param value host值
	 */
	public synchronized void addHost(String value){
		Host newHost = createHost(value);
		Host hostQuery = hostRoot;
		while(null != hostQuery.next) hostQuery = hostQuery.next;
		hostQuery.next = newHost;
	}
	
	/**
	 * 将指定url从表中删除
	 * @param value 指定的url值
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
	 * 在表中删除所有host为value的urls
	 * @param value 指定value的host值
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
	 * 单个host链中url节点的数据结构
	 */
	class Url implements Serializable{
		private static final long serialVersionUID = -7588148472854216481L;
		public String value;
		public Url next;
	}
	
	/**
	 * host主链的数据结构
	 */
	class Host implements Serializable{
		private static final long serialVersionUID = 3661324492879389236L;
		public String value;
		public Url urlRoot;
		public Host next;
	}
}



