import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane; // 入力ダイアログ用
import javax.swing.JPanel;
import javax.swing.Timer;

public class Tetris extends JFrame {

    JLabel statusbar;

    public Tetris() {
        initUI();
    }

    private void initUI() {
        statusbar = new JLabel(" Press S to Start");
        statusbar.setForeground(Color.WHITE);
        statusbar.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(statusbar, BorderLayout.SOUTH);

        SidePanel sidePanel = new SidePanel();
        Board board = new Board(this, sidePanel);
        
        add(board, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        setTitle("Tetris with DB Ranking");
        setSize(600, 850); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.BLACK);
    }

    public JLabel getStatusBar() {
        return statusbar;
    }

    public static void main(String[] args) {
        Tetris game = new Tetris();
        game.setVisible(true);
    }
}

class SidePanel extends JPanel {
    private final int SQUARE_SIZE = 30;   
    private Shape nextPiece;
    private Shape holdPiece;
    
    private int bombCount = 3; 
    private int drillCount = 3; 

    public SidePanel() {
        setPreferredSize(new Dimension(200, 800));
        setBackground(Color.BLACK);
        nextPiece = new Shape();
        nextPiece.setShape(Tetrominoes.NoShape);
        holdPiece = new Shape();
        holdPiece.setShape(Tetrominoes.NoShape);
    }

    public void updateNextPiece(Shape piece) {
        this.nextPiece = piece;
        repaint();
    }

    public void updateHoldPiece(Shape piece) {
        this.holdPiece = piece;
        repaint();
    }
    
    public void setItemCounts(int bombs, int drills) {
        this.bombCount = bombs;
        this.drillCount = drills;
        repaint();
    }
    
    public void reset() {
        nextPiece.setShape(Tetrominoes.NoShape);
        holdPiece.setShape(Tetrominoes.NoShape);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("NEXT", 70, 60);

        if (nextPiece.getShape() != Tetrominoes.NoShape) {
            drawPiece(g, nextPiece, 80, 150);
        }

        g.setColor(Color.WHITE);
        g.drawString("HOLD", 70, 300);

        if (holdPiece.getShape() != Tetrominoes.NoShape) {
            drawPiece(g, holdPiece, 80, 390);
        }
        
        g.setColor(Color.WHITE);
        g.drawString("ITEMS", 70, 540);
        
        int iconX = 40;
        int iconY = 570;
        g.setColor(Color.RED);
        g.fillOval(iconX, iconY, SQUARE_SIZE, SQUARE_SIZE);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("B", iconX + 10, iconY + 20);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("x" + bombCount + " (B)", iconX + 40, iconY + 20);

        int drillY = iconY + 50;
        g.setColor(Color.CYAN); 
        g.fillRect(iconX, drillY, SQUARE_SIZE, SQUARE_SIZE); 
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("D", iconX + 10, drillY + 20);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("x" + drillCount + " (V)", iconX + 40, drillY + 20);
    }

    private void drawPiece(Graphics g, Shape piece, int offsetX, int offsetY) {
        for (int i = 0; i < 4; ++i) {
            int x = piece.x(i);
            int y = piece.y(i);
            drawSquare(g, offsetX + x * SQUARE_SIZE, 
                          offsetY + y * SQUARE_SIZE, 
                          piece.getShape());
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
            new Color(102, 204, 102), new Color(102, 102, 204),
            new Color(204, 204, 102), new Color(204, 102, 204),
            new Color(102, 204, 204), new Color(218, 170, 0),
            new Color(255, 50, 50), 
            new Color(0, 255, 255)  
        };
        
        Color color = colors[shape.ordinal()];
        
        if (shape == Tetrominoes.BombShape) {
            g.setColor(Color.RED);
            g.fillOval(x + 2, y + 2, SQUARE_SIZE - 4, SQUARE_SIZE - 4);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("B", x + 8, y + 20);
            return;
        }
        
        if (shape == Tetrominoes.DrillShape) {
            g.setColor(Color.CYAN);
            g.fillRect(x + 2, y + 2, SQUARE_SIZE - 4, SQUARE_SIZE - 4);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("D", x + 8, y + 20);
            return;
        }

        g.setColor(color);
        g.fillRect(x + 1, y + 1, SQUARE_SIZE - 2, SQUARE_SIZE - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + SQUARE_SIZE - 1, x, y);
        g.drawLine(x, y, x + SQUARE_SIZE - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + SQUARE_SIZE - 1,
                x + SQUARE_SIZE - 1, y + SQUARE_SIZE - 1);
        g.drawLine(x + SQUARE_SIZE - 1, y + SQUARE_SIZE - 1,
                x + SQUARE_SIZE - 1, y + 1);
    }
    
