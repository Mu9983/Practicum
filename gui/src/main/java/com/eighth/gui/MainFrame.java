package com.eighth.gui;

import com.eighth.dialog.AddParcelDialog;
import com.eighth.dialog.EnqueueTaskDialog;
import com.eighth.service.ExpressService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * 主窗口：菜单栏、查询区、货架绘图区、队列面板、包裹表格；
 * 底部South：左侧状态栏，右侧横向排布快捷操作按钮
 */
public class MainFrame extends JFrame {
    private final ExpressService expressService;
    private StatusBar statusBar;
    private MainPanel mainPanel;

    // 快捷按钮
    private JButton btnAddParcel;
    private JButton btnAddTask;
    private JButton btnTakeFromQueue;
    private JButton btnUndo;

    public MainFrame(ExpressService service) {
        this.expressService = service;
        initWindow();
        initMenuBar();
        initComponent();
        bindBottomButtonEvent();
    }

    private void initWindow() {
        setTitle("智能快递货架定位及取件管理系统");
        setSize(1450, 840);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("文件(F)");
        JMenuItem saveItem = new JMenuItem("保存数据");
        JMenuItem loadItem = new JMenuItem("加载数据");
        JMenuItem exitItem = new JMenuItem("退出");
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu operateMenu = new JMenu("操作(O)");
        JMenuItem addParcelItem = new JMenuItem("新增包裹");
        JMenuItem addTaskItem = new JMenuItem("加入取件队列");
        JMenuItem takeParcelItem = new JMenuItem("执行取件");
        JMenuItem undoItem = new JMenuItem("撤销上一步");
        operateMenu.add(addParcelItem);
        operateMenu.add(addTaskItem);
        operateMenu.add(takeParcelItem);
        operateMenu.addSeparator();
        operateMenu.add(undoItem);

        JMenu helpMenu = new JMenu("帮助(H)");
        JMenuItem aboutItem = new JMenuItem("关于系统");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(operateMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        //==== 文件菜单事件：弹出文件选择器，不写死路径 ====
        saveItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择保存快递数据文件");
            fileChooser.setSelectedFile(new File("express_data.txt"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件(*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                try {
                    expressService.saveToFile(filePath);
                    statusBar.setMsg("✅保存文件成功：" + filePath);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "保存失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择要加载的快递数据txt文件");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件(*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                try {
                    expressService.loadFromFile(filePath);
                    mainPanel.refreshAll();
                    statusBar.setMsg("✅加载数据完成：" + filePath);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "加载失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exitItem.addActionListener(e -> System.exit(0));

        //==== 菜单事件（备用入口，调用封装方法） ====
        addParcelItem.addActionListener(e -> openAddParcelDialog());
        addTaskItem.addActionListener(e -> openEnqueueTaskDialog());
        takeParcelItem.addActionListener(e -> doQueueTakeAction());
        undoItem.addActionListener(e -> doUndoAction());

        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "快递货架定位及取件管理系统\n2404111 满子皜 刘羽展",
                "关于", JOptionPane.INFORMATION_MESSAGE));
    }

    /**
     * 初始化底部：左侧状态栏，右侧一排快捷按钮
     */
    private void initComponent() {
        setLayout(new BorderLayout());
        mainPanel = new MainPanel(expressService);
        statusBar = new StatusBar();

        //==== 组装底部面板 South：左边状态栏，右边按钮组 ====
        JPanel bottomWholePanel = new JPanel(new BorderLayout());
        // 右侧按钮面板 FlowLayout 靠右，横向排列
        JPanel btnRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4));
        btnAddParcel = new JButton("新增包裹");
        btnAddTask = new JButton("加入取件队列");
        btnTakeFromQueue = new JButton("取件出队");
        btnUndo = new JButton("撤销上一步");

        btnRightPanel.add(btnAddParcel);
        btnRightPanel.add(btnAddTask);
        btnRightPanel.add(btnTakeFromQueue);
        btnRightPanel.add(btnUndo);

        bottomWholePanel.add(statusBar, BorderLayout.CENTER);
        bottomWholePanel.add(btnRightPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(bottomWholePanel, BorderLayout.SOUTH);
    }

    /**
     * 绑定底部按钮点击事件，和菜单共用同一套业务逻辑
     */
    private void bindBottomButtonEvent(){
        btnAddParcel.addActionListener(e -> openAddParcelDialog());
        btnAddTask.addActionListener(e -> openEnqueueTaskDialog());
        btnTakeFromQueue.addActionListener(e -> doQueueTakeAction());
        btnUndo.addActionListener(e -> doUndoAction());
    }

    // ==========业务动作抽取方法，菜单、底部按钮复用============
    private void openAddParcelDialog(){
        new AddParcelDialog(this, expressService, () -> {
            mainPanel.refreshAll();
            statusBar.setMsg("✅包裹入库成功");
        });
    }

    private void openEnqueueTaskDialog(){
        // 传入mainPanel引用，修复之前空对象bug
        new EnqueueTaskDialog(this, expressService, mainPanel, () -> {
            mainPanel.refreshAll();
            statusBar.setMsg("✅任务加入取件队列");
        });
    }

    private void doQueueTakeAction(){
        try {
            var parcel = mainPanel.doQueueTake();
            if(parcel != null){
                mainPanel.refreshAll();
                statusBar.setMsg("✅执行取件完成，取件码："+parcel.getPickCode());
            }
        }catch (Exception ex){
            JOptionPane.showMessageDialog(this,ex.getMessage(),"队列取件",JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doUndoAction(){
        try {
            expressService.undo();
            mainPanel.refreshAll();
            statusBar.setMsg("✅撤销操作成功");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "撤销失败：" + ex.getMessage(), "错误", JOptionPane.WARNING_MESSAGE);
        }
    }

}
