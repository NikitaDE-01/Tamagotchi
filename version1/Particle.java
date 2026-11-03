class Particle {
    double x;
    double y;
    double vx;
    double vy;
    int life;
    int kind; // 0 heart, 1 sparkle, 2 bubble, 3 confetti, 4 sweat, 5 note, 6 crumb

    Particle(double x, double y, double vx, double vy, int life, int kind) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.kind = kind;
    }
}