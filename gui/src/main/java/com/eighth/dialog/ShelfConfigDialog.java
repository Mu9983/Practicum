package com.eighth.dialog;

import com.eighth.Shelf;
import javax.swing.*;
import java.awt.*;

public class ShelfConfigDialog extends JDialog {

    private JSpinner spinnerShelfNum;
    private JSpinner spinnerRow;
    private JSpinner spinnerCol;
    private final Runnable callback;

    public ShelfConfigDialog(JFrame owner, Runnable callback) {
        super(owner, "自定义货架配置", true);
        this.callback = callback;
        initUi();
        setSize(340, 220);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void initUi() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        // 使用当前静态值作为初始值，最小1，最大给合理上限
        spinnerShelfNum = new JSpinner(new SpinnerNumberModel(Shelf.SHELF_NUM,1,20,1));
        spinnerRow = new JSpinner(new SpinnerNumberModel(Shelf.ROW,1,10,1));
        spinnerCol = new JSpinner(new SpinnerNumberModel(Shelf.COL,1,30,1));

        panel.add(new JLabel("货架总数量："));
        panel.add(spinnerShelfNum);
        panel.add(new JLabel("每个货架行数："));
        panel.add(spinnerRow);
        panel.add(new JLabel("每个货架列数："));
        panel.add(spinnerCol);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("确认配置");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout(10,10));
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dispose());

        okBtn.addActionListener(e -> {
            int shelfNum = (Integer) spinnerShelfNum.getValue();
            int row = (Integer) spinnerRow.getValue();
            int col = (Integer) spinnerCol.getValue();

            if(shelfNum <1 || row <1 || col <1){
                JOptionPane.showMessageDialog(this,"数值必须大于等于1！","输入错误",JOptionPane.ERROR_MESSAGE);
                return;
            }
            int maxSlot = shelfNum * row * col;
            if(maxSlot > 1000){
                int opt = JOptionPane.showConfirmDialog(this,
                        String.format("当前总货位数量为%d，数量过大可能造成界面卡顿，确认继续吗？",maxSlot),
                        "警告",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(opt != JOptionPane.YES_OPTION){
                    return;
                }
            }

            // 修改Shelf静态全局配置
            Shelf.SHELF_NUM = shelfNum;
            Shelf.ROW = row;
            Shelf.COL = col;

            JOptionPane.showMessageDialog(this,"货架配置已修改，系统将清空所有包裹数据！");
            dispose();
            if(callback != null){
                callback.run();
            }
        });
    }
}
