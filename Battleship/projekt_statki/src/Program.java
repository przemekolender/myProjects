import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Application;
import java.util.ArrayList;

public class Program extends Application{
    private BoardFX board1;
    private BoardFX board2;
    private Player rightPlayer, leftPlayer;
    Stage window;
    Scene mainScreen,
            placeShipsSingleplayer, newPlaceShipsSingleplayer, fightSingleplayer, //scenes used in singleplayer game
            placeShipsMultiplayer1, placeShipsMultiplayer2, fightMultiplayer, //scenes used in mutiplayer game
            newPlaceShipsMultiplayer1, newPlaceShipsMultiplayer2, //scenes used in mutiplayer game
            winnerScene;

    //every scene is created by funcion name set + scene name (for example setMainScreen)

    int index = 0;
    boolean running = true; //indicates if it is time for placing ships or fight
    boolean enemyTurn = false; //indictaes when is enemy (player with board on the left) turn
    ArrayList<Tuple3> board1Setup, board2Setup; //remember ships setup on board




    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)  {
        window = primaryStage;
        primaryStage.setTitle("Battleship");
        primaryStage.setResizable(false);
        primaryStage.show();
        setMainScreen();
        window.setScene(mainScreen);
    }

    //Select a game mode================================================================================================
    private void setMainScreen(){
        Button singlePlayer = new Button("SinglePlayer");
        singlePlayer.setPrefSize(300, 200);
        singlePlayer.setOnAction(e -> {
            rightPlayer = new Player();
            setPlaceShipsSingleplayer();
            window.setScene(placeShipsSingleplayer);
        });

        Button multiPlayer = new Button("Multiplayer");
        multiPlayer.setPrefSize(300, 200);
        multiPlayer.setOnAction(e -> {
            setPlaceShipsMultiplayer1();
            window.setScene(placeShipsMultiplayer1);
        });

        Text title = getText("Battleship", 5);
        Text info = getText("Select a game mode", 2);

        Button exit = new Button("Exit game");
        exit.setPrefSize(100, 40);
        exit.setOnAction(e -> System.exit(0));

        HBox modeBox = new HBox(30, singlePlayer, multiPlayer);
        modeBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(50, title, info, modeBox, exit);
        content.setAlignment(Pos.CENTER);
        mainScreen = new Scene(content, 800, 600);
    }


    //place ships on board singleplayer=================================================================================
    private void setPlaceShipsSingleplayer(){
        int[] shipToPlace = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};

        Button go = new Button("Start");
        go.setPrefSize(120, 50);

        board1 = new BoardFX(false, e -> {
            if (!running)
                return;

            BoardFX.Cell cell = (BoardFX.Cell) e.getSource();
            if (board1.placeShip(new Ship(shipToPlace[index], e.getButton() == MouseButton.PRIMARY), cell.X(), cell.Y(), false)) {
                if (index++ == 9) {
                    running = !running;
                    go.setOnAction(event -> {
                        setFightSingleplayer();
                        window.setScene(fightSingleplayer);
                    });
                }
            }
        });

        BorderPane root = new BorderPane();
        Text text = getText("Place your ships", 2);

        Button main = getMainMenuButton();
        root.setTop(main);

        Button random = new Button("Random location");
        random.setPrefSize(120, 50);
        random.setOnAction(e -> {
            board1Setup = board1.randomPlaceShips();
            setNewPlaceShipsSingleplayer();
            window.setScene(newPlaceShipsSingleplayer);
            running = !running;
        });

        HBox buttons = new HBox(20, random, go);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(30, text, board1, buttons);
        box.setAlignment(Pos.CENTER);
        root.setCenter(box);

        placeShipsSingleplayer = new Scene(root, 800, 600);
    }

    //enalbles to draw positions for ships multiple times===============================================================
    private void setNewPlaceShipsSingleplayer(){
        board1 = new BoardFX(board1Setup);
        Button go = new Button("Start");
        go.setPrefSize(120, 50);
        go.setOnAction(event -> {
            setFightSingleplayer();
            window.setScene(fightSingleplayer);
        });

        BorderPane root = new BorderPane();
        Text text = getText("Place your ships", 2);

        Button main = getMainMenuButton();
        root.setTop(main);

        Button random = new Button("Random location");
        random.setPrefSize(120, 50);
        random.setOnAction(e -> {
            board1Setup = board1.randomPlaceShips();
            setNewPlaceShipsSingleplayer();
            window.setScene(newPlaceShipsSingleplayer);
        });

        HBox buttons = new HBox(20, random, go);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(30, text, board1, buttons);
        box.setAlignment(Pos.CENTER);
        root.setCenter(box);

        newPlaceShipsSingleplayer = new Scene(root, 800, 600);
    }


    //play with bot=====================================================================================================
    private void setFightSingleplayer(){
        leftPlayer = new Player();

        board2 = new BoardFX(true, event -> {
            if(running){
                return;
            }
            BoardFX.Cell cell = (BoardFX.Cell) event.getSource();

            if (!cell.isAlreadyShoot()){
                cell.setAlreadyShoot();
                if (cell.shoot()) {
                    rightPlayer.hit();
                    enemyTurn = false;
                }
                else{
                    enemyTurn = true;
                }
            }
            if (rightPlayer.getHealth() == 0) {
                setWinnerScene("You");
                window.setScene(winnerScene);
            }


            while (enemyTurn) {
                BoardFX.Cell cell2;
                do{
                    int[] a = rightPlayer.shoot();
                    cell2 = board1.getCell(a[0], a[1]);
                }while(cell2.isAlreadyShoot());

                if(cell2.shoot()){
                    cell2.setAlreadyShoot();
                    leftPlayer.hit();
                    enemyTurn = true;
                }
                else{
                    enemyTurn = false;
                }
                if (leftPlayer.getHealth() == 0) {
                    setWinnerScene("Bot");
                    window.setScene(winnerScene);
                }
            }
        });

        Text player = getText("Your board", 2);
        Text bot = getText("Bot's board", 2);

        VBox left = new VBox(30, player, board1);
        left.setAlignment(Pos.CENTER);
        VBox right = new VBox(30, bot, board2);
        right.setAlignment(Pos.CENTER);

        HBox box = new HBox(100, left, right);
        box.setAlignment(Pos.CENTER);

        Button main = getMainMenuButton();

        BorderPane root = new BorderPane();
        root.setTop(main);
        root.setCenter(box);

        fightSingleplayer = new Scene(root, 800, 600);
    }


    //place ships on board multiplayer, 1st perosn======================================================================
    private void setPlaceShipsMultiplayer1(){
        int[] shipToPlace = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        board1Setup = new ArrayList<>();

        Button go = new Button("Next Player");
        go.setPrefSize(120, 50);

        board1 = new BoardFX(false, e -> {
            if (!running)
                return;

            BoardFX.Cell cell = (BoardFX.Cell) e.getSource();
            if (board1.placeShip(new Ship(shipToPlace[index], e.getButton() == MouseButton.PRIMARY), cell.X(), cell.Y(), false)) {
                board1Setup.add(new Tuple3(cell.X(), cell.Y(), e.getButton() == MouseButton.PRIMARY));
                if (index++ == 9) {
                    running = !running;
                    go.setOnAction(event -> {
                        setPlaceShipsMultiplayer2();
                        window.setScene(placeShipsMultiplayer2);
                    });
                }
            }
        });

        BorderPane root = new BorderPane();
        Text text = getText("Place your ships, Player 1", 2);

        Button main = getMainMenuButton();
        root.setTop(main);

        Button random = new Button("Random location");
        random.setPrefSize(120, 50);
        random.setOnAction(e -> {
            board1Setup = board1.randomPlaceShips();
            setNewPlaceShipsMultiplayer1();
            window.setScene(newPlaceShipsMultiplayer1);
            running = !running;
        });

        HBox buttons = new HBox(20, random, go);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(30, text, board1, buttons);
        box.setAlignment(Pos.CENTER);
        root.setCenter(box);

        placeShipsMultiplayer1 = new Scene(root, 800, 600);
    }
    //enables 1st person to draw random position for ships multiple times===============================================
    private void setNewPlaceShipsMultiplayer1(){
        board1 = new BoardFX(board1Setup);
        Button go = new Button("Next Player");
        go.setPrefSize(120, 50);
        go.setOnAction(event -> {
            setPlaceShipsMultiplayer2();
            window.setScene(placeShipsMultiplayer2);
        });

        BorderPane root = new BorderPane();
        Text text = getText("Place your ships, Player 1", 2);

        Button main = getMainMenuButton();
        root.setTop(main);

        Button random = new Button("Random location");
        random.setPrefSize(120, 50);
        random.setOnAction(e -> {
            board1Setup = board1.randomPlaceShips();
            setNewPlaceShipsMultiplayer1();
            window.setScene(newPlaceShipsMultiplayer1);
        });

        HBox buttons = new HBox(20, random, go);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(30, text, board1, buttons);
        box.setAlignment(Pos.CENTER);
        root.setCenter(box);

        newPlaceShipsMultiplayer1 = new Scene(root, 800, 600);
    }

    //place ships on board multiplayer, 2nd perosn========================================================================
    private void setPlaceShipsMultiplayer2(){
        running = true;
        index = 0;
        int[] shipToPlace = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        board2Setup = new ArrayList<>();

        Button go = new Button("Start");
        go.setPrefSize(120, 50);

        board2 = new BoardFX(false, e -> {
            if (!running)
                return;

            BoardFX.Cell cell = (BoardFX.Cell) e.getSource();
            if (board2.placeShip(new Ship(shipToPlace[index], e.getButton() == MouseButton.PRIMARY), cell.X(), cell.Y(), true)) {
                board2Setup.add(new Tuple3(cell.X(), cell.Y(), e.getButton() == MouseButton.PRIMARY));
                if (index++ == 9) {
                    running = !running;
                    go.setOnAction(event -> {
                        setFightMultiplayer();
                        window.setScene(fightMultiplayer);
                    });
                }
            }
        });

        BorderPane root = new BorderPane();
        Text text = getText("Place your ships, Player 2", 2);

        Button main = getMainMenuButton();
        root.setTop(main);

        Button random = new Button("Random location");
        random.setPrefSize(120, 50);
        random.setOnAction(e -> {
            board2Setup = board2.randomPlaceShips();
            setNewPlaceShipsMultiplayer2();
            window.setScene(newPlaceShipsMultiplayer2);
            running = !running;
        });

        HBox buttons = new HBox(20, random, go);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(30, text, board2, buttons);
        box.setAlignment(Pos.CENTER);
        root.setCenter(box);

        placeShipsMultiplayer2 = new Scene(root, 800, 600);
    }
    //enables 2nd person to draw random positions for ships many times==================================================
    private void setNewPlaceShipsMultiplayer2(){
        board2 = new BoardFX(board2Setup);
        Button go = new Button("Start");
        go.setPrefSize(120, 50);
        go.setOnAction(event -> {
            setFightMultiplayer();
            window.setScene(fightMultiplayer);
        });

        BorderPane root = new BorderPane();
        Text text = getText("Place your ships, Player 2", 2);

        Button main = getMainMenuButton();
        root.setTop(main);

        Button random = new Button("Random location");
        random.setPrefSize(120, 50);
        random.setOnAction(e -> {
            board2Setup = board2.randomPlaceShips();
            setNewPlaceShipsMultiplayer2();
            window.setScene(newPlaceShipsMultiplayer2);
        });

        HBox buttons = new HBox(20, random, go);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(30, text, board2, buttons);
        box.setAlignment(Pos.CENTER);
        root.setCenter(box);

        newPlaceShipsMultiplayer2 = new Scene(root, 800, 600);
    }

    //fight multiplayer=================================================================================================
    private void setFightMultiplayer(){
        BorderPane root = new BorderPane();
        rightPlayer = new Player();
        leftPlayer = new Player();

        board1 = new BoardFX(board1Setup, board2Setup ,event -> {
            if(running){
                return;
            }
            BoardFX.Cell cell = (BoardFX.Cell) event.getSource();
            if(!enemyTurn){
                if(cell.isRightBoard()){
                    if (!cell.isAlreadyShoot()){
                        cell.setAlreadyShoot();
                        if (cell.shoot()) {
                            rightPlayer.hit();
                            enemyTurn = false;
                        }
                        else{
                            enemyTurn = true;
                        }
                    }
                }
            }
            if(enemyTurn){
                if(!cell.isRightBoard()){
                    if (!cell.isAlreadyShoot()){
                        cell.setAlreadyShoot();
                        if (cell.shoot()) {
                            leftPlayer.hit();
                            enemyTurn = true;
                        }
                        else{
                            enemyTurn = false;
                        }
                    }
                }
            }
            if (rightPlayer.getHealth() == 0) {
                setWinnerScene("Player 1");
                window.setScene(winnerScene);
            }
            else if (leftPlayer.getHealth() == 0){
                setWinnerScene("Player 2");
                window.setScene(winnerScene);
            }
        });
        HBox box = new HBox(board1);
        box.setAlignment(Pos.CENTER);

        Button main = getMainMenuButton();

        root.setCenter(box);
        root.setTop(main);

        fightMultiplayer = new Scene(root, 800, 600);

    }
    //help function - simple text creator
    Text getText(String content, int scale){
        Text text = new Text(content);
        text.setScaleX(scale);
        text.setScaleY(scale);
        return text;
    }

    // sets scene which appears after someone wins
    private void setWinnerScene(String winner){
        Text text;
        Text subtext;
        if(winner.equals("You")){
             text = getText("You win!", 6);
             subtext = getText("Congratulations!", 3);
        }
        else if(winner.equals("Bot")){
            text = getText("Bot wins!", 8);
            subtext = getText("Try again", 3);
        }
        else{
            text = getText(winner + " wins!", 8);
            subtext = getText("Congratulations!", 3);
        }
        Button main = getMainMenuButton();

        Button exit = new Button("Exit game");
        exit.setPrefSize(100, 40);
        exit.setOnAction(e -> System.exit(0));

        HBox buttons = new HBox(30, main, exit);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(80, text, subtext, buttons);
        box.setAlignment(Pos.CENTER);
        winnerScene = new Scene(box, 800, 600);
    }

    //help function, makes main menu button, which appeears on every scene
    private Button getMainMenuButton(){
        Button main = new Button("Main menu");
        main.setPrefSize(100, 40);
        main.setOnAction(event -> {
            window.setScene(mainScreen);
            running = true;
            index = 0;
        });

        return main;
    }

}