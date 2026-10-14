import javax.swing.*;
import java.awt.*;

public class Tamagotchi extends JFrame {
    int modus;  // modus=0(Eizustand); modus=1(Wachstumszustand);
                // modus=2(Spielzustand); modus=3(Schlafzustand);
                // modus=4(Krankheitsmodus); modus=5(Füttermodus);

    //Button initializing
    ImageIcon Image_Button_a = new ImageIcon("H:/Downloads/Button_a.png");
    ImageIcon Image_Button_b = new ImageIcon("H:/Downloads/Button_b.png");
    ImageIcon Image_Button_c = new ImageIcon("H:/Downloads/Button_c.png");
    ImageIcon Image_Button_reset = new ImageIcon("H:/Downloads/Button_reset.png");

    ImageIcon mainScreenImage = new ImageIcon("H:/Downloads/EGG.png");

    JButton Button_a = new JButton(Image_Button_a);
    JButton Button_b = new JButton(Image_Button_b);
    JButton Button_c = new JButton(Image_Button_c);
    JButton Button_reset = new JButton(Image_Button_reset);


    Tamagotchi() {

        this.setSize(700, 850);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);



        JLabel text = new JLabel(/*Image*/);

        JPanel hauptPanel = new JPanel();                                   //Panel for screen
        this.add(hauptPanel);
        hauptPanel.setSize(600,750);

        JPanel buttonPanel = new JPanel(new GridLayout(1,4, 10, 10));

        this.add(buttonPanel);
        buttonPanel.add(Button_a);
        //Panel
        hauptPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        //Button a
        Button_a.setSize(200, 100);
        Button_a.setVisible(true);
        buttonPanel.add(Button_a);
        Button_a.addActionListener(e -> a());
        Button_a.setBounds(50,750,50,50);

        Button_b.setSize(50,50);
        Button_b.setVisible(true);
        buttonPanel.add(Button_b);
        Button_b.addActionListener(e -> b());

        Button_c.setSize(50,50);
        Button_c.setVisible(true);
        buttonPanel.add(Button_c);
        Button_c.addActionListener(e -> c());

        Button_reset.setSize(50,50);
        Button_reset.setVisible(true);
        buttonPanel.add(Button_reset);
        Button_reset.addActionListener(e -> reset());
        hauptPanel.add(buttonPanel);
        this.add(hauptPanel);

        //J-label
        text.setText("TS IS THE LABEL RIGHT HERE");
        text.setSize(100,100);
        hauptPanel.add(text);


       // frame.add(new JLabel(new ImageIcon()));                // Source - https://stackoverflow.com/a/18027889
                                                                                                // Posted by Rollyng, modified by community. See post 'Timeline' for change history
                                                                                                // Retrieved 2026-10-09, License - CC BY-SA 3.0




        this.setVisible(true);


        modus = 0;
    }

    void ausgabe() {
        System.out.println(modus);


    }

    void a() {
        switch (modus) {
            case 1:
                modus = 2;
                break;
            case 2:
                modus = 3;
                break;
            case 3:
                modus = 4;
                break;
            case 4:
                modus = 5;
                break;
            case 5:
                modus = 1;
                break;
        }
        showPicture(modus);
        System.out.println("Butoon a switched MODUS to " + modus);
    }

    void b() {
        if (modus == 0) {
            modus = 1;
        }
        showPicture(modus);
        System.out.println("Button b switched MODUS to " + modus);
    }

    void c() {
        switch (modus) {
            case 1:
                modus = 5;
                break;
            case 2:
                modus = 1;
                break;
            case 3:
                modus = 2;
                break;
            case 4:
                modus = 3;
                break;
            case 5:
                modus = 4;
                break;
        }
        showPicture(modus);
        System.out.println("Butoon C switched MODUS to " + modus);

    }

    void reset() {
        modus = 0;
        System.out.println("Reset guh");
    }


    void showPicture(int modus) {
        switch (modus){
            case 0:
                //Hier muss die Animation für die Brütung stattfinden
                break;
            case 1:
                
        }
    }
}
