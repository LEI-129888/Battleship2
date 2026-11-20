package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Fleet.
 * Author: 129867
 * Date: 2026-11-18 18:25
 * Cyclomatic Complexity:
 * - createRandom(): 4
 * - constructor: 1
 * - getShips(): 1
 * - addShip(): 5
 * - getShipsLike(): 4
 * - getFloatingShips(): 3
 * - getSunkShips(): 3
 * - shipAt(): 4
 * - isCollisionFree(): 4
 * - printShips(): 3
 * - printStatus(): 1
 * - printShipsByCategory(): 2
 * - printFloatingShips(): 1
 * - printAllShips(): 1
 */
public class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@AfterEach
	void tearDown() {
		fleet = null;
	}

	// --- Constructor & createRandom ---

	@Test
	@DisplayName("Test Fleet constructor initializes an empty ship list")
	void constructor() {
		assertAll("Fleet constructor",
				() -> assertNotNull(fleet, "Error: Instance of Fleet should not be null."),
				() -> assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with an empty ships list.")
		);
	}

	@Test
	@DisplayName("Test createRandom creates a valid fleet with FLEET_SIZE ships")
	void createRandom() {
		IFleet randomFleet = Fleet.createRandom();
		assertAll("createRandom",
				() -> assertNotNull(randomFleet, "Error: createRandom should return a non-null fleet instance."),
				() -> assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size(),
						"Error: createRandom should create a fleet with FLEET_SIZE ships.")
		);
	}

	// --- getShips ---

	@Test
	@DisplayName("Test getShips returns empty list initially and added ships afterwards")
	void getShips() {
		assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);
		assertAll("getShips after addition",
				() -> assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship."),
				() -> assertEquals(ship, fleet.getShips().getFirst(), "Error: Fleet's first ship should match the added ship.")
		);
	}

	// --- addShip ---

	@Test
	@DisplayName("Test addShip adds a valid ship inside the board without collision")
	void addShip1() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		assertAll("addShip valid",
				() -> assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully."),
				() -> assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.")
		);
	}

	@Test
	@DisplayName("Test addShip returns false when fleet size exceeds FLEET_SIZE")
	void addShip2() {
		for (int i = 0; i <= Fleet.FLEET_SIZE; i++) {
			fleet.getShips().add(new Barge(Compass.NORTH, new Position(0, 0)));
		}
		IShip anotherShip = new Barge(Compass.NORTH, new Position(5, 5));
		assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size exceeds FLEET_SIZE.");
	}

	@Test
	@DisplayName("Test addShip returns false when ship is outside the board")
	void addShip3() {
		IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
		assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
	}

	@Test
	@DisplayName("Test addShip returns false when there is a collision risk")
	void addShip4() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship1);
		assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
	}

	@Test
	@DisplayName("Test addShip throws AssertionError when ship is null")
	void addShip5() {
		assertThrows(AssertionError.class, () -> fleet.addShip(null),
				"Error: expected AssertionError when ship is null.");
	}

	// --- getShipsLike ---

	@Test
	@DisplayName("Test getShipsLike returns only ships matching the given category")
	void getShipsLike1() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(ship1);
		fleet.addShip(ship2);

		List<IShip> barges = fleet.getShipsLike("Barca");
		assertAll("getShipsLike matching",
				() -> assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'."),
				() -> assertEquals(ship1, barges.getFirst(), "Error: The returned ship should match ship1.")
		);
	}

	@Test
	@DisplayName("Test getShipsLike returns empty list when no ship matches")
	void getShipsLike2() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		assertTrue(fleet.getShipsLike("Galeao").isEmpty(),
				"Error: Expected empty list when no ship matches category.");
	}

	@Test
	@DisplayName("Test getShipsLike throws AssertionError when category is null")
	void getShipsLike3() {
		assertThrows(AssertionError.class, () -> fleet.getShipsLike(null),
				"Error: expected AssertionError when category is null.");
	}

	// --- getFloatingShips ---

	@Test
	@DisplayName("Test getFloatingShips returns only ships still floating")
	void getFloatingShips() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(ship1);
		fleet.addShip(ship2);

		assertEquals(2, fleet.getFloatingShips().size(), "Error: All ships should be floating initially.");

		ship1.sink();
		List<IShip> floating = fleet.getFloatingShips();
		assertAll("getFloatingShips after sinking one",
				() -> assertEquals(1, floating.size(), "Error: Only one ship should be floating after sinking ship1."),
				() -> assertEquals(ship2, floating.getFirst(), "Error: Remaining floating ship should be ship2.")
		);
	}

	// --- getSunkShips ---

	@Test
	@DisplayName("Test getSunkShips returns only sunk ships")
	void getSunkShips() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(ship1);
		fleet.addShip(ship2);

		assertTrue(fleet.getSunkShips().isEmpty(), "Error: No ships should be sunk initially.");

		ship1.sink();
		List<IShip> sunk = fleet.getSunkShips();
		assertAll("getSunkShips after sinking one",
				() -> assertEquals(1, sunk.size(), "Error: Only one ship should be sunk."),
				() -> assertEquals(ship1, sunk.getFirst(), "Error: Sunk ship should be ship1.")
		);
	}

	// --- shipAt ---

	@Test
	@DisplayName("Test shipAt returns the ship occupying a position or null if empty")
	void shipAt1() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertAll("shipAt found and not found",
				() -> assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the ship at (1, 1)."),
				() -> assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty position (5, 5).")
		);
	}

	@Test
	@DisplayName("Test shipAt throws AssertionError when position is null")
	void shipAt2() {
		assertThrows(AssertionError.class, () -> fleet.shipAt(null),
				"Error: expected AssertionError when pos is null.");
	}

	// --- isCollisionFree (private) ---

	@Test
	@DisplayName("Test isCollisionFree detects overlapping and non-overlapping ships")
	void isCollisionFree1() throws Exception {
		Method method = Fleet.class.getDeclaredMethod("isCollisionFree", IShip.class);
		method.setAccessible(true);

		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip shipFar = new Barge(Compass.NORTH, new Position(5, 5));
		fleet.addShip(ship1);

		assertAll("isCollisionFree false and true",
				() -> assertFalse((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should not be collision free."),
				() -> assertTrue((Boolean) method.invoke(fleet, shipFar), "Error: Distant ships should be collision free.")
		);
	}

	@Test
	@DisplayName("Test isCollisionFree throws AssertionError when ship is null")
	void isCollisionFree2() throws Exception {
		Method method = Fleet.class.getDeclaredMethod("isCollisionFree", IShip.class);
		method.setAccessible(true);

		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> method.invoke(fleet, new Object[]{null}),
				"Error: expected InvocationTargetException wrapping AssertionError.");
		assertInstanceOf(AssertionError.class, ex.getCause(),
				"Error: expected AssertionError as cause when ship is null.");
	}

	// --- Print methods ---

	@Test
	@DisplayName("Test printShips prints non-empty and empty ship lists without throwing")
	void printShips1() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);
		assertAll("printShips valid lists",
				() -> assertDoesNotThrow(() -> fleet.printShips(fleet.getShips()), "Error: printShips should not throw for non-empty list."),
				() -> assertDoesNotThrow(() -> fleet.printShips(Collections.emptyList()), "Error: printShips should not throw for empty list.")
		);
	}

	@Test
	@DisplayName("Test printShips throws AssertionError when list is null")
	@SuppressWarnings("DataFlowIssue")
	void printShips2() {
		assertThrows(AssertionError.class, () -> fleet.printShips(null),
				"Error: expected AssertionError when ships list is null.");
	}

	@Test
	@DisplayName("Test printStatus executes without throwing")
	void printStatus() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
	}

	@Test
	@DisplayName("Test printShipsByCategory executes for valid category and throws on null")
	void printShipsByCategory() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		assertAll("printShipsByCategory",
				() -> assertDoesNotThrow(() -> fleet.printShipsByCategory("Barca"),
						"Error: printShipsByCategory should not throw for valid category."),
				() -> assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null),
						"Error: expected AssertionError when category is null.")
		);
	}

	@Test
	@DisplayName("Test printFloatingShips executes without throwing")
	void printFloatingShips() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		assertDoesNotThrow(fleet::printFloatingShips, "Error: printFloatingShips should not throw any exceptions.");
	}

	@Test
	@DisplayName("Test printAllShips executes without throwing")
	void printAllShips() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		assertDoesNotThrow(fleet::printAllShips, "Error: printAllShips should not throw any exceptions.");
	}
}