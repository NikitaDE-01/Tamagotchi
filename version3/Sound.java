import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Sound {
    static final float RATE = 22050f;
    static final AudioFormat FMT = new AudioFormat(RATE, 16, 1, true, false);
    static final Random R = new Random();
    static volatile boolean on = true;
    static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "sound");
        t.setDaemon(true);
        return t;
    });

    //Ton
    static double[] n(double f0, double f1, double ms, int wave) { return new double[]{f0, f1, ms, wave}; }

    static double[] rest(double ms) { return n(0, 0, ms, 0); }

    //Klang erzeugen
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

    //Moduswechsel
    static final byte[] SELECT = build(n(1046, 1046, 45, 0));
    static final byte[] BACK = build(n(988, 988, 50, 0), rest(15), n(660, 660, 70, 0));
    static final byte[][] ENTER = new byte[6][];

    static {
        for (int i = 0; i < 6; i++) {
            double f = 600 + i * 90;
            ENTER[i] = build(n(f, f, 60, 0), rest(20), n(f * 1.5, f * 1.5, 90, 0));
        }
        ENTER[2] = build(n(659, 659, 150, 3), n(523, 523, 150, 3), n(392, 392, 320, 3));
    }

    //Snoopy
    static final byte[] YIP = build(n(900, 1500, 70, 0), rest(25), n(1100, 1800, 90, 0));
    static final byte[] BARK = build(n(520, 260, 90, 0), rest(40), n(520, 260, 110, 0));
    static final byte[] NO = build(n(300, 200, 120, 0), rest(30), n(300, 190, 170, 0));
    static final byte[] SCOLD = build(n(420, 210, 110, 1), rest(30), n(440, 180, 160, 1));
    static final byte[] MUNCH = build(n(0, 0, 45, 2), rest(10), n(180, 120, 30, 0));
    //Schlafen
    static final byte[] SNORE = build(n(110, 80, 350, 1), rest(60), n(90, 70, 300, 1));
    static final byte[] YAWN = build(n(800, 300, 500, 3));
    //Woodstock
    static final byte[] CHIRP = build(n(2200, 3000, 35, 3), n(3000, 2400, 35, 3), n(2400, 3200, 35, 3),
            rest(45), n(2600, 3300, 35, 3), n(3300, 2500, 40, 3));
    //Spiel
    static final byte[] WIN = build(n(523, 523, 90, 0), n(659, 659, 90, 0), n(784, 784, 90, 0), n(1047, 1047, 220, 0));
    static final byte[] LOSE = build(n(392, 392, 140, 0), n(330, 330, 140, 0), n(262, 262, 300, 1));
    //Warnung
    static final byte[] ATT = build(n(1568, 1568, 70, 0), rest(60), n(1568, 1568, 70, 0), rest(60), n(1568, 1568, 70, 0));
    static final byte[] CRACK = build(n(0, 0, 30, 2), rest(10), n(900, 400, 25, 0));
    //Schlüpfen
    static final byte[] HATCH = build(n(523, 523, 100, 0), n(659, 659, 100, 0), n(784, 784, 100, 0), n(1047, 1047, 100, 0),
            n(784, 784, 80, 0), n(1047, 1047, 100, 0), n(1319, 1319, 260, 0));
    static final byte[] SPLASH = build(n(600, 1200, 40, 3), n(800, 1500, 40, 3), n(500, 1100, 40, 3),
            n(900, 1700, 40, 3), n(700, 1400, 40, 3));

    //Tod
    static final byte[] DEATH = build(n(523, 523, 220, 3), n(494, 494, 220, 3), n(466, 466, 220, 3), n(440, 440, 220, 3),
            n(392, 392, 220, 3), n(330, 330, 500, 3), n(196, 130, 600, 1));
    static final byte[] GHOST = build(n(300, 1300, 700, 3), rest(60), n(1300, 1000, 300, 3));
    static final byte[] ALARM = build(n(880, 880, 120, 0), rest(60), n(660, 660, 120, 0), rest(60),
            n(880, 880, 120, 0), rest(60), n(660, 660, 200, 0));
    static final byte[] HEART = build(n(110, 60, 70, 1), rest(90), n(110, 60, 70, 1));
    static final byte[] RECOVER = build(n(523, 523, 80, 0), n(784, 784, 80, 0), n(1047, 1047, 80, 0), n(1568, 1568, 200, 0));

    //Abspielen
    static void play(byte[] data) {
        if (!on) return;
        EX.submit(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(FMT, data, 0, data.length);
                clip.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) clip.close(); });
                clip.start();
            } catch (Exception | Error ex) {
                //Kein Audiogeraet
            }
        });
    }

    //Ton an aus
    static void toggle() {
        on = !on;
        if (on) play(SELECT);
    }
}
