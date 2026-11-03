class Tamagotchi {
    boolean hatched = false;
    boolean sleeping = false;
    boolean sick = false;
    boolean dead = false;

    int fullness = 70;
    int happiness = 70;
    int cleanliness = 90;
    int energy = 70;
    int discipline = 50;

    int neglect = 0;
    int sickProgress = 0;

    private int limit(int value) {
        return Math.max(0, Math.min(100, value));
    }

    void tick() {
        if (!hatched || dead) {
            return;
        }

        if (sleeping) {
            energy = limit(energy + 5);
            fullness = limit(fullness - 1);
        } else {
            energy = limit(energy - 1);
            fullness = limit(fullness - 2);
            cleanliness = limit(cleanliness - 1);
            happiness = limit(happiness - 1);
        }

        if (sick) {
            happiness = limit(happiness - 1);
        }

        int zeros = 0;
        if (fullness == 0) zeros++;
        if (cleanliness == 0) zeros++;
        if (happiness == 0) zeros++;
        if (energy == 0) zeros++;

        if (!sick) {
            if (zeros > 0) {
                neglect++;
            } else {
                neglect = Math.max(0, neglect - 1);
            }
            if (neglect >= 6) {
                sick = true;
                sickProgress = 0;
            }
        } else {
            if (fullness > 40 && cleanliness > 40 && energy > 40 && happiness > 20) {
                sickProgress--;
            } else {
                sickProgress++;
            }
            if (sickProgress <= -8) {
                sick = false;
                neglect = 0;
            }
        }
    }

    void hatch() {
        hatched = true;
        sleeping = false;
    }

    void eatMeal() {
        fullness = limit(fullness + 30);
        happiness = limit(happiness + 5);
        cleanliness = limit(cleanliness - 5);
    }

    void eatSnack() {
        fullness = limit(fullness + 12);
        happiness = limit(happiness + 12);
        discipline = limit(discipline - 4);
    }

    void playBall() {
        happiness = limit(happiness + 18);
        energy = limit(energy - 10);
        fullness = limit(fullness - 5);
        cleanliness = limit(cleanliness - 5);
    }

    void addHappiness(int v) {
        happiness = limit(happiness + v);
    }

    void shower() {
        cleanliness = 100;
        happiness = limit(happiness + 4);
    }

    void scold() {
        discipline = limit(discipline + 18);
        happiness = limit(happiness - 6);
    }

    void reset() {
        hatched = false;
        sleeping = false;
        sick = false;
        dead = false;
        fullness = 70;
        happiness = 70;
        cleanliness = 90;
        energy = 70;
        discipline = 50;
        neglect = 0;
        sickProgress = 0;
    }
}