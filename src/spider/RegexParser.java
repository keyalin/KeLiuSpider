/**
 * 该文件提供了利用正则表达式提取网页上的链接、正文的接口
 */
package spider;

import java.util.regex.*;
import java.util.*;

/**
 * 使用正则表达式解析网页， 获得网页上链接的集合， 图片链接的集合，以及正文 
 * @author keyalin
 *
 */
public class RegexParser {

	/**
	 * 获得网页上链接的集合， 并且把它添加到taskmanager 的todeHref同步队列中去 
	 * @param html 网页的源代码字符串
	 * @param baseUrl 网页的网址
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
    * 获得网页上链接的集合， 并且把它添加到taskmanager 的todeImages同步队列中去 
    *@param html 网页的源代码字符串
	* @param baseUrl 网页的网址
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
	 * 此方法为保留方法。 作用：返回一个用指定regex解析html而得到的linkedlist
	 * @param html
	 * @param regex
	 * @return 用指定regex解析html而得到的linkedlist
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
	 * 获取网页上的正文内容， 若一个都没有， 若无正文就返回null
	 * @param html 网页的源代码字符串
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

