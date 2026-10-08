package com.eighth.gui;

import com.eighth.*;
import com.eighth.service.ExpressService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 货架可视化画布：竖直单列布局，放大货位格子，UI全部从1开始编号，双击弹窗带加入队列按钮
 */
public class ShelfCanvas extends JPanel {
    private final ExpressService service;
    private final MainPanel mainPanel;
    private Parcel highlightParcel = null;

    // 放大货位尺寸
    private static final int CELL_W = 32;
    private static final int CELL_H = 32;
    private static final int GAP = 6;
    // 货架之间垂直间距
    private static final int SHELF_VERTICAL_GAP = 30;

    public ShelfCanvas(ExpressService service, MainPanel mainPanel) {
        this.service = service;
        this.mainPanel = mainPanel;
        // 动态计算画布高度
        int singleShelfHeight = (CELL_H * Shelf.ROW) + GAP * 12 + 35;
        int totalHeight = Shelf.SHELF_NUM * (singleShelfHeight + SHELF_VERTICAL_GAP);
        setPreferredSize(new Dimension(1300, totalHeight));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e.getX(), e.getY());
                }
            }
        });
    }

    public void setHighlightParcel(Parcel parcel) {
        this.highlightParcel = parcel;
    }

    /**
     * 双击检测货位，自定义弹窗：空货位带【添加包裹】按钮；有包裹带【加入取件队列】按钮
     */
    private void handleDoubleClick(int mx, int my) {
        int shelfIndex = -1;
        int row = -1;
        int col = -1;

        int shelfUnitW = (CELL_W * Shelf.COL) + GAP * 8;
        int shelfUnitH = (CELL_H * Shelf.ROW) + GAP * 12;
        int startX = 12;
        int startY = 15;

        for (int s = 0; s < Shelf.SHELF_NUM; s++) {
            int baseX = startX;
            int baseY = startY + s * (shelfUnitH + SHELF_VERTICAL_GAP);

            if (mx >= baseX && mx <= baseX + shelfUnitW
                    && my >= baseY && my <= baseY + shelfUnitH) {
                shelfIndex = s;
                int innerX = mx - baseX - GAP;
                int innerY = my - baseY - GAP;
                col = innerX / (CELL_W + GAP);
                row = innerY / (CELL_H + GAP);
                break;
            }
        }

        if (shelfIndex < 0 || row < 0 || col < 0
                || row >= Shelf.ROW || col >= Shelf.COL) {
            return;
        }

        try {
            Parcel[] parcels = service.getShelfExpress(shelfIndex);
            Parcel clickParcel = null;
            for (Parcel p : parcels) {
                if (p.getShelf() == shelfIndex
                        && p.getSlot().getRow().equals(row)
                        && p.getSlot().getCol().equals(col)) {
                    clickParcel = p;
                    break;
                }
            }

            int shelfShow = shelfIndex + 1;
            int posIndex = row * Shelf.COL + col + 1;

            // ==========空货位：自定义弹窗，带【添加包裹】按钮==========
            if (clickParcel == null) {
                final int finalShelfIndex = shelfIndex;
                final int finalRow = row;
                final int finalCol = col;

                JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "货位信息", true);
                dialog.setSize(340, 180);
                dialog.setLocationRelativeTo(null);
                dialog.setResizable(false);

                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BorderLayout(10, 10));
                contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                JTextArea infoText = new JTextArea();
                infoText.setEditable(false);
                infoText.setFont(new Font("Dialog", Font.PLAIN, 13));
                String info = String.format(
                        "该货位为空\n" +
                                "货架号：%d\n" +
                                "货位号：%d",
                        shelfShow,
                        posIndex
                );
                infoText.setText(info);
                contentPanel.add(infoText, BorderLayout.CENTER);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
                JButton btnAdd = new JButton("添加包裹");
                JButton btnClose = new JButton("关闭");
                btnPanel.add(btnAdd);
                btnPanel.add(btnClose);
                contentPanel.add(btnPanel, BorderLayout.SOUTH);

                dialog.add(contentPanel);

                btnAdd.addActionListener(ev -> {
                    dialog.dispose();
                    int uiShelf = finalShelfIndex + 1;
                    int uiRow = finalRow + 1;
                    int uiCol = finalCol + 1;
                    mainPanel.openAddParcelWithPos(uiShelf, uiRow, uiCol);
                });

                btnClose.addActionListener(ev -> dialog.dispose());
                dialog.setVisible(true);
                return;
            }

            // ==========非空货位逻辑==========
            final Parcel finalClickParcel = clickParcel;
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "货位信息", true);
            dialog.setSize(380, 240);
            dialog.setLocationRelativeTo(null);
            dialog.setResizable(false);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JTextArea infoText = new JTextArea();
            infoText.setEditable(false);
            infoText.setFont(new Font("Dialog", Font.PLAIN, 13));

            String info = String.format(
                    "包裹ID：%d\n" +
                            "取件码：%s\n" +
                            "收件人：%s\n" +
                            "状态：%s\n" +
                            "货架号：%d\n" +
                            "货位号：%d",
                    clickParcel.getId(),
                    clickParcel.getPickCode(),
                    clickParcel.getReceiverName(),
                    clickParcel.getStatus().getDescription(),
                    shelfShow,
                    posIndex
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
                if (finalClickParcel.getStatus() == ParcelStatus.TAKEN) {
                    JOptionPane.showMessageDialog(dialog, "该包裹已经出库，不能加入队列！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                PickTask task = new PickTask(System.currentTimeMillis(), finalClickParcel, System.currentTimeMillis(), 0);
                boolean ok = mainPanel.enqueueTask(task);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "✅已成功加入取件队列");
                    mainPanel.refreshAll();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "❌队列已满，无法加入！", "提示", JOptionPane.WARNING_MESSAGE);
                }
            });

            btnClose.addActionListener(ev -> dialog.dispose());
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "读取货位异常：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font shelfTitleFont = new Font("Dialog", Font.BOLD, 14);
        Font cellNumFont = new Font("Dialog", Font.PLAIN, 10);

        int shelfUnitW = (CELL_W * Shelf.COL) + GAP * 8;
        int shelfUnitH = (CELL_H * Shelf.ROW) + GAP * 12;
        int startX = 12;
        int startY = 15;

        for (int shelfNo = 0; shelfNo < Shelf.SHELF_NUM; shelfNo++) {
            int baseX = startX;
            int baseY = startY + shelfNo * (shelfUnitH + SHELF_VERTICAL_GAP);

            g2d.setFont(shelfTitleFont);
            g2d.setColor(Color.BLACK);
            g2d.drawString("【货架" + (shelfNo + 1) + "】", baseX, baseY - 6);

            Parcel[] shelfParcelList = service.getShelfExpress(shelfNo);

            for (int r = 0; r < Shelf.ROW; r++) {
                for (int c = 0; c < Shelf.COL; c++) {
                    int cellX = baseX + GAP + c * (CELL_W + GAP);
                    int cellY = baseY + GAP + r * (CELL_H + GAP);

                    int posIndex = r * Shelf.COL + c + 1;

                    Parcel curParcel = null;
                    for (Parcel p : shelfParcelList) {
                        if (p.getShelf() == shelfNo
                                && p.getSlot().getRow().equals(r)
                                && p.getSlot().getCol().equals(c)) {
                            curParcel = p;
                            break;
                        }
                    }

                    // 空位灰色，在库绿色，超期橙色，异常红色，已出库灰色
                    if (curParcel != null) {
                        switch (curParcel.getStatus()) {
                            case IN_STOCK:
                                g2d.setColor(new Color(144, 238, 144));
                                break;
                            case EXPIRED:
                                g2d.setColor(new Color(255, 165, 0));
                                break;
                            case ABNORMAL:
                                g2d.setColor(new Color(255, 102, 102));
                                break;
                            case TAKEN:
                                g2d.setColor(Color.LIGHT_GRAY);
                                break;
                            default:
                                g2d.setColor(Color.LIGHT_GRAY);
                                break;
                        }
                    } else {
                        g2d.setColor(Color.LIGHT_GRAY);
                    }

                    // 查询高亮优先覆盖
                    if (highlightParcel != null
                            && highlightParcel.getShelf() == shelfNo
                            && highlightParcel.getSlot().getRow().equals(r)
                            && highlightParcel.getSlot().getCol().equals(c)) {
                        g2d.setColor(Color.YELLOW);
                    }

                    g2d.fillRect(cellX, cellY, CELL_W, CELL_H);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(cellX, cellY, CELL_W, CELL_H);

                    g2d.setFont(cellNumFont);
                    g2d.setColor(Color.BLACK);
                    FontMetrics fm = g2d.getFontMetrics();
                    String numStr = String.valueOf(posIndex);
                    int textX = cellX + (CELL_W - fm.stringWidth(numStr)) / 2;
                    int textY = cellY + (CELL_H + fm.getAscent()) / 2 - 2;
                    g2d.drawString(numStr, textX, textY);
                }
            }
        }

        //底部图例
        int legendY = getHeight() - 20;
        g2d.setFont(new Font("Dialog", Font.PLAIN, 12));

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(20, legendY, 18, 18);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(20, legendY, 18, 18);
        g2d.drawString("空位", 45, legendY + 13);

        g2d.setColor(new Color(144, 238, 144));
        g2d.fillRect(90, legendY, 18, 18);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(90, legendY, 18, 18);
        g2d.drawString("在库", 115, legendY + 13);

        g2d.setColor(new Color(255, 165, 0));
        g2d.fillRect(160, legendY, 18, 18);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(160, legendY, 18, 18);
        g2d.drawString("超期", 185, legendY + 13);

        g2d.setColor(new Color(255, 102, 102));
        g2d.fillRect(230, legendY, 18, 18);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(230, legendY, 18, 18);
        g2d.drawString("异常", 255, legendY + 13);

        g2d.setColor(Color.YELLOW);
        g2d.fillRect(300, legendY, 18, 18);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(300, legendY, 18, 18);
        g2d.drawString("查询高亮", 325, legendY + 13);
    }
}
