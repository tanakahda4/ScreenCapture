package com.tanakahda;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main implements MouseListener, MouseMotionListener {

    private JFrame mainFrame;

    private Container contentPane;

    private JPanel buttonPanel;

    private JButton captureButton;

    /** スクリーンキャプチャ時に画面に重ねて、paintPanelを上に載せて矩形を描く */
    private JFrame overlayFrame;
    /** スクリーンキャプチャ時に矩形を描くクラス。このクラスのインナークラスとして定義する。 */
    private PaintPanel paintPanel;

    // 範囲指定した開始座標
    private Point startPoint;
    // 範囲指定した終了座標
    private Point endPoint;

    private int mainFramePositionX;

    private int mainFramePositionY;

    /**
     * メインルーチン
     * @param args
     */
    public static void main(String[] args) {
        new Main();
    }

    /**
     * コンストラクタ
     */
    public Main(){
        createMainFrame();
    }

    /**
     * メインフレームを作成します。
     */
    private void createMainFrame() {
        mainFrame = new JFrame("Capture");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 300);
        mainFrame.setLocationRelativeTo(null);
        contentPane = mainFrame.getContentPane();
        contentPane.setLayout(null);

        captureButton = new JButton("Capture");
        captureButton.setBounds(400, 0, 90, 60);
        contentPane.add(captureButton);

        mainFrame.getRootPane();

        captureButton.addActionListener(e -> {
            beforeProcess();
            int x = mainFrame.getX();
            int y = mainFrame.getY();
            // 範囲指定するためにメインウィンドウを閉じる
            mainFrame.setVisible(false);
            mainFrame.setLocation(x, y);
        });

        buttonPanel = new JPanel();
        buttonPanel.setLayout(null);

        contentPane.add(buttonPanel);
        buttonPanel.setBounds(0,180,500,200);

        buttonPanel.add(captureButton);
        mainFrame.setVisible(true);
    }

    /**
     * 画面キャプチャの前処理を実行します。
     */
    public void beforeProcess() {
        // mainFrameの現在位置を記録しておく
        mainFramePositionX = mainFrame.getX();
        mainFramePositionY = mainFrame.getY();
        mainFrame.setLocation(-1000, -1000);
        JFrame.setDefaultLookAndFeelDecorated(false);

        overlayFrame = new JFrame();
        overlayFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        overlayFrame.setSize(screenSize.width, screenSize.height);

        paintPanel = new PaintPanel();
        paintPanel.setBackground(new Color(0, 0, 0, 0));
        paintPanel.addMouseListener(this);
        paintPanel.addMouseMotionListener(this);

        overlayFrame.getContentPane().add(paintPanel);
        // フレームをWindowsの中央に移動する
        overlayFrame.setLocationRelativeTo(null);
        overlayFrame.setUndecorated(true);
        //半透明化
        overlayFrame.setBackground(new Color(0x0, true));
        overlayFrame.addMouseListener(this);
        overlayFrame.addMouseMotionListener(this);
        overlayFrame.setVisible(true);
    }

    /**
     * 画面キャプチャの後処理を実行します。
     */
    public void afterProcess() {
        // 半透明ウィンドウを終了
        overlayFrame.dispose();
        //File filepath = FilePathCreate();
        var timestamp = new Timestamp(System.currentTimeMillis());

        try {
            var dir = new File(Constants.OUTPUT_DIR + "/" + timestamp + ".png");
            new ScreenCapture().execute(dir, startPoint, endPoint);
        } catch (AWTException | IOException e1) {
            e1.printStackTrace();
        }
        // 範囲指定した座標を表示
        System.out.println("startPoint = " + startPoint + " endPoint = " + endPoint);
        // 自分を表示(元の位置に移動)
        mainFrame.setLocation(mainFramePositionX, mainFramePositionY);
        // メインウィンドウを表示
        mainFrame.setVisible(true);
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        int x = (int)paintPanel.draggingRect.getX();
        int y = (int)paintPanel.draggingRect.getY();
        paintPanel.draggingRect.setSize(e.getX() - x, e.getY() - y);
        paintPanel.repaint();

    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // 範囲指定した開始点を記録
        startPoint = e.getPoint();
        paintPanel.draggingRect.setBounds(e.getX(), e.getY(), 0, 0);
        paintPanel.isDragging = true;
        paintPanel.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // 範囲指定した終了点を記録
        endPoint = e.getPoint();
        // 後処理
        paintPanel.isDragging = false;
        Dimension rectSize = paintPanel.draggingRect.getSize();
        if (rectSize.getWidth() > 0 && rectSize.getHeight() > 0) {
            paintPanel.rects.add(new Rectangle(paintPanel.draggingRect));
        }
        paintPanel.draggingRect.setBounds(0, 0, 0, 0);
        paintPanel.repaint();

        afterProcess();
    }

    /**
     *　マウスドラッグで矩形を描画するクラスです。
     */
    final class PaintPanel extends JPanel {

        public List<Rectangle> rects;

        public Rectangle draggingRect;

        /** ドラッグしている状態の真偽値 */
        public volatile boolean isDragging;

        /**
         * コンストラクタ
         */
        PaintPanel() {
            this.rects = Collections.synchronizedList(new ArrayList<>());
            this.draggingRect = new Rectangle(0, 0);
            this.isDragging = false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setXORMode(Color.RED);
            if (isDragging) {
                g.drawRect(draggingRect.x, draggingRect.y, draggingRect.width, draggingRect.height);
            }
            for (Rectangle r : rects) {
                g.drawRect(r.x, r.y, r.width, r.height);
            }
        }
    }
}