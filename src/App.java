import javax.swing.JFrame;
import javax.swing.ImageIcon;

public class App {
    public static void main(String[] args) throws Exception {
        // Configuración del tablero
        int rowCount = 21;       // Número de filas del tablero
        int columnCount = 19;    // Número de columnas del tablero
        int tileSize = 32;       // Tamaño de cada celda en píxeles
        int boardWidth = columnCount * tileSize;  // Ancho total del tablero
        int boardHeight = rowCount * tileSize;   // Alto total del tablero

        // Creación de la ventana principal
        JFrame frame = new JFrame("PAC MAN ERIKIKI");
        
        // Establecer ícono de la ventana
        frame.setIconImage(new ImageIcon(App.class.getResource("/icon.png")).getImage());

        // Configuración de la ventana
        frame.setSize(boardWidth, boardHeight);  // Tamaño de la ventana
        frame.setLocationRelativeTo(null);       // Centrar en pantalla
        frame.setResizable(false);               // No redimensionable
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Cerrar aplicación al salir

        // Crear e iniciar el juego
        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);      // Añadir el panel del juego a la ventana
        frame.pack();               // Ajustar tamaño de la ventana
        frame.setVisible(true);     // Mostrar ventana
    }
}