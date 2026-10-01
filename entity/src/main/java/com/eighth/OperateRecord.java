package com.eighth;

public class OperateRecord {

    private OperateType operateType; //操作类型 ADD / DELETE / UPDATE
    private Integer parcelId;    //快递ID
    private Parcel oldData;     //操作前数据
    private Parcel newData;     //操作后数据

    public OperateRecord(OperateType operateType, Integer parcelId, Parcel oldData, Parcel newData) {
        this.operateType = operateType;
        this.parcelId = parcelId;
        this.oldData = oldData;
        this.newData = newData;
    }

    public OperateRecord() {

    }

    public OperateType getOperateType() {
        return operateType;
    }

    public void setOperateType(OperateType operateType) {
        this.operateType = operateType;
    }

    public Integer getParcelId() {
        return parcelId;
    }

    public void setParcelId(Integer parcelId) {
        this.parcelId = parcelId;
    }

    public Parcel getOldData() {
        return oldData;
    }

    public void setOldData(Parcel oldData) {
        this.oldData = oldData;
    }

    public Parcel getNewData() {
        return newData;
    }

    public void setNewData(Parcel newData) {
        this.newData = newData;
    }
}
