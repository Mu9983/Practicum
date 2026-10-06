package com.eighth;

import java.util.Arrays;

/**
 * 自定义顺序栈
 *
 * @param <E> 元素类型
 */
public class MyStack<E> {

    private E[] data;
    private int top;
    private int capacity;

    public MyStack(int capacity) {
        this.capacity = capacity;
        this.data = (E[]) new Object[capacity];
        this.top = 0;
    }

    public MyStack() {
        this(10);
    }

    /**
     * 判空
     *
     * @return 空：true，非空：false
     */
    public boolean isEmpty() {
        return top == 0;
    }

    /**
     * 判满
     *
     * @return 满：true，未满：false
     */
    public boolean isFull() {
        return top == capacity;
    }

    /**
     * 入栈
     *
     * @param element 元素
     */
    public void push(E element) {
        if (top == capacity) {
            resize(this.capacity * 2);
        }
        data[top++] = element;
    }

    /**
     * 弹出栈顶元素
     *
     * @return 元素
     */
    public E pop() {
        if (isEmpty()) {
            throw new RuntimeException("栈为空，无法出栈");
        }
        E ret = data[top - 1];
        data[top - 1] = null;
        top--;

        if (top > 0 && top == capacity / 4) {
            resize(capacity / 2);
        }
        return ret;
    }

    /**
     * 查看栈顶元素
     *
     * @return 元素
     */
    public E peek() {
        if (isEmpty()) {
            throw new RuntimeException("栈为空，无法获取栈顶");
        }
        return data[top - 1];
    }

    /**
     * 栈大小
     *
     * @return 大小
     */
    public int size() {
        return top;
    }

    /**
     * 扩容/缩容
     *
     * @param newCapacity 新栈容量
     */
    public void resize(int newCapacity) {
        E[] newData = (E[]) new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, top);
        data = newData;
        capacity = newCapacity;
    }

    /**
     * 清空栈
     */
    public void clear() {
        top = 0;
        Arrays.fill(data, null);
    }
}
