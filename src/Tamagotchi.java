public class Tamagotchi {
    int modus; // modus=0(Eizustand); modus=1(Wachstumszustand);
               // modus=2(Spielzustand); modus=3(Schlafzustand);
               // modus=4(Krankheitsmodus); modus=5(Füttermodus);

    void Tamagotchi(){


    }

    void ausgabe(){


    }

    void a(){

    }

    void b() {
        if (modus == 0) {
            modus = 1;
        }
    }

    void c(){
    switch(modus){
        case 1:
            modus=5;
            break;
        case 2:
            modus=1;
            break;

    }

    }

    void reset(){modus=0;}

}
