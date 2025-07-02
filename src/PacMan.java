import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    // Estados del juego
    enum GameState {
        MAIN_MENU,
        PLAYING,
        PAUSED,
        GAME_OVER,
        PLAYER_DIED
    }
    
    private GameState gameState = GameState.MAIN_MENU; // Estado inicial ahora es MENU
    
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'R';
        char nextDirection = 'R';
        int velocityX = 4;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateVelocity() {
            switch (direction) {
                case 'U':
                    velocityX = 0;
                    velocityY = -4;
                    break;
                case 'D':
                    velocityX = 0;
                    velocityY = 4;
                    break;
                case 'L':
                    velocityX = -4;
                    velocityY = 0;
                    break;
                case 'R':
                    velocityX = 4;
                    velocityY = 0;
                    break;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
            this.direction = 'R';
            this.nextDirection = 'R';
            updateVelocity();
        }
    }

    // Variables del juego
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;
    private Image menuBackground;
    private Image logoImage;

    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    Timer deathTimer;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean showPacman = true;
    
    // Botones del menú
    private JButton startButton;
    private JButton restartButton;
    private JButton menuButton;
    private JButton resumeButton;
    private JButton exitButton;
    
    // Fuentes personalizadas
    private Font customFontLarge;
    private Font customFontMedium;
    private Font customFontSmall;
    private Font arialBlackFont;

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        setLayout(null);

        // Cargar fuentes
        try {
            InputStream is = getClass().getResourceAsStream("./PAC-FONT.TTF");
            customFontLarge = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(40f);
            customFontMedium = customFontLarge.deriveFont(20f);
            customFontSmall = customFontLarge.deriveFont(20f);
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFontLarge);
        } catch (Exception e) {
            System.err.println("Error cargando fuente personalizada: " + e.getMessage());
            customFontLarge = new Font("Arial Black", Font.BOLD, 48);
            customFontMedium = new Font("Arial Black", Font.BOLD, 24);
            customFontSmall = new Font("Arial Black", Font.BOLD, 18);
        }
        
        arialBlackFont = new Font("Arial Black", Font.BOLD, 20);

        // Carga de imágenes
        loadImages();
        
        // Configuración de botones
        setupButtons();
        
        // Inicialmente mostramos solo los botones del menú principal
        showMainMenuButtons(true);
        
        gameLoop = new Timer(16, this);
        deathTimer = new Timer(1000, e -> {
            deathTimer.stop();
            if (lives <= 0) {
                gameOver();
            } else {
                resetPositions();
                gameState = GameState.PLAYING;
                showPacman = true;
                gameLoop.start();
            }
            repaint();
        });
    }

    private void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
        
        // Cargar imágenes para el menú
        menuBackground = new ImageIcon(getClass().getResource("./menu_background.png")).getImage()
            .getScaledInstance(boardWidth, boardHeight, Image.SCALE_SMOOTH);
        logoImage = new ImageIcon(getClass().getResource("./logo.png")).getImage();
    }

    private void setupButtons() {
        Color buttonColor = new Color(255, 255, 0);
        Color borderColor = new Color(33, 33, 222);
        
        // Botón de Iniciar Juego (para el menú principal)
        startButton = new JButton("INICIAR JUEGO");
        startButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 90, 300, 50);
        startButton.addActionListener(e -> startGame());
        startButton.setBackground(buttonColor);
        startButton.setForeground(Color.BLACK);
        startButton.setFont(customFontMedium);
        startButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        startButton.setFocusPainted(false);
        add(startButton);

        // Botón de Reiniciar Nivel
        restartButton = new JButton("REINICIAR NIVEL");
        restartButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 10, 300, 50);
        restartButton.addActionListener(e -> restartLevel());
        restartButton.setBackground(buttonColor);
        restartButton.setForeground(Color.BLACK);
        restartButton.setFont(customFontMedium);
        restartButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        restartButton.setFocusPainted(false);
        add(restartButton);

        // Botón de Menú Principal
        menuButton = new JButton("MENU PRINCIPAL");
        menuButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 80, 300, 50);
        menuButton.addActionListener(e -> showMainMenu());
        menuButton.setBackground(buttonColor);
        menuButton.setForeground(Color.BLACK);
        menuButton.setFont(customFontMedium);
        menuButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        menuButton.setFocusPainted(false);
        add(menuButton);
        
        // Botón de Continuar
        resumeButton = new JButton("CONTINUAR");
        resumeButton.setBounds(boardWidth/2 - 150, boardHeight/2 - 60, 300, 50);
        resumeButton.addActionListener(e -> togglePause());
        resumeButton.setBackground(buttonColor);
        resumeButton.setForeground(Color.BLACK);
        resumeButton.setFont(customFontMedium);
        resumeButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        resumeButton.setFocusPainted(false);
        add(resumeButton);
        
        // Botón de Salir del Juego
        exitButton = new JButton("SALIR DEL JUEGO");
        exitButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 150, 300, 50);
        exitButton.addActionListener(e -> exitGame());
        exitButton.setBackground(buttonColor);
        exitButton.setForeground(Color.BLACK);
        exitButton.setFont(customFontMedium);
        exitButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        exitButton.setFocusPainted(false);
        add(exitButton);
        
        // Mostramos solo los botones necesarios según el estado
        showMainMenuButtons(true);
    }

    private void showMainMenuButtons(boolean show) {
        startButton.setVisible(show);
        exitButton.setVisible(show);
        restartButton.setVisible(!show);
        menuButton.setVisible(!show);
        resumeButton.setVisible(false);
    }

    private void showGameButtons(boolean show) {
        startButton.setVisible(false);
        exitButton.setVisible(show);
        restartButton.setVisible(show);
        menuButton.setVisible(show);
        resumeButton.setVisible(show);
    }

        private void showGameoverButtons(boolean show) {
        startButton.setVisible(false);
        exitButton.setVisible(show);
        restartButton.setVisible(show);
        menuButton.setVisible(show);
        resumeButton.setVisible(!show);
    }

    //HAY QUE COLORCAR EL SHOWGAMEOVERBUTTONS
    //DEBE APARECER SOLO REINICIAR, SALIR, MENU, NO DEBE IR CONTINUAR

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileMapChar = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileMapChar) {
                    case 'X':
                        walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                        break;
                    case 'b':
                        ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'o':
                        ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'p':
                        ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'r':
                        ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'P':
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        break;
                    case ' ':
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        switch (gameState) {
            case MAIN_MENU:
                drawMainMenu(g);
                break;
            case PLAYING:
            case PLAYER_DIED:
                drawGame(g);
                break;
            case PAUSED:
                drawGame(g);
                drawPauseScreen(g);
                break;
            case GAME_OVER:
                drawGame(g);
                drawGameOver(g);
                break;
        }
    }

    private void drawMainMenu(Graphics g) {
        // Dibujar fondo del menú
        g.drawImage(menuBackground, 0, 0, boardWidth, boardHeight, this);
        
        // Dibujar logo
        int logoWidth = logoImage.getWidth(null);
        int logoHeight = logoImage.getHeight(null);
        g.drawImage(logoImage, (boardWidth - logoWidth)/2, boardHeight/4 - logoHeight/2, null);
        
        // Dibujar título
        g.setColor(Color.YELLOW);
        g.setFont(customFontLarge);
        String title = "PAC-MAN ERIKIKI";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (boardWidth - titleWidth)/2, boardHeight/3 + 50);
    }

    private void drawGame(Graphics g) {
        // Dibujar fondo del juego
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        // Dibuja Pac-Man (si no está en estado de muerte o si debe mostrarse)
        if (showPacman && gameState != GameState.PLAYER_DIED) {
            switch (pacman.direction) {
                case 'U': g.drawImage(pacmanUpImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'D': g.drawImage(pacmanDownImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'L': g.drawImage(pacmanLeftImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'R': g.drawImage(pacmanRightImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
            }
        }

        // Dibuja fantasmas
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Dibuja paredes
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Dibuja comida
        g.setColor(Color.orange);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Dibuja puntaje y vidas
        g.setColor(Color.GREEN);
        g.setFont(customFontSmall);
        String livesText = "x";
        g.drawString(livesText, tileSize/2, tileSize/2);
        
        g.setFont(arialBlackFont);
        String numbers = lives + " SCORE: " + score;
        int textWidth = g.getFontMetrics().stringWidth(numbers);
        g.drawString(numbers, tileSize/2 + g.getFontMetrics(customFontSmall).stringWidth(livesText), tileSize/2);
        
        // Dibuja mensaje de muerte
        if (gameState == GameState.PLAYER_DIED) {
            g.setColor(Color.RED);
            g.setFont(arialBlackFont);
            String deathText = "PERDISTE UNA VIDA";
            textWidth = g.getFontMetrics().stringWidth(deathText);
            g.drawString(deathText, (boardWidth - textWidth)/2, boardHeight/2);
        }
    }

    private void drawPauseScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        g.setColor(Color.YELLOW);
        g.setFont(customFontLarge);
        String pauseText = "PAUSA";
        int textWidth = g.getFontMetrics().stringWidth(pauseText);
        g.drawString(pauseText, (boardWidth - textWidth)/2, boardHeight/2 - 120);
        
        g.setColor(Color.WHITE);
        g.setFont(customFontMedium);
        String instruction = "Presiona P para continuar";
        textWidth = g.getFontMetrics().stringWidth(instruction);
        g.drawString(instruction, (boardWidth - textWidth)/2, boardHeight/2 + 260);
        
        showGameButtons(true);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        g.setColor(Color.RED);
        g.setFont(customFontLarge);
        String gameOverText = "GAME OVER";
        int textWidth = g.getFontMetrics().stringWidth(gameOverText);
        g.drawString(gameOverText, (boardWidth - textWidth)/2, boardHeight/2 - 80);
        
        g.setColor(Color.YELLOW);
        g.setFont(customFontMedium);
        String scoreLabel = "PUNTUACION: ";
        int labelWidth = g.getFontMetrics().stringWidth(scoreLabel);
        
        g.setFont(arialBlackFont);
        String scoreNumbers = String.valueOf(score);
        int numbersWidth = g.getFontMetrics().stringWidth(scoreNumbers);
        
        int totalWidth = labelWidth + numbersWidth;
        int startX = (boardWidth - totalWidth)/2;
        
        g.setFont(customFontMedium);
        g.drawString(scoreLabel, startX, boardHeight/2 - 30);
        
        g.setFont(arialBlackFont);
        g.drawString(scoreNumbers, startX + labelWidth, boardHeight/2 - 30);
        
        showGameoverButtons(true);
    }

private void startGame() {
    loadMap();
    resetPositions();
    gameState = GameState.PLAYING;
    // Ocultamos todos los botones
    startButton.setVisible(false);
    exitButton.setVisible(false);
    restartButton.setVisible(false);
    menuButton.setVisible(false);
    resumeButton.setVisible(false);
    // Reiniciamos valores del juego
    score = 0;
    lives = 3;
    showPacman = true;
    // Iniciamos el juego
    gameLoop.start();
    requestFocus();
}

    private void showMainMenu() {
        gameState = GameState.MAIN_MENU;
        gameLoop.stop();
        showMainMenuButtons(true);
        repaint();
    }

    public void move() {
        if (gameState != GameState.PLAYING) return;
        
        movePacman();
        moveGhosts();
        checkFoodCollision();
    }

    private void movePacman() {
        if (pacman.nextDirection != pacman.direction) {
            tryChangeDirection();
        }
        
        int oldX = pacman.x;
        int oldY = pacman.y;
        
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        
        if (checkWallCollision(pacman)) {
            pacman.x = oldX;
            pacman.y = oldY;
        }
        
        checkBoardBounds();
    }

    private void tryChangeDirection() {
        int tempX = pacman.x;
        int tempY = pacman.y;
        char tempDirection = pacman.direction;
        int tempVX = pacman.velocityX;
        int tempVY = pacman.velocityY;
        
        pacman.direction = pacman.nextDirection;
        pacman.updateVelocity();
        
        tempX += pacman.velocityX;
        tempY += pacman.velocityY;
        
        Block tempPacman = new Block(null, tempX, tempY, pacman.width, pacman.height);
        
        if (!checkWallCollision(tempPacman)) {
            pacman.updateVelocity();
        } else {
            pacman.direction = tempDirection;
            pacman.velocityX = tempVX;
            pacman.velocityY = tempVY;
        }
    }

    private boolean checkWallCollision(Block block) {
        for (Block wall : walls) {
            if (collision(block, wall)) {
                return true;
            }
        }
        return false;
    }

    private void checkBoardBounds() {
        if (pacman.x < -pacman.width) {
            pacman.x = boardWidth;
        } else if (pacman.x > boardWidth) {
            pacman.x = -pacman.width;
        }
    }

    private void moveGhosts() {
        for (Block ghost : ghosts) {
            int oldX = ghost.x;
            int oldY = ghost.y;
            
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            if (checkWallCollision(ghost) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                ghost.x = oldX;
                ghost.y = oldY;
                ghost.direction = directions[random.nextInt(4)];
                ghost.updateVelocity();
            }
            
            if (collision(ghost, pacman) && gameState == GameState.PLAYING) {
                playerDied();
            }
        }
    }

    private void playerDied() {
        lives--;
        showPacman = false;
        gameState = GameState.PLAYER_DIED;
        gameLoop.stop();
        deathTimer.start();
        repaint();
    }

    private void gameOver() {
        gameState = GameState.GAME_OVER;
        gameLoop.stop();
        showGameoverButtons(true);
        repaint();
    }

    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            gameLoop.stop();
            showGameButtons(true);
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            gameLoop.start();
            showGameButtons(false);
            requestFocus();
        }
        repaint();
    }

    private void exitGame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        System.exit(0);
    }

    private void checkFoodCollision() {
        HashSet<Block> eatenFood = new HashSet<>();
        for (Block food : foods) {
            if (collision(pacman, food)) {
                eatenFood.add(food);
                score += 10;
            }
        }
        foods.removeAll(eatenFood);
        
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.direction = directions[random.nextInt(4)];
            ghost.updateVelocity();
        }
    }

    private void restartLevel() {
        score = 0;
        lives = 3;
        loadMap();
        resetPositions();
        gameState = GameState.PLAYING;
        showGameButtons(false);
        showPacman = true;
        gameLoop.start();
        requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            move();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameState == GameState.GAME_OVER) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                restartLevel();
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_P:
                if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
                    togglePause();
                }
                break;
            case KeyEvent.VK_UP:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'U';
                break;
            case KeyEvent.VK_DOWN:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'D';
                break;
            case KeyEvent.VK_LEFT:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'L';
                break;
            case KeyEvent.VK_RIGHT:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'R';
                break;
            //O TAMBIEN CON AWSD EN EL TECLADO
            case KeyEvent.VK_W:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'U';
                break;
            case KeyEvent.VK_S:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'D';
                break;
            case KeyEvent.VK_A:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'L';
                break;
            case KeyEvent.VK_D:
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'R';
                break;
            case KeyEvent.VK_ESCAPE:
                if (gameState == GameState.PLAYING) {
                    togglePause();
                } else if (gameState == GameState.PAUSED) {
                    showMainMenu();
                }
                break;
        }
        
        if (gameState == GameState.PLAYING) {
            tryChangeDirection();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("PAC-MAN ERIKIKI");
        
        // Configurar propiedades de la ventana
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setUndecorated(false);
        
        // Configurar el contenido del juego
        PacMan game = new PacMan();
        frame.add(game);
        frame.pack();
        
        // Centrar la ventana
        frame.setLocationRelativeTo(null);
        
        // Hacer visible la ventana
        frame.setVisible(true);
        
        // Enfocar el juego para que reciba eventos de teclado
        game.requestFocus();
    }
}