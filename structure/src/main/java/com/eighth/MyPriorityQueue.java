package com.eighth;

import java.util.ArrayList;
import java.util.List;

/**
 * 优先队列 大顶堆，针对PickTask
 * 排序规则：
 * 1. abnormalLevel 越大优先级越高
 * 2. 等级相同，createTime 更小（任务创建更早）优先
 */
public class MyPriorityQueue<E extends PickTask> {
    private List<E> heap;

    public MyPriorityQueue() {
        heap = new ArrayList<>();
    }

    private int parent(int idx) {
        return (idx - 1) / 2;
    }

    private int left(int idx) {
        return idx * 2 + 1;
    }

    private int right(int idx) {
        return idx * 2 + 2;
    }

    /**
     * 比较两个task，返回true：a优先级 高于 b
     */
    private boolean isHigher(E a, E b) {
        if (a.getAbnormalLevel() != b.getAbnormalLevel()) {
            return a.getAbnormalLevel() > b.getAbnormalLevel();
        }
        //等级相等，创建时间更早优先
        return a.getCreateTime() < b.getCreateTime();
    }

    /**
     * 入堆
     */
    public boolean offer(E e) {
        if (e == null) {
            return false;
        }
        heap.add(e);
        siftUp(heap.size() - 1);
        return true;
    }

    private void siftUp(int idx) {
        while (idx > 0) {
            int p = parent(idx);
            if (isHigher(heap.get(p), heap.get(idx))) {
                break;
            }
            //交换
            E temp = heap.get(p);
            heap.set(p, heap.get(idx));
            heap.set(idx, temp);
            idx = p;
        }
    }

    /**
     * 弹出堆顶（优先级最高元素）
     */
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        E top = heap.get(0);
        int lastIdx = heap.size() - 1;
        heap.set(0, heap.get(lastIdx));
        heap.remove(lastIdx);
        siftDown(0);
        return top;
    }

    private void siftDown(int idx) {
        int size = heap.size();
        while (true) {
            int l = left(idx);
            int r = right(idx);
            int max = idx;
            if (l < size && isHigher(heap.get(l), heap.get(max))) {
                max = l;
            }
            if (r < size && isHigher(heap.get(r), heap.get(max))) {
                max = r;
            }
            if (max == idx) {
                break;
            }
            E temp = heap.get(idx);
            heap.set(idx, heap.get(max));
            heap.set(max, temp);
            idx = max;
        }
    }

    /**
     * 查看堆顶不弹出
     */
    public E peek() {
        if (isEmpty()) return null;
        return heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    /**
     * 获取全部元素（用于UI展示，不破坏堆结构）
     */
    public List<E> getAll() {
        return new ArrayList<>(heap);
    }

    public void clear() {
        heap.clear();
    }
}
