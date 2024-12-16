import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.*;
import java.util.Objects;

public class GameWithGUI extends Application {
    private static final int MAP_SIZE = 5;
    private static final int MAX_ACTIONS_PER_TURN = 3;
    private boolean[][] playerTerritory = new boolean[MAP_SIZE][MAP_SIZE];
    private boolean[][] aiTerritory = new boolean[MAP_SIZE][MAP_SIZE];
    private int[][] map = new int[MAP_SIZE][MAP_SIZE];
    private int playerFarmers = 3, aiFarmers = 3;
    private int playerRice = 5, aiRice = 5;
    private int playerWater = 5, aiWater = 5;
    private int playerActions = 0;
    private int aiActions = 0;
    private int numai = 1, numpl = 1;

    private TextArea gameLog;
    private GridPane mapGrid;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Пошаговая игра");

        gameLog = new TextArea();
        gameLog.setEditable(false);

        mapGrid = new GridPane();
        initializeMap();
        mapGrid = new GridPane();
        mapGrid.setHgap(5);
        mapGrid.setVgap(5);
        updateMapDisplay();

        // Создаём кнопки действий
        Button gatherWaterButton = new Button("Набрать воду");
        gatherWaterButton.setOnAction(e -> gatherWater());

        Button waterRiceButton = new Button("Полить рис");
        waterRiceButton.setOnAction(e -> waterRice());

        Button buildHouseButton = new Button("Построить дом");
        buildHouseButton.setOnAction(e -> buildHouse());

        Button captureTerritoryButton = new Button("Захватить территорию");
        captureTerritoryButton.setOnAction(e -> {
            try {
                captureTerritory();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button endTurnButton = new Button("Завершить ход");
        endTurnButton.setOnAction(e -> endTurn());

        Button endgamebutton = new Button("Завершить игру");
        endgamebutton.setOnAction(e -> endgame());

        Button remgamebutton = new Button("Вспомнить игру");
        remgamebutton.setOnAction(e -> remgame());

        // Организуем кнопки в вертикальную колонку
        VBox actionButtons = new VBox(10, gatherWaterButton, waterRiceButton, buildHouseButton, captureTerritoryButton, endTurnButton, endgamebutton, remgamebutton);

        BorderPane layout = new BorderPane();
        layout.setLeft(actionButtons); // Кнопки слева
        layout.setCenter(mapGrid); // Карта в центре
        layout.setBottom(gameLog); // Лог внизу

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeMap() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                Rectangle cell = new Rectangle(50, 50);
                cell.setStroke(Color.BLACK);
                mapGrid.add(cell, j, i);
                if (playerTerritory[i][j]) {
                    cell.setFill(Color.GREEN); // Захвачено игроком
                } else if (aiTerritory[i][j]) {
                    cell.setFill(Color.RED); // Захвачено AI
                } else {
                    cell.setFill(Color.LIGHTGRAY); // Доступно для захвата игроку
                }

                map[i][j] = (int) (Math.random() * 3 + 1); // Требования для захвата клетки
                if (i == MAP_SIZE - 1 && j == MAP_SIZE - 1) {
                    playerTerritory[i][j] = true;
                }
                if (i == 0 && j == 0) {
                    aiTerritory[i][j] = true;
                }
            }
        }
        updateMapDisplay();
    }

