package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ScoreBoardManager.
 * Author: ${user.name}
 * Date: 2026-11-17 21:59
 * Cyclomatic Complexity:
 * - constructor: 1
 * - saveScore(): 3
 * - printScoreboard(): 5
 */
class ScoreBoardManagerTest {

    private static final Path SCOREBOARD_FILE = Path.of("scoreboard.csv").toAbsolutePath();

    @TempDir
    Path tempDir;

    private ScoreBoardManager scoreBoardManager;
    private Path backupFile;
    private boolean originalFileExisted;

    @BeforeEach
    @SuppressWarnings("InstantiationOfUtilityClass")
    void setUp() throws IOException {
        scoreBoardManager = new ScoreBoardManager();
        backupFile = tempDir.resolve("scoreboard-original.csv");
        originalFileExisted = Files.exists(SCOREBOARD_FILE);

        if (originalFileExisted) {
            assertTrue(Files.isRegularFile(SCOREBOARD_FILE),
                    "Error: expected scoreboard.csv to be a regular file before the test.");
            Files.copy(SCOREBOARD_FILE, backupFile);
        }
        Files.deleteIfExists(SCOREBOARD_FILE);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(SCOREBOARD_FILE);
        if (originalFileExisted) {
            Files.copy(backupFile, SCOREBOARD_FILE);
        }
        scoreBoardManager = null;
    }

    @Test
    @DisplayName("O construtor cria uma instância do gestor")
    void constructor() {
        assertNotNull(scoreBoardManager,
                "Error: expected setup to create a ScoreBoardManager instance.");
    }

    @Test
    @DisplayName("saveScore grava o resultado de uma vitória")
    void saveScore1() throws IOException {
        ScoreBoardManager.saveScore("Ana", 12, 8, true);

        String content = Files.readString(SCOREBOARD_FILE);
        assertAll(
                () -> assertTrue(content.contains(";Ana;12;8;Vitoria"),
                        "Error: expected the saved row to contain Ana's winning score.")
        );
    }

    @Test
    @DisplayName("saveScore grava o resultado de uma derrota ou jogo incompleto")
    void saveScore2() throws IOException {
        ScoreBoardManager.saveScore("Rui", 20, 3, false);

        String content = Files.readString(SCOREBOARD_FILE);
        assertAll(
                () -> assertTrue(content.contains(";Rui;20;3;Incompleto/Derrota"),
                        "Error: expected the saved row to contain Rui's incomplete game.")
        );
    }

    @Test
    @DisplayName("saveScore trata um erro ao abrir o ficheiro")
    void saveScore3() throws IOException {
        Files.createDirectory(SCOREBOARD_FILE);
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;

        try (PrintStream capturedError = new PrintStream(errorOutput, true, StandardCharsets.UTF_8)) {
            System.setErr(capturedError);
            assertDoesNotThrow(() -> ScoreBoardManager.saveScore("Ana", 1, 1, true),
                    "Error: expected saveScore to handle the file error without propagating it.");
        } finally {
            System.setErr(originalError);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8).contains("Erro ao guardar no scoreboard:"),
                "Error: expected saveScore to report the caught IOException.");
    }

    @Test
    @DisplayName("printScoreboard informa quando ainda não existe ficheiro")
    void printScoreboard1() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            ScoreBoardManager.printScoreboard();
        } finally {
            System.setOut(originalOutput);
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Ainda nao existem jogos registados"),
                "Error: expected a message when scoreboard.csv does not exist.");
    }

    @Test
    @DisplayName("printScoreboard apresenta o cabeçalho quando o ficheiro está vazio")
    void printScoreboard2() throws IOException {
        Files.createFile(SCOREBOARD_FILE);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            ScoreBoardManager.printScoreboard();
        } finally {
            System.setOut(originalOutput);
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Jogador"),
                "Error: expected the rendered scoreboard to contain its header.");
    }

    @Test
    @DisplayName("printScoreboard apresenta uma linha com cinco campos")
    void printScoreboard3() throws IOException {
        Files.writeString(SCOREBOARD_FILE, "17/11/2026 21:00;Ana;12;8;Vitoria\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            ScoreBoardManager.printScoreboard();
        } finally {
            System.setOut(originalOutput);
        }

        String rendered = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(rendered.contains("Ana"),
                        "Error: expected the rendered scoreboard to contain the player name."),
                () -> assertTrue(rendered.contains("Vitoria"),
                        "Error: expected the rendered scoreboard to contain the game result.")
        );
    }

    @Test
    @DisplayName("printScoreboard ignora linhas que não têm cinco campos")
    void printScoreboard4() throws IOException {
        Files.writeString(SCOREBOARD_FILE, "linha;malformada\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            ScoreBoardManager.printScoreboard();
        } finally {
            System.setOut(originalOutput);
        }

        String rendered = output.toString(StandardCharsets.UTF_8);
        assertTrue(rendered.contains("Jogador"),
                "Error: expected the scoreboard header even when its only row is malformed.");
        assertFalse(rendered.contains("malformada"),
                "Error: expected malformed rows to be excluded from the scoreboard.");
    }

    @Test
    @DisplayName("printScoreboard trata um erro ao ler o ficheiro")
    void printScoreboard5() throws IOException {
        Files.createDirectory(SCOREBOARD_FILE);
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;

        try (PrintStream capturedError = new PrintStream(errorOutput, true, StandardCharsets.UTF_8)) {
            System.setErr(capturedError);
            assertDoesNotThrow(ScoreBoardManager::printScoreboard,
                    "Error: expected printScoreboard to handle the file error without propagating it.");
        } finally {
            System.setErr(originalError);
        }

        assertTrue(errorOutput.toString(StandardCharsets.UTF_8).contains("Erro ao ler o scoreboard:"),
                "Error: expected printScoreboard to report the caught IOException.");
    }
}
