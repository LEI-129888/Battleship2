package battleship;

import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import battleship.history.GameRecord;
import battleship.history.GameHistoryWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 *  The constants about sound
	 */
	private static final String SPLASH = "splash";
	private static final String EXPLOSION = "explosion";
	private static final String ALARM = "alarm";

	/**
	 * The action executed by each menu command.
	 */
	private static final Map<Command, Consumer<MenuSession>> COMMAND_ACTIONS = Map.ofEntries(
			Map.entry(Command.GERAFROTA, Tasks::generateFleet),
			Map.entry(Command.LEFROTA, Tasks::loadFleet),
			Map.entry(Command.STATUS, Tasks::printFleetStatus),
			Map.entry(Command.MAPA, Tasks::printMap),
			Map.entry(Command.RAJADA, Tasks::fireSalvo),
			Map.entry(Command.SIMULA, Tasks::simulate),
			Map.entry(Command.TIROS, Tasks::printShots),
			Map.entry(Command.AJUDA, session -> menuHelp()),
			Map.entry(Command.HISTORICO, Tasks::printHistory),
			Map.entry(Command.PDF, Tasks::exportPdf),
			Map.entry(Command.SCOREBOARD, Tasks::showScoreboard),
			Map.entry(Command.TUI, Tasks::openTui),
			Map.entry(Command.TEMPO, Tasks::printLastTime),
			Map.entry(Command.DESCONHECIDO, session -> unknownCommand())
	);

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		MenuSession session = new MenuSession(new Scanner(System.in));
		menuHelp();

		System.out.print("> ");
		Command command = Command.fromKeyword(session.in.next());
		while (command != Command.DESISTIR) {
			COMMAND_ACTIONS.get(command).accept(session);
			System.out.print("> ");
			command = Command.fromKeyword(session.in.next());
		}

		saveForfeit(session);
		stopTimer(session.timer);
		System.out.println(GOODBYE_MESSAGE);
	}

	/**
	 * Gera uma frota aleatória e inicia um novo jogo
	 * @param session sessão atual do menu
	 */
	private static void generateFleet(MenuSession session) {
		session.myFleet = Fleet.createRandom();
		startGame(session);
	}

	/**
	 * Lê uma frota introduzida pelo utilizador e inicia um novo jogo
	 * @param session sessão atual do menu
	 */
	private static void loadFleet(MenuSession session) {
		session.myFleet = buildFleet(session.in);
		startGame(session);
	}

	/**
	 * Inicia um novo jogo com a frota da sessão
	 * @param session sessão atual do menu
	 */
	private static void startGame(MenuSession session) {
		session.game = new Game(session.myFleet);
		session.current = new GameRecord(); //inicializa o registo do jogo
		session.game.printMyBoard(false, true);
		session.timer.start();
	}

	/**
	 * Mostra o estado da frota
	 * @param session sessão atual do menu
	 */
	private static void printFleetStatus(MenuSession session) {
		if (session.myFleet != null)
			session.myFleet.printStatus();
	}

	/**
	 * Mostra o mapa da frota
	 * @param session sessão atual do menu
	 */
	private static void printMap(MenuSession session) {
		if (session.myFleet != null)
			session.game.printMyBoard(false, true);
	}

	/**
	 * Executa uma rajada do jogador e uma da IA, terminando o jogo se alguém ganhou
	 * @param session sessão atual do menu
	 */
	private static void fireSalvo(MenuSession session) {
		if (session.game != null) {
			session.lastAiResult = executeTurns(session.game, session.ai, session.in, session.lastAiResult, session.timer);

			if (session.game.getRemainingShips() == 0 || checkPlayerWon(session.game)) {
				endGame(session.game, session.historyWriter, session.current);
			}
		} else {
			System.out.println("Ainda não iniciou nenhum jogo.");
		}
	}

	/**
	 * Simula um jogo completo entre a IA do jogador e a IA inimiga
	 * @param session sessão atual do menu
	 */
	private static void simulate(MenuSession session) {
		if (session.game != null) {
			runSimulation(session);
		} else {
			System.out.println("Ainda não iniciou nenhum jogo.");
		}
	}

	/**
	 * Executa turnos simulados até o jogo acabar ou a simulação ser interrompida
	 * @param session sessão atual do menu
	 */
	private static void runSimulation(MenuSession session) {
		AIPlayer playerAi = new AIPlayer();
		String lastPlayerAiResult = "";

		while (session.game.getRemainingShips() > 0 && !checkPlayerWon(session.game)) {
			lastPlayerAiResult = simulatePlayerTurn(session.game, playerAi, lastPlayerAiResult);

			if (checkPlayerWon(session.game)) {
				printBoards(session.game, false);
				endGame(session.game, session.historyWriter, session.current);
				return;
			}

			simulateEnemyTurn(session);
			printBoards(session.game, false);

			if (session.game.getRemainingShips() == 0 || checkPlayerWon(session.game)) {
				endGame(session.game, session.historyWriter, session.current);
				return;
			}

			if (!waitBeforeNextTurn())
				return;
		}
	}

	/**
	 * Executa a rajada simulada da IA do jogador, regista o tempo e toca o som correspondente
	 * @param game jogo atual
	 * @param playerAi agente que joga pelo jogador
	 * @param lastPlayerAiResult json com o resultado da última rajada da IA do jogador
	 * @return resultado da nova rajada da IA do jogador
	 */
	private static String simulatePlayerTurn(IGame game, AIPlayer playerAi, String lastPlayerAiResult) {
		StopWatch turnTimer = new StopWatch();
		turnTimer.start();

		int enemyShipsBefore = game.getAlienFleet().getFloatingShips().size();
		int hitsBefore = game.getHits();

		firePlayerAiShots(game, playerAi, lastPlayerAiResult);

		turnTimer.stop();
		game.setLastTime(turnTimer.getTime());

		IMove lastPlayerMove = game.getMyMoves().getLast();
		String playerAiResult = lastPlayerMove.processEnemyFire(false);

		int enemyShipsAfter = game.getAlienFleet().getFloatingShips().size();
		int hitsAfter = game.getHits();
		SoundManager.play(decideSoundToPlay(enemyShipsBefore, enemyShipsAfter, hitsBefore, hitsAfter).concat(".mp3"));
		return playerAiResult;
	}

	/**
	 * Dispara os tiros da IA do jogador, ou tiros aleatórios se a IA falhar
	 * @param game jogo atual
	 * @param playerAi agente que joga pelo jogador
	 * @param lastPlayerAiResult json com o resultado da última rajada da IA do jogador
	 */
	private static void firePlayerAiShots(IGame game, AIPlayer playerAi, String lastPlayerAiResult) {
		System.out.println("\n[SIMULAÇÃO] IA do Jogador a processar...");
		String playerAiShotsJson = playerAi.nextShot(lastPlayerAiResult);
		if (playerAiShotsJson != null) {
			try {
				game.firePlayerShots(parseAiShots(playerAiShotsJson));
			} catch (Exception e) {
				game.randomPlayerFire();
			}
		} else {
			game.randomPlayerFire();
		}
	}

	/**
	 * Executa a rajada simulada da IA inimiga e guarda o resultado na sessão
	 * @param session sessão atual do menu
	 */
	private static void simulateEnemyTurn(MenuSession session) {
		System.out.println("\n[SIMULAÇÃO] IA Inimiga a processar...");
		String enemyAiShotsJson = session.ai.nextShot(session.lastAiResult);
		if (enemyAiShotsJson != null) {
			try {
				session.game.fireShots(parseAiShots(enemyAiShotsJson));
			} catch (Exception e) {
				session.game.randomEnemyFire();
			}
		} else {
			session.game.randomEnemyFire();
		}

		IMove lastEnemyMove = session.game.getAlienMoves().getLast();
		session.lastAiResult = lastEnemyMove.processEnemyFire(false);
	}

	/**
	 * Espera 3 segundos entre turnos simulados
	 * @return true se esperou, false se a espera foi interrompida
	 */
	private static boolean waitBeforeNextTurn() {
		try {
			Thread.sleep(3000);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	/**
	 * Mostra os tiros válidos realizados nos dois tabuleiros
	 * @param session sessão atual do menu
	 */
	private static void printShots(MenuSession session) {
		if (session.game != null) {
			System.out.println("\n--- O MEU TABULEIRO ---");
			session.game.printMyBoard(true, true);
			System.out.println("\n--- TABULEIRO INIMIGO ---");
			session.game.printAlienBoard(true, true);
		}
	}

	/**
	 * Mostra o histórico de partidas guardadas
	 * @param session sessão atual do menu
	 */
	private static void printHistory(MenuSession session) {
		List<GameRecord> historico = session.historyWriter.loadAllGames();
		System.out.println("================= HISTÓRICO DE PARTIDAS ===================");
		if(historico.isEmpty()) {
			System.out.println("Nenhuma partida gravada até ao momento.");
		} else {
			for (GameRecord r: historico) {
				System.out.printf("[%s] Resultado: %s | Tiros: %d | Acertos: %d%n",
						r.getTimestamp(), r.getOutcome(), r.getTotalShots(), r.getTotalHits());
			}
		}
		System.out.println("===========================================================");
	}

	/**
	 * Exporta as jogadas do jogo atual para PDF
	 * @param session sessão atual do menu
	 */
	private static void exportPdf(MenuSession session) {
		if (session.game != null) {
			List<IMove> movesToExport = !session.game.getMyMoves().isEmpty() ? session.game.getMyMoves() : session.game.getAlienMoves();
			PdfExporter.exportMovesToPdf(movesToExport, "relatorio_jogadas.pdf");
		} else {
			System.out.println("Ainda nao iniciou nenhum jogo!");
		}
	}

	/**
	 * Guarda a pontuação do jogo atual, se existir, e mostra o scoreboard
	 * @param session sessão atual do menu
	 */
	private static void showScoreboard(MenuSession session) {
		IGame game = session.game;
		if (game != null && (!game.getMyMoves().isEmpty() || !game.getAlienMoves().isEmpty())) {
			int totalMoves = !game.getMyMoves().isEmpty() ? game.getMyMoves().size() : game.getAlienMoves().size();
			boolean playerWon = checkPlayerWon(game) && game.getRemainingShips() > 0;
			ScoreBoardManager.saveScore("Jogador", totalMoves, game.getHits(), playerWon);
		}
		ScoreBoardManager.printScoreboard();
	}

	/**
	 * Abre o tabuleiro gráfico no terminal
	 * @param session sessão atual do menu
	 */
	private static void openTui(MenuSession session) {
		if (session.game != null) {
			new BoardTUI((Game) session.game, session.current, session.historyWriter, session.ai).start();
		} else
			System.out.println("Ainda não iniciou nenhum jogo.");
	}

	/**
	 * Mostra o tempo da última rajada do jogador
	 * @param session sessão atual do menu
	 */
	private static void printLastTime(MenuSession session) {
		if (session.game != null) {
			if (session.game.getLastTime() != -1)
				System.out.println("Tempo da última jogada: " + session.game.getLastTime()/1000.0 + "s");
			else
				System.out.println("Ainda nào foi executada nenhuma rajada.");
			session.game.printMyBoard(true, false);
		}
		else
			System.out.println("Ainda não foi iniciado nenhum jogo.");
	}

	/**
	 * Avisa o utilizador de que o comando não existe
	 */
	private static void unknownCommand() {
		System.out.println("Que comando é esse??? Repete ...");
	}

	/**
	 * Guarda o jogo atual como desistência, se ainda não tiver resultado
	 * @param session sessão atual do menu
	 */
	private static void saveForfeit(MenuSession session) {
		GameRecord current = session.current;
		if (current != null && current.getOutcome() == null) {
			current.setOutcome("DESISTÊNCIA");
			if (session.game != null) {
				List<IMove> movesToSave = !session.game.getMyMoves().isEmpty() ? session.game.getMyMoves() : session.game.getAlienMoves();
				for (IMove move : movesToSave) {
					current.addMove(move);
				}
			}
			session.historyWriter.saveGame(current);
		}
	}

	/**
	 * Para o cronómetro, se estiver a contar
	 * @param timer cronómetro das jogadas
	 */
	private static void stopTimer(StopWatch timer) {
		if (timer.isStarted()) {
			timer.stop();
		}
		// Quando desiste simplesmente para o timer
		if (timer.isStarted()) timer.stop(); //apanhamos exceção
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + Command.GERAFROTA.getKeyword() + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + Command.LEFROTA.getKeyword() + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + Command.STATUS.getKeyword() + ": Mostra o status atual da frota.");
		System.out.println("- " + Command.MAPA.getKeyword() + ": Exibe o mapa da frota.");
		System.out.println("- " + Command.RAJADA.getKeyword() + ": Realiza uma rajada de disparos.");
		System.out.println("- " + Command.SIMULA.getKeyword() + ": Simula um jogo completo.");
		System.out.println("- " + Command.TIROS.getKeyword() + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + Command.HISTORICO.getKeyword() + ": Exibe o histórico de partidas guardadas.");
		System.out.println("- " + Command.TUI.getKeyword() + ": Exibe o tabuleiro gráfico no terminal.");
		System.out.println("- " + Command.TEMPO.getKeyword() + ": Exibe o tempo que o jogador demorou para fazer a última rajada.");
		System.out.println("- " + Command.SCOREBOARD.getKeyword() + ": Imprime dados do jogo na consola");
		System.out.println("- " + Command.PDF.getKeyword() + ": Gera o ficheiro pdf com os dados que são impressos na consola no scoreboard");
		System.out.println("- " + Command.DESISTIR.getKeyword() + ": Encerra o jogo.");
		System.out.println("===============================================================");
	}
	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0; // i represents the total of successfully created ships
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, build it and returns it
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {

		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {

		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		// Verifica se ainda há tokens disponíveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next(); // Primeiro token
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next(); // Segundo token, se disponível
		}

		String input = (part2 != null) ? part1 + part2 : part1;

		// Normalizar o input para tratar letras maiúsculas e minúsculas
		input = input.toUpperCase();

		// Verificar os dois formatos possíveis: compactos e com espaço
		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(input.substring(1)); // Extrair a linha
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(part2); // Extrair a linha
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}

	/**
	 * Decides which sound should be played based on ships before and after the move and hits before and after the move
	 * @param shipsBefore Number of ships remaining before the move
	 * @param shipsAfter Number of ships remaining after the move
	 * @param hitsBefore Number of total hits before the move
	 * @param hitsAfter Number of total hits after the move
	 * @return String which represents the soundFileName at /src/java/resources
	 */
	public static String decideSoundToPlay(int shipsBefore, int shipsAfter, int hitsBefore, int hitsAfter) {
		if (shipsAfter < shipsBefore)
			return ALARM;
		else if (hitsAfter > hitsBefore)
			return EXPLOSION;
		else return SPLASH;
	}

	/**
	 * Executa um turno, uma rajada do jogador e uma da IA
	 * @param game jogo atual
	 * @param ai agente que joga pelo adversário
	 * @param in scanner do STDIN
	 * @param lastAiResult json com o resultado da última rajada da IA
	 * @param timer instancia de timer para contar o tempo das jogadas
	 * @return resultado da nova rajada da IA
	 */
	private static String executeTurns(IGame game, AIPlayer ai, Scanner in, String lastAiResult, StopWatch timer) {
		int enemyShipsBefore = game.getAlienFleet().getFloatingShips().size();
		int hitsBefore = game.getHits();

		game.readPlayerFire(in);

		recordTurnTime(game, timer);

		int enemyShipsAfter = game.getAlienFleet().getFloatingShips().size();
		int hitsAfter = game.getHits();

		SoundManager.play(decideSoundToPlay(enemyShipsBefore, enemyShipsAfter, hitsBefore, hitsAfter).concat(".mp3"));

		String newAiReport = lastAiResult;
		if (!checkPlayerWon(game)) {
			newAiReport = executeAiTurn(game, ai, lastAiResult);
		}

		printBoards(game, true);

		timer.start();
		return newAiReport;
	}

	private static void printBoards(IGame game, boolean showLegend) {
		System.out.println("\n--- O MEU TABULEIRO ---");
		game.printMyBoard(true, showLegend);
		System.out.println("\n--- TABULEIRO INIMIGO ---");
		game.printAlienBoard(true, showLegend);

		System.out.println("Tempo da jogada: " + (game.getLastTime() / 1000.0) + "s");
	}

	private static String executeAiTurn(IGame game, AIPlayer ai, String lastAiResult) {
		String newAiReport;
		System.out.println("IA a processar...");
		String aiShotsJson = ai.nextShot(lastAiResult);
		if (aiShotsJson != null) {
			try {
				List<IPosition> aiShots = parseAiShots(aiShotsJson);
				game.fireShots(aiShots);
			} catch (Exception e) {
				game.randomEnemyFire();
			}
		} else {
			game.randomEnemyFire();
		}
		IMove lastMove = game.getAlienMoves().getLast();
		newAiReport = lastMove.processEnemyFire(false);
		return newAiReport;
	}

	private static void recordTurnTime(IGame game, StopWatch timer) {
		if (timer.isStarted()) {
			timer.stop();
		}
		game.setLastTime(timer.getTime());
		timer.reset();
	}

	/**
	 * Converte coordenadas em JSON geradas pela IA numa lista de IPositions.
	 * @param jsonShots coordenadas geradas pela IA
	 * @return lista de IPositions
	 */
	public static List<IPosition> parseAiShots(String jsonShots) {
		List<IPosition> shots = new ArrayList<>();
		try {
			String cleanJson = jsonShots.replace("```json", "").replace("```", "").trim();
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(cleanJson);
			for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
				char charRow = node.get("row").asText().toUpperCase().charAt(0);
				int row = charRow - 'A';
				int col = node.get("column").asInt() - 1;
				shots.add(new Position(row, col));
			}
		} catch (Exception e) {
			System.err.println("Erro ao ler o JSON da IA: " + e.getMessage());
			throw new RuntimeException("Formato inválido devolvido pela IA.");
		}
		return shots;
	}

	private static void endGame(IGame game, GameHistoryWriter historyWriter, GameRecord current) {
		if (current != null && current.getOutcome() == null) {
			current.setOutcome(game.getRemainingShips() == 0 ? "DERROTA" : "VITÓRIA");
			List<IMove> movesToSave = !game.getMyMoves().isEmpty() ? game.getMyMoves() : game.getAlienMoves();
			for (IMove move : movesToSave) {
				current.addMove(move);
			}
			historyWriter.saveGame(current);
		}
		game.over();
		System.exit(0);
	}

	/**
	 * Verifica se o jogador afundou toda a frota do oponente
	 * @param game estado do jogo a verificasr
	 * @return true se afundou, false se não
	 */
	private static boolean checkPlayerWon(IGame game) {
		return game.getAlienFleet().getFloatingShips().isEmpty();
	}

}