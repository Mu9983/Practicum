package com.eighth.dialog;
import com.eighth.Parcel;
import com.eighth.Shelf;
import com.eighth.ShelfSlot;
import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;
import com.eighth.utils.PickCodeUtils;

import javax.swing.*;
import java.awt.*;

/**
 * 新增包裹入库弹窗：UI界面全部从1开始，业务层转换回0下标
 */
public class AddParcelDialog extends JDialog {
    private final ExpressService service;
    private final Runnable callback;
    // 菜单按钮模式预生成候选取件码，双击模式为null
    private String candidatePickCode = null;

    // =========【构造：菜单/底部按钮调用：自动查找第一个空闲货位】=========
    public AddParcelDialog(JFrame owner, ExpressService service, Runnable callback) {
        super(owner,"新增包裹入库",true);
        this.service = service;
        this.callback = callback;

        int[] freePos = service.findFirstFreeSlot();
        if(freePos == null){
            JOptionPane.showMessageDialog(owner,"暂无空闲货位，无法新增包裹！","提示",JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        int uiShelf = freePos[0] + 1;
        int uiRow = freePos[1] + 1;
        int uiCol = freePos[2] + 1;

        try {
            candidatePickCode = PickCodeUtils.generateUniquePickCode(code -> service.containsKey(code));
        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(owner, e.getMessage(),"错误",JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initUi(uiShelf, uiRow, uiCol);
        setSize(360,240);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    // =========【原有重载构造：双击空货位调用，预填传入的UI坐标，完全保留不变】=========
    public AddParcelDialog(JFrame owner, ExpressService service, int initUiShelf, int initUiRow, int initUiCol, Runnable callback){
        super(owner,"新增包裹入库",true);
        this.service = service;
        this.callback = callback;
        this.candidatePickCode = null;
        initUi(initUiShelf, initUiRow, initUiCol);
        setSize(360,240);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void initUi(int initUiShelf, int initUiRow, int initUiCol){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4,2,6,6));
        JTextField receiverField = new JTextField();

        JSpinner shelfSpinner = new JSpinner(new SpinnerNumberModel(initUiShelf,1, Shelf.SHELF_NUM,1));
        JSpinner rowSpinner = new JSpinner(new SpinnerNumberModel(initUiRow,1, Shelf.ROW,1));
        JSpinner colSpinner = new JSpinner(new SpinnerNumberModel(initUiCol,1, Shelf.COL,1));

        panel.add(new JLabel("收件人姓名："));
        panel.add(receiverField);
        panel.add(new JLabel("货架编号(1‑n)"));
        panel.add(shelfSpinner);
        panel.add(new JLabel("行号(1‑n)"));
        panel.add(rowSpinner);
        panel.add(new JLabel("列号(1‑n)"));
        panel.add(colSpinner);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确认入库");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel,BorderLayout.CENTER);
        getContentPane().add(btnPanel,BorderLayout.SOUTH);

        cancelBtn.addActionListener(e->dispose());

        okBtn.addActionListener(e->{
            String name = receiverField.getText().trim();
            int shelfUi = (Integer)shelfSpinner.getValue();
            int rowUi = (Integer)rowSpinner.getValue();
            int colUi = (Integer)colSpinner.getValue();

            if(name.isBlank()){
                JOptionPane.showMessageDialog(this,"收件人不能为空");
                return;
            }
            try{
                int shelf = shelfUi - 1;
                int row = rowUi -1;
                int col = colUi -1;
                ShelfSlot slot = new ShelfSlot();
                slot.setRow(row);
                slot.setCol(col);

                //执行入库，拿到返回的包裹对象
                Parcel parcel;
                if(candidatePickCode != null){
                    parcel = service.addParcel(name,shelf,slot,candidatePickCode);
                }else{
                    parcel = service.addParcel(name,shelf,slot);
                }
                //关闭新增弹窗
                dispose();

                //=====入库成功，弹出信息弹窗=====
                String info = String.format(
                        "✅包裹入库成功\n" +
                                "货架号：%d\n" +
                                "行号：%d\n" +
                                "列号：%d\n" +
                                "收件人：%s\n" +
                                "取件码：%s",
                        parcel.getShelf()+1,
                        parcel.getSlot().getRow()+1,
                        parcel.getSlot().getCol()+1,
                        parcel.getReceiverName(),
                        parcel.getPickCode()
                );
                JOptionPane.showMessageDialog(null, info,"入库成功",JOptionPane.INFORMATION_MESSAGE);

                if(callback != null){
                    callback.run();
                }
            }catch (BusinessException ex){
                JOptionPane.showMessageDialog(this,ex.getMessage(),"入库异常",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
