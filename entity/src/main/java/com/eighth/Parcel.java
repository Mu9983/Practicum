package com.eighth;

import java.time.LocalDateTime;

/**
 * 包裹实体
 */
public class Parcel {

    private Integer id;         //包裹唯一编号
    private String pickCode;    //取件码：哈希表检索key
    private String receiverName;//收件人
    private LocalDateTime arriveTime;    //到站时间戳
    private ParcelStatus status;//包裹状态 枚举：IN_STOCK 在库 / PICKED 已取件 / ABNORMAL 异常 / EXPIRED 超期
    private int shelf;          //所属货架编号
    private int slot;           //货位编号

    public Parcel(Integer id, String pickCode, String receiverName, LocalDateTime arriveTime, ParcelStatus status, int shelf, int slot) {
        this.id = id;
        this.pickCode = pickCode;
        this.receiverName = receiverName;
        this.arriveTime = arriveTime;
        this.status = status;
        this.shelf = shelf;
        this.slot = slot;
    }

    public Parcel() {

    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPickCode() {
        return pickCode;
    }

    public void setPickCode(String pickCode) {
        this.pickCode = pickCode;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public LocalDateTime getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(LocalDateTime arriveTime) {
        this.arriveTime = arriveTime;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }

    public int getShelf() {
        return shelf;
    }

    public void setShelf(int shelf) {
        this.shelf = shelf;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "Parcel{" +
                "id=" + id +
                ", pickCode='" + pickCode + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", arriveTime=" + arriveTime +
                ", status=" + status +
                ", shelf=" + shelf +
                ", slot=" + slot +
                '}';
    }
}
