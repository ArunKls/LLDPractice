package practice;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class LRUCache {
    class Node {
        int key;
        int val;
        Node prev;
        Node next;

        Node(int key, int val) {
            this.key = key;
            this.val = val;
        }
    }

    private final Node head = new Node(-1, -1);
    private final Node tail = new Node(-1, -1);
    private final int cap;
    private final HashMap<Integer, Node> map = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public LRUCache(int capacity) {
        cap = capacity;
        head.next = tail;
        tail.prev = head;
    }

    private void addNode(Node newNode) {
        Node temp = head.next;
        newNode.next = temp;
        newNode.prev = head;
        head.next = newNode;
        temp.prev = newNode;
    }

    private void deleteNode(Node delNode) {
        Node prevNode = delNode.prev;
        Node nextNode = delNode.next;
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }

    public int get(int key) {
        lock.lock();
        try {
            if (map.containsKey(key)) {
                Node resNode = map.get(key);
                int ans = resNode.val;

                map.remove(key);
                deleteNode(resNode);
                addNode(resNode);
                map.put(key, head.next);
                return ans;
            }
            return -1;
        } finally {
            lock.unlock();
        }
    }

    public void put(int key, int value) {
        lock.lock();
        try {
            if (map.containsKey(key)) {
                Node curr = map.get(key);
                map.remove(key);
                deleteNode(curr);
            }

            if (map.size() == cap) {
                map.remove(tail.prev.key);
                deleteNode(tail.prev);
            }

            Node newNode = new Node(key, value);
            addNode(newNode);
            map.put(key, head.next);
        } finally {
            lock.unlock();
        }
    }
}
