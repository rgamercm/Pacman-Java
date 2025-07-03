// Importa las clases necesarias para gráficos y componentes de interfaz de usuario
import java.awt.*;  
// Importa clases para manejar eventos (teclado, ratón)
import java.awt.event.*; 
// Importa clase para manejar imágenes en memoria
import java.awt.image.BufferedImage; 
// Importa clase para leer flujos de entrada de datos
import java.io.InputStream; 
// Importa clase para colecciones que no permiten duplicados
import java.util.HashSet; 
// Importa clase para generar números aleatorios
import java.util.Random; 
// Importa clase para listas dinámicas
import java.util.ArrayList; 
// Importa interfaz para listas
import java.util.List; 
// Importa componentes de interfaz gráfica Swing
import javax.swing.*; 
// Importa clases para reproducción de audio
import javax.sound.sampled.*; 
// Importa clase para manejo de archivos
import java.io.File; 
// Importa clase para lectura/escritura de imágenes
import javax.imageio.ImageIO; 

// Clase principal del juego que hereda de JPanel e implementa interfaces para eventos
public class PacMan extends JPanel implements ActionListener, KeyListener {
    // Enumeración que define los posibles estados del juego
    enum GameState {
        MAIN_MENU,       // Estado cuando se muestra el menú principal
        COLOR_SELECTION,  // Estado para seleccionar color de Pac-Man
        PLAYING,          // Estado cuando el juego está en curso
        PAUSED,          // Estado cuando el juego está pausado
        GAME_OVER,       // Estado cuando el juego termina
        PLAYER_DIED       // Estado cuando el jugador pierde una vida
    }
    
    // Variable que guarda el estado actual del juego, inicializado en MAIN_MENU
    private GameState gameState = GameState.MAIN_MENU; 
    
    // Clase interna que representa cualquier elemento del juego (Pac-Man, fantasmas, paredes, etc.)
    class Block {
        // Posición horizontal del bloque en píxeles
        int x; 
        // Posición vertical del bloque en píxeles
        int y; 
        // Ancho del bloque en píxeles
        int width; 
        // Alto del bloque en píxeles
        int height;
        // Imagen que representa al bloque
        Image image; 

        // Posición horizontal inicial (para reiniciar)
        int startX; 
        // Posición vertical inicial (para reiniciar)
        int startY;
        // Dirección actual (U=Arriba, D=Abajo, L=Izquierda, R=Derecha)
        char direction = 'R'; 
        // Siguiente dirección solicitada por el jugador
        char nextDirection = 'R'; 
        // Velocidad horizontal actual
        int velocityX = 4; 
        // Velocidad vertical actual
        int velocityY = 0; 
        
        // Estrategia de movimiento del fantasma (0-2)
        int movementStrategy;  
        // Marca de tiempo del último cambio de dirección
        long lastDirectionChange; 
        // Intervalo entre cambios de dirección
        int changeInterval; 
        // Velocidad base del bloque
        int velocity = 4; 

        // Constructor del bloque
        Block(Image image, int x, int y, int width, int height) {
            // Asigna la imagen del bloque
            this.image = image;
            // Asigna posición x
            this.x = x;
            // Asigna posición y
            this.y = y;
            // Asigna ancho
            this.width = width;
            // Asigna alto
            this.height = height;
            // Guarda posición inicial x
            this.startX = x;
            // Guarda posición inicial y
            this.startY = y;
            
            // Asigna estrategia de movimiento aleatoria (0, 1 o 2)
            this.movementStrategy = random.nextInt(3); 
            // Registra tiempo actual como último cambio de dirección
            this.lastDirectionChange = System.currentTimeMillis();
            // Establece intervalo aleatorio entre 1000 y 3000 ms
            this.changeInterval = 1000 + random.nextInt(2000); 
        }
        
        // Método para actualizar la velocidad según la dirección actual
        void updateVelocity() {
            switch (direction) {
                case 'U': // Si dirección es arriba
                    velocityX = 0;      // No hay movimiento horizontal
                    velocityY = -velocity; // Movimiento vertical negativo (arriba)
                    break;
                case 'D': // Si dirección es abajo
                    velocityX = 0;      // No hay movimiento horizontal
                    velocityY = velocity; // Movimiento vertical positivo (abajo)
                    break;
                case 'L': // Si dirección es izquierda
                    velocityX = -velocity; // Movimiento horizontal negativo (izquierda)
                    velocityY = 0;      // No hay movimiento vertical
                    break;
                case 'R':  // Si dirección es derecha
                    velocityX = velocity; // Movimiento horizontal positivo (derecha)
                    velocityY = 0;      // No hay movimiento vertical
                    break;
            }
        }
         
        // Método para reiniciar el bloque a su posición y estado inicial
        void reset() {
            // Restablece posición x
            this.x = this.startX;
            // Restablece posición y
            this.y = this.startY;
            // Restablece dirección a derecha
            this.direction = 'R';
            // Restablece siguiente dirección a derecha
            this.nextDirection = 'R';
            // Asigna nueva estrategia de movimiento aleatoria
            this.movementStrategy = random.nextInt(3);
            // Registra tiempo actual como último cambio
            this.lastDirectionChange = System.currentTimeMillis();
            // Establece nuevo intervalo aleatorio
            this.changeInterval = 1000 + random.nextInt(2000);
            // Actualiza velocidad según dirección
            updateVelocity();
        }
    }

