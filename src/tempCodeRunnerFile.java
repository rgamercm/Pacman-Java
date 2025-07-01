import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
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

        void updateDirection(char newDirection) {
            char oldDirection = this.direction;
            this.direction = newDirection;
            updateVelocity();
            
            // Simula el movimiento para ver si hay colisión
            int tempX = this.x + this.velocityX;
            int tempY = this.y + this.velocityY;
            
            boolean canMove = true;
            for (Block wall : walls) {
                if (tempX < wall.x + wall.width &&
                    tempX + this.width > wall.x &&
                    tempY < wall.y + wall.height &&
                    tempY + this.height > wall.y) {
                    canMove = false;
                    break;
                }
            }
            
            if (!canMove) {
                // Si no puede moverse, vuelve a la dirección anterior
                this.direction = oldDirection;
                updateVelocity();
            }
        }

        void updateVelocity() {
            // Velocidad reducida para mejor control
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -2;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = 2;
            }
            else if (this.direction == 'L') {
                this.velocityX = -2;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = 2;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

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
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        
        gameLoop = new Timer(16, this); // ~60 FPS
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Dibuja Pac-Man con la imagen correspondiente a su dirección
        switch (pacman.direction) {
            case 'U': g.drawImage(pacmanUpImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
            case 'D': g.drawImage(pacmanDownImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
            case 'L': g.drawImage(pacmanLeftImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
            case 'R': g.drawImage(pacmanRightImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + score, tileSize/2, tileSize/2);
        }
        else {
            g.drawString("x" + lives + " Score: " + score, tileSize/2, tileSize/2);
        }
    }

    public void move() {
        // Guarda posición anterior
        int oldX = pacman.x;
        int oldY = pacman.y;
        
        // Mueve a Pac-Man
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Verifica colisión con paredes
        boolean collided = false;
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                collided = true;
                break;
            }
        }

        if (collided) {
            // Si hay colisión, detiene a Pac-Man
            pacman.x = oldX;
            pacman.y = oldY;
            pacman.velocityX = 0;
            pacman.velocityY = 0;
        }

        // Verifica colisión con fantasmas
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            // Movimiento de fantasmas
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            // Verifica colisión con paredes
            boolean ghostCollided = false;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghostCollided = true;
                    break;
                }
            }
            
            if (ghostCollided) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection);
            }
        }

        // Verifica colisión con comida
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
            return;
        }

        // Cambia dirección según tecla presionada
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                pacman.updateDirection('U');
                break;
            case KeyEvent.VK_DOWN:
                pacman.updateDirection('D');
                break;
            case KeyEvent.VK_LEFT:
                pacman.updateDirection('L');
                break;
            case KeyEvent.VK_RIGHT:
                pacman.updateDirection('R');
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man Erika");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new PacMan());
        frame.pack();
        frame.setVisible(true);
    }
}