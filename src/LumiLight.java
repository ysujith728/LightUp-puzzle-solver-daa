/*
 * ================================================================
 * LumiNet – Machine Light Up (Akari) Game
 * ================================================================
 *
 * DESCRIPTION 
 * ------------------------------------------------
 * LumiNet is an intelligent implementation of the
 * classic Light-Up (Akari) puzzle game where the
 * player competes against a CPU opponent.
 *
 * The objective is to place bulbs so that:
 *  - Every white cell is illuminated
 *  - Bulbs do not see each other
 *  - Numbered black cells are satisfied
 *
 * ALGORITHMIC FEATURES
 * ------------------------------------------------
 * 1. Backtracking-based CPU Solver
 *    - Recursive search explores bulb placements
 *    - Ensures a valid move exists before committing
 *
 * 2. Dynamic Programming (DP) State Pruning
 *    - Board states encoded and stored in a HashSet
 *    - Previously explored states are skipped to
 *      avoid redundant computation
 *
 * 3. Divide & Conquer Conflict Detection
 *    - Efficient visibility and constraint checks
 *      across grid partitions
 *
 * 4. Greedy Puzzle Generation
 *    - Generates playable boards using heuristic
 *      bulb placement strategies
 *
 * 5. Merge Sort Leaderboard Ranking
 *    - Player scores ranked using efficient
 *      divide-and-conquer sorting
 *
 * 6. Inversion-based Board Complexity Analysis
 *    - Estimates puzzle difficulty using
 *      inversion count metrics
 *
 * KEY DATA STRUCTURES
 * ------------------------------------------------
 * Grid (2D array) – board representation
 * Graph (implicit grid graph) – visibility relations
 * HashSet – DP memoization of board states
 * ArrayList – leaderboard & timing data
 * Recursion – Backtracking solver exploration
 *
 * PERFORMANCE FEATURES
 * ------------------------------------------------
 * • DP-based pruning to reduce redundant searches
 * • Recursive backtracking with controlled depth
 * • CPU failure detection for unsatisfiable states
 * • Real-time solver performance metrics:
 *      - recursive calls
 *      - maximum recursion depth
 *      - DP-pruned states
 *      - solver runtime
 * • Merge Sort ranking for scalable leaderboard
 * • Board difficulty estimation using inversion metrics
 *
 * TIME COMPLEXITIES
 * ------------------------------------------------
 * Backtracking Solver       : O(b^d) worst case
 * DP State Lookup           : O(1)
 * Conflict Detection        : O(n log n)
 * Merge Sort Ranking        : O(n log n)
 * Greedy Board Generation   : O(n²)
 *
 * AUTHOR NOTE
 * ------------------------------------------------
 * This project demonstrates practical application
 * of Divide & Conquer, Greedy, Backtracking, and
 * Dynamic Programming paradigms in an interactive
 * puzzle-solving environment using JavaFX.
 * ================================================================
 */

