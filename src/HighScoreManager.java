import java.io.*;

public class HighScoreManager {
    private static final String FILE = "bestScore.txt";

    public static int loadBestScore() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            return Integer.parseInt(br.readLine());
        } catch (Exception e) { return 0; }   // fichier absent → 0
    }

    public static void saveBestScore(int score) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println(score);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
