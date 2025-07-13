
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class SnakeGameApp extends JFrame {

    public SnakeGameApp() {
        setTitle("Snake Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGameApp());
    }
}

class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    boolean paused = false;
    Timer timer;
    Random random;

    enum State {
        MENU, PLAYING, OPTIONS, HIGH_SCORE, GAME_OVER, PAUSED
    }
    State gameState = State.MENU;

    int delay = 100;  // speed delay
    int highScore = 0;

    // Menu UI elements
    String[] menuItems = {"Play Game", "Options", "High Score", "Quit"};
    int menuSelection = 0;

    String[] optionItems = {"Speed: ", "Back"};
    int optionSelection = 0;

    Font menuFont = new Font("Arial", Font.BOLD, 42);
    Font optionFont = new Font("Arial", Font.PLAIN, 28);
    Font infoFont = new Font("Arial", Font.PLAIN, 20);

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        this.addMouseListener(new MyMouseAdapter());
        random = new Random();
    }

    private void startGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 100 - i * UNIT_SIZE;
            y[i] = 100;
        }
        spawnApple();
        running = true;
        paused = false;
        timer = new Timer(delay, this);
        timer.start();
        gameState = State.PLAYING;
    }

    private void spawnApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void actionPerformed(ActionEvent e) {
        if (running && !paused && gameState == State.PLAYING) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' ->
                y[0] -= UNIT_SIZE;
            case 'D' ->
                y[0] += UNIT_SIZE;
            case 'L' ->
                x[0] -= UNIT_SIZE;
            case 'R' ->
                x[0] += UNIT_SIZE;
        }
    }

    private void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            spawnApple();
        }
    }

    private void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                gameOver();
            }
        }
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            gameOver();
        }
    }

    private void gameOver() {
        running = false;
        timer.stop();
        gameState = State.GAME_OVER;
        if (applesEaten > highScore) {
            highScore = applesEaten;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (gameState) {
            case MENU ->
                drawMenu(g);
            case PLAYING ->
                drawGame(g);
            case OPTIONS ->
                drawOptions(g);
            case HIGH_SCORE ->
                drawHighScore(g);
            case GAME_OVER ->
                drawGameOver(g);
            case PAUSED ->
                drawPaused(g);
        }
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.green);
        g.setFont(menuFont);
        drawCenteredString(g, "SNAKE GAME", SCREEN_WIDTH, SCREEN_HEIGHT / 5);

        g.setFont(optionFont);
        for (int i = 0; i < menuItems.length; i++) {
            if (i == menuSelection) {
                g.setColor(Color.yellow);
            } else {
                g.setColor(Color.white);
            }
            drawCenteredString(g, menuItems[i], SCREEN_WIDTH, SCREEN_HEIGHT / 2 + i * 50);
        }

        g.setFont(infoFont);
        drawCenteredString(g, "Use UP/DOWN arrows and ENTER to select", SCREEN_WIDTH, SCREEN_HEIGHT - 50);
        drawCenteredString(g, "Or click on the options below", SCREEN_WIDTH, SCREEN_HEIGHT - 25);
    }

    private void drawOptions(Graphics g) {
        g.setColor(Color.cyan);
        g.setFont(menuFont);
        drawCenteredString(g, "OPTIONS", SCREEN_WIDTH, SCREEN_HEIGHT / 5);

        g.setFont(optionFont);
        for (int i = 0; i < optionItems.length; i++) {
            if (i == optionSelection) {
                g.setColor(Color.yellow); 
            }else {
                g.setColor(Color.white);
            }

            String text = optionItems[i];
            if (i == 0) {
                text += delay + " ms (Lower=Faster)";
            }
            drawCenteredString(g, text, SCREEN_WIDTH, SCREEN_HEIGHT / 2 + i * 50);
        }

        g.setFont(infoFont);
        drawCenteredString(g, "Use UP/DOWN to change speed when selected", SCREEN_WIDTH, SCREEN_HEIGHT - 50);
        drawCenteredString(g, "Press ESC or Back to return to menu", SCREEN_WIDTH, SCREEN_HEIGHT - 25);
    }

    private void drawHighScore(Graphics g) {
        g.setColor(Color.orange);
        g.setFont(menuFont);
        drawCenteredString(g, "HIGH SCORE", SCREEN_WIDTH, SCREEN_HEIGHT / 3);

        g.setFont(optionFont);
        drawCenteredString(g, String.valueOf(highScore), SCREEN_WIDTH, SCREEN_HEIGHT / 2);

        g.setFont(infoFont);
        drawCenteredString(g, "Press ESC to return to menu", SCREEN_WIDTH, SCREEN_HEIGHT - 50);
    }

    private void drawGame(Graphics g) {
        // Draw apple
        g.setColor(Color.red);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

        // Draw snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                g.setColor(Color.green);
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            } else {
                g.setColor(new Color(45, 180, 0));
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }
        }

        // Score
        g.setColor(Color.white);
        g.setFont(infoFont);
        g.drawString("Score: " + applesEaten, 10, 20);
        g.drawString("High Score: " + highScore, 10, 40);

        if (paused) {
            drawPaused(g);
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(menuFont);
        drawCenteredString(g, "GAME OVER", SCREEN_WIDTH, SCREEN_HEIGHT / 3);

        g.setFont(optionFont);
        drawCenteredString(g, "Your Score: " + applesEaten, SCREEN_WIDTH, SCREEN_HEIGHT / 2);

        g.setFont(infoFont);
        drawCenteredString(g, "Press R to Restart or ESC for Menu", SCREEN_WIDTH, SCREEN_HEIGHT / 2 + 50);
    }

    private void drawPaused(Graphics g) {
        g.setColor(new Color(255, 255, 255, 150));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.setColor(Color.blue);
        g.setFont(menuFont);
        drawCenteredString(g, "PAUSED", SCREEN_WIDTH, SCREEN_HEIGHT / 2);
    }

    // Utility to center strings
    private void drawCenteredString(Graphics g, String text, int width, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    // Keyboard input handler
    private class MyKeyAdapter extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            switch (gameState) {
                case MENU -> {
                    if (key == KeyEvent.VK_UP) {
                        menuSelection = (menuSelection - 1 + menuItems.length) % menuItems.length;
                        repaint();
                    } else if (key == KeyEvent.VK_DOWN) {
                        menuSelection = (menuSelection + 1) % menuItems.length;
                        repaint();
                    } else if (key == KeyEvent.VK_ENTER) {
                        selectMenuItem();
                    }
                }
                case OPTIONS -> {
                    if (key == KeyEvent.VK_UP) {
                        optionSelection = (optionSelection - 1 + optionItems.length) % optionItems.length;
                        repaint();
                    } else if (key == KeyEvent.VK_DOWN) {
                        optionSelection = (optionSelection + 1) % optionItems.length;
                        repaint();
                    } else if (key == KeyEvent.VK_LEFT) {
                        if (optionSelection == 0 && delay < 300) {
                            delay += 10;
                            if (running) {
                                timer.setDelay(delay);
                            }
                            repaint();
                        }
                    } else if (key == KeyEvent.VK_RIGHT) {
                        if (optionSelection == 0 && delay > 30) {
                            delay -= 10;
                            if (running) {
                                timer.setDelay(delay);
                            }
                            repaint();
                        }
                    } else if (key == KeyEvent.VK_ESCAPE) {
                        gameState = State.MENU;
                        repaint();
                    } else if (key == KeyEvent.VK_ENTER && optionSelection == 1) {
                        gameState = State.MENU;
                        repaint();
                    }
                }
                case PLAYING -> {
                    if (!paused) {
                        if (key == KeyEvent.VK_LEFT && direction != 'R') {
                            direction = 'L'; 
                        }else if (key == KeyEvent.VK_RIGHT && direction != 'L') {
                            direction = 'R'; 
                        }else if (key == KeyEvent.VK_UP && direction != 'D') {
                            direction = 'U'; 
                        }else if (key == KeyEvent.VK_DOWN && direction != 'U') {
                            direction = 'D'; 
                        }else if (key == KeyEvent.VK_P) {
                            paused = true;
                            gameState = State.PAUSED;
                            repaint();
                        } else if (key == KeyEvent.VK_ESCAPE) {
                            running = false;
                            timer.stop();
                            gameState = State.MENU;
                            repaint();
                        }
                    } else {
                        if (key == KeyEvent.VK_P) {
                            paused = false;
                            gameState = State.PLAYING;
                            repaint();
                        } else if (key == KeyEvent.VK_ESCAPE) {
                            running = false;
                            timer.stop();
                            gameState = State.MENU;
                            repaint();
                        }
                    }
                }
                case GAME_OVER -> {
                    if (key == KeyEvent.VK_R) {
                        startGame();
                    } else if (key == KeyEvent.VK_ESCAPE) {
                        gameState = State.MENU;
                        repaint();
                    }
                }
                case HIGH_SCORE -> {
                    if (key == KeyEvent.VK_ESCAPE) {
                        gameState = State.MENU;
                        repaint();
                    }
                }
            }
        }
    }

    // Mouse input handler for clicking on menus
    private class MyMouseAdapter extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            int mx = e.getX();
            int my = e.getY();

            if (gameState == State.MENU) {
                // Calculate button bounds and check clicks
                int baseY = SCREEN_HEIGHT / 2;
                for (int i = 0; i < menuItems.length; i++) {
                    int yPos = baseY + i * 50;
                    FontMetrics fm = getFontMetrics(optionFont);
                    int width = fm.stringWidth(menuItems[i]);
                    int xPos = (SCREEN_WIDTH - width) / 2;
                    Rectangle rect = new Rectangle(xPos, yPos - fm.getHeight(), width, fm.getHeight());
                    if (rect.contains(mx, my)) {
                        menuSelection = i;
                        selectMenuItem();
                        return;
                    }
                }
            } else if (gameState == State.OPTIONS) {
                int baseY = SCREEN_HEIGHT / 2;
                for (int i = 0; i < optionItems.length; i++) {
                    int yPos = baseY + i * 50;
                    FontMetrics fm = getFontMetrics(optionFont);
                    int width = fm.stringWidth(optionItems[i] + (i == 0 ? delay + " ms" : ""));
                    int xPos = (SCREEN_WIDTH - width) / 2;
                    Rectangle rect = new Rectangle(xPos, yPos - fm.getHeight(), width, fm.getHeight());
                    if (rect.contains(mx, my)) {
                        optionSelection = i;
                        if (i == 1) { // Back button
                            gameState = State.MENU;
                        }
                        repaint();
                        return;
                    }
                }
            }
        }
    }

    private void selectMenuItem() {
        switch (menuSelection) {
            case 0 ->
                startGame();
            case 1 -> {
                gameState = State.OPTIONS;
                optionSelection = 0;
                repaint();
            }
            case 2 -> {
                gameState = State.HIGH_SCORE;
                repaint();
            }
            case 3 ->
                System.exit(0);
        }
    }
}
