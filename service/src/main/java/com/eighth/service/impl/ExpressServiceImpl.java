package com.eighth.service.impl;

import com.eighth.*;
import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;
import com.eighth.utils.PickCodeUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExpressServiceImpl implements ExpressService {
    private final MyHashMap<String, Parcel> myHashMap = new MyHashMap<>();
    private final MyQueue<Parcel> myQueue = new MyQueue<>();
    private final MyStack<OperateRecord> myStack = new MyStack<>();
    private Shelf[] shelves = new Shelf[Shelf.SHELF_NUM];
    private final ParcelTrie trie = new ParcelTrie();

    private int lastShelfIdx = 0;
    private int lastRowIdx = 0;
    private int lastColIdx = 0;

    public ExpressServiceImpl() {
        for (int i = 0; i < Shelf.SHELF_NUM; i++) {
            shelves[i] = new Shelf(Shelf.ROW, Shelf.COL);
        }
    }

    public void reinitShelfAll(){
        myHashMap.clear();
        myStack.clear();
        myQueue.clear();
        trie.clear(); //清空trie
        shelves = new Shelf[Shelf.SHELF_NUM];
        for (int i = 0; i < Shelf.SHELF_NUM; i++) {
            shelves[i] = new Shelf(Shelf.ROW, Shelf.COL);
        }
        ParcelId.PARCEL_ID = 0;
        lastShelfIdx = 0;
        lastRowIdx = 0;
        lastColIdx = 0;
    }

    @Override
    public int[] findFirstFreeSlot() {
        boolean startFlag = false;
        for (int s = 0; s < Shelf.SHELF_NUM; s++) {
            Shelf shelf = shelves[s];
            for (int r = 0; r < Shelf.ROW; r++) {
                for (int c = 0; c < Shelf.COL; c++) {
                    if (!startFlag) {
                        if (s == lastShelfIdx && r == lastRowIdx && c == lastColIdx) {
                            startFlag = true;
                        } else {
                            continue;
                        }
                    }
                    ShelfSlot slot = shelf.getSlots()[r][c];
                    if (!slot.isOccupied()) {
                        // 找到空位，更新游标为此位置，下次从【下一个】开始
                        lastShelfIdx = s;
                        lastRowIdx = r;
                        lastColIdx = c;
                        return new int[]{s, r, c};
                    }
                }
            }
        }

        for (int s = 0; s < Shelf.SHELF_NUM; s++) {
            Shelf shelf = shelves[s];
            for (int r = 0; r < Shelf.ROW; r++) {
                for (int c = 0; c < Shelf.COL; c++) {
                    ShelfSlot slot = shelf.getSlots()[r][c];
                    if (!slot.isOccupied()) {
                        lastShelfIdx = s;
                        lastRowIdx = r;
                        lastColIdx = c;
                        return new int[]{s, r, c};
                    }
                }
            }
        }
        // 全部货位已满
        return null;
    }

    @Override
    public Parcel addParcel(String receiverName, int shelfIndex, ShelfSlot inputSlot) {
        if (inputSlot == null) {
            throw new BusinessException("货位不能为null");
        }
        Integer row = inputSlot.getRow();
        Integer col = inputSlot.getCol();
        if (row == null || col == null) {
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
        parcel.setStoreTime(System.currentTimeMillis());
        realSlot.setParcel(parcel);
        myHashMap.put(pickCode, parcel);
        trie.insert(parcel);
        OperateRecord record = new OperateRecord(OperateType.ADD, parcel.getId(), parcel);
        myStack.push(record);
        myQueue.enqueue(parcel);
        return parcel;
    }

    @Override
    public Parcel addParcel(String receiverName, int shelfIndex, ShelfSlot inputSlot, String prePickCode) {
        if (inputSlot == null) {
            throw new BusinessException("货位不能为null");
        }
        Integer row = inputSlot.getRow();
        Integer col = inputSlot.getCol();
        if (row == null || col == null) {
            throw new BusinessException("货位行列不能为空");
        }
        if (receiverName == null || receiverName.isEmpty()) {
            throw new BusinessException("用户名为空");
        }
        if (shelfIndex < 0 || shelfIndex >= Shelf.SHELF_NUM) {
            throw new BusinessException("货架编号非法");
        }
        if (prePickCode == null || prePickCode.isBlank()) {
            throw new BusinessException("预生成取件码不能为空");
        }
        if (myHashMap.containsKey(prePickCode)) {
            throw new BusinessException("取件码已经存在，请重试");
        }
        ShelfSlot realSlot = shelves[shelfIndex].getSlots()[row][col];
        if (realSlot.isOccupied()) {
            throw new BusinessException("该货位已存在包裹，不能重复存放");
        }
        Parcel parcel = new Parcel(ParcelId.PARCEL_ID++, prePickCode, receiverName,
                LocalDateTime.now(), ParcelStatus.IN_STOCK, shelfIndex, realSlot);
        realSlot.setParcel(parcel);
        myHashMap.put(prePickCode, parcel);
        trie.insert(parcel); //插入Trie
        OperateRecord record = new OperateRecord(OperateType.ADD, parcel.getId(), parcel);
        myStack.push(record);
        myQueue.enqueue(parcel);
        return parcel;
    }

    @Override
    public Parcel takeParcel(String pickCode) {
        Parcel parcel = myHashMap.get(pickCode);
        if (parcel == null) {
            throw new BusinessException("取件码不存在，无法取件");
        }
        if (parcel.getStatus() == ParcelStatus.TAKEN || parcel.getStatus() == ParcelStatus.ABNORMAL) {
            throw new BusinessException("该包裹已经被取出或是异常件，无法取件");
        }
        int shelf = parcel.getShelf();
        ShelfSlot slot = parcel.getSlot();
        shelves[shelf].getSlots()[slot.getRow()][slot.getCol()].setParcel(null);
        parcel.setStatus(ParcelStatus.TAKEN);
        myHashMap.remove(pickCode);
        trie.remove(parcel);
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
        if(record == null){
            throw new BusinessException("没有可撤销的操作");
        }
        Parcel parcel = record.getParcel();
        if (parcel == null) {
            return null;
        }

        if (record.getOperateType() == OperateType.ADD) {
            //原有ADD逻辑不变
            int shelfIndex = parcel.getShelf();
            int row = parcel.getSlot().getRow();
            int col = parcel.getSlot().getCol();
            shelves[shelfIndex].getSlots()[row][col].setParcel(null);
            myHashMap.remove(parcel.getPickCode());
            trie.remove(parcel);
        } else if (record.getOperateType() == OperateType.REMOVE) {
            //原有REMOVE逻辑不变
            int shelfIndex = parcel.getShelf();
            int row = parcel.getSlot().getRow();
            int col = parcel.getSlot().getCol();
            shelves[shelfIndex].getSlots()[row][col].setParcel(parcel);
            parcel.setStatus(ParcelStatus.IN_STOCK);
            myHashMap.put(parcel.getPickCode(), parcel);
            trie.insert(parcel);
        } else if(record.getOperateType() == OperateType.UPDATE){
            //回滚状态 + 异常等级 + 异常原因
            if(record.getOldStatus() != null){
                parcel.setStatus(record.getOldStatus());
            }
            parcel.setAbnormalLevel(record.getOldAbnormalLevel());
            parcel.setAbnormalReason(record.getOldAbnormalReason());
        }
        return parcel;
    }


    @Override
    public Parcel searchByCode(String pickCode) {
        return myHashMap.get(pickCode);
    }

    @Override
    public boolean containsKey(String code) {
        return myHashMap.containsKey(code);
    }

    @Override
    public Parcel[] getShelfExpress(int shelfIndex) {
        if (shelfIndex < 0 || shelfIndex >= Shelf.SHELF_NUM) {
            return new Parcel[0];
        }
        Parcel[] temp = new Parcel[Shelf.SLOTS_PER_SHELF()];
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
    public void saveToFile(String filePath) throws BusinessException {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            for (Parcel parcel : myHashMap.values()) {
                Integer level = parcel.getAbnormalLevel();
                String reason = parcel.getAbnormalReason();

                String levelStr = (level == null) ? "" : level.toString();
                String reasonStr = (reason == null) ? "" : reason;
                String storeTimeStr = (parcel.getStoreTime() == null) ? "" : parcel.getStoreTime().toString();

                String line = String.format("%d,%s,%s,%s,%s,%s,%d,%d,%d,%s,%s,%s",
                        parcel.getId(),
                        parcel.getPickCode(),
                        parcel.getReceiverName(),
                        parcel.getArriveTime(),
                        parcel.getArriveTime(),
                        parcel.getStatus().name(),
                        parcel.getShelf(),
                        parcel.getSlot().getRow(),
                        parcel.getSlot().getCol(),
                        levelStr,
                        reasonStr,
                        storeTimeStr
                );
                pw.println(line);
            }
        } catch (java.io.IOException e) {
            throw new BusinessException("保存文件IO异常：" + e.getMessage());
        }
    }

    @Override
    public LoadResult loadFromFile(String filePath) throws BusinessException {
        //加载前清空全部旧数据
        myHashMap.clear();
        trie.clear();
        for (int s = 0; s < Shelf.SHELF_NUM; s++) {
            for (int r = 0; r < Shelf.ROW; r++) {
                for (int c = 0; c < Shelf.COL; c++) {
                    shelves[s].getSlots()[r][c].setParcel(null);
                }
            }
        }

        List<String> errorLogs = new ArrayList<>();
        int totalLines = 0;
        int successLines = 0;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                totalLines++;
                line = line.trim();
                if (line.isBlank()) {
                    continue; //跳过空行，不计错误
                }
                try {
                    //=====【这一整块是原来单行解析逻辑，全部包在这个内层try】=====
                    String[] parts = line.split(",", -1);
                    if (parts.length < 10) {
                        throw new RuntimeException("字段数量不足，至少需要10个字段");
                    }

                    int id = Integer.parseInt(parts[0]);
                    String pickCode = parts[1];
                    String receiver = parts[2];
                    LocalDateTime arriveTime = LocalDateTime.parse(parts[3]);
                    ParcelStatus status = ParcelStatus.valueOf(parts[4]);

                    int shelfIdx = Integer.parseInt(parts[5]);
                    int rowIdx = Integer.parseInt(parts[6]);
                    int colIdx = Integer.parseInt(parts[7]);

                    String levelText = parts[8].trim();
                    String reasonText = parts[9];
                    String storeTimeText = parts[10].trim();

                    Integer abnormalLevel = null;
                    if (!levelText.isBlank()) {
                        abnormalLevel = Integer.parseInt(levelText);
                    }
                    String abnormalReason = null;
                    if (!reasonText.isBlank()) {
                        abnormalReason = reasonText;
                    }

                    Long storeTime = null;
                    if (!storeTimeText.isBlank()) {
                        storeTime = Long.parseLong(storeTimeText);
                    }

                    //校验货架行列是否在当前系统配置范围内，超出直接标记该行错误
                    if (shelfIdx < 0 || shelfIdx >= Shelf.SHELF_NUM
                            || rowIdx < 0 || rowIdx >= Shelf.ROW
                            || colIdx < 0 || colIdx >= Shelf.COL) {
                        throw new RuntimeException("货架/行/列超出当前系统配置范围");
                    }

                    ShelfSlot slot = new ShelfSlot();
                    slot.setRow(rowIdx);
                    slot.setCol(colIdx);

                    Parcel parcel = new Parcel();
                    parcel.setId(id);
                    parcel.setPickCode(pickCode);
                    parcel.setReceiverName(receiver);
                    parcel.setArriveTime(arriveTime);
                    parcel.setStatus(status);
                    parcel.setShelf(shelfIdx);
                    parcel.setSlot(slot);
                    parcel.setAbnormalLevel(abnormalLevel);
                    parcel.setAbnormalReason(abnormalReason);
                    parcel.setStoreTime(storeTime);

                    //校验：该货位不能重复存在包裹（文件重复同坐标）
                    if(shelves[shelfIdx].getSlots()[rowIdx][colIdx].getParcel() != null){
                        throw new RuntimeException("该货位已经存在包裹，数据重复冲突");
                    }

                    shelves[shelfIdx].getSlots()[rowIdx][colIdx].setParcel(parcel);
                    myHashMap.put(pickCode, parcel);
                    trie.insert(parcel);

                    successLines++;
                } catch (Exception e) {
                    //捕获单行所有解析异常，记录日志，不向外抛出
                    String errMsg = String.format("第%d行解析失败：%s，原始行：%s",
                            lineNumber, e.getMessage(), line);
                    errorLogs.add(errMsg);
                }
            }
        } catch (java.io.IOException e) {
            //文件打不开，属于致命异常，向上抛出，整个加载失败
            throw new BusinessException("读取文件IO异常：" + e.getMessage());
        }
        return new LoadResult(totalLines, successLines, errorLogs);
    }


    @Override
    public Parcel[] searchByPrefix(String prefix) {
        var list = trie.searchPrefix(prefix);
        return list.toArray(new Parcel[0]);
    }

    @Override
    public void markAbnormal() {
        //无参接口，GUI不使用本实现，GUI传pickCode调用下面重载
        throw new BusinessException("请传入取件码");
    }

    @Override
    public void markAbnormal(String pickCode, String abnormalReason, Integer abnormalLevel) {
        Parcel parcel = myHashMap.get(pickCode);
        if(parcel == null){
            throw new BusinessException("取件码不存在");
        }
        if(parcel.getStatus() == ParcelStatus.TAKEN){
            throw new BusinessException("已出库包裹不能标记异常");
        }
        if(parcel.getStatus() == ParcelStatus.ABNORMAL){
            throw new BusinessException("该包裹已经是异常件");
        }
        //保存旧状态、旧异常信息，用于undo回滚
        ParcelStatus oldStatus = parcel.getStatus();
        Integer oldLv = parcel.getAbnormalLevel();
        String oldReason = parcel.getAbnormalReason();

        //更新包裹
        parcel.setStatus(ParcelStatus.ABNORMAL);
        parcel.setAbnormalLevel(abnormalLevel);
        parcel.setAbnormalReason(abnormalReason);

        //存入操作记录，保存全部旧信息
        OperateRecord record = new OperateRecord(OperateType.UPDATE, parcel.getId(), parcel,
                oldStatus, oldLv, oldReason);
        myStack.push(record);
    }

    @Override
    public void resolveAbnormal() {
        throw new BusinessException("请传入取件码");
    }

    @Override
    public void resolveAbnormal(String pickCode) {
        Parcel parcel = myHashMap.get(pickCode);
        if(parcel == null){
            throw new BusinessException("取件码不存在");
        }
        if(parcel.getStatus() == ParcelStatus.TAKEN){
            throw new BusinessException("已出库包裹不能解除异常");
        }
        if(parcel.getStatus() != ParcelStatus.ABNORMAL){
            throw new BusinessException("该包裹不是异常件，无需解除");
        }
        //保存旧数据用于undo
        ParcelStatus oldStatus = parcel.getStatus();
        Integer oldLv = parcel.getAbnormalLevel();
        String oldReason = parcel.getAbnormalReason();

        parcel.setStatus(ParcelStatus.IN_STOCK);
        //解除异常：清空异常等级和原因
        parcel.setAbnormalLevel(null);
        parcel.setAbnormalReason(null);

        OperateRecord record = new OperateRecord(OperateType.UPDATE, parcel.getId(), parcel,
                oldStatus, oldLv, oldReason);
        myStack.push(record);
    }
    @Override
    public OverdueStatResult getOverdueStat(long overdueThresholdMs) {
        OverdueStatResult res = new OverdueStatResult();
        Collection<Parcel> allParcelList = myHashMap.values();

        res.totalAll = allParcelList.size();
        res.overdueList = new ArrayList<>();

        int inStockCount = 0;
        for(Parcel p : allParcelList){
            if(p.getStatus() == ParcelStatus.TAKEN){
                continue;
            }
            inStockCount++;
            long duration = p.getStoreDurationMs();
            if(duration > overdueThresholdMs){
                res.overdueList.add(p);
            }
        }
        res.totalInStock = inStockCount;
        res.totalOverdue = res.overdueList.size();
        return res;
    }

    @Override
    public void scanAndMarkOverdue(long overdueThresholdMs) {
        Collection<Parcel> allParcelList = myHashMap.values();
        for(Parcel p : allParcelList){
            if(p.getStatus() == ParcelStatus.IN_STOCK){
                long dur = p.getStoreDurationMs();
                if(dur > overdueThresholdMs){
                    p.setStatus(ParcelStatus.EXPIRED);
                }
            }
        }
    }

    @Override
    public void transferParcel(String pickCode, int targetShelf, int targetRow, int targetCol) throws BusinessException {
        //1.取出包裹
        Parcel parcel = myHashMap.get(pickCode);
        if(parcel == null){
            throw new BusinessException("找不到该取件码对应的包裹");
        }
        //校验目标坐标合法性
        if(targetShelf <0 || targetShelf >= Shelf.SHELF_NUM
                || targetRow <0 || targetRow >= Shelf.ROW
                || targetCol <0 || targetCol >= Shelf.COL){
            throw new BusinessException("目标货架/行/列超出系统配置范围");
        }
        //校验目标货位是否被占用
        if(shelves[targetShelf].getSlots()[targetRow][targetCol].getParcel() != null){
            throw new BusinessException("目标货位已经存在包裹，无法转移");
        }

        //获取旧位置（0下标）
        int oldShelf = parcel.getShelf();
        int oldRow = parcel.getSlot().getRow();
        int oldCol = parcel.getSlot().getCol();

        //校验：源货位上确实是这个包裹（防止数据不一致）
        Parcel srcParcel = shelves[oldShelf].getSlots()[oldRow][oldCol].getParcel();
        if(srcParcel == null || !srcParcel.getPickCode().equals(pickCode)){
            throw new BusinessException("源货位与包裹信息不匹配，禁止转移");
        }

        // ========= 清空旧货位 =========
        shelves[oldShelf].getSlots()[oldRow][oldCol].setParcel(null);

        // ========= 修改包裹对象内部位置信息 =========
        parcel.setShelf(targetShelf);
        ShelfSlot newSlot = new ShelfSlot();
        newSlot.setRow(targetRow);
        newSlot.setCol(targetCol);
        parcel.setSlot(newSlot);

        // ========= 放入新货位 =========
        shelves[targetShelf].getSlots()[targetRow][targetCol].setParcel(parcel);
    }

    @Override
    public Parcel getParcelByPos(int shelf, int row, int col) {
        return shelves[shelf].getSlots()[row][col].getParcel();
    }

}
