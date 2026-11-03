import javax.swing.JButton;
import java.awt.*;

class RoundButton extends JButton {
    private final Color base;

    RoundButton(Color base) {
        this.base = base;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusable(false);
        setRolloverEnabled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        boolean pressed = getModel().isPressed();
        int dy = pressed ? 3 : 0;

        // metal mounting ring
        g2.setPaint(new GradientPaint(0, 0, new Color(230, 235, 240), 0, h, new Color(120, 130, 140)));
        g2.fillOval(0, 0, w - 1, h - 1);

        // colored cap
        g2.setPaint(new RadialGradientPaint(
                w * 0.38f, (h * 0.32f) + dy, w * 0.8f,
                new float[]{0f, 0.55f, 1f},
                new Color[]{base.brighter(), base, base.darker()}
        ));
        g2.fillOval(3, 3 + dy, w - 7, h - 7);

        // glossy highlight
        g2.setColor(new Color(255, 255, 255, pressed ? 70 : 140));
        g2.fillOval(w / 4, h / 6 + dy, w / 2, h / 3);

        // rim
        g2.setColor(base.darker().darker());
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(4, 4 + dy, w - 9, h - 9);

        // pressed glow
        if (pressed) {
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 110));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(1, 1, w - 3, h - 3);
        }

        g2.dispose();
    }
}