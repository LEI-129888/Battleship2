package battleship;

import javazoom.jl.player.Player;
import java.io.InputStream;

public class SoundManager {

    public static void play(String soundFileName) {
        if (soundFileName == null || soundFileName.isEmpty()) {
            System.err.println("Nome vazio");
            return;
        }
        new Thread(() -> {
            try {
                InputStream is = SoundManager.class.getResourceAsStream("/" + soundFileName);
                if (is == null) {
                    System.err.println("Ficheiro de som não encontrado: " + soundFileName);
                    return;
                }
                Player player = new Player(is);
                player.play();
            } catch (Exception e) {
                System.err.println("Erro ao reproduzir o som: " + e.getMessage());
            }
        }).start();
    }

    public static void playSplash() {
        play("splash.mp3");
    }

    public static void playExplosion() {
        play("explosion.mp3");
    }

    public static void playAlarm() {
        play("alarm.mp3");
    }
}