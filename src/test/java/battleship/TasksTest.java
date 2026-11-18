package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Tasks.
 * Author: alexa
 * Date: 2026-11-17 22:16
 * Cyclomatic Complexity:
 * - constructor: 1
 * - menu(): not covered; interactive game flow
 * - menuHelp(): 1
 * - buildFleet(): 4
 * - readShip(): 1
 * - readPosition(): 1
 * - readClassicPosition(): 7 (the true outcome of its second format check is unreachable)
 * - decideSoundToPlay(): 3
 * - parseAiShots(): 3
 * - checkPlayerWon(): 1
 * - executeTurns(): 5 (timer not started / started; winning / non-winning branch not both reachable with an injected AI)
 * - endGame(): not covered because it calls System.exit(0)
 */
class TasksTest {

    @TempDir
    Path tempDir;

    private Tasks tasks;

    @BeforeEach
    void setUp() {
        tasks = new Tasks();
    }

    @AfterEach
    void tearDown() {
        tasks = null;
    }

    @Test
    @DisplayName("O construtor cria uma instância de Tasks")
    void constructor() {
        assertNotNull(tasks,
                "Error: expected setup to create a Tasks instance.");
    }

    @Test
    @DisplayName("menuHelp apresenta os comandos disponíveis")
    void menuHelp() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            Tasks.menuHelp();
        } finally {
            System.setOut(originalOutput);
        }

        String help = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(help.contains("Digite um dos comandos"),
                        "Error: expected the help text to include its instructions."),
                () -> assertTrue(help.contains("desisto"),
                        "Error: expected the help text to include the exit command.")
        );
    }

    @Test
    @DisplayName("buildFleet cria uma frota com onze navios válidos")
    void buildFleet1() {
        Fleet fleet = Tasks.buildFleet(new Scanner(fleetInput("")));

        assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
                "Error: expected buildFleet to add eleven valid ships.");
    }

    @Test
    @DisplayName("buildFleet ignora um tipo de navio desconhecido")
    void buildFleet2() {
        Fleet fleet = Tasks.buildFleet(new Scanner(fleetInput("desconhecido 0 0 n ")));

        assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
                "Error: expected an unknown ship type not to prevent building the full fleet.");
    }

    @Test
    @DisplayName("buildFleet ignora um navio que colide com outro")
    void buildFleet3() {
        Fleet fleet = Tasks.buildFleet(new Scanner(fleetInput("barca 0 0 n ")));

        assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
                "Error: expected a colliding ship to be rejected while later valid ships are added.");
    }

    @Test
    @DisplayName("buildFleet ignora um navio fora do tabuleiro")
    void buildFleet4() {
        Fleet fleet = Tasks.buildFleet(new Scanner(fleetInput("barca 20 20 n ")));

        assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
                "Error: expected an out-of-board ship to be rejected while the valid fleet is built.");
    }

    @Test
    @DisplayName("readShip cria um navio a partir da entrada")
    void readShip() {
        Ship ship = Tasks.readShip(new Scanner("barca 0 0 n"));

        assertAll(
                () -> assertNotNull(ship,
                        "Error: expected readShip to create a ship for a recognized type."),
                () -> assertEquals("Barca", ship.getCategory(),
                        "Error: expected readShip to preserve the barge category."),
                () -> assertEquals(Compass.NORTH, ship.getBearing(),
                        "Error: expected readShip to parse the north bearing.")
        );
    }

    @Test
    @DisplayName("readPosition lê coordenadas numéricas")
    void readPosition() {
        Position position = Tasks.readPosition(new Scanner("2 5"));

        assertAll(
                () -> assertEquals(2, position.getRow(),
                        "Error: expected readPosition to parse row 2."),
                () -> assertEquals(5, position.getColumn(),
                        "Error: expected readPosition to parse column 5.")
        );
    }

    @Test
    @DisplayName("readClassicPosition rejeita entrada sem tokens")
    void readClassicPosition1() {
        assertThrows(IllegalArgumentException.class,
                () -> Tasks.readClassicPosition(new Scanner("")),
                "Error: expected an empty input to throw IllegalArgumentException.");
    }

    @Test
    @DisplayName("readClassicPosition lê coordenadas compactas")
    void readClassicPosition2() {
        Position position = (Position) Tasks.readClassicPosition(new Scanner("A3"));

        assertAll(
                () -> assertEquals(0, position.getRow(),
                        "Error: expected compact coordinate A3 to map to row 0."),
                () -> assertEquals(2, position.getColumn(),
                        "Error: expected compact coordinate A3 to map to column 2.")
        );
    }

    @Test
    @DisplayName("readClassicPosition aceita letras minúsculas")
    void readClassicPosition3() {
        Position position = (Position) Tasks.readClassicPosition(new Scanner("b4"));

        assertAll(
                () -> assertEquals(1, position.getRow(),
                        "Error: expected lowercase coordinate b4 to map to row 1."),
                () -> assertEquals(3, position.getColumn(),
                        "Error: expected lowercase coordinate b4 to map to column 3.")
        );
    }

    @Test
    @DisplayName("readClassicPosition lê coordenadas separadas por espaço")
    void readClassicPosition4() {
        Position position = (Position) Tasks.readClassicPosition(new Scanner("C 5"));

        assertAll(
                () -> assertEquals(2, position.getRow(),
                        "Error: expected spaced coordinate C 5 to map to row 2."),
                () -> assertEquals(4, position.getColumn(),
                        "Error: expected spaced coordinate C 5 to map to column 4.")
        );
    }

    @Test
    @DisplayName("readClassicPosition rejeita um formato não reconhecido")
    void readClassicPosition5() {
        assertThrows(IllegalArgumentException.class,
                () -> Tasks.readClassicPosition(new Scanner("invalid")),
                "Error: expected an invalid coordinate to throw IllegalArgumentException.");
    }

    @Test
    @DisplayName("readClassicPosition rejeita tokens separados com primeira parte inválida")
    void readClassicPosition6() {
        assertThrows(IllegalArgumentException.class,
                () -> Tasks.readClassicPosition(new Scanner("AB 3")),
                "Error: expected a multi-letter row to be rejected.");
    }

    @Test
    @DisplayName("readClassicPosition propaga overflow da linha")
    void readClassicPosition7() {
        assertThrows(NumberFormatException.class,
                () -> Tasks.readClassicPosition(new Scanner("A99999999999999999999")),
                "Error: expected an overflowing row number to throw NumberFormatException.");
    }

    @Test
    @DisplayName("decideSoundToPlay escolhe alarme quando um navio é afundado")
    void decideSoundToPlay1() {
        assertEquals("alarm", Tasks.decideSoundToPlay(3, 2, 1, 1),
                "Error: expected the alarm when the remaining ship count decreases.");
    }

    @Test
    @DisplayName("decideSoundToPlay escolhe explosão quando há novos acertos")
    void decideSoundToPlay2() {
        assertEquals("explosion", Tasks.decideSoundToPlay(3, 3, 1, 2),
                "Error: expected the explosion when hits increase and ships do not decrease.");
    }

    @Test
    @DisplayName("decideSoundToPlay escolhe splash quando não há alteração")
    void decideSoundToPlay3() {
        assertEquals("splash", Tasks.decideSoundToPlay(3, 3, 1, 1),
                "Error: expected splash when neither ships nor hits change.");
    }

    @Test
    @DisplayName("parseAiShots converte uma lista de coordenadas")
    void parseAiShots1() {
        List<IPosition> shots = Tasks.parseAiShots(
                "[{\"row\":\"a\",\"column\":1},{\"row\":\"C\",\"column\":4}]");

        assertAll(
                () -> assertEquals(2, shots.size(),
                        "Error: expected both coordinates in the JSON to be parsed."),
                () -> assertEquals(new Position(0, 0), shots.get(0),
                        "Error: expected coordinate A1 to map to row 0 and column 0."),
                () -> assertEquals(new Position(2, 3), shots.get(1),
                        "Error: expected coordinate C4 to map to row 2 and column 3.")
        );
    }

    @Test
    @DisplayName("parseAiShots devolve uma lista vazia para um array vazio")
    void parseAiShots2() {
        assertTrue(Tasks.parseAiShots("[]").isEmpty(),
                "Error: expected an empty JSON array to produce an empty shot list.");
    }

    @Test
    @DisplayName("parseAiShots lança uma exceção para JSON inválido")
    void parseAiShots3() {
        assertThrows(RuntimeException.class,
                () -> Tasks.parseAiShots("{ invalid json"),
                "Error: expected malformed AI JSON to throw RuntimeException.");
    }

    @Test
    @DisplayName("menu termina corretamente quando o utilizador desiste sem iniciar um jogo")
    void menu() {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream("desisto\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            assertDoesNotThrow(Tasks::menu,
                    "Error: expected menu to exit without error when the user immediately resigns.");
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Bons ventos!"),
                "Error: expected menu to print its goodbye message after resigning.");
    }

    @Test
    @DisplayName("menu percorre comandos que não requerem jogo iniciado")
    void menu1() {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commands = "estado mapa rajada simula tiros ajuda historico pdf scoreboard tui tempo comando-invalido desisto\n";

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            try {
                Tasks.menu();
            } catch (Exception e) {
                String msg = e.getMessage();
                assertTrue(msg != null && (msg.contains(".env") || msg.contains("GOOGLE_API") || msg.contains("API key") || msg.contains("openAiApiKey")),
                        "Error: expected the menu to throw an exception mentioning .env when the AI is not configured.");
            }
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        String menuOutput = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(menuOutput.contains("Ainda não iniciou nenhum jogo."),
                        "Error: expected commands requiring a game to report that no game has started."),
                () -> assertTrue(menuOutput.contains("Ainda nao iniciou nenhum jogo!"),
                        "Error: expected PDF export without a game to report that no game has started."),
                () -> assertTrue(menuOutput.contains("Ainda não foi iniciado nenhum jogo."),
                        "Error: expected the time command without a game to report that no game has started."),
                () -> assertTrue(menuOutput.contains("Que comando é esse???"),
                        "Error: expected an unknown command to take the switch default branch."),
                () -> assertTrue(menuOutput.contains("Bons ventos!"),
                        "Error: expected menu to reach the exit branch after processing commands.")
        );
    }

    @Test
    @DisplayName("menuJogoCompleto processa comandos com uma partida ativa (sem IA injetada)")
    void menuJogoCompleto() throws Exception {
        Path historyFile = Path.of("historico_jogos.json");
        Path scoreboardFile = Path.of("scoreboard.csv");
        Path historyBackup = tempDir.resolve("historico-jogos-backup.json");
        Path scoreboardBackup = tempDir.resolve("scoreboard-backup.csv");
        boolean historyExisted = Files.exists(historyFile);
        boolean scoreboardExisted = Files.exists(scoreboardFile);
        if (historyExisted) {
            Files.copy(historyFile, historyBackup);
        }
        if (scoreboardExisted) {
            Files.copy(scoreboardFile, scoreboardBackup);
        }

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Passa apenas os comandos para construir a frota, testar comandos básicos ativos, e sair.
        // Sem o comando "rajada" não precisamos de lidar com a resposta imprevisível da IA.
        String input = "lefrota\n"
                + fleetInput("")
                + "historico\n"
                + "scoreboard\n"
                + "desisto\n";

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            try {
                Tasks.menu();
            } catch (Exception e) {
                String msg = e.getMessage();
                assertTrue(msg != null && (msg.contains(".env") || msg.contains("GOOGLE_API") || msg.contains("API key") || msg.contains("openAiApiKey")),
                        "Error: expected the menu to throw an exception mentioning .env when the AI is not configured.");
            }
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
            restoreFile(historyFile, historyBackup, historyExisted);
            restoreFile(scoreboardFile, scoreboardBackup, scoreboardExisted);
        }

        String menuOutput = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(menuOutput.contains("HISTÓRICO DE PARTIDAS") || menuOutput.contains("historico"),
                        "Error: expected the historico command to run while game is active."),
                () -> assertTrue(menuOutput.contains("Rajadas") || menuOutput.contains("scoreboard"),
                        "Error: expected the scoreboard command to run while game is active."),
                () -> assertTrue(menuOutput.contains("Bons ventos!"),
                        "Error: expected the menu to terminate after the desisto command.")
        );
    }

    @Test
    @DisplayName("checkPlayerWon devolve true quando a frota inimiga está vazia")
    void checkPlayerWon1() throws Exception {
        assertTrue(invokeCheckPlayerWon(List.of()),
                "Error: expected an empty enemy fleet to mean the player has won.");
    }

    @Test
    @DisplayName("checkPlayerWon devolve false quando ainda há navios inimigos")
    void checkPlayerWon2() throws Exception {
        assertFalse(invokeCheckPlayerWon(List.of(new Barge(Compass.NORTH, new Position(0, 0)))),
                "Error: expected a non-empty enemy fleet to mean the player has not won.");
    }

    @Test
    @DisplayName("executeTurns reinicia um cronómetro que ainda não estava iniciado")
    void executeTurns1() throws Exception {
        assertExecuteTurnsRestartsTimer(false);
    }

    @Test
    @DisplayName("executeTurns para e reinicia um cronómetro já iniciado")
    void executeTurns2() throws Exception {
        assertExecuteTurnsRestartsTimer(true);
    }

    private static String fleetInput(String prefix) {
        StringBuilder input = new StringBuilder(prefix);
        List<int[]> coordinates = new ArrayList<>();
        for (int row = 0; row < 10; row += 2) {
            coordinates.add(new int[]{row, 0});
            coordinates.add(new int[]{row, 3});
        }
        coordinates.add(new int[]{1, 6});
        coordinates.add(new int[]{3, 6});

        for (int[] coordinate : coordinates.subList(0, Fleet.FLEET_SIZE)) {
            input.append("barca ")
                    .append(coordinate[0])
                    .append(' ')
                    .append(coordinate[1])
                    .append(" n ");
        }
        return input.toString();
    }

    private static boolean invokeCheckPlayerWon(List<IShip> floatingShips) throws Exception {
        IFleet enemyFleet = (IFleet) Proxy.newProxyInstance(
                IFleet.class.getClassLoader(),
                new Class<?>[]{IFleet.class},
                (proxy, method, args) -> method.getName().equals("getFloatingShips") ? floatingShips : null
        );
        IGame game = (IGame) Proxy.newProxyInstance(
                IGame.class.getClassLoader(),
                new Class<?>[]{IGame.class},
                (proxy, method, args) -> method.getName().equals("getAlienFleet") ? enemyFleet : null
        );

        Method checkPlayerWon = Tasks.class.getDeclaredMethod("checkPlayerWon", IGame.class);
        checkPlayerWon.setAccessible(true);
        return (boolean) checkPlayerWon.invoke(null, game);
    }

    private static void assertExecuteTurnsRestartsTimer(boolean startTimer) throws Exception {
        AtomicLong recordedTime = new AtomicLong(-1);
        IFleet enemyFleet = (IFleet) Proxy.newProxyInstance(
                IFleet.class.getClassLoader(),
                new Class<?>[]{IFleet.class},
                (proxy, method, args) -> method.getName().equals("getFloatingShips") ? List.of() : null
        );
        IGame game = (IGame) Proxy.newProxyInstance(
                IGame.class.getClassLoader(),
                new Class<?>[]{IGame.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getAlienFleet":
                            return enemyFleet;
                        case "getHits":
                            return 0;
                        case "setLastTime":
                            recordedTime.set((long) args[0]);
                            return null;
                        case "getLastTime":
                            return recordedTime.get();
                        default:
                            return null;
                    }
                }
        );
        StopWatch timer = new StopWatch();
        if (startTimer) {
            timer.start();
        }

        Method executeTurns = Tasks.class.getDeclaredMethod(
                "executeTurns", IGame.class, AIPlayer.class, Scanner.class, String.class, StopWatch.class);
        executeTurns.setAccessible(true);

        Object report;
        try {
            report = executeTurns.invoke(null, game, null, new Scanner(""), "previous report", timer);
        } catch (InvocationTargetException e) {
            throw new AssertionError("Error: expected executeTurns to complete for an already-won game.", e.getCause());
        }

        assertAll(
                () -> assertEquals("previous report", report,
                        "Error: expected the previous AI report to be retained when the player has won."),
                () -> assertTrue(timer.isStarted(),
                        "Error: expected executeTurns to restart the timer before returning."),
                () -> assertTrue(recordedTime.get() >= 0,
                        "Error: expected executeTurns to record the elapsed turn time.")
        );
        timer.stop();
    }

    private static void restoreFile(Path file, Path backup, boolean existedBeforeTest) throws Exception {
        Files.deleteIfExists(file);
        if (existedBeforeTest) {
            Files.copy(backup, file);
        }
    }
}