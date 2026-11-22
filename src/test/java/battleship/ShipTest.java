package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Ship.
 * Author: 129867
 * Date: 2026-11-18 18:20
 * Cyclomatic Complexity:
 * - buildShip(): 9
 * - constructor: 1
 * - getCategory(): 1
 * - getPositions(): 1
 * - getAdjacentPositions(): 5
 * - getPosition(): 1
 * - getBearing(): 1
 * - getSize(): 1
 * - stillFloating(): 3
 * - getTopMostPos(): 3
 * - getBottomMostPos(): 3
 * - getLeftMostPos(): 3
 * - getRightMostPos(): 3
 * - occupies(): 4
 * - tooCloseTo(IShip): 4
 * - tooCloseTo(IPosition): 4
 * - isInsideBoard(): 5
 * - shoot(): 5
 * - sink(): 2
 * - toString(): 1
 */
public class ShipTest {

    private Ship ship;
    private Ship shipBorder;
    private Ship multiPosShip;

    @BeforeEach
    void setUp() {
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
        // Concrete anonymous subclass with 3 non-monotonic positions to cover all loop/if branches
        multiPosShip = new Ship("TestShip", Compass.NORTH, new Position(5, 5), 3) {
            {
                getPositions().add(new Position(5, 5));
                getPositions().add(new Position(3, 7));
                getPositions().add(new Position(7, 3));
            }
        };
    }

    @AfterEach
    void tearDown() {
        ship = null;
        shipBorder = null;
        multiPosShip = null;
    }

    // --- buildShip tests ---

    @Test
    @DisplayName("Test buildShip creates a Barge")
    void buildShip1() {
        Ship s = Ship.buildShip("barca", Compass.NORTH, new Position(1, 1));
        assertInstanceOf(Barge.class, s, "Error: expected Barge instance");
    }

    @Test
    @DisplayName("Test buildShip creates a Caravel")
    void buildShip2() {
        Ship s = Ship.buildShip("caravela", Compass.NORTH, new Position(1, 1));
        assertInstanceOf(Caravel.class, s, "Error: expected Caravel instance");
    }

    @Test
    @DisplayName("Test buildShip creates a Carrack")
    void buildShip3() {
        Ship s = Ship.buildShip("nau", Compass.NORTH, new Position(1, 1));
        assertInstanceOf(Carrack.class, s, "Error: expected Carrack instance");
    }

    @Test
    @DisplayName("Test buildShip creates a Frigate")
    void buildShip4() {
        Ship s = Ship.buildShip("fragata", Compass.NORTH, new Position(1, 1));
        assertInstanceOf(Frigate.class, s, "Error: expected Frigate instance");
    }

    @Test
    @DisplayName("Test buildShip creates a Galleon")
    void buildShip5() {
        Ship s = Ship.buildShip("galeao", Compass.NORTH, new Position(1, 1));
        assertInstanceOf(Galleon.class, s, "Error: expected Galleon instance");
    }

    @Test
    @DisplayName("Test buildShip returns null for unknown ship kind")
    void buildShip6() {
        Ship s = Ship.buildShip("submarino", Compass.NORTH, new Position(1, 1));
        assertNull(s, "Error: expected null for unknown ship kind");
    }

    @Test
    @DisplayName("Test buildShip throws AssertionError when shipKind is null")
    @SuppressWarnings("DataFlowIssue")
    void buildShip7() {
        assertThrows(AssertionError.class,
                () -> Ship.buildShip(null, Compass.NORTH, new Position(1, 1)),
                "Error: expected AssertionError when shipKind is null");
    }

    @Test
    @DisplayName("Test buildShip throws AssertionError when bearing is null")
    void buildShip8() {
        assertThrows(AssertionError.class,
                () -> Ship.buildShip("barca", null, new Position(1, 1)),
                "Error: expected AssertionError when bearing is null");
    }

    @Test
    @DisplayName("Test buildShip throws AssertionError when pos is null")
    void buildShip9() {
        assertThrows(AssertionError.class,
                () -> Ship.buildShip("barca", Compass.NORTH, null),
                "Error: expected AssertionError when pos is null");
    }

