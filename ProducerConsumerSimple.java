package practice;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class ProducerConsumerHelper {
    private int[] buffer;
    private int fill_ptr = 0;
    private int use_ptr = 0;
    private int count = 0;
    private int max_size = 10;
    private ReentrantLock lock = new ReentrantLock();
    private Condition fill = lock.newCondition();
    private Condition empty = lock.newCondition();
    private Semaphore sFull = new Semaphore(0);
    private Semaphore sEmpty;

    public ProducerConsumerHelper(int[] buffer) {
        this.buffer = buffer;
        this.sEmpty = new Semaphore(max_size);
    }

    public ProducerConsumerHelper(int[] buffer, int max_size) {
        this.buffer = buffer;
        this.max_size = max_size;
        this.sEmpty = new Semaphore(max_size);
    }

    private void put(int val) {
        this.buffer[this.fill_ptr] = val;
        this.fill_ptr = (this.fill_ptr + 1) % (this.max_size);
        this.count++;

    }

    private int get() {
        int ret = this.buffer[this.use_ptr];
        this.use_ptr = (this.use_ptr + 1) % (this.max_size);
        this.count--;
        return ret;
    }

    public Runnable producer_cv(int loops) {
        return (() -> {
            for (int i = 0; i < loops; i++) {
                lock.lock();
                try {
                    while (this.count == this.max_size) {
                        this.empty.await();
                    }
                    put((int) (Math.random() * 100 + 1));
                    this.fill.signal();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    public Runnable consumer_cv(int loops) {
        return (() -> {
            for (int i = 0; i < loops; i++) {
                int val = -1;
                lock.lock();
                try {
                    while (this.count == 0) {
                        this.fill.await();
                    }
                    val = get();
                    this.empty.signal();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                    System.out.println(val);
                }
            }
        });
    }

    public Runnable producer_sem(int loops) {
        return (() -> {
            for (int i = 0; i < loops; i++) {
                try {
                    sEmpty.acquire();
                    lock.lock();
                    if (this.count == this.max_size) {
                        throw new Exception("Buffer full");
                    }
                    put((int) (Math.random() * 100 + 1));
                    lock.unlock();
                    sFull.release();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("after produce " + Arrays.toString(this.buffer));
                }
            }
        });
    }

    public Runnable consumer_sem(int loops) {
        return (() -> {
            for (int i = 0; i < loops; i++) {
                int val = -1;
                try {
                    sFull.acquire();
                    lock.lock();
                    if (this.count == 0) {
                        throw new Exception("Buffer empty");
                    }
                    val = get();
                    lock.unlock();
                    sEmpty.release();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("after consume " + val);
                }
            }
        });
    }
}

class ProducerConsumerSimple {
    public static void main(String[] args) {
        int max_size = 5;
        int[] buffer = new int[max_size];
        ProducerConsumerHelper pc = new ProducerConsumerHelper(buffer, max_size);
        // Runnable producer_cv = pc.producer_cv(10);
        // Runnable consumer_cv = pc.consumer_cv(10);
        Runnable producer_sem1 = pc.producer_sem(10);
        Runnable consumer_sem1 = pc.consumer_sem(10);
        Runnable producer_sem2 = pc.producer_sem(10);
        Runnable consumer_sem2 = pc.consumer_sem(10);
        // new Thread(producer_cv).start();
        // new Thread(consumer_cv).start();
        new Thread(producer_sem1).start();
        new Thread(consumer_sem1).start();
        new Thread(producer_sem2).start();
        new Thread(consumer_sem2).start();
    }
}
