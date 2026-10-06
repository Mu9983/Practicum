package com.eighth;

/**
 * 货架，内部管理多个货位
 */
public class Shelf {

    private int rowCount;       //行数
    private int colCount;       //列数
    private ShelfSlot[][] slots;//二维货位数组

    public static final Integer SHELF_NUM = 10;
    public static final Integer ROW = 5;
    public static final Integer COL = 20;
    public static final Integer SLOTS_PER_SHELF = ROW * COL;

    /**
     * 初始化货架，创建全部空货位
     * @param rows 行数
     * @param cols 列数
     */
    public Shelf(int rows, int cols){
        this.rowCount = rows;
        this.colCount = cols;
        slots = new ShelfSlot[rowCount][colCount];
        //循环初始化每一个货位为未占用
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                ShelfSlot slot = new ShelfSlot();
                slot.setRow(i);
                slot.setCol(j);
                slots[i][j] = slot;
            }
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public void setColCount(int colCount) {
        this.colCount = colCount;
    }

    public ShelfSlot[][] getSlots() {
        return slots;
    }

    public void setSlots(ShelfSlot[][] slots) {
        this.slots = slots;
    }
}