    private int squareWidth() { return SQUARE_SIZE; }
    private int squareHeight() { return SQUARE_SIZE; }
}

class Board extends JPanel implements ActionListener {

    private final int VISIBLE_HEIGHT = 20; 
    private final int HIDDEN_HEIGHT = 2;   
    private final int BOARD_HEIGHT = VISIBLE_HEIGHT + HIDDEN_HEIGHT; 
    private final int BOARD_WIDTH = 10;
    
    private final int PERIOD_INTERVAL = 600; 

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean canHold = true; 
    
    private boolean isMenu = true; 
    private boolean isResult = false; 
    
    private boolean isAnimating = false; 
    private List<Integer> animRows = new ArrayList<>();   
    private List<Integer> animCols = new ArrayList<>();   
    private Color animColor = Color.WHITE; 
    
    private int bombCount = 3; 
    private int drillCount = 3; 

    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private JLabel statusbar;
    private Shape curPiece;
    private Shape nextPiece; 
    private Shape holdPiece; 
    private Tetrominoes[] board;
    private SidePanel sidePanel; 
    
    // DB用: ランキングデータ保持用
    private List<String> rankingData = new ArrayList<>();

    public Board(Tetris parent, SidePanel sidePanel) {
        setFocusable(true);
        setBackground(Color.BLACK);
        this.sidePanel = sidePanel;
        
        curPiece = new Shape();
        nextPiece = new Shape(); 
        holdPiece = new Shape(); 
        
        timer = new Timer(PERIOD_INTERVAL, this);
        
        statusbar = parent.getStatusBar();
        board = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        addKeyListener(new TAdapter());
        clearBoard();
        
        sidePanel.reset();
    }

    public void start() {
        isMenu = false;
        isResult = false;
        isStarted = true;
        isFallingFinished = false;
        isPaused = false;
        isAnimating = false;
        numLinesRemoved = 0;
        canHold = true;
        
        bombCount = 3;
        drillCount = 3; 
        sidePanel.setItemCounts(bombCount, drillCount);

        clearBoard();
        
        holdPiece.setShape(Tetrominoes.NoShape);
        sidePanel.updateHoldPiece(holdPiece);

        nextPiece.setRandomShape();
        sidePanel.updateNextPiece(nextPiece);
        
        newPiece();
        timer.start();
        statusbar.setText("0");
    }

    private void goToMenu() {
        isResult = false;
        isMenu = true;
        sidePanel.reset();
        statusbar.setText(" Press S to Start");
        repaint();
    }

