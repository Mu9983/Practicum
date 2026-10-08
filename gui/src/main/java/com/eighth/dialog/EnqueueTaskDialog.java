package com.eighth.dialog;

import com.eighth.Parcel;
import com.eighth.PickTask;
import com.eighth.exception.BusinessException;
import com.eighth.gui.MainPanel;
import com.eighth.service.ExpressService;

import javax.swing.*;
import java.awt.*;

/**
 * 将包裹加入循环取件队列弹窗，输入取件码
 */
public class EnqueueTaskDialog extends JDialog {
    private final ExpressService service;
    private final MainPanel mainPanel;
    private final Runnable callback;

    // 构造函数增加MainPanel入参，不再自己去解析组件
    public EnqueueTaskDialog(JFrame owner, ExpressService service, MainPanel mainPanel, Runnable callback){
        super(owner,"加入取件任务队列",true);
        this.service = service;
        this.mainPanel = mainPanel;
        this.callback = callback;
        initUi();
        setSize(360,180);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void initUi(){
        JPanel panel = new JPanel(new GridLayout(2,2,8,8));
        JTextField codeField = new JTextField();
        panel.add(new JLabel("包裹取件码:"));
        panel.add(codeField);

        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("加入队列");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout(10,10));
        getContentPane().add(panel,BorderLayout.CENTER);
        getContentPane().add(btnPanel,BorderLayout.SOUTH);

        cancelBtn.addActionListener(e->dispose());
        okBtn.addActionListener(e->{
            String code = codeField.getText().trim();
            if(code.isBlank()){
                JOptionPane.showMessageDialog(this,"请输入取件码");
                return;
            }
            try{
                Parcel parcel = service.searchByCode(code);
                if(parcel == null){
                    throw new BusinessException("取件码不存在！");
                }
                PickTask task = new PickTask(System.currentTimeMillis(),parcel,System.currentTimeMillis(),0);
                boolean ok = mainPanel.enqueueTask(task);
                if(!ok){
                    JOptionPane.showMessageDialog(this,"队列已满，无法加入任务！","队列",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                dispose();
                callback.run();
            }catch (BusinessException ex){
                JOptionPane.showMessageDialog(this,ex.getMessage(),"错误",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
