import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class SnoopyGotchi {

    //Start
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnoopyGotchi::start);
    }

    //Fenster
    static void start() {
        JFrame f = new JFrame("SnoopyGotchi");
        f.setUndecorated(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Spiel laden
        Game game = new Game();
        Save.read(game);
        Shell shell = new Shell(game, f);
        f.setContentPane(shell);
        f.pack();
        f.setResizable(false);
        f.setLocationRelativeTo(null);

        //Eiform
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
            f.setBackground(new Color(0, 0, 0, 0));
        } else if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            f.setShape(shell.egg);
        }
        f.setVisible(true);

        //Speichern beim Beenden
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Save.write(game)));
        //Takt
        new javax.swing.Timer(100, e -> { game.tick(); shell.repaint(); }).start();
    }
}
