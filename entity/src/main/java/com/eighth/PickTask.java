package com.eighth;

/**
 * 取件排队任务；用于循环队列、优先队列
 */
public class PickTask {

    private long taskId;        //任务id
    private Parcel parcel;      //关联包裹
    private long createTime;    //任务登记时间戳
    private int abnormalLevel;  //异常等级，0普通，数字越大优先级越高

    public PickTask(long taskId, Parcel parcel, long createTime, int abnormalLevel) {
        this.taskId = taskId;
        this.parcel = parcel;
        this.createTime = createTime;
        this.abnormalLevel = abnormalLevel;
    }

    public PickTask() {
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getAbnormalLevel() {
        return abnormalLevel;
    }

    public void setAbnormalLevel(int abnormalLevel) {
        this.abnormalLevel = abnormalLevel;
    }
}