    // Número de filas en el tablero
    private int rowCount = 21; 
    // Número de columnas en el tablero
    private int columnCount = 19; 
    // Tamaño en píxeles de cada celda del tablero
    private int tileSize = 32; 
    // Ancho total del tablero en píxeles
    private int boardWidth = columnCount * tileSize; 
    // Alto total del tablero en píxeles
    private int boardHeight = rowCount * tileSize; 
    
    // Imagen de las paredes del laberinto
    private Image wallImage; 
    // Imagen del fantasma azul
    private Image blueGhostImage; 
    // Imagen del fantasma naranja
    private Image orangeGhostImage; 
    // Imagen del fantasma rosa
    private Image pinkGhostImage; 
    // Imagen del fantasma rojo
    private Image redGhostImage;  
    // Imagen de Pac-Man mirando arriba
    private Image pacmanUpImage;
    // Imagen de Pac-Man mirando abajo
    private Image pacmanDownImage; 
    // Imagen de Pac-Man mirando izquierda
    private Image pacmanLeftImage; 
    // Imagen de Pac-Man mirando derecha
    private Image pacmanRightImage; 
    // Imagen de fondo para el menú
    private Image menuBackground; 
    // Imagen del logo del juego
    private Image logoImage; 
    
    // Gestor de sonido para el movimiento
    private SoundManager moveSound = new SoundManager(); 
    // Gestor de sonido para la muerte
    private SoundManager dieSound = new SoundManager();  
    // Referencia al sonido actual que se está reproduciendo
    private SoundManager currentSound = null; 
    
    // Mapa del juego representado como un array de Strings
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX", // X = Pared
        "X        X        X", // Espacio = Comida
        "X XX XXX X XXX XX X",  // P = Posición inicial de Pac-Man
        "X                 X", // b = Fantasma azul
        "X XX X XXXXX X XX X", // o = Fantasma naranja
        "X    X       X    X", // p = Fantasma rosa
        "XXXX XXXX XXXX XXXX", // r = Fantasma rojo
        "OOOX X       X XOOO",  // O = Comida especial (no implementada)
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
    
    // Conjunto que almacena todos los bloques de pared
    HashSet<Block> walls; 
    // Conjunto que almacena todos los bloques de comida
    HashSet<Block> foods; 
    // Conjunto que almacena todos los bloques de fantasmas
    HashSet<Block> ghosts; 
    // Bloque que representa al jugador (Pac-Man)
    Block pacman; 

    // Temporizador para el bucle principal del juego (~60 FPS)
    Timer gameLoop; 
    // Temporizador para la animación de muerte
    Timer deathTimer;  
    // Array con las posibles direcciones (Arriba, Abajo, Izquierda, Derecha)
    char[] directions = {'U', 'D', 'L', 'R'};
    // Objeto para generar números aleatorios
    Random random = new Random(); 
    
    // Puntuación actual del jugador
    int score = 0;  
    // Vidas restantes del jugador
    int lives = 3; 
    // Indica si se debe mostrar a Pac-Man (para animación de muerte)
    boolean showPacman = true; 
    
    // Botón para iniciar el juego
    private JButton startButton; 
    // Botón para seleccionar color
    private JButton colorSelectionButton; 
    // Botón para reiniciar el nivel
    private JButton restartButton; 
    // Botón para volver al menú principal
    private JButton menuButton; 
    // Botón para continuar el juego
    private JButton resumeButton; 
    // Botón para salir del juego
    private JButton exitButton; 
    // Botón alternativo para salir del juego
    private JButton exiteButton; 
    // Botón para mostrar controles
    private JButton controlsButton; 
    // Botón para mostrar información del juego
    private JButton aboutButton; 

    // Fuente personalizada grande
    private Font customFontLarge;
    // Fuente personalizada mediana
    private Font customFontMedium;
    // Fuente personalizada pequeña
    private Font customFontSmall;  
    // Fuente alternativa Arial Black
    private Font arialBlackFont;

