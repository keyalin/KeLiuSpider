/**
 * 该文件定义了一个实用工具类，提供了一些小功能的实现接口。
 */
package spider;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * 提供一些小功能实现函数
 * @author keyalin
 *
 */
public class Utility {
	
	/**
	 * 判断网址是否相对路径，若是，就将其转换为绝对路径，并且返回网址的绝对路径，若失败返回null
	 * 同时去掉带有？的动态地址
	 * @param baseUrl 
	 * @param url 
	 * @return 若null表示网址不合法, 否则返回完整的url值
	 */
	public static String normalize(String baseUrl, String url){
		try{
			if(url.startsWith("#")) return null;
			if(-1 != url.indexOf("?")) return null;
			//判断是否绝对路径
			if(url.startsWith("http://")) return url;
			else{
				int index = baseUrl.lastIndexOf("/");
				if(index < 7){
					baseUrl = baseUrl + "/";
				}
				else{
					baseUrl = baseUrl.substring(0, index+1);
				}
				if(url.startsWith("/")) url = baseUrl + url.substring(1);
				else {
					url = baseUrl + url;
				}
				return url;
			}
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 获得图片的格式
	 * @param url 图片的url
	 * @return 图片格式
	 */
	public static String getImagFormat(String url){
		int index = url.lastIndexOf(".");
		if(index == -1) return null;
		else if((url.length() - index) > 10) return null;
		else {
			String format = url.substring(index + 1);
			return format;
		}
	}
	
	/**
	 * 获得url所在的主机名
	 * @param url 图片的url
	 * @return host或者null
	 */
	public static String getHost(String url){
		if(url.length() < 8)return null;
		int index = url.indexOf("/", 7);
		if(index == -1) return url.substring(7);
		else {
			String host = url.substring(7, index);
			return host;
		}
	}
	
	/**
	 * 定义一个函数，将数字字符串转换为十进制数
	 * @param numberString
	 * @return 对应的int数
	 */
	 public static int convertNumberStringToInt(String numberString){
		  char[] array = numberString.toCharArray();
		  int length = array.length;
		  int temp = 0;
		  for(int i = 0; i < length; i++){
			  int j = array[i] - '0';
			  if(j > 9 || j < 0) return -1;
			  else{
				  temp =  j + temp * 10;
			  }
		  }
		  return temp;
	 }
	 
	 /**
	  * 将buff里面的数据转换为字节数组
	  * @param buff BufferedInputStream
	  * @return buff 流里面所有的字节组成的数组
	  * @throws IOException
	  */
	 public static byte[] readBufferToBytes(BufferedInputStream buff) throws IOException{
		  byte[] temp = new byte[1024];
		  byte[] bytes = new byte[0];
		  while((buff.read(temp, 0, 1024)) != -1){
			  int length1 = bytes.length;
			  int length2 = length1 + 1024;
			  byte[] bbb = new byte[length2];
			  for(int i = 0; i < length1; i++){
				  bbb[i] = bytes[i];
			  }
			  for(int j = length1; j < length2; j++){
				  bbb[j] = temp[j - length1];
			  }
			  bytes = bbb;
		  }
		  return bytes;
	 }
	 
	 /**
	  * 获得两个时间戳的时间间隔, 并且用字符串的形式返回.最大单位是date， 所以爬取时间不要超过一个月
	  * @param timestamp1 时间戳1
	  * @param timestamp2 之时间戳2
	  * @return 当前时间与时间戳的时间间隔字符串形式。 格式为 .. dates ..hours ..minutes..seconds
	  */
	 public static String getTimeInterval(Calendar timestamp1, Calendar timestamp2){
		 int dates = timestamp2.get(Calendar.DATE) - timestamp1.get(Calendar.DATE);
		 int hours = timestamp2.get(Calendar.HOUR) - timestamp1.get(Calendar.HOUR);
		 int minitus = timestamp2.get(Calendar.MINUTE) -timestamp1.get(Calendar.MINUTE);
		 int seconds = timestamp2.get(Calendar.SECOND) - timestamp1.get(Calendar.SECOND);
		 StringBuilder sb = new StringBuilder();
		 sb.append(dates);
		 sb.append("dates");
		 sb.append(hours);
		 sb.append("hours");
		 sb.append(minitus);
		 sb.append("minitus");
		 sb.append(seconds);
		 sb.append("seconds");
		 return sb.toString();
	 }
	 
	 /**
	  * 将大于0的double数字格式化输出 比如： 123456.789 ~ 123 456.7
	  * @param value
	  * @return value的格式化输出方式
	  */
	 public static String format(double value){
		 if(value < 0.0) return "0.0";
		 char[] temp = Double.toString(value).toCharArray();
		 int length = temp.length;
		 int index = -1;
		 for(int i = 0; i < length; i++){
			 if(temp[i] == '.') {
				 index = i;
				 break;
			 }
		 }
		 
		 int append;
		 if(index % 3 == 0) {
			 append = index / 3 - 1;
		 }
		 else {
			 append = index/3;
		 }		 
		 int size = index + append + 2;
		 char[] result = new char[size];
		 
		 int count = 3;
		 result[size - 1] = temp[index + 1];
		 result[size - 2] = temp[index];
		 for(int i = size -3; i != -1; i --){
			 if(count != 0){
				 result[i] = temp[--index];
				 count --;
			 }else {
				 count = 3;
				 result[i] = ' ';
			 }
		 }

		 StringBuilder sb = new StringBuilder();
		 for(int i = 0; i < size; i++){
			 sb.append(result[i]);
		 }
		 
		 return sb.toString();
	 }
	 

	 /**
	  * 将字符数组转换为字符串
	  * @param a 字符数组
	  * @return a对应的字符串
	  */
	 public static String ConvertCharArrayToString(char[] a){
		 StringBuilder sb = new StringBuilder();
		 for(char ch : a){
			 sb.append(ch);
		 }
		 return sb.toString();
	 }
	 
	 /**
	  * 向指定ip地址的节点发送请求
	  * @param req 请求对象
	  * @param ip 子节点的ip地址
	  * @param port 子节点的port
	  * @return 1表示成功， 0表示失败
	  */
	public static int sendRequest(DSBDataCommunication req, String ip, int port){
			Socket socket = null;
			ObjectOutputStream send = null;
			//建立与子节点的连接
			try {
				socket = new Socket(ip, port);
				//创建add请求对象			
				send = new ObjectOutputStream (new BufferedOutputStream(
												(socket.getOutputStream())));
				send.writeObject(req);
				send.flush();
				return 1;
			} catch (Exception e){
				return 0;
			}
			finally {
				try {
					socket.close();
					send.close();
				} catch (Exception e1) {}
			}
		}

	/**
	 * 将信息以弹出窗口的形式展示在界面
	 * @param message 要展示的信息
	 */
	public static void showMessage(String message){
		JOptionPane.showMessageDialog(null, message);
		return;
	}
	
	/**
	 * 以YY-MM-DD HH:MM:SS的形式返回一个calendar实例日期的string
	 * @param timestamp calendar实例
	 * @return YY-MM-DD HH:MM:SS格式的字符串
	 */
	public static String getFormatTime(Calendar timestamp){
		StringBuilder sb = new StringBuilder();
		sb.append(timestamp.get(Calendar.YEAR));
		int i = 0;
		sb.append("-");
		if(9 > (i = timestamp.get(Calendar.MONTH))){
			sb.append(0);
		}
		sb.append(i + 1);
		sb.append("-");
		if(10 > (i = timestamp.get(Calendar.DATE))){
			sb.append(0);
		}
		sb.append(i);
		sb.append(" ");
		if(10 > (i = timestamp.get(Calendar.HOUR_OF_DAY))){
			sb.append(0);
		}
		sb.append(i);
		sb.append(":");
		if(10 > (i = timestamp.get(Calendar.MINUTE))){
			sb.append(0);
		}
		sb.append(i);
		sb.append(":");
		if(10 > (i = timestamp.get(Calendar.SECOND))){
			sb.append(0);
		}
		sb.append(i);
		return sb.toString();
	}
	
	/**
	 * 统计指定目录下所有文件的文字行数总和
	 * @param dir 目录的路径
	 * @return 所有文件的文字行数总和
	 * @throws IOException 
	 */
	public static int linesTotal(String dir) throws IOException{
		File file = new File(dir);
		int i = 0;
		for(File f : file.listFiles()){
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			while(br.readLine() != null){
				i ++;
			}
			br.close();
		}
		return i;
	}
	
	 public static void main(String[] args) throws Exception{
	        System.out.println(linesTotal("src/spider"));		 
	 }
}

/**
 *
 * 当一个函数不能被调用时抛出此类异常
 * @author keyalin
 *
 */
class NotCallException extends Exception{
	NotCallException(){
		super("cann't call me!");
	}
}
