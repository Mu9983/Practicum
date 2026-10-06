package com.eighth.service.impl;

import com.eighth.*;
import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;
import com.eighth.utils.PickCodeUtils;

import java.io.*;
import java.time.LocalDateTime;

public class ExpressServiceImpl implements ExpressService {

    private final MyHashMap<String, Parcel> myHashMap = new MyHashMap<>();
    private final MyQueue<Parcel> myQueue = new MyQueue<>();
    private final MyStack<OperateRecord> myStack = new MyStack<>();
    private final Shelf[] shelves = new Shelf[Shelf.SHELF_NUM];

    public ExpressServiceImpl() {
        for (int i = 0; i < Shelf.SHELF_NUM; i++) {
            shelves[i] = new Shelf(Shelf.ROW, Shelf.COL);
        }
    }

    public MyHashMap<String, Parcel> getMyHashMap() {
        return myHashMap;
    }

    @Override
    public void addParcel(String receiverName, int shelfIndex, ShelfSlot inputSlot) {
        if (inputSlot == null) {
            throw new BusinessException("货位不能为null");
        }
        Integer row = inputSlot.getRow();
        Integer col = inputSlot.getCol();
        if(row == null || col == null){
            throw new BusinessException("货位行列不能为空");
        }
        if (receiverName == null || receiverName.isEmpty()) {
            throw new BusinessException("用户名为空");
        }
        if (shelfIndex < 0 || shelfIndex >= Shelf.SHELF_NUM) {
            throw new BusinessException("货架编号非法");
        }

        ShelfSlot realSlot = shelves[shelfIndex].getSlots()[row][col];

        if (realSlot.isOccupied()) {
            throw new BusinessException("该货位已存在包裹，不能重复存放");
        }

        String pickCode = PickCodeUtils.generateUniquePickCode(code -> myHashMap.get(code) != null);
        Parcel parcel = new Parcel(ParcelId.PARCEL_ID++, pickCode, receiverName,
                LocalDateTime.now(), ParcelStatus.IN_STOCK, shelfIndex, realSlot);

        // 修改【内部真实货位】，不是外部传入的inputSlot
        realSlot.setParcel(parcel);

        myHashMap.put(pickCode, parcel);
        OperateRecord record = new OperateRecord(OperateType.ADD, parcel.getId(), parcel);
        myStack.push(record);
        myQueue.enqueue(parcel);
    }


    @Override
    public Parcel takeParcel(String pickCode) {
        Parcel parcel = myHashMap.get(pickCode);
        if (parcel == null) {
            throw new BusinessException("取件码不存在，无法取件");
        }
        if (parcel.getStatus() != ParcelStatus.IN_STOCK) {
            throw new BusinessException("该包裹已经被取出或未入库");
        }

        // 货架置空
        int shelf = parcel.getShelf();
        ShelfSlot slot = parcel.getSlot();
        shelves[shelf].getSlots()[slot.getRow()][slot.getCol()].setParcel(null);

        // 修改状态
        parcel.setStatus(ParcelStatus.TAKEN);
        myHashMap.remove(pickCode);

        // 压栈用于撤销：REMOVE操作，记录被拿走的包裹
        OperateRecord record = new OperateRecord(OperateType.REMOVE, parcel.getId(), parcel);
        myStack.push(record);
        return parcel;
    }

    @Override
    public Parcel undo() {
        if (myStack.isEmpty()) {
            throw new BusinessException("没有可撤销的操作");
        }
        OperateRecord record = myStack.pop();
        // 新增：防御判空
        if(record == null){
            throw new BusinessException("没有可撤销的操作");
        }
        Parcel parcel = record.getParcel();
        if (parcel == null) {
            return null;
        }

        int shelfIndex = parcel.getShelf();
        int row = parcel.getSlot().getRow();
        int col = parcel.getSlot().getCol();

        if (record.getOperateType() == OperateType.ADD) {
            shelves[shelfIndex].getSlots()[row][col].setParcel(null);
            myHashMap.remove(parcel.getPickCode());
        } else if (record.getOperateType() == OperateType.REMOVE) {
            shelves[shelfIndex].getSlots()[row][col].setParcel(parcel);
            parcel.setStatus(ParcelStatus.IN_STOCK);
            myHashMap.put(parcel.getPickCode(), parcel);
        }
        return parcel;
    }


