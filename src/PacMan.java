import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.Timer;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    private final int tileSize = 32;
    private final int rows = 21;
    private final int cols = 19;
    private Timer timer;
    private boolean isPaused = false;

    private Image wallImg, foodImg, cherryImg;
    private Image pacRightImg, pacLeftImg, pacUpImg, pacDownImg;

    private Block pacman;
    private HashSet<Block> walls = new HashSet<>();
    private HashSet<Block> foods = new HashSet<>();
    private ArrayList<Block> ghosts = new ArrayList<>();
    private Random rand = new Random();
    private final char[] directions = {'U', 'D', 'L', 'R'};

    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private int bestScore = HighScoreManager.loadBestScore();

    private Block cherry = null;
    private long lastCherryTime = System.currentTimeMillis();
    private final long cherryInterval = 10000;
    private final long cherryDuration = 5000;
    private long cherryStartTime = 0;

    private final Runnable onReturnToMenu;
    private JButton menuButton;
    private JButton replayButton;

    private int mouthAngle = 0;
    private boolean mouthOpening = true;

    public PacMan() {
        this(Difficulty.EASY, () -> {});
    }

    public PacMan(Difficulty difficulty) {
        this(difficulty, () -> {});
    }

    public PacMan(Difficulty difficulty, Runnable onReturnToMenu) {
        this.difficulty = difficulty;
        this.onReturnToMenu = onReturnToMenu;
        setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);
        addKeyListener(this);
        loadImages();
        loadMap();
        setupEndScreenButtons();
        timer = new Timer(50, this);
        timer.start();
    }

    private void setupEndScreenButtons() {
        int width = 200, height = 40;
        int x = (cols * tileSize - width) / 2;

        replayButton = new JButton("Rejouer");
        replayButton.setFont(new Font("Arial", Font.BOLD, 16));
        replayButton.setBounds(x, 330, width, height);
        replayButton.setVisible(false);
        replayButton.addActionListener(e -> restartGame());
        add(replayButton);

        menuButton = new JButton("Retour au menu");
        menuButton.setFont(new Font("Arial", Font.BOLD, 16));
        menuButton.setBounds(x, 380, width, height);
        menuButton.setVisible(false);
        menuButton.addActionListener(e -> {
            timer.stop();
            onReturnToMenu.run();
        });
        add(menuButton);
    }

    private void restartGame() {
        score = 0;
        lives = 3;
        gameOver = false;
        ghostsScared = false;
        isInvincible = false;
        cherry = null;
        superFruit = null;
        replayButton.setVisible(false);
        menuButton.setVisible(false);
        loadMap();
        timer.start();
        requestFocusInWindow();
    }

    private void loadImages() {
        wallImg = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        foodImg = new ImageIcon(getClass().getResource("./cherry2.png")).getImage();
        cherryImg = new ImageIcon(getClass().getResource("./cherry.png")).getImage();
        pacRightImg = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
        pacLeftImg = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacUpImg = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacDownImg = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        superFruitImg = new ImageIcon(getClass().getResource("./cherry2.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./scaredGhost.png")).getImage();



    }
    private Image blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
    private Image pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
    private Image redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
    private Image orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();

    private Image superFruitImg;
    private Block superFruit = null;
    private long superFruitStartTime = 0;
    private final long superFruitDuration = 5000;
    private boolean isInvincible = false;

    private Image scaredGhostImage;
    private boolean ghostsScared = false;
    private long scaredStartTime = 0;
    private final long scaredDuration = 5000;
    private final long scaredFlashThreshold = 2000; // derniers ms de la peur : les fantômes clignotent
    private final long scaredFlashInterval = 200;    // vitesse du clignotement
    private int ghostEatCombo = 0;

    private int currentLevel = 1;

    public enum Difficulty { EASY, MEDIUM, HARD }
    private Difficulty difficulty = Difficulty.EASY;


    private void loadMapFromFile(int level) {
        List<String[]> lines = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File("level" + level + ".txt"));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.trim().isEmpty()) {
                    lines.add(line.split(""));
                }
            }
            tileMap = lines.toArray(new String[0][0]);
            loadMap(); // recharge les objets Block
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du niveau " + level);
            e.printStackTrace();
            // Option : revenir au niveau 1 en cas d'erreur
            currentLevel = 1;
            loadMapFromFile(currentLevel);
        }
    }



    private String[][] tileMap = {
            {"X","X","X","X","X","X","X","X","X","X","X","X","X","X","X","X","X","X","X"},
            {"X","P","S"," "," "," "," "," ","X"," "," "," ","S"," "," "," "," "," ","X"},
            {"X"," ","X","X"," ","X","X","X"," ","X","X","X"," ","X","X"," ","X"," ","X"},
            {"X"," "," "," "," "," "," "," "," "," "," "," "," "," "," "," "," "," ","X"},
            {"X"," ","X","X"," ","X","X","X","X","X","X","X"," ","X","X"," ","X","X","X"},
            {"X","S"," "," "," "," "," "," "," "," "," "," ","X"," "," "," "," "," ","X"},
            {"X","X","X","X"," ","X","X","X","X"," ","X","X","X","X"," ","X","X","X","X"},
            {"X","O","O","O"," ","X"," "," "," "," "," "," ","X"," "," "," "," "," ","X"},
            {"X","X","X","X"," ","X"," ","X","X","r","X","X"," ","X"," ","X","X","O","X"},
            {"X"," "," "," "," "," ","b","p","o","S"," "," "," "," "," "," "," "," ","X"},
            {"X","X","X","X"," ","X"," ","X","X","X","X","X"," ","X"," ","X"," "," ","X"},
            {"X","X","O","O"," ","X"," "," "," "," "," "," ","X"," "," ","O","X","O","X"},
            {"X","X","X","X"," ","X"," ","X","X","X","X","X"," ","X","S","X","X","X","X"},
            {"X"," "," "," "," "," "," "," ","X"," "," "," "," "," "," "," "," "," ","X"},
            {"X"," ","X","X"," ","X","X","X"," ","X","X","X"," ","X","X"," ","X"," ","X"},
            {"X"," "," ","X"," "," "," "," ","p"," "," "," "," ","X"," "," ","X"," ","X"},
            {"X","X"," ","X"," ","X"," ","X","X","X","X","X"," ","X"," ","X"," ","X","X"},
            {"X"," "," "," ","X"," "," "," ","X"," "," "," "," ","X"," "," "," "," ","X"},
            {"X"," ","X","X","X","X","X","X"," ","X"," ","X","X","X"," ","X","X"," ","X"},
            {"X"," "," "," "," "," ","S"," "," "," "," "," "," "," "," "," "," "," ","X"},
            {"X","X","X","X","X","X","X","X","X","X","X","X","X","X","X","X"," ","X","X"}
        };

        private void loadMap() {
            walls.clear();
            foods.clear();
            ghosts.clear();
            pacman = null;
        
            for (int i = 0; i < tileMap.length; i++) {
                for (int j = 0; j < tileMap[i].length; j++) {
                    String tile = tileMap[i][j];
                    int x = j * tileSize;
                    int y = i * tileSize;
        
                    switch (tile) {
                        case "X":
                            walls.add(new Block(wallImg, x, y, tileSize, tileSize));
                            break;
                        case " ":
                            foods.add(new Block(foodImg, x + tileSize/4, y + tileSize/4, tileSize/2, tileSize/2));
                            break;
                        case "P":
                            pacman = new Block(pacRightImg, x, y, tileSize, tileSize);
                            break;
                        case "b":
                            Block blueGhost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                            blueGhost.direction = directions[rand.nextInt(directions.length)];
                            updateGhostVelocity(blueGhost);
                            ghosts.add(blueGhost);
                            break;
                        case "p":
                            Block pinkGhost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                            pinkGhost.direction = directions[rand.nextInt(directions.length)];
                            updateGhostVelocity(pinkGhost);
                            ghosts.add(pinkGhost);
                            break;
                        case "r":
                            Block redGhost = new Block(redGhostImage, x, y, tileSize, tileSize);
                            redGhost.direction = directions[rand.nextInt(directions.length)];
                            updateGhostVelocity(redGhost);
                            ghosts.add(redGhost);
                            break;
                        case "o":
                            Block orangeGhost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                            orangeGhost.direction = directions[rand.nextInt(directions.length)];
                            updateGhostVelocity(orangeGhost);
                            ghosts.add(orangeGhost);
                            break;
                        case "S":
                            foods.add(new Block(superFruitImg, x + tileSize/4, y + tileSize/4, tileSize/2, tileSize/2));
                            break;
                        
                    }
                }
            }
        }
        

    private void updateGhostVelocity(Block ghost) {
        int speed;
        switch (difficulty) {
            case EASY: speed = tileSize / 8; break;
            case MEDIUM: speed = tileSize / 6; break;
            case HARD: speed = tileSize / 4; break;
            default: speed = tileSize / 4;
        }
        if (ghost.isScared) speed /= 2;
        
        switch (ghost.direction) {
            case 'U': ghost.velocityX = 0; ghost.velocityY = -speed; break;
            case 'D': ghost.velocityX = 0; ghost.velocityY = speed; break;
            case 'L': ghost.velocityX = -speed; ghost.velocityY = 0; break;
            case 'R': ghost.velocityX = speed; ghost.velocityY = 0; break;
        }
        
    }

    private void moveGhosts() {
        for (Block ghost : ghosts) {
            int px = ghost.x, py = ghost.y;
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (isColliding(ghost, wall)) {
                    ghost.x = px; ghost.y = py;
                    ghost.direction = chooseGhostDirection(ghost);
                    updateGhostVelocity(ghost); break;
                }
            }
        }

    }

    private boolean isWallAt(int x, int y) {
        for (Block wall : walls) {
            if (x < wall.x + wall.width && x + tileSize > wall.x &&
                y < wall.y + wall.height && y + tileSize > wall.y) {
                return true;
            }
        }
        return false;
    }

    // Tire une case au hasard qui n'est pas un mur, pour les cerises et le super fruit.
    private int[] randomFloorTile() {
        int x, y;
        do {
            x = rand.nextInt(cols) * tileSize;
            y = rand.nextInt(rows) * tileSize;
        } while (isWallAt(x, y));
        return new int[] { x, y };
    }

    // À une intersection, un fantôme non effrayé se rapproche de Pac-Man la
    // plupart du temps, un fantôme effrayé s'en éloigne ; sinon, direction aléatoire.
    private char chooseGhostDirection(Block ghost) {
        if (pacman == null || rand.nextInt(100) >= 65) {
            return directions[rand.nextInt(directions.length)];
        }

        char best = directions[rand.nextInt(directions.length)];
        int bestDist = ghost.isScared ? -1 : Integer.MAX_VALUE;
        boolean found = false;

        for (char d : directions) {
            int nx = ghost.x, ny = ghost.y;
            switch (d) {
                case 'U': ny -= tileSize; break;
                case 'D': ny += tileSize; break;
                case 'L': nx -= tileSize; break;
                case 'R': nx += tileSize; break;
            }
            if (isWallAt(nx, ny)) continue;

            int dist = Math.abs(nx - pacman.x) + Math.abs(ny - pacman.y);
            boolean better = ghost.isScared ? dist > bestDist : dist < bestDist;
            if (better) { bestDist = dist; best = d; found = true; }
        }

        return found ? best : directions[rand.nextInt(directions.length)];
    }

    private boolean isColliding(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
               a.y < b.y + b.height && a.y + a.height > b.y;
    }

    private void movePacman() {
        if (gameOver) return;
        int px = pacman.x, py = pacman.y;
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        for (Block wall : walls) if (isColliding(pacman, wall)) { pacman.x = px; pacman.y = py; break; }

        Iterator<Block> foodIt = foods.iterator();
        while (foodIt.hasNext()) {
            Block food = foodIt.next();
            if (isColliding(pacman, food)) {
                foodIt.remove();
                SoundPlayer.playPelletEaten();
                if (food.image == superFruitImg) {
                    score += 50;
                    scareGhosts();
                } else {
                    score += 10;
                }
                break;
            }
        }

        long now = System.currentTimeMillis();
        if (cherry == null && now - lastCherryTime >= cherryInterval) {
            int[] pos = randomFloorTile();
            cherry = new Block(cherryImg, pos[0], pos[1], tileSize, tileSize);
            cherryStartTime = now;
        }
        if (cherry != null && now - cherryStartTime >= cherryDuration) {
            cherry = null; lastCherryTime = now;
        }
        if (cherry != null && isColliding(pacman, cherry)) {
            score += 50; cherry = null; lastCherryTime = now;
        }

        now = System.currentTimeMillis();

        if (superFruit == null && rand.nextInt(500) == 0) {
            int[] pos = randomFloorTile();
            superFruit = new Block(superFruitImg, pos[0], pos[1], tileSize, tileSize);
            superFruitStartTime = now;
        }
        if (superFruit != null && isColliding(pacman, superFruit)) {
            superFruit = null;
            isInvincible = true;
            superFruitStartTime = now;
            score += 100;
        }
        if (isInvincible && now - superFruitStartTime >= superFruitDuration) {
            isInvincible = false;
        }
            
        for (Block ghost : ghosts) {
            if (isColliding(pacman, ghost)) {
                if (ghost.isScared) {
                    SoundPlayer.playGhostEaten();
                    ghost.x = ghost.startX;
                    ghost.y = ghost.startY;
                    ghost.isScared = false;
                    ghost.image = ghost.originalImage;
                    ghostEatCombo++;
                    score += (int) (200 * Math.pow(2, ghostEatCombo - 1)); // 200, 400, 800, 1600
                } else if (!isInvincible) {
                    if (--lives <= 0) {
                        gameOver = true;
                        timer.stop();
                        if (score > bestScore) {
                            bestScore = score;
                            HighScoreManager.saveBestScore(bestScore);
                        }
                        replayButton.setVisible(true);
                        menuButton.setVisible(true);
                    }
                    else resetPositions();
                }
                break;
            }
        }

        if (foods.isEmpty()) {
            currentLevel++;
            loadMapFromFile(currentLevel);
        }
        
        
        


    }

    private void scareGhosts() {
        ghostsScared = true;
        scaredStartTime = System.currentTimeMillis();
        ghostEatCombo = 0;

        for (Block ghost : ghosts) {
            ghost.isScared = true;
            ghost.image = scaredGhostImage;
        }
    }

    // Bascule les fantômes encore effrayés entre leur couleur "peur" et leur
    // couleur d'origine durant les dernières secondes, puis met fin à l'effet.
    private void updateScaredGhosts() {
        long elapsed = System.currentTimeMillis() - scaredStartTime;
        long remaining = scaredDuration - elapsed;

        if (remaining <= 0) {
            ghostsScared = false;
            for (Block ghost : ghosts) {
                ghost.isScared = false;
                if (ghost.image == scaredGhostImage) {
                    ghost.image = ghost.originalImage;
                }
            }
        } else if (remaining <= scaredFlashThreshold) {
            boolean flashOn = (elapsed / scaredFlashInterval) % 2 == 0;
            for (Block ghost : ghosts) {
                if (ghost.isScared) {
                    ghost.image = flashOn ? scaredGhostImage : ghost.originalImage;
                }
            }
        }
    }
    

    private void resetPositions() { loadMap(); }

    private void updatePacmanVelocity() {
        int speed = tileSize/4;
        switch (pacman.direction) {
            case 'U': pacman.velocityX = 0; pacman.velocityY = -speed; pacman.image = pacUpImg; break;
            case 'D': pacman.velocityX = 0; pacman.velocityY = speed; pacman.image = pacDownImg; break;
            case 'L': pacman.velocityX = -speed; pacman.velocityY = 0; pacman.image = pacLeftImg; break;
            case 'R': pacman.velocityX = speed; pacman.velocityY = 0; pacman.image = pacRightImg; break;
        }
    }

    private void drawCentered(Graphics g, String text, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (cols * tileSize - w) / 2, y);
    }

    // Fait osciller l'angle de la bouche entre 0° (fermée) et 40° (ouverte)
    // pendant que Pac-Man se déplace ; se referme dès qu'il est immobile.
    private void animatePacman() {
        if (pacman == null || (pacman.velocityX == 0 && pacman.velocityY == 0)) {
            mouthAngle = 0;
            return;
        }
        if (mouthOpening) {
            mouthAngle += 6;
            if (mouthAngle >= 40) { mouthAngle = 40; mouthOpening = false; }
        } else {
            mouthAngle -= 6;
            if (mouthAngle <= 0) { mouthAngle = 0; mouthOpening = true; }
        }
    }

    // Pac-Man est dessiné comme un disque jaune avec un secteur découpé pour la
    // bouche (plutôt que via le sprite, dont la bouche est fixe) afin que
    // l'ouverture/fermeture soit réellement visible.
    private void drawPacman(Graphics g, Block p) {
        int directionAngle;
        switch (p.direction) {
            case 'R': directionAngle = 0; break;
            case 'U': directionAngle = 90; break;
            case 'L': directionAngle = 180; break;
            case 'D': directionAngle = 270; break;
            default: directionAngle = 0;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.YELLOW);
        if (mouthAngle <= 2) {
            g2.fillOval(p.x, p.y, p.width, p.height);
        } else {
            g2.fillArc(p.x, p.y, p.width, p.height, directionAngle + mouthAngle / 2, 360 - mouthAngle);
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Block wall : walls) g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        for (Block food : foods) g.drawImage(food.image, food.x, food.y, food.width, food.height, null);
        for (Block ghost : ghosts) g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        if (cherry != null) g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        if (pacman != null) {
            drawPacman(g, pacman);
        }
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score + "   Lives: " + lives + "   Best: " + bestScore, 10, 20);
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            drawCentered(g, "GAME OVER", 250);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            drawCentered(g, "Score : " + score + "   Meilleur score : " + bestScore, 290);
        }
        if (isPaused && !gameOver) {          // éviter de l’afficher par‑dessus GAME OVER
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String t = "PAUSE";
            int w = g.getFontMetrics().stringWidth(t);
            g.drawString(t, (getWidth() - w) / 2, getHeight() / 2);
        }
        
        if (superFruit != null)
            g.drawImage(superFruit.image, superFruit.x, superFruit.y, superFruit.width, superFruit.height, null);

    }

    @Override public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            movePacman();
            moveGhosts();
            animatePacman();
        }

        if (ghostsScared) {
            updateScaredGhosts();
        }

        repaint();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {
        // ① Espace : on bascule pause / reprise
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isPaused = !isPaused;
            return;                  // on ne traite rien d’autre
        }
    
        // ② Si le jeu est en pause ou terminé, on ignore les flèches
        if (isPaused) return;
    
        if (gameOver) return;
    
        // ③ Touches directionnelles
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    pacman.direction = 'U'; break;
            case KeyEvent.VK_DOWN:  pacman.direction = 'D'; break;
            case KeyEvent.VK_LEFT:  pacman.direction = 'L'; break;
            case KeyEvent.VK_RIGHT: pacman.direction = 'R'; break;
        }
        updatePacmanVelocity();
    }
    

    private class Block {
        int x, y, width, height;
        Image image;
        Image originalImage;
        int startX, startY;
        int velocityX = 0, velocityY = 0;
        char direction = 'R';
        boolean isScared = false;

        public Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.originalImage = image;
            this.x = x;
            this.y = y;
            this.startX = x;
            this.startY = y;
            this.width = width;
            this.height = height;
        }


    }
}