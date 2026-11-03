final class Art {
    static final int BG = Lcd.BG, DK = Lcd.DK, MD = Lcd.MD, WH = Lcd.WH;

    private Art() { }

    //Spiegeln
    static double cx(Pose p, double dx) { return p.x + p.dir * dx; }

    static int px(Pose p, int dx) { return p.dir > 0 ? p.x + dx : p.x - dx - 1; }

    //Snoopy
    static void snoopy(Lcd c, Pose p) {
        int gy = 44 - p.bob;
        //Schwanz
        int t0 = px(p, -8), t1 = px(p, -12);
        c.line(t0, gy - 9, t1, gy - 12 + p.tail * 3, DK);
        c.line(t0, gy - 8, t1, gy - 11 + p.tail * 3, DK);
        //Körper
        c.outlined(cx(p, 0), gy - 8, 7, 7.5, WH);
        //Halsband
        for (int dx = -3; dx <= 3; dx++) { c.set(px(p, dx), gy - 14, MD); c.set(px(p, dx), gy - 13, MD); }
        //Füße
        c.outlined(cx(p, -3), gy - 2, 3, 1.5, WH);
        c.outlined(cx(p, 4), gy - 2, 3, 1.5, WH);
        //Arm
        if (p.arm == 0) c.outlined(cx(p, 6), gy - 7, 1.5, 3, WH);
        else c.outlined(cx(p, 10), gy - 13, 1.5, 3, WH);
        //Kopf
        double hx = cx(p, 2), hy = gy - 22, sx = cx(p, 9), sy = gy - 20.5;
        c.ellipse(hx, hy, 8, 7, DK);
        c.ellipse(sx, sy, 6, 4, DK);
        c.ellipse(hx, hy, 7, 6, WH);
        c.ellipse(sx, sy, 5, 3, WH);
        //Nase
        c.ellipse(cx(p, 13), gy - 22, 2.5, 2, DK);
        //Ohr
        c.ellipse(cx(p, -3), gy - 22, 2.5, 3.5, DK);
        c.ellipse(cx(p, -3 + p.ear * 1.5), gy - 17, 2.5, 4.5, DK);
        //Auge
        int ex = px(p, 5), ey = gy - 25;
        switch (p.eyes) {
            case 0: c.set(ex, ey, DK); c.set(ex, ey + 1, DK); break;
            case 1: c.set(px(p, 4), ey + 1, DK); c.set(px(p, 5), ey + 1, DK); c.set(px(p, 6), ey + 1, DK); break;
            case 2: c.set(px(p, 4), ey + 2, DK); c.set(px(p, 5), ey + 1, DK); c.set(px(p, 6), ey + 2, DK); break;
            case 3:
                c.set(ex, ey + 1, DK); c.set(ex, ey + 2, DK);
                c.set(px(p, 3), ey - 2, DK); c.set(px(p, 4), ey - 1, DK); c.set(px(p, 5), ey - 1, DK);
                c.set(px(p, 6), ey, DK); c.set(px(p, 7), ey, DK);
                break;
            case 5:
                c.set(px(p, 4), ey, DK); c.set(px(p, 6), ey, DK); c.set(px(p, 5), ey + 1, DK);
                c.set(px(p, 4), ey + 2, DK); c.set(px(p, 6), ey + 2, DK);
                break;
            default:
                c.set(ex, ey + 1, DK); c.set(ex, ey + 2, DK);
                c.set(px(p, 3), ey, DK); c.set(px(p, 4), ey - 1, DK); c.set(px(p, 5), ey - 1, DK); c.set(px(p, 6), ey - 2, DK);
                c.set(ex, ey + 4, MD);
        }
        //Mund
        int my = gy - 19;
        switch (p.mouth) {
            case 1:
                c.rect(Math.min(px(p, 8), px(p, 11)), my - 1, 4, 3, DK);
                c.set(px(p, 9), my + 1, MD); c.set(px(p, 10), my + 1, MD);
                break;
            case 2:
                for (int dx = 8; dx <= 12; dx++) c.set(px(p, dx), my, DK);
                c.set(px(p, 7), my - 1, DK); c.set(px(p, 13), my - 1, DK);
                break;
            case 3:
                for (int dx = 8; dx <= 12; dx++) c.set(px(p, dx), my, DK);
                c.set(px(p, 7), my + 1, DK); c.set(px(p, 13), my + 1, DK);
                break;
            default:
                for (int dx = 8; dx <= 11; dx++) c.set(px(p, dx), my, DK);
        }
    }

    //Woodstock
    static void woodstock(Lcd c, int x, int y, int flap, int dir) {
        c.outlined(x, y, 3, 2.5, MD);
        c.line(x - dir, y, x - dir * 3, flap == 0 ? y - 4 : y + 2, DK);
        c.set(x, y - 4, DK); c.set(x + dir, y - 5, DK);
        c.set(x + dir * 4, y, DK); c.set(x + dir * 5, y, DK);
        c.set(x + dir * 2, y - 1, DK);
        c.set(x - 1, y + 3, DK); c.set(x + 1, y + 3, DK);
    }

    //Busch
    static void bush(Lcd c, int x) {
        c.outlined(x, 41, 5, 3.5, MD);
        c.set(x - 2, 40, WH); c.set(x + 2, 39, WH); c.set(x, 42, WH);
    }

    //Kaka
    static void poop(Lcd c, int x, int y) {
        String[] r = {"..1..", ".111.", ".111.", "11111"};
        c.icon(x, y, r, DK);
        if ((x + y) % 2 == 0) { c.set(x + 1, y - 2, MD); c.set(x + 3, y - 3, MD); }
    }

    //Napf
    static void bowl(Lcd c, int x, int y, double frac) {
        c.rect(x, y, 11, 1, DK); c.rect(x + 1, y + 1, 9, 1, DK);
        c.rect(x + 2, y + 2, 7, 1, DK); c.rect(x + 3, y + 3, 5, 1, DK);
        if (frac > 0.05) c.ellipse(x + 5.5, y, 4.5 * frac, 2.5 * frac + 0.5, MD);
    }

    //Pizza
    static void pizza(Lcd c, int x, int y, double frac) {
        int h = 9;
        for (int r = 0; r < h; r++) {
            int wi = (int) Math.round(11.0 * (1.0 - r / (double) h) * frac);
            if (wi <= 0) continue;
            int x0 = x + (11 - wi) / 2;
            c.rect(x0, y + r, wi, 1, r < 2 ? DK : MD);
            c.set(x0, y + r, DK); c.set(x0 + wi - 1, y + r, DK);
        }
        if (frac > 0.5) { c.set(x + 3, y + 3, WH); c.set(x + 7, y + 3, WH); c.set(x + 5, y + 5, WH); }
    }

    //Ei
    static void egg(Lcd c, int ox, int crack, boolean flash) {
        int fill = flash ? DK : WH, ink = flash ? WH : DK;
        c.ellipse(24 + ox, 33, 11, 11, DK);
        c.ellipse(24 + ox, 27, 9, 11, DK);
        c.ellipse(24 + ox, 33, 10, 10, fill);
        c.ellipse(24 + ox, 27, 8, 10, fill);
        c.ellipse(19 + ox, 35, 3, 2.5, ink);
        c.ellipse(29 + ox, 30, 2.5, 3, ink);
        c.ellipse(23 + ox, 41, 2, 1.2, MD);
        if (crack >= 1) { c.line(21 + ox, 22, 24 + ox, 25, ink); c.line(24 + ox, 25, 22 + ox, 28, ink); }
        if (crack >= 2) { c.line(24 + ox, 25, 28 + ox, 24, ink); c.line(28 + ox, 24, 30 + ox, 27, ink); }
        if (crack >= 3) {
            c.line(22 + ox, 28, 25 + ox, 31, ink); c.line(25 + ox, 31, 23 + ox, 35, ink);
            c.line(30 + ox, 27, 27 + ox, 30, ink);
        }
    }

    //Hütte
    static void doghouse(Lcd c) {
        for (int y = 26; y <= 38; y++) {
            double half = (y - 26) * (20.0 / 12.0);
            int x0 = (int) Math.round(24 - half), x1 = (int) Math.round(24 + half);
            c.rect(x0, y, x1 - x0 + 1, 1, MD);
            c.set(x0, y, DK); c.set(x1, y, DK);
        }
        c.rect(4, 38, 41, 1, DK);
        c.rect(8, 39, 33, 6, WH);
        c.rect(8, 39, 1, 6, DK); c.rect(40, 39, 1, 6, DK);
        c.ellipse(24, 45, 5, 6, DK);
        c.rect(0, 45, 48, 3, BG);
    }

    //Schlafzeichen
    static void zzz(Lcd c, int tick, int x, int y) {
        for (int k = 0; k < 3; k++) {
            int u = (tick / 3 + k * 4) % 12;
            c.text("Z", x + u, y - u / 3, DK);
        }
    }

    //Geist
    static void ghost(Lcd c, int dy, int tick) {
        Lcd tmp = new Lcd();
        tmp.clear();
        Pose p = new Pose();
        p.x = 24 + (int) Math.round(2 * Math.sin(tick * 0.3));
        p.bob = dy; p.eyes = 5; p.mouth = 2; p.arm = 1;
        p.ear = (tick / 3) % 2 * 2 - 1; p.tail = (tick / 3) % 2;
        snoopy(tmp, p);
        for (int y = 0; y < Lcd.H; y++)
            for (int x = 0; x < Lcd.W; x++)
                if (tmp.get(x, y) != BG && (x + y) % 2 == 0) c.set(x, y, tmp.get(x, y));
        int hx = p.x + 2, hy = 44 - dy - 31;
        c.ellipse(hx, hy, 6, 2, DK);
        c.ellipse(hx, hy, 4, 1, BG);
        int by = 44 - dy - 12, f = (tick / 2) % 2 * 3;
        c.line(p.x - 8, by, p.x - 14, by - 6 + f, DK);
        c.line(p.x - 14, by - 6 + f, p.x - 11, by - 1, DK);
        c.line(p.x + 8, by, p.x + 14, by - 6 + f, DK);
        c.line(p.x + 14, by - 6 + f, p.x + 11, by - 1, DK);
    }

    //Grab
    static void grave(Lcd c, int x, int y) {
        c.ellipse(x + 8, y + 8, 9, 9, DK);
        c.rect(x - 1, y + 8, 18, 10, DK);
        c.ellipse(x + 8, y + 8, 8, 8, MD);
        c.rect(x, y + 8, 16, 9, MD);
        c.rect(x + 8, y + 1, 1, 5, DK);
        c.rect(x + 6, y + 3, 5, 1, DK);
        c.text("RIP", x + 3, y + 9, DK);
        c.ellipse(x + 8, y + 18, 13, 3.5, DK);
        c.ellipse(x + 8, y + 18, 12, 2.5, WH);
    }

    //Blume
    static void flower(Lcd c, int x, int y) {
        c.rect(x, y + 1, 1, 3, DK);
        c.set(x - 1, y, DK); c.set(x + 1, y, DK); c.set(x, y - 1, DK);
        c.set(x, y, WH);
    }

    //Boden
    static void ground(Lcd c) {
        for (int x = 0; x < Lcd.W; x += 2) c.set(x, 45, DK);
    }

    //Blase
    static void ring(Lcd c, int x, int y) {
        c.set(x + 1, y, DK); c.set(x, y + 1, DK); c.set(x + 2, y + 1, DK); c.set(x + 1, y + 2, DK);
    }

    //Symbole
    static final String[][] ICONS = {
        {"0001000", "0010100", "0000000", "1111111", "0111110", "0011100", "0000000"},
        {"0011100", "0101010", "1001001", "1111111", "1001001", "0101010", "0011100"},
        {"0111100", "1110000", "1100000", "1100000", "1110000", "0111100", "0000000"},
        {"0001000", "0001000", "0011100", "0111110", "1111111", "0111110", "0011100"},
        {"0011100", "0011100", "0011100", "0011100", "0001000", "0000000", "0011100"},
        {"0110110", "1111111", "1111111", "0111110", "0011100", "0001000", "0000000"}
    };

    //Menüleiste
    static void strip(Lcd c, int active, boolean filled) {
        for (int i = 0; i < 6; i++) {
            int x0 = i * 8;
            if (i == active && filled) { c.rect(x0, 0, 8, 9, DK); c.icon(x0 + 1, 1, ICONS[i], BG); }
            else c.icon(x0 + 1, 1, ICONS[i], DK);
        }
        c.rect(0, 9, Lcd.W, 1, DK);
    }
}
