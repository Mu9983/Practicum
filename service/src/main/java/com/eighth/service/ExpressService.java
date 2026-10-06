package com.eighth.service;

import com.eighth.Parcel;
import com.eighth.ShelfSlot;

public interface ExpressService {

    /**
     * 新增快递信息
     * @param receiverName 用户名
     * @param shelf   货架号
     * @param slot 货位号
     */
    void addParcel(String receiverName, int shelf, ShelfSlot slot);

    /**
     * 用户取件，删除快递
     *
     * @param code 取件码
     * @return 被取出的快递
     */
    Parcel takeParcel(String code);

    /**
     * 撤销最近一次操作（入库 / 取件）
     *
     * @return 被回滚的快递对象
     */
    Parcel undo();

    /**
     * 根据取件码查询快递，不删除
     *
     * @param code 取件码
     * @return 找到返回Parcel，找不到返回null
     */
    Parcel searchByCode(String code);

    /**
     * 获取指定货架所有快递，返回数组，用于渲染
     *
     * @param shelfIndex 货架编号
     * @return 快递数组
     */
    Parcel[] getShelfExpress(int shelfIndex);

    /**
     * 获取整个系统内快递总数量
     *
     * @return 总件数
     */
    int getTotalExpressCount();

    /**
     * 查询全部超时快递（结合MyPriorityQueue小顶堆）
     *
     * @return 超时快递数组
     */
    Parcel[] getTimeoutExpress();

    /**
     * 将快递数据保存到本地txt文件
     */
    void saveToFile(String filePath);

    /**
     * 从txt文件加载快递数据
     */
    void loadFromFile(String filePath);

}
