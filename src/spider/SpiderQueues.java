/**
 * ���ļ�����������ϵͳ�������ݵ����ݽṹ
 */

package spider;

import java.util.concurrent.*;

/**
 * ����������ͬ�����У� LinksQueue���ڴ�Ž�Ҫ��ȡ����ҳ���ӣ�ImgsQueue���ڴ�Ž�Ҫ��ȡ��ͼƬ
 * @author keyalin
 *
 */
public class SpiderQueues {

}

/**
 * LinksQueue���ڴ�Ž�Ҫ��ȡ����ҳ����
 * @author keyalin
 *
 */
class LinksQueue<T> extends LinkedBlockingQueue<T>{
	
}

/**
 * ImagsQueue���ڴ�Ž�Ҫ��ȡ��ͼƬ
 * @author keyalin
 *
 */
class ImagesQueue<T> extends LinkedBlockingQueue<T>{
	
}

/**
 * visitedQueue���ڴ���Ѿ�����ȡ�����ӡ�
 * һ���ػ������ݾͶ�ʧ�ˣ� ��û�кŵ÷������������⡣ ���ڱ��棬 Ȼ�����������ʱ�򣬽�����װ��
 * ��δ���visitedQueue��С �� �Լ����ٷ��ʵ����⣬ �������Ҫ��ϸ�о�
 */
class VisitedQueue<T> extends LinkedBlockingQueue<T>{
	
}




