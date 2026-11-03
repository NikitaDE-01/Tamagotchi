import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

class TamagotchiFrame extends JFrame {
    static final Screen[] MENU = {
            Screen.FEED,
            Screen.SLEEP,
            Screen.PLAY,
            Screen.CLEAN,
            Screen.DISCIPLINE,
            Screen.STATUS
    };

    final Tamagotchi pet = new Tamagotchi();
    final List<Particle> particles = new ArrayList<>();

    Screen screen = Screen.EGG;
    Screen pendingScreen = Screen.HOME;
    int menuIndex = 0;
    int feedOption = 0;
    boolean lightsOut = false;
    int busy = 0;
    int wiggle = 0;
    int animTick = 0;
    int wipe = 0;

    // growth
    int ageTicks = 0;
    boolean adult = false;
    int evolveTimer = 0;

    // mini game
    boolean gameActive = false;
    int gameRound = 0;
    int gameScore = 0;
    double marker = 0;
    double markerDir = 1;
    int resultTimer = 0;
    int resultKind = 0;

    private final LcdScreen lcd;
    private final javax.swing.Timer animTimer;
    private final javax.swing.Timer modelTimer;
    private final boolean shapedWindow;
    private Point dragOffset;

    TamagotchiFrame() {
        setTitle("Snoopy Tamagotchi Deluxe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setFocusable(true);

        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        shapedWindow = gd.isWindowTranslucencySupported(
                java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT
        );

        if (shapedWindow) {
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));
        }

        setSize(460, 660);
        setLocationRelativeTo(null);

        JPanel root = new ShellPanel();
        root.setLayout(null);
        setContentPane(root);

        // brown top tab = exit button
        RoundButton tab = new RoundButton(new Color(120, 70, 40));
        tab.setBounds(178, 4, 104, 26);
        tab.setToolTipText("Exit");
        tab.addActionListener(e -> System.exit(0));
        root.add(tab);

        lcd = new LcdScreen(this);
        lcd.setBounds(100, 124, 260, 222);
        root.add(lcd);

        // exactly 3 buttons: A, B, C
        Color[] buttonColors = {
                new Color(40, 90, 200),
                new Color(220, 40, 40),
                new Color(240, 200, 40)
        };
        int[][] buttonBounds = {
                {124, 556, 52, 52},
                {204, 566, 52, 52},
                {284, 556, 52, 52}
        };

        for (int i = 0; i < buttonColors.length; i++) {
            RoundButton b = new RoundButton(buttonColors[i]);
            b.setBounds(
                    buttonBounds[i][0],
                    buttonBounds[i][1],
                    buttonBounds[i][2],
                    buttonBounds[i][3]
            );
            final int index = i;
            b.addActionListener(e -> press(index));
            root.add(b);
        }

        installDrag(root);
        installDrag(lcd);

        // animation / game loop
        animTimer = new javax.swing.Timer(110, e -> {
            animTick++;

            if (wipe > 0) {
                wipe--;
                if (wipe == 5) {
                    screen = pendingScreen;
                }
            }

            if (wiggle > 0) {
                wiggle--;
            }

            if (busy > 0) {
                busy--;
                if (busy == 0) {
                    if (screen == Screen.EGG && pet.hatched) {
                        goScreen(Screen.HOME, false);
                        SoundPlayer.clickOk();
                        spawnConfetti();
                    } else if (screen == Screen.CLEAN) {
                        pet.shower();
                        SoundPlayer.cleanDone();
                        spawnSparkles();
                    }
                }
            }

            if (evolveTimer > 0) {
                evolveTimer--;
            }

            // mini game marker
            if (screen == Screen.PLAY && gameActive && resultTimer == 0) {
                marker += 0.02 * markerDir;
                if (marker > 1) {
                    marker = 1;
                    markerDir = -1;
                }
                if (marker < 0) {
                    marker = 0;
                    markerDir = 1;
                }
            }

            if (resultTimer > 0) {
                resultTimer--;
                if (resultTimer == 0 && gameRound <= 0) {
                    endGame();
                }
            }

            // particles
            for (Particle p : particles) {
                p.x += p.vx;
                p.y += p.vy;
                if (p.kind == 3 || p.kind == 6) {
                    p.vy += 0.05;
                }
                p.life--;
            }
            particles.removeIf(p -> p.life <= 0 || p.y > LcdScreen.ROWS + 2);

            lcd.repaint();
        });
        animTimer.start();