    // --- Constructor & Getters ---

    @Test
    @DisplayName("Test Ship constructor initializes fields properly")
    void constructor() {
        assertAll("Ship constructor initialization",
                () -> assertNotNull(ship, "Error: Ship instance should not be null."),
                () -> assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect."),
                () -> assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect."),
                () -> assertEquals(1, ship.getSize(), "Error: Ship size is incorrect."),
                () -> assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.")
        );
    }

    @Test
    @DisplayName("Test Ship constructor throws NullPointerException on null arguments")
    void constructorNullChecks() {
        assertAll("Constructor null checks",
                () -> assertThrows(NullPointerException.class,
                        () -> new Ship(null, Compass.NORTH, new Position(0, 0), 1) {
                        },
                        "Error: expected NPE for null category"),
                () -> assertThrows(NullPointerException.class,
                        () -> new Ship("Barca", null, new Position(0, 0), 1) {
                        },
                        "Error: expected NPE for null bearing"),
                () -> assertThrows(NullPointerException.class,
                        () -> new Ship("Barca", Compass.NORTH, null, 1) {
                        },
                        "Error: expected NPE for null position")
        );
    }

    @Test
    @DisplayName("Test getCategory returns ship category")
    void getCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    @Test
    @DisplayName("Test getSize returns ship size")
    void getSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    @Test
    @DisplayName("Test getBearing returns ship bearing")
    void getBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    @Test
    @DisplayName("Test getPosition returns initial ship position")
    void getPosition() {
        assertEquals(new Position(5, 5), ship.getPosition(), "Error: Ship position should be (5, 5).");
    }

    @Test
    @DisplayName("Test getPositions returns occupied positions")
    void getPositions() {
        List<IPosition> positions = ship.getPositions();
        assertAll("Ship positions",
                () -> assertNotNull(positions, "Error: Ship positions should not be null."),
                () -> assertEquals(1, positions.size(), "Error: Ship should have exactly one position."),
                () -> assertEquals(5, positions.getFirst().getRow(), "Error: Position's row should be 5."),
                () -> assertEquals(5, positions.getFirst().getColumn(), "Error: Position's column should be 5.")
        );
    }

    // --- getAdjacentPositions ---

    @Test
    @DisplayName("Test getAdjacentPositions for single-cell ships (center and border)")
    void getAdjacentPositions1() {
        assertAll("Adjacent positions for Barge",
                () -> assertEquals(8, ship.getAdjacentPositions().size(), "Error: expected 8 adjacent positions in center."),
                () -> assertEquals(3, shipBorder.getAdjacentPositions().size(), "Error: expected 3 adjacent positions in corner.")
        );
    }

    @Test
    @DisplayName("Test getAdjacentPositions for multi-cell ship excludes own positions and duplicates")
    void getAdjacentPositions2() {
        Ship caravel = new Caravel(Compass.EAST, new Position(5, 5));
        List<IPosition> adj = caravel.getAdjacentPositions();
        assertAll("Adjacent positions for Caravel",
                () -> assertEquals(10, adj.size(), "Error: expected 10 unique adjacent positions for a 2-cell ship in center."),
                () -> assertFalse(adj.contains(new Position(5, 5)), "Error: adjacent positions must not contain ship's own position."),
                () -> assertFalse(adj.contains(new Position(5, 6)), "Error: adjacent positions must not contain ship's own position.")
        );
    }

    // --- stillFloating ---

