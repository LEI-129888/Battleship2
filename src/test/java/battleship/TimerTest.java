package battleship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimerTest {
    @Test
    public void testLastTimeRecording() {
        IFleet fleet = new Fleet().createRandom();
        Game game = new Game(fleet);

        assertEquals(-1, game.getLastTime(), "O tempo inicial da última jogada deve ser -1.");

        // Primeira jogada
        long simulatedTime1 = 2500;
        game.setLastTime(simulatedTime1);

        assertEquals(simulatedTime1, game.getLastTime(), "O tempo deve ter sido atualizado para 2500.");

        // Segunda jogada
        long simulatedTime2 = 1200;
        game.setLastTime(simulatedTime2);

        assertEquals(simulatedTime2, game.getLastTime(), "O Tempo deve ter sido reiniciado e substituido por 1200.");
    }
}
