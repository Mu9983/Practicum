package com.eighth.dialog;

import com.eighth.ShelfSlot;
import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;

import javax.swing.*;
import java.awt.*;

/**
 * 新增包裹入库弹窗：UI界面全部从1开始，业务层转换回0下标
 */
public class AddParcelDialog extends JDialog {
    private final ExpressService service;
    private final Runnable callback;

    // 普通构造：默认货架1,行1,列1（菜单、右下角按钮调用）
    public AddParcelDialog(JFrame owner, ExpressService service, Runnable callback){
        this(owner, service, 1, 1, 1, callback);
    }

    // 重载构造：支持传入预填UI坐标(1开始)，双击空货位调用
    public AddParcelDialog(JFrame owner, ExpressService service, int initUiShelf, int initUiRow, int initUiCol, Runnable callback){
        super(owner,"新增包裹入库",true);
        this.service = service;
        this.callback = callback;
        initUi(initUiShelf, initUiRow, initUiCol);
        setSize(360,240);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * @param initUiShelf UI货架 1‑10
     * @param initUiRow   UI行 1‑5
     * @param initUiCol   UI列 1‑20
     */
    private void initUi(int initUiShelf, int initUiRow, int initUiCol){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5,2,6,6));

        JTextField receiverField = new JTextField();
        // UI:货架1~10；底层0‑9，使用传入的初始值
        JSpinner shelfSpinner = new JSpinner(new SpinnerNumberModel(initUiShelf,1,10,1));
        // UI:行1‑5；底层0‑4
        JSpinner rowSpinner = new JSpinner(new SpinnerNumberModel(initUiRow,1,5,1));
        // UI:列1‑20；底层0‑19
        JSpinner colSpinner = new JSpinner(new SpinnerNumberModel(initUiCol,1,20,1));

        panel.add(new JLabel("收件人姓名："));
        panel.add(receiverField);
        panel.add(new JLabel("货架编号(1‑10)"));
        panel.add(shelfSpinner);
        panel.add(new JLabel("行号(1‑5)"));
        panel.add(rowSpinner);
        panel.add(new JLabel("列号(1‑20)"));
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
            // UI拿到1开始的值
            int shelfUi = (Integer)shelfSpinner.getValue();
            int rowUi = (Integer)rowSpinner.getValue();
            int colUi = (Integer)colSpinner.getValue();

            if(name.isBlank()){
                JOptionPane.showMessageDialog(this,"收件人不能为空");
                return;
            }
            try{
                // UI输入(1‑10,1‑5,1‑20) → 转换底层0下标
                int shelf = shelfUi - 1;
                int row = rowUi -1;
                int col = colUi -1;

                ShelfSlot slot = new ShelfSlot();
                slot.setRow(row);
                slot.setCol(col);
                service.addParcel(name,shelf,slot);
                dispose();
                if(callback != null){
                    callback.run();
                }
            }catch (BusinessException ex){
                JOptionPane.showMessageDialog(this,ex.getMessage(),"入库异常",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
