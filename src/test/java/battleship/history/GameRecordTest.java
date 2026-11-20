package battleship.history;

import battleship.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for GameRecord.
 * Author: alexa
 * Date: 2026-11-17 22:07
 * Cyclomatic Complexity:
 * - constructor: 1
 * - addMove(): 4
 * - getTimestamp(): 1
 * - setTimestamp(): 1
 * - getOutcome(): 1
 * - setOutcome(): 1
 * - getTotalShots(): 1
 * - setTotalShots(): 1
 * - getTotalHits(): 1
 * - setTotalHits(): 1
 * - getMoves(): 1
 * - setMoves(): 1
 */
class GameRecordTest {

    private GameRecord gameRecord;

    @BeforeEach
    void setUp() {
        gameRecord = new GameRecord();
    }

    @AfterEach
    void tearDown() {
        gameRecord = null;
    }

    @Test
    @DisplayName("constructor initializes a timestamp")
    void constructor() {
        assertNotNull(gameRecord.getTimestamp(),
                "Error: expected the constructor to initialize the timestamp.");
        assertTrue(gameRecord.getTimestamp().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "Error: expected the timestamp to use yyyy-MM-dd HH:mm:ss format.");
    }

    @Test
    @DisplayName("addMove adds shots when shot results are null")
    void addMove1() {
        IMove move = new Move(1, List.of(new Position(1, 1), new Position(1, 2)), null);

        gameRecord.addMove(move);

        assertAll(
                () -> assertEquals(1, gameRecord.getMoves().size(),
                        "Error: expected the move to be added when shot results are null."),
                () -> assertEquals(2, gameRecord.getTotalShots(),
                        "Error: expected both shots from the move to be counted."),
                () -> assertEquals(0, gameRecord.getTotalHits(),
                        "Error: expected no hits when shot results are null.")
        );
    }

    @Test
    @DisplayName("addMove handles an empty shot result list")
    void addMove2() {
        IMove move = new Move(1, List.of(new Position(2, 1)), List.of());

        gameRecord.addMove(move);

        assertAll(
                () -> assertEquals(1, gameRecord.getMoves().size(),
                        "Error: expected the move with no shot results to be added."),
                () -> assertEquals(1, gameRecord.getTotalShots(),
                        "Error: expected the move's shot to be counted."),
                () -> assertEquals(0, gameRecord.getTotalHits(),
                        "Error: expected no hits from an empty shot result list.")
        );
    }

    @Test
    @DisplayName("addMove counts shots without hits")
    void addMove3() {
        IMove move = new Move(1, List.of(new Position(3, 1)),
                List.of(new IGame.ShotResult(true, false, null, false)));

        gameRecord.addMove(move);

        assertAll(
                () -> assertEquals(1, gameRecord.getMoves().size(),
                        "Error: expected the move containing a miss to be added."),
                () -> assertEquals(1, gameRecord.getTotalShots(),
                        "Error: expected the move's shot to be counted."),
                () -> assertEquals(0, gameRecord.getTotalHits(),
                        "Error: expected a shot with no ship to add no hits.")
        );
    }

    @Test
    @DisplayName("addMove counts a shot that hits a ship")
    void addMove4() {
        IShip ship = new Barge(Compass.NORTH, new Position(0,0));
        IMove move = new Move(1, List.of(new Position(4, 1)),
                List.of(new IGame.ShotResult(true, false, ship, false)));

        gameRecord.addMove(move);

        assertAll(
                () -> assertEquals(1, gameRecord.getMoves().size(),
                        "Error: expected the move containing a hit to be added."),
                () -> assertEquals(1, gameRecord.getTotalShots(),
                        "Error: expected the move's shot to be counted."),
                () -> assertEquals(1, gameRecord.getTotalHits(),
                        "Error: expected a shot with a non-null ship to count as a hit.")
        );
    }

    @Test
    @DisplayName("getTimestamp returns the stored timestamp")
    void getTimestamp() {
        gameRecord.setTimestamp("2026-11-17 22:07:00");

        assertEquals("2026-11-17 22:07:00", gameRecord.getTimestamp(),
                "Error: expected getTimestamp to return the stored timestamp.");
    }

    @Test
    @DisplayName("setTimestamp updates the timestamp")
    void setTimestamp() {
        gameRecord.setTimestamp("2026-11-17 22:07:00");

        assertEquals("2026-11-17 22:07:00", gameRecord.getTimestamp(),
                "Error: expected setTimestamp to update the timestamp.");
    }

    @Test
    @DisplayName("getOutcome returns the stored outcome")
    void getOutcome() {
        gameRecord.setOutcome("Vitoria");

        assertEquals("Vitoria", gameRecord.getOutcome(),
                "Error: expected getOutcome to return the stored outcome.");
    }

    @Test
    @DisplayName("setOutcome updates the outcome")
    void setOutcome() {
        gameRecord.setOutcome("Derrota");

        assertEquals("Derrota", gameRecord.getOutcome(),
                "Error: expected setOutcome to update the outcome.");
    }

    @Test
    @DisplayName("getTotalShots returns the stored shot count")
    void getTotalShots() {
        gameRecord.setTotalShots(15);

        assertEquals(15, gameRecord.getTotalShots(),
                "Error: expected getTotalShots to return the stored shot count.");
    }

    @Test
    @DisplayName("setTotalShots updates the shot count")
    void setTotalShots() {
        gameRecord.setTotalShots(15);

        assertEquals(15, gameRecord.getTotalShots(),
                "Error: expected setTotalShots to update the shot count.");
    }

    @Test
    @DisplayName("getTotalHits returns the stored hit count")
    void getTotalHits() {
        gameRecord.setTotalHits(7);

        assertEquals(7, gameRecord.getTotalHits(),
                "Error: expected getTotalHits to return the stored hit count.");
    }

    @Test
    @DisplayName("setTotalHits updates the hit count")
    void setTotalHits() {
        gameRecord.setTotalHits(7);

        assertEquals(7, gameRecord.getTotalHits(),
                "Error: expected setTotalHits to update the hit count.");
    }

    @Test
    @DisplayName("getMoves returns the stored move list")
    void getMoves() {
        List<IMove> moves = new ArrayList<>();
        gameRecord.setMoves(moves);

        assertSame(moves, gameRecord.getMoves(),
                "Error: expected getMoves to return the stored move list.");
    }

    @Test
    @DisplayName("setMoves updates the move list")
    void setMoves() {
        List<IMove> moves = List.of(new Move(1, List.of(new Position(5, 1)), List.of()));

        gameRecord.setMoves(moves);

        assertSame(moves, gameRecord.getMoves(),
                "Error: expected setMoves to store the supplied move list.");
    }
}
