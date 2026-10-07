import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Einfaches Tamagotchi-Spiel mit Java Swing.
 *
 * Anforderungen:
 * - Startzustand als Ei
 * - 5 verschiedene Modi
 * - Buttons zum Wechseln der Modi
 * - Pixelartige Darstellung
 * - Tamagotchi-artiges Fenster
 * - Audioausgabe beim Moduswechsel
 * - Der Modus "Essen" ist vollständig programmiert
 */
public class test extends JFrame {

    // ------------------------------------------------------------
    // Modi
    // ------------------------------------------------------------

    enum Modus {
        EI,
        SCHLAFEN,
        ESSEN,
        SPIELEN,
        SAEUBERN,
        DISZIPLINIEREN
    }

    private Modus aktuellerModus = Modus.EI;

    // ------------------------------------------------------------
    // Spielwerte
    // ------------------------------------------------------------

    private int hunger = 50;
    private int glueck = 50;
    private int sauberkeit = 50;
    private int disziplin = 50;

    private JLabel statusLabel;
    private JPanel bildschirm;

    private final Random random = new Random();

    // ------------------------------------------------------------
    // Konstruktor
    // ------------------------------------------------------------

    public test() {

        setTitle("Mein Tamagotchi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Eierförmiges Tamagotchi-Fenster wird durch einen
        // abgerundeten Hauptbereich simuliert.
        setSize(500, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(new Color(45, 45, 45));

        erstelleOberflaeche();

        setVisible(true);
    }

    // ------------------------------------------------------------
    // Oberfläche
    // ------------------------------------------------------------

    private void erstelleOberflaeche() {

        JPanel hauptPanel = new JPanel(new BorderLayout(10, 10));
        hauptPanel.setBackground(new Color(255, 210, 80));
        hauptPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(80, 60, 20), 8),
                        BorderFactory.createEmptyBorder(
                                20, 20, 20, 20)
                )
        );

        // Titel
        JLabel titel = new JLabel("TAMAGOTCHI", SwingConstants.CENTER);
        titel.setFont(new Font("Monospaced", Font.BOLD, 28));
        titel.setForeground(new Color(50, 40, 20));

        hauptPanel.add(titel, BorderLayout.NORTH);

        // --------------------------------------------------------
        // Bildschirm
        // --------------------------------------------------------

        bildschirm = new JPanel();
        bildschirm.setPreferredSize(new Dimension(400, 330));
        bildschirm.setBackground(new Color(170, 220, 160));
        bildschirm.setBorder(
                BorderFactory.createLineBorder(
                        new Color(40, 80, 40), 8)
        );

        bildschirm.setLayout(new BorderLayout());

        hauptPanel.add(bildschirm, BorderLayout.CENTER);

        // --------------------------------------------------------
        // Status
        // --------------------------------------------------------

        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        hauptPanel.add(statusLabel, BorderLayout.SOUTH);

        // --------------------------------------------------------
        // Buttons
        // --------------------------------------------------------

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        buttonPanel.setBackground(new Color(255, 210, 80));

        JButton schlafenButton =
                erstelleButton("SCHLAFEN", Modus.SCHLAFEN);

        JButton essenButton =
                erstelleButton("ESSEN", Modus.ESSEN);

        JButton spielenButton =
                erstelleButton("SPIELEN", Modus.SPIELEN);

        JButton saeubernButton =
                erstelleButton("SAEUBERN", Modus.SAEUBERN);

        JButton disziplinButton =
                erstelleButton("DISZIPLIN", Modus.DISZIPLINIEREN);

        // Die Buttonanzahl ist variabel.
        buttonPanel.add(schlafenButton);
        buttonPanel.add(essenButton);
        buttonPanel.add(spielenButton);
        buttonPanel.add(saeubernButton);
        buttonPanel.add(disziplinButton);

        hauptPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Frame
        add(hauptPanel);

