package practice;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

class QObject{
	private Runnable command;
	private long scheduleTime;
	public long delay;
	public int type;

	public QObject(Runnable command, long scheduleTime, int type){
		this.command = command;
		this.scheduleTime = scheduleTime;
		this.type = type;
	}

	public QObject(Runnable command, long scheduleTime, long delay, int type){
		this.command = command;
		this.scheduleTime = scheduleTime;
		this.delay = delay;
		this.type = type;
	}

	public long getScheduleTime(){
		return this.scheduleTime;
	}

	public void setScheduleTime(long scheduleTime){
		this.scheduleTime = scheduleTime;
	}
	
	public Runnable getRunnable(){
		return this.command;
	}
}

class ScheduleThreads{

	private PriorityQueue<QObject> pq;
	private Lock lock = new ReentrantLock();
	private Condition taskAdded = lock.newCondition();
	private ThreadPoolExecutor exec;

	public ScheduleThreads(int numThreads){
		pq = new PriorityQueue<>(Comparator.comparingLong(QObject::getScheduleTime));
		exec = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
	}
	
	public void schedule(Runnable command, long delay, TimeUnit unit, int type){
		lock.lock();
		delay = unit.toMillis(delay);
		pq.add(new QObject(command, System.currentTimeMillis(), delay, type));
		taskAdded.signalAll();
		lock.unlock();
	}
	
	public void schedule(Runnable command, long initialDelay, long delay, TimeUnit unit, int type){
		lock.lock();
		pq.add(new QObject(command, System.currentTimeMillis() + unit.toMillis(initialDelay), unit.toMillis(delay), type));
		taskAdded.signalAll();
		lock.unlock();
	}

	public void run(){
		while(true){
			lock.lock();
			try {
				while (pq.isEmpty()){
					taskAdded.await();
				}
				if (System.currentTimeMillis() < pq.peek().getScheduleTime()){
					taskAdded.await(pq.peek().getScheduleTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
				}
				else {
					QObject obj = pq.poll();
					CompletableFuture<?> future = CompletableFuture.runAsync(obj.getRunnable(), exec);
					if (obj.type == 1){
						obj.setScheduleTime(System.currentTimeMillis() + obj.delay);
						pq.add(obj);
					}
					else if (obj.type == 2){
						future.thenRunAsync(()->{
							obj.setScheduleTime(System.currentTimeMillis() + obj.delay);
							pq.add(obj);
						});
					}
				}
			} catch (InterruptedException e){
				System.out.println(e);
			} finally {
				lock.unlock();
			}
		}
	}
    
}


class ScheduledExecutorService {
	public static ScheduleThreads scheduler = new ScheduleThreads(10);
	/**
	* Creates and executes a one-shot action that becomes enabled after the given delay.
	*/
	public static void schedule(Runnable command, long delay, TimeUnit unit) {
		scheduler.schedule(command, delay, unit, 0);
	}

	/**
	* Creates and executes a periodic action that becomes enabled first after the given initial delay, and 
	* subsequently with the given period; that is executions will commence after initialDelay then 
	* initialDelay+period, then initialDelay + 2 * period, and so on.
	*/
	public static void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		scheduler.schedule(command, initialDelay, period, unit, 1);
	}

	/*
	* Creates and executes a periodic action that becomes enabled first after the given initial delay, and 
	* subsequently with the given delay between the termination of one execution and the commencement of the next.
	*/
	public static void scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		scheduler.schedule(command, initialDelay, delay, unit, 2);
	}

	public static void main(String[] args) throws Exception {
		
        Runnable task1 = getRunnableTask("Task1");
		schedule(task1, 1, TimeUnit.SECONDS);
		Runnable task2 = getRunnableTask("Task2");
		scheduleAtFixedRate(task2,1, 5, TimeUnit.SECONDS);
		Runnable task3 = getRunnableTask("Task3", 15, TimeUnit.SECONDS);
		scheduleWithFixedDelay(task3,1,2,TimeUnit.SECONDS);
		Runnable task4 = getRunnableTask("Task4");
		scheduleAtFixedRate(task4,1, 10, TimeUnit.SECONDS);
		scheduler.run();
    }

	private static Runnable getRunnableTask(String s) {
		return () -> {
			System.out.println(s +" started at " + System.currentTimeMillis());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(s +" ended at " + System.currentTimeMillis());
		};
	}

	private static Runnable getRunnableTask(String s, long time_to_sleep, TimeUnit unit) {
		return () -> {
			System.out.println(s +" started at " + System.currentTimeMillis());
			try {
				Thread.sleep(unit.toMillis(time_to_sleep));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(s +" ended at " + System.currentTimeMillis());
		};
	}

}
