package battleship;

import java.util.List;

/**
 * The type Carrack represents a ship with a size of 3 units.
 * It is positioned on the game board based on its bearing and initial position.
 *
 * Author: britoeabreu
 * Date: 2023-10-10
 * Time: 15:30
 */
public class Carrack extends Ship {

	/**
	 * Instantiates a new Carrack.
	 *
	 * @param bearing The bearing of the ship (NORTH, SOUTH, EAST, or WEST).
	 * @param pos     The initial position of the ship on the game board.
	 */
	public Carrack(Compass bearing, IPosition pos) {
		super("Nau", bearing, pos, 3);

		switch (bearing) {
			case SOUTH:
				addVerticalPositions(pos);
				break;
			case NORTH:
				addVerticalPositions(pos);
				break;
			case EAST:
				addHorizontalPositions(pos);
				break;
			case WEST:
				addHorizontalPositions(pos);
				break;
		}
	}

	/**
	 * Adds the ship's positions horizontally, starting at the given position.
	 *
	 * @param pos the initial position of the ship
	 */
	private void addHorizontalPositions(IPosition pos) {
		for (int c = 0; c < this.getSize(); c++)
			getPositions().add(new Position(pos.getRow(), pos.getColumn() + c));
	}

	/**
	 * Adds the ship's positions vertically, starting at the given position.
	 *
	 * @param pos the initial position of the ship
	 */
	private void addVerticalPositions(IPosition pos) {
		for (int r = 0; r < this.getSize(); r++)
			getPositions().add(new Position(pos.getRow() + r, pos.getColumn()));
	}
}