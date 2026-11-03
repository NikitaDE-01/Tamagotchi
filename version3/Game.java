import java.util.Arrays;
import java.util.Random;

final class Game {
    //Zustände
    enum S { EGG, HATCH, HOME, FEED, PLAY, SLEEP, CLEAN, DISC, STATUS, DEATH, DEAD }

    //Konstanten
    static final int DYING_TIME = 30;
    static final int BG = Lcd.BG, DK = Lcd.DK, MD = Lcd.MD, WH = Lcd.WH;
    static final S[] MODES = {S.FEED, S.PLAY, S.SLEEP, S.CLEAN, S.DISC, S.STATUS};
    static final String[] NAMES = {"ESSEN", "SPIELEN", "SCHLAFEN", "SAUBER", "DISZIPLIN", "STATUS"};
    static final int[] POOP_X = {3, 34, 41};

    //Werte
    final Random rnd = new Random();
    S s;
    int sel, tick, t;
    int hunger, fun, disc, energy, poops;
    boolean naughty, attention;
    int hungerSec, funSec, energySec, poopIn, attSec, naughtySec, sleepSec;
    int labelT, muteT, happyT, woodT;
    //Essen
    int feedChoice, eatT, refuseT;
    //Spielen
    int ps, pt, round, wins, hidden, guess;
    final boolean[] won = new boolean[3];
    //Sauber und Disziplin
    int washT, scoldT, scoldRes;
    //Alter und Tod
    long bornAt, diedAt, bestSec;
    boolean dying, newRecord;
    int dyingLeft, confirmT, discSec;

    //Start
    Game() { reset(); }

    //Neues Ei
    void reset() {
        s = S.EGG; sel = 0; tick = 0; t = 0;
        hunger = 3; fun = 3; disc = 2; energy = 4; poops = 0;
        naughty = false; attention = false;
        hungerSec = funSec = energySec = attSec = naughtySec = sleepSec = 0;
        poopIn = -1; labelT = muteT = happyT = 0; woodT = -1;
        feedChoice = 0; eatT = -1; refuseT = 0;
        washT = -1; scoldT = -1; scoldRes = 0;
        bornAt = 0; diedAt = 0;
        dying = false; newRecord = false;
        dyingLeft = 0; confirmT = 0; discSec = 0;
        newGame();
    }

    //Hilfen
    boolean dark() { return s == S.SLEEP || dead(); }

    boolean dead() { return s == S.DEATH || s == S.DEAD; }

    boolean alive() { return s != S.EGG && s != S.HATCH && !dead(); }

    long lived() {
        long end = dead() ? diedAt : System.currentTimeMillis();
        return Math.max(0, (end - bornAt) / 1000);
    }

    static String fmt(long sec) {
        long d = sec / 86400, h = sec / 3600 % 24, m = sec / 60 % 60, sc = sec % 60;
        if (d > 0) return String.format("%dT %02d:%02d", d, h, m);
        return String.format("%02d:%02d:%02d", h, m, sc);
    }

    //Alter
    String age() {
        if (bornAt == 0 || s == S.EGG || s == S.HATCH) return "";
        return fmt(lived());
    }

    //Sterben
    void die() {
        s = S.DEATH; t = 0;
        dying = false; attention = false; naughty = false;
        diedAt = System.currentTimeMillis();
        long l = lived();
        newRecord = l > bestSec;
        if (newRecord) bestSec = l;
        Save.write(this);
    }

