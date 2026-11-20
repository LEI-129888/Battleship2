package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Position.
 * Author: britoeabreu
 * Date: 2024-03-19 15:30
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getRow: 1
 * - getColumn: 1
 * - isValid: 4
 * - isAdjacentTo: 4
 * - isOccupied: 1
 * - isHit: 1
 * - occupy: 1
 * - shoot: 1
 * - equals: 3
 * - hashCode: 1
 * - toString: 1
 */
public class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	//	position = new Position('C', 4);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@Test
	@DisplayName("Construtor com coordenadas inteiras cria uma posição livre e não atingida")
	void constructor1() {
		Position pos = new Position(1, 1);
		assertNotNull(pos, "Failed to create Position: object is null");
		assertEquals(1, pos.getRow(), "Failed to set row: expected 1 but got " + pos.getRow());
		assertEquals(1, pos.getColumn(), "Failed to set column: expected 1 but got " + pos.getColumn());
		assertFalse(pos.isOccupied(), "New position should not be occupied");
		assertFalse(pos.isHit(), "New position should not be hit");
	}

	@Test
	@DisplayName("Construtor com notação clássica converte linha e coluna corretamente")
	void constructor2() {
		Position pos = new Position('A', 1);
		assertEquals(0, pos.getRow(), "Character 'A' should map to row 0");
		assertEquals(0, pos.getColumn(), "Column 1 should map to internal column 0");
		assertFalse(pos.isOccupied());
		assertFalse(pos.isHit());
	}

	@Test
	@DisplayName("getRow devolve a linha correta")
	void getRow() {
		assertEquals(2, position.getRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("getColumn devolve a coluna correta")
	void getColumn() {
		assertEquals(3, position.getColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@Test
	@DisplayName("getClassicRow devolve a letra da linha correta")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("getClassicColumn devolve o número da coluna correto")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@Test
	@DisplayName("isInside devolve true para uma posição dentro do tabuleiro")
	void isValid1() {
		position = new Position(0, 0);
		assertTrue(position.isInside(), "Position (0,0) should be valid");
	}

	@Test
	@DisplayName("isInside devolve false para uma linha negativa")
	void isValid2() {
		position = new Position(-1, 5);
		assertFalse(position.isInside(), "Position with negative row should be invalid");
	}

	@Test
	@DisplayName("isInside devolve false para uma coluna negativa")
	void isValid3() {
		position = new Position(5, -1);
		assertFalse(position.isInside(), "Position with negative column should be invalid");
	}

	@Test
	@DisplayName("isInside devolve false para uma linha igual ou superior a BOARD_SIZE")
	void isValid4() {
		position = new Position(Game.BOARD_SIZE, 5);
		assertFalse(position.isInside(), "Position with row >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("isInside devolve false para uma coluna igual ou superior a BOARD_SIZE")
	void isValid5() {
		position = new Position(5, Game.BOARD_SIZE);
		assertFalse(position.isInside(), "Position with column >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("isAdjacentTo deteta uma posição adjacente na horizontal")
	void isAdjacentTo1() {
		Position other = new Position(2, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect horizontally adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo deteta uma posição adjacente na vertical")
	void isAdjacentTo2() {
		Position other = new Position(3, 3);
		assertTrue(position.isAdjacentTo(other), "Failed to detect vertically adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo deteta uma posição adjacente na diagonal")
	void isAdjacentTo3() {
		Position other = new Position(3, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect diagonally adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo devolve false para uma posição não adjacente")
	void isAdjacentTo4() {
		Position other = new Position(4, 5);
		assertFalse(position.isAdjacentTo(other), "Non-adjacent position incorrectly identified as adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo devolve false quando a diferença de colunas é superior a 1")
	void isAdjacentTo5() {
		Position other = new Position(2, 5);
		assertFalse(position.isAdjacentTo(other), "A position with column difference > 1 should not be adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo lança NullPointerException para argumento nulo")
	void isAdjacentToWithNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"isAdjacentTo should throw NullPointerException for null input");
	}

	@Test
	@DisplayName("isOccupied passa a true depois de occupy")
	void isOccupied() {
		assertFalse(position.isOccupied(), "New position should not be occupied");
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@Test
	@DisplayName("isHit passa a true depois de shoot")
	void isHit() {
		assertFalse(position.isHit(), "New position should not be hit");
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	@Test
	@DisplayName("equals devolve true para posições com as mesmas coordenadas")
	void equals1() {
		Position same = new Position(2, 3);
        assertEquals(position, same, "Equal positions not identified as equal");
	}

	@Test
	@DisplayName("equals devolve false quando comparado com null")
	void equals2() {
        assertNotEquals(null, position, "Position should not equal null");
	}

	@Test
	@DisplayName("equals devolve false quando comparado com um objeto de outro tipo")
	void equals3() {
		Object other = new Object();
        assertNotEquals(position, other, "Position should not equal non-Position object");
	}

	@Test
	@DisplayName("equals devolve false para a mesma linha e coluna diferente")
	void equals4() {
		Position other = new Position(2, 4);
        assertNotEquals(position, other, "Positions with the same row but different column should not be equal");
	}

	@SuppressWarnings("EqualsWithItself")
	@Test
	@DisplayName("equals devolve true quando comparado consigo próprio")
	void equals5() {
        assertEquals(position, position, "A position should be equal to itself");
	}

	@Test
	@DisplayName("equals devolve false para linha diferente e a mesma coluna")
	void equals6() {
		Position other = new Position(1, 3);
        assertNotEquals(position, other, "Positions with different row but same column should not be equal");
	}

	@Test
	@DisplayName("hashCode é igual para posições iguais")
	void hashCodeConsistency() {
		Position same = new Position(2, 3);
		assertEquals(position.hashCode(), same.hashCode(),
				"Hash codes not consistent for equal positions");
	}

	@Test
	@DisplayName("toString devolve a posição em notação clássica")
	void toStringFormat() {
		String expected = "C4";
		assertEquals(expected, position.toString(),
				"Incorrect string representation: expected '" + expected +
						"' but got '" + position.toString() + "'");
	}

	@Test
	@DisplayName("randomPosition devolve uma posição não nula dentro do tabuleiro")
	void randomPosition() {
		Position pos = Position.randomPosition();
		assertNotNull(pos, "Random position should not be null");
		assertTrue(pos.isInside(), "Random position must be inside the board bounds");
	}

	@Test
	@DisplayName("adjacentPositions devolve 8 posições para uma posição central")
	void adjacentPositions1() {
		Position middlePos = new Position(5, 5);
		assertEquals(8, middlePos.adjacentPositions().size(), "Middle position should have 8 adjacent positions");
	}

	@Test
	@DisplayName("adjacentPositions devolve 3 posições para um canto")
	void adjacentPositions2() {
		Position cornerPos = new Position(0, 0);
		assertEquals(3, cornerPos.adjacentPositions().size(), "Corner position should have only 3 valid adjacent positions");
	}
}