    @Test
    @DisplayName("Test stillFloating returns true when at least one position is intact")
    void stillFloating1() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    @Test
    @DisplayName("Test stillFloating returns false when all positions are hit")
    void stillFloating2() {
        ship.getPositions().getFirst().shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    // --- Top, Bottom, Left, Right ---

    @Test
    @DisplayName("Test getTopMostPos on single-cell and multi-cell ships")
    void getTopMostPos() {
        assertAll("getTopMostPos",
                () -> assertEquals(5, ship.getTopMostPos(), "Error: expected topmost pos 5 for Barge."),
                () -> assertEquals(3, multiPosShip.getTopMostPos(), "Error: expected topmost pos 3 for multiPosShip.")
        );
    }

    @Test
    @DisplayName("Test getBottomMostPos on single-cell and multi-cell ships")
    void getBottomMostPos() {
        assertAll("getBottomMostPos",
                () -> assertEquals(5, ship.getBottomMostPos(), "Error: expected bottommost pos 5 for Barge."),
                () -> assertEquals(7, multiPosShip.getBottomMostPos(), "Error: expected bottommost pos 7 for multiPosShip.")
        );
    }

    @Test
    @DisplayName("Test getLeftMostPos on single-cell and multi-cell ships")
    void getLeftMostPos() {
        assertAll("getLeftMostPos",
                () -> assertEquals(5, ship.getLeftMostPos(), "Error: expected leftmost pos 5 for Barge."),
                () -> assertEquals(3, multiPosShip.getLeftMostPos(), "Error: expected leftmost pos 3 for multiPosShip.")
        );
    }

    @Test
    @DisplayName("Test getRightMostPos on single-cell and multi-cell ships")
    void getRightMostPos() {
        assertAll("getRightMostPos",
                () -> assertEquals(5, ship.getRightMostPos(), "Error: expected rightmost pos 5 for Barge."),
                () -> assertEquals(7, multiPosShip.getRightMostPos(), "Error: expected rightmost pos 7 for multiPosShip.")
        );
    }

    // --- occupies ---

    @Test
    @DisplayName("Test occupies returns true for occupied position")
    void occupies1() {
        assertTrue(ship.occupies(new Position(5, 5)), "Error: Ship should occupy position (5, 5).");
    }

    @Test
    @DisplayName("Test occupies returns false for non-occupied position")
    void occupies2() {
        assertFalse(ship.occupies(new Position(1, 1)), "Error: Ship should not occupy position (1, 1).");
    }

    @Test
    @DisplayName("Test occupies throws AssertionError when position is null")
    void occupies3() {
        assertThrows(AssertionError.class, () -> ship.occupies(null),
                "Error: expected AssertionError when pos is null.");
    }

    // --- tooCloseTo(IShip) ---

    @Test
    @DisplayName("Test tooCloseTo(IShip) returns true when ships are adjacent")

    void tooCloseToShip1() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
    }

    @Test
    @DisplayName("Test tooCloseTo(IShip) returns false when ships are far apart")
    void tooCloseToShip2() {
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    @Test
    @DisplayName("Test tooCloseTo(IShip) throws AssertionError when other ship is null")
    void tooCloseToShip3() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IShip) null),
                "Error: expected AssertionError when other ship is null.");
    }

    // --- tooCloseTo(IPosition) ---

    @Test
    @DisplayName("Test tooCloseTo(IPosition) returns true for adjacent position")
    void tooCloseToPosition1() {
        assertTrue(ship.tooCloseTo(new Position(5, 6)), "Error: Ship should be too close to (5, 6).");
    }

    @Test
    @DisplayName("Test tooCloseTo(IPosition) returns false for non-adjacent position")
    void tooCloseToPosition2() {
        assertFalse(ship.tooCloseTo(new Position(7, 7)), "Error: Ship should not be too close to (7, 7).");
    }

    @Test
    @DisplayName("Test tooCloseTo(IPosition) throws AssertionError when position is null")
    void tooCloseToPosition3() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IPosition) null),
                "Error: expected AssertionError when position is null.");
    }

    // --- isInsideBoard ---

    @Test
    @DisplayName("Test isInsideBoard returns true when ship is inside board")
    void isInsideBoard1() {
        assertTrue(ship.isInsideBoard(), "Error: Ship at (5, 5) should be inside board.");
    }

    @Test
    @DisplayName("Test isInsideBoard returns false when leftmost position is negative")
    void isInsideBoard2() {
        Ship outLeft = new Barge(Compass.NORTH, new Position(5, -1));
        assertFalse(outLeft.isInsideBoard(), "Error: Ship with column -1 should be outside board.");
    }
}