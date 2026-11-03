import javax.swing.JPanel;
import java.awt.*;

class LcdScreen extends JPanel {
    static final int COLS = 64;
    static final int ROWS = 56;

    private final TamagotchiFrame game;

    LcdScreen(TamagotchiFrame game) {
        this.game = game;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        boolean night = (game.screen == Screen.SLEEP && game.lightsOut)
                || game.screen == Screen.DEAD;
        Color bg = night ? new Color(38, 46, 36) : new Color(206, 216, 162);
        Color on = night ? new Color(216, 226, 172) : new Color(36, 40, 32);

        // backlight glow
        g2.setPaint(new RadialGradientPaint(
                getWidth() / 2f, getHeight() / 2f, getWidth() * 0.75f,
                new float[]{0f, 1f},
                new Color[]{bg.brighter(), bg}
        ));
        g2.fillRect(0, 0, getWidth(), getHeight());

        int cell = Math.min(getWidth() / COLS, getHeight() / ROWS);
        if (cell <= 0) {
            g2.dispose();
            return;
        }

        int x0 = (getWidth() - COLS * cell) / 2;
        int y0 = (getHeight() - ROWS * cell) / 2;

        int tick = game.animTick;
        boolean blink = (tick / 3) % 2 == 0;

        switch (game.screen) {
            case EGG:
                drawEgg(g2, cell, x0, y0, on, tick);
                break;
            case HOME:
                drawHome(g2, cell, x0, y0, on, bg, tick, blink);
                break;
            case FEED:
                drawFeed(g2, cell, x0, y0, on, tick);
                break;
            case SLEEP:
                drawSleep(g2, cell, x0, y0, on, tick, night, blink);
                break;
            case PLAY:
                drawPlay(g2, cell, x0, y0, on, tick, blink);
                break;
            case CLEAN:
                drawClean(g2, cell, x0, y0, on, tick);
                break;
            case DISCIPLINE:
                drawDiscipline(g2, cell, x0, y0, on, tick, blink);
                break;
            case STATUS:
                drawStatus(g2, cell, x0, y0, on, tick);
                break;
            case DEAD:
                drawDead(g2, cell, x0, y0, on, tick, blink);
                break;
        }

        drawParticles(g2, cell, x0, y0, on);

        // LCD pixel grid
        g2.setColor(new Color(0, 0, 0, 16));
        for (int x = 0; x <= COLS; x++) {
            g2.drawLine(x0 + x * cell, y0, x0 + x * cell, y0 + ROWS * cell);
        }
        for (int y = 0; y <= ROWS; y++) {
            g2.drawLine(x0, y0 + y * cell, x0 + COLS * cell, y0 + y * cell);
        }

        // vignette
        g2.setPaint(new RadialGradientPaint(
                getWidth() / 2f, getHeight() / 2f, getWidth() * 0.72f,
                new float[]{0.55f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 70)}
        ));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // evolve flash
        if (game.evolveTimer > 0 && game.evolveTimer % 2 == 0) {
            fillBlock(g2, cell, x0, y0, on, 0, 0, COLS, ROWS);
        }

        // screen wipe transition
        if (game.wipe > 0) {
            int closed = game.wipe > 5 ? (10 - game.wipe) : game.wipe;
            int cols = closed * COLS / 10;
            fillBlock(g2, cell, x0, y0, on, 0, 0, cols, ROWS);
            fillBlock(g2, cell, x0, y0, on, COLS - cols, 0, cols, ROWS);
        }

        // glass reflection
        g2.setColor(new Color(255, 255, 255, 22));
        g2.fillPolygon(
                new int[]{0, getWidth() / 3, 0, 0},
                new int[]{0, 0, getHeight() / 2, getHeight() / 2},
                4
        );

