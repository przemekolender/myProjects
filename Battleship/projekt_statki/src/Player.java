import java.util.Random;

public class Player {
    int health;

    Player(){
        this.health = 20;
    }

    public int[] shoot(){
        Random random = new Random();
        return new int[]{random.nextInt(10), random.nextInt(10)};
    }


    public void hit(){
        setHealth(getHealth() - 1);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
