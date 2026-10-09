package battleship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SoundLogicTest {
    @Test
    public void testSplash() {
        int shipsBefore = 1;
        int shipsAfter = 1;
        int hitsBefore = 10;
        int hitsAfter = 10;
        String result = Tasks.decideSoundToPlay(shipsBefore, shipsAfter, hitsBefore, hitsAfter);
        assertEquals("splash", result, "Deve retornar 'splash' quando nenhum tiro acerta.");
    }
    @Test
    public void testExplosion() {
        int shipsBefore = 1;
        int shipsAfter = 1;
        int hitsBefore = 10;
        int hitsAfter = 11;
        String result = Tasks.decideSoundToPlay(shipsBefore, shipsAfter, hitsBefore, hitsAfter);
        assertEquals("explosion", result, "Deve retornar 'explosion' quando acerta pelo menos um tiro mas não afunda.");
    }
    @Test
    public void testAlarm() {
        int shipsBefore = 2;
        int shipsAfter = 1;
        int hitsBefore = 10;
        int hitsAfter = 11;
        String result = Tasks.decideSoundToPlay(shipsBefore, shipsAfter, hitsBefore, hitsAfter);
        assertEquals("alarm", result, "Deve retornar 'alarm' quando afunda pelo menos um barco.");
    }
}
