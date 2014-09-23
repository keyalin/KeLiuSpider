/**
 * 该文件用于使用dom树对网页提出正文和关键词
 */

package spider;

import  org.htmlparser.*;
import org.htmlparser.tags.ImageTag;
import  org.htmlparser.tags.LinkTag;
import  org.htmlparser.util.*;
import  org.htmlparser.nodes.*;
import  java.util.*;

	/**
	 * 使用dom方式解析html， 有以下几个功能， 提取网页所有的文本， 提取网页的正文主体，提取网页的链接集合，提取网页的图像链接集合。
	 *   提取含有指定关键字节点， 提取网页中的表单
	 * 该功能只适合对单个网页做仔细的分析，批量处理内存损耗非常大， 爬虫在解析网页提取链接过程中建议使用正则表达式。
	 */
public class DomParser {
	
	/**
	 * 获得网页中所有的文本，如何没有返回null 
	 * @param args
	 * @throws Exception
	 */
	public static String getTotalText(String html){
		if(html == null) return null;
		StringBuilder text = new StringBuilder();
		try{
			html = html.replaceAll("<--.*?-->", "");
			html = html.replaceAll("<style.*?style>", "");
			html = html.replaceAll("<script.*?script>", "");
			Parser parser = Parser.createParser(html, "UTF-8");
			NodeList nodes  =  parser.extractAllNodesThatMatch( new  NodeFilter() {
	             public   boolean  accept(Node node) {
	                 return   true ;
	            }
	        });
			SimpleNodeIterator it = nodes.elements();
			while(it.hasMoreNodes()){
				Node node = it.nextNode();
				if(node instanceof TextNode) {
					text.append(node.toPlainTextString());
					text.append("\r\n");
				}
				else continue;
			}
			return text.toString();
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 获取网页中主体正文部分， 一般来说网页中文本最长的那个节点，就是网页的正文主体部分。
	 * 有没有新的算法？
	 * @param args
	 * @throws Exception
	 */
	public static String getMainText(String html){
		if(html == null) return null;
		String mainText = "";
		try{
			html = html.replaceAll("<--.*?-->", "");
			html = html.replaceAll("<style.*?style>", "");
			html = html.replaceAll("<script.*?script>", "");
			Parser parser = Parser.createParser(html, "UTF-8");
			NodeList nodes  =  parser.extractAllNodesThatMatch( new  NodeFilter() {
	             public   boolean  accept(Node node) {
	                 return   true ;
	            }
	        });
			SimpleNodeIterator it = nodes.elements();
			while(it.hasMoreNodes()){
				Node node = it.nextNode();
				if(node instanceof TextNode) {
					String temp = node.toPlainTextString();
					if(mainText.length() < temp.length()){
						mainText = temp;
					}
				}
			}
			if(mainText == "") return null;
			else{
				return mainText;
			}
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 获取网页的链接集合
	 * @param args
	 * @throws Exception
	 */
	public static Map<String, String> getLinks(String html){
		if(html == null) return null;
		Map<String, String> links = new HashMap<String, String>();
		try{
			html = html.replaceAll("<--.*?-->", "");
			html = html.replaceAll("<style.*?style>", "");
			html = html.replaceAll("<script.*?script>", "");
			Parser parser = Parser.createParser(html, "UTF-8");
			NodeList nodes  =  parser.extractAllNodesThatMatch( new  NodeFilter() {
	             public   boolean  accept(Node node) {
	                 return   true ;
	            }
	        });
			SimpleNodeIterator it = nodes.elements();
			while(it.hasMoreNodes()){
				Node node = it.nextNode();
				if(node instanceof LinkTag) {
					LinkTag tag = (LinkTag)node;
					String url = tag.getLink();
					String text = tag.getStringText();
					links.put(url, text);
				}
				else continue;
			}
			return links;

		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 获取网页的图片链接集合
	 * @param args
	 * @throws Exception
	 */
	public static Map<String, String> getImgs(String html){
		if(html == null) return null;
		Map<String, String> imgs = new HashMap<String, String>();
		try{
			html = html.replaceAll("<--.*?-->", "");
			html = html.replaceAll("<style.*?style>", "");
			html = html.replaceAll("<script.*?script>", "");
			Parser parser = Parser.createParser(html, "UTF-8");
			NodeList nodes  =  parser.extractAllNodesThatMatch( new  NodeFilter() {
	             public   boolean  accept(Node node) {
	                 return   true ;
	            }
	        });
			SimpleNodeIterator it = nodes.elements();
			while(it.hasMoreNodes()){
				Node node = it.nextNode();
				if(node instanceof ImageTag) {
					ImageTag tag = (ImageTag)node;
					String url = tag.getImageURL();
					String text = tag.getText();
					imgs.put(url, text);
				}
				else continue;
			}
			return imgs;

		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 获取具有包含指定关键字的节点集合
	 */
	
	public static LinkedList<String> getKeywordText(String html, String key){
		if(html == null || key == null) return null;
		LinkedList<String> list = new LinkedList<String>();
		try{
			html = html.replaceAll("<--.*?-->", "");
			html = html.replaceAll("<style.*?style>", "");
			html = html.replaceAll("<script.*?script>", "");
			Parser parser = Parser.createParser(html, "UTF-8");
			NodeList nodes  =  parser.extractAllNodesThatMatch( new  NodeFilter() {
	             public   boolean  accept(Node node) {
	                 return   true ;
	            }
	        });
			SimpleNodeIterator it = nodes.elements();
			while(it.hasMoreNodes()){
				Node node = it.nextNode();
				NodeList ChildList = node.getChildren();
				if(ChildList == null){
					String text = node.toPlainTextString();
					if(text.indexOf(key) != -1) list.add(text);
				}
				else continue;
			}
			return list;
		}catch(Exception e){
			return null;
		}
	}
	public static void main(String[] args){
		System.out.print(getMainText(GetUrlContents.getHtml("http://topic.t.sina.com.cn/sports/TaidavsQUANBEI/index.shtml", 3000)));
	}
}

	
	


