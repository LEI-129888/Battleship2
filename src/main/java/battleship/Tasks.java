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
	 * Strings to be used by the user
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String HISTORICO = "historico"; //devemos poder ver um histórico dos jogos a partir do menu
	private static final String PDF = "pdf";
	private static final String SCOREBOARD = "scoreboard";
	private static final String TUI = "tui";
	private static final String TEMPO = "tempo";

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		IFleet myFleet = null;
		IGame game = null;
		StopWatch timer = new StopWatch();
		GameHistoryWriter historyWriter = new GameHistoryWriter(); //para escrever o historico
		GameRecord current = null; //guardar este jogo
		AIPlayer ai = new AIPlayer();
		String lastAiResult = "";
		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(DESISTIR)) {

			switch (command) {
				case GERAFROTA:
					myFleet = Fleet.createRandom();
					game = new Game(myFleet);
					current =  new GameRecord(); //inicializa o registo do jogo
					game.printMyBoard(false, true);
					timer.start();
					break;
				case LEFROTA:
					myFleet = buildFleet(in);
					game = new Game(myFleet);
					current = new GameRecord();
					game.printMyBoard(false, true);
					timer.start();
					break;
				case STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
					if (game != null) {
						lastAiResult = executeTurns(game, ai, in, lastAiResult, timer);

						if (game.getRemainingShips() == 0 || checkPlayerWon(game)) {
							endGame(game, historyWriter, current);
						}
					} else {
						System.out.println("Ainda não iniciou nenhum jogo.");
					}
					break;
				case SIMULA:
					if (game != null) {
						AIPlayer playerAi = new AIPlayer();
						String lastPlayerAiResult = "";

						while (game.getRemainingShips() > 0 && !checkPlayerWon(game)) {
							StopWatch turnTimer = new StopWatch();
							turnTimer.start();

							int enemyShipsBefore = game.getAlienFleet().getFloatingShips().size();
							int hitsBefore = game.getHits();

							System.out.println("\n[SIMULAÇÃO] IA do Jogador a processar...");
							String playerAiShotsJson = playerAi.nextShot(lastPlayerAiResult);
							if (playerAiShotsJson != null) {
								try {
									List<IPosition> playerShots = parseAiShots(playerAiShotsJson);
									game.firePlayerShots(playerShots);
								} catch (Exception e) {
									game.randomPlayerFire();
								}
							} else {
								game.randomPlayerFire();
							}

							turnTimer.stop();
							game.setLastTime(turnTimer.getTime());

							IMove lastPlayerMove = game.getMyMoves().getLast();
							lastPlayerAiResult = lastPlayerMove.processEnemyFire(false);

							int enemyShipsAfter = game.getAlienFleet().getFloatingShips().size();
							int hitsAfter = game.getHits();
							SoundManager.play(decideSoundToPlay(enemyShipsBefore, enemyShipsAfter, hitsBefore, hitsAfter).concat(".mp3"));

							if (checkPlayerWon(game)) {
								System.out.println("\n--- O MEU TABULEIRO ---");
								game.printMyBoard(true, false);
								System.out.println("\n--- TABULEIRO INIMIGO ---");
								game.printAlienBoard(true, false);
								System.out.println("Tempo da jogada: " + (game.getLastTime() / 1000.0) + "s");
								endGame(game, historyWriter, current);
								break;
							}

							System.out.println("\n[SIMULAÇÃO] IA Inimiga a processar...");
							String enemyAiShotsJson = ai.nextShot(lastAiResult);
							if (enemyAiShotsJson != null) {
								try {
									List<IPosition> enemyShots = parseAiShots(enemyAiShotsJson);
									game.fireShots(enemyShots);
								} catch (Exception e) {
									game.randomEnemyFire();
								}
							} else {
								game.randomEnemyFire();
							}

							IMove lastEnemyMove = game.getAlienMoves().getLast();
							lastAiResult = lastEnemyMove.processEnemyFire(false);

							System.out.println("\n--- O MEU TABULEIRO ---");
							game.printMyBoard(true, false);
							System.out.println("\n--- TABULEIRO INIMIGO ---");
							game.printAlienBoard(true, false);
							System.out.println("Tempo da jogada: " + (game.getLastTime() / 1000.0) + "s");

							if (game.getRemainingShips() == 0 || checkPlayerWon(game)) {
								endGame(game, historyWriter, current);
								break;
							}

							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								break;
							}
						}
					} else {
						System.out.println("Ainda não iniciou nenhum jogo.");
					}
					break;
				case TIROS:
					if (game != null) {
						System.out.println("\n--- O MEU TABULEIRO ---");
						game.printMyBoard(true, true);
						System.out.println("\n--- TABULEIRO INIMIGO ---");
						game.printAlienBoard(true, true);
					}
					break;
                case AJUDA:
                    menuHelp();
                    break;
				case HISTORICO:
					List<GameRecord> historico = historyWriter.loadAllGames();
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
					break;
				case PDF:
					if (game != null) {
						List<IMove> movesToExport = !game.getMyMoves().isEmpty() ? game.getMyMoves() : game.getAlienMoves();
						PdfExporter.exportMovesToPdf(movesToExport, "relatorio_jogadas.pdf");
					} else {
						System.out.println("Ainda nao iniciou nenhum jogo!");
					}
					break;

				case SCOREBOARD:
					if (game != null && (!game.getMyMoves().isEmpty() || !game.getAlienMoves().isEmpty())) {
						int totalMoves = !game.getMyMoves().isEmpty() ? game.getMyMoves().size() : game.getAlienMoves().size();
						boolean playerWon = checkPlayerWon(game) && game.getRemainingShips() > 0;
						ScoreBoardManager.saveScore("Jogador", totalMoves, game.getHits(), playerWon);
					}
					ScoreBoardManager.printScoreboard();
					break;

				case TUI:
					if (game != null) {
						BoardTUI tui = new BoardTUI((Game) game, current, historyWriter, ai);
						tui.start();
					} else
						System.out.println("Ainda não iniciou nenhum jogo.");
					break;


				case TEMPO:
					if (game != null) {
						if (game.getLastTime() != -1)
							System.out.println("Tempo da última jogada: " + game.getLastTime()/1000.0 + "s");
						else
							System.out.println("Ainda nào foi executada nenhuma rajada.");
						game.printMyBoard(true, false);
					}
					else
						System.out.println("Ainda não foi iniciado nenhum jogo.");
					break;
				default:
					System.out.println("Que comando é esse??? Repete ...");
			}
			System.out.print("> ");
			command = in.next();
		}

		if (current != null && current.getOutcome() == null) {
			current.setOutcome("DESISTÊNCIA");
			if (game != null) {
				List<IMove> movesToSave = !game.getMyMoves().isEmpty() ? game.getMyMoves() : game.getAlienMoves();
				for (IMove move : movesToSave) {
					current.addMove(move);
				}
			}
			historyWriter.saveGame(current);
		}
		if (timer.isStarted()) {
			timer.stop();
		}
		System.out.println(GOODBYE_MESSAGE);
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + GERAFROTA + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + LEFROTA + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + STATUS + ": Mostra o status atual da frota.");
		System.out.println("- " + MAPA + ": Exibe o mapa da frota.");
		System.out.println("- " + RAJADA + ": Realiza uma rajada de disparos.");
		System.out.println("- " + SIMULA + ": Simula um jogo completo.");
		System.out.println("- " + TIROS + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + HISTORICO + ": Exibe o histórico de partidas guardadas.");
		System.out.println("- " + TUI + ": Exibe o tabuleiro gráfico no terminal.");
		System.out.println("- " + TEMPO + ": Exibe o tempo que o jogador demorou para fazer a última rajada.");
		System.out.println("- " + SCOREBOARD + ": Imprime dados do jogo na consola");
		System.out.println("- " + PDF + ": Gera o ficheiro pdf com os dados que são impressos na consola no scoreboard");
		System.out.println("- " + DESISTIR + ": Encerra o jogo.");
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

		if (timer.isStarted()) {
			timer.stop();
		}
		game.setLastTime(timer.getTime());
		timer.reset();

		int enemyShipsAfter = game.getAlienFleet().getFloatingShips().size();
		int hitsAfter = game.getHits();

		SoundManager.play(decideSoundToPlay(enemyShipsBefore, enemyShipsAfter, hitsBefore, hitsAfter).concat(".mp3"));

		String newAiReport = lastAiResult;
		if (!checkPlayerWon(game)) {
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
		}

		System.out.println("\n--- O MEU TABULEIRO ---");
		game.printMyBoard(true, true);
		System.out.println("\n--- TABULEIRO INIMIGO ---");
		game.printAlienBoard(true, true);

		System.out.println("Tempo da jogada: " + (game.getLastTime() / 1000.0) + "s");

		timer.start();
		return newAiReport;
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