        zeigeModus();
    }

    // ------------------------------------------------------------
    // Button erstellen
    // ------------------------------------------------------------

    private JButton erstelleButton(String text, Modus modus) {

        JButton button = new JButton(text);

        button.setFont(new Font("Monospaced", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 235, 190));

        button.addActionListener(e -> wechselZuModus(modus));

        return button;
    }

    // ------------------------------------------------------------
    // Modus wechseln
    // ------------------------------------------------------------

    private void wechselZuModus(Modus neuerModus) {

        aktuellerModus = neuerModus;

        // Piepton beim Wechsel
        Toolkit.getDefaultToolkit().beep();

        zeigeModus();
    }

    // ------------------------------------------------------------
    // Aktuellen Modus anzeigen
    // ------------------------------------------------------------

    private void zeigeModus() {

        bildschirm.removeAll();

        switch (aktuellerModus) {

            case EI:
                zeigeEi();
                break;

            case SCHLAFEN:
                zeigeSchlafen();
                break;

            case ESSEN:
                zeigeEssen();
                break;

            case SPIELEN:
                zeigeSpielen();
                break;

            case SAEUBERN:
                zeigeSaeubern();
                break;

            case DISZIPLINIEREN:
                zeigeDisziplin();
                break;
        }

        statusLabel.setText(
                "Hunger: " + hunger +
                "   Glück: " + glueck +
                "   Sauber: " + sauberkeit +
                "   Disziplin: " + disziplin
        );

        bildschirm.revalidate();
        bildschirm.repaint();
    }

    // ------------------------------------------------------------
    // EI
    // ------------------------------------------------------------

    private void zeigeEi() {

        bildschirm.setLayout(new BorderLayout());

        JPanel pixelPanel = new PixelPanel("EI");

        bildschirm.add(pixelPanel, BorderLayout.CENTER);

        JLabel text = new JLabel(
                "Dein Tamagotchi wartet...",
                SwingConstants.CENTER);

        text.setFont(new Font("Monospaced", Font.BOLD, 15));

        bildschirm.add(text, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------
    // SCHLAFEN
    // ------------------------------------------------------------

    private void zeigeSchlafen() {

        bildschirm.setLayout(new BorderLayout());

        bildschirm.add(
                new PixelPanel("SCHLAFEN"),
                BorderLayout.CENTER
        );

        JLabel text = new JLabel(
                "Zzz... Zzz...",
                SwingConstants.CENTER);

        text.setFont(new Font("Monospaced", Font.BOLD, 18));

        bildschirm.add(text, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------
    // ESSEN
    // Vollständig programmierter Modus
    // ------------------------------------------------------------

    private void zeigeEssen() {

        bildschirm.setLayout(new BorderLayout());

        JPanel oben = new JPanel(new BorderLayout());
        oben.setOpaque(false);

        JLabel titel = new JLabel(
                "WAS SOLL ICH ESSEN?",
                SwingConstants.CENTER);

        titel.setFont(
                new Font("Monospaced", Font.BOLD, 18)
        );

        oben.add(titel, BorderLayout.NORTH);

        bildschirm.add(oben, BorderLayout.NORTH);

        // Essens-Buttons
        JPanel essenPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        essenPanel.setOpaque(false);

        JButton apfel = new JButton("APFEL");
        JButton keks = new JButton("KEKS");
        JButton kuchen = new JButton("KUCHEN");

        apfel.setFont(new Font("Monospaced", Font.BOLD, 12));
        keks.setFont(new Font("Monospaced", Font.BOLD, 12));
        kuchen.setFont(new Font("Monospaced", Font.BOLD, 12));

        apfel.addActionListener(e -> fuettere("Apfel", 15));
        keks.addActionListener(e -> fuettere("Keks", 10));
        kuchen.addActionListener(e -> fuettere("Kuchen", 25));

        essenPanel.add(apfel);
        essenPanel.add(keks);
        essenPanel.add(kuchen);

        bildschirm.add(essenPanel, BorderLayout.SOUTH);

        // Pixel-Tamagotchi
        bildschirm.add(
                new PixelPanel("ESSEN"),
                BorderLayout.CENTER
        );
    }

    // ------------------------------------------------------------
    // Füttern
    // ------------------------------------------------------------

    private void fuettere(String essen, int wert) {

        hunger -= wert;

        if (hunger < 0) {
            hunger = 0;
        }

        glueck += 5;

        if (glueck > 100) {
            glueck = 100;
        }

        Toolkit.getDefaultToolkit().beep();

        JOptionPane.showMessageDialog(
                this,
                "Lecker! Dein Tamagotchi hat " +
                essen + " gegessen.",
                "Fütterung",
                JOptionPane.INFORMATION_MESSAGE
        );

        zeigeModus();
    }

    // ------------------------------------------------------------
    // SPIELEN
    // ------------------------------------------------------------

    private void zeigeSpielen() {

        bildschirm.setLayout(new BorderLayout());

        bildschirm.add(
                new PixelPanel("SPIELEN"),
                BorderLayout.CENTER
        );

        JButton spielButton = new JButton(
                "SPIEL STARTEN"
        );

        spielButton.setFont(
                new Font("Monospaced", Font.BOLD, 14)
        );

        spielButton.addActionListener(e -> spiele());

        bildschirm.add(
                spielButton,
                BorderLayout.SOUTH
        );
    }

    // ------------------------------------------------------------
    // Einfaches Minispiel
    // ------------------------------------------------------------

    private void spiele() {

        int zahl = random.nextInt(3) + 1;

        String eingabe = JOptionPane.showInputDialog(
                this,
                "Rate eine Zahl von 1 bis 3!"
        );

        if (eingabe == null) {
            return;
        }

        try {

            int geraten = Integer.parseInt(eingabe);

            if (geraten == zahl) {

                glueck += 20;

                if (glueck > 100) {
                    glueck = 100;
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Richtig! Dein Tamagotchi freut sich!"
                );

            } else {

                glueck -= 5;

                if (glueck < 0) {
                    glueck = 0;
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Leider falsch! Die Zahl war " + zahl + "."
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Bitte eine Zahl eingeben!"
            );
        }

        zeigeModus();
    }

    // ------------------------------------------------------------
    // SAEUBERN
    // ------------------------------------------------------------

    private void zeigeSaeubern() {

        bildschirm.setLayout(new BorderLayout());

        bildschirm.add(
                new PixelPanel("SAEUBERN"),
                BorderLayout.CENTER
        );

        JButton sauberButton =
                new JButton("PUTZEN");

        sauberButton.setFont(
                new Font("Monospaced", Font.BOLD, 16)
        );

        sauberButton.addActionListener(e -> {

            sauberkeit += 25;

            if (sauberkeit > 100) {
                sauberkeit = 100;
            }

            Toolkit.getDefaultToolkit().beep();

            JOptionPane.showMessageDialog(
                    this,
                    "Das Tamagotchi ist jetzt sauber!"
            );

            zeigeModus();
        });

        bildschirm.add(
                sauberButton,
                BorderLayout.SOUTH
        );
    }

    // ------------------------------------------------------------
    // DISZIPLINIEREN
    // ------------------------------------------------------------

    private void zeigeDisziplin() {

        bildschirm.setLayout(new BorderLayout());

        bildschirm.add(
                new PixelPanel("DISZIPLIN"),
                BorderLayout.CENTER
        );

        JButton lobButton =
                new JButton("LOBEN");

        JButton schimpfButton =
                new JButton("SCHIMPFEN");

        lobButton.setFont(
                new Font("Monospaced", Font.BOLD, 13)
        );

        schimpfButton.setFont(
                new Font("Monospaced", Font.BOLD, 13)
        );

        JPanel panel = new JPanel(
                new GridLayout(1, 2)
        );

        panel.setOpaque(false);

        panel.add(lobButton);
        panel.add(schimpfButton);

        lobButton.addActionListener(e -> {

            disziplin += 5;

            if (disziplin > 100) {
                disziplin = 100;
            }

            glueck += 5;

            if (glueck > 100) {
                glueck = 100;
            }

            zeigeModus();
        });

        schimpfButton.addActionListener(e -> {

            disziplin += 15;

            if (disziplin > 100) {
                disziplin = 100;
            }

            glueck -= 10;

            if (glueck < 0) {
                glueck = 0;
            }

            zeigeModus();
        });

        bildschirm.add(
                panel,
                BorderLayout.SOUTH
        );
    }

    // ============================================================
    // PIXEL-DARSTELLUNG
    // ============================================================

    class PixelPanel extends JPanel {

        private String modus;

        public PixelPanel(String modus) {

            this.modus = modus;

            setBackground(
                    new Color(170, 220, 160)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            // Pixel-Darstellung
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF
            );

            int pixel = 10;

            int startX = getWidth() / 2 - 70;
            int startY = 70;

            Color dunkel =
                    new Color(40, 70, 40);

            Color schwarz =
                    new Color(20, 30, 20);

            Color weiss =
                    new Color(220, 245, 200);

            // ----------------------------------------------------
            // Ei
            // ----------------------------------------------------

            if (modus.equals("EI")) {

                g2.setColor(dunkel);

                int[][] ei = {

                        {0,1,1,1,1,0},
                        {1,1,1,1,1,1},
                        {1,1,1,1,1,1},
                        {1,1,1,1,1,1},
                        {0,1,1,1,1,0},
                        {0,0,1,1,0,0}
                };

                zeichnePixelBild(
                        g2,
                        ei,
                        startX,
                        startY,
                        pixel
                );

            } else {

                // ------------------------------------------------
                // Körper
                // ------------------------------------------------

                g2.setColor(dunkel);

                int[][] koerper = {

                        {0,0,1,1,1,1,0,0},
                        {0,1,1,1,1,1,1,0},
                        {1,1,1,1,1,1,1,1},
                        {1,1,1,1,1,1,1,1},
                        {1,1,1,1,1,1,1,1},
                        {0,1,1,1,1,1,1,0},
                        {0,0,1,1,1,1,0,0}
                };

                zeichnePixelBild(
                        g2,
                        koerper,
                        startX,
                        startY,
                        pixel
                );

                // Augen
                g2.setColor(black());

                g2.fillRect(
                        startX + 25,
                        startY + 25,
                        10,
                        10
                );

                g2.fillRect(
                        startX + 45,
                        startY + 25,
                        10,
                        10
                );

                // Mund
                g2.fillRect(
                        startX + 30,
                        startY + 50,
                        20,
                        5
                );

                // ------------------------------------------------
                // Modus-spezifische Pixel
                // ------------------------------------------------

                g2.setColor(weiss);

                if (modus.equals("ESSEN")) {

                    // Apfel
                    g2.fillRect(
                            startX + 85,
                            startY + 20,
                            15,
                            15
                    );

                    g2.fillRect(
                            startX + 90,
                            startY + 15,
                            5,
                            5
                    );
                }

                if (modus.equals("SCHLAFEN")) {

                    // Z
                    g2.fillRect(
                            startX + 100,
                            startY,
                            10,
                            5
                    );

                    g2.fillRect(
                            startX + 105,
                            startY + 5,
                            5,
                            5
                    );

                    g2.fillRect(
                            startX + 100,
                            startY + 10,
                            10,
                            5
                    );
                }

                if (modus.equals("SPIELEN")) {

                    // Ball
                    g2.fillRect(
                            startX + 95,
                            startY + 55,
                            15,
                            15
                    );
                }

                if (modus.equals("SAEUBERN")) {

                    // Bürste
                    g2.fillRect(
                            startX + 90,
                            startY + 35,
                            25,
                            8
                    );

                    g2.fillRect(
                            startX + 105,
                            startY + 43,
                            8,
                            25
                    );
                }

                if (modus.equals("DISZIPLIN")) {

                    // Ausrufezeichen
                    g2.fillRect(
                            startX + 100,
                            startY + 15,
                            8,
                            35
                    );

                    g2.fillRect(
                            startX + 100,
                            startY + 55,
                            8,
                            8
                    );
                }
            }

            g2.dispose();
        }

        private Color black() {
            return new Color(20, 30, 20);
        }

        private void zeichnePixelBild(
                Graphics2D g,
                int[][] bild,
                int x,
                int y,
                int pixel
        ) {

            for (int zeile = 0;
                 zeile < bild.length;
                 zeile++) {

                for (int spalte = 0;
                     spalte < bild[zeile].length;
                     spalte++) {

                    if (bild[zeile][spalte] == 1) {

                        g.fillRect(
                                x + spalte * pixel,
                                y + zeile * pixel,
                                pixel,
                                pixel
                        );
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------
    // Main
    // ------------------------------------------------------------

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new Tamagotchi();

        });
    }
}
