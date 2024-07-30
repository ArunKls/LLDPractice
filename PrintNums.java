package practice;

import java.util.function.IntConsumer;
import java.util.concurrent.*;

class PrintOdd implements Callable<String> {
    public int count;
    public IntConsumer print = a -> System.out.println(a);
    public Printer printer;

    public PrintOdd(Printer p) {
        printer = p;
    }

    @Override
    public String call() throws Exception {
        printer.odd(print);
        return "Odd done";
    }
}

class PrintEven implements Callable<String> {
    public int count;
    public IntConsumer print = a -> System.out.println(a);
    public Printer printer;

    public PrintEven(Printer p) {
        printer = p;
    }

    @Override
    public String call() throws Exception {
        printer.even(print);
        return "Even done";
    }
}

class Printer {
    public final int count;
    public Semaphore sOdd;
    public Semaphore sEven;

    public Printer(int count) {
        this.count = count;
        sOdd = new Semaphore(0);
        sEven = new Semaphore(1);
    }

    public void odd(IntConsumer print) {
        for (int i = 1; i < this.count; i += 2) {
            try {
                sOdd.acquire();
                print.accept(i);
            } catch (Exception ignore) {
                System.out.println(ignore);
            } finally {
                sEven.release();
            }
        }
    }

    public void even(IntConsumer print) {
        for (int i = 0; i <= this.count; i += 2) {
            try {
                sEven.acquire();
                print.accept(i);
            } catch (Exception ignore) {
                System.out.println(ignore);
            } finally {
                sOdd.release();
            }
        }
    }
}
class PrintNums{
    public static void main(String[] args) throws Exception{
        Printer p = new Printer(10);
        PrintOdd t0 = new PrintOdd(p);
        FutureTask<String> f1 = new FutureTask<>(t0);
        Thread tc0 = new Thread(f1);
        PrintEven t1 = new PrintEven(p);
        FutureTask<String> f2 = new FutureTask<>(t1);
        Thread tc1 = new Thread(f2);
        tc0.start();
        tc1.start();
        System.out.println("Result: " + f1.get());
        System.out.println("Result: " + f2.get());
    }
}
