package practice;

import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class CacheService<K, V> {
    private HashMap<K, V> map;
    private PriorityQueue<Entry<K, Long>> queue;
    private ReentrantLock lock = new ReentrantLock();
    private Condition fill = lock.newCondition();

    public CacheService() {
        this.map = new HashMap<K, V>();
        this.queue = new PriorityQueue<>(Entry.comparingByValue());
    }

    public void put(K key, V value, long ttl, TimeUnit unit) {
        if (ttl <= 0){
            return;
        }
        lock.lock();
        try {
            this.queue.add(new SimpleEntry<>(key, System.currentTimeMillis() + unit.toMillis(ttl)));
            this.map.put(key, value);
            this.fill.signal();
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        return this.map.getOrDefault(key, null);
    }

    public Runnable cache() {
        return (() -> {
            lock.lock();
            try {
                System.out.println("Cache started");
                while (true){
                    while (this.queue.isEmpty())
                        fill.await();
                    if (System.currentTimeMillis() < this.queue.peek().getValue())
                        fill.await(this.queue.peek().getValue() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    else {
                        K key = this.queue.poll().getKey();
                        // System.out.println(key + " " + this.map.get(key));
                        this.map.remove(key);
                    }
                }
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
                System.out.println("Cache ended");
            }
        });
    }
}

class CacheTTL {
    private static void assertEquals(String s1, String s2) {
        // System.out.println(s1 + " " + s2);
        if (s2.equals(s1)){
            System.out.println("Passed");
        } else{
            System.out.println("Failed");
        }
    }

    private static void assertNull(String s) {
        // System.out.println(s);
        if (s == null){
            System.out.println("Passed");
        } else{
            System.out.println("Failed");
        }
    }

    public static void main(String[] args) {
        CacheService<String, String> cache = new CacheService<>();
        Thread cacheThread = new Thread(cache.cache());
        cacheThread.start();
        try {
            cache.put("key1", "value1", 10, TimeUnit.SECONDS);
            assertEquals("value1", cache.get("key1"));
            cache.put("key2", "value2", 1, TimeUnit.SECONDS);

            // Wait for more than the TTL
            Thread.sleep(1500);

            // Assert that the value is no longer available after expiry
            assertNull(cache.get("value2"));

            cache.put("key3", "value3", 1, TimeUnit.SECONDS);

            // Wait for 500 milliseconds and then overwrite the value
            Thread.sleep(500);
            cache.put("key3", "newValue3", 2, TimeUnit.SECONDS);

            // Assert that the updated value is retrieved before the new TTL expires
            assertEquals("newValue3", cache.get("key3"));

            // Wait for the new TTL to expire
            Thread.sleep(2500);

            // Assert that the value is no longer available after the new TTL
            assertNull(cache.get("key3"));

            cache.put("key4", "value4", 1, TimeUnit.SECONDS);
            cache.put("key5", "value5", 2, TimeUnit.SECONDS);

            // Wait for 1500 milliseconds
            Thread.sleep(1500);

            // Assert that the first entry has expired but the second one is still available
            assertNull(cache.get("key4"));
            assertEquals("value5", cache.get("key5"));

            // Wait for another 1500 milliseconds to let the second entry expire
            Thread.sleep(1500);

            // Assert that the second entry has also expired
            assertNull(cache.get("key5"));

            cache.put("key6", "value6", 0, TimeUnit.SECONDS);

            // Assert that the value expires immediately
            assertNull(cache.get("key6"));

            cache.put("key7", null, 10, TimeUnit.SECONDS);

            // Assert that the value is stored and can be retrieved
            assertNull(cache.get("key7"));

            cacheThread.interrupt();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
