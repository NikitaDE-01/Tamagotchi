import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

final class Save {
    static final Path FILE = Paths.get(System.getProperty("user.home"), ".snoopygotchi.properties");

    private Save() { }

    //Speichern
    static synchronized void write(Game g) {
        Properties p = new Properties();
        p.setProperty("alive", String.valueOf(g.alive()));
        p.setProperty("dead", String.valueOf(g.dead()));
        p.setProperty("born", String.valueOf(g.bornAt));
        p.setProperty("died", String.valueOf(g.diedAt));
        p.setProperty("best", String.valueOf(g.bestSec));
        p.setProperty("record", String.valueOf(g.newRecord));
        p.setProperty("sel", String.valueOf(g.sel));
        p.setProperty("hunger", String.valueOf(g.hunger));
        p.setProperty("fun", String.valueOf(g.fun));
        p.setProperty("disc", String.valueOf(g.disc));
        p.setProperty("energy", String.valueOf(g.energy));
        p.setProperty("poops", String.valueOf(g.poops));
        p.setProperty("naughty", String.valueOf(g.naughty));
        p.setProperty("poopIn", String.valueOf(g.poopIn));
        p.setProperty("hungerSec", String.valueOf(g.hungerSec));
        p.setProperty("funSec", String.valueOf(g.funSec));
        p.setProperty("energySec", String.valueOf(g.energySec));
        p.setProperty("discSec", String.valueOf(g.discSec));
        p.setProperty("naughtySec", String.valueOf(g.naughtySec));
        p.setProperty("dying", String.valueOf(g.dying));
        p.setProperty("dyingLeft", String.valueOf(g.dyingLeft));
        p.setProperty("sound", String.valueOf(Sound.on));
        try (OutputStream o = Files.newOutputStream(FILE)) {
            p.store(o, "SnoopyGotchi");
        } catch (Exception e) {
            //Ignorieren
        }
    }

    //Laden
    static void read(Game g) {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            Sound.on = Boolean.parseBoolean(p.getProperty("sound", "true"));
            g.bestSec = Math.max(0, Long.parseLong(p.getProperty("best", "0")));
            boolean dead = Boolean.parseBoolean(p.getProperty("dead", "false"));
            boolean alive = Boolean.parseBoolean(p.getProperty("alive", p.getProperty("hatched", "false")));
            if (!dead && !alive) return;
            g.bornAt = Long.parseLong(p.getProperty("born", "0"));
            //Tot
            if (dead) {
                g.s = Game.S.DEAD;
                g.diedAt = Long.parseLong(p.getProperty("died", "0"));
                g.newRecord = Boolean.parseBoolean(p.getProperty("record", "false"));
                return;
            }
            //Weiter im Hauptbildschirm
            g.s = Game.S.HOME;
            g.sel = clamp(p, "sel", 0, 5);
            g.hunger = clamp(p, "hunger", 0, 4);
            g.fun = clamp(p, "fun", 0, 4);
            g.disc = clamp(p, "disc", 0, 4);
            g.energy = clamp(p, "energy", 0, 4);
            g.poops = clamp(p, "poops", 0, 3);
            g.naughty = Boolean.parseBoolean(p.getProperty("naughty", "false"));
            g.poopIn = clamp(p, "poopIn", -1, 100);
            g.hungerSec = clamp(p, "hungerSec", 0, 44);
            g.funSec = clamp(p, "funSec", 0, 54);
            g.energySec = clamp(p, "energySec", 0, 79);
            g.discSec = clamp(p, "discSec", 0, 149);
            g.naughtySec = clamp(p, "naughtySec", 0, 89);
            g.dying = Boolean.parseBoolean(p.getProperty("dying", "false"));
            g.dyingLeft = clamp(p, "dyingLeft", 1, Game.DYING_TIME);
            g.newGame();
        } catch (Exception e) {
            long best = g.bestSec;
            g.reset();
            g.bestSec = best;
        }
    }

    //Grenzen
    private static int clamp(Properties p, String key, int lo, int hi) {
        int v = Integer.parseInt(p.getProperty(key, String.valueOf(lo)));
        return Math.max(lo, Math.min(hi, v));
    }
}
