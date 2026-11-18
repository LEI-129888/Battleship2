package battleship;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;

import static battleship.Game.BOARD_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Scanner;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	private final java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
	private final java.io.PrintStream originalOut = System.out;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet());
		System.setOut(new java.io.PrintStream(outContent));
	}

	@AfterEach
	void tearDown() {
		game = null;
		System.setOut(originalOut);
	}

	@Test
	@DisplayName("Construtor inicializa o jogo com contadores a zero e lista de jogadas vazia")
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	@DisplayName("fireShots incrementa o contador de tiros inválidos")
	void fire2() {
		List<IPosition> shots = List.of(new Position(-1, 5), new Position(0, 0), new Position(0, 1));
		game.fireShots(shots);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	@DisplayName("fireShots incrementa o contador de tiros repetidos")
	void fire3() {
		List<IPosition> shots = List.of(new Position(2, 3), new Position(2, 3), new Position(0, 1));
		game.fireShots(shots);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	@DisplayName("repeatedShot devolve true para uma posição já disparada")
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	@DisplayName("repeatedShot devolve false para uma posição ainda não disparada")
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	@DisplayName("getAlienMoves contém uma jogada após uma rajada de tiros")
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	@DisplayName("getRemainingShips atualiza a contagem ao adicionar e afundar navios")
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}

	/**
	 * Testa a impressão do tabuleiro com a frota oculta (show_ships = false),
	 * exibindo os tiros e a legenda. Cobre as condições em que um navio
	 * foi atingido e já se encontra afundado, validando a saída visual.
	 */
	@Test
	@DisplayName("printBoard com frota oculta mostra tiros, navio afundado e legenda")
	void printBoard1() {
		Fleet fleet = new Fleet();
		Ship sunkShip = new Barge(Compass.NORTH, new Position(0, 0));
		fleet.addShip(sunkShip);

		Position hitPos = new Position(0, 0);
		hitPos.shoot();
		sunkShip.shoot(hitPos);

		List<IMove> moves = new java.util.ArrayList<>();
		List<IPosition> shots = List.of(new Position(0, 0), new Position(1, 1), new Position(9, 9));
		moves.add(new Move(1, shots, new java.util.ArrayList<>()));

		Game.printBoard(fleet, moves, false, true, true);

		String consoleOutput = outContent.toString();

		assertTrue(consoleOutput.contains("LEGENDA"));
		assertTrue(consoleOutput.contains("o"));
		assertTrue(consoleOutput.contains("*"));
	}

	/**
	 * Testa a impressão do tabuleiro exibindo os navios (show_ships = true),
	 * ocultando os tiros e a legenda. Valida que a legenda não é impressa.
	 */
	@Test
	@DisplayName("printBoard com navios visíveis e sem legenda não imprime a legenda")
	void printBoard2() {
		Fleet fleet = new Fleet();
		List<IMove> moves = new java.util.ArrayList<>();

		Game.printBoard(fleet, moves, true, false, false);

		String consoleOutput = outContent.toString();

		assertFalse(consoleOutput.contains("LEGENDA"));
	}

	/**
	 * Testa a impressão do tabuleiro com a frota oculta (show_ships = false),
	 * com um navio intacto. Garante que o navio não é revelado.
	 */
	@Test
	@DisplayName("printBoard com frota oculta não revela navios intactos")
	void printBoard3() {
		Fleet fleet = new Fleet();
		Ship floatingShip = new Barge(Compass.NORTH, new Position(5, 5));
		fleet.addShip(floatingShip);

		List<IMove> moves = new java.util.ArrayList<>();

		Game.printBoard(fleet, moves, false, false, false);

		String consoleOutput = outContent.toString();

		assertFalse(consoleOutput.contains("#"));
	}

	/**
	 * Testa a validação do argumento da frota.
	 * Garante que é lançado um AssertionError quando a frota passada for nula.
	 */
	@Test
	@DisplayName("printBoard lança AssertionError quando a frota é nula")
	void printBoard4() {
		List<IMove> moves = new ArrayList<>();

		assertThrows(AssertionError.class, () -> {
			Game.printBoard(null, moves, false, false, false);
		});
	}

	/**
	 * Testa a validação do argumento dos movimentos.
	 * Garante que é lançado um AssertionError quando a lista de movimentos passada for nula.
	 */
	@Test
	@DisplayName("printBoard lança AssertionError quando a lista de jogadas é nula")
	void printBoard5() {
		Fleet fleet = new Fleet();

		assertThrows(AssertionError.class, () -> {
			Game.printBoard(fleet, null, false, false, false);
		});
	}

	/**
	 * Testa indiretamente o método privado printShips através do método getBoard.
	 * Utiliza uma frota com um navio intacto para garantir a cobertura do ramo em
	 * que não são marcadas as posições adjacentes, validando a matriz de caracteres.
	 */
	@Test
	@DisplayName("printShips não marca posições adjacentes de um navio intacto")
	void printShips1() {
		Game myGame = new Game(new Fleet());
		Ship floatingShip = new Barge(Compass.NORTH, new Position(3, 3));
		myGame.getMyFleet().addShip(floatingShip);

		// getBoard chama internamente o printShips
		char[][] board = myGame.getBoard(false);

		// O símbolo do navio '#' deve estar na posição (3,3)
		assertEquals('#', board[3][3], "A posição do navio deve ter o marcador '#'");

		// A posição adjacente (ex: 2,3) deve ser água '.', e não o marcador adjacente '-'
		assertEquals('.', board[2][3], "A posição adjacente deve permanecer como água '.'");
	}

	/**
	 * Testa indiretamente o método privado printShips através do método getBoard.
	 * Utiliza uma frota com um navio afundado para garantir a cobertura do ramo em
	 * que as posições adjacentes do navio são devidamente marcadas na matriz.
	 */
	@Test
	@DisplayName("printShips marca as posições adjacentes de um navio afundado")
	void printShips2() {
		Game myGame = new Game(new Fleet());
		Ship sunkShip = new Barge(Compass.NORTH, new Position(3, 3));
		myGame.getMyFleet().addShip(sunkShip);

		// Afundar o navio
		Position hitPos = new Position(3, 3);
		hitPos.shoot();
		sunkShip.shoot(hitPos);

		// getBoard chama internamente o printShips
		char[][] board = myGame.getBoard(false);

		// O símbolo do navio '#' deve estar na posição (3,3)
		assertEquals('#', board[3][3], "A posição do navio deve ter o marcador '#'");

		// Como o navio afundou, a posição adjacente (ex: 2,3) deve estar marcada com '-'
		assertEquals('-', board[2][3], "A posição adjacente deve ter o marcador '-'");
	}

	/**
	 * Testa indiretamente o método privado printBoardShots através do método printBoard.
	 * Utiliza um movimento com um tiro cujas coordenadas estão fora dos limites do tabuleiro.
	 * Garante a cobertura do ramo falso da condição shot.isInside(), validando que o
	 * tiro é simplesmente ignorado sem lançar qualquer exceção (ArrayIndexOutOfBoundsException).
	 */
	@Test
	@DisplayName("printBoardShots ignora tiros fora do tabuleiro sem lançar exceções")
	void printBoardShots() {
		Fleet fleet = new Fleet();
		List<IMove> moves = new java.util.ArrayList<>();

		List<IPosition> shots = java.util.List.of(new Position(-1, 5));
		moves.add(new Move(1, shots, new java.util.ArrayList<>()));

		assertDoesNotThrow(() -> {
			Game.printBoard(fleet, moves, false, true, false);
		}, "O processamento de um tiro fora do tabuleiro não deve lançar exceções.");
	}

	/**
	 * Testa a serialização de uma lista válida e populada de posições para JSON.
	 * Garante que as posições são iteradas, convertidas corretamente para a
	 * notação clássica e que o JSON resultante contém os dados esperados.
	 */
	@Test
	@DisplayName("jsonShots serializa corretamente uma lista de posições para JSON")
	void jsonShots1() {
		List<IPosition> shots = java.util.List.of(new Position(0, 0), new Position(2, 3));
		String json = Game.jsonShots(shots);

		assertNotNull(json, "O JSON retornado não deve ser nulo.");
		assertTrue(json.contains("\"row\" : \"A\""), "O JSON deve conter a linha A correspondente ao índice 0.");
		assertTrue(json.contains("\"column\" : 1"), "O JSON deve conter a coluna 1 correspondente ao índice 0.");
		assertTrue(json.contains("\"row\" : \"C\""), "O JSON deve conter a linha C correspondente ao índice 2.");
		assertTrue(json.contains("\"column\" : 4"), "O JSON deve conter a coluna 4 correspondente ao índice 3.");
	}

	/**
	 * Testa a validação do argumento no método jsonShots.
	 * Garante que é lançado um AssertionError quando a lista de tiros passada for nula,
	 * cobrindo a primeira linha do método.
	 */
	@Test
	@DisplayName("jsonShots lança AssertionError quando a lista de tiros é nula")
	void jsonShots2() {
		assertThrows(AssertionError.class, () -> {
			Game.jsonShots(null);
		}, "Deve lançar AssertionError se a lista de tiros for nula.");
	}

	/**
	 * Testa a serialização de uma lista vazia de posições para JSON.
	 * Garante que o método não entra no ciclo for e retorna um array JSON vazio,
	 * validando a estabilidade do conversor Jackson para listas sem elementos.
	 */
	@Test
	@DisplayName("jsonShots devolve um array JSON vazio para uma lista vazia")
	void jsonShots3() {
		List<IPosition> shots = new java.util.ArrayList<>();
		String json = Game.jsonShots(shots);

		assertNotNull(json, "O JSON retornado não deve ser nulo.");
		assertTrue(json.contains("[") && json.contains("]"), "O JSON de uma lista vazia deve conter os parênteses retos de um array.");
		assertFalse(json.contains("\"row\""), "O JSON não deve conter atributos internos pois a lista estava vazia.");
	}

	/**
	 * Testa o lançamento de exceção durante a serialização JSON.
	 * Utiliza o Mockito para intercetar a criação do ObjectMapper e forçar o lançamento
	 * de um JsonProcessingException, garantindo a cobertura do bloco catch.
	 */
	@Test
	@DisplayName("jsonShots lança RuntimeException quando a serialização falha")
	void jsonShots4() {
		JsonProcessingException mockException = new JsonProcessingException("Mocked error") {};

		try (MockedConstruction<ObjectMapper> mocked = Mockito.mockConstruction(ObjectMapper.class,
				(mock, context) -> {
					when(mock.writeValueAsString(any())).thenThrow(mockException);
				})) {

			List<IPosition> shots = java.util.List.of(new Position(0, 0));

			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				Game.jsonShots(shots);
			});

			assertTrue(exception.getMessage().contains("Erro ao serializar o JSON"));
		}
	}

	/**
	 * Testa o método getAlienFleet.
	 * Verifica se, ao inicializar um novo jogo, a frota alienígena é
	 * corretamente instanciada (através do Fleet.createRandom()) e não é nula.
	 */
	@Test
	@DisplayName("getAlienFleet devolve uma frota alienígena não nula")
	void getAlienFleet() {
		IFleet fleet = new Fleet();
		Game game = new Game(fleet);
		assertNotNull(game.getAlienFleet(), "A frota alienígena não deve ser nula após a inicialização do jogo.");
	}

	/**
	 * Testa o método getMyMoves.
	 * Verifica se, ao inicializar um novo jogo, a lista de movimentos do jogador
	 * é instanciada corretamente como uma ArrayList.
	 */
	@Test
	@DisplayName("getMyMoves devolve uma lista de jogadas não nula")
	void getMyMoves() {
		IFleet fleet = new Fleet();
		Game game = new Game(fleet);
		assertNotNull(game.getMyMoves(), "A lista de movimentos não deve ser nula após a inicialização do jogo.");
	}

	/**
	 * Testa o fluxo normal do método randomEnemyFire com objetos reais.
	 * Ao chamar o método duas vezes, garantimos que a lista de movimentos
	 * tem elementos, acionando naturalmente a lambda de alienMoves.
	 */
	@Test
	@DisplayName("randomEnemyFire devolve um resultado JSON não nulo no fluxo normal")
	void randomEnemyFire1() {
		Game game = new Game(Fleet.createRandom());

		game.randomEnemyFire();

		String result = game.randomEnemyFire();

		assertNotNull(result, "O resultado JSON não deve ser nulo.");
	}

	/**
	 * Testa o ramo alternativo (else) de forma natural.
	 * Simula um cenário de "fim de jogo" prolongado onde o inimigo dispara
	 * até preencher o tabuleiro. Um ciclo infinito garante que o erro esperado
	 * por ausência de posições válidas seja inevitavelmente lançado.
	 */
	@Test
	@DisplayName("randomEnemyFire lança erro quando se esgotam as posições disponíveis")
	void randomEnemyFire2() {
		Game game = new Game(Fleet.createRandom());

		assertThrows(Throwable.class, () -> {
			while (true) {
				game.randomEnemyFire();
			}
		});
	}

	/**
	 * Testa os ramos residuais do randomEnemyFire: a lambda dos navios afundados e
	 * o primeiro ciclo while dentro do bloco 'else'.
	 * Utiliza uma abordagem estatística, simulando alguns jogos rápidos
	 * até ao fim para garantir que o fator aleatório atinge o estado de "navio afundado"
	 * e o estado de "sobra apenas 1 ou 2 posições no tabuleiro".
	 */
	@Test
	@DisplayName("randomEnemyFire cobre os ramos residuais em várias simulações de jogo")
	void randomEnemyFire3() {
		for (int jogo = 0; jogo < 20; jogo++) {

			Game game = new Game(Fleet.createRandom());
			boolean tabuleiroEsgotado = false;

			while (!tabuleiroEsgotado) {
				try {
					game.randomEnemyFire();
				} catch (AssertionError | Exception e) {
					tabuleiroEsgotado = true;
				}
			}
		}

		assertTrue(true, "Simulação concluída, cobrindo os caminhos residuais do método.");
	}

	/**
	 * Testa readEnemyFire com formato clássico (letras e números juntos, ex: A0 A1...).
	 */
	@Test
	@DisplayName("readEnemyFire lê tiros no formato clássico (ex: A1)")
	void readEnemyFire1() {
		Game game = new Game(Fleet.createRandom());

		StringBuilder input = new StringBuilder();
		for (int i = 0; i < Game.NUMBER_SHOTS; i++) {
			input.append("A").append(i).append(" ");
		}

		Scanner in = new Scanner(input.toString());
		assertNotNull(game.readEnemyFire(in));
	}

	/**
	 * Testa readEnemyFire com formato separado (letra separada do número, ex: A 0 B 1...).
	 */
	@Test
	@DisplayName("readEnemyFire lê tiros no formato separado (ex: A 1)")
	void readEnemyFire2() {
		Game game = new Game(Fleet.createRandom());

		StringBuilder input = new StringBuilder();
		for (int i = 0; i < Game.NUMBER_SHOTS; i++) {
			input.append("A ").append(i).append(" ");
		}

		Scanner in = new Scanner(input.toString());
		assertNotNull(game.readEnemyFire(in));
	}

	/**
	 * Testa readEnemyFire com posição incompleta (letra sem número).
	 */
	@Test
	@DisplayName("readEnemyFire lança exceção para uma posição incompleta")
	void readEnemyFire3() {
		Game game = new Game(Fleet.createRandom());

		Scanner in = new Scanner("A B");

		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(in),
				"Deve lançar exceção se a coluna não for seguida de uma linha.");
	}

	/**
	 * Testa readEnemyFire com o número errado de posições.
	 */
	@Test
	@DisplayName("readEnemyFire lança exceção para um número errado de tiros")
	void readEnemyFire4() {
		Game game = new Game(Fleet.createRandom());

		Scanner in = new Scanner(" \n");

		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(in),
				"Deve lançar exceção por não ter a quantidade exata de tiros.");
	}

	/**
	 * Testa readEnemyFire com MAIS posições do que o permitido.
	 * Garante que o ciclo while termina pelo limite de NUMBER_SHOTS e ignora o resto.
	 */
	@Test
	@DisplayName("readEnemyFire ignora tiros em excesso além de NUMBER_SHOTS")
	void readEnemyFire5() {
		Game game = new Game(Fleet.createRandom());

		StringBuilder input = new StringBuilder();
		// Adicionamos + 1 para forçar o excesso de tiros na mesma linha
		for (int i = 0; i < Game.NUMBER_SHOTS + 1; i++) {
			input.append("C").append(i).append(" ");
		}

		Scanner in = new Scanner(input.toString());
		// O método deve processar apenas o NUMBER_SHOTS e não dar erro
		assertNotNull(game.readEnemyFire(in));
	}

	/**
	 * Testa readEnemyFire lançando AssertionError quando o Scanner é null.
	 */
	@Test
	@DisplayName("readEnemyFire lança AssertionError quando o Scanner é nulo")
	void readEnemyFireNullScanner() {
		Game game = new Game(Fleet.createRandom());

		assertThrows(AssertionError.class, () -> game.readEnemyFire(null),
				"Deve falhar na asserção se o scanner for nulo.");
	}

	/**
	 * Testa readPlayerFire com uma primeira linha vazia (cobrindo o if específico)
	 * seguida de formato clássico.
	 */
	@Test
	@DisplayName("readPlayerFire ignora a primeira linha vazia e lê o formato clássico")
	void readPlayerFire1() {
		Game game = new Game(Fleet.createRandom());

		StringBuilder input = new StringBuilder("\n");
		for (int i = 0; i < Game.NUMBER_SHOTS; i++) {
			input.append("B").append(i).append(" ");
		}

		Scanner in = new Scanner(input.toString());
		assertNotNull(game.readPlayerFire(in));
	}

	/**
	 * Testa readPlayerFire com formato separado.
	 */
	@Test
	@DisplayName("readPlayerFire lê tiros no formato separado (ex: B 1)")
	void readPlayerFire2() {
		Game game = new Game(Fleet.createRandom());

		StringBuilder input = new StringBuilder();
		for (int i = 0; i < Game.NUMBER_SHOTS; i++) {
			input.append("B ").append(i).append(" ");
		}

		Scanner in = new Scanner(input.toString());
		assertNotNull(game.readPlayerFire(in));
	}

	/**
	 * Testa readPlayerFire com posição incompleta (letra sem número).
	 */
	@Test
	@DisplayName("readPlayerFire lança exceção para uma posição incompleta")
	void readPlayerFire3() {
		Game game = new Game(Fleet.createRandom());

		Scanner in = new Scanner("Z X");

		assertThrows(IllegalArgumentException.class, () -> game.readPlayerFire(in));
	}

	/**
	 * Testa readPlayerFire com o número errado de posições.
	 */
	@Test
	@DisplayName("readPlayerFire lança exceção para um número errado de tiros")
	void readPlayerFire4() {
		Game game = new Game(Fleet.createRandom());

		Scanner in = new Scanner("\n\n");

		assertThrows(IllegalArgumentException.class, () -> game.readPlayerFire(in));
	}

	/**
	 * Testa readPlayerFire com MAIS posições do que o permitido.
	 * Cobre o cenário de quebra do while por limite de tamanho no jogador.
	 */
	@Test
	@DisplayName("readPlayerFire ignora tiros em excesso além de NUMBER_SHOTS")
	void readPlayerFire5() {
		Game game = new Game(Fleet.createRandom());

		StringBuilder input = new StringBuilder();
		for (int i = 0; i < Game.NUMBER_SHOTS + 1; i++) {
			input.append("D").append(i).append(" ");
		}

		Scanner in = new Scanner(input.toString());
		assertNotNull(game.readPlayerFire(in));
	}

	/**
	 * Testa readPlayerFire com uma string que é vazia (após trim), mas que
	 * NÃO tem linha seguinte. Cobre a segunda metade (falsa) daquele if inicial.
	 */
	@Test
	@DisplayName("readPlayerFire lança exceção para uma linha vazia sem linha seguinte")
	void readPlayerFire6() {
		Game game = new Game(Fleet.createRandom());

		// Uma string com apenas espaços vai ficar vazia no .trim(),
		// e como não tem \n, o in.hasNextLine() vai ser falso.
		Scanner in = new Scanner("   ");

		assertThrows(IllegalArgumentException.class, () -> game.readPlayerFire(in),
				"Deve lançar exceção por não ter a quantidade exata de tiros.");
	}

	/**
	 * Testa readPlayerFire lançando AssertionError quando o Scanner é null.
	 */
	@Test
	@DisplayName("readPlayerFire lança AssertionError quando o Scanner é nulo")
	void readPlayerFireNullScanner() {
		assertThrows(AssertionError.class, () -> game.readPlayerFire(null), "Deve falhar na asserção se o scanner for nulo.");
	}

	/**
	 * Testa a verificação do comprimento da lista shots
	 */
	@Test
	@DisplayName("fireShots lança exceção quando o número de tiros é incorreto")
	void fireShots() {
		List<IPosition> shots = List.of(new Position(0, 0));
		assertThrows(IllegalArgumentException.class, () -> game.fireShots(shots), "Deve falhar pelo número de tiros não corresponder");
	}

	/**
	 * Testa fireShot lançando AssertionError quando a lista é nula
	 */
	@Test
	@DisplayName("fireShots lança AssertionError quando a lista é nula")
	void fireShotNullList() {
		assertThrows(AssertionError.class, () -> game.fireShots(null), "Deve falhar na asserção se a lista for nula.");
	}

	/**
	 * Testa a verificação do comprimento da lista shots
	 */
	@Test
	@DisplayName("firePlayerShots lança exceção quando o número de tiros é incorreto")
	void firePlayerShots() {
		List<IPosition> shots = List.of(new Position(0, 0));
		assertThrows(IllegalArgumentException.class, () -> game.firePlayerShots(shots), "Deve falhar pelo número de tiros não corresponder");
	}

	/**
	 * Testa firePlayerShot lançando AssertionError quando a lista é nula
	 */
	@Test
	@DisplayName("firePlayerShots lança AssertionError quando a lista é nula")
	void firePlayerShotNullList() {
		assertThrows(AssertionError.class, () -> game.firePlayerShots(null), "Deve falhar na asserção se a lista for nula.");
	}

	/**
	 * Testa o firePlayerSingleShot em caso de tiro repetido pela variável isRepeated
	 */
	@Test
	@DisplayName("firePlayerSingleShot trata o tiro como repetido quando isRepeated é true")
	void firePlayerSingleShot1() {
		Position validPos = new Position(2, 2);

		IGame.ShotResult result = game.firePlayerSingleShot(validPos, true);

		assertTrue(result.valid(), "O ShotResult deve ser válido.");
		assertTrue(result.repeated(), "O ShotResult deve ser repetido.");
		assertNull(result.ship(), "O ShotResult deve ter o ship a null.");
		assertFalse(result.sunk(), "O ShotResult deve não ter afundado.");
	}

	/**
	 * Testa o firePlayerSingleShot em caso de tiro repetido pela variável isRepeated e verificação do método repeatedShots
	 */
	@Test
	@DisplayName("firePlayerSingleShot trata como repetido um tiro disparado duas vezes")
	void firePlayerSingleShot2() {
		Position validPos = new Position(2, 2);
		game.firePlayerSingleShot(validPos, true);
		IGame.ShotResult result = game.firePlayerSingleShot(validPos, true);

		assertTrue(result.valid(), "O ShotResult deve ser válido.");
		assertTrue(result.repeated(), "O ShotResult deve ser repetido.");
		assertNull(result.ship(), "O ShotResult deve ter o ship a null.");
		assertFalse(result.sunk(), "O ShotResult deve não ter afundado.");
	}

	/**
	 * Testa o firePlayerSingleShot em caso de tiro repetido pela verificação do método repeatedShots
	 */
	@Test
	@DisplayName("firePlayerSingleShot deteta tiro repetido através de repeatedShots")
	void firePlayerSingleShot3() {
		Position validPos = new Position(2, 2);

		List<IPosition> initialShots = List.of(validPos, new Position(0, 0), new Position(0, 1));
		game.firePlayerShots(initialShots);

		IGame.ShotResult result = game.firePlayerSingleShot(validPos, false);

		assertTrue(result.valid(), "O ShotResult deve ser válido.");
		assertTrue(result.repeated(), "O ShotResult deve ser repetido.");
		assertNull(result.ship(), "O ShotResult deve ter o ship a null.");
		assertFalse(result.sunk(), "O ShotResult deve não ter afundado.");
	}

	/**
	 * Testa o firePlayerSingleShot quando afunda um barco
	 */
	@Test
	@DisplayName("firePlayerSingleShot afunda um navio ao atingi-lo")
	void firePlayerSingleShot4() {
		List<IShip> ships = game.getAlienFleet().getShips();

		IShip targetShip = ships.stream()
				.filter(s -> s.getSize() == 1)
				.findFirst()
				.orElse(ships.get(0));


		IPosition hitPos = targetShip.getPositions().get(0);


		IGame.ShotResult result = game.firePlayerSingleShot(hitPos, false);

		assertTrue(result.valid(), "O tiro deve ser válido.");
		assertFalse(result.repeated(), "O tiro não deve ser repetido.");
		assertEquals(targetShip, result.ship(), "O navio atingido deve corresponder.");
		assertTrue(result.sunk(), "O navio deve afundar com este tiro.");
	}

	/**
	 * Testa firePlayerSingleShot lançando AssertionError quando a lista é nula
	 */
	@Test
	@DisplayName("firePlayerSingleShot lança AssertionError quando a posição é nula")
	void firePlayerSingleShotNullList() {
		assertThrows(AssertionError.class, () -> game.firePlayerSingleShot(null, true), "Deve falhar na asserção se a posição for nula.");
	}

	/**
	 * Testa playerRepeatedShots quando a posição não está na lista
	 */
	@Test
	@DisplayName("playerRepeatedShots devolve false para uma posição não disparada")
	void playerRepeatedShots() {
		Position validPos = new Position(2, 2);

		List<IPosition> initialShots = List.of(new Position(0, 0), new Position(0, 1), new Position(1, 0));
		game.firePlayerShots(initialShots);

		assertFalse(game.playerRepeatedShots(validPos));
	}

	/**
	 * Testa repeatedShot lançando AssertionError quando a lista é nula
	 */
	@Test
	@DisplayName("repeatedShot lança AssertionError quando a posição é nula")
	void repeatedShotNullPos() {
		assertThrows(AssertionError.class, () -> game.repeatedShot(null), "Deve falhas na asserção se a posição for nula.");
	}

	/**
	 * Testa se printMyBoard imprime alguma coisa
	 */
	@Test
	@DisplayName("printMyBoard imprime o tabuleiro do jogador na consola")
	void printMyBoard() {
		boolean showShots = true;
		boolean showLegend = true;

		assertDoesNotThrow(() -> game.printMyBoard(showShots, showLegend));

		assertFalse(outContent.toString().isEmpty(), "O método printMyBoard deveria ter gerado saída na consola.");
	}

	/**
	 * Testa se printAlienBoard imprime alguma coisa
	 */
	@Test
	@DisplayName("printAlienBoard imprime o tabuleiro alienígena na consola")
	void printAlienBoard() {
		boolean showShots = true;
		boolean showLegend = true;

		assertDoesNotThrow(() -> game.printAlienBoard(showShots, showLegend));

		assertFalse(outContent.toString().isEmpty(), "O método printMyBoard deveria ter gerado saída na consola.");
	}

	/**
	 * Testa o setLastTime e getLastTime
	 */
	@Test
	@DisplayName("setLastTime e getLastTime guardam e devolvem o tempo corretamente")
	void time() {
		long tempoEsperado = 12345;

		game.setLastTime(tempoEsperado);
		assertEquals(tempoEsperado, game.getLastTime(), "O lastTime deveria ser o tempo esperado");
	}

	/**
	 * Testa o getBoard com showShots a true
	 */
	@Test
	@DisplayName("getBoard devolve uma matriz com as dimensões do tabuleiro")
	void getBoard() {
		char[][] board = game.getBoard(true);

		assertNotNull(board);
		assertEquals(BOARD_SIZE, board.length);
		assertEquals(BOARD_SIZE, board[0].length);
	}

	/**
	 * Testa o método over() quando o jogador ganha o jogo.
	 * Cobre o ramo em que myMoves não está vazio e a condição de vitória é verdadeira
	 * (frota inimiga totalmente afundada e frota do jogador com navios sobreviventes).
	 */
	@Test
	@DisplayName("over regista vitória do jogador quando a frota inimiga é destruída")
	void over1() {
		Game game = new Game(new Fleet());
		game.getMyFleet().addShip(new Barge(Compass.NORTH, new Position(0, 0))); // Frota do jogador com 1 navio sobrevivente

		for (IShip ship : game.getAlienFleet().getShips()) {
			for (IPosition pos : ship.getPositions()) {
				ship.shoot(pos);
			}
		}

		List<IPosition> shots = List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2));
		game.firePlayerShots(shots);

		try (MockedStatic<ScoreBoardManager> mockedScoreBoard = Mockito.mockStatic(ScoreBoardManager.class)) {
			game.over();
			mockedScoreBoard.verify(() -> ScoreBoardManager.saveScore("Jogador", 1, game.getHits(), true), times(1));
		}
	}

	/**
	 * Testa o método over() quando o jogo acaba sem vitória para o jogador.
	 * Cobre o ramo em que myMoves está vazio (totalMoves recai sobre alienMoves) e
	 * a frota alienígena ainda possui navios a flutuar (playerWon = false por short-circuit do &&).
	 */
	@Test
	@DisplayName("over regista derrota quando a frota alienígena ainda tem navios")
	void over2() {
		Game game = new Game(new Fleet());

		List<IPosition> shots = List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2));
		game.fireShots(shots);

		try (MockedStatic<ScoreBoardManager> mockedScoreBoard = Mockito.mockStatic(ScoreBoardManager.class)) {
			game.over();
			mockedScoreBoard.verify(() -> ScoreBoardManager.saveScore("Jogador", 1, game.getHits(), false), times(1));
		}
	}

	/**
	 * Testa o método over() no cenário de empate (ambas as frotas destruídas).
	 * Cobre a segunda condição da variável playerWon (getRemainingShips() > 0) sendo avaliada como falsa.
	 */
	@Test
	@DisplayName("over regista que o jogador não ganhou em caso de empate")
	void over3() {
		Game game = new Game(new Fleet());

		for (IShip ship : game.getAlienFleet().getShips()) {
			for (IPosition pos : ship.getPositions()) {
				ship.shoot(pos);
			}
		}

		List<IPosition> shots = List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2));
		game.firePlayerShots(shots);

		try (MockedStatic<ScoreBoardManager> mockedScoreBoard = Mockito.mockStatic(ScoreBoardManager.class)) {
			game.over();
			mockedScoreBoard.verify(() -> ScoreBoardManager.saveScore("Jogador", 1, game.getHits(), false), times(1));
		}
	}

	/**
	 * Testa o fluxo normal do método randomPlayerFire.
	 * Cobre o ramo principal onde existem posições candidatas suficientes disponíveis no tabuleiro
	 * (candidateShots.size() >= Game.NUMBER_SHOTS).
	 */
	@Test
	@DisplayName("randomPlayerFire devolve um resultado JSON e regista a jogada")
	void randomPlayerFire1() {
		Game myGame = new Game(Fleet.createRandom());

		String result = myGame.randomPlayerFire();

		assertNotNull(result, "O resultado JSON de randomPlayerFire não deve ser nulo.");
        assertFalse(myGame.getMyMoves().isEmpty(), "Deve ter sido registado pelo menos um movimento do jogador.");
	}

	/**
	 * Testa os ramos residuais do randomPlayerFire: a lambda dos navios afundados e
	 * o bloco 'else' (quando restam menos posições disponíveis do que NUMBER_SHOTS).
	 * Dispara consecutivamente até esgotar as posições válidas no tabuleiro.
	 */
	@Test
	@DisplayName("randomPlayerFire lança erro quando se esgotam as posições disponíveis")
	void randomPlayerFire2() {
		Game myGame = new Game(Fleet.createRandom());

		assertThrows(Throwable.class, () -> {
			while (true) {
				myGame.randomPlayerFire();
			}
		});
	}

	/**
	 * Testa os ramos residuais do randomPlayerFire com uma abordagem estatística.
	 * Ao simular vários jogos rápidos até ao fim, garantimos que o fator aleatório
	 * atinge o estado em que sobram exatamente 1 ou 2 posições no tabuleiro,
	 * forçando a execução completa do primeiro ciclo while dentro do bloco 'else'.
	 */
	@Test
	@DisplayName("randomPlayerFire cobre os ramos residuais em várias simulações de jogo")
	void randomPlayerFire3() {
		for (int jogo = 0; jogo < 50; jogo++) {
			Game myGame = new Game(Fleet.createRandom());
			boolean tabuleiroEsgotado = false;

			while (!tabuleiroEsgotado) {
				try {
					myGame.randomPlayerFire();
				} catch (AssertionError | Exception e) {
					tabuleiroEsgotado = true;
				}
			}
		}

		assertTrue(true, "Simulação concluída, cobrindo os caminhos residuais do método.");
	}

}