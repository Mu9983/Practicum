package com.eighth;

public class OperateRecord {

    private OperateType operateType; //操作类型 ADD / DELETE / UPDATE
    private Integer parcelId;    //快递ID
    private Parcel parcel;     //包裹数据

    public OperateRecord(OperateType operateType, Integer parcelId, Parcel parcel) {
        this.operateType = operateType;
        this.parcelId = parcelId;
        this.parcel = parcel;
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

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }
}
