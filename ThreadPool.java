package practice;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ThreadPool {
    private final List<Worker> workers;
    private final Queue<Runnable> taskQueue;
    private boolean isStopped;

    public ThreadPool(int numThreads) {
        workers = new LinkedList<>();
        taskQueue = new LinkedList<>();
        isStopped = false;

        for (int i = 0; i < numThreads; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            worker.start();
        }
    }

    public synchronized void enqueue(Runnable task) {
        if (isStopped) throw new IllegalStateException("ThreadPool is stopped");
        taskQueue.add(task);
        notify();
    }

    public synchronized void stop() {
        isStopped = true;
        // Wake up all workers
        notifyAll();
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }

    private synchronized Runnable getTask() throws InterruptedException {
        while (taskQueue.isEmpty() && !isStopped) {
            wait();
        }
        if (isStopped) return null;
        return taskQueue.poll();
    }

    private class Worker extends Thread {
        public void run() {
            while (true) {
                Runnable task;
                try {
                    task = getTask();
                    if (task == null) break;
                    task.run();
                } catch (InterruptedException e) {
                    // Thread interrupted, exit
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        ThreadPool pool = new ThreadPool(5);

        for (int i = 1; i <= 100; i++) {
            int taskId = i;
            pool.enqueue(() -> {
                System.out.println("Executing: " + taskId + " in thread: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(100); // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for tasks to complete and stop the pool (in a real scenario, you may want to use a more sophisticated shutdown mechanism)
        try {
            Thread.sleep(5000); // Wait some time for tasks to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pool.stop();
    }
}
