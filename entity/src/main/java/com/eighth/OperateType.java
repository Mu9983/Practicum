package com.eighth;

public enum OperateType {

    ADD("添加"),
    REMOVE("删除"),
    UPDATE("更新");

    private final String description;

    OperateType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
