package practice;

import java.util.concurrent.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class WriterObj implements Runnable {

    public Queue<String> queue;

    public WriterObj(Queue<String> queue) {
        this.queue = queue;
        new Thread(this, "Producer").start();
    }

    @Override
    public void run() {
        ReaderWriter.acquireWriteLock();
        String s = "";
        for (int j = 0; j < 5; j++) {
            int c = 65 + (int) (Math.random() * 26);
            s += (char) c;
        }
        System.out.println("Wrote to queue");
        this.queue.add(s);
        ReaderWriter.releaseWriteLock();
    }
}

class ReaderObj implements Runnable {
    // public int count;
    public Queue<String> queue;
    public int wait;

    public ReaderObj(Queue<String> queue, int wait) {
        this.queue = queue;
        this.wait = wait;
        new Thread(this, "Consumer").start();
    }

    @Override
    public void run() {
        ReaderWriter.acquireReadLock();
        try {
            Thread.sleep(wait * 1000);
        } catch (InterruptedException e) {

        }
        System.out.println("Queue elements:" + queue);
        ReaderWriter.releaseReadLock();
    }
}

public class ReaderWriter {
    public static int limit = 5;
    public static int count = 100;
    public static ReentrantLock lock = new ReentrantLock();
    public static final Semaphore writeLock = new Semaphore(1);
    public static final Condition condition = lock.newCondition();
    public static boolean writing = false;
    private static int readers = 0;

    public static void acquireReadLock() {
        lock.lock();
        // if (readers == 0) {
        //     acquireWriteLock();
        // }
        while (writing){
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        readers++;
        lock.unlock();
    }

    public static void releaseReadLock() {
        lock.lock();
        // if (readers == 1) {
        //     releaseWriteLock();
        // }
        readers--;
        if (readers == 0){
            condition.signalAll();
        }
        lock.unlock();
    }

    public static void acquireWriteLock() {
        // try {
        //     writeLock.acquire();
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        lock.lock();
        while(readers > 0 || writing){
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        writing = true;
        lock.unlock();
    }

    public static void releaseWriteLock() {
        // writeLock.release();
        lock.lock();
        try {
            writing = false;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
        lock.unlock();
    }

    public static void main(String[] args) throws Exception {
        Queue<String> queue = new LinkedList<>();

        new ReaderObj(queue, 2);
        new WriterObj(queue);
        new ReaderObj(queue, 3);
        new ReaderObj(queue, 2);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        System.out.println("Queue Elements: " + queue);
    }
}
