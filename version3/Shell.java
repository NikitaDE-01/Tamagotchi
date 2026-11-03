import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

final class Shell extends JPanel {
    static final int WIN_W = 340, WIN_H = 440, SCALE = 4, LX = 74, LY = 96;
    static final int[] PAL = {0xBECDA0, 0x232D23, 0x708260, 0xE8F0D6};
    static final int[] PAL_DARK = {0x69785A, 0x0F140F, 0x465540, 0x8C9B78};
    static final int[][] BTN_POS = {{92, 346}, {170, 366}, {248, 346}};

    final Game game;
    final Lcd lcd = new Lcd();
    final BufferedImage img = new BufferedImage(Lcd.W, Lcd.H, BufferedImage.TYPE_INT_RGB);
    final RoundButton[] btn = new RoundButton[3];
    final Shape egg = eggShape(WIN_W, WIN_H);
    private Point dragStart;

    //Eiform
    static Shape eggShape(double w, double h) {
        Path2D p = new Path2D.Double();
        p.moveTo(w / 2, 4);
        p.curveTo(w * 0.82, 4, w - 4, h * 0.52, w - 4, h * 0.66);
        p.curveTo(w - 4, h * 0.88, w * 0.76, h - 4, w / 2, h - 4);
        p.curveTo(w * 0.24, h - 4, 4, h * 0.88, 4, h * 0.66);
        p.curveTo(4, h * 0.52, w * 0.18, 4, w / 2, 4);
        p.closePath();
        return p;
    }

    Shell(Game game, JFrame frame) {
        this.game = game;
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(WIN_W, WIN_H));

        //Tasten
        String[] labels = {"A", "B", "C"};
        for (int i = 0; i < 3; i++) {
            final int k = i;
            btn[i] = new RoundButton(labels[i]);
            btn[i].setBounds(BTN_POS[i][0] - 28, BTN_POS[i][1] - 28, 56, 56);
            btn[i].addActionListener(e -> game.press(k));
            add(btn[i]);
        }
        bind(KeyEvent.VK_A, 0); bind(KeyEvent.VK_LEFT, 0); bind(KeyEvent.VK_1, 0);
        bind(KeyEvent.VK_B, 1); bind(KeyEvent.VK_ENTER, 1); bind(KeyEvent.VK_SPACE, 1); bind(KeyEvent.VK_2, 1);
        bind(KeyEvent.VK_C, 2); bind(KeyEvent.VK_RIGHT, 2); bind(KeyEvent.VK_BACK_SPACE, 2); bind(KeyEvent.VK_3, 2);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quit");
        getActionMap().put("quit", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { System.exit(0); }
        });

        //Menü
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mute = new JMenuItem("Ton an/aus");
        mute.addActionListener(e -> Sound.toggle());
        JMenuItem restart = new JMenuItem("Neues Ei (Neustart)");
        restart.addActionListener(e -> { game.reset(); Save.write(game); });
        JMenuItem quit = new JMenuItem("Beenden");
        quit.addActionListener(e -> System.exit(0));
        menu.add(mute);
        menu.add(restart);
        menu.addSeparator();
        menu.add(quit);

        //Maus
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) menu.show(Shell.this, e.getX(), e.getY());
                else dragStart = e.getPoint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) menu.show(Shell.this, e.getX(), e.getY());
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (frame != null && dragStart != null && SwingUtilities.isLeftMouseButton(e))
                    frame.setLocation(e.getXOnScreen() - dragStart.x, e.getYOnScreen() - dragStart.y);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    //Tastatur
    private void bind(int key, int idx) {
        String name = "btn" + idx + "_" + key;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { btn[idx].doClick(70); }
        });
    }

    @Override
    //Zeichnen
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        //Schale
        g.setPaint(new GradientPaint(0, 0, new Color(255, 118, 98), WIN_W, WIN_H, new Color(186, 28, 52)));
        g.fill(egg);
        Shape oldClip = g.getClip();
        g.clip(egg);
        g.setPaint(new GradientPaint(0, 20, new Color(255, 255, 255, 90), 0, 200, new Color(255, 255, 255, 0)));
        g.fill(new Ellipse2D.Double(36, 14, 150, 190));
        g.setColor(new Color(255, 255, 255, 38));
        for (int i = 0; i < 3; i++) {
            int px = 60 + i * 110, py = 398 - (i == 1 ? 14 : 0);
            g.fillOval(px, py, 18, 14);
            g.fillOval(px - 5, py - 9, 7, 8);
            g.fillOval(px + 3, py - 12, 7, 8);
            g.fillOval(px + 11, py - 9, 7, 8);
        }
        g.setClip(oldClip);
        g.setColor(new Color(105, 10, 28));
        g.setStroke(new BasicStroke(5f));
        g.draw(egg);

        //Titel
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 22));
        FontMetrics fm = g.getFontMetrics();
        String title = "SnoopyGotchi";
        int tx = (WIN_W - fm.stringWidth(title)) / 2;
        g.setColor(new Color(105, 10, 28, 160));
        g.drawString(title, tx + 2, 64);
        g.setColor(new Color(255, 248, 235));
        g.drawString(title, tx, 62);

        //Rahmen
        g.setColor(new Color(58, 58, 70));
        g.fillRoundRect(LX - 14, LY - 14, 192 + 28, 192 + 28, 36, 36);
        g.setColor(new Color(150, 150, 165));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(LX - 14, LY - 14, 192 + 28, 192 + 28, 36, 36);

        //Display
        game.render(lcd);
        int[] pal = game.dark() ? PAL_DARK : PAL;
        for (int y = 0; y < Lcd.H; y++)
            for (int x = 0; x < Lcd.W; x++)
                img.setRGB(x, y, pal[lcd.px[y * Lcd.W + x]]);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, LX, LY, Lcd.W * SCALE, Lcd.H * SCALE, null);
        g.setColor(new Color(0, 0, 0, 20));
        g.setStroke(new BasicStroke(1f));
        for (int i = 1; i < Lcd.W; i++) {
            g.drawLine(LX + i * SCALE, LY, LX + i * SCALE, LY + Lcd.H * SCALE);
            g.drawLine(LX, LY + i * SCALE, LX + Lcd.W * SCALE, LY + i * SCALE);
        }
        g.setColor(new Color(0, 0, 0, 110));
        g.drawRect(LX - 1, LY - 1, 193, 193);
        g.setPaint(new GradientPaint(LX, LY, new Color(255, 255, 255, 45), LX, LY + 90, new Color(255, 255, 255, 0)));
        g.fillRect(LX, LY, 192, 90);

        //Alter
        String age = game.age();
        if (!age.isEmpty()) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            fm = g.getFontMetrics();
            String label = (game.s == Game.S.DEAD ? "LEBTE " : "ALTER ") + age;
            g.setColor(new Color(255, 240, 225));
            g.drawString(label, (WIN_W - fm.stringWidth(label)) / 2, 324);
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        fm = g.getFontMetrics();
        //Beschriftung
        String[] cap = {"W\u00C4HLEN", "OK", "ZUR\u00DCCK"};
        g.setColor(new Color(255, 240, 225));
        for (int i = 0; i < 3; i++)
            g.drawString(cap[i], BTN_POS[i][0] - fm.stringWidth(cap[i]) / 2, BTN_POS[i][1] + 44);
        g.dispose();
    }
}
