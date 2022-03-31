import javafx.scene.paint.Color;

public enum Fields {
    SEA(Color.DEEPSKYBLUE),
    SHIP(Color.DARKGRAY),
    HIT(Color.FIREBRICK),
    MISS(Color.DARKBLUE);

    private Color color;

    Fields(Color color){
        this.color = color;
    }


    public Color getColor() {
        return color;
    }
}
