import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

// Génère de courts bips synthétisés (aucun fichier audio requis) pour les
// pastilles mangées et les fantômes mangés, joués sur un thread à part pour
// ne jamais bloquer la boucle de jeu.
public class SoundPlayer {

    private static final float SAMPLE_RATE = 44100f;

    public static void playPelletEaten() {
        playSequence(new double[] { 880 }, new int[] { 45 });
    }

    public static void playGhostEaten() {
        playSequence(new double[] { 400, 600, 900 }, new int[] { 60, 60, 90 });
    }

    private static void playSequence(double[] frequencies, int[] durationsMs) {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                for (int n = 0; n < frequencies.length; n++) {
                    int samples = (int) (SAMPLE_RATE * durationsMs[n] / 1000);
                    byte[] buffer = new byte[samples * 2];
                    for (int i = 0; i < samples; i++) {
                        double angle = 2.0 * Math.PI * i * frequencies[n] / SAMPLE_RATE;
                        // enveloppe pour éviter les clics au début/à la fin de chaque note
                        double envelope = Math.min(1.0, Math.min(i, samples - i) / 200.0);
                        short sample = (short) (Math.sin(angle) * Short.MAX_VALUE * 0.3 * envelope);
                        buffer[2 * i] = (byte) (sample & 0xff);
                        buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xff);
                    }
                    line.write(buffer, 0, buffer.length);
                }

                line.drain();
                line.close();
            } catch (LineUnavailableException e) {
                // Aucun périphérique audio disponible : on ignore silencieusement.
            }
        }).start();
    }
}
