package com.eighth;

/**
 * 货架，内部管理多个货位
 */
public class Shelf {

    private int shelfNo;        //货架编号
    private int rowCount;       //行数
    private int colCount;       //列数
    private ShelfSlot[][] slots;//二维货位数组

    /**
     * 初始化货架，创建全部空货位
     * @param shelfNo 货架号
     * @param rows 行数
     * @param cols 列数
     */
    public Shelf(int shelfNo, int rows, int cols){
        this.shelfNo = shelfNo;
        this.rowCount = rows;
        this.colCount = cols;
        slots = new ShelfSlot[rowCount][colCount];
        //循环初始化每一个货位为未占用
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                slots[i][j] = new ShelfSlot();
            }
        }
    }

    public int getShelfNo() {
        return shelfNo;
    }

    public void setShelfNo(int shelfNo) {
        this.shelfNo = shelfNo;
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
