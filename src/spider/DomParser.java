/**
 * ���ļ�����ʹ��dom������ҳ������ĺ͹ؼ���
 */

package spider;

import  org.htmlparser.*;
import org.htmlparser.tags.ImageTag;
import  org.htmlparser.tags.LinkTag;
import  org.htmlparser.util.*;
import  org.htmlparser.nodes.*;
import  java.util.*;

	/**
	 * ʹ��dom��ʽ����html�� �����¼������ܣ� ��ȡ��ҳ���е��ı��� ��ȡ��ҳ���������壬��ȡ��ҳ�����Ӽ��ϣ���ȡ��ҳ��ͼ�����Ӽ��ϡ�
	 *   ��ȡ����ָ���ؼ��ֽڵ㣬 ��ȡ��ҳ�еı�
	 * �ù���ֻ�ʺ϶Ե�����ҳ����ϸ�ķ��������������ڴ���ķǳ��� �����ڽ�����ҳ��ȡ���ӹ����н���ʹ��������ʽ��
	 */
public class DomParser {
	
	/**
	 * �����ҳ�����е��ı������û�з���null 
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
	 * ��ȡ��ҳ���������Ĳ��֣� һ����˵��ҳ���ı�����Ǹ��ڵ㣬������ҳ���������岿�֡�
	 * ��û���µ��㷨��
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
	 * ��ȡ��ҳ�����Ӽ���
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
	 * ��ȡ��ҳ��ͼƬ���Ӽ���
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
	 * ��ȡ���а���ָ���ؼ��ֵĽڵ㼯��
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

	
	


