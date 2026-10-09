package battleship;

import org.junit.jupiter.api.Test;

public class SoundTest {
    @Test
    public void testNull() {
        SoundManager.play(null);
    }

    @Test
    public void testEmpty() {
        SoundManager.play("");
    }

    @Test
    public void testExistingFile() {
        SoundManager.play("splash.mp3");
    }

    @Test
    public void testInsistingFile() {
        SoundManager.play("nonexistence.mp3");
    }
}
