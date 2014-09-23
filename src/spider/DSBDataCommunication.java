/**
 * 该文件定义了分布式爬虫子节点和主节点的通信请求数据结构的超类
 */
package spider;

import java.io.Serializable;

/**
 * 此类有两个继承类， MainUnitCom, NodeCom.
 * 他们二者分别定义了总机和子节点通信时发出的请求的数据结构
 * @author keyalin
 *
 */
public abstract class DSBDataCommunication implements Serializable{
	private static final long serialVersionUID = 6625562298061107990L;
}
