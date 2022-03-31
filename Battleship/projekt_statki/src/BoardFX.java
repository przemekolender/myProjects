import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.Random;


public class BoardFX extends Parent {
    private VBox oneBoard, rows, rows2; //containesrs used to keep board
    boolean hideShip; // indicates if ships should be hidden on the board
    boolean multiplayer; //game mode

    //first contructor, used to make battles and place ships manually
    BoardFX(boolean hideShip, EventHandler<? super MouseEvent> handler){
        this.hideShip = hideShip;
        this.oneBoard = new VBox();
        this.multiplayer = false;

        for(int y = 0; y < 10; y++){
            HBox row = new HBox();
            for(int x = 0; x < 10; x++){
                Cell c = new Cell(x, y);
                c.setOnMouseClicked(handler);
                row.getChildren().add(c);
            }
            oneBoard.getChildren().add(row);
        }

        getChildren().add(oneBoard);

        if(hideShip){
            randomPlaceShips();
        }
    }
    //second constructor, used to place ships on boards in multiplayer
    BoardFX(ArrayList<Tuple3> setup1, ArrayList<Tuple3> setup2, EventHandler<? super MouseEvent> handler){
        this.hideShip = true;
        this.multiplayer = true;
        //this.twoBoards = new HBox();
        rows = new VBox();
        rows2 = new VBox();

        for(int y = 0; y < 10; y++){
            HBox row = new HBox();
            HBox row2 = new HBox();
            for(int x = 0; x < 10; x++){
                Cell c = new Cell(x, y, true);
                Cell c2 = new Cell(x, y, false);
                c.setOnMouseClicked(handler);
                c2.setOnMouseClicked(handler);
                row.getChildren().add(c);
                row2.getChildren().add(c2);
            }
            rows.getChildren().add(row);
            rows2.getChildren().add(row2);
        }

        Program p = new Program();
        Text player1 = p.getText("Player 1", 2);
        Text player2 = p.getText("Player 2", 2);

        VBox left = new VBox(30, player2, rows);
        left.setAlignment(Pos.CENTER);
        VBox right = new VBox(30, player1, rows2);
        right.setAlignment(Pos.CENTER);


        getChildren().add(new HBox(100, right, left));


        int index = 0;
        int[] shipToPlace = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        while(index <= 9){
            Tuple3 tuple1 = setup1.get(index);
            Ship ship = new Ship(shipToPlace[index], tuple1.isVer());
            placeShip(ship, tuple1.getX(), tuple1.getY(), false);

            Tuple3 tuple2 = setup2.get(index);
            Ship ship2 = new Ship(shipToPlace[index], tuple2.isVer());
            placeShip(ship2, tuple2.getX(), tuple2.getY(), true);
            index++;
        }
    }
    //third constructor, used to create new random setups of ships
    BoardFX(ArrayList<Tuple3> setup1){
        this.hideShip = false;
        this.oneBoard = new VBox();
        this.multiplayer = false;

        for(int y = 0; y < 10; y++){
            HBox row = new HBox();
            for(int x = 0; x < 10; x++){
                Cell c = new Cell(x, y);
                row.getChildren().add(c);
            }
            oneBoard.getChildren().add(row);
        }

        getChildren().add(oneBoard);
        int index = 0;
        int[] shipToPlace = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        while(index <= 9){
            Tuple3 tuple1 = setup1.get(index);
            Ship ship = new Ship(shipToPlace[index], tuple1.isVer());
            placeShip(ship, tuple1.getX(), tuple1.getY(), false);
            index++;
        }
    }

    //put ships randomly on board
    public ArrayList<Tuple3> randomPlaceShips(){
        int index = 0;
        int[] shipToPlace = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        boolean[] verticalShip = {true, false};
        int x, y;
        boolean ver;
        ArrayList<Tuple3> setup = new ArrayList<>();

        while(index <= 9){
            Random random = new Random();
            x = random.nextInt(10);
            y = random.nextInt(10);
            ver = verticalShip[random.nextInt(2)];
            Ship ship = new Ship(shipToPlace[index], ver);
            boolean success = placeShip(ship, x, y, false);
            if(success) {
                index++;
                setup.add(new Tuple3(x, y, ver));
            }
        }
        return setup;
    }

