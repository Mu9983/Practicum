package com.eighth.gui;

import com.eighth.Parcel;
import com.eighth.Shelf;
import com.eighth.dialog.AddParcelDialog;
import com.eighth.dialog.EnqueueTaskDialog;
import com.eighth.exception.BusinessException;
import com.eighth.service.ExpressService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * 主窗口：菜单栏、查询区、货架绘图区、队列面板、包裹表格；
 * 底部South：左侧状态栏，右侧快捷操作按钮（已移除全局取件出队按钮）
 */
public class MainFrame extends JFrame {
    private final ExpressService expressService;
    private StatusBar statusBar;
    private MainPanel mainPanel;
    // 底部快捷按钮，已移除 btnTakeFromQueue
    private JButton btnAddParcel;
    private JButton btnAddTask;
    private JButton btnUndo;

    // ========= 超期阈值，单位毫秒，默认18小时 =========
    private long overdueThresholdMs = 18L * 3600 * 1000;

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

    /**
     * 将毫秒 → 时、分、秒
     * @param ms 毫秒
     * @return int[]{hour,minute,second}
     */
    private int[] millisToHms(long ms){
        long totalSec = ms /1000;
        int h = (int)(totalSec / 3600);
        int m = (int)((totalSec % 3600)/60);
        int s = (int)(totalSec % 60);
        return new int[]{h,m,s};
    }

    /**
     * 时、分、秒 → 总毫秒
     */
    private long hmsToMillis(int h,int m,int s){
        return h * 3600_000L + m *60_000L + s *1000L;
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
        JMenuItem shelfConfigItem = new JMenuItem("自定义货架配置");
        JMenuItem configOverdueItem = new JMenuItem("设置超期时间");
        JMenuItem transferItem = new JMenuItem("包裹转移");

        JMenuItem statOverdueItem = new JMenuItem("超期统计");
        JMenuItem undoItem = new JMenuItem("撤销上一步");

        operateMenu.add(addParcelItem);
        operateMenu.add(addTaskItem);
        operateMenu.add(takeParcelItem);
        operateMenu.add(shelfConfigItem);
        operateMenu.add(configOverdueItem);
        operateMenu.add(transferItem);
        operateMenu.add(statOverdueItem);
        operateMenu.addSeparator();
        operateMenu.add(undoItem);

        JMenu helpMenu = new JMenu("帮助(H)");
        JMenuItem aboutItem = new JMenuItem("关于系统");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(operateMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // ========= 文件菜单事件 =========
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
            fileChooser.setDialogTitle("选择要加载的数据文件");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件(*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            int opt = fileChooser.showOpenDialog(MainFrame.this);
            if(opt != JFileChooser.APPROVE_OPTION){
                return;
            }
            File selectFile = fileChooser.getSelectedFile();
            String filePath = selectFile.getAbsolutePath();
            try {
                ExpressService.LoadResult result = expressService.loadFromFile(filePath);
                mainPanel.refreshAll();

                StringBuilder sb = new StringBuilder();
                sb.append("文件读取完毕\n");
                sb.append("总读取行数：").append(result.totalLines).append("\n");
                sb.append("成功解析行数：").append(result.successLines).append("\n");
                sb.append("失败行数：").append(result.errorLogs.size()).append("\n\n");
                if(!result.errorLogs.isEmpty()){
                    sb.append("【错误明细】\n");
                    for(String err : result.errorLogs){
                        sb.append(err).append("\n");
                    }
                }else{
                    sb.append("✅所有行解析正常");
                }

                JTextArea ta = new JTextArea(sb.toString());
                ta.setEditable(false);
                JScrollPane sp = new JScrollPane(ta);
                sp.setPreferredSize(new Dimension(600,350));
                JOptionPane.showMessageDialog(MainFrame.this, sp,"加载报告",JOptionPane.INFORMATION_MESSAGE);
                statusBar.setMsg("文件加载完成，成功："+result.successLines+"行，失败："+result.errorLogs.size()+"行");
            } catch (BusinessException ex) {
                //致命错误：文件打不开等
                JOptionPane.showMessageDialog(MainFrame.this, "文件加载失败："+ex.getMessage(),"致命错误",JOptionPane.ERROR_MESSAGE);
            }
        });


        exitItem.addActionListener(e -> System.exit(0));

        // ========= 操作菜单事件 =========
        addParcelItem.addActionListener(e -> openAddParcelDialog());
        addTaskItem.addActionListener(e -> openEnqueueTaskDialog());
        takeParcelItem.addActionListener(e -> doQueueTakeAction());
        undoItem.addActionListener(e -> doUndoAction());

        shelfConfigItem.addActionListener(e -> {
            new com.eighth.dialog.ShelfConfigDialog(this, () -> {
                expressService.reinitShelfAll();
                mainPanel.taskQueue.clear();
                mainPanel.priorityTaskQueue.clear();
                mainPanel.refreshAll();
                statusBar.setMsg("✅货架配置已更新，已清空所有包裹数据与任务队列");
            });
        });

        // ----------------【设置超期时间：JSpinner 时、分、秒调节条】----------------
        configOverdueItem.addActionListener(e -> {
            int[] hms = millisToHms(overdueThresholdMs);
            int currH = hms[0];
            int currM = hms[1];
            int currS = hms[2];

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER,8,10));
            panel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

            //时：0~99小时
            JSpinner spinHour = new JSpinner(new SpinnerNumberModel(currH,0,99,1));
            //分：0‑59
            JSpinner spinMin = new JSpinner(new SpinnerNumberModel(currM,0,59,1));
            //秒：0‑59
            JSpinner spinSec = new JSpinner(new SpinnerNumberModel(currS,0,59,1));

            panel.add(new JLabel("时"));
            panel.add(spinHour);
            panel.add(new JLabel("分"));
            panel.add(spinMin);
            panel.add(new JLabel("秒"));
            panel.add(spinSec);

            int opt = JOptionPane.showConfirmDialog(this,panel,"设置包裹超期时间",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
            if(opt != JOptionPane.OK_OPTION){
                return;
            }

            int h = (Integer) spinHour.getValue();
            int m = (Integer) spinMin.getValue();
            int s = (Integer) spinSec.getValue();

            long newMs = hmsToMillis(h,m,s);
            if(newMs <= 0){
                JOptionPane.showMessageDialog(this,"超期时长不能为0！","输入错误",JOptionPane.ERROR_MESSAGE);
                return;
            }
            overdueThresholdMs = newMs;
            JOptionPane.showMessageDialog(this,String.format("✅已设置超期时间：%02d时%02d分%02d秒",h,m,s));
        });