    private void updateMapDisplay() {
        mapGrid.getChildren().clear();
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                Rectangle cell = new Rectangle(50, 50);
                cell.setStroke(Color.BLACK);

                if (playerTerritory[i][j]) {
                    cell.setFill(Color.BLUE); // Клетки игрока
                } else if (aiTerritory[i][j]) {
                    cell.setFill(Color.RED); // Клетки AI
                } else {
                    cell.setFill(Color.LIGHTGRAY); // Незахваченные клетки
                }
                mapGrid.add(cell, j, i);

                // Добавление текста в ячейку
                if (!playerTerritory[i][j] && !aiTerritory[i][j]) {
                    TextArea textArea = new TextArea(String.valueOf(map[i][j]));
                    textArea.setEditable(false);
                    textArea.setMaxWidth(50);
                    textArea.setMaxHeight(50);
                    mapGrid.add(textArea, j, i);
                }
            }
        }
    }

    private void gatherWater() {
        if (playerActions < MAX_ACTIONS_PER_TURN) {
            playerWater += 5;
            playerActions++;
            gameLog.appendText("Вы набрали воды. Вода: " + playerWater + "\n");
            checkEndTurn();
        }
    }

    private void waterRice() {
        if (playerActions < MAX_ACTIONS_PER_TURN) {
            if (playerWater >= 2) {
                playerWater -= 2;
                playerRice += 3;
                playerActions++;
                gameLog.appendText("Вы полили рис. Рис: " + playerRice + ", Вода: " + playerWater + "\n");
            } else {
                gameLog.appendText("Недостаточно воды для поливки риса.\n");
            }
            checkEndTurn();
        }
    }

    private void buildHouse() {
        if (playerActions < MAX_ACTIONS_PER_TURN) {
            if (playerRice >= 5 && playerWater >= 5) {
                playerRice -= 3;
                playerWater -= 5;
                playerFarmers++;
                playerActions++;
                gameLog.appendText("Вы построили дом. Крестьяне: " + playerFarmers + "\n");
            } else {
                gameLog.appendText("Недостаточно ресурсов для постройки дома.\n");
            }
            checkEndTurn();
        }
    }

    private void captureTerritory() throws InterruptedException {
        if (playerActions < MAX_ACTIONS_PER_TURN) {
            for (int i = 0; i < MAP_SIZE; i++) {
                for (int j = 0; j < MAP_SIZE; j++) {
                    if (isAdjacentToPlayer(i, j) && !playerTerritory[i][j] && !aiTerritory[i][j]) {
                        if (playerFarmers >= map[i][j]) {
                            playerFarmers -= map[i][j];
                            playerTerritory[i][j] = true;
                            playerActions++;
                            gameLog.appendText("Вы захватили клетку [" + i + "," + j + "]\n");
                            updateMapDisplay();
                            checkEndTurn();
                            numpl++;
                            if (numpl > ((MAP_SIZE * MAP_SIZE) / 2)) {
                                gameLog.appendText("Игрок победил\n");
                                updateMapDisplay();
                                try {
                                    Thread.sleep(1000); // Задержка в 3000 миллисекунд (3 секунды)
                                } catch (InterruptedException e) {
                                    e.printStackTrace(); // Если поток прервали, выводим ошибку
                                }

                                System.exit(0); // Завершение программы
                            }
                            return;

                        }
                    }
                }
            }
            gameLog.appendText("Нет доступных клеток для захвата.\n");
        }
    }

    private void checkEndTurn() {
        if (playerActions >= MAX_ACTIONS_PER_TURN) {
            endTurn();
        }
    }

    private void endTurn() {
        playerActions = 0;
        gameLog.appendText("Ваш ход завершён.\n");
        performAIActions();
    }
    private void remgame() {
        try (BufferedReader br = new BufferedReader(new FileReader("Output.txt"))) {
            String line;
            for (int d = 0; d < 8; d++) {
                line = br.readLine();
                System.out.println(line);
                if (line.startsWith("pa")) {
                    int a = Integer.parseInt(line.substring(2));
                    playerActions = (a);
                }
                if (line.startsWith("pr")) {
                    int a = Integer.parseInt(line.substring(2));
                    playerRice = (a);
                }
                if (line.startsWith("pf")) {
                    int a = Integer.parseInt(line.substring(2));
                    playerFarmers = (a);
                }
                if (line.startsWith("pw")) {
                    int a = Integer.parseInt(line.substring(2));
                    playerWater = (a);
                }
                if (line.startsWith("aa")) {
                    int a = Integer.parseInt(line.substring(2));
                    aiActions = (a);
                }
                if (line.startsWith("ar")) {
                    int a = Integer.parseInt(line.substring(2));
                    aiRice = (a);
                }
                if (line.startsWith("af")) {
                    int a = Integer.parseInt(line.substring(2));
                    aiFarmers = (a);
                }
                if (line.startsWith("aw")) {
                    int a = Integer.parseInt(line.substring(2));
                    aiWater = (a);
                }
            }
            String[][] matrix = new String[MAP_SIZE][MAP_SIZE];
            String[] mass;

            for (int i = 0; i < 5; i++) {
                mass = br.readLine().trim().split(" ");
                for (int m = 0; m < 5; m++){
                    System.out.println(mass[m]);
                    if (Objects.equals(mass[m], "P")){
                        playerTerritory[i][m] = true;
                        updateMapDisplay();
                    }
                    else if (Objects.equals(mass[m], "A")) {
                        aiTerritory[i][m] = true;
                        updateMapDisplay();
                    }
                    else   {
                        map[i][m] = Integer.parseInt(mass[m]);}



                }
            }

        }
        catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        updateMapDisplay();
        return;
        }
    }

    private void endgame() {
        String[][] mapsq = new String[5][5];
            for (int i = 0; i < mapsq.length; i++) {
                for (int j = 0; j < mapsq[0].length; j++) {
                    if (playerTerritory[i][j]) mapsq[i][j] = "P";
                    else if (aiTerritory[i][j]) mapsq[i][j] = "A";
                    else mapsq[i][j] = String.valueOf(map[i][j]);
                    System.out.println(mapsq[i][j]);
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
                writer.write("pa" + playerActions);
                writer.newLine();
                writer.write("pw" + playerWater);
                writer.newLine();
                writer.write("pr" + playerRice);
                writer.newLine();
                writer.write("pf" + playerFarmers);
                writer.newLine();
                writer.write("aa" + aiActions);
                writer.newLine();
                writer.write("aw" + aiWater);
                writer.newLine();
                writer.write("ar" + aiRice);
                writer.newLine();
                writer.write("af" + aiFarmers);
                writer.newLine();

                for (String[] row : mapsq) {
                    for (String value : row) {
                        writer.write(value + " ");
                    }
                    writer.newLine();
                }

                System.out.println("Данные успешно записаны в файл.");
            } catch (IOException e) {
                System.err.println("Произошла ошибка при записи файла: " + e.getMessage());

                return;
            }
            System.exit(0);
        }

    private void performAIActions() {
        aiActions = 0;

        while (aiActions < MAX_ACTIONS_PER_TURN) {
            if (tryCapture()==false) {
                if (tryBuildHouse()==false) {
                    if (tryWaterRice()==false) {
                        tryGatherWater();
                    }
                }
            }
        }
        gameLog.appendText("Ход AI завершён.\n");
    }

    private boolean tryCapture() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (isAdjacentToAI(i, j) && !aiTerritory[i][j] && !playerTerritory[i][j]) {
                    if (aiFarmers >= map[i][j]) {
                        aiFarmers -= map[i][j];
                        aiTerritory[i][j] = true;
                        aiActions++;
                        gameLog.appendText("AI захватил [" + i + "," + j + "]\n");
                        updateMapDisplay();
                        numai++;
                        if (numai > ((MAP_SIZE*MAP_SIZE)/2)) {
                            gameLog.appendText("AI победил\n");
                            updateMapDisplay();
                            try {
                                Thread.sleep(3000); // Задержка в 3000 миллисекунд (3 секунды)
                            } catch (InterruptedException e) {
                                e.printStackTrace(); // Если поток прервали, выводим ошибку
                            }

                            System.exit(0); // Завершение программы
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAdjacentToPlayer(int x, int y) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < MAP_SIZE && ny >= 0 && ny < MAP_SIZE && playerTerritory[nx][ny] && !aiTerritory[nx][ny])  {
                return true;
            }
        }
        return false;
    }

    private boolean isAdjacentToAI(int x, int y) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < MAP_SIZE && ny >= 0 && ny < MAP_SIZE && aiTerritory[nx][ny] && !playerTerritory[nx][ny]) {
                return true;
            }
        }
        return false;
    }

    private boolean tryBuildHouse() {
        if (aiWater >= 3 && aiRice >= 5) {
            aiWater -= 5;
            aiRice -= 3;
            aiFarmers++;
            aiActions++;
            gameLog.appendText("AI построил дом крестьянина\n");
            return true;
        }
        return false;
    }

    private boolean tryWaterRice() {
        if (aiWater >= 2) {
            aiWater -= 2;
            aiRice += 3;
            aiActions++;
            gameLog.appendText("AI поливает рис\n");
            return true;
        }
        return false;
    }

    private boolean tryGatherWater() {
        aiWater += 5;
        aiActions++;
        gameLog.appendText("AI набрал воды\n");
        return true;
    }

}

