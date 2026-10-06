package com.eighth;

import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;
import com.eighth.service.impl.ExpressServiceImpl;

public class ExpressServiceTest {
    public static void main(String[] args) {
        ExpressService service = new ExpressServiceImpl();
        String testFile = "express_test.txt";

        System.out.println("=====【测试1】ShelfSlot内部状态校验 setParcel自动同步occupied =====");
        ShelfSlot slotTest = new ShelfSlot();
        System.out.println("初始occupied：" + slotTest.isOccupied());
        Parcel tempParcel = new Parcel();
        slotTest.setParcel(tempParcel);
        System.out.println("放入包裹后occupied：" + slotTest.isOccupied());
        slotTest.setParcel(null);
        System.out.println("清空包裹后occupied：" + slotTest.isOccupied());


        System.out.println("\n=====【测试2】新增包裹 addParcel =====");
        try {
            // 构造合法货位
            ShelfSlot slot00 = new ShelfSlot();
            slot00.setRow(0);
            slot00.setCol(0);
            service.addParcel("张三", 0, slot00);

            ShelfSlot slot01 = new ShelfSlot();
            slot01.setRow(0);
            slot01.setCol(1);
            service.addParcel("李四", 0, slot01);

            System.out.println("✅新增两个包裹完成");
            System.out.println("总包裹数 = " + service.getTotalExpressCount());
        } catch (BusinessException e) {
            System.out.println("❌新增异常：" + e.getMessage());
        }


        System.out.println("\n=====【测试3】同一货位重复存放，预期抛出异常 =====");
        try {
            ShelfSlot slotRepeat = new ShelfSlot();
            slotRepeat.setRow(0);
            slotRepeat.setCol(0);
            service.addParcel("王五",0,slotRepeat);
        } catch (BusinessException e) {
            System.out.println("✅捕获预期异常：" + e.getMessage());
        }


        System.out.println("\n=====【测试4】货架查询 getShelfExpress =====");
        Parcel[] shelf0List = service.getShelfExpress(0);
        System.out.println("货架0查到包裹数量：" + shelf0List.length);
        for (Parcel p : shelf0List) {
            if(p != null){
                System.out.println("    > 收件人：" + p.getReceiverName()
                        + " 取件码：" + p.getPickCode());
            }
        }


        System.out.println("\n=====【测试5】取件 takeParcel + 撤销 undo =====");
        try {
            Parcel[] list = service.getShelfExpress(0);
            String pickCode = list[0].getPickCode();
            System.out.println("准备取件，取件码：" + pickCode);

            Parcel took = service.takeParcel(pickCode);
            System.out.println("✅取件成功，收件人：" + took.getReceiverName());
            System.out.println("取件后总包裹数：" + service.getTotalExpressCount());

            //撤销取件
            Parcel undoParcel = service.undo();
            System.out.println("✅撤销完成，恢复包裹：" + undoParcel.getReceiverName());
            System.out.println("撤销后总包裹数：" + service.getTotalExpressCount());
        } catch (BusinessException e) {
            System.out.println("❌取件/撤销异常：" + e.getMessage());
        }


        System.out.println("\n=====【测试6】按取件码查询 searchByCode =====");
        try {
            Parcel[] arr = service.getShelfExpress(0);
            String code = arr[0].getPickCode();
            Parcel find = service.searchByCode(code);
            System.out.println("✅查询成功，收件人：" + find.getReceiverName());
        }catch (Exception e){
            System.out.println("❌查询异常："+e.getMessage());
        }


        System.out.println("\n=====【测试7】保存到文件 saveToFile =====");
        try{
            service.saveToFile(testFile);
            System.out.println("✅文件保存成功");
        }catch (BusinessException e){
            System.out.println("❌保存失败："+e.getMessage());
        }


        System.out.println("\n=====【测试8】新建Service，从文件加载 loadFromFile =====");
        ExpressService newService = new ExpressServiceImpl();
        try{
            newService.loadFromFile(testFile);
            System.out.println("✅加载完成，加载后总包裹数：" + newService.getTotalExpressCount());
            Parcel[] loaded = newService.getShelfExpress(0);
            for(Parcel p : loaded){
                if(p!=null){
                    System.out.println("    >加载得到："+p.getReceiverName()+" "+p.getPickCode());
                }
            }
        }catch (BusinessException e){
            System.out.println("❌读取文件异常："+e.getMessage());
        }


        System.out.println("\n=====【测试9】无操作时执行undo，预期抛异常 =====");
        try {
            //循环undo直到栈空
            while(true){
                service.undo();
            }
        }catch (BusinessException e){
            System.out.println("✅捕获预期异常：" + e.getMessage());
        }


        System.out.println("\n=====【测试10】查询超时包裹 getTimeoutExpress =====");
        Parcel[] timeout = service.getTimeoutExpress();
        System.out.println("当前超时包裹数量：" + timeout.length);


        System.out.println("\n================全部测试结束================");
    }
}
