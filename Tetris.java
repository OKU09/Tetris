import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Tetris extends JFrame {

    JLabel statusbar;

    public Tetris() {
        initUI();
    }

    private void initUI() {
        statusbar = new JLabel(" 0");
        statusbar.setForeground(Color.WHITE);
        statusbar.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(statusbar, BorderLayout.SOUTH);

        SidePanel sidePanel = new SidePanel();
        Board board = new Board(this, sidePanel);
        
        add(board, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        board.start();

        setTitle("Tetris with Grid & Black Theme");
        setSize(600, 850); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // フレーム自体の背景も黒に設定（念のため）
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

    public SidePanel() {
        setPreferredSize(new Dimension(200, 800));
        // 背景色を黒に変更
        setBackground(Color.BLACK);
        nextPiece = new Shape();
        nextPiece.setShape(Tetrominoes.NoShape);
    }

    public void updateNextPiece(Shape piece) {
        this.nextPiece = piece;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        // 文字位置を少し調整
        g.drawString("NEXT", 70, 60);

        if (nextPiece.getShape() == Tetrominoes.NoShape) return;

        int offsetX = 80;
        // 【修正点】描画基準位置を下げて重なりを回避 (100 -> 150)
        int offsetY = 150; 

        for (int i = 0; i < 4; ++i) {
            int x = nextPiece.x(i);
            int y = nextPiece.y(i);
            drawSquare(g, offsetX + x * SQUARE_SIZE, 
                          offsetY - y * SQUARE_SIZE, 
                          nextPiece.getShape());
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
            new Color(102, 204, 102), new Color(102, 102, 204),
            new Color(204, 204, 102), new Color(204, 102, 204),
            new Color(102, 204, 204), new Color(218, 170, 0)
        };
        Color color = colors[shape.ordinal()];

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
}

class Board extends JPanel implements ActionListener {

    private final int VISIBLE_HEIGHT = 20; 
    private final int HIDDEN_HEIGHT = 2;   
    private final int BOARD_HEIGHT = VISIBLE_HEIGHT + HIDDEN_HEIGHT; 
    private final int BOARD_WIDTH = 10;
    
    private final int PERIOD_INTERVAL = 300; 

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private JLabel statusbar;
    private Shape curPiece;
    private Shape nextPiece; 
    private Tetrominoes[] board;
    private SidePanel sidePanel; 

    public Board(Tetris parent, SidePanel sidePanel) {
        setFocusable(true);
        // 背景色を黒に変更
        setBackground(Color.BLACK);
        this.sidePanel = sidePanel;
        
        curPiece = new Shape();
        nextPiece = new Shape(); 
        
        timer = new Timer(PERIOD_INTERVAL, this);
        timer.start();

        statusbar = parent.getStatusBar();
        board = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        addKeyListener(new TAdapter());
        clearBoard();
    }

    public void start() {
        if (isPaused) return;

        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        clearBoard();

        nextPiece.setRandomShape();
        sidePanel.updateNextPiece(nextPiece);
        
        newPiece();
        timer.start();
    }

    private void pause() {
        if (!isStarted) return;

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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
        int boardBottom = boardTop + BOARD_HEIGHT * squareHeight();
        int boardRight = BOARD_WIDTH * squareWidth();

        // --- グリッド線の描画 ---
        // 黒背景に合わせて、暗いグレーに変更
        g.setColor(new Color(50, 50, 50));

        // 縦線
        for (int i = 0; i <= BOARD_WIDTH; ++i) {
            int x = i * squareWidth();
            g.drawLine(x, boardTop, x, boardBottom);
        }
        // 横線
        for (int i = 0; i <= BOARD_HEIGHT; ++i) {
            int y = boardTop + i * squareHeight();
            g.drawLine(0, y, boardRight, y);
        }
        // ---------------------------

        // 盤面のブロック描画
        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetrominoes.NoShape)
                    drawSquare(g, 0 + j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
            }
        }

        // 落下中ブロックの描画
        if (curPiece.getShape() != Tetrominoes.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape());
            }
        }
        
        // ロックアウト境界線（赤線）
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
        for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        boolean entirelyOffScreen = true;
        for (int i = 0; i < 4; ++i) {
            int y = curY - curPiece.y(i);
            if (y < VISIBLE_HEIGHT) {
                entirelyOffScreen = false;
                break;
            }
        }

        if (entirelyOffScreen) {
            gameOver("Game Over (Lock Out)");
            return;
        }

        removeFullLines();

        if (!isFallingFinished)
            newPiece();
    }

    private void newPiece() {
        curPiece.setShape(nextPiece.getShape());
        nextPiece.setRandomShape();
        sidePanel.updateNextPiece(nextPiece);

        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetrominoes.NoShape);
            gameOver("Game Over (Block Out)");
        }
    }
    
    private void gameOver(String message) {
        timer.stop();
        isStarted = false;
        statusbar.setText(" " + message);
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

    private void removeFullLines() {
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
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            statusbar.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            curPiece.setShape(Tetrominoes.NoShape);
            repaint();
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
            new Color(102, 204, 102), new Color(102, 102, 204),
            new Color(204, 204, 102), new Color(204, 102, 204),
            new Color(102, 204, 204), new Color(218, 170, 0)
        };

        Color color = colors[shape.ordinal()];

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
            if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

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
            }
        }
    }
}

enum Tetrominoes {
    NoShape, ZShape, SShape, LineShape,
    TShape, SquareShape, LShape, MirroredLShape
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
            {{1, -1}, {0, -1}, {0, 0}, {0, 1}}
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
        if (pieceShape == Tetrominoes.SquareShape) return this;

        Shape result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.setX(i, y(i));
            result.setY(i, -x(i));
        }
        return result;
    }

    public Shape rotateRight() {
        if (pieceShape == Tetrominoes.SquareShape) return this;

        Shape result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }
        return result;
    }
}