import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;

public class StartScreen extends JPanel {

    private final JLabel bestLabel;

    public StartScreen(Consumer<PacMan.Difficulty> onStart) {
        setPreferredSize(new Dimension(19 * 32, 21 * 32));
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("PAC-MAN");
        title.setForeground(Color.YELLOW);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        gbc.gridy = 0;
        add(title, gbc);

        bestLabel = new JLabel("Meilleur score : " + HighScoreManager.loadBestScore());
        bestLabel.setForeground(Color.WHITE);
        bestLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 1;
        add(bestLabel, gbc);

        JLabel diffLabel = new JLabel("Difficulté :");
        diffLabel.setForeground(Color.WHITE);
        diffLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridy = 2;
        add(diffLabel, gbc);

        JComboBox<String> diffBox = new JComboBox<>(new String[] { "Débutant", "Intermédiaire", "Avancé" });
        gbc.gridy = 3;
        add(diffBox, gbc);

        JButton playButton = new JButton("Jouer");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridy = 4;
        add(playButton, gbc);

        JLabel hint = new JLabel("Flèches pour bouger · Espace pour pause");
        hint.setForeground(Color.GRAY);
        hint.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridy = 5;
        add(hint, gbc);

        playButton.addActionListener(e -> {
            PacMan.Difficulty difficulty;
            switch (diffBox.getSelectedIndex()) {
                case 1: difficulty = PacMan.Difficulty.MEDIUM; break;
                case 2: difficulty = PacMan.Difficulty.HARD; break;
                default: difficulty = PacMan.Difficulty.EASY;
            }
            onStart.accept(difficulty);
        });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                bestLabel.setText("Meilleur score : " + HighScoreManager.loadBestScore());
            }
        });
    }
}
