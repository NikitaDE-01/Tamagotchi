import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SnoopyGotchi - ein Tamagotchi-Automat mit Snoopy als Haustier.
 *
 * Bedienung (nur 3 Tasten, auch per Tastatur: A/B/C bzw. 1/2/3 bzw. Pfeil links/Enter/Pfeil rechts):
 *   Startbildschirm (Ei):  B = Ei ausbruten
 *   Hauptbildschirm:       A = naechstes Symbol waehlen (Piepston)
 *                          B = gewaehltes Symbol / Modus starten
 *                          C = Ton an/aus
 *   In einem Modus:        C = zurueck zum Hauptbildschirm
 *                          A/B = modusabhaengig (siehe Anzeige)
 *
 * Modi: ESSEN, SPIELEN, SCHLAFEN, SAUBER, DISZIPLIN und STATUS.
 * Das Fenster ist eiformig (rahmenlos, verschiebbar per Maus, Rechtsklick = Menue, ESC = Beenden).
 *
 * Aufbau der Datei:
 *   Lcd      - kleine Pixel-Leinwand (48x48 "Hardware"-Pixel) mit Zeichenfunktionen und 3x5-Pixelschrift
 *   Art      - Pixelgrafiken (Snoopy, Woodstock, Ei, Hundehuette, ...)
 *   Sound    - synthetisierte Piepsgeraeusche (javax.sound.sampled)
 *   Game     - der eigentliche Automat (Zustaende, Beduerfnisse, Animationen)
 *   Shell    - das eiformige Gehaeuse mit Display (JPanel) und drei Tasten (JButton)
 */
public class SnoopyGotchi {

    // Farbindizes der 4-Farben-LCD-Palette
    static final int BG = 0, DK = 1, MD = 2, WH = 3;

    // =====================================================================================
    //  LCD: Pixelleinwand
    // =====================================================================================
    static final class Lcd {
        static final int W = 48, H = 48;
        final int[] px = new int[W * H];

        private static final String[] FONT_DATA = {
            "A010101111101101", "B110101110101110", "C011100100100011", "D110101101101110",
            "E111100110100111", "F111100110100100", "G011100101101011", "H101101111101101",
            "I111010010010111", "J001001001101010", "K101101110101101", "L100100100100111",
            "M101111111101101", "N111101101101101", "O010101101101010", "P110101110100100",
            "Q010101101111011", "R110101110101101", "S011100010001110", "T111010010010010",
            "U101101101101111", "V101101101101010", "W101101111111101", "X101101010101101",
            "Y101101010010010", "Z111001010100111", "0111101101101111", "1010110010010111",
            "2110001010100111", "3110001010001110", "4101101111001001", "5111100110001110",
            "6011100111101111", "7111001010010010", "8111101111101111", "9111101111001110",
            "!010010010000010", ".000000000000010", "-000000111000000", ":000010000010000",
            "?110001010000010", " 000000000000000"
        };
        private static final Map<Character, String> FONT = new HashMap<>();
        static {
            for (String s : FONT_DATA) FONT.put(s.charAt(0), s.substring(1));
        }

        void clear() { Arrays.fill(px, BG); }

        void set(int x, int y, int c) {
            if (x >= 0 && y >= 0 && x < W && y < H) px[y * W + x] = c;
        }

        int get(int x, int y) {
            return (x >= 0 && y >= 0 && x < W && y < H) ? px[y * W + x] : BG;
        }

        void rect(int x, int y, int w, int h, int c) {
            for (int j = 0; j < h; j++) for (int i = 0; i < w; i++) set(x + i, y + j, c);
        }

        void line(int x0, int y0, int x1, int y1, int c) {
            int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
            int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
            while (true) {
                set(x0, y0, c);
                if (x0 == x1 && y0 == y1) break;
                int e2 = 2 * err;
                if (e2 >= dy) { err += dy; x0 += sx; }
                if (e2 <= dx) { err += dx; y0 += sy; }
            }
        }

        void ellipse(double cx, double cy, double rx, double ry, int c) {
            for (int y = (int) Math.floor(cy - ry); y <= (int) Math.ceil(cy + ry); y++)
                for (int x = (int) Math.floor(cx - rx); x <= (int) Math.ceil(cx + rx); x++) {
                    double dx = (x + 0.5 - cx) / rx, dy = (y + 0.5 - cy) / ry;
                    if (dx * dx + dy * dy <= 1.0) set(x, y, c);
                }
        }

        /** Gefuellte Ellipse mit 1 Pixel dunklem Rand. */
        void outlined(double cx, double cy, double rx, double ry, int fill) {
            ellipse(cx, cy, rx + 1, ry + 1, DK);
            ellipse(cx, cy, rx, ry, fill);
        }

        void icon(int x, int y, String[] rows, int c) {
            for (int r = 0; r < rows.length; r++)
                for (int i = 0; i < rows[r].length(); i++)
                    if (rows[r].charAt(i) == '1') set(x + i, y + r, c);
        }

        void text(String s, int x, int y, int c) {
            for (char ch : s.toUpperCase().toCharArray()) {
                String g = FONT.get(ch);
                if (g != null)
                    for (int r = 0; r < 5; r++)
                        for (int i = 0; i < 3; i++)
                            if (g.charAt(r * 3 + i) == '1') set(x + i, y + r, c);
                x += 4;
            }
        }

        static int textW(String s) { return s.length() * 4 - 1; }

        void textC(String s, int y, int c) { text(s, (W - textW(s)) / 2, y, c); }

        void heart(int x, int y, boolean filled) {
            String[] h = {".#.#.", "#####", ".###.", "..#.."};
            for (int r = 0; r < 4; r++)
                for (int i = 0; i < 5; i++)
                    if (h[r].charAt(i) == '#') set(x + i, y + r, DK);
            if (!filled) { set(x + 1, y + 1, BG); set(x + 3, y + 1, BG); set(x + 2, y + 2, BG); }
        }
    }

