package battleship;

import java.util.Scanner;

import org.apache.commons.lang3.time.StopWatch;
import battleship.history.GameRecord;
import battleship.history.GameHistoryWriter;

/**
 * The state of a menu session, shared by all the menu commands.
 */
class MenuSession {
	/**
	 * The scanner used to read the user's commands.
	 */
	final Scanner in;

	/**
	 * The timer used to measure the time of each turn.
	 */
	final StopWatch timer = new StopWatch();

	/**
	 * The writer used to save the game history.
	 */
	final GameHistoryWriter historyWriter = new GameHistoryWriter(); //para escrever o historico

	/**
	 * The AI that plays for the opponent.
	 */
	final AIPlayer ai = new AIPlayer();

	/**
	 * The player's fleet.
	 */
	IFleet myFleet = null;

	/**
	 * The current game.
	 */
	IGame game = null;

	/**
	 * The record of the current game.
	 */
	GameRecord current = null; //guardar este jogo

	/**
	 * The result of the last AI salvo, in JSON.
	 */
	String lastAiResult = "";

	/**
	 * Instantiates a new menu session.
	 *
	 * @param in the scanner used to read the user's commands
	 */
	MenuSession(Scanner in) {
		this.in = in;
	}
}