    @Override
    public Parcel searchByCode(String pickCode) {
        return myHashMap.get(pickCode);
    }

    @Override
    public Parcel[] getShelfExpress(int shelfIndex) {
        if (shelfIndex < 0 || shelfIndex >= Shelf.SHELF_NUM) {
            return new Parcel[0];
        }
        // 收集该货架所有非null包裹
        Parcel[] temp = new Parcel[Shelf.SLOTS_PER_SHELF];
        int count = 0;
        for (int i = 0; i < Shelf.ROW; i++) {
            for (int j = 0; j < Shelf.COL; j++) {
                if (shelves[shelfIndex].getSlots()[i][j].getParcel() != null) {
                    temp[count++] = shelves[shelfIndex].getSlots()[i][j].getParcel();
                }
            }
        }
        Parcel[] res = new Parcel[count];
        System.arraycopy(temp, 0, res, 0, count);
        return res;
    }

    @Override
    public int getTotalExpressCount() {
        int count = 0;
        for (Shelf shelf : shelves) {
            for (int i = 0; i < Shelf.ROW; i++) {
                for (int j = 0; j < Shelf.COL; j++) {
                    if (shelf.getSlots()[i][j].getParcel() != null) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Parcel[] getTimeoutExpress() {
        return new Parcel[0];
    }

    @Override
    public void saveToFile(String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Shelf shelf : shelves) {
                for (int i = 0; i < Shelf.ROW; i++) {
                    for (int j = 0; j < Shelf.COL; j++) {
                        Parcel p = shelf.getSlots()[i][j].getParcel();
                        if (p != null) {
                            // id,pickCode,receiverName,inTime,status,shelf,slot
                            String line = String.format("%d,%s,%s,%s,%s,%d,%d,%d",
                                    p.getId(),
                                    p.getPickCode(),
                                    p.getReceiverName(),
                                    p.getArriveTime().toString(),
                                    p.getStatus().name(),
                                    p.getShelf(),
                                    p.getSlot().getRow(),
                                    p.getSlot().getCol());
                            bw.write(line);
                            bw.newLine();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new BusinessException("保存文件失败");
        }
    }

    @Override
    public void loadFromFile(String filePath) {
        // 清空内存全部数据
        for (Shelf shelf : shelves) {
            for (int i = 0; i < Shelf.ROW; i++) {
                for (int j = 0; j < Shelf.COL; j++) {
                    shelf.getSlots()[i][j].setParcel(null);
                }
            }
        }
        myHashMap.clear();
        myStack.clear();
        myQueue.clear();

        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String pickCode = parts[1];
                String name = parts[2];
                LocalDateTime time = LocalDateTime.parse(parts[3]);
                ParcelStatus status = ParcelStatus.valueOf(parts[4]);
                int shelfNo = Integer.parseInt(parts[5]);
                int row = Integer.parseInt(parts[6]);
                int col = Integer.parseInt(parts[7]);
                ShelfSlot realSlot = shelves[shelfNo].getSlots()[row][col];

                Parcel parcel = new Parcel(id, pickCode, name, time, status, shelfNo, realSlot);
                realSlot.setParcel(parcel);

                myHashMap.put(pickCode, parcel);
                if (id >= ParcelId.PARCEL_ID) {
                    ParcelId.PARCEL_ID = id + 1;
                }
                myQueue.enqueue(parcel);

            }
        } catch (Exception e) {
            throw new BusinessException("读取文件失败");
        }
    }
}
