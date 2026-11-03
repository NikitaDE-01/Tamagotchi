import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;

class SoundPlayer {
    enum Wave { SQUARE, TRI, SINE }

    private static final float SAMPLE_RATE = 22050f;

    static void clickMove() {
        melody(new int[]{900}, 35, Wave.SQUARE, 0.18f);
    }

    static void clickOk() {
        melody(new int[]{1200, 1600}, 45, Wave.SQUARE, 0.18f);
    }

    static void clickBack() {
        melody(new int[]{500}, 60, Wave.TRI, 0.22f);
    }

    static void hatch() {
        melody(new int[]{523, 659, 784, 1046, 1318}, 90, Wave.SQUARE, 0.22f);
    }

    static void evolve() {
        melody(new int[]{392, 523, 659, 784, 1046, 1318, 1568}, 100, Wave.SQUARE, 0.22f);
    }

    static void perfect() {
        melody(new int[]{1046, 1568, 2093}, 60, Wave.SQUARE, 0.2f);
    }

    static void good() {
        melody(new int[]{784, 988}, 70, Wave.TRI, 0.22f);
    }

    static void miss() {
        melody(new int[]{196, 155, 130}, 90, Wave.SQUARE, 0.2f);
    }

    static void eat() {
        melody(new int[]{330, 0, 330, 392, 0, 440}, 80, Wave.TRI, 0.25f);
    }

    static void cleanDone() {
        melody(new int[]{660, 770, 880, 990, 1100, 1320}, 55, Wave.SINE, 0.25f);
    }

    static void scold() {
        melody(new int[]{220, 220, 180}, 120, Wave.SQUARE, 0.2f);
    }

    static void sleepOn() {
        melody(new int[]{523, 392, 330, 262}, 140, Wave.TRI, 0.22f);
    }

    static void sleepOff() {
        melody(new int[]{262, 330, 392, 523}, 110, Wave.TRI, 0.22f);
    }

    static void gameStart() {
        melody(new int[]{659, 659, 659, 880, 1046}, 80, Wave.SQUARE, 0.2f);
    }

    static void gameOver(boolean win) {
        if (win) {
            melody(new int[]{784, 988, 1175, 1568}, 110, Wave.SQUARE, 0.22f);
        } else {
            melody(new int[]{392, 330, 262}, 140, Wave.TRI, 0.22f);
        }
    }

    static void call() {
        melody(new int[]{1046, 0, 1046, 0, 1046}, 110, Wave.SQUARE, 0.25f);
    }

    static void dead() {
        melody(new int[]{392, 370, 349, 330, 262}, 170, Wave.TRI, 0.25f);
    }

    static void playModeSound(Screen screen) {
        switch (screen) {
            case EGG:
                melody(new int[]{320, 480, 620}, 90, Wave.TRI, 0.25f);
                break;
            case FEED:
                melody(new int[]{523, 659, 784}, 80, Wave.SQUARE, 0.18f);
                break;
            case SLEEP:
                melody(new int[]{392, 330, 262}, 120, Wave.TRI, 0.22f);
                break;
            case PLAY:
                melody(new int[]{659, 784, 988, 784}, 80, Wave.SQUARE, 0.18f);
                break;
            case CLEAN:
                melody(new int[]{880, 990, 1100}, 70, Wave.SINE, 0.25f);
                break;
            case DISCIPLINE:
                melody(new int[]{220, 175}, 130, Wave.SQUARE, 0.2f);
                break;
            case STATUS:
                melody(new int[]{600, 700}, 60, Wave.TRI, 0.2f);
                break;
            case DEAD:
                melody(new int[]{220, 196}, 200, Wave.TRI, 0.22f);
                break;
            default:
                melody(new int[]{500}, 60, Wave.TRI, 0.2f);
                break;
        }
    }

    private static void melody(int[] frequencies, int durationMs, Wave wave, float volume) {
        Thread thread = new Thread(() -> {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            SourceDataLine line = null;

            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                for (int frequency : frequencies) {
                    byte[] data = tone(frequency, durationMs, wave, volume);
                    line.write(data, 0, data.length);
                }

                line.drain();
            } catch (Exception ex) {
                Toolkit.getDefaultToolkit().beep();
            } finally {
                if (line != null) {
                    line.close();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private static byte[] tone(int frequency, int durationMs, Wave wave, float volume) {
        int samples = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buffer = new byte[samples * 2];

        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            double phase = 2.0 * Math.PI * frequency * t;
            double value;

            switch (wave) {
                case SQUARE:
                    value = Math.sin(phase) >= 0 ? 1.0 : -1.0;
                    break;
                case TRI:
                    value = 2.0 / Math.PI * Math.asin(Math.sin(phase));
                    break;
                default:
                    value = Math.sin(phase);
                    break;
            }

            double attack = Math.min(1.0, i / 60.0);
            double decay = Math.min(1.0, (samples - i) / (samples * 0.5));
            double env = attack * decay;

            short s = (short) (value * Short.MAX_VALUE * volume * env);

            buffer[2 * i] = (byte) (s & 0xFF);
            buffer[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
        }

        return buffer;
    }
}