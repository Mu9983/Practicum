package com.eighth.gui;

import com.eighth.*;
import com.eighth.dialog.AddParcelDialog;
import com.eighth.model.ParcelTableModel;
import com.eighth.service.ExpressService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * 中心面板：顶部查询区，中间分割面板（左侧货架画布，右侧【队列面板+包裹表格】）
 */
public class MainPanel extends JPanel {
    private final ExpressService service;
    private ParcelTableModel tableModel;
    private ShelfCanvas shelfCanvas;

    MyPriorityQueue<PickTask> priorityTaskQueue = new MyPriorityQueue<>();
    private JTextArea priorityQueueTextArea; //优先队列文本域
    private JTextArea queueTextArea;        //普通队列
    MyQueue<PickTask> taskQueue = new MyQueue<>(); //原有普通循环队列

    // 两个队列按钮
    private JButton btnPriorityDequeue;
    private JButton btnNormalDequeue;

    public MainPanel(ExpressService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        initTopSearchPanel();
        initCenterSplitPanel();
        bindQueueButtonEvent(); //绑定两个队列按钮事件
    }

    /**
     * 顶部查询面板：输入取件码查询，实现货位高亮
     */
    private void initTopSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("🔍取件码查询（查询后货架自动高亮货位）"));
        JTextField codeInput = new JTextField(12);
        JButton searchBtn = new JButton("查询");
        JButton clearHighlightBtn = new JButton("清除高亮");
        searchPanel.add(new JLabel("输入6位取件码:"));
        searchPanel.add(codeInput);
        searchPanel.add(searchBtn);
        searchPanel.add(clearHighlightBtn);

        searchBtn.addActionListener(e -> {
            String code = codeInput.getText().trim();
            if (code.isBlank()) {
                JOptionPane.showMessageDialog(this, "请输入取件码前缀（数字）");
                return;
            }
            Parcel[] matchArray = service.searchByPrefix(code);
            if (matchArray == null || matchArray.length == 0) {
                JOptionPane.showMessageDialog(this, "❌没有匹配该前缀的包裹！", "查询结果", JOptionPane.WARNING_MESSAGE);
                shelfCanvas.setHighlightParcel(null);
                shelfCanvas.repaint();
                return;
            }
            if (matchArray.length > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("共找到 %d 条匹配前缀【%s】的包裹：\n", matchArray.length, code));
                for (Parcel p : matchArray) {
                    sb.append(String.format("取件码:%s 收件人:%s 货架:%d 货位:%d\n",
                            p.getPickCode(),
                            p.getReceiverName(),
                            p.getShelf() + 1,
                            p.getSlot().getRow() * Shelf.COL + p.getSlot().getCol() + 1));
                }
                JOptionPane.showMessageDialog(this, sb.toString(), "多条匹配结果", JOptionPane.INFORMATION_MESSAGE);
                shelfCanvas.setHighlightParcel(null);
                shelfCanvas.repaint();
                return;
            }
            Parcel parcel = matchArray[0];
            shelfCanvas.setHighlightParcel(parcel);
            shelfCanvas.repaint();
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "查询结果", true);
            dialog.setSize(380, 220);
            dialog.setLocationRelativeTo(null);
            dialog.setResizable(false);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            int shelfShow = parcel.getShelf() + 1;
            int row0 = parcel.getSlot().getRow();
            int col0 = parcel.getSlot().getCol();
            int posNo = row0 * Shelf.COL + col0 + 1;
            JTextArea infoText = new JTextArea();
            infoText.setEditable(false);
            infoText.setFont(new Font("Dialog", Font.PLAIN, 13));
            String info = String.format(
                    "✅查询成功\n" +
                            "包裹ID：%d\n" +
                            "取件码：%s\n" +
                            "收件人：%s\n" +
                            "货架号：%d\n" +
                            "货位号：%d",
                    parcel.getId(),
                    parcel.getPickCode(),
                    parcel.getReceiverName(),
                    shelfShow,
                    posNo
            );
            infoText.setText(info);
            contentPanel.add(infoText, BorderLayout.CENTER);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            JButton btnEnqueue = new JButton("加入取件队列");
            JButton btnClose = new JButton("关闭");
            btnPanel.add(btnEnqueue);
            btnPanel.add(btnClose);
            contentPanel.add(btnPanel, BorderLayout.SOUTH);
            dialog.add(contentPanel);
            btnEnqueue.addActionListener(ev -> {
                PickTask task = new PickTask(System.currentTimeMillis(), parcel, System.currentTimeMillis(), 0);
                boolean ok = enqueueTask(task);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "✅已成功加入取件队列");
                    refreshAll();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "❌队列已满，无法加入！", "提示", JOptionPane.WARNING_MESSAGE);
                }
            });
            btnClose.addActionListener(ev -> dialog.dispose());
            dialog.setVisible(true);
        });

        clearHighlightBtn.addActionListener(e -> {
            shelfCanvas.setHighlightParcel(null);
            shelfCanvas.repaint();
        });
        add(searchPanel, BorderLayout.NORTH);
    }

    private void initCenterSplitPanel() {
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        mainSplit.setDividerLocation(860);
        //左侧：货架绘图画布
        shelfCanvas = new ShelfCanvas(service, this);
        JScrollPane scrollCanvas = new JScrollPane(shelfCanvas);
        scrollCanvas.setBorder(BorderFactory.createTitledBorder("📦货架可视化（双击货位查看包裹详情）"));
        mainSplit.setLeftComponent(scrollCanvas);

        //右侧：三层垂直分割
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        rightSplit.setDividerLocation(140);

        //==== 1.异常优先任务面板：增加底部按钮面板 ====
        JPanel priorityPanel = new JPanel(new BorderLayout());
        priorityPanel.setBorder(BorderFactory.createTitledBorder("⚠️异常优先任务队列"));
        priorityQueueTextArea = new JTextArea();
        priorityQueueTextArea.setEditable(false);
        priorityPanel.add(new JScrollPane(priorityQueueTextArea), BorderLayout.CENTER);
        //底部按钮
        JPanel priorityBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnPriorityDequeue = new JButton("异常任务出队");
        priorityBtnPanel.add(btnPriorityDequeue);
        priorityPanel.add(priorityBtnPanel, BorderLayout.SOUTH);
        rightSplit.setTopComponent(priorityPanel);

        //==== 2.普通取件任务队列面板：增加底部按钮面板 ====
        JSplitPane midBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        midBottomSplit.setDividerLocation(140);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("⏳普通取件任务队列"));
        queueTextArea = new JTextArea();
        queueTextArea.setEditable(false);
        queuePanel.add(new JScrollPane(queueTextArea), BorderLayout.CENTER);
        //底部按钮
        JPanel normalBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnNormalDequeue = new JButton("普通任务出队");
        normalBtnPanel.add(btnNormalDequeue);
        queuePanel.add(normalBtnPanel, BorderLayout.SOUTH);
        midBottomSplit.setTopComponent(queuePanel);

        //====3.包裹表格 ====
        tableModel = new ParcelTableModel();
        JTable parcelTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(parcelTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("📋全部包裹列表"));
        midBottomSplit.setBottomComponent(tableScroll);

        rightSplit.setBottomComponent(midBottomSplit);
        mainSplit.setRightComponent(rightSplit);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void bindQueueButtonEvent() {
        //异常任务出队：完全原样，优先队列不做付费弹窗
        btnPriorityDequeue.addActionListener(e -> {
            try {
                Parcel p = dequeuePriorityTask();
                JOptionPane.showMessageDialog(this, "✅异常任务处理完成，取件码：" + p.getPickCode());
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "异常队列", JOptionPane.WARNING_MESSAGE);
            }
        });

        //普通任务出队：会遇到返回null（用户取消付费）
        btnNormalDequeue.addActionListener(e -> {
            try {
                Parcel p = dequeueNormalTask();
                if (p != null) {
                    JOptionPane.showMessageDialog(this, "✅普通任务取件完成，取件码：" + p.getPickCode());
                }
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "普通队列", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * 只出【异常优先队列】
     */
    public Parcel dequeuePriorityTask() {
        if (priorityTaskQueue.isEmpty()) {
            throw new RuntimeException("异常优先队列为空！");
        }
        PickTask task = priorityTaskQueue.poll();
        return service.takeParcel(task.getParcel().getPickCode());
    }

    /**
     * 只出【普通取件队列】
     */
    public Parcel dequeueNormalTask() {
        if (taskQueue.isEmpty()) {
            throw new RuntimeException("普通取件队列为空！");
        }
        PickTask task = taskQueue.peek();
        boolean canTake = checkExpiredAndPayDialog(task);
        //任务必须离开队列
        taskQueue.dequeue();
        if(!canTake){
            JOptionPane.showMessageDialog(this, "已取消本次取件任务，包裹保留在货架上", "取消取件",JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return service.takeParcel(task.getParcel().getPickCode());
    }


    /**
     * 菜单栏“执行取件”：优先异常优先队列，再普通队列
     * 只有普通队列出来的任务才做超期付费弹窗；异常优先队列任务直接执行
     */
    public Parcel doQueueTake() {
        PickTask task;
        boolean fromPriorityQueue;
        if (!priorityTaskQueue.isEmpty()) {
            task = priorityTaskQueue.poll();
            fromPriorityQueue = true;
        } else {
            if (taskQueue.isEmpty()) {
                throw new RuntimeException("取件队列为空！");
            }
            task = taskQueue.peek();
            fromPriorityQueue = false;
        }

        // 如果来自异常优先队列：直接取件，不走付费弹窗
        if(fromPriorityQueue){
            return service.takeParcel(task.getParcel().getPickCode());
        }

        // 来自普通队列，执行超期付费弹窗
        boolean canTake = checkExpiredAndPayDialog(task);
        taskQueue.dequeue();
        if(!canTake){
            JOptionPane.showMessageDialog(this, "已取消本次取件任务，包裹保留在货架上", "取消取件",JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return service.takeParcel(task.getParcel().getPickCode());
    }


    /**
     * 全部刷新：货架画布、表格、队列文本
     */
    public void refreshAll() {
        shelfCanvas.resetCanvasSize();
        refreshTable();
        refreshQueueText();
        refreshPriorityQueueText();
        shelfCanvas.repaint();
    }

    /**
     * 刷新异常优先队列文本域
     */
    private void refreshPriorityQueueText() {
        priorityQueueTextArea.setText("");
        if (priorityTaskQueue.isEmpty()) {
            priorityQueueTextArea.setText("【异常优先队列为空】");
            return;
        }
        List<PickTask> list = priorityTaskQueue.getAll();
        for (int i = 0; i < list.size(); i++) {
            PickTask task = list.get(i);
            Parcel p = task.getParcel();
            String reasonShow = p.getAbnormalReason() == null ? "无" : p.getAbnormalReason();
            priorityQueueTextArea.append(String.format("[%d] 取件码:%s | 收件人:%s |异常等级:%d |异常原因:%s\n",
                    i + 1, p.getPickCode(), p.getReceiverName(), p.getAbnormalLevel(), reasonShow));
        }
        priorityQueueTextArea.repaint();
    }

    private void refreshTable() {
        int total = service.getTotalExpressCount();
        Parcel[] res = new Parcel[total];
        int idx = 0;
        for (int s = 0; s < 10; s++) {
            Parcel[] temp = service.getShelfExpress(s);
            for (Parcel p : temp) {
                if (idx < res.length) {
                    res[idx++] = p;
                }
            }
        }
        Arrays.sort(res, (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return Long.compare(o1.getId(), o2.getId());
        });
        tableModel.setData(res);
        tableModel.fireTableDataChanged();
    }

    private void refreshQueueText() {
        queueTextArea.setText("");
        if (taskQueue.isEmpty()) {
            queueTextArea.setText("【队列为空，暂无取件任务】");
            return;
        }
        Object[] objArray = taskQueue.getAll();
        for (int i = 0; i < objArray.length; i++) {
            PickTask task = (PickTask) objArray[i];
            Parcel p = task.getParcel();
            queueTextArea.append(String.format("[%d] 取件码:%s | 收件人:%s\n",
                    i + 1, p.getPickCode(), p.getReceiverName()));
        }
        queueTextArea.repaint();
    }

    public boolean enqueueTask(PickTask task) {
        return taskQueue.enqueue(task);
    }

    public boolean enqueuePriorityTask(PickTask task) {
        return priorityTaskQueue.offer(task);
    }

    /**
     * ShelfCanvas双击空货位调用，预填位置（UI：1开始）
     */
    public void openAddParcelWithPos(int uiShelf, int uiRow, int uiCol) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        new AddParcelDialog(frame, service, uiShelf, uiRow, uiCol, this::refreshAll);
    }

    /**
     * 超期包裹付费弹窗处理
     * @param task 当前要出队的取件任务
     * @return true=已支付，可以执行真正takeParcel；false=取消，丢弃任务，包裹保留货架
     */
    private boolean checkExpiredAndPayDialog(PickTask task) {
        Parcel parcel = task.getParcel();
        // 不是超期包裹，直接放行
        if(parcel.getStatus() != ParcelStatus.EXPIRED){
            return true;
        }
        // 超期，弹出付费对话框
        Object[] options = {"已支付", "取消"};
        int opt = JOptionPane.showOptionDialog(this,
                "⚠️该包裹已经超期，取件需要支付1元。",
                "超期付费提示",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]);
        // 0=已支付；1=取消
        return opt == 0;
    }

}
