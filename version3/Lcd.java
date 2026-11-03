import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class Lcd {
    //Größe
    static final int W = 48, H = 48;
    static final int BG = 0, DK = 1, MD = 2, WH = 3;
    final int[] px = new int[W * H];

    //Schrift
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

    //Löschen
    void clear() { Arrays.fill(px, BG); }

    //Pixel setzen
    void set(int x, int y, int c) {
        if (x >= 0 && y >= 0 && x < W && y < H) px[y * W + x] = c;
    }

    //Pixel lesen
    int get(int x, int y) {
        return (x >= 0 && y >= 0 && x < W && y < H) ? px[y * W + x] : BG;
    }

    //Rechteck
    void rect(int x, int y, int w, int h, int c) {
        for (int j = 0; j < h; j++) for (int i = 0; i < w; i++) set(x + i, y + j, c);
    }

    //Linie
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

    //Ellipse
    void ellipse(double cx, double cy, double rx, double ry, int c) {
        for (int y = (int) Math.floor(cy - ry); y <= (int) Math.ceil(cy + ry); y++)
            for (int x = (int) Math.floor(cx - rx); x <= (int) Math.ceil(cx + rx); x++) {
                double dx = (x + 0.5 - cx) / rx, dy = (y + 0.5 - cy) / ry;
                if (dx * dx + dy * dy <= 1.0) set(x, y, c);
            }
    }

    //Mit Rand
    void outlined(double cx, double cy, double rx, double ry, int fill) {
        ellipse(cx, cy, rx + 1, ry + 1, DK);
        ellipse(cx, cy, rx, ry, fill);
    }

    //Symbol
    void icon(int x, int y, String[] rows, int c) {
        for (int r = 0; r < rows.length; r++)
            for (int i = 0; i < rows[r].length(); i++)
                if (rows[r].charAt(i) == '1') set(x + i, y + r, c);
    }

    //Text
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

    //Textbreite
    static int textW(String s) { return s.length() * 4 - 1; }

    //Text mittig
    void textC(String s, int y, int c) { text(s, (W - textW(s)) / 2, y, c); }

    //Herz
    void heart(int x, int y, boolean filled) {
        String[] h = {".#.#.", "#####", ".###.", "..#.."};
        for (int r = 0; r < 4; r++)
            for (int i = 0; i < 5; i++)
                if (h[r].charAt(i) == '#') set(x + i, y + r, DK);
        if (!filled) { set(x + 1, y + 1, BG); set(x + 3, y + 1, BG); set(x + 2, y + 2, BG); }
    }
}
