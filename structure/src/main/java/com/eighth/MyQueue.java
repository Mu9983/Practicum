package com.eighth;

/**
 * 自定义队列
 */
public class MyQueue<E> {

    private E[] data;
    private int front;
    private int rear;
    private int capacity;

    public MyQueue(int capacity) {
        this.capacity = capacity + 1;
        this.front = 0;
        this.rear = 0;
        this.data = (E[]) new Object[capacity];
    }

    public MyQueue() {
        this(16);
    }

    /**
     * 判断是否已满
     *
     * @return 满：true，未满：false
     */
    public boolean isFull() {
        return (rear + 1) % capacity == front;
    }

    /**
     * 判断是否为空
     *
     * @return 空：true，非空：false
     */
    public boolean isEmpty() {
        return front == rear;
    }

    /**
     * 入队
     *
     * @param e 元素
     * @return 成功：true，失败：false
     */
    public boolean enqueue(E e) {
        if (isFull()) {
            return false;
        }
        data[rear] = e;
        rear = (rear + 1) % capacity;
        return true;
    }

    /**
     * 出队
     *
     * @return 出队的元素
     */
    public E dequeue() {
        if (isEmpty()) {
            return null;
        }
        E result = data[front];
        data[front] = null;
        front = (front + 1) % capacity;
        return result;
    }

    /**
     * 查看队首，不出队
     *
     * @return 队首元素
     */
    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return data[front];
    }

    /**
     * 查看队列实际大小
     *
     * @return 大小
     */
    public int size() {
        return (rear - front + capacity) % capacity;
    }

    /**
     * 得到所有元素
     * @return 所有元素数组
     */
    public E[] getAll() {
        int len = size();
        E[] arr = (E[]) new Object[len];
        int index = this.front;
        for (int i = 0; i < len; i++) {
            arr[i] = data[index];
            index = (index + 1) % capacity;
        }
        return arr;
    }

    /**
     * 清除所有元素
     */
    public void clear() {
        front = 0;
        rear = 0;
        for (int i = 0; i < capacity; i++) {
            data[i] = null;
        }
    }

}