    // =====================================================================================
    //  Pixelgrafiken
    // =====================================================================================
    /** Haltung/Ausdruck von Snoopy: wird pro Frame neu gesetzt. */
    static final class Pose {
        int x = 22;      // Mittelpunkt des Koerpers
        int dir = 1;     // 1 = schaut nach rechts, -1 = nach links
        int bob = 0;     // Hoehe ueber dem Boden (Huepfen)
        int eyes = 0;    // 0 offen, 1 zu, 2 froehlich (^), 3 boese, 4 traurig
        int mouth = 0;   // 0 neutral, 1 offen, 2 Laecheln, 3 traurig
        int ear = 0;     // -1/0/1: Ohr schwingt
        int tail = 0;    // 0/1: Schwanz wedelt
        int arm = 0;     // 0 unten, 1 oben
    }

    static double cx(Pose p, double dx) { return p.x + p.dir * dx; }

    static int px(Pose p, int dx) { return p.dir > 0 ? p.x + dx : p.x - dx - 1; }

    static void snoopy(Lcd c, Pose p) {
        int gy = 44 - p.bob;
        // Schwanz
        int t0 = px(p, -8), t1 = px(p, -12);
        c.line(t0, gy - 9, t1, gy - 12 + p.tail * 3, DK);
        c.line(t0, gy - 8, t1, gy - 11 + p.tail * 3, DK);
        // Koerper
        c.outlined(cx(p, 0), gy - 8, 7, 7.5, WH);
        // rotes Halsband (hier mittelgrau)
        for (int dx = -3; dx <= 3; dx++) { c.set(px(p, dx), gy - 14, MD); c.set(px(p, dx), gy - 13, MD); }
        // Fuesse
        c.outlined(cx(p, -3), gy - 2, 3, 1.5, WH);
        c.outlined(cx(p, 4), gy - 2, 3, 1.5, WH);
        // Arm
        if (p.arm == 0) c.outlined(cx(p, 6), gy - 7, 1.5, 3, WH);
        else c.outlined(cx(p, 10), gy - 13, 1.5, 3, WH);
        // Kopf + Schnauze (erst beide Umrisse, dann beide Fuellungen -> keine Linie dazwischen)
        double hx = cx(p, 2), hy = gy - 22, sx = cx(p, 9), sy = gy - 20.5;
        c.ellipse(hx, hy, 8, 7, DK);
        c.ellipse(sx, sy, 6, 4, DK);
        c.ellipse(hx, hy, 7, 6, WH);
        c.ellipse(sx, sy, 5, 3, WH);
        // grosse schwarze Nase
        c.ellipse(cx(p, 13), gy - 22, 2.5, 2, DK);
        // langes schwarzes Schlappohr
        c.ellipse(cx(p, -3), gy - 22, 2.5, 3.5, DK);
        c.ellipse(cx(p, -3 + p.ear * 1.5), gy - 17, 2.5, 4.5, DK);
        // Auge
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
            default:
                c.set(ex, ey + 1, DK); c.set(ex, ey + 2, DK);
                c.set(px(p, 3), ey, DK); c.set(px(p, 4), ey - 1, DK); c.set(px(p, 5), ey - 1, DK); c.set(px(p, 6), ey - 2, DK);
                c.set(ex, ey + 4, MD);
        }
        // Mund
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

    /** Woodstock, Snoopys kleiner Vogelfreund. */
    static void woodstock(Lcd c, int x, int y, int flap, int dir) {
        c.outlined(x, y, 3, 2.5, MD);
        c.line(x - dir, y, x - dir * 3, flap == 0 ? y - 4 : y + 2, DK);
        c.set(x, y - 4, DK); c.set(x + dir, y - 5, DK);
        c.set(x + dir * 4, y, DK); c.set(x + dir * 5, y, DK);
        c.set(x + dir * 2, y - 1, DK);
        c.set(x - 1, y + 3, DK); c.set(x + 1, y + 3, DK);
    }

    static void bush(Lcd c, int x) {
        c.outlined(x, 41, 5, 3.5, MD);
        c.set(x - 2, 40, WH); c.set(x + 2, 39, WH); c.set(x, 42, WH);
    }

    static void poop(Lcd c, int x, int y) {
        String[] r = {"..1..", ".111.", ".111.", "11111"};
        c.icon(x, y, r, DK);
        if ((x + y) % 2 == 0) { c.set(x + 1, y - 2, MD); c.set(x + 3, y - 3, MD); }
    }

    static void bowl(Lcd c, int x, int y, double frac) {
        c.rect(x, y, 11, 1, DK); c.rect(x + 1, y + 1, 9, 1, DK);
        c.rect(x + 2, y + 2, 7, 1, DK); c.rect(x + 3, y + 3, 5, 1, DK);
        if (frac > 0.05) c.ellipse(x + 5.5, y, 4.5 * frac, 2.5 * frac + 0.5, MD);
    }

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

