package battleship;

/**
 * The commands available in the game menu.
 */
public enum Command {
	AJUDA("ajuda"),
	GERAFROTA("gerafrota"),
	LEFROTA("lefrota"),
	DESISTIR("desisto"),
	RAJADA("rajada"),
	TIROS("tiros"),
	MAPA("mapa"),
	STATUS("estado"),
	SIMULA("simula"),
	HISTORICO("historico"), //devemos poder ver um histórico dos jogos a partir do menu
	PDF("pdf"),
	SCOREBOARD("scoreboard"),
	TUI("tui"),
	TEMPO("tempo"),
	DESCONHECIDO(null);

	/**
	 * The text the user types to use this command.
	 */
	private final String keyword;

	/**
	 * Instantiates a new Command.
	 *
	 * @param keyword the text the user types to use this command
	 */
	Command(String keyword) {
		this.keyword = keyword;
	}

	/**
	 * Gets the keyword.
	 *
	 * @return the text the user types to use this command
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * Converts the text typed by the user into a command.
	 *
	 * @param text the text typed by the user
	 * @return the matching command, or DESCONHECIDO if there is none
	 */
	public static Command fromKeyword(String text) {
		for (Command c : values())
			if (c.keyword != null && c.keyword.equals(text))
				return c;
		return DESCONHECIDO;
	}
}
