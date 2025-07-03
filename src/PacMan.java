import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import javax.imageio.ImageIO;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    // Estados del juego
    enum GameState {
        MAIN_MENU,
        COLOR_SELECTION,
        PLAYING,
        PAUSED,
        GAME_OVER,
        PLAYER_DIED
    }
    
    private GameState gameState = GameState.MAIN_MENU;
    
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
        
        // Nuevos campos para el comportamiento mejorado de fantasmas
        int movementStrategy; // 0-2
        long lastDirectionChange;
        int changeInterval;
        int velocity = 4;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
            
            // Inicialización de nuevos campos
            this.movementStrategy = random.nextInt(3);
            this.lastDirectionChange = System.currentTimeMillis();
            this.changeInterval = 1000 + random.nextInt(2000);
        }

        void updateVelocity() {
            switch (direction) {
                case 'U':
                    velocityX = 0;
                    velocityY = -velocity;
                    break;
                case 'D':
                    velocityX = 0;
                    velocityY = velocity;
                    break;
                case 'L':
                    velocityX = -velocity;
                    velocityY = 0;
                    break;
                case 'R':
                    velocityX = velocity;
                    velocityY = 0;
                    break;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
            this.direction = 'R';
            this.nextDirection = 'R';
            this.movementStrategy = random.nextInt(3);
            this.lastDirectionChange = System.currentTimeMillis();
            this.changeInterval = 1000 + random.nextInt(2000);
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

    private SoundManager moveSound = new SoundManager();
    private SoundManager dieSound = new SoundManager();
    private SoundManager currentSound = null;

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
    private JButton colorSelectionButton;
    private JButton restartButton;
    private JButton menuButton;
    private JButton resumeButton;
    private JButton exitButton;
    private JButton exiteButton;
    private JButton controlsButton;
    private JButton aboutButton;

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

        // Carga de imágenes (cargamos el amarillo por defecto)
        loadImages("amarillo");
        
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
                startMoveSound();
            }
            repaint();
        });
    }

    private void loadImages(String color) {
        try {
            wallImage = ImageIO.read(getClass().getResource("./wall.png"));
            blueGhostImage = ImageIO.read(getClass().getResource("./blueGhost.png"));
            orangeGhostImage = ImageIO.read(getClass().getResource("./orangeGhost.png"));
            pinkGhostImage = ImageIO.read(getClass().getResource("./pinkGhost.png"));
            redGhostImage = ImageIO.read(getClass().getResource("./redGhost.png"));

            // Cargar imágenes de Pac-Man según el color seleccionado
            pacmanUpImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanUp.png"));
            pacmanDownImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanDown.png"));
            pacmanLeftImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanLeft.png"));
            pacmanRightImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanRight.png"));
            
            // Cargar imágenes para el menú
            Image rawImage = ImageIO.read(getClass().getResource("./menu_background.png"));
            BufferedImage scaledImage = new BufferedImage(boardWidth, boardHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(rawImage, 0, 0, boardWidth, boardHeight, null);
            g2d.dispose();
            menuBackground = scaledImage;

            logoImage = ImageIO.read(getClass().getResource("./logo.png"));
        } catch (Exception e) {
            System.err.println("Error cargando imágenes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupButtons() {
        Color buttonColor = new Color(255, 255, 0);
        Color borderColor = new Color(33, 33, 222);

        // Botón de Iniciar Juego
        startButton = new JButton("INICIAR JUEGO");
        startButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 30, 300, 50);
        startButton.addActionListener(e -> showColorSelection());
        startButton.setBackground(buttonColor);
        startButton.setForeground(Color.BLACK);
        startButton.setFont(customFontMedium);
        startButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        startButton.setFocusPainted(false);
        add(startButton);

        // Botón de Selección de Color
        colorSelectionButton = new JButton("SELECCION COLOR");
        colorSelectionButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 30, 300, 50);
        colorSelectionButton.addActionListener(e -> showColorOptions());
        colorSelectionButton.setBackground(buttonColor);
        colorSelectionButton.setForeground(Color.BLACK);
        colorSelectionButton.setFont(customFontMedium);
        colorSelectionButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        colorSelectionButton.setFocusPainted(false);
        colorSelectionButton.setVisible(false);
        add(colorSelectionButton);

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

        // Botón de Controles
        controlsButton = new JButton("CONTROLES");
        controlsButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 90, 300, 40);
        controlsButton.setBackground(buttonColor);
        controlsButton.setForeground(Color.BLACK);
        controlsButton.setFont(customFontSmall);
        controlsButton.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        controlsButton.setFocusPainted(false);
        controlsButton.addActionListener(e -> showControls());
        add(controlsButton);

        // Botón de Info del Juego
        aboutButton = new JButton("INFO DEL JUEGO");
        aboutButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 140, 300, 40);
        aboutButton.setBackground(buttonColor);
        aboutButton.setForeground(Color.BLACK);
        aboutButton.setFont(customFontSmall);
        aboutButton.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        aboutButton.setFocusPainted(false);
        aboutButton.addActionListener(e -> showAbout());
        add(aboutButton);

        // Botón de Salir del Juego
        exitButton = new JButton("SALIR DEL JUEGO");
        exitButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 190, 300, 50);
        exitButton.addActionListener(e -> exitGame());
        exitButton.setBackground(buttonColor);
        exitButton.setForeground(Color.BLACK);
        exitButton.setFont(customFontMedium);
        exitButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        exitButton.setFocusPainted(false);
        add(exitButton);

        // Botón alternativo de Salir del Juego
        exiteButton = new JButton("SALIR DEL JUEGO");
        exiteButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 150, 300, 50);
        exiteButton.addActionListener(e -> exitGame());
        exiteButton.setBackground(buttonColor);
        exiteButton.setForeground(Color.BLACK);
        exiteButton.setFont(customFontMedium);
        exiteButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        exiteButton.setFocusPainted(false);
        add(exiteButton);
    }

    private void showMainMenuButtons(boolean show) {
        startButton.setVisible(show);
        colorSelectionButton.setVisible(false);
        restartButton.setVisible(!show);
        menuButton.setVisible(!show);
        resumeButton.setVisible(false);
        controlsButton.setVisible(show);
        aboutButton.setVisible(show);
        exitButton.setVisible(show);
        exiteButton.setVisible(!show);
    }

    private void showColorSelection() {
        startButton.setVisible(false);
        colorSelectionButton.setVisible(true);
        gameState = GameState.COLOR_SELECTION;
        repaint();
    }

    private void showColorOptions() {
        JFrame colorFrame = new JFrame("Selecciona el color de tu Pac-Man");
        colorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Configuración del tamaño y layout
        colorFrame.setSize(650, 400); // Tamaño un poco más grande
        colorFrame.setLayout(new GridLayout(2, 4, 15, 15));
        
        // Centrado preciso en la pantalla
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - colorFrame.getWidth()) / 2;
        int y = (screenSize.height - colorFrame.getHeight()) / 2;
        colorFrame.setLocation(x, y);

        String[] colors = {"amarillo", "azul", "negro", "celeste", "rojo", "rosa", "verde"};
        Color[] colorValues = {Color.YELLOW, Color.BLUE, Color.BLACK, Color.CYAN, Color.RED, Color.PINK, Color.GREEN};

        // Fuente mejorada para los botones
        Font colorButtonFont = new Font("Arial", Font.BOLD, 14);

        for (int i = 0; i < colors.length; i++) {
            JButton colorButton = new JButton(colors[i].toUpperCase()) {
                @Override
                protected void paintComponent(Graphics g) {
                    // Fondo del botón
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(getBackground());
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Fondo blanco para el texto
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    String text = getText();
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    int padding = 8;
                    
                    // Rectángulo blanco con bordes redondeados
                    int arc = 15; // Radio para bordes redondeados
                    g2.fillRoundRect((getWidth() - textWidth)/2 - padding, 
                                   (getHeight() - textHeight)/2 - padding/2,
                                   textWidth + padding*2, 
                                   textHeight + padding, 
                                   arc, arc);
                    
                    // Texto
                    g2.setColor(Color.BLACK);
                    g2.drawString(text, (getWidth() - textWidth)/2, 
                                (getHeight() - fm.getHeight())/2 + fm.getAscent());
                    g2.dispose();
                }
            };
            
            colorButton.setBackground(colorValues[i]);
            colorButton.setFont(colorButtonFont);
            colorButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            colorButton.setFocusPainted(false);
            colorButton.addActionListener(e -> {
                loadImages(colors[getIndex(colorButton.getText().toLowerCase(), colors)]);
                colorFrame.dispose();
                startGameAfterColorSelection();
            });
            
            colorFrame.add(colorButton);
        }

        // Hacer la ventana modal (bloquea la ventana principal)
        colorFrame.setResizable(false);
        colorFrame.setVisible(true);
    }

    private int getIndex(String text, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(text)) {
                return i;
            }
        }
        return 0;
    }

    private void startGameAfterColorSelection() {
        colorSelectionButton.setVisible(false);
        startGame();
    }

    private void startGame() {
        loadMap();
        resetPositions();
        gameState = GameState.PLAYING;
        
        // Ocultar botones no necesarios
        startButton.setVisible(false);
        exitButton.setVisible(false);
        exiteButton.setVisible(false);
        restartButton.setVisible(false);
        menuButton.setVisible(false);
        resumeButton.setVisible(false);
        controlsButton.setVisible(false); 
        aboutButton.setVisible(false);    

        // Reiniciar valores del juego
        score = 0;
        lives = 3;
        showPacman = true;
        
        // Iniciar juego
        gameLoop.start();
        requestFocus();
        startMoveSound();
    }

    private void showMainMenu() {
        gameState = GameState.MAIN_MENU;
        gameLoop.stop();
        showMainMenuButtons(true);
        stopAllSounds();
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
        long currentTime = System.currentTimeMillis();
        
        for (Block ghost : ghosts) {
            // Guardar posición anterior
            int oldX = ghost.x;
            int oldY = ghost.y;
            
            // Mover al fantasma según su velocidad actual
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            // Verificar colisiones con paredes o bordes
            boolean collided = checkWallCollision(ghost) || 
                             ghost.x <= 0 || 
                             ghost.x + ghost.width >= boardWidth;
            
            if (collided) {
                // Revertir movimiento
                ghost.x = oldX;
                ghost.y = oldY;
                
                // Elegir nueva dirección basada en la personalidad del fantasma
                char newDirection = chooseNewDirection(ghost, oldX, oldY);
                ghost.direction = newDirection;
                ghost.updateVelocity();
            }
            
            // Cambio de dirección periódico (sin necesidad de colisión)
            if (currentTime - ghost.lastDirectionChange > ghost.changeInterval) {
                ghost.direction = chooseNewDirection(ghost, ghost.x, ghost.y);
                ghost.updateVelocity();
                ghost.lastDirectionChange = currentTime;
                ghost.changeInterval = 1000 + random.nextInt(2000); // 1-3 segundos
            }
            
            // Verificar colisión con Pac-Man
            if (collision(ghost, pacman) && gameState == GameState.PLAYING) {
                playerDied();
            }
        }
    }

    private char chooseNewDirection(Block ghost, int currentX, int currentY) {
        // Obtener direcciones posibles (sin causar colisión inmediata)
        List<Character> possibleDirections = new ArrayList<>();
        
        // Verificar cada dirección posible
        for (char dir : new char[]{'U', 'D', 'L', 'R'}) {
            Block temp = new Block(null, currentX, currentY, ghost.width, ghost.height);
            temp.direction = dir;
            temp.updateVelocity();
            temp.x += temp.velocityX;
            temp.y += temp.velocityY;
            
            if (!checkWallCollision(temp) && 
                temp.x > 0 && 
                temp.x + temp.width < boardWidth) {
                possibleDirections.add(dir);
            }
        }
        
        // Si no hay direcciones posibles (raro caso), usar aleatorio
        if (possibleDirections.isEmpty()) {
            return directions[random.nextInt(4)];
        }
        
        // Basar la decisión en la personalidad del fantasma
        switch (ghost.movementStrategy) {
            case 0: // Perseguidor agresivo
                if (random.nextDouble() < 0.7) { // 70% de perseguir
                    return chasePacMan(ghost, currentX, currentY, possibleDirections);
                }
                break;
                
            case 1: // Explorador
                if (random.nextDouble() < 0.3) { // 30% de perseguir
                    return chasePacMan(ghost, currentX, currentY, possibleDirections);
                }
                break;
                
            case 2: // Aleatorio con preferencia por dirección actual
                if (random.nextDouble() < 0.5 && possibleDirections.contains(ghost.direction)) {
                    return ghost.direction; // 50% de mantener dirección
                }
                break;
        }
        
        // Dirección aleatoria entre las posibles
        return possibleDirections.get(random.nextInt(possibleDirections.size()));
    }

    private char chasePacMan(Block ghost, int currentX, int currentY, List<Character> possibleDirections) {
        // Calcular diferencias con posición de Pac-Man
        int dx = pacman.x - currentX;
        int dy = pacman.y - currentY;
        
        // Determinar mejores direcciones para perseguir
        char preferredHorizontal = dx > 0 ? 'R' : 'L';
        char preferredVertical = dy > 0 ? 'D' : 'U';
        
        // Priorizar la dirección con mayor diferencia
        if (Math.abs(dx) > Math.abs(dy)) {
            if (possibleDirections.contains(preferredHorizontal)) {
                return preferredHorizontal;
            } else if (possibleDirections.contains(preferredVertical)) {
                return preferredVertical;
            }
        } else {
            if (possibleDirections.contains(preferredVertical)) {
                return preferredVertical;
            } else if (possibleDirections.contains(preferredHorizontal)) {
                return preferredHorizontal;
            }
        }
        
        // Si no puede perseguir directamente, elegir aleatoria
        return possibleDirections.get(random.nextInt(possibleDirections.size()));
    }

    private void playerDied() {
        lives--;
        showPacman = false;
        gameState = GameState.PLAYER_DIED;
        gameLoop.stop();
        
        stopAllSounds();
        playSound("./die.wav", false);
        
        deathTimer.start();
        repaint();
    }

    private void gameOver() {
        gameState = GameState.GAME_OVER;
        gameLoop.stop();
        stopAllSounds();
        showGameoverButtons(true);
        repaint();
    }

    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            gameLoop.stop();
            stopMoveSound();
            showGameButtons(true);
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            gameLoop.start();
            startMoveSound();
            showGameButtons(false);
            requestFocus();
        }
        repaint();
    }

    private void exitGame() {
        stopAllSounds();
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
        startMoveSound();
    }

    private void showControls() {
        String message = """
            CONTROLES DEL JUEGO:

            - FLECHAS o teclas AWSD para mover a Pac-Man
            - P: Pausar / Reanudar
            - ESC: Ir al menu principal desde pausa

            Objetivo:
            - Come todos los puntos
            - Evita los fantasmas
            - Diviertete !!!
            """;
        JOptionPane.showMessageDialog(this, message, "Controles", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String message = """
            Programacion del juego:

            - Lenguaje: Java
            - Librerias: Swing y AWT
            - Programado por: Erika Villasmil
            - Recursos:
              * Imagenes: sprites de Pac-Man y fantasmas
              * Fuente personalizada: PAC-FONT.TTF
            - Logica implementada desde cero:
              * Movimiento de Pac-Man y enemigos
              * Colisiones con muros, comida, enemigos
              * Estados de juego: Menu, Pausa, Game Over

            Gracias por jugar PAC-MAN ERIKIKI !!!
            """;
        JOptionPane.showMessageDialog(this, message, "Acerca del Juego", JOptionPane.INFORMATION_MESSAGE);
    }

    // Métodos para manejo de sonidos
    private void startMoveSound() {
        if (gameState == GameState.PLAYING) {
            stopAllSounds();
            moveSound.playSound("./move.wav", true);
            currentSound = moveSound;
        }
    }

    private void stopMoveSound() {
        if (currentSound == moveSound) {
            moveSound.stop();
            currentSound = null;
        }
    }

    private void playSound(String path, boolean loop) {
        stopAllSounds();
        if (loop) {
            moveSound.playSound(path, true);
            currentSound = moveSound;
        } else {
            dieSound.playSound(path, false);
            currentSound = dieSound;
        }
    }

    private void stopAllSounds() {
        moveSound.stop();
        dieSound.stop();
        currentSound = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        switch (gameState) {
            case MAIN_MENU:
                drawMainMenu(g);
                break;
            case COLOR_SELECTION:
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
        g.drawImage(menuBackground, 0, 0, boardWidth, boardHeight, this);
        
        int logoWidth = logoImage.getWidth(null);
        int logoHeight = logoImage.getHeight(null);
        g.drawImage(logoImage, (boardWidth - logoWidth)/2, boardHeight/4 - logoHeight/2, null);
        
        g.setColor(Color.YELLOW);
        g.setFont(customFontLarge);
        String title = "PAC-MAN ERIKIKI";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (boardWidth - titleWidth)/2, boardHeight/3 + 50);
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        if (showPacman && gameState != GameState.PLAYER_DIED) {
            switch (pacman.direction) {
                case 'U': g.drawImage(pacmanUpImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'D': g.drawImage(pacmanDownImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'L': g.drawImage(pacmanLeftImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'R': g.drawImage(pacmanRightImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
            }
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.orange);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setColor(Color.GREEN);
        g.setFont(customFontSmall);
        String livesText = "x";
        g.drawString(livesText, tileSize/2, tileSize/2);
        
        g.setFont(arialBlackFont);
        String numbers = lives + " SCORE: " + score;
        int textWidth = g.getFontMetrics().stringWidth(numbers);
        g.drawString(numbers, tileSize/2 + g.getFontMetrics(customFontSmall).stringWidth(livesText), tileSize/2);
        
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

    private void showGameButtons(boolean show) {
        startButton.setVisible(false);
        exiteButton.setVisible(show);
        restartButton.setVisible(show);
        menuButton.setVisible(show);
        resumeButton.setVisible(show);
    }

    private void showGameoverButtons(boolean show) {
        startButton.setVisible(false);
        exiteButton.setVisible(show);
        restartButton.setVisible(show);
        menuButton.setVisible(show);
        resumeButton.setVisible(!show);
    }

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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setUndecorated(false);
        
        PacMan game = new PacMan();
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocus();
        frame.setIconImage(new ImageIcon(PacMan.class.getResource("icon.png")).getImage());
    }
}

class SoundManager {
    private Clip clip;
    private boolean isLooping = false;

    public void playSound(String path, boolean loop) {
        stop();
        isLooping = loop;
        
        try {
            InputStream audioSrc = getClass().getResourceAsStream(path);
            if (audioSrc == null) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            } else {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioSrc);
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            }
            
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
}