package com.eighth.service.impl;

import com.eighth.*;
import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;

import java.time.LocalDateTime;

public class ExpressServiceImpl implements ExpressService {

    private final MyHashMap<String, Parcel> myHashMap = new MyHashMap<>();
    private final MyQueue<Parcel> myQueue = new MyQueue<>();
    private final MyStack<OperateRecord> myStack = new MyStack<>();

    public MyHashMap<String, Parcel> getMyHashMap() {
        return myHashMap;
    }

    @Override
    public void addParcel(String pickCode, String receiverName, int shelf, int slot) {
        if (pickCode.isEmpty() || receiverName.isEmpty()) {
            throw new BusinessException("包裹信息不存在");
        }
        Parcel parcel = new Parcel(ParcelId.PARCEL_ID + 1, pickCode, receiverName,
                LocalDateTime.now(), ParcelStatus.IN_STOCK, shelf, slot);
        myHashMap.put(pickCode, parcel);
        myQueue.enqueue(parcel);
        OperateRecord record = new OperateRecord(OperateType.ADD, parcel.getId(), null, parcel);
        myStack.push(record);

    }

    @Override
    public Parcel takeParcel(String code) {
        return null;
    }

    @Override
    public Parcel undo() {
        return null;
    }

    @Override
    public Parcel searchByCode(String code) {
        return null;
    }

    @Override
    public Parcel[] getShelfExpress(int shelfIndex) {
        return new Parcel[0];
    }

    @Override
    public int getTotalExpressCount() {
        return 0;
    }

    @Override
    public Parcel[] getTimeoutExpress() {
        return new Parcel[0];
    }

    @Override
    public Parcel[] sortByCode() {
        return new Parcel[0];
    }

    @Override
    public void saveToFile(String filePath) {

    }

    @Override
    public void loadFromFile(String filePath) {

    }
}
