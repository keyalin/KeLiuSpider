package test;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * @author keyalin
 *
 */
public abstract class Fangzheng extends MainFrame{
	Fangzheng(){}
	protected int hostnumber;//��������
	protected int nodenumber;//�ӽڵ�����
	protected int filenumber;//�ļ���������
	protected int swap;//�������
	protected int punishment;

	
	protected double totaltime = 0;//��������ʱ��
	protected long comunication = 0;//���ڵ㽻������
	protected int totalsize = 0;// ���������ļ���λ����
	protected int validfile = 0;//��Ч�ļ���
	protected double duplicatefile = 0;//�����ļ���
	
	/*
	 * �������п�������
	 */
	protected Fangzheng mainframe = this;
	protected ReentrantLock nodelock = new ReentrantLock();
	protected int[][] a;//��ʱ��
	protected int[] b;//���ڵ���ÿ���ӽڵ��ʱ�ӱ�
	protected Node[] nodes;
	protected Web[] webs;
	protected ExecutorService exec = Executors.newCachedThreadPool();
	
	/*
	 * �ӽڵ�
	 */
	class Node implements Runnable{
		int id;
		int timeout;//�ڵ㳬ʱ��ֵ
		int communication = 0;
		double broad;//�ӽڵ����
		Queue<Integer> todo = new Queue<Integer>();
		Queue<Integer> own = new Queue<Integer>();
		Queue<Integer> other = new Queue<Integer>();
		ReentrantLock lock = new ReentrantLock();
		Node(int id, int timeout, double broad){
			this.id = id;
			this.timeout = timeout;
			this.broad = broad;
		}
		public void run(){
			Random ran = new Random(Calendar.getInstance().get(Calendar.SECOND));
			while(!Thread.currentThread().isInterrupted()){
				if(null == todo.peek()){//û������
					continue;
				}
				int id = todo.peek();
				if(other.contains(id)) {//���񱻱���ץȡ
					todo.poll();
					continue;
				}
				webs[id].lock.lock();
				try{
					if(0 == webs[id].number){
						todo.poll();
						continue;
					}
					else{
						if(timeout < a[this.id][id]){//��ʱ							
							mainframe.nodelock.lock();
							mainframe.totaltime += timeout;
							mainframe.nodelock.unlock();
							if(own.contains(id)) continue;
							else{
								todo.poll();
								other.offer(id);
								mainframe.nodelock.lock();
								try{
									mainframe.comunication++;//���͸�����
									mainframe.totaltime += b[this.id];//���͸�������ʱ��
									int i = 0;
									for(Node node : nodes){
										if(node.id == this.id) continue;
										if(node.own.contains(id)) {//��������ڵ��Ѿ�ץȡ��
											i ++;
											break;
										}
									}
									if(0 == i){//û�������ڵ�ץȡ��
										for(Node node : nodes){
											if(node.id == this.id) continue;
											node.todo.offer(id);
											mainframe.comunication++;//������������
										}
									}
								}finally{
									mainframe.nodelock.unlock();
								}
							}
						}
						else{//ץȡ�ɹ�
							mainframe.nodelock.lock();
							mainframe.totaltime += a[this.id][id];
							int size = ran.nextInt(10) + 1;
							mainframe.totaltime += 1.0 * size / this.broad * a[this.id][id];//��ȡ�ļ�ʱ��
							mainframe.totalsize += size;//�ļ���������
							mainframe.nodelock.unlock();
							int produce = ran.nextInt(swap);//ģ����ҳ��������һ�����ʵõ�������web����
							if(1 == produce){
								todo.offer(ran.nextInt(mainframe.hostnumber));
							}/////
							if(own.contains(id)){//�Ѿ�ץȡ������
								webs[id].number--;
								mainframe.nodelock.lock();
								mainframe.validfile++;
								mainframe.nodelock.unlock();
								continue;
							}
							else{//���͸�����
								mainframe.nodelock.lock();
								mainframe.comunication++;
								mainframe.totaltime += b[this.id];//���͸�������ʱ��
								int i = 0;
								for(Node node : nodes){
									if(node.id == this.id) continue;
									if(node.own.contains(id)) {//��������ڵ��Ѿ�ץȡ��
										i ++;
										break;
									}
								}
								if(0 == i){//û�������ڵ�ץȡ��
									webs[id].number --;
									mainframe.validfile++;
									own.offer(id);
								}
								else{//�������ڵ�ץȡ��
									mainframe.comunication++;
									other.offer(id);//�����ظ��ļ����ظ�ʱ��,����Ҫ�����޸�
									double duplicate =1.0 * mainframe.punishment / 100 * b[this.id];
									mainframe.totalsize += b[this.id] * 5;
									duplicatefile += duplicate;
									mainframe.totaltime += b[this.id];
								}
								mainframe.nodelock.unlock();
							}
						}
					}
				}
				finally{
					webs[id].lock.unlock();
				}
			}
		}
	}
	
	/*
	 * ����
	 */
	class Web{
		int id;
		int number;
		ReentrantLock lock = new ReentrantLock();
	}
	
}

class Queue<T> extends LinkedBlockingQueue<T>{
	private static final long serialVersionUID = -2994401448353896856L;
}


