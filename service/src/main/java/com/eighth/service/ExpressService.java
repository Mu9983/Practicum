package com.eighth.service;

import com.eighth.Parcel;
import com.eighth.ShelfSlot;
import com.eighth.exception.BusinessException;

import java.util.List;

public interface ExpressService {

    /**
     * 新增快递信息
     *
     * @param receiverName 用户名
     * @param shelf        货架号
     * @param slot         货位号
     * @return 新增完成的包裹对象
     */
    Parcel addParcel(String receiverName, int shelf, ShelfSlot slot);

    /**
     * 新增快递信息，允许外部传入已经生成好的唯一取件码
     *
     * @param receiverName 用户名
     * @param shelf        货架号
     * @param slot         货位号
     * @param prePickCode  已经查重完毕的候选取件码
     * @return 新增完成的包裹对象
     */
    Parcel addParcel(String receiverName, int shelf, ShelfSlot slot, String prePickCode);

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

    class LoadResult {
        public int totalLines;        //总读取行数
        public int successLines;       //解析成功行数
        public List<String> errorLogs; //错误日志：行号+原因

        public LoadResult(int totalLines, int successLines, List<String> errorLogs) {
            this.totalLines = totalLines;
            this.successLines = successLines;
            this.errorLogs = errorLogs;
        }
    }

    /**
     * 加载数据文件，单行损坏不会中断整体加载
     * @param filePath 文件路径
     * @return 加载统计结果
     * @throws BusinessException 文件打不开、IO致命错误
     */
    LoadResult loadFromFile(String filePath) throws BusinessException;


    /**
     * 重新初始化全部货架，清空所有业务数据；修改货架配置后调用
     */
    void reinitShelfAll();

    /**
     * 查找系统第一个空闲货位，返回int[3]：[shelfIndex(0下标),row(0下标),col(0下标)]，无空位返回null
     *
     * @return null代表无空闲货位
     */
    int[] findFirstFreeSlot();

    /**
     * 判断取件码是否存在
     */
    boolean containsKey(String code);

    /**
     * 前缀查询取件码，Trie字典树
     *
     * @param prefix 取件码数字前缀
     * @return 匹配的包裹数组
     */
    Parcel[] searchByPrefix(String prefix);

    /**
     * 标记包裹异常（无参，GUI调用）
     */
    void markAbnormal();

    /**
     * 标记包裹异常，传入取件码
     *
     * @param pickCode 取件码
     */
    void markAbnormal(String pickCode, String abnormalReason, Integer abnormalLevel);

    /**
     * 解除异常恢复IN_STOCK（无参，GUI调用）
     */
    void resolveAbnormal();

    /**
     * 解除异常恢复IN_STOCK，传入取件码
     *
     * @param pickCode 取件码
     */
    void resolveAbnormal(String pickCode);

    /**
     * 超期统计结果封装
     */
    class OverdueStatResult {
        public int totalAll;               //全部包裹
        public int totalInStock;           //在库包裹
        public int totalOverdue;           //超期包裹数量
        public List<Parcel> overdueList;   //超期包裹列表
    }

    OverdueStatResult getOverdueStat(long overdueThresholdMs);

    void scanAndMarkOverdue(long overdueThresholdMs);

    /**
     * 包裹转移
     *
     * @param pickCode    包裹取件码
     * @param targetShelf 目标货架【0下标】
     * @param targetRow   目标行【0下标】
     * @param targetCol   目标列【0下标】
     * @throws BusinessException 包裹不存在 / 目标货位已占用 / 参数越界
     */
    void transferParcel(String pickCode, int targetShelf, int targetRow, int targetCol) throws BusinessException;

    /**
     * 根据货架、行、列(0下标)获取货位上的包裹，可能返回null（货位为空）
     *
     * @param shelf 货架0下标
     * @param row   行0下标
     * @param col   列0下标
     * @return 该货位包裹，空则返回null
     */
    Parcel getParcelByPos(int shelf, int row, int col);

}