    static void egg(Lcd c, int ox, int crack, boolean flash) {
        int fill = flash ? DK : WH, ink = flash ? WH : DK;
        c.ellipse(24 + ox, 33, 11, 11, DK);
        c.ellipse(24 + ox, 27, 9, 11, DK);
        c.ellipse(24 + ox, 33, 10, 10, fill);
        c.ellipse(24 + ox, 27, 8, 10, fill);
        c.ellipse(19 + ox, 35, 3, 2.5, ink);     // "Snoopy-Flecken"
        c.ellipse(29 + ox, 30, 2.5, 3, ink);
        c.ellipse(23 + ox, 41, 2, 1.2, MD);
        if (crack >= 1) { c.line(21 + ox, 22, 24 + ox, 25, ink); c.line(24 + ox, 25, 22 + ox, 28, ink); }
        if (crack >= 2) { c.line(24 + ox, 25, 28 + ox, 24, ink); c.line(28 + ox, 24, 30 + ox, 27, ink); }
        if (crack >= 3) {
            c.line(22 + ox, 28, 25 + ox, 31, ink); c.line(25 + ox, 31, 23 + ox, 35, ink);
            c.line(30 + ox, 27, 27 + ox, 30, ink);
        }
    }

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
        c.ellipse(24, 45, 5, 6, DK);        // Eingang
        c.rect(0, 45, 48, 3, BG);
    }

    static void zzz(Lcd c, int tick, int x, int y) {
        for (int k = 0; k < 3; k++) {
            int u = (tick / 3 + k * 4) % 12;
            c.text("Z", x + u, y - u / 3, DK);
        }
    }

    static void ground(Lcd c) {
        for (int x = 0; x < Lcd.W; x += 2) c.set(x, 45, DK);
    }

    static void ring(Lcd c, int x, int y) {
        c.set(x + 1, y, DK); c.set(x, y + 1, DK); c.set(x + 2, y + 1, DK); c.set(x + 1, y + 2, DK);
    }

    static final String[][] ICONS = {
        {"0001000", "0010100", "0000000", "1111111", "0111110", "0011100", "0000000"},   // Napf
        {"0011100", "0101010", "1001001", "1111111", "1001001", "0101010", "0011100"},   // Ball
        {"0111100", "1110000", "1100000", "1100000", "1110000", "0111100", "0000000"},   // Mond
        {"0001000", "0001000", "0011100", "0111110", "1111111", "0111110", "0011100"},   // Tropfen
        {"0011100", "0011100", "0011100", "0011100", "0001000", "0000000", "0011100"},   // Ausrufezeichen
        {"0110110", "1111111", "1111111", "0111110", "0011100", "0001000", "0000000"}    // Herz
    };

    static void strip(Lcd c, int active, boolean filled) {
        for (int i = 0; i < 6; i++) {
            int x0 = i * 8;
            if (i == active && filled) { c.rect(x0, 0, 8, 9, DK); c.icon(x0 + 1, 1, ICONS[i], BG); }
            else c.icon(x0 + 1, 1, ICONS[i], DK);
        }
        c.rect(0, 9, Lcd.W, 1, DK);
    }

    // =====================================================================================
    //  Ton: synthetisierte Piepser
    // =====================================================================================
    static final class Sound {
        static final float RATE = 22050f;
        static final AudioFormat FMT = new AudioFormat(RATE, 16, 1, true, false);
        static final Random R = new Random();
        static volatile boolean on = true;
        static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sound");
            t.setDaemon(true);
            return t;
        });

        /** Ein Ton: Startfrequenz, Endfrequenz, Dauer (ms), Wellenform (0 Rechteck, 1 Dreieck, 2 Rauschen, 3 Sinus). */
        static double[] n(double f0, double f1, double ms, int wave) { return new double[]{f0, f1, ms, wave}; }

        static double[] rest(double ms) { return n(0, 0, ms, 0); }

        static byte[] build(double[]... notes) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (double[] nt : notes) {
                int len = (int) (RATE * nt[2] / 1000.0);
                int wave = (int) nt[3];
                double phase = 0;
                for (int i = 0; i < len; i++) {
                    double u = (double) i / len;
                    double f = nt[0] + (nt[1] - nt[0]) * u;
                    double env = Math.min(1.0, Math.min(i / 60.0, (len - i) / 200.0));
                    double v = 0;
                    if (wave == 2) v = R.nextDouble() * 2 - 1;
                    else if (nt[0] > 0 || nt[1] > 0) {
                        phase += f / RATE;
                        if (phase >= 1) phase -= 1;
                        if (wave == 0) v = phase < 0.5 ? 1 : -1;
                        else if (wave == 1) v = 4 * Math.abs(phase - 0.5) - 1;
                        else v = Math.sin(2 * Math.PI * phase);
                    }
                    short s = (short) (v * env * 0.22 * 32767);
                    out.write(s & 0xFF);
                    out.write((s >> 8) & 0xFF);
                }
            }
            return out.toByteArray();
        }

        static final byte[] SELECT = build(n(1046, 1046, 45, 0));
        static final byte[] BACK = build(n(988, 988, 50, 0), rest(15), n(660, 660, 70, 0));
        static final byte[][] ENTER = new byte[6][];
        static {
            for (int i = 0; i < 6; i++) {
                double f = 600 + i * 90;
                ENTER[i] = build(n(f, f, 60, 0), rest(20), n(f * 1.5, f * 1.5, 90, 0));
            }
            // Schlafen: sanftes Wiegenlied-Motiv
            ENTER[2] = build(n(659, 659, 150, 3), n(523, 523, 150, 3), n(392, 392, 320, 3));
        }
        static final byte[] YIP = build(n(900, 1500, 70, 0), rest(25), n(1100, 1800, 90, 0));
        static final byte[] BARK = build(n(520, 260, 90, 0), rest(40), n(520, 260, 110, 0));
        static final byte[] NO = build(n(300, 200, 120, 0), rest(30), n(300, 190, 170, 0));
        static final byte[] SCOLD = build(n(420, 210, 110, 1), rest(30), n(440, 180, 160, 1));
        static final byte[] MUNCH = build(n(0, 0, 45, 2), rest(10), n(180, 120, 30, 0));
        static final byte[] SNORE = build(n(110, 80, 350, 1), rest(60), n(90, 70, 300, 1));
        static final byte[] YAWN = build(n(800, 300, 500, 3));
        static final byte[] CHIRP = build(n(2200, 3000, 35, 3), n(3000, 2400, 35, 3), n(2400, 3200, 35, 3),
                rest(45), n(2600, 3300, 35, 3), n(3300, 2500, 40, 3));
        static final byte[] WIN = build(n(523, 523, 90, 0), n(659, 659, 90, 0), n(784, 784, 90, 0), n(1047, 1047, 220, 0));
        static final byte[] LOSE = build(n(392, 392, 140, 0), n(330, 330, 140, 0), n(262, 262, 300, 1));
        static final byte[] ATT = build(n(1568, 1568, 70, 0), rest(60), n(1568, 1568, 70, 0), rest(60), n(1568, 1568, 70, 0));
        static final byte[] CRACK = build(n(0, 0, 30, 2), rest(10), n(900, 400, 25, 0));
        static final byte[] HATCH = build(n(523, 523, 100, 0), n(659, 659, 100, 0), n(784, 784, 100, 0), n(1047, 1047, 100, 0),
                n(784, 784, 80, 0), n(1047, 1047, 100, 0), n(1319, 1319, 260, 0));
        static final byte[] SPLASH = build(n(600, 1200, 40, 3), n(800, 1500, 40, 3), n(500, 1100, 40, 3),
                n(900, 1700, 40, 3), n(700, 1400, 40, 3));

        static void play(byte[] data) {
            if (!on) return;
            EX.submit(() -> {
                try {
                    Clip clip = AudioSystem.getClip();
                    clip.open(FMT, data, 0, data.length);
                    clip.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) clip.close(); });
                    clip.start();
                } catch (Exception | Error ex) {
                    // kein Audiogeraet vorhanden: still weiterlaufen
                }
            });
        }

        static void toggle() {
            on = !on;
            if (on) play(SELECT);
        }
    }

    // =====================================================================================
    //  Spiellogik: der Automat
    // =====================================================================================
    static final class Game {
        enum S { EGG, HATCH, HOME, FEED, PLAY, SLEEP, CLEAN, DISC, STATUS }

        static final S[] MODES = {S.FEED, S.PLAY, S.SLEEP, S.CLEAN, S.DISC, S.STATUS};
        static final String[] NAMES = {"ESSEN", "SPIELEN", "SCHLAFEN", "SAUBER", "DISZIPLIN", "STATUS"};
        static final int[] POOP_X = {3, 34, 41};

        final Random rnd = new Random();
        S s;
        int sel, tick, t;
        int hunger, fun, disc, energy, poops;            // jeweils 0..4 (Herzen)
        boolean naughty, attention;
        int hungerSec, funSec, energySec, poopIn, attSec, naughtySec, sleepSec;
        int labelT, muteT, happyT, woodT;
        // Essen
        int feedChoice, eatT, refuseT;
        // Spielen (Woodstock-Raten)
        int ps, pt, round, wins, hidden, guess;
        final boolean[] won = new boolean[3];
        // Saeubern / Disziplin
        int washT, scoldT, scoldRes;

        Game() { reset(); }

        void reset() {
            s = S.EGG; sel = 0; tick = 0; t = 0;
            hunger = 3; fun = 3; disc = 2; energy = 4; poops = 0;
            naughty = false; attention = false;
            hungerSec = funSec = energySec = attSec = naughtySec = sleepSec = 0;
            poopIn = -1; labelT = muteT = happyT = 0; woodT = -1;
            feedChoice = 0; eatT = -1; refuseT = 0;
            washT = -1; scoldT = -1; scoldRes = 0;
            newGame();
        }

        boolean dark() { return s == S.SLEEP; }

        // ------------------------------------------------------------ Eingabe
        void press(int b) {   // 0 = A, 1 = B, 2 = C
            switch (s) {
                case EGG:
                    if (b == 1) { s = S.HATCH; t = 0; Sound.play(Sound.ENTER[1]); }
                    else Sound.play(Sound.SELECT);
                    break;
                case HATCH:
                    break;
                case HOME:
                    if (b == 0) { sel = (sel + 1) % 6; labelT = 15; Sound.play(Sound.SELECT); }
                    else if (b == 1) enter(sel);
                    else { Sound.toggle(); muteT = 15; }
                    break;
                default:
                    modePress(b);
            }
        }

        void enter(int idx) {
            s = MODES[idx]; t = 0;
            Sound.play(Sound.ENTER[idx]);
            switch (s) {
                case FEED: feedChoice = 0; eatT = -1; refuseT = 0; break;
                case PLAY: newGame(); if (ps == 3) Sound.play(Sound.YAWN); break;
                case SLEEP: sleepSec = 0; break;
                case CLEAN: washT = -1; break;
                case DISC: scoldT = -1; scoldRes = 0; break;
                default: break;
            }
        }

        void leave() {
            s = S.HOME; t = 0;
            Sound.play(Sound.BACK);
        }

        void newGame() {
            round = 0; wins = 0; pt = 0; guess = 0;
            Arrays.fill(won, false);
            hidden = rnd.nextInt(2);
            ps = energy == 0 ? 3 : 0;
        }

        void modePress(int b) {
            if (b == 2) { leave(); return; }
            switch (s) {
                case FEED:
                    if (eatT >= 0) return;
                    if (b == 0) { feedChoice ^= 1; Sound.play(Sound.SELECT); }
                    else if (hunger >= 4) { refuseT = 18; Sound.play(Sound.NO); }
                    else eatT = 0;
                    break;
                case PLAY:
                    if (ps == 0) { guess = b; ps = 1; pt = 0; Sound.play(Sound.SELECT); }
                    break;
                case CLEAN:
                    if (b == 1 && washT < 0) washT = 0;
                    break;
                case DISC:
                    if (b == 1 && scoldT < 0) { scoldT = 0; Sound.play(Sound.SCOLD); }
                    break;
                default:
                    break;
            }
        }

        // ------------------------------------------------------------ Zeitsteuerung (alle 100 ms)
        void tick() {
            tick++;
            if (labelT > 0) labelT--;
            if (muteT > 0) muteT--;
            if (happyT > 0) happyT--;

            if (s == S.EGG) return;
            if (s == S.HATCH) {
                t++;
                if (t == 8 || t == 14 || t == 19) Sound.play(Sound.CRACK);
                if (t == 24) Sound.play(Sound.HATCH);
                if (t == 29) Sound.play(Sound.YIP);
                if (t >= 40) { s = S.HOME; happyT = 30; }
                return;
            }

            switch (s) {
                case HOME:
                    if (!naughty && tick % 240 == 80 && hunger > 1 && fun > 1 && energy > 0) Sound.play(Sound.YIP);
                    if (woodT < 0) {
                        if (rnd.nextInt(250) == 0) { woodT = 0; Sound.play(Sound.CHIRP); }
                    } else if (++woodT > 50) woodT = -1;
                    break;
                case FEED:
                    if (refuseT > 0) refuseT--;
                    if (eatT >= 0) {
                        eatT++;
                        if (eatT % 6 == 0) Sound.play(Sound.MUNCH);
                        if (eatT >= 24) {
                            hunger = Math.min(4, hunger + (feedChoice == 0 ? 2 : 1));
                            if (feedChoice == 1) fun = Math.min(4, fun + 1);
                            if (poopIn < 0) poopIn = 20 + rnd.nextInt(20);
                            eatT = -1; happyT = 25;
                            Sound.play(Sound.YIP);
                        }
                    }
                    break;
                case PLAY:
                    if (ps == 1) {
                        pt++;
                        if (pt == 5) {
                            boolean win = guess == hidden;
                            won[round] = win;
                            if (win) { wins++; Sound.play(Sound.CHIRP); } else Sound.play(Sound.LOSE);
                        }
                        if (pt >= 26) {
                            round++; pt = 0;
                            if (round >= 3) {
                                ps = 2;
                                fun = Math.min(4, fun + (wins >= 2 ? 2 : 1));
                                energy = Math.max(0, energy - 1);
                                Sound.play(wins >= 2 ? Sound.WIN : Sound.LOSE);
                            } else { ps = 0; hidden = rnd.nextInt(2); }
                        }
                    } else if (ps == 2) {
                        if (++pt >= 35) newGame();
                    }
                    break;
                case SLEEP:
                    t++;
                    if (t % 40 == 20) Sound.play(Sound.SNORE);
                    break;
                case CLEAN:
                    if (washT >= 0) {
                        washT++;
                        if (washT == 1 || washT == 14) Sound.play(Sound.SPLASH);
                        if (washT == 20) poops = 0;
                        if (washT >= 36) washT = -1;
                    }
                    break;
                case DISC:
                    if (scoldT >= 0) {
                        scoldT++;
                        if (scoldT == 10) {
                            if (naughty) {
                                disc = Math.min(4, disc + 1);
                                naughty = false; naughtySec = 0; scoldRes = 1; happyT = 22;
                                Sound.play(Sound.YIP);
                            } else {
                                fun = Math.max(0, fun - 1);
                                scoldRes = 2;
                                Sound.play(Sound.LOSE);
                            }
                        }
                        if (scoldT >= 32) scoldT = -1;
                    }
                    break;
                default:
                    break;
            }
            if (tick % 10 == 0) perSecond();
        }

        void perSecond() {
            if (s == S.SLEEP) {                 // im Schlaf erholt sich Snoopy, sonst steht die Zeit still
                if (++sleepSec % 8 == 0) energy = Math.min(4, energy + 1);
                return;
            }
            if (++hungerSec >= 45) { hungerSec = 0; hunger = Math.max(0, hunger - 1); }
            if (++funSec >= 55) { funSec = 0; fun = Math.max(0, fun - 1); }
            if (++energySec >= 80) { energySec = 0; energy = Math.max(0, energy - 1); }
            if (poopIn > 0 && --poopIn == 0) { if (poops < 3) poops++; poopIn = -1; }
            if (!naughty && rnd.nextDouble() < 0.004 * (5 - disc)) { naughty = true; Sound.play(Sound.BARK); }
            if (naughty && ++naughtySec >= 90) { naughtySec = 0; disc = Math.max(0, disc - 1); }

            boolean need = hunger <= 1 || fun <= 1 || energy <= 1 || poops > 0 || naughty;
            if (need && !attention) { attention = true; attSec = 0; Sound.play(Sound.ATT); }
            else if (!need) attention = false;
            else if (++attSec >= 30) { attSec = 0; Sound.play(Sound.ATT); }
        }

        // ------------------------------------------------------------ Darstellung
        void render(Lcd c) {
            c.clear();
            switch (s) {
                case EGG: renderEgg(c); break;
                case HATCH: renderHatch(c); break;
                case HOME: renderHome(c); break;
                case FEED: renderFeed(c); break;
                case PLAY: renderPlay(c); break;
                case SLEEP: renderSleep(c); break;
                case CLEAN: renderClean(c); break;
                case DISC: renderDisc(c); break;
                case STATUS: renderStatus(c); break;
            }
        }

        void renderEgg(Lcd c) {
            c.textC("SNOOPY", 4, DK);
            if ((tick / 5) % 2 == 0) c.textC("DRUECKE B", 11, DK);
            int ox = (tick % 30 < 6) ? ((tick % 2 == 0) ? -1 : 1) : 0;
            egg(c, ox, 0, false);
        }

        void renderHatch(Lcd c) {
            if (t < 24) {
                int crack = (t >= 6 ? 1 : 0) + (t >= 12 ? 1 : 0) + (t >= 18 ? 1 : 0);
                int ox = t >= 3 ? ((t % 2 == 0) ? -1 : 1) : 0;
                egg(c, ox, crack, false);
            } else if (t < 28) {
                egg(c, 0, 3, t % 2 == 0);
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    c.line((int) Math.round(24 + Math.cos(a) * 15), (int) Math.round(30 + Math.sin(a) * 15),
                           (int) Math.round(24 + Math.cos(a) * 23), (int) Math.round(30 + Math.sin(a) * 23), DK);
                }
            } else {
                Pose p = new Pose();
                p.bob = Math.min(10, t - 28); p.eyes = 2; p.mouth = 1; p.arm = 1;
                p.ear = (t % 2 == 0) ? 1 : -1; p.tail = t % 2;
                snoopy(c, p);
                Lcd tmp = new Lcd();
                tmp.clear();
                egg(tmp, 0, 3, false);
                for (int y = 36; y < Lcd.H; y++)
                    for (int x = 0; x < Lcd.W; x++)
                        if (tmp.get(x, y) != BG) c.set(x, y, tmp.get(x, y));
                for (int x = 0; x < Lcd.W; x++)
                    if (tmp.get(x, 36) != BG) c.set(x, 35 + ((x / 2) % 2), DK);
                if (t >= 33) { c.heart(8, 12, true); c.heart(36, 14, true); }
            }
        }

        void idle(Pose p, int cyc) {
            p.x = 22;
            if (cyc < 80) {                                   // ruhig sitzen, blinzeln, Schwanz wedeln
                p.eyes = (cyc % 40 < 3) ? 1 : 0;
                p.mouth = ((cyc / 20) % 2 == 0) ? 0 : 2;
                p.tail = (cyc / 4) % 2;
                p.ear = ((cyc / 12) % 3) - 1;
            } else if (cyc < 120) {                           // Snoopy-Tanz
                boolean up = ((cyc - 80) / 3) % 2 == 0;
                p.eyes = 2; p.mouth = 1; p.arm = 1;
                p.bob = up ? 3 : 0; p.ear = up ? 1 : -1; p.tail = up ? 1 : 0;
                p.dir = (((cyc - 80) / 14) % 2 == 0) ? 1 : -1;
            } else if (cyc < 160) {                           // schaut nach links
                p.dir = -1; p.eyes = (cyc % 40 < 3) ? 1 : 0; p.mouth = 2; p.tail = (cyc / 5) % 2;
            } else {                                          // huepft hin und her
                int ph = cyc - 160;
                if (ph < 40) { p.dir = 1; p.x = 14 + ph * 16 / 40; }
                else { p.dir = -1; p.x = 30 - (ph - 40) * 16 / 40; }
                p.bob = (ph % 6 < 2) ? 1 : 0; p.mouth = 2; p.tail = (ph / 3) % 2; p.ear = (ph % 6 < 3) ? 1 : 0;
            }
        }

        void renderHome(Lcd c) {
            strip(c, sel, (tick / 4) % 2 == 0);
            ground(c);
            for (int i = 0; i < poops; i++) poop(c, POOP_X[i], 41);
            Pose p = new Pose();
            if (naughty) {
                boolean up = (tick / 3) % 2 == 0;
                p.eyes = 3; p.mouth = up ? 1 : 0; p.bob = up ? 3 : 0; p.arm = 1; p.tail = up ? 1 : 0;
                p.ear = up ? 1 : -1; p.dir = ((tick / 12) % 2 == 0) ? 1 : -1;
                snoopy(c, p);
            } else if (energy == 0) {
                p.eyes = 1; p.mouth = ((tick / 8) % 2 == 0) ? 1 : 0; p.ear = 1;
                snoopy(c, p);
                zzz(c, tick, 33, 15);
            } else if (hunger <= 1 || fun <= 1) {
                p.eyes = 4; p.mouth = 3; p.ear = 1; p.tail = 1;
                snoopy(c, p);
            } else {
                idle(p, tick % 240);
                snoopy(c, p);
            }
            if (woodT >= 0)
                woodstock(c, 50 - woodT, 14 + (int) Math.round(1.5 * Math.sin(woodT * 0.5)), (woodT % 4 < 2) ? 0 : 1, -1);
            if (happyT > 0) {
                int hy = 22 - (30 - Math.min(30, happyT)) / 3;
                c.heart(p.x + 2, hy, true);
                if (happyT < 22) c.heart(p.x + 10, hy + 4, true);
            }
            if (attention && (tick / 5) % 2 == 0) { c.rect(43, 12, 2, 5, DK); c.rect(43, 18, 2, 2, DK); }
            String top = null;
            if (muteT > 0) top = Sound.on ? "TON AN" : "TON AUS";
            else if (labelT > 0) top = NAMES[sel];
            if (top != null) {
                int w = Lcd.textW(top);
                c.rect((Lcd.W - w) / 2 - 2, 10, w + 4, 7, BG);
                c.textC(top, 11, DK);
            }
        }

        void renderFeed(Lcd c) {
            strip(c, 0, true);
            ground(c);
            c.text(feedChoice == 0 ? "NAPF" : "PIZZA", 1, 10, DK);
            double frac = eatT < 0 ? 1.0 : Math.max(0.0, (24 - eatT) / 24.0);
            if (feedChoice == 0) bowl(c, 33, 41, frac); else pizza(c, 33, 35, frac);
            Pose p = new Pose();
            p.x = 17;
            if (refuseT > 0) {
                p.dir = (refuseT / 3) % 2 == 0 ? 1 : -1; p.eyes = 1; p.mouth = 3; p.ear = (refuseT % 2 == 0) ? 1 : -1;
                c.text("SATT", 30, 10, DK);
            } else if (eatT >= 0) {
                boolean chew = (eatT / 3) % 2 == 0;
                p.x = 18; p.eyes = 2; p.mouth = chew ? 1 : 2; p.ear = chew ? 1 : -1; p.tail = eatT % 2;
            } else {
                p.eyes = (tick % 40 < 3) ? 1 : 0; p.mouth = ((tick / 8) % 2 == 0) ? 1 : 2; p.tail = (tick / 3) % 2;
                p.arm = (tick / 8) % 2;
            }
            snoopy(c, p);
            if (happyT > 0) c.heart(p.x + 2, 22 - (30 - Math.min(30, happyT)) / 3, true);
        }

        void renderPlay(Lcd c) {
            strip(c, 1, true);
            ground(c);
            bush(c, 6);
            bush(c, 42);
            Pose p = new Pose();
            p.x = 22;
            if (ps == 3) {
                p.eyes = 1; p.mouth = ((tick / 8) % 2 == 0) ? 1 : 0; p.ear = 1;
                c.text("MUEDE", 1, 10, DK);
                zzz(c, tick, 33, 15);
            } else if (ps == 0) {
                p.dir = ((tick / 10) % 2 == 0) ? 1 : -1; p.mouth = 2; p.ear = ((tick / 6) % 3) - 1; p.tail = (tick / 4) % 2;
                if ((tick / 5) % 2 == 0) c.text("WO?", 1, 10, DK);
            } else if (ps == 1) {
                p.dir = guess == 0 ? -1 : 1;
                boolean showRes = pt >= 5;
                if (showRes && won[round]) {
                    boolean up = (pt / 3) % 2 == 0;
                    p.eyes = 2; p.mouth = 1; p.arm = 1; p.bob = up ? 3 : 0; p.ear = up ? 1 : -1;
                } else if (showRes) { p.eyes = 4; p.mouth = 3; p.ear = 1; }
                else p.mouth = 0;
                if (showRes) {
                    int hx = hidden == 0 ? 6 : 42;
                    woodstock(c, hx, 41 - Math.min(pt - 5, 8), (pt % 4 < 2) ? 0 : 1, hidden == 0 ? 1 : -1);
                    bush(c, hx);
                }
            } else {
                boolean good = wins >= 2;
                if (good) {
                    boolean up = (pt / 3) % 2 == 0;
                    p.eyes = 2; p.mouth = 1; p.arm = 1; p.bob = up ? 3 : 0; p.ear = up ? 1 : -1; p.tail = up ? 1 : 0;
                } else { p.eyes = 4; p.mouth = 3; p.ear = 1; }
                c.text(good ? "GEWONNEN" : "VERLOREN", 3, 10, DK);
            }
            snoopy(c, p);
            if (ps != 3 && ps != 2) {
                for (int i = 0; i < 3; i++) {
                    boolean done = i < round || (ps == 1 && i == round && pt >= 5);
                    int sx = 37 + i * 4;
                    if (done) { if (won[i]) c.rect(sx, 11, 3, 3, DK); else { c.rect(sx, 11, 3, 3, DK); c.set(sx + 1, 12, BG); } }
                    else c.set(sx + 1, 12, DK);
                }
            }
        }

        void renderSleep(Lcd c) {
            strip(c, 2, true);
            doghouse(c);
            ground(c);
            // Snoopy liegt auf dem Hundehuettendach (auf dem Ruecken, Nase nach oben)
            c.outlined(14, 21, 2, 3, WH);
            c.outlined(18, 20, 2, 3, WH);
            c.outlined(23, 23, 9, 3, WH);
            c.ellipse(29, 24, 2.5, 3.5, DK);
            c.ellipse(31, 20, 5.5, 4.5, DK);
            c.ellipse(34.5, 18, 3.5, 3.5, DK);
            c.ellipse(31, 20, 4.5, 3.5, WH);
            c.ellipse(34.5, 18, 2.5, 2.5, WH);
            c.ellipse(36, 15.5, 2, 1.5, DK);                  // Nase
            c.ellipse(28, 24, 2.5, 3.5, DK);                  // Ohr haengt herunter
            c.set(31, 19, DK); c.set(32, 19, DK); c.set(33, 19, DK);   // geschlossenes Auge
            zzz(c, tick, 12, 16);
            for (int i = 0; i < 4; i++) c.rect(1 + i * 4, 11, 3, 3, i < energy ? DK : BG);
            for (int i = energy; i < 4; i++) { c.rect(1 + i * 4, 11, 3, 3, DK); c.set(2 + i * 4, 12, BG); }
        }

        void renderClean(Lcd c) {
            strip(c, 3, true);
            ground(c);
            if (washT < 20) for (int i = 0; i < poops; i++) poop(c, POOP_X[i], 41);
            Pose p = new Pose();
            p.x = 20;
            if (washT >= 0) {
                p.eyes = 1; p.mouth = 2; p.ear = (washT % 4 < 2) ? 1 : -1; p.tail = washT % 2; p.arm = 1;
                snoopy(c, p);
                for (int i = 0; i < 14; i++) c.rect((i * 7 + 3) % 46, 10 + ((washT * 3 + i * 11) % 34), 1, 2, MD);
                for (int i = 0; i < 5; i++) ring(c, 6 + i * 9, 40 - ((washT * 2 + i * 9) % 28));
            } else {
                if (poops == 0) {
                    p.eyes = (tick % 40 < 3) ? 1 : 0; p.mouth = 2; p.tail = (tick / 4) % 2;
                    c.text("SAUBER", 1, 10, DK);
                    if ((tick / 4) % 2 == 0) { c.set(38, 18, DK); c.set(37, 19, DK); c.set(39, 19, DK); c.set(38, 20, DK); }
                } else { p.eyes = 4; p.mouth = 3; p.ear = 1; c.text("IGITT", 1, 10, DK); }
                snoopy(c, p);
            }
        }

        void renderDisc(Lcd c) {
            strip(c, 4, true);
            ground(c);
            Pose p = new Pose();
            p.x = 17;
            if (scoldT >= 0 && scoldT < 10) {
                p.eyes = 4; p.mouth = 3; p.ear = 1;
                c.rect(40, 18, 3, 10, DK); c.rect(40, 31, 3, 3, DK);
                if ((scoldT / 2) % 2 == 0) {
                    c.line(38, 17, 36, 15, DK); c.line(44, 17, 46, 15, DK);
                    c.line(37, 24, 35, 24, DK); c.line(45, 24, 47, 24, DK);
                }
            } else if (scoldT >= 10 && scoldRes == 1) {
                p.eyes = 2; p.mouth = 2; p.tail = (tick / 3) % 2;
                c.text("BRAV!", 1, 10, DK);
            } else if (scoldT >= 10) {
                p.eyes = 4; p.mouth = 3; p.ear = 1;
                c.text("GRUNDLOS", 1, 10, DK);
            } else if (naughty) {
                boolean up = (tick / 3) % 2 == 0;
                p.eyes = 3; p.mouth = up ? 1 : 0; p.bob = up ? 3 : 0; p.arm = 1; p.ear = up ? 1 : -1; p.tail = up ? 1 : 0;
                if ((tick / 4) % 2 == 0) c.text("UNARTIG", 1, 10, DK);
            } else {
                p.eyes = (tick % 40 < 3) ? 1 : 0; p.mouth = 2; p.tail = (tick / 6) % 2;
                c.text("ALLES OK", 1, 10, DK);
            }
            snoopy(c, p);
            if (happyT > 0) c.heart(p.x + 2, 22 - (30 - Math.min(30, happyT)) / 3, true);
        }

        void renderStatus(Lcd c) {
            strip(c, 5, true);
            String[] names = {"SATT", "SPASS", "BRAV", "KRAFT"};
            int[] v = {hunger, fun, disc, energy};
            for (int i = 0; i < 4; i++) {
                int y = 14 + i * 8;
                c.text(names[i], 1, y, DK);
                for (int h = 0; h < 4; h++) c.heart(26 + h * 5, y, h < v[i]);
            }
        }
    }

    // =====================================================================================
    //  Gehaeuse: Ei + Display + 3 Tasten
    // =====================================================================================
    static final class RoundButton extends JButton {
        RoundButton(String label) {
            super(label);
            setFocusable(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
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

    static final class Shell extends JPanel {
        static final int WIN_W = 340, WIN_H = 440, SCALE = 4, LX = 74, LY = 96;
        static final int[] PAL = {0xBECDA0, 0x232D23, 0x708260, 0xE8F0D6};
        static final int[] PAL_DARK = {0x69785A, 0x0F140F, 0x465540, 0x8C9B78};

        final Game game;
        final Lcd lcd = new Lcd();
        final BufferedImage img = new BufferedImage(Lcd.W, Lcd.H, BufferedImage.TYPE_INT_RGB);
        final RoundButton[] btn = new RoundButton[3];
        final Shape egg = eggShape(WIN_W, WIN_H);
        private Point dragStart;

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

            String[] labels = {"A", "B", "C"};
            int[][] pos = {{92, 346}, {170, 366}, {248, 346}};
            for (int i = 0; i < 3; i++) {
                final int k = i;
                btn[i] = new RoundButton(labels[i]);
                btn[i].setBounds(pos[i][0] - 28, pos[i][1] - 28, 56, 56);
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

            JPopupMenu menu = new JPopupMenu();
            JMenuItem mute = new JMenuItem("Ton an/aus");
            mute.addActionListener(e -> Sound.toggle());
            JMenuItem restart = new JMenuItem("Neues Ei (Neustart)");
            restart.addActionListener(e -> game.reset());
            JMenuItem quit = new JMenuItem("Beenden");
            quit.addActionListener(e -> System.exit(0));
            menu.add(mute); menu.add(restart); menu.addSeparator(); menu.add(quit);

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

        private void bind(int key, int idx) {
            String name = "btn" + idx + "_" + key;
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), name);
            getActionMap().put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { btn[idx].doClick(70); }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // Ei-Schale
            g.setPaint(new GradientPaint(0, 0, new Color(255, 118, 98), WIN_W, WIN_H, new Color(186, 28, 52)));
            g.fill(egg);
            Shape oldClip = g.getClip();
            g.clip(egg);
            g.setPaint(new GradientPaint(0, 20, new Color(255, 255, 255, 90), 0, 200, new Color(255, 255, 255, 0)));
            g.fill(new Ellipse2D.Double(36, 14, 150, 190));
            g.setColor(new Color(255, 255, 255, 38));       // dezentes Pfotenmuster
            for (int i = 0; i < 3; i++) {
                int px = 60 + i * 110, py = 398 - (i == 1 ? 14 : 0);
                g.fillOval(px, py, 18, 14);
                g.fillOval(px - 5, py - 9, 7, 8); g.fillOval(px + 3, py - 12, 7, 8); g.fillOval(px + 11, py - 9, 7, 8);
            }
            g.setClip(oldClip);
            g.setColor(new Color(105, 10, 28));
            g.setStroke(new BasicStroke(5f));
            g.draw(egg);

            // Titel
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 22));
            FontMetrics fm = g.getFontMetrics();
            String title = "SnoopyGotchi";
            int tx = (WIN_W - fm.stringWidth(title)) / 2;
            g.setColor(new Color(105, 10, 28, 160));
            g.drawString(title, tx + 2, 64);
            g.setColor(new Color(255, 248, 235));
            g.drawString(title, tx, 62);

            // Display-Rahmen
            g.setColor(new Color(58, 58, 70));
            g.fillRoundRect(LX - 14, LY - 14, 192 + 28, 192 + 28, 36, 36);
            g.setColor(new Color(150, 150, 165));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(LX - 14, LY - 14, 192 + 28, 192 + 28, 36, 36);

            // LCD
            game.render(lcd);
            int[] pal = game.dark() ? PAL_DARK : PAL;
            for (int y = 0; y < Lcd.H; y++)
                for (int x = 0; x < Lcd.W; x++)
                    img.setRGB(x, y, pal[lcd.px[y * Lcd.W + x]]);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(img, LX, LY, Lcd.W * SCALE, Lcd.H * SCALE, null);
            g.setColor(new Color(0, 0, 0, 20));              // Pixelraster
            g.setStroke(new BasicStroke(1f));
            for (int i = 1; i < Lcd.W; i++) {
                g.drawLine(LX + i * SCALE, LY, LX + i * SCALE, LY + Lcd.H * SCALE);
                g.drawLine(LX, LY + i * SCALE, LX + Lcd.W * SCALE, LY + i * SCALE);
            }
            g.setColor(new Color(0, 0, 0, 110));
            g.drawRect(LX - 1, LY - 1, 193, 193);
            g.setPaint(new GradientPaint(LX, LY, new Color(255, 255, 255, 45), LX, LY + 90, new Color(255, 255, 255, 0)));
            g.fillRect(LX, LY, 192, 90);

            // Beschriftung der Tasten
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            fm = g.getFontMetrics();
            String[] cap = {"W\u00C4HLEN", "OK", "ZUR\u00DCCK"};
            int[][] pos = {{92, 346}, {170, 366}, {248, 346}};
            g.setColor(new Color(255, 240, 225));
            for (int i = 0; i < 3; i++)
                g.drawString(cap[i], pos[i][0] - fm.stringWidth(cap[i]) / 2, pos[i][1] + 44);
            g.dispose();
        }
    }

    // =====================================================================================
    //  Start
    // =====================================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnoopyGotchi::start);
    }

    static void start() {
        JFrame f = new JFrame("SnoopyGotchi");
        f.setUndecorated(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Game game = new Game();
        Shell shell = new Shell(game, f);
        f.setContentPane(shell);
        f.pack();
        f.setResizable(false);
        f.setLocationRelativeTo(null);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
            f.setBackground(new Color(0, 0, 0, 0));          // Fenster ist nur das Ei
        } else if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            f.setShape(shell.egg);
        }
        f.setVisible(true);
        new javax.swing.Timer(100, e -> { game.tick(); shell.repaint(); }).start();
    }
}