    // =============================================
    // CONSTRUCTOR PRINCIPAL
    // =============================================
    public PacMan() {
        // Establece el tamaño preferido del panel
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // Establece el color de fondo como negro
        setBackground(Color.BLACK);
        // Añade este panel como escuchador de eventos de teclado
        addKeyListener(this); 
        // Permite que el panel reciba eventos de teclado
        setFocusable(true); 
        // Establece layout manual (sin administrador de diseño)
        setLayout(null); 

        // Intenta cargar fuentes personalizadas
        try {
            // Obtiene el flujo de entrada para el archivo de fuente
            InputStream is = getClass().getResourceAsStream("./PAC-FONT.TTF");
            // Crea la fuente grande a partir del archivo TTF
            customFontLarge = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(40f);
            // Deriva la fuente mediana de la grande con tamaño 20
            customFontMedium = customFontLarge.deriveFont(20f);
            // Deriva la fuente pequeña de la grande con tamaño 20
            customFontSmall = customFontLarge.deriveFont(20f);
            
            // Obtiene el entorno gráfico local
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            // Registra la fuente personalizada en el sistema
            ge.registerFont(customFontLarge);
        } catch (Exception e) {
            // Si hay error, usa fuentes por defecto
            System.err.println("Error cargando fuente personalizada: " + e.getMessage());
            customFontLarge = new Font("Arial Black", Font.BOLD, 48);
            customFontMedium = new Font("Arial Black", Font.BOLD, 24);
            customFontSmall = new Font("Arial Black", Font.BOLD, 18);
        }
        
        // Crea fuente Arial Black tamaño 20
        arialBlackFont = new Font("Arial Black", Font.BOLD, 20);

        // Carga las imágenes iniciales (color amarillo por defecto)
        loadImages("amarillo");
        
        // Configura todos los botones de la interfaz
        setupButtons();
        
        // Muestra solo los botones del menú principal al inicio
        showMainMenuButtons(true);
        
        // Crea el temporizador del juego (16ms ≈ 60 FPS)
        gameLoop = new Timer(16, this); 
        // Crea el temporizador para la animación de muerte
        deathTimer = new Timer(1000, e -> {
            // Detiene el temporizador de muerte
            deathTimer.stop();
            // Si no quedan vidas
            if (lives <= 0) {
                // Muestra pantalla de fin de juego
                gameOver(); 
            } else {
                // Reinicia posiciones de los personajes
                resetPositions(); 
                // Cambia estado a jugando
                gameState = GameState.PLAYING;
                // Muestra a Pac-Man
                showPacman = true;
                // Reanuda el juego
                gameLoop.start(); 
                // Reproduce sonido de movimiento
                startMoveSound(); 
            }
            // Vuelve a dibujar el panel
            repaint(); 
        });
    }

    // =============================================
    // MÉTODOS DE CARGA DE RECURSOS
    // =============================================

    // Carga las imágenes según el color seleccionado para Pac-Man
    private void loadImages(String color) {
        try {
            // Carga imagen de las paredes
            wallImage = ImageIO.read(getClass().getResource("./wall.png"));
            // Carga imagen del fantasma azul
            blueGhostImage = ImageIO.read(getClass().getResource("./blueGhost.png"));
            // Carga imagen del fantasma naranja
            orangeGhostImage = ImageIO.read(getClass().getResource("./orangeGhost.png"));
            // Carga imagen del fantasma rosa
            pinkGhostImage = ImageIO.read(getClass().getResource("./pinkGhost.png"));
            // Carga imagen del fantasma rojo
            redGhostImage = ImageIO.read(getClass().getResource("./redGhost.png"));

            // Carga imágenes de Pac-Man según el color seleccionado
            pacmanUpImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanUp.png"));
            pacmanDownImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanDown.png"));
            pacmanLeftImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanLeft.png"));
            pacmanRightImage = ImageIO.read(getClass().getResource("./pacman_" + color + "/pacmanRight.png"));
            
            // Carga y escala la imagen de fondo del menú
            Image rawImage = ImageIO.read(getClass().getResource("./menu_background.png"));
            // Crea una imagen en memoria del tamaño del tablero
            BufferedImage scaledImage = new BufferedImage(boardWidth, boardHeight, BufferedImage.TYPE_INT_ARGB);
            // Obtiene el contexto gráfico para dibujar
            Graphics2D g2d = scaledImage.createGraphics();
            // Establece interpolación de alta calidad
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            // Dibuja la imagen original escalada al tamaño del tablero
            g2d.drawImage(rawImage, 0, 0, boardWidth, boardHeight, null);
            // Libera recursos
            g2d.dispose();
            // Asigna la imagen escalada como fondo del menú
            menuBackground = scaledImage;
            // Carga la imagen del logo
            logoImage = ImageIO.read(getClass().getResource("./logo.png"));
        } catch (Exception e) {
            // Si hay error, muestra mensaje y traza de error
            System.err.println("Error cargando imágenes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =============================================
    // CONFIGURACIÓN DE INTERFAZ DE USUARIO
    // =============================================
    private void setupButtons() {
        // Color amarillo para los botones
        Color buttonColor = new Color(255, 255, 0);
        // Color azul oscuro para los bordes
        Color borderColor = new Color(33, 33, 222);

        // Configuración del botón de Iniciar Juego
        startButton = new JButton("INICIAR JUEGO");
        // Posición y tamaño del botón (centrado horizontalmente)
        startButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 30, 300, 50);
        // Acción al hacer clic: muestra selección de color
        startButton.addActionListener(e -> showColorSelection());
        // Color de fondo amarillo
        startButton.setBackground(buttonColor);
        // Color de texto negro
        startButton.setForeground(Color.BLACK);
        // Fuente mediana personalizada
        startButton.setFont(customFontMedium);
        // Borde azul de 3 píxeles
        startButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        // Sin efecto visual al tener foco
        startButton.setFocusPainted(false);
        // Añade el botón al panel
        add(startButton);

        // Configuración del botón de Selección de Color
        colorSelectionButton = new JButton("SELECCION COLOR");
        // Misma posición que el botón de inicio
        colorSelectionButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 30, 300, 50);
        // Acción al hacer clic: muestra opciones de color
        colorSelectionButton.addActionListener(e -> showColorOptions());
        // Mismos colores y estilo que el botón de inicio
        colorSelectionButton.setBackground(buttonColor);
        colorSelectionButton.setForeground(Color.BLACK);
        colorSelectionButton.setFont(customFontMedium);
        colorSelectionButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        colorSelectionButton.setFocusPainted(false);
        // Inicialmente oculto
        colorSelectionButton.setVisible(false);
        // Añade el botón al panel
        add(colorSelectionButton);

        // Configuración del botón de Reiniciar Nivel
        restartButton = new JButton("REINICIAR NIVEL");
        // Posición ligeramente más arriba que el botón de inicio
        restartButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 10, 300, 50);
        // Acción al hacer clic: reinicia el nivel
        restartButton.addActionListener(e -> restartLevel());
        // Mismos colores y estilo
        restartButton.setBackground(buttonColor);
        restartButton.setForeground(Color.BLACK);
        restartButton.setFont(customFontMedium);
        restartButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        restartButton.setFocusPainted(false);
        // Añade el botón al panel
        add(restartButton);

        // Configuración del botón de Menú Principal
        menuButton = new JButton("MENU PRINCIPAL");
        // Posición debajo del botón de reinicio
        menuButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 80, 300, 50);
        // Acción al hacer clic: muestra el menú principal
        menuButton.addActionListener(e -> showMainMenu());
        // Mismos colores y estilo
        menuButton.setBackground(buttonColor);
        menuButton.setForeground(Color.BLACK);
        menuButton.setFont(customFontMedium);
        menuButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        menuButton.setFocusPainted(false);
        // Añade el botón al panel
        add(menuButton);

