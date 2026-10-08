package com.eighth.dialog;

import com.eighth.Parcel;
import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;

import javax.swing.*;
import java.awt.*;

/**
 * 取件弹窗，输入取件码
 */
public class TakeParcelDialog extends JDialog {
    private final ExpressService service;
    private final Runnable callback;

    public TakeParcelDialog(JFrame owner, ExpressService service,Runnable callback){
        super(owner,"快递取件",true);
        this.service = service;
        this.callback = callback;
        init();
        setSize(340,160);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void init(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,2,8,8));
        JTextField codeField = new JTextField();
        panel.add(new JLabel("请输入6位取件码："));
        panel.add(codeField);

        JPanel btnPanel = new JPanel();
        JButton ok = new JButton("确认取件");
        JButton cancel = new JButton("取消");
        btnPanel.add(ok);
        btnPanel.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel,BorderLayout.CENTER);
        getContentPane().add(btnPanel,BorderLayout.SOUTH);

        cancel.addActionListener(e->dispose());
        ok.addActionListener(e->{
            String pickCode = codeField.getText().trim();
            try{
                Parcel parcel = service.takeParcel(pickCode);
                JOptionPane.showMessageDialog(this,"取件成功！包裹："+parcel.getReceiverName());
                dispose();
                callback.run();
            }catch (BusinessException ex){
                JOptionPane.showMessageDialog(this,ex.getMessage());
            }
        });
    }
}
