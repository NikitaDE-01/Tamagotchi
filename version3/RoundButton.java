import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

final class RoundButton extends JButton {
    //Taste
    RoundButton(String label) {
        super(label);
        setFocusable(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    //Zeichnen
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean down = getModel().isArmed() || getModel().isPressed();
        int d = Math.min(getWidth(), getHeight()) - 8;
        int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2 + (down ? 2 : 0);
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(x + 1, y + (down ? 2 : 5), d, d);
        g.setPaint(new GradientPaint(x, y, down ? new Color(255, 225, 110) : new Color(255, 246, 175),
                x, y + d, new Color(236, 168, 38)));
        g.fillOval(x, y, d, d);
        g.setColor(new Color(120, 70, 10));
        g.setStroke(new BasicStroke(2f));
        g.drawOval(x, y, d, d);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(getText(), x + (d - fm.stringWidth(getText())) / 2, y + (d + fm.getAscent() - fm.getDescent()) / 2);
        g.dispose();
    }
}