        // Configuración del botón de Continuar
        resumeButton = new JButton("CONTINUAR");
        // Posición más arriba que el botón de inicio
        resumeButton.setBounds(boardWidth/2 - 150, boardHeight/2 - 60, 300, 50);
        // Acción al hacer clic: alterna pausa
        resumeButton.addActionListener(e -> togglePause());
        // Mismos colores y estilo
        resumeButton.setBackground(buttonColor);
        resumeButton.setForeground(Color.BLACK);
        resumeButton.setFont(customFontMedium);
        resumeButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        resumeButton.setFocusPainted(false);
        // Añade el botón al panel
        add(resumeButton);

        // Configuración del botón de Controles
        controlsButton = new JButton("CONTROLES");
        // Posición debajo del botón de inicio
        controlsButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 90, 300, 40);
        // Color de fondo amarillo
        controlsButton.setBackground(buttonColor);
        // Color de texto negro
        controlsButton.setForeground(Color.BLACK);
        // Fuente pequeña personalizada
        controlsButton.setFont(customFontSmall);
        // Borde azul de 2 píxeles
        controlsButton.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        // Sin efecto visual al tener foco
        controlsButton.setFocusPainted(false);
        // Acción al hacer clic: muestra controles
        controlsButton.addActionListener(e -> showControls());
        // Añade el botón al panel
        add(controlsButton);

        // Configuración del botón de Info del Juego
        aboutButton = new JButton("INFO DEL JUEGO");
        // Posición debajo del botón de controles
        aboutButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 140, 300, 40);
        // Mismos colores y estilo que el botón de controles
        aboutButton.setBackground(buttonColor);
        aboutButton.setForeground(Color.BLACK);
        aboutButton.setFont(customFontSmall);
        aboutButton.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        aboutButton.setFocusPainted(false);
        // Acción al hacer clic: muestra información
        aboutButton.addActionListener(e -> showAbout());
        // Añade el botón al panel
        add(aboutButton);

        // Configuración del botón de Salir del Juego
        exitButton = new JButton("SALIR DEL JUEGO");
        // Posición debajo del botón de información
        exitButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 190, 300, 50);
        // Acción al hacer clic: sale del juego
        exitButton.addActionListener(e -> exitGame());
        // Mismos colores y estilo que los botones grandes
        exitButton.setBackground(buttonColor);
        exitButton.setForeground(Color.BLACK);
        exitButton.setFont(customFontMedium);
        exitButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        exitButton.setFocusPainted(false);
        // Añade el botón al panel
        add(exitButton);

        // Configuración del botón alternativo de Salir del Juego
        exiteButton = new JButton("SALIR DEL JUEGO");
        // Posición diferente al otro botón de salir
        exiteButton.setBounds(boardWidth/2 - 150, boardHeight/2 + 150, 300, 50);
        // Misma acción que el otro botón de salir
        exiteButton.addActionListener(e -> exitGame());
        // Mismos colores y estilo
        exiteButton.setBackground(buttonColor);
        exiteButton.setForeground(Color.BLACK);
        exiteButton.setFont(customFontMedium);
        exiteButton.setBorder(BorderFactory.createLineBorder(borderColor, 3));
        exiteButton.setFocusPainted(false);
        // Añade el botón al panel
        add(exiteButton);
    }

    // =============================================
    // MÉTODOS DE MANEJO DE MENÚS
    // =============================================
    
    // Muestra u oculta los botones del menú principal según el parámetro
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

    // Muestra la selección de color (oculta inicio, muestra selección color)
    private void showColorSelection() {
        startButton.setVisible(false);
        colorSelectionButton.setVisible(true);
        gameState = GameState.COLOR_SELECTION;
        repaint();
    }

    // Muestra una ventana emergente con las opciones de color para Pac-Man
    private void showColorOptions() {
        // Crea una nueva ventana para selección de color
        JFrame colorFrame = new JFrame("Selecciona el color de tu Pac-Man");
        // Configura para que se cierre solo esta ventana al salir
        colorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Tamaño de la ventana (650x400 píxeles)
        colorFrame.setSize(650, 400); 
        // Layout de rejilla 2x4 con espacio de 15 píxeles
        colorFrame.setLayout(new GridLayout(2, 4, 15, 15));
        
        // Centra la ventana en la pantalla
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - colorFrame.getWidth()) / 2;
        int y = (screenSize.height - colorFrame.getHeight()) / 2;
        colorFrame.setLocation(x, y);
        
        // Colores disponibles para Pac-Man
        String[] colors = {"amarillo", "azul", "negro", "celeste", "rojo", "rosa", "verde"};
        // Valores de color correspondientes
        Color[] colorValues = {Color.YELLOW, Color.BLUE, Color.BLACK, Color.CYAN, Color.RED, Color.PINK, Color.GREEN};

        // Fuente para los botones de color
        Font colorButtonFont = new Font("Arial", Font.BOLD, 14);

        // Crea un botón para cada color disponible
        for (int i = 0; i < colors.length; i++) {
            // Crea un botón personalizado para cada color
            JButton colorButton = new JButton(colors[i].toUpperCase()) {
                @Override
                protected void paintComponent(Graphics g) {
                    // Obtiene contexto gráfico 2D
                    Graphics2D g2 = (Graphics2D) g.create();
                    // Rellena el fondo con el color del botón
                    g2.setColor(getBackground());
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Dibuja un fondo blanco para el texto
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
                    
                    // Dibuja el texto en negro sobre el fondo blanco
                    g2.setColor(Color.BLACK);
                    g2.drawString(text, (getWidth() - textWidth)/2, 
                                (getHeight() - fm.getHeight())/2 + fm.getAscent());
                    g2.dispose();
                }
            };
            
            // Establece el color de fondo según el color correspondiente
            colorButton.setBackground(colorValues[i]);
            // Establece la fuente
            colorButton.setFont(colorButtonFont);
            // Sin borde (se dibuja manualmente)
            colorButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            // Sin efecto visual al tener foco
            colorButton.setFocusPainted(false);
            // Acción al hacer clic: carga imágenes con ese color y cierra la ventana
            colorButton.addActionListener(e -> {
                loadImages(colors[getIndex(colorButton.getText().toLowerCase(), colors)]);
                colorFrame.dispose();
                startGameAfterColorSelection();
            });
            
            // Añade el botón a la ventana
            colorFrame.add(colorButton);
        }

        // Impide redimensionar la ventana
        colorFrame.setResizable(false);
        // Hace visible la ventana
        colorFrame.setVisible(true);
    }

    // =============================================
    // MÉTODOS DE INICIO Y CONTROL DEL JUEGO
    // =============================================
    
    // Obtiene el índice de un texto en un array de strings
    private int getIndex(String text, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(text)) {
                return i;
            }
        }
        return 0;
    }

    // Inicia el juego después de seleccionar un color
    private void startGameAfterColorSelection() {
        colorSelectionButton.setVisible(false);
        startGame();
    }

    // Inicia el juego principal
    private void startGame() {
        // Carga el mapa del juego
        loadMap();
        // Reinicia posiciones de los personajes
        resetPositions();
        // Establece estado de juego
        gameState = GameState.PLAYING;
        
        // Oculta botones no necesarios durante el juego
        startButton.setVisible(false);
        exitButton.setVisible(false);
        exiteButton.setVisible(false);
        restartButton.setVisible(false);
        menuButton.setVisible(false);
        resumeButton.setVisible(false);
        controlsButton.setVisible(false); 
        aboutButton.setVisible(false);    

        // Reinicia valores del juego
        score = 0;
        lives = 3;
        showPacman = true;
        
        // Inicia el bucle del juego
        gameLoop.start();
        // Establece foco en el panel para recibir eventos de teclado
        requestFocus();
        // Reproduce sonido de movimiento
        startMoveSound();
    }

    // Muestra el menú principal
    private void showMainMenu() {
        gameState = GameState.MAIN_MENU;
        gameLoop.stop();
        showMainMenuButtons(true);
        stopAllSounds();
        repaint();
    }

    // =============================================
    // MÉTODOS DE MOVIMIENTO Y LÓGICA DEL JUEGO
    // =============================================
    
    // Método principal de movimiento que se ejecuta en cada frame
    public void move() {
        // Solo se mueve si el estado es PLAYING
        if (gameState != GameState.PLAYING) return;
        
        // Mueve a Pac-Man
        movePacman(); 
        // Mueve los fantasmas
        moveGhosts(); 
        // Verifica si Pac-Man ha comido comida
        checkFoodCollision(); 
    }

    // Mueve a Pac-Man según su dirección actual
    private void movePacman() {
        // Intenta cambiar de dirección si hay una solicitud diferente a la actual
        if (pacman.nextDirection != pacman.direction) {
            tryChangeDirection();
        }
        // Guarda la posición anterior
        int oldX = pacman.x;
        int oldY = pacman.y;
        // Actualiza la posición según la velocidad
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        // Verifica colisión con paredes
        if (checkWallCollision(pacman)) {
            // Si hay colisión, revierte el movimiento
            pacman.x = oldX;
            pacman.y = oldY;
        }
        // Verifica si debe teletransportarse al otro lado del tablero
        checkBoardBounds(pacman);
    }

    // Intenta cambiar la dirección de Pac-Man
    private void tryChangeDirection() {
        // Guarda el estado actual temporalmente
        int tempX = pacman.x;
        int tempY = pacman.y;
        char tempDirection = pacman.direction;
        int tempVX = pacman.velocityX;
        int tempVY = pacman.velocityY;
        
        // Prueba la nueva dirección
        pacman.direction = pacman.nextDirection;
        pacman.updateVelocity();
        
        // Simula el movimiento
        tempX += pacman.velocityX;
        tempY += pacman.velocityY;
        
        // Crea un Pac-Man temporal para verificar colisión
        Block tempPacman = new Block(null, tempX, tempY, pacman.width, pacman.height);
        
        // Si no hay colisión con la nueva dirección
        if (!checkWallCollision(tempPacman)) {
            // Acepta el cambio de dirección
            pacman.updateVelocity();
        } else {
            // Si hay colisión, revierte al estado anterior
            pacman.direction = tempDirection;
            pacman.velocityX = tempVX;
            pacman.velocityY = tempVY;
        }
    }

    // Verifica si un bloque colisiona con alguna pared
    private boolean checkWallCollision(Block block) {
        for (Block wall : walls) {
            if (collision(block, wall)) {
                return true;
            }
        }
        return false;
    }

    // Verifica los bordes del tablero para teletransporte
    private void checkBoardBounds(Block entity) {
        // Si se sale por la izquierda
        if (entity.x < -entity.width) {
            entity.x = boardWidth; // Aparece por la derecha
        } 
        // Si se sale por la derecha
        else if (entity.x > boardWidth) {
            entity.x = -entity.width; // Aparece por la izquierda
        }
    }

    // Versión especial de teletransporte para fantasmas
    private void checkGhostBoardBounds(Block ghost) {
        // Teletransporte normal como Pac-Man
        if (ghost.x < -ghost.width) {
            ghost.x = boardWidth;
        } else if (ghost.x > boardWidth) {
            ghost.x = -ghost.width;
        }
        
        // 10% de probabilidad de cambiar dirección después del teletransporte
        if (random.nextInt(100) < 10) {
            ghost.direction = directions[random.nextInt(4)];
            ghost.updateVelocity();
        }
    }

    // Mueve todos los fantasmas del juego
    private void moveGhosts() {
        long currentTime = System.currentTimeMillis();
        
        for (Block ghost : ghosts) {
            // Guarda posición anterior
            int oldX = ghost.x;
            int oldY = ghost.y;
            
            // Mueve el fantasma según su velocidad
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            // Aplica teletransporte si llega a los bordes
            checkGhostBoardBounds(ghost);
            
            // Verifica colisión con paredes
            boolean collided = checkWallCollision(ghost);
            
            if (collided) {
                // Revertir movimiento si hay colisión
                ghost.x = oldX;
                ghost.y = oldY;
                
                // Elegir nueva dirección según personalidad del fantasma
                char newDirection = chooseNewDirection(ghost, oldX, oldY);
                ghost.direction = newDirection;
                ghost.updateVelocity();
            }
            
            // Cambio de dirección periódico (aunque no haya colisión)
            if (currentTime - ghost.lastDirectionChange > ghost.changeInterval) {
                ghost.direction = chooseNewDirection(ghost, ghost.x, ghost.y);
                ghost.updateVelocity();
                ghost.lastDirectionChange = currentTime;
                ghost.changeInterval = 1000 + random.nextInt(2000); // 1-3 segundos
            }
            
            // Verifica colisión con Pac-Man
            if (collision(ghost, pacman) && gameState == GameState.PLAYING) {
                playerDied();
            }
        }
    }

    // Elige una nueva dirección para un fantasma
    private char chooseNewDirection(Block ghost, int currentX, int currentY) {
        // Lista de direcciones posibles (sin colisión inmediata)
        List<Character> possibleDirections = new ArrayList<>();
        
        // Prueba cada dirección posible
        for (char dir : new char[]{'U', 'D', 'L', 'R'}) {
            Block temp = new Block(null, currentX, currentY, ghost.width, ghost.height);
            temp.direction = dir;
            temp.updateVelocity();
            temp.x += temp.velocityX;
            temp.y += temp.velocityY;
            
            // Si no hay colisión y está dentro de los bordes
            if (!checkWallCollision(temp) && 
                temp.x > 0 && 
                temp.x + temp.width < boardWidth) {
                possibleDirections.add(dir);
            }
        }
        
        // Si no hay direcciones posibles, elige aleatoria
        if (possibleDirections.isEmpty()) {
            return directions[random.nextInt(4)];
        }
        
        // Decide según la estrategia de movimiento del fantasma
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

    // Intenta perseguir a Pac-Man eligiendo la mejor dirección posible
    private char chasePacMan(Block ghost, int currentX, int currentY, List<Character> possibleDirections) {
        // Calcula diferencias con posición de Pac-Man
        int dx = pacman.x - currentX;
        int dy = pacman.y - currentY;
        
        // Determina direcciones preferidas
        char preferredHorizontal = dx > 0 ? 'R' : 'L';
        char preferredVertical = dy > 0 ? 'D' : 'U';
        
        // Prioriza la dirección con mayor diferencia
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
        
        // Si no puede perseguir directamente, elige aleatoria
        return possibleDirections.get(random.nextInt(possibleDirections.size()));
    }

    // Maneja la muerte del jugador
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

    // Maneja el fin del juego
    private void gameOver() {
        gameState = GameState.GAME_OVER;
        gameLoop.stop();
        stopAllSounds();
        showGameoverButtons(true);
        repaint();
    }

    // Alterna entre pausa y reanudación del juego
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

    // Sale del juego
    private void exitGame() {
        stopAllSounds();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        System.exit(0);
    }

    // Verifica si Pac-Man ha comido comida
    private void checkFoodCollision() {
        HashSet<Block> eatenFood = new HashSet<>();
        for (Block food : foods) {
            if (collision(pacman, food)) {
                eatenFood.add(food);
                score += 10;
            }
        }
        foods.removeAll(eatenFood);
        
        // Si no queda comida, recarga el mapa
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    // Verifica colisión entre dos bloques
    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    // Reinicia las posiciones de Pac-Man y los fantasmas
    public void resetPositions() {
        pacman.reset();
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.direction = directions[random.nextInt(4)];
            ghost.updateVelocity();
        }
    }

    // Reinicia el nivel completo
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

    // Muestra un diálogo con los controles del juego
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

    // Muestra un diálogo con información sobre el juego
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

    // =============================================
    // MÉTODOS PARA MANEJO DE SONIDOS
    // =============================================

    // Inicia el sonido de movimiento
    private void startMoveSound() {
        if (gameState == GameState.PLAYING) {
            stopAllSounds();
            moveSound.playSound("./move.wav", true);
            currentSound = moveSound;
        }
    }

    // Detiene el sonido de movimiento
    private void stopMoveSound() {
        if (currentSound == moveSound) {
            moveSound.stop();
            currentSound = null;
        }
    }

    // Reproduce un sonido (con opción de loop)
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

    // Detiene todos los sonidos
    private void stopAllSounds() {
        moveSound.stop();
        dieSound.stop();
        currentSound = null;
    }

    // =============================================
    // MÉTODOS DE DIBUJO
    // =============================================

    // Método principal de dibujo del juego
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Dibuja según el estado actual del juego
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

    // Dibuja el menú principal
    private void drawMainMenu(Graphics g) {
        // Dibuja el fondo del menú
        g.drawImage(menuBackground, 0, 0, boardWidth, boardHeight, this);
        
        // Obtiene dimensiones del logo
        int logoWidth = logoImage.getWidth(null);
        int logoHeight = logoImage.getHeight(null);
        // Dibuja el logo centrado horizontalmente
        g.drawImage(logoImage, (boardWidth - logoWidth)/2, boardHeight/4 - logoHeight/2, null);
        
        // Dibuja el título del juego
        g.setColor(Color.YELLOW);
        g.setFont(customFontLarge);
        String title = "PAC-MAN ERIKIKI";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (boardWidth - titleWidth)/2, boardHeight/3 + 50);
    }

    // Dibuja el juego principal
    private void drawGame(Graphics g) {
        // Fondo negro
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        // Dibuja a Pac-Man si está visible y no está en estado de muerte
        if (showPacman && gameState != GameState.PLAYER_DIED) {
            switch (pacman.direction) {
                case 'U': g.drawImage(pacmanUpImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'D': g.drawImage(pacmanDownImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'L': g.drawImage(pacmanLeftImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
                case 'R': g.drawImage(pacmanRightImage, pacman.x, pacman.y, pacman.width, pacman.height, null); break;
            }
        }

        // Dibuja todos los fantasmas
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Dibuja todas las paredes
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Dibuja toda la comida
        g.setColor(Color.orange);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Dibuja la información de vidas y puntuación
        g.setColor(Color.GREEN);
        g.setFont(customFontSmall);
        String livesText = "x";
        g.drawString(livesText, tileSize/2, tileSize/2);
        
        g.setFont(arialBlackFont);
        String numbers = lives + " SCORE: " + score;
        int textWidth = g.getFontMetrics().stringWidth(numbers);
        g.drawString(numbers, tileSize/2 + g.getFontMetrics(customFontSmall).stringWidth(livesText), tileSize/2);
        
        // Si el jugador acaba de morir, muestra mensaje
        if (gameState == GameState.PLAYER_DIED) {
            g.setColor(Color.RED);
            g.setFont(arialBlackFont);
            String deathText = "PERDISTE UNA VIDA";
            textWidth = g.getFontMetrics().stringWidth(deathText);
            g.drawString(deathText, (boardWidth - textWidth)/2, boardHeight/2);
        }
    }

    // Dibuja la pantalla de pausa
    private void drawPauseScreen(Graphics g) {
        // Fondo semitransparente
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        // Texto "PAUSA"
        g.setColor(Color.YELLOW);
        g.setFont(customFontLarge);
        String pauseText = "PAUSA";
        int textWidth = g.getFontMetrics().stringWidth(pauseText);
        g.drawString(pauseText, (boardWidth - textWidth)/2, boardHeight/2 - 120);
        
        // Instrucción para continuar
        g.setColor(Color.WHITE);
        g.setFont(customFontMedium);
        String instruction = "Presiona P para continuar";
        textWidth = g.getFontMetrics().stringWidth(instruction);
        g.drawString(instruction, (boardWidth - textWidth)/2, boardHeight/2 + 260);
        
        // Muestra botones relevantes para pausa
        showGameButtons(true);
    }

    // Dibuja la pantalla de fin de juego
    private void drawGameOver(Graphics g) {
        // Fondo semitransparente
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        // Texto "GAME OVER"
        g.setColor(Color.RED);
        g.setFont(customFontLarge);
        String gameOverText = "GAME OVER";
        int textWidth = g.getFontMetrics().stringWidth(gameOverText);
        g.drawString(gameOverText, (boardWidth - textWidth)/2, boardHeight/2 - 80);
        
        // Puntuación
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
        
        // Muestra botones relevantes para fin de juego
        showGameoverButtons(true);
    }

    // Muestra u oculta botones durante el juego
    private void showGameButtons(boolean show) {
        startButton.setVisible(false);
        exiteButton.setVisible(show);
        restartButton.setVisible(show);
        menuButton.setVisible(show);
        resumeButton.setVisible(show);
    }

    // Muestra u oculta botones en fin de juego
    private void showGameoverButtons(boolean show) {
        startButton.setVisible(false);
        exiteButton.setVisible(show);
        restartButton.setVisible(show);
        menuButton.setVisible(show);
        resumeButton.setVisible(!show);
    }

    // Carga el mapa del juego desde tileMap
    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        // Recorre cada celda del mapa
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileMapChar = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                // Crea el elemento correspondiente según el carácter
                switch (tileMapChar) {
                    case 'X': // Pared
                        walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                        break;
                    case 'b': // Fantasma azul
                        ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'o': // Fantasma naranja
                        ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'p': // Fantasma rosa
                        ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'r': // Fantasma rojo
                        ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'P': // Pac-Man
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        break;
                    case ' ': // Comida
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                }
            }
        }
    }

    // =============================================
    // MANEJADORES DE EVENTOS
    // =============================================

    // Se ejecuta en cada tick del temporizador del juego
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            move();
        }
        repaint();
    }

    // Maneja eventos de teclas presionadas
    @Override
    public void keyPressed(KeyEvent e) {
        // Si está en game over, Enter reinicia el juego
        if (gameState == GameState.GAME_OVER) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                restartLevel();
            }
            return;
        }

        // Maneja diferentes teclas según el código
        switch (e.getKeyCode()) {
            case KeyEvent.VK_P: // Tecla P: pausa/reanuda
                if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
                    togglePause();
                }
                break;
            case KeyEvent.VK_UP: // Flecha arriba
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'U';
                break;
            case KeyEvent.VK_DOWN: // Flecha abajo
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'D';
                break;
            case KeyEvent.VK_LEFT: // Flecha izquierda
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'L';
                break;
            case KeyEvent.VK_RIGHT: // Flecha derecha
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'R';
                break;
            case KeyEvent.VK_W: // Tecla W (arriba)
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'U';
                break;
            case KeyEvent.VK_S: // Tecla S (abajo)
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'D';
                break;
            case KeyEvent.VK_A: // Tecla A (izquierda)
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'L';
                break;
            case KeyEvent.VK_D: // Tecla D (derecha)
                if (gameState == GameState.PLAYING) pacman.nextDirection = 'R';
                break;
            case KeyEvent.VK_ESCAPE: // Tecla ESC
                if (gameState == GameState.PLAYING) {
                    togglePause();
                } else if (gameState == GameState.PAUSED) {
                    showMainMenu();
                }
                break;
        }
        
        // Intenta cambiar de dirección si está jugando
        if (gameState == GameState.PLAYING) {
            tryChangeDirection();
        }
    }

    // Métodos no utilizados de la interfaz KeyListener
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    // =============================================
    // MÉTODO MAIN - PUNTO DE ENTRADA
    // =============================================

    public static void main(String[] args) {
        // Crea la ventana principal
        JFrame frame = new JFrame("PAC-MAN ERIKIKI");
        // Configura para que se cierre la aplicación al cerrar la ventana
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Impide redimensionar la ventana
        frame.setResizable(false);
        // Muestra la decoración de ventana (bordes, título)
        frame.setUndecorated(false);
        
        // Crea una instancia del juego
        PacMan game = new PacMan();
        // Añade el juego a la ventana
        frame.add(game);
        // Ajusta el tamaño de la ventana al contenido
        frame.pack();
        // Centra la ventana en la pantalla
        frame.setLocationRelativeTo(null);
        // Hace visible la ventana
        frame.setVisible(true);
        // Establece foco en el juego para recibir eventos de teclado
        game.requestFocus();
        // Establece el icono de la ventana
        frame.setIconImage(new ImageIcon(PacMan.class.getResource("icon.png")).getImage());
    }
}

// Clase para manejar la reproducción de sonidos
class SoundManager {
    // Clip de audio que se está reproduciendo
    private Clip clip;
    // Indica si el sonido debe repetirse
    private boolean isLooping = false;

    // Reproduce un sonido desde una ruta
    public void playSound(String path, boolean loop) {
        // Detiene cualquier sonido previo
        stop();
        isLooping = loop;
        
        try {
            // Intenta cargar el sonido como recurso interno
            InputStream audioSrc = getClass().getResourceAsStream(path);
            if (audioSrc == null) {
                // Si no está como recurso, carga como archivo externo
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            } else {
                // Carga el recurso interno
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioSrc);
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            }
            
            // Reproduce en bucle o una sola vez según el parámetro
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
        }
    }

    // Detiene la reproducción del sonido
    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    // Verifica si hay un sonido reproduciéndose
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
}