    private void pause() {
        if (!isStarted || isMenu || isResult) return; 

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            statusbar.setText(" paused");
        } else {
            timer.start();
            statusbar.setText(String.valueOf(numLinesRemoved));
        }
        repaint();
    }

    private void hold() {
        if (!isStarted || isPaused || !canHold || isAnimating || isMenu || isResult) return;

        Tetrominoes currentShape = curPiece.getShape();
        if (currentShape == Tetrominoes.BombShape || currentShape == Tetrominoes.DrillShape) return;
        
        if (holdPiece.getShape() == Tetrominoes.NoShape) {
            holdPiece.setShape(currentShape);
            newPiece();
        } else {
            Tetrominoes heldShape = holdPiece.getShape();
            holdPiece.setShape(currentShape);
            curPiece.setShape(heldShape);
            curX = BOARD_WIDTH / 2;
            curY = BOARD_HEIGHT - 1 + curPiece.minY();
        }

        sidePanel.updateHoldPiece(holdPiece);
        canHold = false; 
        repaint();
    }
    
    private void useBomb() {
        if (!isStarted || isPaused || bombCount <= 0 || isAnimating || isMenu || isResult) return;
        if (curPiece.getShape() == Tetrominoes.BombShape || curPiece.getShape() == Tetrominoes.DrillShape) return;
        
        bombCount--;
        sidePanel.setItemCounts(bombCount, drillCount);
        curPiece.setShape(Tetrominoes.BombShape);
        repaint();
    }
    
    private void useDrill() {
        if (!isStarted || isPaused || drillCount <= 0 || isAnimating || isMenu || isResult) return;
        if (curPiece.getShape() == Tetrominoes.BombShape || curPiece.getShape() == Tetrominoes.DrillShape) return;
        
        drillCount--;
        sidePanel.setItemCounts(bombCount, drillCount);
        curPiece.setShape(Tetrominoes.DrillShape);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (isMenu) {
            drawMenu(g);
        } else if (isResult) {
            drawResult(g);
        } else {
            doDrawing(g);
        }
    }
    
    private void drawMenu(Graphics g) {
        Dimension size = getSize();
        int w = size.width;
        int h = size.height;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        g.setColor(Color.ORANGE);
        drawCenteredString(g, "TETRIS", w, h / 4);

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Press 'S' to Start", w, h / 2 - 50);
        
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.LIGHT_GRAY);
        int helpY = h / 2 + 30;
        int step = 25;
        drawCenteredString(g, "[ Controls ]", w, helpY);
        drawCenteredString(g, "Arrow Keys : Move / Rotate", w, helpY + step);
        drawCenteredString(g, "Space : Drop", w, helpY + step * 2);
        drawCenteredString(g, "C : Hold", w, helpY + step * 3);
        drawCenteredString(g, "B : Bomb Item", w, helpY + step * 4);
        drawCenteredString(g, "V : Drill Item", w, helpY + step * 5); 
        drawCenteredString(g, "P : Pause", w, helpY + step * 6);
    }
    
    // ★ ランキング表示の描画 ★
    private void drawResult(Graphics g) {
        Dimension size = getSize();
        int w = size.width;
        int h = size.height;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        g.setColor(Color.RED);
        drawCenteredString(g, "GAME OVER", w, h / 6);

        g.setFont(new Font("SansSerif", Font.BOLD, 25));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Your Score: " + numLinesRemoved, w, h / 4);
        
        // --- ランキング表示エリア ---
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        drawCenteredString(g, "=== TOP 5 RANKING ===", w, h / 4 + 50);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        int rankY = h / 4 + 90;
        
        if (rankingData.isEmpty()) {
             drawCenteredString(g, "No Data / DB Error", w, rankY);
        } else {
            for (String line : rankingData) {
                drawCenteredString(g, line, w, rankY);
                rankY += 30;
            }
        }
        // -------------------------

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(Color.LIGHT_GRAY);
        drawCenteredString(g, "Press 'S' to Restart", w, h - 100);
        drawCenteredString(g, "Press 'M' to Menu", w, h - 70);
    }
    
    private void drawCenteredString(Graphics g, String text, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void doDrawing(Graphics g) {
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
        int boardBottom = boardTop + BOARD_HEIGHT * squareHeight();
        int boardRight = BOARD_WIDTH * squareWidth();

        g.setColor(new Color(50, 50, 50));
        for (int i = 0; i <= BOARD_WIDTH; ++i) {
            int x = i * squareWidth();
            g.drawLine(x, boardTop, x, boardBottom);
        }
        for (int i = 0; i <= BOARD_HEIGHT; ++i) {
            int y = boardTop + i * squareHeight();
            g.drawLine(0, y, boardRight, y);
        }

        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetrominoes.NoShape)
                    drawSquare(g, 0 + j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
            }
        }

        if (curPiece.getShape() != Tetrominoes.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape());
            }
        }
        
        if (isAnimating) {
            g.setColor(animColor);
            for (int y : animRows) {
                int drawY = boardTop + (BOARD_HEIGHT - y - 1) * squareHeight();
                g.fillRect(0, drawY, boardRight, squareHeight());
            }
            for (int x : animCols) {
                int drawX = x * squareWidth();
                g.fillRect(drawX, boardTop, squareWidth(), boardBottom - boardTop);
            }
        }
        
        int lineY = boardTop + (BOARD_HEIGHT - VISIBLE_HEIGHT) * squareHeight();
        g.setColor(Color.RED);
        g.fillRect(0, lineY - 1, boardRight, 3); 
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1))
                break;
            --newY;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1))
            pieceDropped();
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; ++i)
            board[i] = Tetrominoes.NoShape;
    }

    private void pieceDropped() {
        boolean isBomb = (curPiece.getShape() == Tetrominoes.BombShape);
        boolean isDrill = (curPiece.getShape() == Tetrominoes.DrillShape);

        for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }
        
        if (isBomb) {
            explodeBomb(curX, curY); 
            return;
        }
        if (isDrill) {
            explodeDrill(curX);
            return;
        }

        checkAndRemoveLines(); 
    }
    
    private void startAnimation(Color color, ActionListener onFinish) {
        if (animRows.isEmpty() && animCols.isEmpty()) {
            onFinish.actionPerformed(null);
            return;
        }

        isAnimating = true;
        animColor = color;
        timer.stop();
        repaint();

        Timer animTimer = new Timer(200, e -> {
            isAnimating = false;
            ((Timer)e.getSource()).stop();
            onFinish.actionPerformed(null); 
            animRows.clear();
            animCols.clear();
            if (isStarted) {
                if (!isFallingFinished) newPiece();
                timer.start();
            }
            repaint();
        });
        animTimer.setRepeats(false);
        animTimer.start();
    }

    private void explodeBomb(int centerX, int centerY) {
        animRows.clear();
        for (int y = centerY - 1; y <= centerY + 1; y++) {
            if (y >= 0 && y < BOARD_HEIGHT) {
                animRows.add(y);
            }
        }

        startAnimation(new Color(255, 165, 0, 200), e -> {
            for (int y : animRows) {
                for (int x = 0; x < BOARD_WIDTH; x++) {
                    board[(y * BOARD_WIDTH) + x] = Tetrominoes.NoShape;
                }
            }
            checkGameOver();
        });
    }

    private void explodeDrill(int centerX) {
        animCols.clear();
        if (centerX >= 0 && centerX < BOARD_WIDTH) {
            animCols.add(centerX);
        }

        startAnimation(new Color(0, 255, 255, 200), e -> {
            for (int x : animCols) {
                for (int y = 0; y < BOARD_HEIGHT; y++) {
                    board[(y * BOARD_WIDTH) + x] = Tetrominoes.NoShape;
                }
            }
            checkGameOver();
        });
    }

    private void checkAndRemoveLines() {
        List<Integer> fullLines = new ArrayList<>();

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                fullLines.add(i);
            }
        }

        if (fullLines.isEmpty()) {
            checkGameOver();
            if (!isFallingFinished) newPiece();
            return;
        }
        
        animRows.clear();
        animRows.addAll(fullLines);

        startAnimation(new Color(255, 255, 255, 180), e -> {
            doRemoveLinesLogic();
            checkGameOver();
        });
    }

    private void doRemoveLinesLogic() {
        int numFullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j)
                         board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                }
                for (int j = 0; j < BOARD_WIDTH; ++j) {
                     board[((BOARD_HEIGHT - 1) * BOARD_WIDTH) + j] = Tetrominoes.NoShape;
                }
                i++; 
            }
        }
        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            statusbar.setText(String.valueOf(numLinesRemoved));
        }
    }
    
    private void checkGameOver() {
        // newPieceで判定
    }

    private void newPiece() {
        curPiece.setShape(nextPiece.getShape());
        
        nextPiece.setRandomShape();
        sidePanel.updateNextPiece(nextPiece);

        curX = BOARD_WIDTH / 2;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        canHold = true;

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetrominoes.NoShape);
            gameOver("Game Over (Block Out)");
        }
    }
    
    // ★ ゲームオーバー時の処理（DB連携） ★
    private void gameOver(String message) {
        timer.stop();
        isStarted = false;
        isResult = true; 
        
        // 1. 名前入力ダイアログを表示
        String name = JOptionPane.showInputDialog(this, "Game Over!\nEnter Your Name:", "Rank Entry", JOptionPane.QUESTION_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            // 2. DBに保存
            ScoreDAO dao = new ScoreDAO();
            dao.saveScore(name, numLinesRemoved);
            
            // 3. ランキングを取得してメモリに保存
            rankingData = dao.getTopRanking();
        } else {
            rankingData.clear();
            rankingData.add("No name entered.");
        }

        statusbar.setText(""); 
        repaint();
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;
            if (shapeAt(x, y) != Tetrominoes.NoShape)
                return false;
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
            new Color(102, 204, 102), new Color(102, 102, 204),
            new Color(204, 204, 102), new Color(204, 102, 204),
            new Color(102, 204, 204), new Color(218, 170, 0),
            new Color(255, 50, 50), 
            new Color(0, 255, 255)  
        };

        Color color = colors[shape.ordinal()];
        
        if (shape == Tetrominoes.BombShape) {
            g.setColor(Color.RED);
            g.fillOval(x + 2, y + 2, squareWidth() - 4, squareHeight() - 4);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("B", x + 8, y + 20);
            return;
        }
        
        if (shape == Tetrominoes.DrillShape) {
            g.setColor(Color.CYAN);
            g.fillRect(x + 2, y + 2, squareWidth() - 4, squareHeight() - 4);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("D", x + 8, y + 20);
            return;
        }

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMenu || isResult) return; 

        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    private int squareWidth() { return (int) getSize().getWidth() / BOARD_WIDTH; }
    private int squareHeight() { return (int) getSize().getHeight() / BOARD_HEIGHT; }
    private Tetrominoes shapeAt(int x, int y) { return board[(y * BOARD_WIDTH) + x]; }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int keycode = e.getKeyCode();

            if (isMenu) {
                if (keycode == 's' || keycode == 'S') {
                    start();
                }
                return;
            }

            if (isResult) {
                if (keycode == 's' || keycode == 'S') {
                    start();
                } else if (keycode == 'm' || keycode == 'M') {
                    goToMenu();
                }
                return;
            }

            if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape || isAnimating) { 
                return;
            }

            if (keycode == 'p' || keycode == 'P') {
                pause();
                return;
            }

            if (isPaused) return;

            switch (keycode) {
                case KeyEvent.VK_LEFT: tryMove(curPiece, curX - 1, curY); break;
                case KeyEvent.VK_RIGHT: tryMove(curPiece, curX + 1, curY); break;
                case KeyEvent.VK_DOWN: tryMove(curPiece.rotateRight(), curX, curY); break;
                case KeyEvent.VK_UP: tryMove(curPiece.rotateLeft(), curX, curY); break;
                case KeyEvent.VK_SPACE: dropDown(); break;
                case KeyEvent.VK_D: oneLineDown(); break;
                case KeyEvent.VK_C: hold(); break; 
                case KeyEvent.VK_B: useBomb(); break;
                case KeyEvent.VK_V: useDrill(); break; 
            }
        }
    }
}