    //puts single ship on board
    public boolean placeShip(Ship ship, int x, int y, boolean right) {
        int length = ship.getLength();
        if(multiplayer){
            if (ship.getVertical()) {
                for (int i = y; i < y + length; i++) {
                    Cell cell = getCell(x, i, right);

                    cell.setType(Fields.SHIP);
                    if (!hideShip) {
                        cell.setFill(cell.getType().getColor());
                    }
                }
            }
            else {
                for (int i = x; i < x + length; i++) {
                    Cell cell = getCell(i, y, right);
                    cell.setType(Fields.SHIP);
                    if (!hideShip) {
                        cell.setFill(cell.getType().getColor());
                    }
                }
            }
            return true;
        }
        else if (canPlaceShip(ship, x, y)) {

            if (ship.getVertical()) {
                for (int i = y; i < y + length; i++) {
                    Cell cell = getCell(x, i);

                    cell.setType(Fields.SHIP);
                    if (!hideShip) {
                        cell.setFill(cell.getType().getColor());
                    }
                }
            }
            else {
                for (int i = x; i < x + length; i++) {
                    Cell cell = getCell(i, y);
                    cell.setType(Fields.SHIP);
                    if (!hideShip) {
                        cell.setFill(cell.getType().getColor());
                    }
                }
            }
            return true;
        }
        return false;
    }

    //indicttes if ship can be placed in given place
    private boolean canPlaceShip(Ship ship, int x, int y) {
        int length = ship.getLength();

        if (ship.getVertical()) {
            for (int i = y; i < y + length; i++) {
                if (!inBoard(x, i))
                    return false;

                Cell cell = getCell(x, i);

                if (cell.getType() != Fields.SEA)
                    return false;

                for (Cell neighbor : getNeighbors(cell)) {
                    if (!inBoard(x, i))
                        return false;

                    if (neighbor.getType() != Fields.SEA)
                        return false;
                }
            }
        }
        else {
            for (int i = x; i < x + length; i++) {
                if (!inBoard(i, y))
                    return false;

                Cell cell = getCell(i, y);
                if (cell.getType() != Fields.SEA)
                    return false;

                for (Cell neighbor : getNeighbors(cell)) {
                    if (!inBoard(i, y))
                        return false;

                    if (neighbor.getType() != Fields.SEA)
                        return false;
                }
            }
        }
        return true;
    }

    //find cell's neighbours
    private Cell[] getNeighbors(Cell cell) {
        ArrayList<Cell> neighbors = new ArrayList<>();

        if (inBoard(cell.X() - 1, cell.Y())) neighbors.add(getCell(cell.X() - 1, cell.Y()));
        if (inBoard(cell.X() + 1, cell.Y())) neighbors.add(getCell(cell.X() + 1, cell.Y()));
        if (inBoard(cell.X(), cell.Y() - 1)) neighbors.add(getCell(cell.X(), cell.Y() - 1));
        if (inBoard(cell.X(), cell.Y() + 1)) neighbors.add(getCell(cell.X(), cell.Y() + 1));

        return neighbors.toArray(new Cell[0]);
    }

    //checks if cell is in board
    private boolean inBoard(int x, int y){
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

    //returns certain cell
    public Cell getCell(int x, int y) {
        return (Cell)((HBox)oneBoard.getChildren().get(y)).getChildren().get(x);
    }

    //retuens central cell in multiplayer game mode
    public Cell getCell(int x, int y, boolean right) {
        if(right){
            return (Cell)((HBox)rows.getChildren().get(y)).getChildren().get(x);
        }
        else{
            return (Cell)((HBox)rows2.getChildren().get(y)).getChildren().get(x);
        }
    }

    /*public void sink(Cell cell){
        Cell[] neigbours = getNeighbors(cell);
        for(Cell c : neigbours){
            if(c.getType() == Fields.SEA){
                c.setType(Fields.MISS);
                c.setFill(c.getType().getColor());
            }
        }
    }*/


    static class Cell extends Rectangle {
        private int x;
        private int y;
        private Fields type;
        private boolean alreadyShoot;
        private boolean rightBoard; //needed in multiplayer, indicates on which side of scene is the board

        Cell(int x, int y){
            super(30, 30);
            this.x = x;
            this.y = y;
            this.type = Fields.SEA;
            this.alreadyShoot = false;

            setFill(type.getColor());
            setStroke(Color.LIGHTGRAY);
        }

        Cell(int x, int y, boolean rightBoard){
            super(30, 30);
            this.x = x;
            this.y = y;
            this.type = Fields.SEA;
            this.alreadyShoot = false;
            this.rightBoard = rightBoard;

            setFill(type.getColor());
            setStroke(Color.LIGHTGRAY);
        }

        public boolean isAlreadyShoot() {
            return alreadyShoot;
        }

        public void setAlreadyShoot() {
            this.alreadyShoot = true;
        }

        public boolean shoot(){
            if (type == Fields.SHIP){
                type = Fields.HIT;
                setFill(type.getColor());
                alreadyShoot = true;
                return true;
            }
            else{
                type = Fields.MISS;
                alreadyShoot = true;
                setFill(type.getColor());
                return false;
            }

        }

        public int X() {
            return x;
        }

        public int Y() {
            return y;
        }

        public boolean isRightBoard() {
            return rightBoard;
        }

        public Fields getType() {
            return type;
        }

        public void setType(Fields type) {
            this.type = type;
        }
    }
}
