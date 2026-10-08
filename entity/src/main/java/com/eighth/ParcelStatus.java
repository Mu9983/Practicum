package com.eighth;

/**
 * 枚举包裹在库状态
 */
public enum ParcelStatus {
    IN_STOCK("在库"),
    ABNORMAL("异常"),
    TAKEN("出库"),
    EXPIRED("超期");

    private final String description;
    ParcelStatus(String description){
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