        g2.dispose();
    }

    // ---------- drawing helpers ----------

    private void drawSprite(Graphics2D g2, int cell, int x0, int y0,
                            Color color, String[] spr, int gx, int gy) {
        g2.setColor(color);
        for (int r = 0; r < spr.length; r++) {
            String row = spr[r];
            for (int c = 0; c < row.length(); c++) {
                if (row.charAt(c) == '#') {
                    g2.fillRect(x0 + (gx + c) * cell, y0 + (gy + r) * cell, cell - 1, cell - 1);
                }
            }
        }
    }

    private void fillBlock(Graphics2D g2, int cell, int x0, int y0,
                           Color color, int gx, int gy, int w, int h) {
        g2.setColor(color);
        g2.fillRect(x0 + gx * cell, y0 + gy * cell, w * cell, h * cell);
    }

    private void drawGround(Graphics2D g2, int cell, int x0, int y0, Color on) {
        for (int x = 0; x < COLS; x += 2) {
            fillBlock(g2, cell, x0, y0, on, x, 42, 1, 1);
        }
    }

    private void drawCloud(Graphics2D g2, int cell, int x0, int y0, Color on, int tick) {
        int cx = 54 - ((tick / 2) % 72);
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.CLOUD, cx, 12);
    }

    private String[] snoopyIdle(int tick) {
        if (!game.adult) {
            return (tick / 4) % 2 == 0
                    ? SpriteLibrary.SNOOPY_BABY
                    : SpriteLibrary.SNOOPY_BABY_2;
        }
        return (tick / 4) % 2 == 0
                ? SpriteLibrary.SNOOPY_IDLE_1
                : SpriteLibrary.SNOOPY_IDLE_2;
    }

    private String[] snoopyMain(int tick) {
        if (game.pet.sick) {
            return SpriteLibrary.SNOOPY_SICK;
        }
        return snoopyIdle(tick);
    }

    private void drawParticles(Graphics2D g2, int cell, int x0, int y0, Color on) {
        for (Particle p : game.particles) {
            int px = (int) p.x;
            int py = (int) p.y;

            switch (p.kind) {
                case 0:
                    drawSprite(g2, cell, x0, y0, on, SpriteLibrary.HEART, px, py);
                    break;
                case 1:
                    drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SPARKLE, px, py);
                    break;
                case 2:
                    drawSprite(g2, cell, x0, y0, on, SpriteLibrary.BUBBLE, px, py);
                    break;
                case 3:
                    fillBlock(g2, cell, x0, y0, on, px, py, 1, 1);
                    break;
                case 4:
                    fillBlock(g2, cell, x0, y0, on, px, py, 1, 2);
                    break;
                case 5:
                    drawSprite(g2, cell, x0, y0, on, SpriteLibrary.NOTE, px, py);
                    break;
                default:
                    fillBlock(g2, cell, x0, y0, on, px, py, 1, 1);
                    break;
            }
        }
    }

    // ---------- pixel font ----------

    private String[] glyph(char c) {
        switch (c) {
            case '0': return new String[]{"###", "#.#", "#.#", "#.#", "###"};
            case '1': return new String[]{".#.", "##.", ".#.", ".#.", "###"};
            case '2': return new String[]{"###", "..#", "###", "#..", "###"};
            case '3': return new String[]{"###", "..#", "###", "..#", "###"};
            case '4': return new String[]{"#.#", "#.#", "###", "..#", "..#"};
            case '5': return new String[]{"###", "#..", "###", "..#", "###"};
            case '6': return new String[]{"###", "#..", "###", "#.#", "###"};
            case '7': return new String[]{"###", "..#", "..#", "..#", "..#"};
            case '8': return new String[]{"###", "#.#", "###", "#.#", "###"};
            case '9': return new String[]{"###", "#.#", "###", "..#", "###"};
            case 'A': return new String[]{".#.", "#.#", "###", "#.#", "#.#"};
            case 'B': return new String[]{"##.", "#.#", "##.", "#.#", "##."};
            case 'D': return new String[]{"##.", "#.#", "#.#", "#.#", "##."};
            case 'E': return new String[]{"###", "#..", "##.", "#..", "###"};
            case 'G': return new String[]{".##", "#..", "#.#", "#.#", ".##"};
            case ' ': return new String[]{"...", "...", "...", "...", "..."};
            default:  return null;
        }
    }

    private void drawText(Graphics2D g2, int cell, int x0, int y0,
                          Color on, String text, int gx, int gy) {
        int cx = gx;
        for (int i = 0; i < text.length(); i++) {
            String[] gl = glyph(text.charAt(i));
            if (gl != null) {
                drawSprite(g2, cell, x0, y0, on, gl, cx, gy);
            }
            cx += 4;
        }
    }

    // ---------- scenes ----------

    private void drawEgg(Graphics2D g2, int cell, int x0, int y0, Color on, int tick) {
        if (!game.pet.hatched) {
            int wx = game.wiggle > 0 ? ((tick % 2 == 0) ? 1 : -1) : 0;
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.EGG_1, 26 + wx, 20);
        } else {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.EGG_2, 26, 20);
            if (game.busy < 3) {
                fillBlock(g2, cell, x0, y0, on, 0, 0, COLS, ROWS);
            }
        }
    }

    private void drawHome(Graphics2D g2, int cell, int x0, int y0,
                          Color on, Color bg, int tick, boolean blink) {
        int[][] pos = {{8, 2}, {26, 2}, {44, 2}, {8, 46}, {26, 46}, {44, 46}};

        for (int i = 0; i < pos.length; i++) {
            if (i == game.menuIndex && blink) {
                fillBlock(g2, cell, x0, y0, on, pos[i][0] - 1, pos[i][1] - 1, 10, 10);
                drawSprite(g2, cell, x0, y0, bg, SpriteLibrary.ICONS[i], pos[i][0], pos[i][1]);
            } else {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.ICONS[i], pos[i][0], pos[i][1]);
            }
        }

        drawCloud(g2, cell, x0, y0, on, tick);
        drawGround(g2, cell, x0, y0, on);
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.DOGHOUSE, 4, 26);

        if (game.pet.sick) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SNOOPY_SICK, 34, 26);
            int fx = 30 + ((tick / 2) % 6);
            int fy = 22 + ((tick / 3) % 3);
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.FLY, fx, fy);
        } else if (game.adult) {
            drawSprite(g2, cell, x0, y0, on, snoopyIdle(tick), 34, 26);
        } else {
            drawSprite(g2, cell, x0, y0, on, snoopyIdle(tick), 38, 32);
        }

        if (blink && game.pet.happiness > 60) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.HEART, 26, 16);
        }

        if (game.pet.cleanliness < 80) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.POOP, 24, 38);
        }

        // needs indicators
        if (blink) {
            if (game.pet.fullness < 40) {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.ICON_FEED, 56, 12);
            }
            if (game.pet.energy < 30) {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.ZZZ, 56, 20);
            }
            if (game.pet.happiness < 40) {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.CROSS, 56, 28);
            }
            if (game.pet.sick) {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SKULL, 56, 36);
            }
        }
    }

    private void drawFeed(Graphics2D g2, int cell, int x0, int y0, Color on, int tick) {
        drawCloud(g2, cell, x0, y0, on, tick);
        drawGround(g2, cell, x0, y0, on);

        drawSprite(g2, cell, x0, y0, on, snoopyMain(tick), 36, 26);

        if (game.busy > 0 && game.feedOption == 0) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.BOWL_EMPTY, 8, 38);
        } else {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.BOWL_FULL, 8, 38);
        }

        if (!(game.busy > 0 && game.feedOption == 1 && game.busy < 6)) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.BONE, 8, 16);
        }

        if (game.feedOption == 0) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.CURSOR, 8, 44);
        } else {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.CURSOR, 8, 24);
        }

        if (game.busy > 0 && game.busy < 6) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.HEART, 28, 18);
        }
    }

    private void drawSleep(Graphics2D g2, int cell, int x0, int y0,
                           Color on, int tick, boolean night, boolean blink) {
        if (night) {
            fillBlock(g2, cell, x0, y0, on, 6, 6, 1, 1);
            fillBlock(g2, cell, x0, y0, on, 58, 10, 1, 1);
            fillBlock(g2, cell, x0, y0, on, 12, 16, 1, 1);
            fillBlock(g2, cell, x0, y0, on, 44, 14, 1, 1);
            if (blink) {
                fillBlock(g2, cell, x0, y0, on, 52, 40, 1, 1);
            }
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.MOON, 48, 2);
        }

        drawGround(g2, cell, x0, y0, on);
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.DOGHOUSE, 4, 26);
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SNOOPY_ROOF_SLEEP, 6, 21);

        int zo = (tick / 2) % 2;
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.ZZZ, 26, 12 - zo);
    }

    private void drawPlay(Graphics2D g2, int cell, int x0, int y0,
                          Color on, int tick, boolean blink) {
        drawGround(g2, cell, x0, y0, on);

        if (!game.gameActive) {
            drawSprite(g2, cell, x0, y0, on, snoopyMain(tick), 24, 26);
            if (blink) {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.NOTE, 20, 14);
                drawText(g2, cell, x0, y0, on, "B", 30, 46);
            }
            return;
        }

        // track
        fillBlock(g2, cell, x0, y0, on, 8, 6, 49, 1);
        fillBlock(g2, cell, x0, y0, on, 28, 4, 9, 1);
        fillBlock(g2, cell, x0, y0, on, 28, 8, 9, 1);

        // marker
        int mx = 8 + (int) (game.marker * 48);
        fillBlock(g2, cell, x0, y0, on, mx, 3, 1, 7);

        // score hearts
        for (int i = 0; i < game.gameScore && i < 10; i++) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.HEART, i * 8, 0);
        }

        // round pips
        for (int i = 0; i < game.gameRound; i++) {
            fillBlock(g2, cell, x0, y0, on, 60 - i * 2, 0, 1, 1);
        }

        // dancing snoopy
        String[] dance;
        if (game.pet.sick) {
            dance = SpriteLibrary.SNOOPY_SICK;
        } else if (game.adult) {
            dance = (tick / 2) % 2 == 0
                    ? SpriteLibrary.SNOOPY_DANCE_1
                    : SpriteLibrary.SNOOPY_DANCE_2;
        } else {
            dance = snoopyIdle(tick);
        }
        drawSprite(g2, cell, x0, y0, on, dance, 24, 26);

        // result icon
        if (game.resultTimer > 0) {
            if (game.resultKind == 1) {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.HEART, 29, 14);
            } else if (game.resultKind == 2) {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.NOTE, 29, 14);
            } else {
                drawSprite(g2, cell, x0, y0, on, SpriteLibrary.CROSS, 29, 14);
            }
        }
    }

    private void drawClean(Graphics2D g2, int cell, int x0, int y0, Color on, int tick) {
        drawGround(g2, cell, x0, y0, on);
        drawSprite(g2, cell, x0, y0, on, snoopyMain(tick), 32, 26);

        boolean poop = game.pet.cleanliness < 80 && !(game.busy > 0 && game.busy < 4);
        if (poop) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.POOP, 10, 38);
        }

        if (game.busy > 0) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SHOWER, 10, 4);
            for (int wy = 10; wy < 34; wy += 4) {
                if (((wy + tick) % 8) < 4) {
                    fillBlock(g2, cell, x0, y0, on, 13, wy, 1, 2);
                    fillBlock(g2, cell, x0, y0, on, 18, wy + 2, 1, 2);
                }
            }
        }
    }

    private void drawDiscipline(Graphics2D g2, int cell, int x0, int y0,
                                Color on, int tick, boolean blink) {
        drawGround(g2, cell, x0, y0, on);
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SNOOPY_SAD, 32, 26);

        if (game.busy > 0 && tick % 2 == 0) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.EXCLAIM_BIG, 26, 10);
        } else if (blink) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SPEECH, 14, 10);
        }
    }

    private void drawStatus(Graphics2D g2, int cell, int x0, int y0, Color on, int tick) {
        String[][] icons = {
                SpriteLibrary.ICON_STATUS,
                SpriteLibrary.ICON_FEED,
                SpriteLibrary.ICON_CLEAN,
                SpriteLibrary.ICON_LIGHT
        };

        int[] stats = {
                game.pet.happiness,
                game.pet.fullness,
                game.pet.cleanliness,
                game.pet.energy
        };

        for (int r = 0; r < 4; r++) {
            int y = 4 + r * 10;
            drawSprite(g2, cell, x0, y0, on, icons[r], 2, y);

            for (int b = 0; b < 10; b++) {
                if (stats[r] / 10 > b) {
                    fillBlock(g2, cell, x0, y0, on, 14 + b * 4, y + 1, 3, 6);
                } else {
                    fillBlock(g2, cell, x0, y0, on, 14 + b * 4, y + 1, 1, 6);
                }
            }
        }

        if (!game.adult) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SNOOPY_BABY, 52, 4);
        } else {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SNOOPY_IDLE_1, 48, 2);
        }

        drawText(g2, cell, x0, y0, on, "AGE " + game.days() + "D", 2, 46);

        if (game.pet.sick && (tick / 3) % 2 == 0) {
            drawSprite(g2, cell, x0, y0, on, SpriteLibrary.SKULL, 54, 42);
        }
    }

    private void drawDead(Graphics2D g2, int cell, int x0, int y0,
                          Color on, int tick, boolean blink) {
        fillBlock(g2, cell, x0, y0, on, 6, 6, 1, 1);
        fillBlock(g2, cell, x0, y0, on, 58, 10, 1, 1);
        fillBlock(g2, cell, x0, y0, on, 10, 14, 1, 1);

        drawGround(g2, cell, x0, y0, on);
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.TOMBSTONE, 23, 22);
        drawText(g2, cell, x0, y0, on, "RIP", 26, 27);

        int gy = 5 + ((tick / 4) % 2);
        drawSprite(g2, cell, x0, y0, on, SpriteLibrary.GHOST, 24, gy);

        drawText(g2, cell, x0, y0, on, "AGE " + game.days() + "D", 2, 48);
        if (blink) {
            drawText(g2, cell, x0, y0, on, "B", 58, 48);
        }
    }
}