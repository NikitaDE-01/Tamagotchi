import javax.swing.*;

public class Tamagotchi extends JFrame {
    int modus;  // modus=0(Eizustand); modus=1(Wachstumszustand);
    // modus=2(Spielzustand); modus=3(Schlafzustand);
    // modus=4(Krankheitsmodus); modus=5(Füttermodus);


    Tamagotchi() {

        JFrame frame = new JFrame("Tamagotchi");                //JFrame from Forum "StackOverflow"
        frame.setSize(500, 650);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);


        JPanel panel = new JPanel();                                //Panel for screen
        frame.add(panel);



        JButton Button_a = new JButton("a");            //Button a
        Button_a.setSize(100, 100);
        Button_a.setVisible(true);
        Button_a.addActionListener(e -> a());       //Chatty-Button-Test
        panel.add(Button_a);


        ImageIcon Image = new ImageIcon("H:/Downloads/EGG.png");
        JLabel text = new JLabel(Image);
        text.setSize(100,100);
        frame.add(text);


       // frame.add(new JLabel(new ImageIcon()));                // Source - https://stackoverflow.com/a/18027889
                                                                                                // Posted by Rollyng, modified by community. See post 'Timeline' for change history
                                                                                                // Retrieved 2026-10-09, License - CC BY-SA 3.0




        frame.setVisible(true);


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
        System.out.println("Butoon a switched MODUS to " + modus);
    }

    void b() {
        if (modus == 0) {
            modus = 1;
        }
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

    }

    void reset() {
        modus = 0;
    }


    void showPicture(int modus) {


    }
}
