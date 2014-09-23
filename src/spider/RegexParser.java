/**
 * ���ļ��ṩ������������ʽ��ȡ��ҳ�ϵ����ӡ����ĵĽӿ�
 */
package spider;

import java.util.regex.*;
import java.util.*;

/**
 * ʹ��������ʽ������ҳ�� �����ҳ�����ӵļ��ϣ� ͼƬ���ӵļ��ϣ��Լ����� 
 * @author keyalin
 *
 */
public class RegexParser {

	/**
	 * �����ҳ�����ӵļ��ϣ� ���Ұ�����ӵ�taskmanager ��todeHrefͬ��������ȥ 
	 * @param html ��ҳ��Դ�����ַ���
	 * @param baseUrl ��ҳ����ַ
	 * @return
	 */
	public static void urlsExtreact(String html, String baseUrl){
		String urlRegex = "(href)[\\s]*=[\\s]*\"[^\"]*\"";
		Pattern p = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		while(m.find()){
			String s = m.group();
			s = s.replaceAll(" ", "");
			int beginIndex = s.indexOf("\"", 5);
			int lastIndex = s.lastIndexOf("\"");
			if(beginIndex == lastIndex) {
				s = null;
				continue;
			}
			else{
				s = s.substring(beginIndex + 1, lastIndex);
				s = Utility.normalize(baseUrl, s);
				if(s == null) continue;
				if(200 < s.length()) continue;
				Manager.todoHrefs.offer(s.intern());
			}			 
		}
	}

   /**
    * �����ҳ�����ӵļ��ϣ� ���Ұ�����ӵ�taskmanager ��todeImagesͬ��������ȥ 
    *@param html ��ҳ��Դ�����ַ���
	* @param baseUrl ��ҳ����ַ
    * @return
    */
	public static void imagesExtreact(String html, String baseUrl){
		String imageRegex = "(src)[\\s]*=[\\s]*\"[^\"]*\"";
		Pattern p = Pattern.compile(imageRegex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		while(m.find()){
			String s = m.group();
			s = s.replaceAll(" ", "");
			int beginIndex = s.indexOf("\"", 4);
			int lastIndex = s.lastIndexOf("\"");
			if(beginIndex == lastIndex) {
				s = null;
				continue;
			}
			else{
				s = s.substring(beginIndex + 1, lastIndex);
				s = Utility.normalize(baseUrl, s);
				if(s == null) continue;
				if(200 < s.length()) continue;
				Manager.todoImages.offer(s.intern());
			}
		}
	}

	
	/**
	 * �˷���Ϊ���������� ���ã�����һ����ָ��regex����html���õ���linkedlist
	 * @param html
	 * @param regex
	 * @return ��ָ��regex����html���õ���linkedlist
	 */
	private static LinkedList<String> extreact(String html, String regex){
		if(html == null) return null;
		else{
			Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(html);
			LinkedList<String> array = new LinkedList<String>();
			while(m.find()){
				array.add(m.group());
			}
			return array;
		}
	}

	/**
	 * ��ȡ��ҳ�ϵ��������ݣ� ��һ����û�У� �������ľͷ���null
	 * @param html ��ҳ��Դ�����ַ���
	 * @return
	 */
	public static String contentsExtreact(String html){
		if(html == null) return null;
		else{
			html = html.replaceAll("(<script).*?(/script>)", " ");
			html = html.replaceAll("(<style).*?(/style>)", " ");
			html = html.replaceAll("<.*?>", " ");
			html = html.replaceAll("&nbsp", "");
			html = html.replaceAll(" \\s*", " ");
			return html;
		}
	}
	public static void main(String[] args){
		Manager.todoHrefs = new LinksQueue<String>();
		int length = "http://www.google.com.hk/search?hl=zh-CN&source=hp&q=mysql+%E6%8F%92%E5%85%A5%E6%95%B0%E6%8D%AE&btnG=Google+%E6%90%9C%E7%B4%A2&meta=&aq=f&aqi=&aql=&oq=".length();
		System.out.println(length);
		String url = "http://www.whu.edu.cn/index.html";
		String html = GetUrlContents.getHtml(url, 100);
		RegexParser.urlsExtreact(html, url);
		//System.out.println(array.size());
		for(String s : Manager.todoHrefs){
			System.out.println(s);
		}
		}
	}

