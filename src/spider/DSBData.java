/**
 * 存储分布式爬虫的控制数据
 */

package spider;

/**
 * @author keyalin
 *
 */
public class DSBData {
	
	//host的数据
	private static String hostIP;
	private static int hostPort;
	
	public static String getHostIP(){
		return hostIP;
	}
	
	public static void setHostIP(String value){
		hostIP = value;
	}
	
	public static int getHostPort(){
		return hostPort;
	}
	
	public static void setHostPort(int value){
		hostPort = value;
	}
	
	public static DHostTable dHostTable;
	public static Object[][] dNodeData;
	
	//node独有的数据
	public static VisitedQueue<String> othersVisitedHost;
	public static VisitedQueue<String> ownVisitedHost;
	private static String nodeIP;
	private static int nodePort;
	
	public static String getNodeIP(){
		return nodeIP;
	}
	
	public static void setNodeIP(String value){
		nodeIP = value;
	}
	
	public static int getNodePort(){
		return nodePort;
	}
	
	public static void setNodePort(int value){
		nodePort = value;
	}
	
}
