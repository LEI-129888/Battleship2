package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Frigate.
 * Author: 129867
 * Date: 2026-11-18 18:38
 * Cyclomatic Complexity:
 * - constructor: 5
 */
public class FrigateTest {

	private Frigate frigate;

	@BeforeEach
	void setUp() {
		frigate = new Frigate(Compass.NORTH, new Position(5, 5));
	}

	@AfterEach
	void tearDown() {
		frigate = null;
	}

	@Test
	@DisplayName("Test Frigate constructor with NORTH bearing")
	void testConstructorNorth() {
		List<IPosition> positions = frigate.getPositions();
		assertAll("Frigate NORTH constructor",
				() -> assertNotNull(frigate, "Error: Frigate instance should not be null."),
				() -> assertEquals("Fragata", frigate.getCategory(), "Error: Frigate category should be 'Fragata'."),
				() -> assertEquals(Compass.NORTH, frigate.getBearing(), "Error: Frigate bearing is incorrect."),
				() -> assertEquals(4, frigate.getSize(), "Error: Frigate size should be 4."),
				() -> assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions."),
				() -> assertEquals(new Position(5, 5), positions.getFirst(), "Error: First position is incorrect for NORTH."),
				() -> assertEquals(new Position(6, 5), positions.get(1), "Error: Second position is incorrect for NORTH."),
				() -> assertEquals(new Position(7, 5), positions.get(2), "Error: Third position is incorrect for NORTH."),
				() -> assertEquals(new Position(8, 5), positions.get(3), "Error: Fourth position is incorrect for NORTH.")
		);
	}

	@Test
	@DisplayName("Test Frigate constructor with SOUTH bearing")
	void testConstructorSouth() {
		frigate = new Frigate(Compass.SOUTH, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();
		assertAll("Frigate SOUTH constructor",
				() -> assertNotNull(frigate, "Error: Frigate instance should not be null."),
				() -> assertEquals(Compass.SOUTH, frigate.getBearing(), "Error: Frigate bearing should be SOUTH."),
				() -> assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions."),
				() -> assertEquals(new Position(5, 5), positions.getFirst(), "Error: First position is incorrect for SOUTH."),
				() -> assertEquals(new Position(6, 5), positions.get(1), "Error: Second position is incorrect for SOUTH."),
				() -> assertEquals(new Position(7, 5), positions.get(2), "Error: Third position is incorrect for SOUTH."),
				() -> assertEquals(new Position(8, 5), positions.get(3), "Error: Fourth position is incorrect for SOUTH.")
		);
	}

	@Test
	@DisplayName("Test Frigate constructor with EAST bearing")
	void testConstructorEast() {
		frigate = new Frigate(Compass.EAST, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();
		assertAll("Frigate EAST constructor",
				() -> assertNotNull(frigate, "Error: Frigate instance should not be null."),
				() -> assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions."),
				() -> assertEquals(new Position(5, 5), positions.getFirst(), "Error: First position is incorrect for EAST."),
				() -> assertEquals(new Position(5, 6), positions.get(1), "Error: Second position is incorrect for EAST."),
				() -> assertEquals(new Position(5, 7), positions.get(2), "Error: Third position is incorrect for EAST."),
				() -> assertEquals(new Position(5, 8), positions.get(3), "Error: Fourth position is incorrect for EAST.")
		);
	}

	@Test
	@DisplayName("Test Frigate constructor with WEST bearing")
	void testConstructorWest() {
		frigate = new Frigate(Compass.WEST, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();
		assertAll("Frigate WEST constructor",
				() -> assertNotNull(frigate, "Error: Frigate instance should not be null."),
				() -> assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions."),
				() -> assertEquals(new Position(5, 5), positions.getFirst(), "Error: First position is incorrect for WEST."),
				() -> assertEquals(new Position(5, 6), positions.get(1), "Error: Second position is incorrect for WEST."),
				() -> assertEquals(new Position(5, 7), positions.get(2), "Error: Third position is incorrect for WEST."),
				() -> assertEquals(new Position(5, 8), positions.get(3), "Error: Fourth position is incorrect for WEST.")
		);
	}

	@Test
	@DisplayName("Test getSize returns 4 for Frigate")
	void testGetSize() {
		assertEquals(4, frigate.getSize(), "Error: Frigate size should be 4.");
	}

	@Test
	@DisplayName("Test stillFloating returns true when all positions are intact")
	void testStillFloating1() {
		assertTrue(frigate.stillFloating(), "Error: Frigate should still be floating.");
	}

	@Test
	@DisplayName("Test stillFloating returns true when only one position is hit")
	void testStillFloating2() {
		frigate.getPositions().getFirst().shoot();
		assertTrue(frigate.stillFloating(), "Error: Frigate should still be floating with one hit position.");
	}

	@Test
	@DisplayName("Test stillFloating returns false when all positions are hit")
	void testStillFloating3() {
		frigate.getPositions().forEach(IPosition::shoot);
		assertFalse(frigate.stillFloating(), "Error: Frigate should not be floating if all positions are hit.");
	}

	@Test
	@DisplayName("Test getTopMostPos returns topmost row")
	void testGetTopMostPos() {
		assertEquals(5, frigate.getTopMostPos(), "Error: The topmost position should be 5.");
	}

	@Test
	@DisplayName("Test getBottomMostPos returns bottommost row")
	void testGetBottomMostPos() {
		assertEquals(8, frigate.getBottomMostPos(), "Error: The bottommost position should be 8.");
	}

	@Test
	@DisplayName("Test getLeftMostPos returns leftmost column")
	void testGetLeftMostPos() {
		assertEquals(5, frigate.getLeftMostPos(), "Error: The leftmost position should be 5.");
	}

	@Test
	@DisplayName("Test getRightMostPos returns rightmost column")
	void testGetRightMostPos() {
		assertEquals(5, frigate.getRightMostPos(), "Error: The rightmost position should be 5.");
	}

	@Test
	@DisplayName("Test Frigate constructor throws NullPointerException on null input")
	void testConstructorInvalidInput() {
		assertAll("Invalid constructor inputs",
				() -> assertThrows(NullPointerException.class, () -> new Frigate(null, null),
						"Error: NullPointerException should be thrown for null input."),
				() -> assertThrows(NullPointerException.class, () -> new Frigate(Compass.NORTH, null),
						"Error: NullPointerException should be thrown for null position.")
		);
	}
}