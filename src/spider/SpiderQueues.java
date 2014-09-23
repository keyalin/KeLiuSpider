/**
 * 该文件定义了爬虫系统下载数据的数据结构
 */

package spider;

import java.util.concurrent.*;

/**
 * 定义了两个同步队列， LinksQueue用于存放将要爬取的网页链接，ImgsQueue用于存放将要爬取的图片
 * @author keyalin
 *
 */
public class SpiderQueues {

}

/**
 * LinksQueue用于存放将要爬取的网页链接
 * @author keyalin
 *
 */
class LinksQueue<T> extends LinkedBlockingQueue<T>{
	
}

/**
 * ImagsQueue用于存放将要爬取的图片
 * @author keyalin
 *
 */
class ImagesQueue<T> extends LinkedBlockingQueue<T>{
	
}

/**
 * visitedQueue用于存放已经被爬取的链接。
 * 一旦关机，数据就丢失了， 有没有号得方法解决这个问题。 定期保存， 然后启动程序的时候，将队列装入
 * 如何处理visitedQueue大小 ， 以及快速访问的问题， 这个问题要仔细研究
 */
class VisitedQueue<T> extends LinkedBlockingQueue<T>{
	
}




