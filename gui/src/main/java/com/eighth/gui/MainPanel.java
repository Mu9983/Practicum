package com.eighth.gui;

import com.eighth.MyQueue;
import com.eighth.Parcel;
import com.eighth.PickTask;
import com.eighth.Shelf;
import com.eighth.dialog.AddParcelDialog;
import com.eighth.model.ParcelTableModel;
import com.eighth.service.ExpressService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * 中心面板：顶部查询区，中间分割面板（左侧货架画布，右侧【队列面板+包裹表格】）
 */
public class MainPanel extends JPanel {
    private final ExpressService service;
    private ParcelTableModel tableModel;
    private ShelfCanvas shelfCanvas;
    private JTextArea queueTextArea;
    private MyQueue<PickTask> taskQueue = new MyQueue<>();

    public MainPanel(ExpressService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        initTopSearchPanel();
        initCenterSplitPanel();
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

        searchBtn.addActionListener(e->{
            String code = codeInput.getText().trim();
            if(code.isBlank()){
                JOptionPane.showMessageDialog(this,"请输入取件码");
                return;
            }
            Parcel parcel = service.searchByCode(code);
            if(parcel == null){
                JOptionPane.showMessageDialog(this,"❌未找到该取件码包裹！","查询结果",JOptionPane.WARNING_MESSAGE);
                shelfCanvas.setHighlightParcel(null);
                shelfCanvas.repaint();
                return;
            }
            // 设置高亮
            shelfCanvas.setHighlightParcel(parcel);
            shelfCanvas.repaint();

            // =========自定义弹窗，带【加入取件队列】按钮=========
            JDialog dialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this),"查询结果",true);
            dialog.setSize(380,220);
            dialog.setLocationRelativeTo(null); //屏幕居中
            dialog.setResizable(false);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout(10,10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

            // 文本信息
            int shelfShow = parcel.getShelf() + 1;
            int row0 = parcel.getSlot().getRow();
            int col0 = parcel.getSlot().getCol();
            int posNo = row0 * Shelf.COL + col0 + 1;

            JTextArea infoText = new JTextArea();
            infoText.setEditable(false);
            infoText.setFont(new Font("Dialog",Font.PLAIN,13));
            String info = String.format(
                    "✅查询成功\n"+
                            "包裹ID：%d\n"+
                            "取件码：%s\n"+
                            "收件人：%s\n"+
                            "货架号：%d\n"+
                            "货位号：%d",
                    parcel.getId(),
                    parcel.getPickCode(),
                    parcel.getReceiverName(),
                    shelfShow,
                    posNo
            );
            infoText.setText(info);
            contentPanel.add(infoText,BorderLayout.CENTER);

            // 底部按钮面板
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,5));
            JButton btnEnqueue = new JButton("加入取件队列");
            JButton btnClose = new JButton("关闭");
            btnPanel.add(btnEnqueue);
            btnPanel.add(btnClose);
            contentPanel.add(btnPanel,BorderLayout.SOUTH);

            dialog.add(contentPanel);

            // 加入取件队列按钮事件
            btnEnqueue.addActionListener(ev->{
                // 构造PickTask
                PickTask task = new PickTask(System.currentTimeMillis(), parcel, System.currentTimeMillis(),0);
                boolean ok = enqueueTask(task);
                if(ok){
                    JOptionPane.showMessageDialog(dialog,"✅已成功加入取件队列");
                    refreshAll();
                    dialog.dispose();
                }else{
                    JOptionPane.showMessageDialog(dialog,"❌队列已满，无法加入！","提示",JOptionPane.WARNING_MESSAGE);
                }
            });

            btnClose.addActionListener(ev-> dialog.dispose());

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

        //右侧：垂直分割，上方队列，下方包裹表格
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        rightSplit.setDividerLocation(220);

        //取件队列面板
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("⏳取件任务队列"));
        queueTextArea = new JTextArea();
        queueTextArea.setEditable(false);
        queuePanel.add(new JScrollPane(queueTextArea));
        rightSplit.setTopComponent(queuePanel);

        //包裹表格
        tableModel = new ParcelTableModel();
        JTable parcelTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(parcelTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("📋全部包裹列表"));
        rightSplit.setBottomComponent(tableScroll);

        mainSplit.setRightComponent(rightSplit);
        add(mainSplit, BorderLayout.CENTER);
    }

    /**
     * 全部刷新：货架画布、表格、队列文本
     */
    public void refreshAll() {
        refreshTable();
        refreshQueueText();
        shelfCanvas.repaint();
    }

    private void refreshTable(){
        int total = service.getTotalExpressCount();
        Parcel[] res = new Parcel[total];
        int idx=0;
        for(int s=0;s<10;s++){
            Parcel[] temp = service.getShelfExpress(s);
            for(Parcel p : temp){
                if(idx < res.length){
                    res[idx++] = p;
                }
            }
        }

        Arrays.sort(res, (o1, o2) -> {
            if(o1 == null && o2 == null) return 0;
            if(o1 == null) return 1;
            if(o2 == null) return -1;
            return Long.compare(o1.getId(), o2.getId());
        });

        tableModel.setData(res);
        tableModel.fireTableDataChanged();
    }


    private void refreshQueueText(){
        queueTextArea.setText("");
        if(taskQueue.isEmpty()){
            queueTextArea.setText("【队列为空，暂无取件任务】");
            return;
        }
        // getAll 返回Object[]，泛型擦除
        Object[] objArray = taskQueue.getAll();
        for(int i=0;i<objArray.length;i++){
            PickTask task = (PickTask) objArray[i];
            Parcel p = task.getParcel();
            queueTextArea.append(String.format("[%d] 取件码:%s | 收件人:%s\n",
                    i+1,p.getPickCode(),p.getReceiverName()));
        }
        queueTextArea.repaint();
    }

    /**
     * 将包裹加入循环取件队列
     */
    public boolean enqueueTask(PickTask task) {
        return taskQueue.enqueue(task);
    }

    /**
     * 队列执行取件出队
     */
    public Parcel doQueueTake() {
        if (taskQueue.isEmpty()) {
            throw new RuntimeException("取件队列为空！");
        }
        PickTask task = taskQueue.dequeue();
        return service.takeParcel(task.getParcel().getPickCode());
    }

    /**
     * ShelfCanvas双击空货位调用，预填位置（UI：1开始）
     */
    public void openAddParcelWithPos(int uiShelf, int uiRow, int uiCol) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        // 调用6参数重载构造
        new AddParcelDialog(frame, service, uiShelf, uiRow, uiCol, this::refreshAll);
    }


}