enum Tetrominoes {
    NoShape, ZShape, SShape, LineShape,
    TShape, SquareShape, LShape, MirroredLShape,
    BombShape, DrillShape 
}

class Shape {
    private Tetrominoes pieceShape;
    private int coords[][];
    private int[][][] coordsTable;

    public Shape() {
        initShape();
    }

    private void initShape() {
        coords = new int[4][2];
        setShape(Tetrominoes.NoShape);
    }

    protected void setShape(Tetrominoes shape) {
        coordsTable = new int[][][]{
            {{0, 0}, {0, 0}, {0, 0}, {0, 0}}, 
            {{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, 
            {{0, -1}, {0, 0}, {1, 0}, {1, 1}}, 
            {{0, -1}, {0, 0}, {0, 1}, {0, 2}}, 
            {{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, 
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}}, 
            {{-1, -1}, {0, -1}, {0, 0}, {0, 1}}, 
            {{1, -1}, {0, -1}, {0, 0}, {0, 1}}, 
            {{0, 0}, {0, 0}, {0, 0}, {0, 0}}, 
            {{0, 0}, {0, 0}, {0, 0}, {0, 0}} 
        };

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; ++j) {
                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }
        pieceShape = shape;
    }

    private void setX(int index, int x) { coords[index][0] = x; }
    private void setY(int index, int y) { coords[index][1] = y; }
    public int x(int index) { return coords[index][0]; }
    public int y(int index) { return coords[index][1]; }
    public Tetrominoes getShape() { return pieceShape; }

    public void setRandomShape() {
        var r = new java.util.Random();
        int x = Math.abs(r.nextInt()) % 7 + 1; 
        Tetrominoes[] values = Tetrominoes.values();
        setShape(values[x]);
    }

    public int minY() {
        int m = coords[0][1];
        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }
        return m;
    }

    public Shape rotateLeft() {
        if (pieceShape == Tetrominoes.SquareShape || pieceShape == Tetrominoes.BombShape || pieceShape == Tetrominoes.DrillShape) return this;

        Shape result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; ++i) {
            result.setX(i, y(i));
            result.setY(i, -x(i));
        }
        return result;
    }

    public Shape rotateRight() {
        if (pieceShape == Tetrominoes.SquareShape || pieceShape == Tetrominoes.BombShape || pieceShape == Tetrominoes.DrillShape) return this;

        Shape result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; ++i) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }
        return result;
    }
}