        //=====菜单栏：包裹转移（需要填写源+目标）=====
        transferItem.addActionListener(e -> {
            // 主面板：左右两部分
            JPanel mainPanelDialog = new JPanel(new GridBagLayout());
            mainPanelDialog.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0.5;
            gbc.weighty = 1;

            // ==========左侧：源货位面板 ==========
            JPanel panelSrc = new JPanel(new GridLayout(4,2,6,8));
            panelSrc.setBorder(BorderFactory.createTitledBorder("当前包裹位置"));

            JSpinner spinSrcShelf = new JSpinner(new SpinnerNumberModel(1,1,Shelf.SHELF_NUM,1));
            JSpinner spinSrcRow   = new JSpinner(new SpinnerNumberModel(1,1,Shelf.ROW,1));
            JSpinner spinSrcCol   = new JSpinner(new SpinnerNumberModel(1,1,Shelf.COL,1));

            panelSrc.add(new JLabel("源货架："));
            panelSrc.add(spinSrcShelf);
            panelSrc.add(new JLabel("源行："));
            panelSrc.add(spinSrcRow);
            panelSrc.add(new JLabel("源列："));
            panelSrc.add(spinSrcCol);

            // ==========右侧：目标货位面板 ==========
            JPanel panelDst = new JPanel(new GridLayout(4,2,6,8));
            panelDst.setBorder(BorderFactory.createTitledBorder("目标货位"));

            JSpinner spinDstShelf = new JSpinner(new SpinnerNumberModel(1,1,Shelf.SHELF_NUM,1));
            JSpinner spinDstRow   = new JSpinner(new SpinnerNumberModel(1,1,Shelf.ROW,1));
            JSpinner spinDstCol   = new JSpinner(new SpinnerNumberModel(1,1,Shelf.COL,1));

            panelDst.add(new JLabel("目标货架："));
            panelDst.add(spinDstShelf);
            panelDst.add(new JLabel("目标行："));
            panelDst.add(spinDstRow);
            panelDst.add(new JLabel("目标列："));
            panelDst.add(spinDstCol);

            // 左右放入主容器
            gbc.gridx = 0;
            mainPanelDialog.add(panelSrc, gbc);
            gbc.gridx = 1;
            mainPanelDialog.add(panelDst, gbc);

            int opt = JOptionPane.showConfirmDialog(MainFrame.this,
                    mainPanelDialog,
                    "包裹转移",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if(opt != JOptionPane.OK_OPTION){
                return;
            }

            // UI界面值转内部0下标
            int srcShelfUi = (Integer) spinSrcShelf.getValue();
            int srcRowUi   = (Integer) spinSrcRow.getValue();
            int srcColUi   = (Integer) spinSrcCol.getValue();

            int dstShelfUi = (Integer) spinDstShelf.getValue();
            int dstRowUi   = (Integer) spinDstRow.getValue();
            int dstColUi   = (Integer) spinDstCol.getValue();

            int srcShelf = srcShelfUi - 1;
            int srcRow   = srcRowUi - 1;
            int srcCol   = srcColUi - 1;

            int dstShelf = dstShelfUi - 1;
            int dstRow   = dstRowUi - 1;
            int dstCol   = dstColUi - 1;

            try {
                Parcel parcel = expressService.getParcelByPos(srcShelf, srcRow, srcCol);
                if(parcel == null){
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "⚠️源货位没有包裹，请检查源货架/行/列！",
                            "提示",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                expressService.transferParcel(parcel.getPickCode(), dstShelf, dstRow, dstCol);
                mainPanel.refreshAll();
                statusBar.setMsg("✅包裹转移完成，取件码：" + parcel.getPickCode());
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(MainFrame.this, ex.getMessage(), "转移失败", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ----【超期统计菜单事件】使用全局overdueThresholdMs ----
        statOverdueItem.addActionListener(e -> {
            try {
                expressService.scanAndMarkOverdue(overdueThresholdMs);
                ExpressService.OverdueStatResult stat = expressService.getOverdueStat(overdueThresholdMs);

                int[] hmsShow = millisToHms(overdueThresholdMs);
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("========包裹超期统计（阈值：%02d时%02d分%02d秒）========\n",
                        hmsShow[0],hmsShow[1],hmsShow[2]));
                sb.append("全部包裹总数：").append(stat.totalAll).append("\n");
                sb.append("当前在库包裹数：").append(stat.totalInStock).append("\n");
                sb.append("⚠️超期包裹数量：").append(stat.totalOverdue).append("\n\n");

                if (stat.totalOverdue > 0) {
                    sb.append("----------超期包裹列表----------\n");
                    for (var p : stat.overdueList) {
                        long durSec = p.getStoreDurationMs() /1000;
                        long day = durSec / (3600*24);
                        long hr = (durSec % (3600*24)) /3600;
                        sb.append(String.format("取件码:%s | 收件人:%s | 已存放：%d天 %d小时\n",
                                p.getPickCode(),
                                p.getReceiverName(),
                                day,hr
                        ));
                    }
                } else {
                    sb.append("✅暂无超期包裹");
                }

                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(520, 340));
                JOptionPane.showMessageDialog(this, scrollPane, "超期统计", JOptionPane.INFORMATION_MESSAGE);
                mainPanel.refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "统计异常", JOptionPane.WARNING_MESSAGE);
            }
        });

        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "快递货架定位及取件管理系统\n2404111 满子皜 刘羽展",
                "关于", JOptionPane.INFORMATION_MESSAGE));
    }

