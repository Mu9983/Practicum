package com.eighth;

import java.util.ArrayList;
import java.util.List;

/**
 * Trie字典树，用于取件码前缀查询，取件码为6位数字字符串"000000"~"999999"
 */
class TrieNode {
    //子节点：0‑9共10个数字
    TrieNode[] children = new TrieNode[10];
    //到达该节点时，存储的包裹集合（同一个前缀下所有包裹）
    List<Parcel> parcelList = new ArrayList<>();
}

public class ParcelTrie {
    private final TrieNode root;

    public ParcelTrie() {
        root = new TrieNode();
    }

    /**
     * 插入一个包裹（取件码作为key）
     */
    public void insert(Parcel parcel) {
        String code = parcel.getPickCode();
        TrieNode curr = root;
        for (char ch : code.toCharArray()) {
            int idx = ch - '0';
            if (curr.children[idx] == null) {
                curr.children[idx] = new TrieNode();
            }
            curr = curr.children[idx];
            //每一层节点都加入该包裹，支持任意前缀查询
            curr.parcelList.add(parcel);
        }
    }

    /**
     * 删除一个包裹
     */
    public void remove(Parcel parcel) {
        String code = parcel.getPickCode();
        TrieNode curr = root;
        for (char ch : code.toCharArray()) {
            int idx = ch - '0';
            if (curr.children[idx] == null) {
                return;
            }
            curr = curr.children[idx];
            curr.parcelList.remove(parcel);
        }
    }

    /**
     * 根据前缀查询所有匹配的包裹
     * @param prefix 前缀字符串，可以1‑6位数字
     * @return 匹配前缀的包裹数组；无匹配返回空数组
     */
    public List<Parcel> searchPrefix(String prefix) {
        List<Parcel> empty = new ArrayList<>();
        if (prefix == null || prefix.isBlank()) {
            return empty;
        }
        TrieNode curr = root;
        for (char ch : prefix.toCharArray()) {
            if (!Character.isDigit(ch)) {
                return empty;
            }
            int idx = ch - '0';
            if (curr.children[idx] == null) {
                return empty;
            }
            curr = curr.children[idx];
        }
        //到达前缀末尾节点，返回该节点下全部包裹
        return new ArrayList<>(curr.parcelList);
    }

    /**
     * 清空整个字典树
     */
    public void clear() {
        for (int i = 0; i < root.children.length; i++) {
            root.children[i] = null;
        }
    }
}
