package practice;
import java.util.Comparator;
import java.util.PriorityQueue;

class Proc{
    public String name;
    private int time_left;
    public Proc(String name, int time){
        this.name = name;
        this.time_left = time;
    }
    public int getTimeLeft(){
        return this.time_left;
    }
    public void setTimeLeft(int interrupt){
        this.time_left -= interrupt;
    }
}

class Scheduler {
    private int interrupt;
    private PriorityQueue<Proc> run_queue = new PriorityQueue<>(Comparator.comparingInt(Proc::getTimeLeft));
    
    public Scheduler(){
        this.interrupt = 2;
    }

    public Scheduler(int interrupt){
        this.interrupt = interrupt;
    }
    
    public void scheduler(){
        int counter = 0;
        boolean cScheduled = false;
        while(!run_queue.isEmpty()){
            Proc next = run_queue.poll();
            if (next.getTimeLeft()-interrupt > 0){
                next.setTimeLeft(interrupt);
                run_queue.add(next);
            }
            System.out.println("Scheduled "+next.name+" at "+counter);
            int run_time = Math.min(interrupt, next.getTimeLeft());
            try{
                Thread.sleep(run_time*1000);
            } catch(InterruptedException e){
                System.out.println(next.name+" failed");
            }
            counter += run_time;
            if (!cScheduled && counter > 5){
                cScheduled = true;
                this.schedule("C", 1);
            }
        }
    }
    public void schedule(String name, int time){
        this.run_queue.add(new Proc(name, time));
    }
}
class PSJFScheduling {
    static Scheduler schedulerObj = new Scheduler();

    public static void main(String args[]){
        schedulerObj.schedule("A", 10);
        schedulerObj.schedule("B", 11);
        schedulerObj.scheduler();
    }
}
