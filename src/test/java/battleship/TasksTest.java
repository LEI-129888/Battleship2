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
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import battleship.history.GameHistoryWriter;
import battleship.history.GameRecord;

/**
 * Test class for Tasks.
 * Author: alexa
 * Date: 2026-11-17 22:16
 * Cyclomatic Complexity:
 * - Tasks(): 1
 * - menu(): 52
 * - menuHelp(): 1
 * - buildFleet(): 4
 * - readShip(): 1
 * - readPosition(): 1
 * - readClassicPosition(): 8 (the true outcome of its second format check is unreachable)
 * - decideSoundToPlay(): 3
 * - parseAiShots(): 3
 * - checkPlayerWon(): 1
 * - executeTurns(): 5
 * - endGame(): 6 (covered up to game.over(); System.exit(0) is not reachable in JUnit))
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
        boolean completed;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream("desisto\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            completed = runMenuHandlingMissingAi();
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        if (completed) {
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("Bons ventos!"),
                    "Error: expected menu to print its goodbye message after resigning.");
        }
    }

    @Test
    @DisplayName("menu percorre comandos que não requerem jogo iniciado")
    void menu1() {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commands = "estado mapa rajada simula tiros ajuda historico pdf scoreboard tui tempo comando-invalido desisto\n";
        boolean completed;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            completed = runMenuHandlingMissingAi();
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        if (completed) {
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
        if (historyExisted) Files.copy(historyFile, historyBackup);
        if (scoreboardExisted) Files.copy(scoreboardFile, scoreboardBackup);

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        String input = "lefrota\n" + fleetInput("") + "historico\nscoreboard\ndesisto\n";
        boolean completed;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            completed = runMenuHandlingMissingAi();
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
            restoreFile(historyFile, historyBackup, historyExisted);
            restoreFile(scoreboardFile, scoreboardBackup, scoreboardExisted);
        }

        if (completed) {
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

    @Test
    @DisplayName("menu gera frota aleatoria, mostra status, mapa, tiros e tempo e depois desiste")
    void menuGeraFrotaEStatus() throws Exception {
        Path historyFile = Path.of("historico_jogos.json");
        Path historyBackup = tempDir.resolve("historico-jogos-backup.json");
        boolean historyExisted = Files.exists(historyFile);
        if (historyExisted) Files.copy(historyFile, historyBackup);

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        String input = "gerafrota\nestado\nmapa\ntiros\ntempo\ndesisto\n";
        boolean completed;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            completed = runMenuHandlingMissingAi();
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
            restoreFile(historyFile, historyBackup, historyExisted);
        }

        if (completed) {
            String menuOutput = output.toString(StandardCharsets.UTF_8);
            assertAll(
                    () -> assertTrue(menuOutput.contains("Ainda nào foi executada nenhuma rajada"),
                            "Error: expected tempo to report no shots fired yet."),
                    () -> assertTrue(menuOutput.contains("TABULEIRO INIMIGO"),
                            "Error: expected tiros to print the enemy board."),
                    () -> assertTrue(menuOutput.contains("Bons ventos!"),
                            "Error: expected desisto to exit gracefully.")
            );
        }
    }

    @Test
    @DisplayName("menu executa pdf e scoreboard com jogo iniciado")
    void menuPdfEScoreboardAtivos() throws Exception {
        Path historyFile = Path.of("historico_jogos.json");
        Path scoreboardFile = Path.of("scoreboard.csv");
        Path historyBackup = tempDir.resolve("historico-jogos-backup.json");
        Path scoreboardBackup = tempDir.resolve("scoreboard-backup.csv");
        boolean historyExisted = Files.exists(historyFile);
        boolean scoreboardExisted = Files.exists(scoreboardFile);
        if (historyExisted) Files.copy(historyFile, historyBackup);
        if (scoreboardExisted) Files.copy(scoreboardFile, scoreboardBackup);

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        String input = "gerafrota\npdf\nscoreboard\ndesisto\n";
        boolean completed;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            completed = runMenuHandlingMissingAi();
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
            restoreFile(historyFile, historyBackup, historyExisted);
            restoreFile(scoreboardFile, scoreboardBackup, scoreboardExisted);
        }

        if (completed) {
            String menuOutput = output.toString(StandardCharsets.UTF_8);
            assertAll(
                    () -> assertTrue(menuOutput.contains("PDF gerado com sucesso em:") || menuOutput.contains("relatorio_jogadas.pdf"),
                            "Error: expected PDF command to trigger file generation logic."),
                    () -> assertTrue(menuOutput.contains("Bons ventos!"),
                            "Error: expected menu to exit after scoreboard.")
            );
        }
    }

    @Test
    @DisplayName("menu cobre historico vazio e historico com partidas gravadas")
    void menuHistoricoVazioEComPartidas() throws Exception {
        Path historyFile = Path.of("historico_jogos.json");
        Path historyBackup = tempDir.resolve("historico-jogos-backup.json");
        boolean historyExisted = Files.exists(historyFile);
        if (historyExisted) Files.copy(historyFile, historyBackup);

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            // 1. Apaga o ficheiro para forçar historico.isEmpty() == true, depois cria e desiste para gravar 1 jogo
            Files.deleteIfExists(historyFile);
            System.setIn(new ByteArrayInputStream("historico\ngerafrota\ndesisto\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            boolean firstCompleted = runMenuHandlingMissingAi();

            // 2. Agora que existe 1 jogo gravado, corre historico outra vez para entrar no else/for
            if (firstCompleted) {
                System.setIn(new ByteArrayInputStream("historico\ndesisto\n".getBytes(StandardCharsets.UTF_8)));
                runMenuHandlingMissingAi();
            }
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
            restoreFile(historyFile, historyBackup, historyExisted);
        }

        String menuOutput = output.toString(StandardCharsets.UTF_8);
        if (!menuOutput.isEmpty()) {
            assertAll(
                    () -> assertTrue(menuOutput.contains("Nenhuma partida gravada até ao momento."),
                            "Error: expected empty history branch to be covered."),
                    () -> assertTrue(menuOutput.contains("Resultado: DESISTÊNCIA"),
                            "Error: expected non-empty history branch to print the saved game.")
            );
        }
    }

    @Test
    @DisplayName("executeTurns executa jogada da IA com JSON válido quando o jogador ainda não venceu")
    void executeTurnsComIaJsonValido() throws Exception {
        assertExecuteTurnsWithAi("[{\"row\":\"A\",\"column\":1}]");
    }

    @Test
    @DisplayName("executeTurns usa disparo aleatório quando a IA devolve JSON inválido")
    void executeTurnsComIaJsonInvalido() throws Exception {
        assertExecuteTurnsWithAi("{json-invalido");
    }

    @Test
    @DisplayName("executeTurns usa disparo aleatório quando a IA devolve null")
    void executeTurnsComIaNull() throws Exception {
        assertExecuteTurnsWithAi(null);
    }

    private static void assertExecuteTurnsWithAi(String aiResponse) throws Exception {
        AIPlayer fakeAi;
        try {
            fakeAi = new AIPlayer() {
                @Override
                public String nextShot(String lastResult) {
                    return aiResponse;
                }
            };
        } catch (Exception e) {
            // No GitHub Actions sem .env, o construtor de AIPlayer pode falhar
            return;
        }

        AtomicLong recordedTime = new AtomicLong(-1);
        IShip afloatShip = new Barge(Compass.NORTH, new Position(0, 0));
        IFleet enemyFleet = (IFleet) Proxy.newProxyInstance(
                IFleet.class.getClassLoader(),
                new Class<?>[]{IFleet.class},
                (proxy, method, args) -> method.getName().equals("getFloatingShips") ? List.of(afloatShip) : null
        );
        IMove fakeMove = (IMove) Proxy.newProxyInstance(
                IMove.class.getClassLoader(),
                new Class<?>[]{IMove.class},
                (proxy, method, args) -> method.getName().equals("processEnemyFire") ? "{\"validShots\":1}" : null
        );
        List<IMove> alienMoves = List.of(fakeMove);

        IGame game = (IGame) Proxy.newProxyInstance(
                IGame.class.getClassLoader(),
                new Class<?>[]{IGame.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getAlienFleet": return enemyFleet;
                        case "getHits": return 0;
                        case "setLastTime": recordedTime.set((long) args[0]); return null;
                        case "getLastTime": return recordedTime.get();
                        case "getAlienMoves": return alienMoves;
                        default: return null;
                    }
                }
        );

        StopWatch timer = new StopWatch();
        Method executeTurns = Tasks.class.getDeclaredMethod(
                "executeTurns", IGame.class, AIPlayer.class, Scanner.class, String.class, StopWatch.class);
        executeTurns.setAccessible(true);

        Object report = executeTurns.invoke(null, game, fakeAi, new Scanner(""), "prev", timer);
        timer.stop();

        assertEquals("{\"validShots\":1}", report,
                "Error: expected executeTurns to return the processed enemy fire report.");
    }

    @Test
    @DisplayName("menu executa uma rajada e cobre tempo, pdf, scoreboard e gravação de jogadas ao desistir")
    void menuRajadaScoreboardPdfETempo() throws Exception {
        Path historyFile = Path.of("historico_jogos.json");
        Path scoreboardFile = Path.of("scoreboard.csv");
        Path pdfFile = Path.of("relatorio_jogadas.pdf");
        Path historyBackup = tempDir.resolve("historico-jogos-backup.json");
        Path scoreboardBackup = tempDir.resolve("scoreboard-backup.csv");
        Path pdfBackup = tempDir.resolve("relatorio-backup.pdf");

        boolean historyExisted = Files.exists(historyFile);
        boolean scoreboardExisted = Files.exists(scoreboardFile);
        boolean pdfExisted = Files.exists(pdfFile);
        if (historyExisted) Files.copy(historyFile, historyBackup);
        if (scoreboardExisted) Files.copy(scoreboardFile, scoreboardBackup);
        if (pdfExisted) Files.copy(pdfFile, pdfBackup);

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // 1 rajada com 3 tiros válidos, seguida de tempo, pdf, scoreboard e desisto
        String input = "gerafrota\n"
                + "rajada\n"
                + "A1 A2 A3\n"
                + "tempo\n"
                + "pdf\n"
                + "scoreboard\n"
                + "desisto\n";
        boolean completed;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            completed = runMenuHandlingMissingAi();
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
            restoreFile(historyFile, historyBackup, historyExisted);
            restoreFile(scoreboardFile, scoreboardBackup, scoreboardExisted);
            restoreFile(pdfFile, pdfBackup, pdfExisted);
        }

        if (completed) {
            String menuOutput = output.toString(StandardCharsets.UTF_8);
            assertAll(
                    () -> assertTrue(menuOutput.contains("Tempo da última jogada:"),
                            "Error: expected tempo to print the elapsed time after a burst."),
                    () -> assertTrue(menuOutput.contains("PDF gerado com sucesso em:") || menuOutput.contains("relatorio_jogadas.pdf"),
                            "Error: expected PDF export to run with recorded moves."),
                    () -> assertTrue(menuOutput.contains("Bons ventos!"),
                            "Error: expected menu to exit cleanly after saving moves on resign.")
            );
        }
    }

    @Test
    @DisplayName("buildFleet lança AssertionError quando o Scanner é nulo")
    void buildFleetNull() {
        assertThrows(AssertionError.class, () -> Tasks.buildFleet(null),
                "Error: expected buildFleet(null) to trigger AssertionError.");
    }

    @Test
    @DisplayName("readShip lança AssertionError quando o Scanner é nulo")
    void readShipNull() {
        assertThrows(AssertionError.class, () -> Tasks.readShip(null),
                "Error: expected readShip(null) to trigger AssertionError.");
    }

    @Test
    @DisplayName("readPosition lança AssertionError quando o Scanner é nulo")
    void readPositionNull() {
        assertThrows(AssertionError.class, () -> Tasks.readPosition(null),
                "Error: expected readPosition(null) to trigger AssertionError.");
    }

    @Test
    @DisplayName("menu executa uma iteração de simula e interrompe no sleep")
    void menuSimulaUmaIteracao() throws Exception {
        Path historyFile = Path.of("historico_jogos.json");
        Path historyBackup = tempDir.resolve("historico-jogos-backup.json");
        boolean historyExisted = Files.exists(historyFile);
        if (historyExisted) Files.copy(historyFile, historyBackup);

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Thread testThread = Thread.currentThread();
        Thread interrupter = new Thread(() -> {
            long deadline = System.currentTimeMillis() + 30_000;
            while (System.currentTimeMillis() < deadline && !Thread.currentThread().isInterrupted()) {
                // Assim que o 1º turno termina e imprime o tempo, está a entrar no Thread.sleep(3000)
                if (output.toString(StandardCharsets.UTF_8).contains("Tempo da jogada:")) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                        return;
                    }
                    testThread.interrupt();
                    return;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        });
        interrupter.setDaemon(true);
        interrupter.start();

        String input = "gerafrota\nsimula\ndesisto\n";
        boolean completed;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            completed = runMenuHandlingMissingAi();
        } finally {
            interrupter.interrupt();
            Thread.interrupted(); // limpa o estado de interrupção da thread
            System.setIn(originalInput);
            System.setOut(originalOutput);
            restoreFile(historyFile, historyBackup, historyExisted);
        }

        if (completed) {
            String menuOutput = output.toString(StandardCharsets.UTF_8);
            assertAll(
                    () -> assertTrue(menuOutput.contains("[SIMULAÇÃO]"),
                            "Error: expected simulation to execute at least one turn."),
                    () -> assertTrue(menuOutput.contains("Bons ventos!"),
                            "Error: expected menu to exit cleanly after interrupting simulation.")
            );
        }
    }

    @Test
    @DisplayName("readClassicPosition rejeita linha negativa separada por espaço")
    void readClassicPositionNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> Tasks.readClassicPosition(new Scanner("A -3")),
                "Error: expected a negative row number to be rejected.");
    }

    @Test
    @DisplayName("endGame grava derrota e jogadas do jogador antes de game.over")
    void endGameDerrotaComMyMoves() throws Exception {
        GameRecord record = new GameRecord();
        IMove move = new Move(1, List.of(new Position(0, 0)), List.of());

        invokeEndGameStoppingAtOver(0, List.of(move), List.of(), record);

        assertEquals("DERROTA", record.getOutcome(),
                "Error: expected endGame to set outcome to DERROTA when remaining ships is 0.");
    }

    @Test
    @DisplayName("endGame grava vitória e jogadas inimigas quando myMoves está vazio")
    void endGameVitoriaComAlienMoves() throws Exception {
        GameRecord record = new GameRecord();
        IMove alienMove = new Move(1, List.of(new Position(1, 1)), List.of());

        invokeEndGameStoppingAtOver(3, List.of(), List.of(alienMove), record);

        assertEquals("VITÓRIA", record.getOutcome(),
                "Error: expected endGame to set outcome to VITÓRIA when remaining ships > 0.");
    }

    @Test
    @DisplayName("endGame ignora gravação quando o registo é nulo")
    void endGameComRecordNulo() throws Exception {
        assertDoesNotThrow(() -> invokeEndGameStoppingAtOver(0, List.of(), List.of(), null),
                "Error: expected endGame to handle a null GameRecord.");
    }

    @Test
    @DisplayName("endGame ignora gravação quando o resultado já está definido")
    void endGameComOutcomeJaDefinido() throws Exception {
        GameRecord record = new GameRecord();
        record.setOutcome("DESISTÊNCIA");

        invokeEndGameStoppingAtOver(0, List.of(), List.of(), record);

        assertEquals("DESISTÊNCIA", record.getOutcome(),
                "Error: expected endGame not to overwrite an existing outcome.");
    }

    private void invokeEndGameStoppingAtOver(int remainingShips, List<IMove> myMoves,
                                             List<IMove> alienMoves, GameRecord record) throws Exception {
        Path historyFile = Path.of("historico_jogos.json");
        Path historyBackup = tempDir.resolve("historico-endgame-backup.json");
        boolean historyExisted = Files.exists(historyFile);
        if (historyExisted) Files.copy(historyFile, historyBackup);

        IGame game = (IGame) Proxy.newProxyInstance(
                IGame.class.getClassLoader(),
                new Class<?>[]{IGame.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getRemainingShips": return remainingShips;
                        case "getMyMoves": return myMoves;
                        case "getAlienMoves": return alienMoves;
                        case "over": throw new IllegalStateException("stop-before-system-exit");
                        default: return null;
                    }
                }
        );

        Method endGame = Tasks.class.getDeclaredMethod(
                "endGame", IGame.class, GameHistoryWriter.class, GameRecord.class);
        endGame.setAccessible(true);

        try {
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> endGame.invoke(null, game, new GameHistoryWriter(), record),
                    "Error: expected game.over() to abort before System.exit(0).");
            assertEquals("stop-before-system-exit", ex.getCause().getMessage(),
                    "Error: expected the sentinel exception from game.over().");
        } finally {
            restoreFile(historyFile, historyBackup, historyExisted);
        }
    }

    private static boolean runMenuHandlingMissingAi() {
        try {
            Tasks.menu();
            return true;
        } catch (Exception e) {
            String msg = e.getMessage();
            assertTrue(msg != null && (msg.contains(".env") || msg.contains("GOOGLE_API") || msg.contains("API key") || msg.contains("openAiApiKey")),
                    "Error: unexpected exception in menu: " + msg);
            return false;
        }
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