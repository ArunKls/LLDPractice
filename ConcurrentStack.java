package practice;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

class Node {
    public int val;
    public Node next;
    public Node(int val){
        this.val = val;
        this.next = null;
    }
    public Node(int val, Node next){
        this.val = val;
        this.next = next;
    }
}

class Stack {
    private Node head = null;
    private ReentrantLock lock = new ReentrantLock();
    public void push(int val){
        Node node = new Node(val);
        lock.lock();
        try{
            if (this.head==null)
                this.head = node;
            else {
                node.next = this.head;
                this.head = node;
            }
        } catch (Exception e){
        } finally {
            lock.unlock();
        }
    }
    public int pop(){
        lock.lock();
        int tmp = -1;
        try{
            if (this.head==null)
                return -1;
            tmp = this.head.val;
            this.head = this.head.next;
        } catch(Exception e){
        } finally {
            lock.unlock();
        }
        return tmp;
    }
}

class LockFreeStack extends Stack {
    private final AtomicReference<Node> head = new AtomicReference<>();

    public void push(int val){
        Node node = new Node(val);
        do {
            node.next = head.get();
        } while (!head.compareAndSet(node.next, node));
    }

    public int pop(){
        Node currentHead;
        Node newHead;
        do {
            if (head.get() == null)
                return -1;
            currentHead = head.get();
            newHead = currentHead.next;
        } while(!head.compareAndSet(currentHead, newHead));
        return currentHead.val;
    }
}

class ConcurrentStack {
    public static void main(String[] args) {
        Stack stack = new LockFreeStack();
        stack.push(1);
        stack.push(2);
        System.out.println(stack.pop());
        System.out.println(stack.pop());
    }
}