        // stat / life simulation
        modelTimer = new javax.swing.Timer(1500, e -> {
            pet.sleeping = (screen == Screen.SLEEP && lightsOut);
            pet.tick();

            if (pet.hatched && !pet.dead) {
                ageTicks++;

                if (!adult && ageTicks >= 25) {
                    adult = true;
                    evolveTimer = 10;
                    SoundPlayer.evolve();
                    spawnConfetti();
                }

                if (ageTicks % 40 == 0
                        && (pet.fullness < 30 || pet.cleanliness < 50 || pet.energy < 25)) {
                    SoundPlayer.call();
                }

                if (pet.sick && ageTicks % 10 == 0) {
                    SoundPlayer.call();
                }

                if (pet.sick && pet.sickProgress >= 12) {
                    die();
                }
            }
        });
        modelTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                        press(0);
                        break;
                    case KeyEvent.VK_B:
                        press(1);
                        break;
                    case KeyEvent.VK_C:
                        press(2);
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        break;
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                applyWindowShape();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                applyWindowShape();
            }
        });
    }

    int days() {
        return ageTicks / 20;
    }

    private void goScreen(Screen s, boolean sound) {
        if (sound) {
            SoundPlayer.playModeSound(s);
        }
        if (s == screen && wipe == 0) {
            return;
        }
        pendingScreen = s;
        wipe = 10;
    }

    void press(int index) {
        if (index == 0) {
            pressA();
        } else if (index == 1) {
            pressB();
        } else {
            pressC();
        }
    }

    private void pressA() {
        if (screen == Screen.DEAD) {
            return;
        }

        SoundPlayer.clickMove();
        switch (screen) {
            case EGG:
                wiggle = 6;
                break;
            case HOME:
                menuIndex = (menuIndex + 1) % MENU.length;
                break;
            case FEED:
                if (busy == 0) {
                    feedOption = 1 - feedOption;
                }
                break;
            case PLAY:
                if (!gameActive) {
                    spawnNotes();
                }
                break;
            default:
                break;
        }
    }

    private void pressB() {
        switch (screen) {
            case EGG:
                if (!pet.hatched) {
                    pet.hatch();
                    busy = 8;
                    SoundPlayer.hatch();
                }
                break;

            case HOME:
                goScreen(MENU[menuIndex], true);
                if (MENU[menuIndex] == Screen.SLEEP) {
                    lightsOut = false;
                }
                break;

            case FEED:
                if (busy == 0) {
                    busy = 8;
                    SoundPlayer.eat();
                    if (feedOption == 0) {
                        pet.eatMeal();
                    } else {
                        pet.eatSnack();
                    }
                    spawnCrumbs();
                }
                break;

            case SLEEP:
                lightsOut = !lightsOut;
                if (lightsOut) {
                    SoundPlayer.sleepOn();
                } else {
                    SoundPlayer.sleepOff();
                }
                break;

            case PLAY:
                if (!gameActive) {
                    gameActive = true;
                    gameRound = 5;
                    gameScore = 0;
                    marker = 0;
                    markerDir = 1;
                    resultTimer = 0;
                    SoundPlayer.gameStart();
                } else if (resultTimer == 0) {
                    double d = Math.abs(marker - 0.5);
                    gameRound--;
                    resultTimer = 8;

                    if (d < 0.08) {
                        gameScore += 2;
                        resultKind = 1;
                        SoundPlayer.perfect();
                        spawnHearts();
                    } else if (d < 0.2) {
                        gameScore += 1;
                        resultKind = 2;
                        SoundPlayer.good();
                        spawnNotes();
                    } else {
                        resultKind = 3;
                        SoundPlayer.miss();
                        spawnSweat();
                    }
                }
                break;

            case CLEAN:
                if (busy == 0) {
                    SoundPlayer.clickOk();
                    if (pet.cleanliness < 80) {
                        busy = 10;
                        spawnBubbles();
                    } else {
                        pet.shower();
                        spawnSparkles();
                    }
                }
                break;

            case DISCIPLINE:
                if (busy == 0) {
                    busy = 8;
                    SoundPlayer.scold();
                    pet.scold();
                    spawnSweat();
                }
                break;

            case DEAD:
                resetGame();
                break;

            default:
                SoundPlayer.clickOk();
                break;
        }
    }

    private void pressC() {
        if (screen == Screen.DEAD) {
            return;
        }

        SoundPlayer.clickBack();

        if (screen == Screen.PLAY && gameActive) {
            gameActive = false;
            return;
        }

        if (screen != Screen.EGG && screen != Screen.HOME) {
            lightsOut = false;
            pet.sleeping = false;
            goScreen(Screen.HOME, false);
        }
    }

    private void endGame() {
        gameActive = false;
        pet.addHappiness(gameScore * 3);
        SoundPlayer.gameOver(gameScore >= 6);
        if (gameScore >= 6) {
            spawnConfetti();
        }
    }

    private void die() {
        pet.dead = true;
        pet.sleeping = false;
        gameActive = false;
        lightsOut = false;
        goScreen(Screen.DEAD, false);
        SoundPlayer.dead();
    }

    private void resetGame() {
        pet.reset();
        particles.clear();
        ageTicks = 0;
        adult = false;
        evolveTimer = 0;
        gameActive = false;
        gameRound = 0;
        gameScore = 0;
        resultTimer = 0;
        menuIndex = 0;
        feedOption = 0;
        lightsOut = false;
        busy = 0;
        goScreen(Screen.EGG, false);
        SoundPlayer.hatch();
    }

    // ---------- particles ----------

    private void spawnHearts() {
        for (int i = 0; i < 4; i++) {
            particles.add(new Particle(28 + Math.random() * 10, 22,
                    (Math.random() - 0.5) * 0.5, -0.4 - Math.random() * 0.3, 16, 0));
        }
    }

    private void spawnSparkles() {
        for (int i = 0; i < 8; i++) {
            particles.add(new Particle(8 + Math.random() * 40, 20 + Math.random() * 20,
                    (Math.random() - 0.5) * 0.4, -0.3 - Math.random() * 0.3, 14, 1));
        }
    }

    private void spawnBubbles() {
        for (int i = 0; i < 6; i++) {
            particles.add(new Particle(10 + Math.random() * 20, 34,
                    (Math.random() - 0.5) * 0.4, -0.4 - Math.random() * 0.4, 18, 2));
        }
    }

    private void spawnConfetti() {
        for (int i = 0; i < 14; i++) {
            particles.add(new Particle(4 + Math.random() * 56, -2 - Math.random() * 6,
                    (Math.random() - 0.5) * 0.6, 0.3 + Math.random() * 0.4, 40, 3));
        }
    }

    private void spawnSweat() {
        for (int i = 0; i < 4; i++) {
            particles.add(new Particle(34 + Math.random() * 8, 24,
                    0.3 + Math.random() * 0.4, 0.2, 12, 4));
        }
    }

    private void spawnNotes() {
        for (int i = 0; i < 3; i++) {
            particles.add(new Particle(26 + Math.random() * 12, 20,
                    (Math.random() - 0.5) * 0.4, -0.4, 16, 5));
        }
    }

    private void spawnCrumbs() {
        for (int i = 0; i < 5; i++) {
            particles.add(new Particle(12 + Math.random() * 8, 34,
                    (Math.random() - 0.5) * 0.6, -0.3, 12, 6));
        }
    }

    // ---------- window ----------

    private void applyWindowShape() {
        if (!shapedWindow || !isDisplayable()) {
            return;
        }

        try {
            setShape(createWindowShape(getWidth(), getHeight()));
        } catch (Exception ignored) {
        }
    }

    static Shape createWindowShape(int w, int h) {
        Path2D.Double p = new Path2D.Double();

        p.moveTo(w * 0.50, h * 0.015);
        p.curveTo(w * 0.75, h * 0.02, w * 0.92, h * 0.22, w * 0.93, h * 0.46);
        p.curveTo(w * 0.94, h * 0.74, w * 0.76, h * 0.985, w * 0.50, h * 0.985);
        p.curveTo(w * 0.24, h * 0.985, w * 0.06, h * 0.74, w * 0.07, h * 0.46);
        p.curveTo(w * 0.08, h * 0.22, w * 0.25, h * 0.02, w * 0.50, h * 0.015);
        p.closePath();

        return p;
    }

    private void installDrag(Component c) {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = SwingUtilities.convertPoint(
                        (Component) e.getSource(),
                        e.getPoint(),
                        TamagotchiFrame.this
                );
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null) {
                    return;
                }

                Point screenPoint = e.getLocationOnScreen();
                setLocation(
                        screenPoint.x - dragOffset.x,
                        screenPoint.y - dragOffset.y
                );
            }
        };

        c.addMouseListener(ma);
        c.addMouseMotionListener(ma);
    }
}