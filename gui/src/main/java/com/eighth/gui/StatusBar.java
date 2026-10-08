package com.eighth.gui;

import javax.swing.*;
import java.awt.*;

/**
 * 底部状态栏，显示提示信息
 */
public class StatusBar extends JPanel {
    private final JLabel label;
    public StatusBar(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEtchedBorder());
        label = new JLabel("就绪");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        add(label,BorderLayout.WEST);
    }
    public void setMsg(String text){
        label.setText(text);
    }
}
