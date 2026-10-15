package com.eighth;

public class OperateRecord {
    private OperateType operateType;
    private Integer parcelId;
    private Parcel parcel;
    private ParcelStatus oldStatus;
    private Integer oldAbnormalLevel;
    private String oldAbnormalReason;

    public OperateRecord(OperateType operateType, Integer parcelId, Parcel parcel,
                         ParcelStatus oldStatus, Integer oldAbnormalLevel, String oldAbnormalReason) {
        this.operateType = operateType;
        this.parcelId = parcelId;
        this.parcel = parcel;
        this.oldStatus = oldStatus;
        this.oldAbnormalLevel = oldAbnormalLevel;
        this.oldAbnormalReason = oldAbnormalReason;
    }

    public OperateRecord(OperateType operateType, Integer parcelId, Parcel parcel, ParcelStatus oldStatus) {
        this(operateType, parcelId, parcel, oldStatus, null, null);
    }

    public OperateRecord(OperateType operateType, Integer parcelId, Parcel parcel) {
        this(operateType, parcelId, parcel, null, null, null);
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

    public ParcelStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(ParcelStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public Integer getOldAbnormalLevel() {
        return oldAbnormalLevel;
    }

    public void setOldAbnormalLevel(Integer oldAbnormalLevel) {
        this.oldAbnormalLevel = oldAbnormalLevel;
    }

    public String getOldAbnormalReason() {
        return oldAbnormalReason;
    }

    public void setOldAbnormalReason(String oldAbnormalReason) {
        this.oldAbnormalReason = oldAbnormalReason;
    }
}

