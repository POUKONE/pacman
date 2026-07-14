import javax.swing.JFrame;
import java.awt.*;
import javax.swing.*;

public class App {
    public static void main (String[] args) throws Exception {
        // Definir la taille de la fenetre
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        // Creation de la fenetre avec ses proprietes
        JFrame frame = new JFrame("Pac Man");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Un CardLayout permet de basculer entre le menu de demarrage et la partie
        CardLayout cardLayout = new CardLayout();
        JPanel container = new JPanel(cardLayout);

        StartScreen startScreen = new StartScreen(difficulty -> {
            PacMan pacManGame = new PacMan(difficulty, () -> cardLayout.show(container, "menu"));
            container.add(pacManGame, "game");
            cardLayout.show(container, "game");
            pacManGame.requestFocusInWindow();
        });

        container.add(startScreen, "menu");
        frame.add(container);

        frame.pack();
        frame.setSize(boardWidth, boardHeight);// definit la taille de la fenetre
        frame.setLocationRelativeTo(null);// centre la fenetre de l'ecran
        frame.setVisible(true);// rend la fenetre visible a l'ecran
    }
}
