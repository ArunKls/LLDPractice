package practice;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;

class Promise {
    String target;
    Runnable newTarget;
    boolean isRunning = true;
    Exception exception;
    boolean isRejected = false;
    boolean isResolved = false;
    ThreadPoolExecutor executor;
    ArrayList<Runnable> callbacks = new ArrayList<>();

    public Promise(ThreadPoolExecutor exec){
        this.executor = exec;
    }
    public Promise(ThreadPoolExecutor exec, String target){
        this.executor = exec;
        this.complete(target);
    }

    public synchronized void resolve(){
        if (isResolved || isRejected){
            System.out.println("Execution has completed");
        }
        this.isRunning = false;
        this.isResolved = true;
        this.executor.submit(newTarget);
    }

    public synchronized void reject(){
        if (isResolved || isRejected){
            System.out.println("Execution has completed");
        }
        if (this.exception == null){
            this.exception = new Exception("No error message");
        }
        this.isRunning = false;
        this.isRejected = true;
        try{
            throw(this.exception);
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void complete(String target){
        if (target.charAt(0) == '4'){
            this.exception = new Exception("Client error");
            this.reject();
        }
        else if (target.charAt(0) == '5'){
            this.exception = new Exception("Server error");
            this.reject();
        }
        else if (target.charAt(0) == '2'){
            Future<?> f = this.executor.submit(newTarget);
            try{
                f.get();
            } catch (Exception e){
                System.out.println(e);
            }
        }
        this.runCallbacks();
    }

    public Promise onResolved(Runnable resolved){
        this.newTarget = resolved;
        return this;
    }

    public Promise onRejected(Exception e){
        this.exception = e;
        return this;
    }

    private void runCallbacks(){
        for (Runnable r: this.callbacks){
            Future<?> f = this.executor.submit(r);
            try{
                f.get();
            } catch (Exception e){
                System.out.println(e);
            }
        }
    }

    public Promise then(Runnable r){
        this.callbacks.add(r);
        return this;
    }

}

class PromiseService {
    public static ThreadPoolExecutor exec = new ThreadPoolExecutor(3, 5, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    public static void main(String args[]){
        Runnable task = (() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignore) {

            } finally {
                System.out.println("Task done");

            }
        });
        Runnable task1 = (() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {

            } finally {
                System.out.println("Callback 1");

            }
        });
        Runnable task2 = (() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {

            } finally {
                System.out.println("Callback 2");

            }
        });
        Promise promise = new Promise(exec);
        promise.onResolved(task).onRejected(null).then(task1).then(task2);
        promise.complete("400");

        exec.shutdown();
        try {
            // Wait for the executor to terminate
            if (!exec.awaitTermination(60, TimeUnit.SECONDS)) {
                exec.shutdownNow(); // Force shutdown if tasks are not completed within the timeout
            }
        } catch (InterruptedException e) {
            exec.shutdownNow();
        }
    }
}