    //Tasten
    void press(int b) {
        switch (s) {
            case EGG:
                if (b == 1) { s = S.HATCH; t = 0; Sound.play(Sound.ENTER[1]); }
                else Sound.play(Sound.SELECT);
                break;
            case HATCH:
            case DEATH:
                break;
            case DEAD:
                if (b == 1) {
                    if (confirmT > 0) { reset(); Save.write(this); Sound.play(Sound.ENTER[1]); }
                    else { confirmT = 30; Sound.play(Sound.SELECT); }
                } else Sound.play(Sound.SELECT);
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

    //Modus starten
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

    //Zurück
    void leave() {
        s = S.HOME; t = 0;
        Sound.play(Sound.BACK);
    }

    //Neues Spiel
    void newGame() {
        round = 0; wins = 0; pt = 0; guess = 0;
        Arrays.fill(won, false);
        hidden = rnd.nextInt(2);
        ps = energy == 0 ? 3 : 0;
    }

    //Tasten im Modus
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

    //Takt
    void tick() {
        tick++;
        if (labelT > 0) labelT--;
        if (muteT > 0) muteT--;
        if (happyT > 0) happyT--;
        if (tick % 100 == 0) Save.write(this);

        if (s == S.EGG) return;
        if (s == S.DEAD) { if (confirmT > 0) confirmT--; return; }
        if (s == S.DEATH) {
            t++;
            if (t == 1) Sound.play(Sound.DEATH);
            if (t == 26) Sound.play(Sound.GHOST);
            if (t >= 90) { s = S.DEAD; Save.write(this); }
            return;
        }
        if (s == S.HATCH) {
            t++;
            if (t == 8 || t == 14 || t == 19) Sound.play(Sound.CRACK);
            if (t == 24) Sound.play(Sound.HATCH);
            if (t == 29) Sound.play(Sound.YIP);
            if (t >= 40) { s = S.HOME; happyT = 30; bornAt = System.currentTimeMillis(); Save.write(this); }
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

    //Jede Sekunde
    void perSecond() {
        if (s == S.SLEEP) {
            if (++sleepSec % 8 == 0) energy = Math.min(4, energy + 1);
            return;
        }
        if (++hungerSec >= 45) { hungerSec = 0; hunger = Math.max(0, hunger - 1); }
        if (++funSec >= (poops >= 3 ? 27 : 55)) { funSec = 0; fun = Math.max(0, fun - 1); }
        if (++energySec >= 80) { energySec = 0; energy = Math.max(0, energy - 1); }
        if (++discSec >= 150) { discSec = 0; disc = Math.max(0, disc - 1); }
        if (poopIn > 0 && --poopIn == 0) { if (poops < 3) poops++; poopIn = -1; }
        if (!naughty && rnd.nextDouble() < 0.004 * (5 - disc)) { naughty = true; Sound.play(Sound.BARK); }
        if (naughty && ++naughtySec >= 90) { naughtySec = 0; disc = Math.max(0, disc - 1); }

        boolean need = hunger <= 1 || fun <= 1 || energy <= 1 || poops > 0 || naughty;
        if (need && !attention) { attention = true; attSec = 0; Sound.play(Sound.ATT); }
        else if (!need) attention = false;
        else if (++attSec >= 30) { attSec = 0; Sound.play(Sound.ATT); }

        //Todesgefahr
        if (hunger == 0 && fun == 0 && disc == 0 && energy == 0) {
            if (!dying) { dying = true; dyingLeft = DYING_TIME; Sound.play(Sound.ALARM); }
            else {
                dyingLeft--;
                if (dyingLeft % 2 == 0) Sound.play(Sound.HEART);
                if (dyingLeft <= 0) die();
            }
        } else if (dying) {
            dying = false; happyT = 20;
            Sound.play(Sound.RECOVER);
        }
    }

    //Zeichnen
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
            case DEATH: renderDeath(c); break;
            case DEAD: renderDead(c); break;
        }
        if (dying && alive()) c.rect(0, 46, Lcd.W * dyingLeft / DYING_TIME, 2, DK);
    }

    //Ei
    void renderEgg(Lcd c) {
        c.textC("SNOOPY", 4, DK);
        if ((tick / 5) % 2 == 0) c.textC("DRUECKE B", 11, DK);
        int ox = (tick % 30 < 6) ? ((tick % 2 == 0) ? -1 : 1) : 0;
        Art.egg(c, ox, 0, false);
    }

    //Schlüpfen
    void renderHatch(Lcd c) {
        if (t < 24) {
            int crack = (t >= 6 ? 1 : 0) + (t >= 12 ? 1 : 0) + (t >= 18 ? 1 : 0);
            int ox = t >= 3 ? ((t % 2 == 0) ? -1 : 1) : 0;
            Art.egg(c, ox, crack, false);
        } else if (t < 28) {
            Art.egg(c, 0, 3, t % 2 == 0);
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                c.line((int) Math.round(24 + Math.cos(a) * 15), (int) Math.round(30 + Math.sin(a) * 15),
                       (int) Math.round(24 + Math.cos(a) * 23), (int) Math.round(30 + Math.sin(a) * 23), DK);
            }
        } else {
            Pose p = new Pose();
            p.bob = Math.min(10, t - 28); p.eyes = 2; p.mouth = 1; p.arm = 1;
            p.ear = (t % 2 == 0) ? 1 : -1; p.tail = t % 2;
            Art.snoopy(c, p);
            Lcd tmp = new Lcd();
            tmp.clear();
            Art.egg(tmp, 0, 3, false);
            for (int y = 36; y < Lcd.H; y++)
                for (int x = 0; x < Lcd.W; x++)
                    if (tmp.get(x, y) != BG) c.set(x, y, tmp.get(x, y));
            for (int x = 0; x < Lcd.W; x++)
                if (tmp.get(x, 36) != BG) c.set(x, 35 + ((x / 2) % 2), DK);
            if (t >= 33) { c.heart(8, 12, true); c.heart(36, 14, true); }
        }
    }

    //Leerlauf
    void idle(Pose p, int cyc) {
        p.x = 22;
        if (cyc < 80) {
            p.eyes = (cyc % 40 < 3) ? 1 : 0;
            p.mouth = ((cyc / 20) % 2 == 0) ? 0 : 2;
            p.tail = (cyc / 4) % 2;
            p.ear = ((cyc / 12) % 3) - 1;
        } else if (cyc < 120) {
            boolean up = ((cyc - 80) / 3) % 2 == 0;
            p.eyes = 2; p.mouth = 1; p.arm = 1;
            p.bob = up ? 3 : 0; p.ear = up ? 1 : -1; p.tail = up ? 1 : 0;
            p.dir = (((cyc - 80) / 14) % 2 == 0) ? 1 : -1;
        } else if (cyc < 160) {
            p.dir = -1; p.eyes = (cyc % 40 < 3) ? 1 : 0; p.mouth = 2; p.tail = (cyc / 5) % 2;
        } else {
            int ph = cyc - 160;
            if (ph < 40) { p.dir = 1; p.x = 14 + ph * 16 / 40; }
            else { p.dir = -1; p.x = 30 - (ph - 40) * 16 / 40; }
            p.bob = (ph % 6 < 2) ? 1 : 0; p.mouth = 2; p.tail = (ph / 3) % 2; p.ear = (ph % 6 < 3) ? 1 : 0;
        }
    }

    //Hauptbildschirm
    void renderHome(Lcd c) {
        Art.strip(c, sel, (tick / 4) % 2 == 0);
        Art.ground(c);
        for (int i = 0; i < poops; i++) Art.poop(c, POOP_X[i], 41);
        Pose p = new Pose();
        if (dying) {
            p.eyes = (tick / 6) % 2 == 0 ? 5 : 4; p.mouth = 3; p.ear = 1; p.tail = 1;
            Art.snoopy(c, p);
            if ((tick / 4) % 2 == 0) c.text("HILFE " + dyingLeft, 1, 10, DK);
        } else if (naughty) {
            boolean up = (tick / 3) % 2 == 0;
            p.eyes = 3; p.mouth = up ? 1 : 0; p.bob = up ? 3 : 0; p.arm = 1; p.tail = up ? 1 : 0;
            p.ear = up ? 1 : -1; p.dir = ((tick / 12) % 2 == 0) ? 1 : -1;
            Art.snoopy(c, p);
        } else if (energy == 0) {
            p.eyes = 1; p.mouth = ((tick / 8) % 2 == 0) ? 1 : 0; p.ear = 1;
            Art.snoopy(c, p);
            Art.zzz(c, tick, 33, 15);
        } else if (hunger <= 1 || fun <= 1) {
            p.eyes = 4; p.mouth = 3; p.ear = 1; p.tail = 1;
            Art.snoopy(c, p);
        } else {
            idle(p, tick % 240);
            Art.snoopy(c, p);
        }
        if (woodT >= 0)
            Art.woodstock(c, 50 - woodT, 14 + (int) Math.round(1.5 * Math.sin(woodT * 0.5)), (woodT % 4 < 2) ? 0 : 1, -1);
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

    //Essen
    void renderFeed(Lcd c) {
        Art.strip(c, 0, true);
        Art.ground(c);
        c.text(feedChoice == 0 ? "NAPF" : "PIZZA", 1, 10, DK);
        double frac = eatT < 0 ? 1.0 : Math.max(0.0, (24 - eatT) / 24.0);
        if (feedChoice == 0) Art.bowl(c, 33, 41, frac); else Art.pizza(c, 33, 35, frac);
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
        Art.snoopy(c, p);
        if (happyT > 0) c.heart(p.x + 2, 22 - (30 - Math.min(30, happyT)) / 3, true);
    }

    //Spielen
    void renderPlay(Lcd c) {
        Art.strip(c, 1, true);
        Art.ground(c);
        Art.bush(c, 6);
        Art.bush(c, 42);
        Pose p = new Pose();
        p.x = 22;
        if (ps == 3) {
            p.eyes = 1; p.mouth = ((tick / 8) % 2 == 0) ? 1 : 0; p.ear = 1;
            c.text("MUEDE", 1, 10, DK);
            Art.zzz(c, tick, 33, 15);
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
                Art.woodstock(c, hx, 41 - Math.min(pt - 5, 8), (pt % 4 < 2) ? 0 : 1, hidden == 0 ? 1 : -1);
                Art.bush(c, hx);
            }
        } else {
            boolean good = wins >= 2;
            if (good) {
                boolean up = (pt / 3) % 2 == 0;
                p.eyes = 2; p.mouth = 1; p.arm = 1; p.bob = up ? 3 : 0; p.ear = up ? 1 : -1; p.tail = up ? 1 : 0;
            } else { p.eyes = 4; p.mouth = 3; p.ear = 1; }
            c.text(good ? "GEWONNEN" : "VERLOREN", 3, 10, DK);
        }
        Art.snoopy(c, p);
        if (ps != 3 && ps != 2) {
            for (int i = 0; i < 3; i++) {
                boolean done = i < round || (ps == 1 && i == round && pt >= 5);
                int sx = 37 + i * 4;
                if (done) {
                    c.rect(sx, 11, 3, 3, DK);
                    if (!won[i]) c.set(sx + 1, 12, BG);
                } else c.set(sx + 1, 12, DK);
            }
        }
    }

    //Schlafen
    void renderSleep(Lcd c) {
        Art.strip(c, 2, true);
        Art.doghouse(c);
        Art.ground(c);
        c.outlined(14, 21, 2, 3, WH);
        c.outlined(18, 20, 2, 3, WH);
        c.outlined(23, 23, 9, 3, WH);
        c.ellipse(29, 24, 2.5, 3.5, DK);
        c.ellipse(31, 20, 5.5, 4.5, DK);
        c.ellipse(34.5, 18, 3.5, 3.5, DK);
        c.ellipse(31, 20, 4.5, 3.5, WH);
        c.ellipse(34.5, 18, 2.5, 2.5, WH);
        c.ellipse(36, 15.5, 2, 1.5, DK);
        c.ellipse(28, 24, 2.5, 3.5, DK);
        c.set(31, 19, DK); c.set(32, 19, DK); c.set(33, 19, DK);
        Art.zzz(c, tick, 12, 16);
        for (int i = 0; i < 4; i++) {
            c.rect(1 + i * 4, 11, 3, 3, i < energy ? DK : BG);
            if (i >= energy) { c.rect(1 + i * 4, 11, 3, 3, DK); c.set(2 + i * 4, 12, BG); }
        }
    }

    //Sauber
    void renderClean(Lcd c) {
        Art.strip(c, 3, true);
        Art.ground(c);
        if (washT < 20) for (int i = 0; i < poops; i++) Art.poop(c, POOP_X[i], 41);
        Pose p = new Pose();
        p.x = 20;
        if (washT >= 0) {
            p.eyes = 1; p.mouth = 2; p.ear = (washT % 4 < 2) ? 1 : -1; p.tail = washT % 2; p.arm = 1;
            Art.snoopy(c, p);
            for (int i = 0; i < 14; i++) c.rect((i * 7 + 3) % 46, 10 + ((washT * 3 + i * 11) % 34), 1, 2, MD);
            for (int i = 0; i < 5; i++) Art.ring(c, 6 + i * 9, 40 - ((washT * 2 + i * 9) % 28));
        } else {
            if (poops == 0) {
                p.eyes = (tick % 40 < 3) ? 1 : 0; p.mouth = 2; p.tail = (tick / 4) % 2;
                c.text("SAUBER", 1, 10, DK);
                if ((tick / 4) % 2 == 0) { c.set(38, 18, DK); c.set(37, 19, DK); c.set(39, 19, DK); c.set(38, 20, DK); }
            } else { p.eyes = 4; p.mouth = 3; p.ear = 1; c.text("IGITT", 1, 10, DK); }
            Art.snoopy(c, p);
        }
    }

    //Disziplin
    void renderDisc(Lcd c) {
        Art.strip(c, 4, true);
        Art.ground(c);
        Pose p = new Pose();
        p.x = 17;
        if (scoldT >= 0 && scoldT < 10) {
            p.eyes = 4; p.mouth = 3; p.ear = 1;
            c.rect(40, 18, 3, 11, DK); c.rect(40, 31, 3, 3, DK);
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
        Art.snoopy(c, p);
        if (happyT > 0) c.heart(p.x + 2, 22 - (30 - Math.min(30, happyT)) / 3, true);
    }

    //Tod
    void renderDeath(Lcd c) {
        Art.ground(c);
        if (t < 16) {
            Pose p = new Pose();
            p.eyes = 5; p.mouth = 3; p.ear = 1; p.tail = 1;
            if (t % 4 != 3) Art.snoopy(c, p);
        } else if (t < 26) {
            int r = (t - 16) * 2 + 4;
            for (int i = 0; i < 10; i++) {
                double a = i * Math.PI / 5;
                c.line((int) Math.round(24 + Math.cos(a) * r), (int) Math.round(30 + Math.sin(a) * r),
                       (int) Math.round(24 + Math.cos(a) * (r + 4)), (int) Math.round(30 + Math.sin(a) * (r + 4)), DK);
            }
        } else {
            Art.ghost(c, Math.min(10, (t - 26) / 3), tick);
        }
    }

    //Grab
    void renderDead(Lcd c) {
        c.ellipse(41, 6, 3.5, 3.5, MD);
        c.ellipse(42.5, 5, 3, 3, BG);
        if ((tick / 6) % 2 == 0) { c.set(3, 4, WH); c.set(10, 9, WH); c.set(44, 14, WH); }
        else { c.set(4, 6, WH); c.set(8, 11, WH); c.set(45, 12, WH); }
        Art.grave(c, 16, 4);
        Art.flower(c, 9, 21);
        Art.flower(c, 12, 22);
        Art.woodstock(c, 39, 22, 1, -1);
        c.set(37, 23 + (tick / 4) % 3, MD);
        int page = (tick / 30) % 2;
        String label = page == 0 ? "LEBENSZEIT" : (newRecord ? "NEUER REKORD" : "REKORD");
        String time = fmt(page == 0 ? lived() : bestSec);
        c.textC(label, 28, DK);
        c.textC(time, 35, DK);
        if (confirmT > 0) { if ((tick / 3) % 2 == 0) c.textC("NOCHMAL B", 42, DK); }
        else if ((tick / 5) % 2 == 0) c.textC("B: NEUES EI", 42, DK);
    }

    //Status
    void renderStatus(Lcd c) {
        Art.strip(c, 5, true);
        String[] names = {"SATT", "SPASS", "BRAV", "KRAFT"};
        int[] v = {hunger, fun, disc, energy};
        for (int i = 0; i < 4; i++) {
            int y = 12 + i * 7;
            c.text(names[i], 1, y, DK);
            for (int h = 0; h < 4; h++) c.heart(26 + h * 5, y, h < v[i]);
        }
        c.rect(0, 39, Lcd.W, 1, DK);
        c.textC(age(), 41, DK);
    }
}