    /**
     * 初始化底部：左侧状态栏，右侧快捷按钮
     */
    private void initComponent() {
        setLayout(new BorderLayout());
        mainPanel = new MainPanel(expressService);
        statusBar = new StatusBar();

        JPanel bottomWholePanel = new JPanel(new BorderLayout());
        JPanel btnRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnAddParcel = new JButton("新增包裹");
        btnAddTask = new JButton("加入取件队列");
        btnUndo = new JButton("撤销上一步");

        btnRightPanel.add(btnAddParcel);
        btnRightPanel.add(btnAddTask);
        btnRightPanel.add(btnUndo);

        bottomWholePanel.add(statusBar, BorderLayout.CENTER);
        bottomWholePanel.add(btnRightPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(bottomWholePanel, BorderLayout.SOUTH);
    }

    /**
     * 绑定底部按钮点击事件
     */
    private void bindBottomButtonEvent() {
        btnAddParcel.addActionListener(e -> openAddParcelDialog());
        btnAddTask.addActionListener(e -> openEnqueueTaskDialog());
        btnUndo.addActionListener(e -> doUndoAction());
    }

    // ==========业务动作抽取方法，菜单复用============
    private void openAddParcelDialog() {
        new AddParcelDialog(this, expressService, () -> {
            mainPanel.refreshAll();
            statusBar.setMsg("✅包裹入库成功");
        });
    }

    private void openEnqueueTaskDialog() {
        new EnqueueTaskDialog(this, expressService, mainPanel, () -> {
            mainPanel.refreshAll();
            statusBar.setMsg("✅任务加入取件队列");
        });
    }

    private void doQueueTakeAction() {
        try {
            var parcel = mainPanel.doQueueTake();
            mainPanel.refreshAll();
            if (parcel != null) {
                statusBar.setMsg("✅执行取件完成，取件码：" + parcel.getPickCode());
            }else{
                statusBar.setMsg("取件任务已取消，包裹保留货架");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "队列取件", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void doUndoAction() {
        try {
            expressService.undo();
            mainPanel.refreshAll();
            statusBar.setMsg("✅撤销操作成功");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "撤销失败：" + ex.getMessage(), "错误", JOptionPane.WARNING_MESSAGE);
        }
    }
}
