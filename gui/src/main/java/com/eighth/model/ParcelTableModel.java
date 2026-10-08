package com.eighth.model;

import com.eighth.Parcel;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;

public class ParcelTableModel extends AbstractTableModel {
    private final String[] columns = {"ID","取件码","收件人","到站时间","状态","货架","行","列"};
    private Parcel[] data = new Parcel[0];
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy‑MM‑dd HH:mm");

    public void setData(Parcel[] arr){
        if(arr==null) data = new Parcel[0];
        else data = arr;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Parcel p = data[rowIndex];
        if(p==null) return null;
        return switch (columnIndex){
            case 0 -> p.getId();
            case 1 -> p.getPickCode();
            case 2 -> p.getReceiverName();
            case 3 -> p.getArriveTime().format(fmt);
            case 4 -> p.getStatus().getDescription();
            case 5 -> p.getShelf() + 1; // UI+1
            case 6 -> p.getSlot().getRow() + 1; // UI+1
            case 7 -> p.getSlot().getCol() + 1; // UI+1
            default -> null;
        };
    }
}