package LightUp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LumiLight extends Application {

    // ==================== FIELDS ====================

    private int rows = 7;
    private int cols = 7;
    private int[][] puzzle;
    private CellPane[][] cells;
    private Set<Point> greedySolution = new HashSet<>();

    private int userScore = 0;
    private int cpuScore = 0;
    private boolean isUserTurn = true;
    private boolean userPlacedBulb = false;
    private Point lastUserMove = null;

    private GridPane grid;
    private Button newBtn, resetBtn, checkBtn, hintBtn, endTurnBtn, graphBtn;
    private Button analyzeBtn;
    private Button leaderboardBtn;
    private ComboBox<String> gridSizeCombo;
    private Label userScoreLabel = new Label("User: 0");
    private Label cpuScoreLabel = new Label("CPU: 0");
    private Label turnLabel = new Label("Turn: USER");
    private Label gridSizeLabel = new Label("Grid Size:");
    private Label timerLabel = new Label("⏱️ 00:00");

    // Time tracking lists for graphs
    private List<Long> greedyTimes = new ArrayList<>();
    private List<Long> lightPropagationTimes = new ArrayList<>();
    private List<Long> conflictDetectionTimes = new ArrayList<>();

    
    private int recursionDepth = 0;
    private int maxRecursionDepth = 0;

    // Sorting algorithm time tracking
    private List<Long> mergeSortTimes = new ArrayList<>();
    private List<Long> inversionCountTimes = new ArrayList<>();
    private List<Integer> boardComplexityScores = new ArrayList<>();

 // ==================== BACKTRACKING ANALYSIS TRACKING ====================
    private int solverRecursiveCalls = 0;
    private int solverPrunedStates = 0;
    private int solverMaxDepth = 0;
    private Set<String> solverVisitedStates = new HashSet<>();
    
    // Leaderboard and game tracking
    private List<PlayerScore> leaderboard = new ArrayList<>();
    private static final String LEADERBOARD_FILE = "lightup_leaderboard.dat";
    private long gameStartTime;
    private int totalMovesMade = 0;
    private int hintsUsed = 0;
    private int bulbsPlacedByUser = 0;

    private VBox mainRoot;
    private Scene mainScene;
    private Stage primaryStage;

    private boolean timerRunning = false;
    private Thread timerThread;
    
    
	 // ==================== BACKTRACKING + DP ====================
	
	 // Memoization for failed board states (DP)
	 private Set<String> failedStatesDP = new HashSet<>();
	
	 // Limit backtracking depth (safety)
	 private static final int MAX_BACKTRACK_DEPTH = 15;
	 
	 
	 
	 private String encodeBoardState(boolean[][] bulb) {
		    StringBuilder sb = new StringBuilder(rows * cols);
		    for (int r = 0; r < rows; r++) {
		        for (int c = 0; c < cols; c++) {
		            sb.append(bulb[r][c] ? '1' : '0');
		        }
		    }
		    return sb.toString();
		}

    // ==================== PlayerScore Inner Class for Leaderboard
    // ====================
    private static class PlayerScore implements Serializable {
        private static final long serialVersionUID = 1L;

        String playerName;
        int score;
        int gridSize;
        int movesMade;
        long timeTaken;
        int hintsUsed;
        int bulbsPlaced;
        double efficiency;
        String date;
        boolean won;

        public PlayerScore(String playerName, int score, int gridSize, int movesMade,
                long timeTaken, int hintsUsed, int bulbsPlaced, boolean won) {
            this.playerName = playerName;
            this.score = score;
            this.gridSize = gridSize;
            this.movesMade = movesMade;
            this.timeTaken = timeTaken;
            this.hintsUsed = hintsUsed;
            this.bulbsPlaced = bulbsPlaced;
            this.efficiency = movesMade > 0 ? (double) score / movesMade : 0;
            this.won = won;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            this.date = LocalDateTime.now().format(formatter);
        }
    }

    // ==================== MergeSorter Class for D&C Implementation
    // ====================
    private static class MergeSorter {

        // Classic Merge Sort - O(n log n)
        public static List<Integer> sort(List<Integer> arr) {
            if (arr == null || arr.size() <= 1)
                return new ArrayList<>(arr);

            int mid = arr.size() / 2;
            List<Integer> left = sort(new ArrayList<>(arr.subList(0, mid)));
            List<Integer> right = sort(new ArrayList<>(arr.subList(mid, arr.size())));

            return merge(left, right);
        }

        private static List<Integer> merge(List<Integer> left, List<Integer> right) {
            List<Integer> result = new ArrayList<>();
            int i = 0, j = 0;

            while (i < left.size() && j < right.size()) {
                if (left.get(i) <= right.get(j)) {
                    result.add(left.get(i++));
                } else {
                    result.add(right.get(j++));
                }
            }

            while (i < left.size())
                result.add(left.get(i++));
            while (j < right.size())
                result.add(right.get(j++));

            return result;
        }

        // Count inversions using modified Merge Sort - O(n log n)
        public static int countInversions(List<Integer> arr) {
            if (arr == null || arr.size() <= 1)
                return 0;
            List<Integer> temp = new ArrayList<>(arr);
            return countInversionsHelper(temp, 0, arr.size() - 1);
        }

        private static int countInversionsHelper(List<Integer> arr, int left, int right) {
            if (left >= right)
                return 0;

            int mid = left + (right - left) / 2;
            int invCount = countInversionsHelper(arr, left, mid)
                    + countInversionsHelper(arr, mid + 1, right);

            // Merge and count cross inversions
            List<Integer> temp = new ArrayList<>();
            int i = left, j = mid + 1;

            while (i <= mid && j <= right) {
                if (arr.get(i) <= arr.get(j)) {
                    temp.add(arr.get(i++));
                } else {
                    temp.add(arr.get(j++));
                    invCount += (mid - i + 1);
                }
            }

            while (i <= mid)
                temp.add(arr.get(i++));
            while (j <= right)
                temp.add(arr.get(j++));

            // Copy back
            for (int k = 0; k < temp.size(); k++) {
                arr.set(left + k, temp.get(k));
            }

            return invCount;
        }
    }

    // ==================== APPLICATION START ====================

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showGridSelectionScreen();
    }

    // ==================== GRID SELECTION SCREEN ====================

    private void showGridSelectionScreen() {
        VBox selectionRoot = new VBox(20);
        selectionRoot.setAlignment(Pos.CENTER);
        selectionRoot.setStyle(
                "-fx-padding: 40px; -fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label titleLabel = new Label("LumiNet");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("User vs CPU");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #e0e0e0;");

        VBox selectionBox = new VBox(15);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 10px;");
        selectionBox.setMaxWidth(400);

        Label selectLabel = new Label("Select Grid Size:");
        selectLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        gridSizeCombo = new ComboBox<>();
        gridSizeCombo.getItems().addAll(
                "5x5 (Easy)", "7x7 (Medium)", "9x9 (Hard)", "11x11 (Expert)", "13x13 (Master)");
        gridSizeCombo.setValue("7x7 (Medium)");
        gridSizeCombo.setStyle("-fx-font-size: 14px;");
        gridSizeCombo.setPrefWidth(200);

        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 16px; -fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 10px 30px; -fx-background-radius: 5px; -fx-cursor: hand;");
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-font-size: 16px; -fx-background-color: #5568d3; " +
                "-fx-text-fill: white; -fx-padding: 10px 30px; " +
                "-fx-background-radius: 5px; -fx-cursor: hand;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-font-size: 16px; -fx-background-color: #667eea; " +
                "-fx-text-fill: white; -fx-padding: 10px 30px; " +
                "-fx-background-radius: 5px; -fx-cursor: hand;"));

        startButton.setOnAction(e -> {
            String selected = gridSizeCombo.getValue();
            int size = Integer.parseInt(selected.split("x")[0]);
            rows = size;
            cols = size;
            initializeGame();
        });

        Button previewLeaderboardBtn = new Button("🏆 View Leaderboard");
        previewLeaderboardBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #FFD700; -fx-text-fill: black; " +
                "-fx-padding: 8px 20px; -fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;");
        previewLeaderboardBtn.setOnAction(e -> showLeaderboard());

        Label infoLabel = new Label(
                "💡 Place bulbs to light up the entire grid!\n⚠️ Bulbs cannot see each other\n🏆 Compete for high scores!");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-text-alignment: center;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(350);

        selectionBox.getChildren().addAll(selectLabel, gridSizeCombo, startButton, previewLeaderboardBtn, infoLabel);
        selectionRoot.getChildren().addAll(titleLabel, subtitleLabel, selectionBox);

        Scene selectionScene = new Scene(selectionRoot, 600, 550);
        primaryStage.setScene(selectionScene);
        primaryStage.setTitle("Light Up Game - Grid Selection");
        primaryStage.show();
    }

    // ==================== GAME INITIALIZATION ====================

    private void initializeGame() {
        mainRoot = new VBox(8);
        mainRoot.setAlignment(Pos.TOP_CENTER);
        mainRoot.setStyle("-fx-padding: 12px; -fx-background-color: #f4f4f4;");

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER);

        // Initialize buttons
        newBtn = new Button("New Game");
        resetBtn = new Button("Reset");
        checkBtn = new Button("Check Solution");
        hintBtn = new Button("Hint (-2 pts)");
        endTurnBtn = new Button("End Turn");
        graphBtn = new Button("📊 Complexity");
        analyzeBtn = new Button("🔍 Analyze Board");
        leaderboardBtn = new Button("🏆 Leaderboard");

        gridSizeLabel = new Label("Grid: " + rows + "x" + cols);
        gridSizeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #764ba2;");

        Button changeGridBtn = new Button("Change Grid");
        changeGridBtn.setStyle("-fx-font-size: 12px;");
        changeGridBtn.setOnAction(e -> {
            resetGameStats();
            showGridSelectionScreen();
        });

        HBox scoreBox = new HBox(20);
        scoreBox.setAlignment(Pos.CENTER);
        userScoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        cpuScoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        turnLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: green;");

        timerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF5722;");

        scoreBox.getChildren().addAll(userScoreLabel, cpuScoreLabel, turnLabel, timerLabel, gridSizeLabel);

        topBar.getChildren().addAll(newBtn, resetBtn, checkBtn, hintBtn, endTurnBtn,
                graphBtn, analyzeBtn, leaderboardBtn, changeGridBtn);

        setupButtonHandlers();

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid.setVgap(2);

        puzzle = new int[rows][cols];
        cells = new CellPane[rows][cols];

        generatePuzzle();
        buildGrid();
        greedySolution = computeGreedySolution();
        updateAll();

        mainRoot.getChildren().addAll(topBar, scoreBox, grid);

        int windowWidth = Math.max(800, cols * 66 + 200);
        int windowHeight = Math.max(680, rows * 66 + 200);

        mainScene = new Scene(mainRoot, windowWidth, windowHeight);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Light Up (Akari) – User vs CPU [" + rows + "x" + cols + "]");

        startGameTimer();
        startTimerUpdate();

        primaryStage.show();
    }

    private void setupButtonHandlers() {
        newBtn.setOnAction(e -> {

            stopGameTimer(); // 🛑 STOP OLD TIMER THREAD

            resetGameStats();

            Platform.runLater(() -> {
                generatePuzzle();
                buildGrid();
                greedySolution = computeGreedySolution();

                startGameTimer(); // ⏱️ RESET TIME (00:00)
                startTimerUpdate(); // ▶ START NEW TIMER THREAD
            });
        });

        resetBtn.setOnAction(e -> {
            clearBoard();
            startGameTimer();
        });

        hintBtn.setOnAction(e -> {
            if (!isUserTurn) {
                showAlert("Not Your Turn", "Please wait for CPU to finish.");
                return;
            }
            updateUserScore(-2);
            trackHintUsed();
            giveGreedyHint();
        });

        endTurnBtn.setOnAction(e -> handleEndTurn());
        checkBtn.setOnAction(e -> checkSolution());
        graphBtn.setOnAction(e -> showTimeComplexityGraph());
        analyzeBtn.setOnAction(e -> showBoardAnalysis());
        leaderboardBtn.setOnAction(e -> showLeaderboard());
    }

    private void resetGameStats() {
        userScore = 0;
        cpuScore = 0;
        isUserTurn = true;
        userPlacedBulb = false;
        lastUserMove = null;
        totalMovesMade = 0;
        hintsUsed = 0;
        bulbsPlacedByUser = 0;

        greedyTimes.clear();
        lightPropagationTimes.clear();
        conflictDetectionTimes.clear();
        mergeSortTimes.clear();
        inversionCountTimes.clear();
        boardComplexityScores.clear();

        updateScores();
        updateTurnLabel();
        
        failedStatesDP.clear();
    }

    private void startGameTimer() {
        gameStartTime = System.currentTimeMillis();
        timerLabel.setText("⏱️ 00:00"); // ✅ RESET DISPLAY IMMEDIATELY
    }

    private void startTimerUpdate() {

        timerRunning = true;

        timerThread = new Thread(() -> {
            while (timerRunning) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        long elapsed = getTimeTaken();
                        long minutes = elapsed / 60;
                        long seconds = elapsed % 60;
                        timerLabel.setText(String.format("⏱️ %02d:%02d", minutes, seconds));
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        timerThread.setDaemon(true);
        timerThread.start();
    }

    private void stopGameTimer() {
        timerRunning = false;

        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }

    /**
     * Generates a random Light-Up puzzle board.
     * Steps:
     * 1. Randomly assign black/white cells
     * 2. Compute greedy bulb solution
     * 3. Convert some black cells into numbered clues
     * 4. Ensure numbers 1–4 exist at least once
     */
    private void generatePuzzle() {

        Random rand = new Random();

        // Step 1: Random black (−1) or white (0)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                puzzle[r][c] = rand.nextDouble() < 0.28 ? -1 : 0;
            }
        }

        // Step 2: Create greedy bulb configuration
        boolean[][] bulb = new boolean[rows][cols];
        computeGreedySolutionInternal(bulb);

        // Step 3: Convert some black cells into numbered clues
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (puzzle[r][c] == -1) {

                    int cnt = 0;
                    int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

                    // Count adjacent bulbs
                    for (int[] x : d) {
                        int rr = r + x[0];
                        int cc = c + x[1];
                        if (in(rr, cc) && bulb[rr][cc])
                            cnt++;
                    }

                    // Assign number if valid
                    if (cnt > 0 && cnt <= 4 && rand.nextDouble() < 0.6) {
                        puzzle[r][c] = cnt;
                    }
                }
            }
        }

        // Ensure at least one of each number exists
        ensureMinimumNumbers();
    }

    private void ensureMinimumNumbers() {

        boolean[] present = new boolean[5];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (puzzle[r][c] >= 1 && puzzle[r][c] <= 4) {
                    present[puzzle[r][c]] = true;
                }
            }
        }

        boolean[][] bulb = new boolean[rows][cols];
        computeGreedySolutionInternal(bulb);

        int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        for (int num = 1; num <= 4; num++) {

            if (!present[num]) {

                outer: for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {

                        if (puzzle[r][c] == -1) {

                            int whiteNeighbors = 0;
                            int bulbNeighbors = 0;

                            for (int[] x : d) {
                                int rr = r + x[0];
                                int cc = c + x[1];

                                if (in(rr, cc)) {
                                    if (puzzle[rr][cc] == 0)
                                        whiteNeighbors++;
                                    if (bulb[rr][cc])
                                        bulbNeighbors++;
                                }
                            }

                            // Strong validation
                            if (whiteNeighbors >= num && bulbNeighbors == num) {
                                puzzle[r][c] = num;
                                break outer;
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildGrid() {
        grid.getChildren().clear();

        int maxCellSize = 64;
        int minCellSize = 40;
        int cellSize = Math.max(minCellSize, Math.min(maxCellSize, 500 / Math.max(rows, cols)));

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new CellPane(r, c, puzzle[r][c], cellSize);
                grid.add(cells[r][c].stack, c, r);
            }
        updateAll();
    }

    private void clearBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] != null) {
                    cells[r][c].hasBulb = false;
                    cells[r][c].marked = false;
                    cells[r][c].lit = false;
                    cells[r][c].conflict = false;
                    if (cells[r][c].bulb != null) {
                        cells[r][c].bulb.setVisible(false);
                    }
                }
            }
        }
        resetGameStats();
        updateAll();
    }
    
    private boolean isBoardStateValid(boolean[][] bulb) {

        // 1. No bulb-bulb visibility conflicts
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (!bulb[r][c]) continue;

                int[][] d = {{-1,0},{1,0},{0,-1},{0,1}};
                for (int[] x : d) {
                    int rr = r + x[0], cc = c + x[1];
                    while (in(rr, cc) && puzzle[rr][cc] == 0) {
                        if (bulb[rr][cc]) return false;
                        rr += x[0];
                        cc += x[1];
                    }
                }
            }
        }

        // 2. Numbered cell overflow check
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (puzzle[r][c] <= 0) continue;

                int cnt = 0;
                int[][] d = {{-1,0},{1,0},{0,-1},{0,1}};
                for (int[] x : d) {
                    int rr = r + x[0], cc = c + x[1];
                    if (in(rr, cc) && bulb[rr][cc]) cnt++;
                }

                if (cnt > puzzle[r][c]) return false; // OVERFLOW
            }
        }

        return true;
    }

    // ==================== TURN MANAGEMENT ====================

    private void handleEndTurn() {
        if (!isUserTurn) {
            showAlert("Not Your Turn", "It's CPU's turn now.");
            return;
        }

        if (!userPlacedBulb) {
            showAlert("No Move Made", "You must place at least one bulb before ending your turn!");
            return;
        }

        if (hasInvalidMove()) {
            showAlert("Invalid Board State", "You have conflicts or violations. Fix them before ending turn!");
            return;
        }

        isUserTurn = false;
        userPlacedBulb = false;
        lastUserMove = null;
        updateTurnLabel();

        new Thread(() -> {
            try {
                Thread.sleep(800);
                Platform.runLater(this::cpuMove);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void cpuMove() {
        boolean moved = executeCpuGreedyMove();

        if (moved) {
            updateCpuScore(+1);
            updateAll();

            if (isPuzzleComplete()) {

                stopGameTimer();
                updateCpuScore(+5);
                showAlert("Game Over", "🤖 CPU WINS!\n\nUser Score: " + userScore + "\nCPU Score: " + cpuScore);
                saveScoreToLeaderboard(false);
                return;
            }
        }

        isUserTurn = true;
        updateTurnLabel();
    }

    private boolean executeCpuGreedyMove() {

        boolean[][] bulb = getCurrentBulbState();

        // Step 1: Generate candidate moves using D&C
        List<Point> candidates = getCandidateMovesUsingDC(bulb);

        // Step 2: Try to find a valid move using Backtracking + DP
        boolean success = backtrackCpuMove(candidates, bulb, 0);

        // ✅ STEP 3: CPU is truly stuck → SHOW POPUP HERE
        if (!success) {
            Platform.runLater(() -> {
                showAlert(
                    "CPU Move Not Possible",
                    "🤖 CPU cannot make a valid move.\n\n" +
                    "All possible moves violate constraints.\n" +
                    "Backtracking and DP pruning confirm\n" +
                    "that no legal continuation exists."
                );
            });
        }

        return success;
    }
    
    private List<Point> getCandidateMovesUsingDC(boolean[][] bulb) {

        List<Point> candidates = new ArrayList<>();

        // Best move from Divide & Conquer heuristic
        Point best = findBestGreedyMove();
        if (best != null) {
            candidates.add(best);
        }

        // Add safe fallback moves (still pruned)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (puzzle[r][c] == 0 &&
                    !bulb[r][c] &&
                    canPlaceBulb(r, c, bulb)) {

                    candidates.add(new Point(r, c));
                }
            }
        }

        return candidates;
    }
    
    
    private boolean backtrackCpuMove(
            List<Point> moves,
            boolean[][] bulb,
            int depth) {

        String stateKey = encodeBoardState(bulb);
        if (failedStatesDP.contains(stateKey)) {
            return false;
        }

        for (Point p : moves) {

            if (!canPlaceBulb(p.r, p.c, bulb)) continue;

            // TRY (simulation only)
            bulb[p.r][p.c] = true;

            // FULL validation
            if (isBoardStateValid(bulb)) {

                // ✅ SAFE COMMIT (UI update happens ONLY here)
                cells[p.r][p.c].toggleBulb(true);
                return true;
            }

            // UNDO
            bulb[p.r][p.c] = false;
        }

        // No valid move found
        failedStatesDP.add(stateKey);
        return false;
    }
    
    private boolean createsConflictState(boolean[][] bulb) {

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (!bulb[r][c]) continue;

                // Check visibility conflict
                int[][] d = { {-1,0}, {1,0}, {0,-1}, {0,1} };

                for (int[] x : d) {
                    int rr = r + x[0];
                    int cc = c + x[1];

                    while (in(rr, cc) && puzzle[rr][cc] == 0) {
                        if (bulb[rr][cc]) {
                            return true; // bulb conflict
                        }
                        rr += x[0];
                        cc += x[1];
                    }
                }
            }
        }
        return false;
    }

    // ==================== OPTIMIZED D&C VERSION ====================
    private Point findBestGreedyMove() {
        boolean[][] tempBulb = getCurrentBulbState();

        // Reset recursion tracking
        maxRecursionDepth = 0;
        recursionDepth = 0;

        // ========== USE DIVIDE & CONQUER APPROACH ==========
        long start = System.nanoTime();
        Point bestMove = findBestMoveDivideConquer(0, 0, rows, cols, tempBulb);
        long end = System.nanoTime();

        long dcTime = (end - start) / 1_000_000;
        if (dcTime == 0)
            dcTime = 1;
        mergeSortTimes.add(dcTime); // Track D&C time

        return bestMove;
    }

    /**
     * Divide & Conquer CPU move selection.
     * Recursively splits grid into quadrants
     * and returns highest scoring move.
     * Time Complexity: O(n log n)
     */
    private Point findBestMoveDivideConquer(int startR, int startC, int endR, int endC, boolean[][] bulb) {

        recursionDepth++;
        maxRecursionDepth = Math.max(maxRecursionDepth, recursionDepth);

        // Base case: small region → direct search
        int regionSize = (endR - startR) * (endC - startC);
        if (regionSize <= 9) {
            recursionDepth--;
            return findBestInRegion(startR, startC, endR, endC, bulb);
        }

        // Divide into 4 quadrants
        int midR = (startR + endR) / 2;
        int midC = (startC + endC) / 2;

        Point q1 = findBestMoveDivideConquer(startR, startC, midR, midC, bulb);
        Point q2 = findBestMoveDivideConquer(startR, midC, midR, endC, bulb);
        Point q3 = findBestMoveDivideConquer(midR, startC, endR, midC, bulb);
        Point q4 = findBestMoveDivideConquer(midR, midC, endR, endC, bulb);

        // Collect candidate moves
        List<ScoredMove> candidates = new ArrayList<>();

        if (q1 != null)
            candidates.add(new ScoredMove(q1.r, q1.c, evaluateMoveScore(q1.r, q1.c, bulb)));
        if (q2 != null)
            candidates.add(new ScoredMove(q2.r, q2.c, evaluateMoveScore(q2.r, q2.c, bulb)));
        if (q3 != null)
            candidates.add(new ScoredMove(q3.r, q3.c, evaluateMoveScore(q3.r, q3.c, bulb)));
        if (q4 != null)
            candidates.add(new ScoredMove(q4.r, q4.c, evaluateMoveScore(q4.r, q4.c, bulb)));

        recursionDepth--;

        if (candidates.isEmpty())
            return null;

        // Use Merge Sort to select best score
        List<Integer> scores = new ArrayList<>();
        for (ScoredMove m : candidates)
            scores.add(m.score);

        List<Integer> sortedScores = MergeSorter.sort(scores);
        int bestScore = sortedScores.get(sortedScores.size() - 1);

        for (ScoredMove m : candidates)
            if (m.score == bestScore)
                return new Point(m.r, m.c);

        return null;
    }

    // ========== BASE CASE: Direct search in small region ==========
    /**
     * Finds best move in a small region using direct search
     * Used as base case for D&C algorithm
     * Time Complexity: O(k²) where k is region size (k ≤ 3)
     */
    private Point findBestInRegion(int startR, int startC, int endR, int endC, boolean[][] bulb) {
        List<ScoredMove> candidates = new ArrayList<>();

        // Scan only cells in this region
        for (int r = startR; r < endR && r < rows; r++) {
            for (int c = startC; c < endC && c < cols; c++) {
                // Check if valid position
                if (cells[r][c] == null)
                    continue;

                if (puzzle[r][c] == 0 && !cells[r][c].hasBulb && canPlaceBulb(r, c, bulb)) {
                    int score = evaluateMoveScore(r, c, bulb);
                    candidates.add(new ScoredMove(r, c, score));
                }
            }
        }

        if (candidates.isEmpty())
            return null;

        // For small lists (≤9 elements), insertion sort is optimal
        insertionSortMoves(candidates);

        return new Point(candidates.get(0).r, candidates.get(0).c);
    }

    // ========== INSERTION SORT FOR SMALL LISTS ==========
    /**
     * Insertion sort - optimal for small arrays (n < 10)
     * Time Complexity: O(n²) but with very low constant factor
     * Used in base case where n ≤ 9
     */
    private void insertionSortMoves(List<ScoredMove> moves) {
        for (int i = 1; i < moves.size(); i++) {
            ScoredMove key = moves.get(i);
            int j = i - 1;

            // Sort in descending order (highest score first)
            while (j >= 0 && moves.get(j).score < key.score) {
                moves.set(j + 1, moves.get(j));
                j--;
            }
            moves.set(j + 1, key);
        }
    }

    /**
     * Evaluates quality of placing a bulb at (r,c).
     * Rewards:
     * - Lighting new cells
     * - Satisfying numbered constraints
     */
    private int evaluateMoveScore(int r, int c, boolean[][] bulb) {
        int score = 0;

        int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        // Reward lighting new cells
        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            while (in(rr, cc) && puzzle[rr][cc] == 0) {
                if (!isLitByOthers(rr, cc, bulb))
                    score += 3;
                rr += x[0];
                cc += x[1];
            }
        }

        // Reward numbered satisfaction
        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            if (in(rr, cc) && puzzle[rr][cc] > 0) {
                int adjacent = countAdjacentBulbs(rr, cc, bulb);

                if (adjacent + 1 == puzzle[rr][cc])
                    score += 10;
                else if (adjacent + 1 < puzzle[rr][cc])
                    score += 2;
            }
        }

        return score;
    }

    // ==================== HINT SYSTEM ====================

    private void giveGreedyHint() {
        for (Point p : greedySolution) {
            if (!cells[p.r][p.c].hasBulb) {
                cells[p.r][p.c].toggleBulb(true);
                userPlacedBulb = true;
                lastUserMove = new Point(p.r, p.c);
                updateAll();
                return;
            }
        }
        showAlert("No Hint", "No more hints available!");
    }

    private Set<Point> computeGreedySolution() {

        boolean[][] bulb = new boolean[rows][cols];

        long start = System.nanoTime(); // ✅ ADD
        computeGreedySolutionInternal(bulb);
        long end = System.nanoTime(); // ✅ ADD

        long time = (end - start) / 1_000_000; // ✅ ADD
        if (time == 0)
            time = 1;
        greedyTimes.add(time); // ✅ ADD

        Set<Point> sol = new HashSet<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (bulb[r][c])
                    sol.add(new Point(r, c));

        return sol;
    }

    /**
     * Greedy bulb placement algorithm.
     * Repeatedly places bulbs in unlit white cells
     * until no further valid placements exist.
     */
    private void computeGreedySolutionInternal(boolean[][] bulb) {
        boolean progress;

        do {
            progress = false;

            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    if (puzzle[r][c] == 0 &&
                            !isLit(r, c, bulb) &&
                            canPlaceBulb(r, c, bulb)) {

                        bulb[r][c] = true;
                        progress = true;
                    }

        } while (progress);
    }

    // ==================== BOARD STATE ====================

    private void updateAll() {
        long start, end;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] != null) {
                    cells[r][c].lit = false;
                }
            }
        }

        start = System.nanoTime();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] != null && cells[r][c].hasBulb) {
                    propagateLight(r, c);
                }
            }
        }
        end = System.nanoTime();
        long lightTime = (end - start) / 1_000_000;
        if (lightTime == 0)
            lightTime = 1;
        lightPropagationTimes.add(lightTime);

        start = System.nanoTime();
        detectBulbConflictsDivideConquer(); // ← NEW D&C METHOD

        end = System.nanoTime();
        long conflictTime = (end - start) / 1_000_000;
        if (conflictTime == 0)
            conflictTime = 1;
        conflictDetectionTimes.add(conflictTime);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] != null) {
                    cells[r][c].refresh();
                }
            }
        }
    }

    /**
     * Propagates light from bulb at (r,c)
     * in 4 directions until blocked by black cell.
     */
    private void propagateLight(int r, int c) {
        if (cells[r][c] == null)
            return;

        cells[r][c].lit = true;

        int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            while (in(rr, cc) && puzzle[rr][cc] == 0 && cells[rr][cc] != null) {
                cells[rr][cc].lit = true;
                rr += x[0];
                cc += x[1];
            }
        }
    }

    // private void detectBulbConflicts() {
    // for (int r = 0; r < rows; r++) {
    // for (int c = 0; c < cols; c++) {
    // if (cells[r][c] != null) {
    // cells[r][c].conflict = false;
    // }
    // }
    // }
    //
    // int[][] d = {{-1,0},{1,0},{0,-1},{0,1}};
    // for (int r = 0; r < rows; r++) {
    // for (int c = 0; c < cols; c++) {
    // if (cells[r][c] != null && cells[r][c].hasBulb) {
    // for (int[] x : d) {
    // int rr = r + x[0], cc = c + x[1];
    // while (in(rr, cc) && puzzle[rr][cc] == 0 && cells[rr][cc] != null) {
    // if (cells[rr][cc].hasBulb) {
    // cells[r][c].conflict = true;
    // cells[rr][cc].conflict = true;
    // }
    // rr += x[0]; cc += x[1];
    // }
    // }
    // }
    // }
    // }
    // }

    // ==================== D&C CONFLICT DETECTION ====================

    /**
     * Detect bulb conflicts using Divide & Conquer.
     * Bulbs conflict if they see each other in
     * same row/column without obstacle.
     * Complexity: O(n log n)
     */
    private void detectBulbConflictsDivideConquer() {

        // Clear previous conflicts
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c] != null)
                    cells[r][c].conflict = false;

        // Collect bulb positions
        List<Point> bulbs = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c] != null && cells[r][c].hasBulb)
                    bulbs.add(new Point(r, c));

        // Apply D&C detection
        if (!bulbs.isEmpty())
            detectConflictsInRegion(0, 0, rows, cols, bulbs);
    }

    /**
     * Recursively detect conflicts in a region using D&C
     * 
     * @param startR Starting row
     * @param startC Starting column
     * @param endR   Ending row (exclusive)
     * @param endC   Ending column (exclusive)
     * @param bulbs  List of all bulbs to check
     */
    private void detectConflictsInRegion(int startR, int startC, int endR, int endC, List<Point> bulbs) {
        // Filter bulbs in this region
        List<Point> regionBulbs = new ArrayList<>();
        for (Point b : bulbs) {
            if (b.r >= startR && b.r < endR && b.c >= startC && b.c < endC) {
                regionBulbs.add(b);
            }
        }

        // BASE CASE: Few bulbs, use direct checking
        if (regionBulbs.size() <= 4) {
            checkConflictsDirectly(regionBulbs);
            return;
        }

        // DIVIDE: Split into 4 quadrants
        int midR = (startR + endR) / 2;
        int midC = (startC + endC) / 2;

        // CONQUER: Recursively check each quadrant
        detectConflictsInRegion(startR, startC, midR, midC, bulbs); // Top-Left
        detectConflictsInRegion(startR, midC, midR, endC, bulbs); // Top-Right
        detectConflictsInRegion(midR, startC, endR, midC, bulbs); // Bottom-Left
        detectConflictsInRegion(midR, midC, endR, endC, bulbs); // Bottom-Right

        // COMBINE: Check conflicts across quadrant boundaries
        checkBoundaryConflicts(startR, startC, endR, endC, midR, midC, regionBulbs);
    }

    /**
     * Direct conflict checking for small regions (base case)
     */
    private void checkConflictsDirectly(List<Point> bulbs) {
        for (int i = 0; i < bulbs.size(); i++) {
            for (int j = i + 1; j < bulbs.size(); j++) {
                Point b1 = bulbs.get(i);
                Point b2 = bulbs.get(j);

                if (canBulbsSeeEachOther(b1.r, b1.c, b2.r, b2.c)) {
                    cells[b1.r][b1.c].conflict = true;
                    cells[b2.r][b2.c].conflict = true;
                }
            }
        }
    }

    /**
     * Check conflicts only across quadrant boundaries
     */
    private void checkBoundaryConflicts(int startR, int startC, int endR, int endC,
            int midR, int midC, List<Point> bulbs) {
        // Separate bulbs by quadrant
        List<Point> topLeft = new ArrayList<>();
        List<Point> topRight = new ArrayList<>();
        List<Point> bottomLeft = new ArrayList<>();
        List<Point> bottomRight = new ArrayList<>();

        for (Point b : bulbs) {
            if (b.r < midR && b.c < midC)
                topLeft.add(b);
            else if (b.r < midR && b.c >= midC)
                topRight.add(b);
            else if (b.r >= midR && b.c < midC)
                bottomLeft.add(b);
            else
                bottomRight.add(b);
        }

        // Only check cross-quadrant conflicts (6 combinations)
        checkCrossQuadrantConflicts(topLeft, topRight);
        checkCrossQuadrantConflicts(topLeft, bottomLeft);
        checkCrossQuadrantConflicts(topLeft, bottomRight);
        checkCrossQuadrantConflicts(topRight, bottomLeft);
        checkCrossQuadrantConflicts(topRight, bottomRight);
        checkCrossQuadrantConflicts(bottomLeft, bottomRight);
    }

    // Helper method to check conflicts between two different quadrants
    private void checkCrossQuadrantConflicts(List<Point> quad1, List<Point> quad2) {
        for (Point b1 : quad1) {
            for (Point b2 : quad2) {
                if (canBulbsSeeEachOther(b1.r, b1.c, b2.r, b2.c)) {
                    cells[b1.r][b1.c].conflict = true;
                    cells[b2.r][b2.c].conflict = true;
                }
            }
        }
    }

    /**
     * Check if two bulbs can see each other (no obstacles between them)
     */
    private boolean canBulbsSeeEachOther(int r1, int c1, int r2, int c2) {
        // Must be in same row or column
        if (r1 != r2 && c1 != c2)
            return false;

        // Check horizontal line
        if (r1 == r2) {
            int minC = Math.min(c1, c2);
            int maxC = Math.max(c1, c2);
            for (int c = minC + 1; c < maxC; c++) {
                if (puzzle[r1][c] != 0)
                    return false; // Blocked by black cell
            }
            return true;
        }

        // Check vertical line
        if (c1 == c2) {
            int minR = Math.min(r1, r2);
            int maxR = Math.max(r1, r2);
            for (int r = minR + 1; r < maxR; r++) {
                if (puzzle[r][c1] != 0)
                    return false; // Blocked by black cell
            }
            return true;
        }

        return false;
    }

    // ==================== VALIDATION ====================

    private boolean hasInvalidMove() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] == null)
                    continue;
                if (cells[r][c].conflict)
                    return true;
                if (puzzle[r][c] > 0 && cells[r][c].isOverflow())
                    return true;
            }
        }
        return false;
    }

    private boolean isMoveInvalid(int r, int c) {
        updateAll();
        if (cells[r][c] == null)
            return true;
        return cells[r][c].conflict || cells[r][c].causesNumberOverflow();
    }

    /**
     * Checks if puzzle is fully solved.
     * Conditions:
     * - All white cells lit
     * - No conflicts
     * - Numbered cells satisfied
     */
    private boolean isPuzzleComplete() {

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {

                if (cells[r][c] == null)
                    continue;

                if (puzzle[r][c] == 0 && !cells[r][c].lit)
                    return false;

                if (cells[r][c].conflict)
                    return false;

                if (puzzle[r][c] > 0 && !cells[r][c].isSatisfied())
                    return false;

                if (puzzle[r][c] > 0 && cells[r][c].isOverflow())
                    return false;
            }

        return true;
    }

    private void checkSolution() {
        if (isPuzzleComplete()) {
            stopGameTimer();
            boolean userWon = userScore > cpuScore;

            saveScoreToLeaderboard(userWon);

            if (userWon) {
                showVictoryScreen();
            } else if (cpuScore > userScore) {
                showAlert("Game Over", "🤖 CPU WINS!\n\nUser Score: " + userScore + "\nCPU Score: " + cpuScore);
            } else {
                showAlert("Draw!", "🤝 It's a TIE!\n\nBoth Score: " + userScore);
            }
        } else {
            showAlert("Incomplete", "Puzzle is not yet solved!");
        }
    }

    private void showVictoryScreen() {
        Stage victoryStage = new Stage();
        victoryStage.initModality(Modality.APPLICATION_MODAL);
        victoryStage.setTitle("🎉 VICTORY! 🎉");

        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 30px; -fx-background-color: linear-gradient(to bottom, #ffd700, #ffa500);");
        root.setAlignment(Pos.CENTER);

        Label title = new Label("🏆 CONGRATULATIONS! 🏆");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 2);");

        Label scoreLabel = new Label("You Win!\n\nFinal Score: " + userScore + "\nCPU Score: " + cpuScore);
        scoreLabel.setFont(Font.font(18));
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        scoreLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label statsLabel = new Label(String.format(
                "📊 Game Statistics:\n" +
                        "• Time: %02d:%02d\n" +
                        "• Total Moves: %d\n" +
                        "• Bulbs Placed: %d\n" +
                        "• Hints Used: %d\n" +
                        "• Efficiency: %.2f pts/move",
                getTimeTaken() / 60, getTimeTaken() % 60,
                totalMovesMade, bulbsPlacedByUser, hintsUsed,
                totalMovesMade > 0 ? (double) userScore / totalMovesMade : 0));
        statsLabel.setFont(Font.font(14));
        statsLabel.setStyle(
                "-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.3); -fx-padding: 15px; -fx-background-radius: 5;");

        Button closeBtn = new Button("Awesome! 🎮");
        closeBtn.setStyle("-fx-font-size: 16px; -fx-background-color: white; -fx-text-fill: #ffa500; " +
                "-fx-padding: 10px 30px; -fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> victoryStage.close());

        root.getChildren().addAll(title, scoreLabel, statsLabel, closeBtn);

        Scene scene = new Scene(root, 400, 500);
        victoryStage.setScene(scene);
        victoryStage.show();
    }

    // ==================== HELPERS ====================

    private boolean[][] getCurrentBulbState() {
        boolean[][] bulb = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] != null) {
                    bulb[r][c] = cells[r][c].hasBulb;
                }
            }
        }
        return bulb;
    }

    private boolean isLitByOthers(int r, int c, boolean[][] bulb) {
        if (bulb[r][c])
            return true;
        int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            while (in(rr, cc) && puzzle[rr][cc] == 0) {
                if (bulb[rr][cc])
                    return true;
                rr += x[0];
                cc += x[1];
            }
        }
        return false;
    }

    private int countAdjacentBulbs(int r, int c, boolean[][] bulb) {
        int cnt = 0;
        int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            if (in(rr, cc) && bulb[rr][cc])
                cnt++;
        }
        return cnt;
    }

    /**
     * Checks if bulb placement at (r,c) is valid.
     * Conditions:
     * - Cell must be white
     * - No visible bulb in row/column
     */
    private boolean canPlaceBulb(int r, int c, boolean[][] bulb) {

        if (puzzle[r][c] != 0)
            return false;

        int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            while (in(rr, cc) && puzzle[rr][cc] == 0) {
                if (bulb[rr][cc])
                    return false;
                rr += x[0];
                cc += x[1];
            }
        }

        return true;
    }

    private boolean isLit(int r, int c, boolean[][] bulb) {
        if (bulb[r][c])
            return true;
        int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            while (in(rr, cc) && puzzle[rr][cc] == 0) {
                if (bulb[rr][cc])
                    return true;
                rr += x[0];
                cc += x[1];
            }
        }
        return false;
    }

    private boolean in(int r, int c) {
        return r >= 0 && c >= 0 && r < rows && c < cols;
    }
    
    private boolean analyzeSolvability(boolean[][] bulb, int depth) {

        solverRecursiveCalls++;
        solverMaxDepth = Math.max(solverMaxDepth, depth);

        System.out.println("Depth: " + depth);

        if (!isBoardStateValid(bulb)) {
            System.out.println("Invalid state at depth " + depth);
            return false;
        }

        if (isPuzzleCompleteState(bulb)) {
            System.out.println("Complete at depth " + depth);
            return true;
        }

        List<Point> candidates = generateCandidateMoves(bulb);
        System.out.println("Candidates: " + candidates.size());

        for (Point p : candidates) {
            bulb[p.r][p.c] = true;
            if (analyzeSolvability(bulb, depth + 1))
                return true;
            bulb[p.r][p.c] = false;
        }

        return false;
    }
    
    private boolean isPuzzleCompleteState(boolean[][] bulb) {

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (puzzle[r][c] == 0) {

                    boolean lit = false;

                    if (bulb[r][c])
                        lit = true;
                    else
                        lit = isLit(r, c, bulb);

                    if (!lit)
                        return false;
                }

                if (puzzle[r][c] > 0) {
                    int adj = countAdjacentBulbs(r, c, bulb);
                    if (adj != puzzle[r][c])
                        return false;
                }
            }
        }

        return true;
    }
    private List<Point> generateCandidateMoves(boolean[][] bulb) {

        List<Point> candidates = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (puzzle[r][c] == 0 &&
                    !bulb[r][c] &&
                    canPlaceBulb(r, c, bulb)) {

                    candidates.add(new Point(r, c));
                }
            }
        }

        return candidates;
    }
    
    

    // ==================== SCORES AND TRACKING ====================

    private void updateUserScore(int delta) {
        userScore += delta;
        updateScores();
    }

    private void updateCpuScore(int delta) {
        cpuScore += delta;
        updateScores();
    }

    private void updateScores() {
        userScoreLabel.setText("User: " + userScore);
        cpuScoreLabel.setText("CPU: " + cpuScore);
    }

    private void updateTurnLabel() {
        if (isUserTurn) {
            turnLabel.setText("Turn: USER");
            turnLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: green;");
        } else {
            turnLabel.setText("Turn: CPU");
            turnLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: blue;");
        }
    }

    private void trackUserMove(boolean placedBulb) {
        totalMovesMade++;
        if (placedBulb) {
            bulbsPlacedByUser++;
        }
    }

    private void trackHintUsed() {
        hintsUsed++;
    }

    private long getTimeTaken() {
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }

    // ==================== LEADERBOARD METHODS ====================

    private void loadLeaderboard() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(LEADERBOARD_FILE))) {
            leaderboard = (List<PlayerScore>) ois.readObject();
        } catch (FileNotFoundException e) {
            leaderboard = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            leaderboard = new ArrayList<>();
        }
    }

    private void saveLeaderboard() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LEADERBOARD_FILE))) {
            oos.writeObject(leaderboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves player score and ranks leaderboard
     * using Merge Sort (Divide & Conquer).
     */
    private void saveScoreToLeaderboard(boolean won) {

        loadLeaderboard();

        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("🏆 New Score!");
        dialog.setHeaderText(won ? "Congratulations! You won!" : "Game Complete!");
        dialog.setContentText("Enter your name:");

        Optional<String> result = dialog.showAndWait();
        String playerName = result.orElse("Anonymous");

        PlayerScore score = new PlayerScore(
                playerName,
                userScore,
                rows,
                totalMovesMade,
                getTimeTaken(),
                hintsUsed,
                bulbsPlacedByUser,
                won);

        leaderboard.add(score);

        // Extract scores
        List<Integer> scores = new ArrayList<>();
        for (PlayerScore ps : leaderboard)
            scores.add(ps.score);

        // Sort using Merge Sort
        List<Integer> sortedScores = MergeSorter.sort(scores);
        Collections.reverse(sortedScores);

        // Rebuild sorted leaderboard
        List<PlayerScore> sortedList = new ArrayList<>();
        for (int sc : sortedScores)
            for (PlayerScore ps : leaderboard)
                if (ps.score == sc && !sortedList.contains(ps)) {
                    sortedList.add(ps);
                    break;
                }

        leaderboard = sortedList;

        if (leaderboard.size() > 100)
            leaderboard = new ArrayList<>(leaderboard.subList(0, 100));

        saveLeaderboard();
    }

    private void showLeaderboard() {
        loadLeaderboard();

        Stage leaderboardStage = new Stage();
        leaderboardStage.initModality(Modality.APPLICATION_MODAL);
        leaderboardStage.setTitle("🏆 Light Up Champion Leaderboard");

        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20px; -fx-background-color: linear-gradient(to bottom, #1a237e, #0d47a1);");
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("🏆 CHAMPIONS LEADERBOARD 🏆");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: gold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 2);");

        HBox filterBox = new HBox(20);
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setStyle("-fx-padding: 10px;");

        ComboBox<String> sizeFilter = new ComboBox<>();
        sizeFilter.getItems().addAll("All Sizes", "5x5", "7x7", "9x9", "11x11", "13x13");
        sizeFilter.setValue("All Sizes");
        sizeFilter.setStyle("-fx-font-size: 14px; -fx-background-color: white;");

        ComboBox<String> sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Sort by Score", "Sort by Efficiency", "Sort by Time");
        sortFilter.setValue("Sort by Score");
        sortFilter.setStyle("-fx-font-size: 14px; -fx-background-color: white;");

        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        filterBox.getChildren().addAll(filterLabel, sizeFilter, sortFilter);

        TableView<PlayerScore> table = new TableView<>();
        table.setStyle("-fx-background-color: transparent;");
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PlayerScore, Integer> rankCol = new TableColumn<>("Rank");
        rankCol.setCellValueFactory(cellData -> {
            int rank = table.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleIntegerProperty(rank).asObject();
        });
        rankCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PlayerScore, String> nameCol = new TableColumn<>("Player");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().playerName));

        TableColumn<PlayerScore, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().score).asObject());
        scoreCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PlayerScore, Integer> sizeCol = new TableColumn<>("Grid");
        sizeCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().gridSize).asObject());
        sizeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PlayerScore, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> {
            long time = cellData.getValue().timeTaken;
            String timeStr = String.format("%02d:%02d", time / 60, time % 60);
            return new SimpleStringProperty(timeStr);
        });
        timeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PlayerScore, String> effCol = new TableColumn<>("Efficiency");
        effCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().efficiency)));
        effCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PlayerScore, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().date));

        table.getColumns().addAll(rankCol, nameCol, scoreCol, sizeCol, timeCol, effCol, dateCol);

        ObservableList<PlayerScore> data = FXCollections.observableArrayList(leaderboard);
        table.setItems(data);

        sizeFilter.setOnAction(e -> {
            String selected = sizeFilter.getValue();
            if (selected.equals("All Sizes")) {
                table.setItems(FXCollections.observableArrayList(leaderboard));
            } else {
                int size = Integer.parseInt(selected.split("x")[0]);
                List<PlayerScore> filtered = new ArrayList<>();
                for (PlayerScore s : leaderboard) {
                    if (s.gridSize == size) {
                        filtered.add(s);
                    }
                }
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        sortFilter.setOnAction(e -> {
            List<PlayerScore> current = new ArrayList<>(table.getItems());
            switch (sortFilter.getValue()) {
                case "Sort by Score":
                    current.sort((a, b) -> Integer.compare(b.score, a.score));
                    break;
                case "Sort by Efficiency":
                    current.sort((a, b) -> Double.compare(b.efficiency, a.efficiency));
                    break;
                case "Sort by Time":
                    current.sort((a, b) -> Long.compare(a.timeTaken, b.timeTaken));
                    break;
            }
            table.setItems(FXCollections.observableArrayList(current));
        });

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-padding: 15px; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;");

        if (!leaderboard.isEmpty()) {
            PlayerScore top = leaderboard.get(0);
            double avgScore = 0;
            for (PlayerScore s : leaderboard) {
                avgScore += s.score;
            }
            avgScore /= leaderboard.size();

            Label topLabel = new Label("🥇 Top: " + top.playerName + " (" + top.score + ")");
            topLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");

            Label avgLabel = new Label("📊 Avg Score: " + String.format("%.1f", avgScore));
            avgLabel.setStyle("-fx-text-fill: white;");

            Label totalLabel = new Label("👥 Players: " + leaderboard.size());
            totalLabel.setStyle("-fx-text-fill: white;");

            statsBox.getChildren().addAll(topLabel, avgLabel, totalLabel);
        }

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #F44336; -fx-text-fill: white; " +
                "-fx-padding: 8px 30px; -fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;");

        clearBtn.setOnAction(e -> {
            leaderboard.clear(); // clear in-memory list
            saveLeaderboard(); // persist empty leaderboard
            table.getItems().clear(); // refresh UI table
            statsBox.getChildren().clear(); // optional: clear stats display
        });

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #FFD700; -fx-text-fill: black; " +
                "-fx-padding: 8px 30px; -fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> leaderboardStage.close());

        HBox buttonBox = new HBox(20, clearBtn, closeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, filterBox, table, statsBox, buttonBox);

        Scene scene = new Scene(root, 900, 600);
        leaderboardStage.setScene(scene);
        leaderboardStage.show();
    }

    // ==================== BOARD ANALYSIS WITH SORTING ====================

    private void showBoardAnalysis() {
        Stage analysisStage = new Stage();
        analysisStage.initModality(Modality.APPLICATION_MODAL);
        analysisStage.setTitle("Board Analysis using Divide & Conquer");

        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20px; -fx-background-color: white;");
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("🔍 Board State Analysis (Merge Sort - O(n log n))");
        title.setFont(Font.font(16));
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0;");

        List<Integer> boardNumbers = extractBoardNumbers();

        long start = System.nanoTime();
        List<Integer> sortedBoard = MergeSorter.sort(new ArrayList<>(boardNumbers));
        long end = System.nanoTime();
        long sortTime = (end - start) / 1_000_000;
        if (sortTime == 0)
            sortTime = 1;
        mergeSortTimes.add(sortTime);

        start = System.nanoTime();
        int inversions = MergeSorter.countInversions(new ArrayList<>(boardNumbers));
        end = System.nanoTime();
        long invTime = (end - start) / 1_000_000;
        if (invTime == 0)
            invTime = 1;
        inversionCountTimes.add(invTime);

        int complexityScore = calculateBoardComplexity(boardNumbers, inversions);
        boardComplexityScores.add(complexityScore);

     // ================= BACKTRACKING SOLVER ANALYSIS =================

        solverRecursiveCalls = 0;
        solverPrunedStates = 0;
        solverMaxDepth = 0;
        solverVisitedStates.clear();
        failedStatesDP.clear();

        boolean[][] bulbState = getCurrentBulbState();

        long solveStart = System.nanoTime();
        boolean solvable = analyzeSolvability(bulbState, 0);
        long solveEnd = System.nanoTime();

        long solveTime = (solveEnd - solveStart) / 1_000_000;
        if (solveTime == 0) solveTime = 1;
        
        HBox sortVisualization = createSortVisualization(boardNumbers, sortedBoard);

        GridPane metricsGrid = createAnalysisMetrics(boardNumbers, sortedBoard,
                inversions, sortTime, invTime,
                complexityScore);

        VBox explanationBox = createDnCExplanation();
        VBox inversionViz = createInversionVisualization(boardNumbers, inversions);

        root.getChildren().addAll(title, sortVisualization, metricsGrid, inversionViz, explanationBox);

        VBox solverBox = new VBox(5);
        solverBox.setStyle("-fx-padding: 10px; -fx-background-color: #e3f2fd;");

        Label solverTitle = new Label("🔎 Backtracking + DP Solver Analysis");
        solverTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2;");

        Label solvableLabel = new Label("Solvable: " + solvable);
        Label callsLabel = new Label("Recursive Calls: " + solverRecursiveCalls);
        Label prunedLabel = new Label("DP Pruned States: " + solverPrunedStates);
        Label depthLabel = new Label("Max Depth: " + solverMaxDepth);
        Label timeLabelSolver = new Label("Solve Time: " + solveTime + " ms");

        solverBox.getChildren().addAll(
            solverTitle,
            solvableLabel,
            callsLabel,
            prunedLabel,
            depthLabel,
            timeLabelSolver
        );

        root.getChildren().add(solverBox); 
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; " +
                "-fx-padding: 10px 30px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> analysisStage.close());
        root.getChildren().add(closeBtn);

        Scene scene = new Scene(root, 750, 750);
        analysisStage.setScene(scene);
        analysisStage.show();
    }

    private List<Integer> extractBoardNumbers() {
        List<Integer> numbers = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] == null)
                    continue;
                if (puzzle[r][c] > 0) {
                    numbers.add(puzzle[r][c]);
                } else if (cells[r][c].hasBulb) {
                    numbers.add(8);
                } else if (puzzle[r][c] == -1) {
                    numbers.add(9);
                } else {
                    numbers.add(0);
                }
            }
        }

        return numbers;
    }

    private int calculateBoardComplexity(List<Integer> board, int inversions) {
        Set<Integer> uniqueSet = new HashSet<>(board);
        int uniqueElements = uniqueSet.size();
        int zeroCount = 0;
        for (int n : board) {
            if (n == 0)
                zeroCount++;
        }

        return (inversions * uniqueElements) / Math.max(1, zeroCount);
    }

    private HBox createSortVisualization(List<Integer> original, List<Integer> sorted) {
        HBox box = new HBox(30);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 20px; -fx-background-color: #f0f0f0; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        VBox originalBox = new VBox(5);
        originalBox.setAlignment(Pos.CENTER);
        Label originalLabel = new Label("Original Board State");
        originalLabel.setStyle("-fx-font-weight: bold;");

        HBox originalViz = new HBox(2);
        originalViz.setAlignment(Pos.CENTER);
        for (int num : original) {
            Rectangle rect = new Rectangle(25, 25);
            rect.setFill(getColorForNumber(num));
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1);

            Text text = new Text(String.valueOf(num));
            text.setFont(Font.font(10));

            StackPane stack = new StackPane();
            stack.getChildren().addAll(rect, text);
            originalViz.getChildren().add(stack);
        }
        originalBox.getChildren().addAll(originalLabel, originalViz);

        Label arrow = new Label("→");
        arrow.setFont(Font.font(30));
        arrow.setStyle("-fx-text-fill: #9C27B0;");

        VBox sortedBox = new VBox(5);
        sortedBox.setAlignment(Pos.CENTER);
        Label sortedLabel = new Label("After Merge Sort (D&C)");
        sortedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0;");

        HBox sortedViz = new HBox(2);
        sortedViz.setAlignment(Pos.CENTER);
        for (int num : sorted) {
            Rectangle rect = new Rectangle(25, 25);
            rect.setFill(getColorForNumber(num));
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1);

            Text text = new Text(String.valueOf(num));
            text.setFont(Font.font(10));

            StackPane stack = new StackPane();
            stack.getChildren().addAll(rect, text);
            sortedViz.getChildren().add(stack);
        }
        sortedBox.getChildren().addAll(sortedLabel, sortedViz);

        box.getChildren().addAll(originalBox, arrow, sortedBox);
        return box;
    }

    private Color getColorForNumber(int num) {
        switch (num) {
            case 0:
                return Color.LIGHTGRAY;
            case 1:
                return Color.LIGHTGREEN;
            case 2:
                return Color.LIGHTBLUE;
            case 3:
                return Color.YELLOW;
            case 4:
                return Color.ORANGE;
            case 8:
                return Color.GOLD;
            case 9:
                return Color.DARKGRAY;
            default:
                return Color.WHITE;
        }
    }

    private GridPane createAnalysisMetrics(List<Integer> original, List<Integer> sorted,
            int inversions, long sortTime, long invTime,
            int complexity) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-padding: 15px; -fx-background-color: #e8f5e8; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Label statsLabel = new Label("📊 Array Statistics:");
        statsLabel.setStyle("-fx-font-weight: bold;");
        grid.add(statsLabel, 0, 0);
        grid.add(new Label("Size: " + original.size() + " elements"), 1, 0);

        Set<Integer> uniqueSet = new HashSet<>(original);
        grid.add(new Label("Unique Values: " + uniqueSet.size()), 2, 0);

        Label invLabel_title = new Label("🔄 Inversion Analysis:");
        invLabel_title.setStyle("-fx-font-weight: bold;");
        grid.add(invLabel_title, 0, 1);

        Label invLabel = new Label(inversions + " inversions");
        invLabel.setStyle(inversions > 100 ? "-fx-text-fill: red;"
                : inversions > 50 ? "-fx-text-fill: orange;" : "-fx-text-fill: green;");
        grid.add(invLabel, 1, 1);
        grid.add(new Label("(Measure of disorder)"), 2, 1);

        Label timeLabel = new Label("⏱️ Time Analysis:");
        timeLabel.setStyle("-fx-font-weight: bold;");
        grid.add(timeLabel, 0, 2);
        grid.add(new Label("Merge Sort: " + sortTime + " ms"), 1, 2);
        grid.add(new Label("Inversion Count: " + invTime + " ms"), 2, 2);

        Label complexityTitle = new Label("🎯 Complexity Score:");
        complexityTitle.setStyle("-fx-font-weight: bold;");
        grid.add(complexityTitle, 0, 3);

        Label complexityLabel = new Label(complexity + " points");
        complexityLabel.setStyle(complexity > 500 ? "-fx-text-fill: red; -fx-font-weight: bold;"
                : complexity > 200 ? "-fx-text-fill: orange; -fx-font-weight: bold;"
                        : "-fx-text-fill: green; -fx-font-weight: bold;");
        grid.add(complexityLabel, 1, 3);
        grid.add(new Label("(Higher = harder to solve)"), 2, 3);

        return grid;
    }

    private VBox createDnCExplanation() {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 15px; -fx-background-color: #f3e5f5; " +
                "-fx-border-color: #9C27B0; -fx-border-width: 2; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("🧠 Divide & Conquer in Action:");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #9C27B0;");

        Label step1 = new Label("1️⃣ DIVIDE: Split array into halves recursively");
        Label step2 = new Label("2️⃣ CONQUER: Sort each half independently");
        Label step3 = new Label("3️⃣ COMBINE: Merge sorted halves (O(n) merge)");
        Label complexity = new Label("📈 Time Complexity: O(n log n) — Much faster than O(n²)!");

        step1.setStyle("-fx-font-size: 11px;");
        step2.setStyle("-fx-font-size: 11px;");
        step3.setStyle("-fx-font-size: 11px;");
        complexity.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        box.getChildren().addAll(title, step1, step2, step3, complexity);
        return box;
    }

    private VBox createInversionVisualization(List<Integer> board, int inversions) {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 15px; -fx-background-color: #fff3e0; " +
                "-fx-border-color: #FF9800; -fx-border-width: 2; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("🔄 Inversion Count: " + inversions);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #FF9800;");

        StringBuilder example = new StringBuilder("Example inversions: ");
        int count = 0;
        for (int i = 0; i < board.size() && count < 5; i++) {
            for (int j = i + 1; j < board.size() && count < 5; j++) {
                if (board.get(i) > board.get(j) && board.get(i) != 9 && board.get(j) != 9) {
                    example.append("(" + board.get(i) + "," + board.get(j) + ") ");
                    count++;
                }
            }
        }

        Label exampleLabel = new Label(example.toString());
        exampleLabel.setStyle("-fx-font-size: 11px;");

        Label meaning = new Label("📌 Inversions measure how unsorted the board is. " +
                "Higher inversions = more complex puzzle!");
        meaning.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
        meaning.setWrapText(true);

        box.getChildren().addAll(title, exampleLabel, meaning);
        return box;
    }

    // ==================== TIME COMPLEXITY GRAPH WITH LOGARITHMIC SCALE
    // ====================

    private void showTimeComplexityGraph() {
        // Debug output
        debugPrintGraphData();

        Stage graphStage = new Stage();
        graphStage.initModality(Modality.APPLICATION_MODAL);
        graphStage.setTitle("Time Complexity Analysis - " + rows + "x" + cols + " Grid");

        // ========== CREATE MAIN CONTENT BOX ==========
        VBox contentBox = new VBox(15);
        contentBox.setStyle("-fx-padding: 20px; -fx-background-color: white;");
        contentBox.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Algorithm Performance Analysis (" + rows + "x" + cols + " Grid)");
        title.setFont(Font.font(18));
        title.setStyle("-fx-font-weight: bold;");

        // ========== CREATE THE GRAPH ==========
        Pane graphContainer = createGraph(); // This returns VBox with subplots

        // ========== CREATE INFO SECTIONS ==========
        VBox statsBox = createStatistics();
        statsBox.setMaxWidth(750);

        VBox recursionBox = createRecursionDepthInfo();
        recursionBox.setMaxWidth(750);

        VBox complexityInfo = createComplexityInfo();
        complexityInfo.setMaxWidth(750);

        // ========== ADD EVERYTHING TO CONTENT BOX ==========
        contentBox.getChildren().addAll(title, graphContainer, statsBox, recursionBox, complexityInfo);

        // ========== WRAP IN SCROLLPANE ==========
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // ========== CREATE SCENE WITH LARGER HEIGHT ==========
        Scene scene = new Scene(scrollPane, 850, 800); // Increased height
        graphStage.setScene(scene);
        graphStage.show();
    }

    private void debugPrintGraphData() {
        System.out.println("=== Graph Data Debug ===");
        System.out.println("Greedy Times: " + greedyTimes.size() + " samples - " + greedyTimes);
        System.out
                .println("Light Propagation: " + lightPropagationTimes.size() + " samples - " + lightPropagationTimes);
        System.out.println(
                "Conflict Detection: " + conflictDetectionTimes.size() + " samples - " + conflictDetectionTimes);
        System.out.println("Merge Sort: " + mergeSortTimes.size() + " samples - " + mergeSortTimes);
        System.out.println("Inversion Count: " + inversionCountTimes.size() + " samples - " + inversionCountTimes);
    }

    private Pane createGraph() {
        VBox container = new VBox(5);
        container.setStyle("-fx-padding: 10px; -fx-background-color: white;");

        // Title
        Label title = new Label("Algorithm Performance Over Time");
        title.setFont(Font.font(14));
        title.setStyle("-fx-font-weight: bold; -fx-padding: 10px;");
        container.getChildren().add(title);

        // Create mini graph for each algorithm (IF DATA EXISTS)
        if (!greedyTimes.isEmpty()) {
            container.getChildren().add(createMiniGraph(greedyTimes, Color.web("#2196F3"), "Greedy Algorithm"));
        }
        if (!conflictDetectionTimes.isEmpty()) {
            container.getChildren()
                    .add(createMiniGraph(conflictDetectionTimes, Color.web("#F44336"), "Conflict Detection"));
        }
        if (!mergeSortTimes.isEmpty()) {
            container.getChildren().add(createMiniGraph(mergeSortTimes, Color.web("#9C27B0"), "Merge Sort (D&C)"));
        }
        if (!inversionCountTimes.isEmpty()) {
            container.getChildren().add(createMiniGraph(inversionCountTimes, Color.web("#FF9800"), "Inversion Count"));
        }
        if (!lightPropagationTimes.isEmpty()) {
            container.getChildren()
                    .add(createMiniGraph(lightPropagationTimes, Color.web("#4CAF50"), "Light Propagation"));
        }

        // ========== IMPORTANT: Set minimum height to prevent overlap ==========
        int numGraphs = 0;
        if (!greedyTimes.isEmpty())
            numGraphs++;
        if (!conflictDetectionTimes.isEmpty())
            numGraphs++;
        if (!mergeSortTimes.isEmpty())
            numGraphs++;
        if (!inversionCountTimes.isEmpty())
            numGraphs++;
        if (!lightPropagationTimes.isEmpty())
            numGraphs++;

        container.setMinHeight(numGraphs * 100 + 50); // Reserve space for all graphs

        return container;
    }

    private Pane createMiniGraph(List<Long> times, Color color, String name) {
        Pane pane = new Pane();
        pane.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; " +
                "-fx-background-color: white; -fx-padding: 5px;");
        pane.setPrefSize(730, 95); // Slightly taller
        pane.setMinHeight(95);
        pane.setMaxHeight(95); // ✅ PREVENT COLLAPSE

        double width = 730;
        double height = 95;
        double padding = 50;
        double graphWidth = width - 2 * padding;
        double graphHeight = height - 40; // More space for graph

        // Title
        Text title = new Text(10, 20, name);
        title.setFont(Font.font(12));
        title.setFill(color);
        title.setStyle("-fx-font-weight: bold;");
        pane.getChildren().add(title);

        // Calculate stats
        long maxTime = Collections.max(times);
        long minTime = Collections.min(times);
        long sum = 0;
        for (long t : times)
            sum += t;
        long avg = sum / times.size();

        // Stats text
        Text stats = new Text(width - 300, 20,
                String.format("Samples: %d | Avg: %dms | Min: %dms | Max: %dms",
                        times.size(), avg, minTime, maxTime));
        stats.setFont(Font.font(9));
        stats.setFill(Color.GRAY);
        pane.getChildren().add(stats);

        // Draw axes
        double baseY = height - 15;
        Line xAxis = new Line(padding, baseY, width - padding, baseY);
        Line yAxis = new Line(padding, 28, padding, baseY);
        xAxis.setStroke(Color.LIGHTGRAY);
        yAxis.setStroke(Color.LIGHTGRAY);
        xAxis.setStrokeWidth(1.5);
        yAxis.setStrokeWidth(1.5);
        pane.getChildren().addAll(xAxis, yAxis);

        // Y-axis labels
        Text yMin = new Text(padding - 38, baseY + 5, "0");
        Text yMax = new Text(padding - 38, 33, maxTime + "ms");
        yMin.setFont(Font.font(9));
        yMax.setFont(Font.font(9));
        pane.getChildren().addAll(yMin, yMax);

        int dataSize = times.size();

        // Plot lines
        if (dataSize > 1) {
            for (int i = 0; i < dataSize - 1; i++) {
                double x1 = padding + ((double) i * graphWidth / (dataSize - 1));
                double y1 = baseY - (times.get(i) * graphHeight / (double) maxTime);

                double x2 = padding + ((double) (i + 1) * graphWidth / (dataSize - 1));
                double y2 = baseY - (times.get(i + 1) * graphHeight / (double) maxTime);

                Line line = new Line(x1, y1, x2, y2);
                line.setStroke(color);
                line.setStrokeWidth(2.5);
                pane.getChildren().add(line);
            }
        }

        // Plot points
        for (int i = 0; i < dataSize; i++) {
            double x = padding + ((double) i * graphWidth / Math.max(1, dataSize - 1));
            double y = baseY - (times.get(i) * graphHeight / (double) maxTime);

            Circle point = new Circle(x, y, 4);
            point.setFill(color);
            point.setStroke(Color.WHITE);
            point.setStrokeWidth(1.5);

            Tooltip tooltip = new Tooltip(String.format("Sample #%d: %d ms", i + 1, times.get(i)));
            Tooltip.install(point, tooltip);

            pane.getChildren().add(point);
        }

        return pane;
    }

    // ========== HELPER METHOD FOR LEGEND ==========
    private void addLegend(Pane pane, double padding, int legendIndex, Color color, String name, int dataSize) {
        double legendX = padding + 10;
        double legendY = padding + 20 + (legendIndex * 25);

        Circle legendDot = new Circle(legendX, legendY, 6, color);
        legendDot.setStroke(Color.WHITE);
        legendDot.setStrokeWidth(1);

        Text legendText = new Text(legendX + 20, legendY + 5,
                String.format("%s (%d sample%s)", name, dataSize, dataSize == 1 ? "" : "s"));
        legendText.setFont(Font.font(11));
        legendText.setStyle("-fx-font-weight: bold;");

        pane.getChildren().addAll(legendDot, legendText);
    }

    // ========== Recursion Depth Visualization ==========
    private VBox createRecursionDepthInfo() {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 15px; -fx-background-color: #fff3e0; " +
                "-fx-border-color: #FF9800; -fx-border-width: 2; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("D&C Recursion Analysis:");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #FF9800;");

        int theoreticalDepth = (int) Math.ceil(Math.log(rows * cols) / Math.log(4));

        Label depthLabel = new Label(String.format(
                "Maximum Recursion Depth: %d levels", maxRecursionDepth));
        depthLabel.setStyle("-fx-font-size: 11px;");

        Label theoreticalLabel = new Label(String.format(
                "Theoretical Maximum: log4(%d) = %d levels", rows * cols, theoreticalDepth));
        theoreticalLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        Label explanation = new Label(
                "Each level divides the grid into 4 quadrants until regions are <=9 cells. " +
                        "This logarithmic division is what makes D&C efficient!");
        explanation.setStyle("-fx-font-size: 10px; -fx-font-style: italic;");
        explanation.setWrapText(true);

        box.getChildren().addAll(title, depthLabel, theoreticalLabel, explanation);
        return box;
    }

    private VBox createStatistics() {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 15px; -fx-background-color: #f5f5f5; -fx-border-color: #ccc; " +
                "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label statsTitle = new Label("Performance Statistics:");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        HBox legendBox = new HBox(15);
        legendBox.setAlignment(Pos.CENTER);

        VBox greedyBox = createStatBox("Greedy", Color.web("#2196F3"), greedyTimes);
        VBox conflictBox = createStatBox("Conflict", Color.web("#F44336"), conflictDetectionTimes);
        VBox mergeBox = createStatBox("Merge Sort", Color.web("#9C27B0"), mergeSortTimes);
        VBox invBox = createStatBox("Inversions", Color.web("#FF9800"), inversionCountTimes);
        VBox lightBox = createStatBox("Light Prop", Color.web("#4CAF50"), lightPropagationTimes);

        legendBox.getChildren().addAll(greedyBox, conflictBox, mergeBox, invBox, lightBox);
        box.getChildren().addAll(statsTitle, legendBox);

        return box;
    }

    private VBox createStatBox(String name, Color color, List<Long> times) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 8px; -fx-background-color: white; -fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;");
        box.setMinWidth(100);

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER);

        Circle colorIndicator = new Circle(6, color);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        titleBox.getChildren().addAll(colorIndicator, nameLabel);

        if (!times.isEmpty()) {
            long sum = 0;
            for (long t : times)
                sum += t;
            long avg = sum / times.size();

            long min = Collections.min(times);
            long max = Collections.max(times);

            Label avgLabel = new Label("Avg: " + avg + " ms");
            Label minLabel = new Label("Min: " + min + " ms");
            Label maxLabel = new Label("Max: " + max + " ms");

            avgLabel.setStyle("-fx-font-size: 9px;");
            minLabel.setStyle("-fx-font-size: 9px;");
            maxLabel.setStyle("-fx-font-size: 9px;");

            box.getChildren().addAll(titleBox, avgLabel, minLabel, maxLabel);
        } else {
            Label noData = new Label("No data");
            noData.setStyle("-fx-font-size: 9px; -fx-text-fill: gray;");
            box.getChildren().addAll(titleBox, noData);
        }

        return box;
    }

    private VBox createComplexityInfo() {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 15px; -fx-background-color: #e3f2fd; -fx-border-color: #2196F3; " +
                "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("Time Complexity Analysis:");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1976D2;");

        int n = rows * cols;

        VBox algoBox = new VBox(3);

        Label greedy = new Label("🔵 CPU Move Selection (D&C): O(n log n)");
        Label greedyDesc = new Label("   Divides grid into quadrants, max depth: " + maxRecursionDepth);

        Label conflict = new Label("🔴 Conflict Detection (D&C): O(n log n)");
        Label conflictDesc = new Label("   Uses quadrant division with boundary checking");

        Label merge = new Label("🟣 Merge Sort (D&C): O(n log n)");
        Label mergeDesc = new Label("   Divides & conquers recursively");

        Label inversion = new Label("🟠 Inversion Count: O(n log n)");
        Label inversionDesc = new Label("   Uses modified Merge Sort");

        Label overall = new Label(
                "📊 CPU Move Selection: O(n log n) using D&C quadrant division\n" +
                        "Conflict Detection: O(n log n) using spatial partitioning\n" +
                        "Merge Sort & Inversion Count: O(n log n)\n" +
                        "✅ ALL algorithms now use Divide & Conquer paradigm!\n" +
                        "Space Complexity: O(log n) for recursion stack");

        greedy.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        conflict.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        merge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9C27B0;");
        inversion.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #FF9800;");

        greedyDesc.setStyle("-fx-font-size: 9px; -fx-text-fill: #555;");
        conflictDesc.setStyle("-fx-font-size: 9px; -fx-text-fill: #555;");
        mergeDesc.setStyle("-fx-font-size: 9px; -fx-text-fill: #555;");
        inversionDesc.setStyle("-fx-font-size: 9px; -fx-text-fill: #555;");
        overall.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        algoBox.getChildren().addAll(greedy, greedyDesc, conflict, conflictDesc,
                merge, mergeDesc, inversion, inversionDesc, overall);
        box.getChildren().addAll(title, algoBox);
        return box;
    }

    // ==================== INNER CLASSES ====================

    private class CellPane {
        int r, c, type;
        boolean hasBulb, marked, lit, conflict;

        StackPane stack = new StackPane();
        Rectangle bg;
        Circle bulb;
        Text num;

        CellPane(int r, int c, int type, int size) {
            this.r = r;
            this.c = c;
            this.type = type;

            bg = new Rectangle(size, size);
            bg.setStroke(Color.BLACK);
            bg.setFill(getInitialColor());

            int bulbRadius = Math.max(8, size / 4);
            bulb = new Circle(bulbRadius);
            bulb.setFill(Color.GOLD);
            bulb.setVisible(false);
            bulb.setStroke(Color.BLACK);
            bulb.setStrokeWidth(1);

            stack.getChildren().addAll(bg, bulb);

            if (type > 0) {
                num = new Text("" + type);
                int fontSize = Math.max(12, size / 4);
                num.setFont(Font.font(fontSize));
                num.setFill(Color.WHITE);
                num.setStroke(Color.BLACK);
                num.setStrokeWidth(0.5);
                stack.getChildren().add(num);
            }

            stack.setOnMouseClicked(e -> {
                if (!isUserTurn) {
                    showAlert("Not Your Turn", "Please wait for CPU's turn to complete.");
                    return;
                }

                if (type != 0)
                    return;

                if (e.getButton() == MouseButton.PRIMARY && !marked) {
                    hasBulb = !hasBulb;
                    bulb.setVisible(hasBulb);

                    if (hasBulb) {
                        updateUserScore(+1);
                        userPlacedBulb = true;
                        lastUserMove = new Point(r, c);
                        trackUserMove(true);

                        updateAll();
                        if (isMoveInvalid(r, c)) {
                            hasBulb = false;
                            bulb.setVisible(false);
                            updateUserScore(-2);
                            userPlacedBulb = false;
                            lastUserMove = null;

                            Platform.runLater(() -> {
                                showAlert("Invalid Move!",
                                        "⚠️ This bulb creates a conflict or violation!\n" +
                                                "It has been automatically removed.\n" +
                                                "Score penalty: -2 points");
                            });
                        }
                    } else {
                        if (lastUserMove != null && lastUserMove.r == r && lastUserMove.c == c) {
                            lastUserMove = null;
                        }
                        trackUserMove(false);
                    }
                }

                if (e.getButton() == MouseButton.SECONDARY && !lit) {
                    marked = !marked;
                }

                updateAll();
            });
        }

        private Color getInitialColor() {
            if (type == -1)
                return Color.BLACK;
            if (type > 0)
                return Color.DARKGRAY;
            return Color.WHITE;
        }

        boolean isSatisfied() {
            int cnt = 0;
            int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
            for (int[] x : d) {
                int rr = r + x[0], cc = c + x[1];
                if (in(rr, cc) && cells[rr][cc] != null && cells[rr][cc].hasBulb)
                    cnt++;
            }
            return cnt == type;
        }

        boolean isOverflow() {
            int cnt = 0;
            int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
            for (int[] x : d) {
                int rr = r + x[0], cc = c + x[1];
                if (in(rr, cc) && cells[rr][cc] != null && cells[rr][cc].hasBulb)
                    cnt++;
            }
            return cnt > type;
        }

        void toggleBulb(boolean v) {
            hasBulb = v;
            bulb.setVisible(v);
        }

        void refresh() {
            if (type == -1) {
                bg.setFill(Color.BLACK);
                return;
            }

            if (type > 0) {
                if (isOverflow()) {
                    bg.setFill(Color.DARKRED);
                } else if (isSatisfied()) {
                    bg.setFill(Color.DARKGREEN);
                } else {
                    bg.setFill(Color.DARKGRAY);
                }
                return;
            }

            if (hasBulb) {
                bulb.setFill((conflict || causesNumberOverflow()) ? Color.RED : Color.GOLD);
                bg.setFill(Color.web("#FFF59D"));
                return;
            }

            bg.setFill(lit ? Color.web("#FFFDE7") : Color.WHITE);

            if (marked) {
                bg.setFill(Color.web("#E1F5FE"));
            }
        }

        boolean causesNumberOverflow() {
            int[][] d = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
            for (int[] x : d) {
                int rr = r + x[0], cc = c + x[1];
                if (in(rr, cc) && cells[rr][cc] != null && puzzle[rr][cc] > 0 && cells[rr][cc].isOverflow())
                    return true;
            }
            return false;
        }
    }

    private static class Point {
        int r, c;

        Point(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point))
                return false;
            Point p = (Point) o;
            return r == p.r && c == p.c;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, c);
        }
    }

    private static class ScoredMove {
        int r, c, score;

        ScoredMove(int r, int c, int score) {
            this.r = r;
            this.c = c;
            this.score = score;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
