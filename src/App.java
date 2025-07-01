import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        //paso 1

        // creamos la variable contadora de filas
        int rowCount = 21;
        // creamos la variable contadora de columnas
        int columnCount = 19;
        // creamos la variable del tamaño del cuadro
        int tileSize = 32;
        // creamos la variable del ancho de la ventana
        int boardWidth = columnCount * tileSize;
        // creamos la variable del alto de la ventana
        int boardHeight = rowCount * tileSize;


        //creamos la ventana llamada pacman
        JFrame frame = new JFrame("Pac_man");

        //hacemos que sea visible
        //frame.setVisible(true);

        //ajustamos el tamaño de la ventana
        frame.setSize(boardWidth, boardHeight);

        //para establecer su ubicacion en el centro de la pantalla
        frame.setLocationRelativeTo(null);

        //para evitar la modicacion del tamaño de la ventana por el usuario
        frame.setResizable(false);

        //que el juego se cierre si el jugador pulsa en la x de la ventana
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //paso 3
        //creamos instancia del Jpanel
        PacMan pacmanGame = new PacMan();
        //añadimos este jPanel a una ventana:

        frame.add(pacmanGame);
        //nos aseguramos que tengamos el tamaño completo del jPanel dentro de nuestra ventana.
        frame.pack();
        frame.setVisible(true);

        

    }
}
