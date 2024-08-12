package practice;

import java.util.function.Consumer;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.LinkedList;

// ************************* Simplified and implemented with locks *************************
// ********************* (in case of multiple producers and consumers *********************
// **************************** in ProducerConsumerSimple.java) ****************************

class ProducerObj implements Runnable {
    // public int count;
    public Helper helper;
    public int count;

    public ProducerObj(Helper helper, int count) {
        this.helper = helper;
        this.count = count;
        new Thread(this, "Producer").start();
    }

    @Override
    public void run() {
        for (int i = 0; i < this.count; i++) {
            String s = "";
            for (int j = 0; j < 5; j++) {
                int c = 65 + (int) (Math.random() * 26);
                s += (char) c;
            }
            this.helper.produce(s);
        }
    }
}

class ConsumerObj implements Runnable {
    // public int count;
    public Helper helper;
    public int count;

    public ConsumerObj(Helper helper, int count) {
        this.helper = helper;
        this.count = count;
        new Thread(this, "Consumer").start();
    }

    @Override
    public void run() {
        for (int i = 0; i < this.count; i++) {
            this.helper.consume();
        }
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

    public int produce(String s) {
        try {
            sOpen.acquire();
            if (this.queue.size() == this.limit)
                throw new Exception("Limit Reached");
            this.queue.add(s);
            // System.out.println(this.queue.size());
            sClose.release();
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
        return 0;
    }

    public int consume() {
        try {
            sClose.acquire();
            if (this.queue.isEmpty())
                throw new Exception("No elements");
            print.accept("Consumed " + (++consumed) + ": " + this.queue.remove());
            // System.out.println(this.queue.size());
            sOpen.release();
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
        return 0;
    }
}

class ProducerConsumer {

    public static int limit = 5;
    public static int count = 100;

    public static void main(String[] args) throws Exception {
        Queue<String> queue = new LinkedList<>();
        Helper helper = new Helper(queue, limit, count);
        new ProducerObj(helper, count);
        new ConsumerObj(helper, count);
        System.out.println("Queue Elements: " + queue);
    }
}
