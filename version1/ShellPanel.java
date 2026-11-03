import javax.swing.JPanel;
import java.awt.*;
import java.util.Random;

class ShellPanel extends JPanel {
    ShellPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape shell = TamagotchiFrame.createWindowShape(getWidth(), getHeight());

        // radial plastic body
        g2.setPaint(new RadialGradientPaint(
                getWidth() * 0.35f, getHeight() * 0.28f, getWidth() * 1.05f,
                new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(185, 230, 255), new Color(110, 180, 225), new Color(55, 115, 165)}
        ));
        g2.fill(shell);

        // plastic texture + inner rim shading (clipped to shell)
        Shape oldClip = g2.getClip();
        g2.clip(shell);

        Random rnd = new Random(42);
        for (int i = 0; i < 220; i++) {
            int x = rnd.nextInt(getWidth());
            int y = rnd.nextInt(getHeight());
            g2.setColor(rnd.nextBoolean()
                    ? new Color(255, 255, 255, 10)
                    : new Color(0, 40, 80, 10));
            g2.fillOval(x, y, 2, 2);
        }

        g2.setColor(new Color(20, 60, 100, 70));
        g2.setStroke(new BasicStroke(16));
        g2.draw(shell);

        // glossy highlights
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(getWidth() / 7, getHeight() / 16, getWidth() / 3, getHeight() / 6);
        g2.setColor(new Color(255, 255, 255, 55));
        g2.fillOval(getWidth() / 5, getHeight() / 12, getWidth() / 5, getHeight() / 9);
        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillOval(getWidth() - 130, getHeight() / 3, 60, 120);

        // seam groove
        g2.setColor(new Color(30, 75, 115, 100));
        g2.setStroke(new BasicStroke(3));
        g2.drawArc(20, 380, getWidth() - 40, 160, 15, 150);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.drawArc(20, 384, getWidth() - 40, 160, 15, 150);

        // button drop shadows
        g2.setColor(new Color(10, 40, 70, 80));
        g2.fillOval(126, 562, 52, 52);
        g2.fillOval(206, 572, 52, 52);
        g2.fillOval(286, 562, 52, 52);

        // embossed logo
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 26));
        FontMetrics fm = g2.getFontMetrics();
        String logo = "TAMAGOTCHI";
        int lx = (getWidth() - fm.stringWidth(logo)) / 2;
        g2.setColor(new Color(20, 60, 100, 180));
        g2.drawString(logo, lx + 1, 95);
        g2.drawString(logo, lx - 1, 93);
        g2.drawString(logo, lx, 96);
        g2.setColor(Color.WHITE);
        g2.drawString(logo, lx, 92);

        // metallic LCD bezel
        g2.setPaint(new GradientPaint(88, 112, new Color(252, 253, 255), 88, 358, new Color(160, 172, 186)));
        g2.fillRoundRect(88, 112, 284, 246, 36, 36);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(90, 114, 280, 242, 34, 34);
        g2.setColor(new Color(95, 105, 118));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(88, 112, 284, 246, 36, 36);

        // inner matte frame around LCD
        g2.setColor(new Color(35, 38, 44));
        g2.fillRoundRect(96, 120, 268, 230, 26, 26);

        // screws with slot
        int[][] screws = {{96, 120}, {356, 120}, {96, 348}, {356, 348}};
        for (int[] s : screws) {
            g2.setColor(new Color(150, 160, 172));
            g2.fillOval(s[0], s[1], 9, 9);
            g2.setColor(new Color(70, 80, 92));
            g2.drawLine(s[0] + 2, s[1] + 4, s[0] + 7, s[1] + 4);
        }

        // SNOOPY rainbow print with outline + shadow
        String name = "SNOOPY";
        Color[] colors = {
                new Color(225, 45, 45),
                new Color(255, 140, 0),
                new Color(255, 200, 0),
                new Color(60, 180, 60),
                new Color(60, 90, 220),
                new Color(160, 60, 200)
        };
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        for (int i = 0; i < name.length(); i++) {
            String ch = String.valueOf(name.charAt(i));
            int cx = 152 + i * 27;
            g2.setColor(new Color(20, 60, 100, 160));
            g2.drawString(ch, cx + 1, 403);
            g2.setColor(Color.WHITE);
            g2.drawString(ch, cx - 1, 400);
            g2.drawString(ch, cx + 1, 399);
            g2.drawString(ch, cx - 1, 401);
            g2.setColor(colors[i % colors.length]);
            g2.drawString(ch, cx, 400);
        }

        // glossy Snoopy sticker with heart
        g2.setColor(new Color(255, 255, 255, 225));
        g2.fillOval(52, 455, 70, 70);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(58, 460, 30, 16);
        g2.setColor(new Color(30, 50, 90));
        String[] sticker = SpriteLibrary.SNOOPY_IDLE_1;
        for (int r = 0; r < sticker.length; r++) {
            for (int c = 0; c < sticker[r].length(); c++) {
                if (sticker[r].charAt(c) == '#') {
                    g2.fillRect(58 + c * 3, 461 + r * 3, 3, 3);
                }
            }
        }
        g2.setColor(new Color(220, 40, 50));
        g2.fillOval(108, 465, 10, 10);
        g2.fillOval(116, 465, 10, 10);
        g2.fillPolygon(new int[]{108, 126, 117}, new int[]{471, 471, 487}, 3);

        // speaker holes with inner shadow
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                g2.setColor(new Color(20, 60, 95));
                g2.fillOval(352 + c * 12, 596 + r * 12, 7, 7);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawArc(352 + c * 12, 596 + r * 12, 7, 7, 180, 180);
            }
        }

        // embossed button letters
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawString("A", 146, 628);
        g2.drawString("B", 226, 638);
        g2.drawString("C", 306, 628);
        g2.setColor(new Color(10, 20, 35));
        g2.drawString("A", 146, 626);
        g2.drawString("B", 226, 636);
        g2.drawString("C", 306, 626);

        g2.setClip(oldClip);

        // gold trim + outer rim
        g2.setColor(new Color(255, 220, 140, 110));
        g2.setStroke(new BasicStroke(2));
        g2.draw(shell);
        g2.setColor(new Color(30, 75, 115, 235));
        g2.setStroke(new BasicStroke(6));
        g2.draw(shell);

        g2.dispose();
    }
}