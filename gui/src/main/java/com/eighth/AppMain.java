package com.eighth;

import com.eighth.gui.MainFrame;
import com.eighth.service.ExpressService;
import com.eighth.service.impl.ExpressServiceImpl;

import javax.swing.*;

/**
 * GUI程序入口，gui模块启动类
 */
public class AppMain {
    public static void main(String[] args) {
        // 在Swing事件调度线程启动窗口
        SwingUtilities.invokeLater(() -> {
            ExpressService service = new ExpressServiceImpl();
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}
