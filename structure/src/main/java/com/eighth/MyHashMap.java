package com.eighth;

/**
 * 自定义HashMap类
 *
 * @param <K> key
 * @param <V> value
 */
public class MyHashMap<K, V> {

    /**
     * 链表节点
     */
    private static class Node<K, V> {
        private K key;
        private V value;
        private Node<K, V> next;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V>[] table;
    private int size;
    private int capacity;
    private final double loadFactor = 0.75;

    /**
     * 初始化
     */
    public MyHashMap() {
        capacity = 16;
        table = (Node<K, V>[]) new Node[capacity];
        size = 0;
    }

    /**
     * 获取hash下标
     */
    private int hash(K key) {
        if (key == null) {
            return 0;
        }
        return Math.abs(key.hashCode()) % capacity;
    }

    /**
     * 扩容：数组扩大两倍，重新hash迁移全部节点
     */
    private void resize() {
        int oldCap = capacity;
        capacity = capacity * 2;
        Node<K, V>[] oldTable = table;
        table = (Node<K, V>[]) new Node[capacity];
        size = 0;

        //遍历旧哈希表，迁移每一个节点
        for (int i = 0; i < oldCap; i++) {
            Node<K, V> p = oldTable[i];
            while (p != null) {
                put(p.key, p.value);
                p = p.next;
            }
        }
    }

    /**
     * 添加/覆盖键值对
     */
    public void put(K key, V value) {
        if (size >= capacity * loadFactor) {
            resize();
        }
        int index = hash(key);
        Node<K, V> node = table[index];
        while (node != null) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
            node = node.next;
        }
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = table[index];
        table[index] = newNode;
        size++;
    }

    /**
     * 根据key获取value，没有则返回空
     */
    public V get(K key) {
        int index = hash(key);
        Node<K, V> node = table[index];
        while (node != null) {
            if (node.key.equals(key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }

    /**
     * 删除key的节点
     */
    public V remove(K key) {
        int index = hash(key);
        Node<K, V> head = table[index];
        if (head == null) {
            return null;
        }
        if (head.key.equals(key)) {
            V value = head.value;
            table[index] = head.next;
            size--;
            return value;
        }
        Node<K, V> prev = head;
        Node<K, V> curr = prev.next;
        while (curr != null) {
            if (curr.key.equals(key)) {
                V  value = curr.value;
                prev.next = curr.next;
                size--;
                return value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    /**
     * 是否包含key的节点
     * @return 是：true，否：false
     */
    public boolean containsKey(K key){
        return get(key) != null;
    }

    /**
     * 返回实际大小
     */
    public int size(){
        return size;
    }

    /**
     * 清除所有元素
     */
    public void clear(){
        capacity = 16;
        table = (Node<K,V>[]) new Node[capacity];
        size = 0;
    }
}
