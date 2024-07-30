package practice;

import java.util.function.Consumer;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.LinkedList;

class ProducerObj implements Callable<Integer> {
    // public int count;
    public Helper helper;

    public ProducerObj(Helper helper) {
        this.helper = helper;
    }

    @Override
    public synchronized Integer call() throws Exception {
        return this.helper.produce();
    }
}

class Helper {
    Queue<String> queue;
    int limit;
    int count;
    int consumed = 0;
    public Consumer<String> print = a -> System.out.println(a);
    public Semaphore sOpen;
    public Semaphore sClose;

    public Helper(Queue<String> queue, int limit, int count) {
        this.queue = queue;
        this.limit = limit;
        this.count = count;
        sOpen = new Semaphore(limit);
        sClose = new Semaphore(0);
    }

    public int produce() {
        try {
            for (int i = 0; i < this.count; i++) {
                String s = "";
                for (int j = 0; j < 5; j++) {
                    int c = 65 + (int) (Math.random() * 26);
                    s += (char) c;
                }
                sOpen.acquire();
                if (this.queue.size() == this.limit)
                    throw new Exception("Limit Reached");
                this.queue.add(s);
                // System.out.println(this.queue.size());
                sClose.release();
            }
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
        return 0;
    }

    public int consume() {
        try {
            while (consumed < this.count) {
                sClose.acquire();
                if (this.queue.isEmpty())
                    throw new Exception("No elements");
                print.accept("Consumed " + (++consumed) + ": " + this.queue.remove());
                // System.out.println(this.queue.size());
                sOpen.release();
            }
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }
}

class ConsumerObj implements Callable<Integer> {
    // public int count;
    public Helper helper;

    public ConsumerObj(Helper helper) {
        this.helper = helper;
    }

    @Override
    public synchronized Integer call() throws Exception {
        return this.helper.consume();
    }
}

class ProducerConsumer {

    public static int limit = 5;
    public static int count = 100;

    public static void main(String[] args) throws Exception {
        Queue<String> queue = new LinkedList<>();
        Helper helper = new Helper(queue, limit, count);
        ProducerObj t0 = new ProducerObj(helper);
        FutureTask<Integer> f1 = new FutureTask<>(t0);
        Thread tc0 = new Thread(f1);
        ConsumerObj t1 = new ConsumerObj(helper);
        FutureTask<Integer> f2 = new FutureTask<>(t1);
        Thread tc1 = new Thread(f2);
        tc0.start();
        tc1.start();
        System.out.println("Producer result: " + f1.get());
        System.out.println("Consumer result: " + f2.get());
        System.out.println("Queue Elements: " + queue);
    }
}
