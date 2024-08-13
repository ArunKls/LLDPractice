package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class FaultTolerantService {

    // Method to execute a task with retries
    public <T> T executeWithRetry(Callable<T> task, int maxRetries) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                // Attempt to execute the task
                return task.call();
            } catch (Exception e) {
                // If the maximum number of retries is reached, rethrow the exception
                if (attempt >= maxRetries) {
                    throw e;
                }
                // Exponential backoff calculation
                long backoffTime = (long) Math.pow(2, attempt) * 1000;
                System.out.println("Retry attempt " + (attempt + 1) + " failed. Retrying in " + backoffTime + " ms.");
                attempt++;
                try {
                    // Wait for the backoff period before retrying
                    TimeUnit.MILLISECONDS.sleep(backoffTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry sleep interrupted", ie);
                }
            }
        }
    }

    public static void main(String[] args) {
        FaultTolerantService service = new FaultTolerantService();

        Callable<String> task = () -> {
            // Simulate a task that may fail
            if (Math.random() < 0.7) {
                throw new RuntimeException("Task failed");
            }
            return "Task succeeded";
        };

        try {
            String result = service.executeWithRetry(task, 5);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Task failed after retries: " + e.getMessage());
        }
    }
}
