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
        // 文字色を白に設定
        statusbar.setForeground(Color.WHITE);
        statusbar.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(statusbar, BorderLayout.SOUTH);

        SidePanel sidePanel = new SidePanel();
        Board board = new Board(this, sidePanel);
        
        add(board, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        board.start();

        setTitle("Tetris with HOLD");
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

// 右側に表示するパネル（NEXT と HOLD を表示）
class SidePanel extends JPanel {
    private final int SQUARE_SIZE = 30;   
    private Shape nextPiece;
    private Shape holdPiece;

    // アイテムの個数管理用（初期値3個として表示してみます）
    private int bombCount = 3;

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

    // 後でロジックを実装する時に使うためのメソッド
    public void setBombCount(int count) {
        this.bombCount = count;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // --- NEXT 表示 ---
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("NEXT", 70, 60);

        if (nextPiece.getShape() != Tetrominoes.NoShape) {
            drawPiece(g, nextPiece, 80, 150);
        }

        // --- HOLD 表示 ---
        g.setColor(Color.WHITE);
        g.drawString("HOLD", 70, 300); // 少し下に表示

        if (holdPiece.getShape() != Tetrominoes.NoShape) {
            drawPiece(g, holdPiece, 80, 390);
        }

        // テキスト表示
        g.setColor(Color.WHITE);
        g.drawString("ITEM", 70, 540);
        
        // アイコン（爆弾の見た目）を描画
        int iconX = 60;
        int iconY = 570;
        g.setColor(Color.RED);
        g.fillOval(iconX, iconY, SQUARE_SIZE, SQUARE_SIZE); // 赤い丸
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("B", iconX + 10, iconY + 20); // 中に"B"

        // 個数表示
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20)); // フォントサイズを戻す
        g.drawString("x " + bombCount, iconX + 45, iconY + 22);
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
    
    // 【変更点】 速度を 300 -> 400 に変更
    private final int PERIOD_INTERVAL = 400; 

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    // ホールド機能用のフラグ（1ターンに1回のみ）
    private boolean canHold = true; 
    
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private JLabel statusbar;
    private Shape curPiece;
    private Shape nextPiece; 
    private Shape holdPiece; // ホールド中のピース
    private Tetrominoes[] board;
    private SidePanel sidePanel; 

    public Board(Tetris parent, SidePanel sidePanel) {
        setFocusable(true);
        setBackground(Color.BLACK);
        this.sidePanel = sidePanel;
        
        curPiece = new Shape();
        nextPiece = new Shape(); 
        holdPiece = new Shape(); // 初期化
        
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
        canHold = true; // 初期化
        clearBoard();
        
        // HOLD枠のリセット
        holdPiece.setShape(Tetrominoes.NoShape);
        sidePanel.updateHoldPiece(holdPiece);

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

    // --- ホールド機能の実装 ---
    private void hold() {
        if (!isStarted || isPaused || !canHold) return;

        // 現在のピース形状を一時保存
        Tetrominoes currentShape = curPiece.getShape();
        
        if (holdPiece.getShape() == Tetrominoes.NoShape) {
            // ホールド枠が空の場合：現在をホールドに入れ、次はNextから持ってくる
            holdPiece.setShape(currentShape);
            newPiece();
        } else {
            // ホールド枠がある場合：現在とホールドを入れ替える
            Tetrominoes heldShape = holdPiece.getShape();
            holdPiece.setShape(currentShape);
            curPiece.setShape(heldShape);
            
            // 位置をリセット（上部中央へ）
            curX = BOARD_WIDTH / 2 + 1;
            curY = BOARD_HEIGHT - 1 + curPiece.minY();
            
            // リセット後の位置で衝突判定（ゲームオーバー判定にはしないが、念のため）
            // ※本来はスーパーローテーション等の処理が入るが今回はシンプルに位置リセットのみ
        }

        sidePanel.updateHoldPiece(holdPiece);
        canHold = false; // このターンはもうホールドできない
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

        // グリッド線
        g.setColor(new Color(50, 50, 50));
        for (int i = 0; i <= BOARD_WIDTH; ++i) {
            int x = i * squareWidth();
            g.drawLine(x, boardTop, x, boardBottom);
        }
        for (int i = 0; i <= BOARD_HEIGHT; ++i) {
            int y = boardTop + i * squareHeight();
            g.drawLine(0, y, boardRight, y);
        }

        // 盤面の描画
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
        
        // ロックアウトライン
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

        curX = BOARD_WIDTH / 2;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        
        // 新しいピースが出現したとき、ホールド権限を復活
        canHold = true;

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
                // 【追加】 Cキーでホールド
                case KeyEvent.VK_C: hold(); break; 
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