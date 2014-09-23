/**
 * ���ļ�������һ��ʵ�ù����࣬�ṩ��һЩС���ܵ�ʵ�ֽӿڡ�
 */
package spider;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * �ṩһЩС����ʵ�ֺ���
 * @author keyalin
 *
 */
public class Utility {
	
	/**
	 * �ж���ַ�Ƿ����·�������ǣ��ͽ���ת��Ϊ����·�������ҷ�����ַ�ľ���·������ʧ�ܷ���null
	 * ͬʱȥ�����У��Ķ�̬��ַ
	 * @param baseUrl 
	 * @param url 
	 * @return ��null��ʾ��ַ���Ϸ�, ���򷵻�������urlֵ
	 */
	public static String normalize(String baseUrl, String url){
		try{
			if(url.startsWith("#")) return null;
			if(-1 != url.indexOf("?")) return null;
			//�ж��Ƿ����·��
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
	 * ���ͼƬ�ĸ�ʽ
	 * @param url ͼƬ��url
	 * @return ͼƬ��ʽ
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
	 * ���url���ڵ�������
	 * @param url ͼƬ��url
	 * @return host����null
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
	 * ����һ���������������ַ���ת��Ϊʮ������
	 * @param numberString
	 * @return ��Ӧ��int��
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
	  * ��buff���������ת��Ϊ�ֽ�����
	  * @param buff BufferedInputStream
	  * @return buff ���������е��ֽ���ɵ�����
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
	  * �������ʱ�����ʱ����, �������ַ�������ʽ����.���λ��date�� ������ȡʱ�䲻Ҫ����һ����
	  * @param timestamp1 ʱ���1
	  * @param timestamp2 ֮ʱ���2
	  * @return ��ǰʱ����ʱ�����ʱ�����ַ�����ʽ�� ��ʽΪ .. dates ..hours ..minutes..seconds
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
	  * ������0��double���ָ�ʽ����� ���磺 123456.789 ~ 123 456.7
	  * @param value
	  * @return value�ĸ�ʽ�������ʽ
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
	  * ���ַ�����ת��Ϊ�ַ���
	  * @param a �ַ�����
	  * @return a��Ӧ���ַ���
	  */
	 public static String ConvertCharArrayToString(char[] a){
		 StringBuilder sb = new StringBuilder();
		 for(char ch : a){
			 sb.append(ch);
		 }
		 return sb.toString();
	 }
	 
	 /**
	  * ��ָ��ip��ַ�Ľڵ㷢������
	  * @param req �������
	  * @param ip �ӽڵ��ip��ַ
	  * @param port �ӽڵ��port
	  * @return 1��ʾ�ɹ��� 0��ʾʧ��
	  */
	public static int sendRequest(DSBDataCommunication req, String ip, int port){
			Socket socket = null;
			ObjectOutputStream send = null;
			//�������ӽڵ������
			try {
				socket = new Socket(ip, port);
				//����add�������			
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
	 * ����Ϣ�Ե������ڵ���ʽչʾ�ڽ���
	 * @param message Ҫչʾ����Ϣ
	 */
	public static void showMessage(String message){
		JOptionPane.showMessageDialog(null, message);
		return;
	}
	
	/**
	 * ��YY-MM-DD HH:MM:SS����ʽ����һ��calendarʵ�����ڵ�string
	 * @param timestamp calendarʵ��
	 * @return YY-MM-DD HH:MM:SS��ʽ���ַ���
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
	 * ͳ��ָ��Ŀ¼�������ļ������������ܺ�
	 * @param dir Ŀ¼��·��
	 * @return �����ļ������������ܺ�
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
 * ��һ���������ܱ�����ʱ�׳������쳣
 * @author keyalin
 *
 */
class NotCallException extends Exception{
	NotCallException(){
		super("cann't call me!");
	}
}
