import javax.swing.SwingUtilities;

public class TamagotchiSimulator {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TamagotchiFrame frame = new TamagotchiFrame();
            frame.setVisible(true);
        });
    }
}