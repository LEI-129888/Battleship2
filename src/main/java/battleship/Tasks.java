package battleship;

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
						timer.stop();
						System.out.println("Tempo da jogada: "+ timer.getTime()/1000.0 + "s");
						game.setLastTime(timer.getTime());
						timer.reset();
						game.readEnemyFire(in);
						myFleet.printStatus();
						game.printMyBoard(true, false);

						if (game.getRemainingShips() == 0) {
							if (current != null) { //gravar no historico antes de terminar o jogo
								current.setOutcome("DERROTA");
								for (IMove move : game.getAlienMoves()) current.addMove(move); //copia todas as jogadas feitas para a gravação do jogo

								historyWriter.saveGame(current);
							}

							game.over();
							System.exit(0);
						}
						else
							// Timer só é reiniciado quando o jogo ainda não acabou
							timer.start();
					}
					break;
				case SIMULA:
					if (game != null) {
						while (game.getRemainingShips() > 0){
							game.randomEnemyFire();
							myFleet.printStatus();
							game.printMyBoard(true, false);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt(); // Best practice: restore interrupt status
							}
						}

						if (game.getRemainingShips() == 0) {
							if (current != null) { //gravar no histórico- VERIFICAR SE QUEREMOS GUARDAR UMA SIMULAÇÃO NO HISTORICO
								current.setOutcome("DERROTA");
								for (IMove move : game.getAlienMoves()) current.addMove(move);

								historyWriter.saveGame(current);
							}
							game.over();
							System.exit(0);
						}
					}
					break;
				case TIROS:
					if (game != null)
						game.printMyBoard(true, true);
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
						PdfExporter.exportMovesToPdf(game.getAlienMoves(), "relatorio_jogadas.pdf");
					} else {
						System.out.println("Ainda nao iniciou nenhum jogo!");
					}
					break;

				case SCOREBOARD:
					if (game != null && !game.getAlienMoves().isEmpty()) {
						// Guarda o estado atual da partida para poderes ver logo na tabela
						ScoreBoardManager.saveScore("Jogador", game.getAlienMoves().size(), game.getHits(), game.getRemainingShips() == 0);
					}
					ScoreBoardManager.printScoreboard();
					break;

				case TUI:
					if (game != null) {
						BoardTUI tui = new BoardTUI((Game) game, current, historyWriter);
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
		if (current != null) { //guardar no histórico antes da desistencia, se o jogo já tiver começado
			current.setOutcome("DESISTÊNCIA");
			if(game != null) {
				for (IMove move : game.getAlienMoves())
					current.addMove(move); //copia as jogadas da partida para a sessão
			}
			historyWriter.saveGame(current); //salvar a sessão no ficheiro
		}
		// Quando desiste simplesmente para o timer
		timer.stop();
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

}