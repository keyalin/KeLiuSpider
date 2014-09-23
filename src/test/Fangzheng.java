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
	protected int hostnumber;//主机数量
	protected int nodenumber;//子节点数量
	protected int filenumber;//文件个数总量
	protected int swap;//变异概率
	protected int punishment;

	
	protected double totaltime = 0;//爬虫下载时间
	protected long comunication = 0;//主节点交互次数
	protected int totalsize = 0;// 爬虫下载文件单位总量
	protected int validfile = 0;//有效文件数
	protected double duplicatefile = 0;//下载文件数
	
	/*
	 * 程序运行控制数据
	 */
	protected Fangzheng mainframe = this;
	protected ReentrantLock nodelock = new ReentrantLock();
	protected int[][] a;//超时表
	protected int[] b;//主节点与每个子节点的时延表
	protected Node[] nodes;
	protected Web[] webs;
	protected ExecutorService exec = Executors.newCachedThreadPool();
	
	/*
	 * 子节点
	 */
	class Node implements Runnable{
		int id;
		int timeout;//节点超时阈值
		int communication = 0;
		double broad;//子节点带宽
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
				if(null == todo.peek()){//没有任务
					continue;
				}
				int id = todo.peek();
				if(other.contains(id)) {//任务被别人抓取
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
						if(timeout < a[this.id][id]){//超时							
							mainframe.nodelock.lock();
							mainframe.totaltime += timeout;
							mainframe.nodelock.unlock();
							if(own.contains(id)) continue;
							else{
								todo.poll();
								other.offer(id);
								mainframe.nodelock.lock();
								try{
									mainframe.comunication++;//发送给主机
									mainframe.totaltime += b[this.id];//发送给主机的时间
									int i = 0;
									for(Node node : nodes){
										if(node.id == this.id) continue;
										if(node.own.contains(id)) {//检查其他节点已经抓取过
											i ++;
											break;
										}
									}
									if(0 == i){//没有其他节点抓取过
										for(Node node : nodes){
											if(node.id == this.id) continue;
											node.todo.offer(id);
											mainframe.comunication++;//主机交互次数
										}
									}
								}finally{
									mainframe.nodelock.unlock();
								}
							}
						}
						else{//抓取成功
							mainframe.nodelock.lock();
							mainframe.totaltime += a[this.id][id];
							int size = ran.nextInt(10) + 1;
							mainframe.totaltime += 1.0 * size / this.broad * a[this.id][id];//读取文件时间
							mainframe.totalsize += size;//文件下载总量
							mainframe.nodelock.unlock();
							int produce = ran.nextInt(swap);//模拟网页解析，有一定概率得到其他的web划分
							if(1 == produce){
								todo.offer(ran.nextInt(mainframe.hostnumber));
							}/////
							if(own.contains(id)){//已经抓取该主机
								webs[id].number--;
								mainframe.nodelock.lock();
								mainframe.validfile++;
								mainframe.nodelock.unlock();
								continue;
							}
							else{//发送给主机
								mainframe.nodelock.lock();
								mainframe.comunication++;
								mainframe.totaltime += b[this.id];//发送给主机的时间
								int i = 0;
								for(Node node : nodes){
									if(node.id == this.id) continue;
									if(node.own.contains(id)) {//检查其他节点已经抓取过
										i ++;
										break;
									}
								}
								if(0 == i){//没有其他节点抓取过
									webs[id].number --;
									mainframe.validfile++;
									own.offer(id);
								}
								else{//有其他节点抓取过
									mainframe.comunication++;
									other.offer(id);//增加重复文件和重复时间,可能要进行修改
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
	 * 主机
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


