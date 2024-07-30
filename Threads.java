package practice;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class MyThread implements Runnable {
    public int thread_no;

    public MyThread(int thread_no) {
        this.thread_no = thread_no;
    }

    @Override
    public void run() {
        System.out.println("Thread Number: " + this.thread_no);
        try {
            Thread.sleep(1000 * (this.thread_no + 1));
        } catch (InterruptedException e) {

        }
        System.out.println("Done: " + this.thread_no);
    }
}

public class Threads {

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);

    private static Runnable getRunnableTask(String s, long time_to_sleep, TimeUnit unit) {
        return () -> {
            System.out.println(s + " started at " + System.currentTimeMillis());
            try {
                Thread.sleep(unit.toMillis(time_to_sleep));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(s + " ended at " + System.currentTimeMillis());
        };
    }

    public static void main(String[] args) {
        // With Thread
        ArrayList<Thread> ts = new ArrayList<>();
        for(int i=0; i<5; i++){
            // MyThread t = new MyThread(i);
            Thread tc = new Thread(getRunnableTask("Task"+i, i+1, TimeUnit.SECONDS));
            ts.add(tc);
            tc.start();
        }

        // With Future and ThreadPoolExecutor
        Future<?> f;
        class FutureWithName {
            String name;
            Future<?> future;

            public FutureWithName(String name, Future<?> future) {
                this.name = name;
                this.future = future;
            }
        }
        ArrayList<FutureWithName> futures = new ArrayList<>();
        for (int i = 10; i > 0; i--) {
            int wait = i;
            f = executor.submit(getRunnableTask("Task"+wait, wait*2, TimeUnit.SECONDS));
            futures.add(new FutureWithName("Task" + wait, f));
        }
        System.out.println("Task submitted");
        for (FutureWithName f1 : futures) {
            try {
                f1.future.get();
                System.out.println("Done waiting for " + f1.name);
            } catch (Exception e) {

            }
        }

        // With CompletableFuture
        List<CompletableFuture<Void>> futures1 = new ArrayList<>();
        CompletableFuture<Void> future;
        for (int i = 10; i > 0; i--) {
            int wait = i;
            future = CompletableFuture.runAsync(getRunnableTask("Task"+wait, wait*2, TimeUnit.SECONDS), executor);

            // Chain a callback to print "Done waiting for" when the task completes
            futures1.add(future.thenRun(() -> System.out.println("Done waiting for Task" + wait)));
        }
        System.out.println("Tasks submitted");

        // Wait for all tasks to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures1.toArray(new CompletableFuture[0]));
        try {
            allOf.get(); // Blocks until all tasks are complete
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            // Wait for the executor to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Force shutdown if tasks are not completed within the timeout
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
