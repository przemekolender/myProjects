public class Ship {
    private int length;
    private final boolean vertical;

    Ship(int len, boolean ver){
        this.length = len;
        this.vertical = ver;
    }


    public int getLength() {
        return length;
    }


    public boolean getVertical() {
        return vertical;
    }
}
