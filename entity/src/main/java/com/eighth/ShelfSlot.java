package com.eighth;

/**
 * 单个货位
 */
public class ShelfSlot {

    private Integer row;
    private Integer col;
    private boolean occupied;   //货位是否被占用
    private Parcel parcel;      //绑定包裹引用；null代表空位

    public ShelfSlot(boolean occupied, Parcel parcel) {
        this.occupied = occupied;
        this.parcel = parcel;
    }

    public ShelfSlot() {
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
        // 自动同步occupied状态
        this.occupied = (parcel != null);
    }


    @Override
    public String toString() {
        return "{" + "row=" + row + ", col=" + col + ", occupied=" + occupied + ", parcel_id=" + parcel.getId() + '}';
    }

    /**
     * 字符串解析，格式示例："row=1,col=2,occupied=true"
     * 文件读取时使用；注意：此处不解析Parcel对象，只解析货位坐标与占用标记
     *
     * @param s 字符串
     * @return ShelfSlot实例
     */
    public static ShelfSlot parseShelfSlot(String s) {
        ShelfSlot slot = new ShelfSlot();
        if (s == null || s.isBlank()) {
            return slot;
        }
        // 去掉前后大括号，适配toString输出格式
        s = s.trim();
        if (s.startsWith("{") && s.endsWith("}")) {
            s = s.substring(1, s.length() - 1);
        }
        String[] parts = s.split(",");
        for (String part : parts) {
            part = part.trim();
            String[] kv = part.split("=");
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            String value = kv[1].trim();
            switch (key) {
                case "row":
                    if (!"null".equals(value)) {
                        slot.setRow(Integer.parseInt(value));
                    }
                    break;
                case "col":
                    if (!"null".equals(value)) {
                        slot.setCol(Integer.parseInt(value));
                    }
                    break;
                case "occupied":
                    slot.setOccupied(Boolean.parseBoolean(value));
                    break;
            }
        }
        return slot;
    }
}
