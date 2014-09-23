package spider;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

/**
 * �����ṩ�������ܣ� �ṩ��ҳ��ͼƬ���ֽ����������ݿ�洢�� �ṩ��ҳ���ݵ��ַ���������ҳ����
 * @author keyalin
 *
 */
public class GetUrlContents {
	
	/**
	 * ����һ����ַ�� ����վ��html�ַ���.���û�л�ã��ͷ���null, �����ʱ���ء�NULL��
	 * @param baseUrl
	 * @param timeout
	 * @return
	 */
	@SuppressWarnings("null")
	public static String getHtml(String baseUrl, int timeout) {
		HttpURLConnection huc = null;
		try{
			huc = (HttpURLConnection) new URL(baseUrl).openConnection();
			huc.setConnectTimeout(timeout);
			huc.setReadTimeout(timeout);
			huc.connect();
		}catch(SocketTimeoutException e){
			huc.disconnect();
			return "NULL";
		}catch(Exception e){
			try{
				huc.disconnect();
				return null;
			}catch(Exception e1){
				return null;
			}
		}
		BufferedInputStream buff1 = null;
		byte[] bytes = null;
		try{
			buff1 = new BufferedInputStream(huc.getInputStream());
			bytes = Utility.readBufferToBytes(buff1);
		}catch(SocketTimeoutException e){
			huc.disconnect();
			return "NULL";
		}
		catch(IOException e){
			return null;
		}finally{
			try{
				buff1.close();
			}catch(Exception e){}
		}
		ByteBuffer buff = ByteBuffer.allocate(bytes.length);
		buff.rewind();
		buff.put(bytes);
		buff.rewind();
		byte[] dst = new byte[1000];
		ByteBuffer temp = buff.get(dst);
		temp.rewind();
		String str = (Charset.forName("utf8").decode(temp)).toString();
		buff.rewind();
		String charset = get(str);
		if(null == charset) return null;
		str = null;
		String html = (Charset.forName(charset).decode(buff)).toString();
		temp.clear();
		buff.array();
		temp = null;
		buff = null;
		dst = null;
		charset = null;
		bytes = null;
		return html;
	}

	/**
	 * ����ҳͷ1000���ַ��������ҳ��charset��ʽ
	 * @param str ��ҳͷ�ַ���
	 * @return ���ҵ�����charset�� û�з���null
	 */
	public static String get(String str){
		String charset = null;
		String regex = "charset\\s*+=\\s*+\\S*+(\\s*+|\"|\\\\)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		if(m.find()) {
			charset = m.group();
			int index = charset.indexOf(">") - 1;
			if(index > 0) charset = charset.substring(0, index);					
			charset = charset.replaceAll(" ", "");
			charset = charset.replaceAll("charset=", "");
			charset = charset.replace("\"", "");	
			return charset;
		}
		else 
			return null;
	}	
	
	/**
	 * ��ָ����ַ��������ת��Ϊbyte����
	 * @param url ָ����ַ
	 * @return byte����, null��ʾ���ӳ�ʱ
	 */
	public static byte[] getBytes(String url, int timeout){
		BufferedInputStream buff = null;
		HttpURLConnection huc = null;
		try{
			huc = (HttpURLConnection) new URL(url).openConnection();
			huc.setConnectTimeout(timeout);
			huc.setReadTimeout(timeout);
			try{
				huc.connect();
			}catch(SocketTimeoutException e){
				huc.disconnect();
				return null;
			}
			buff = new BufferedInputStream(huc.getInputStream());
			return Utility.readBufferToBytes(buff);
		}catch(IOException e){
			return null;
		}finally{
			try{
				if(null != buff) buff.close();
				if(null != huc) huc.disconnect();
			}catch(Exception e){}
		}
	}
		
	public static void main(String[] args){
		String url = "http://www.sina.com";
		System.out.println(Arrays.toString(getBytes(url, 1000)));
	}
}
