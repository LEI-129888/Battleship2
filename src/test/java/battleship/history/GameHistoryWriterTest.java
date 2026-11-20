package battleship.history;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for GameHistoryWriter.
 * Author: alexa
 * Date: 2026-11-17 22:12
 * Cyclomatic Complexity:
 * - GameHistoryWriter(): 1
 * - GameHistoryWriter(String): 1
 * - loadAllGames(): 4
 * - saveGame(): 2
 */
class GameHistoryWriterTest {

    @TempDir
    Path tempDir;

    private GameHistoryWriter writer;
    private Path historyFile;

    @BeforeEach
    void setUp() {
        historyFile = tempDir.resolve("history.json");
        writer = new GameHistoryWriter(historyFile.toString());
    }

    @AfterEach
    void tearDown() {
        writer = null;
        historyFile = null;
    }

    @Test
    @DisplayName("O construtor sem argumentos cria um writer")
    void constructor() {
        assertDoesNotThrow(() -> new GameHistoryWriter(),
                "Error: expected the no-argument constructor to create a writer.");
    }

    @Test
    @DisplayName("O construtor com nome de ficheiro cria um writer")
    void constructor1() {
        assertNotNull(writer,
                "Error: expected the filename constructor to create a writer.");
    }

    @Test
    @DisplayName("loadAllGames devolve lista vazia quando o ficheiro não existe")
    void loadAllGames1() {
        assertFalse(Files.exists(historyFile),
                "Error: expected the history file not to exist before loading.");

        assertTrue(writer.loadAllGames().isEmpty(),
                "Error: expected an empty list when the history file does not exist.");
    }

    @Test
    @DisplayName("loadAllGames devolve lista vazia quando o ficheiro está vazio")
    void loadAllGames2() throws Exception {
        Files.createFile(historyFile);

        assertTrue(writer.loadAllGames().isEmpty(),
                "Error: expected an empty list when the existing history file has zero bytes.");
    }

    @Test
    @DisplayName("loadAllGames carrega registos de JSON válido")
    void loadAllGames3() {
        GameRecord record = new GameRecord();
        record.setOutcome("Vitoria");
        assertTrue(writer.saveGame(record),
                "Error: expected saving a valid record to succeed before loading it.");

        List<GameRecord> records = writer.loadAllGames();
        assertAll(
                () -> assertEquals(1, records.size(),
                        "Error: expected one record to be loaded from valid JSON."),
                () -> assertEquals("Vitoria", records.getFirst().getOutcome(),
                        "Error: expected the loaded record to retain its outcome.")
        );
    }

    @Test
    @DisplayName("loadAllGames trata conteúdo JSON inválido")
    void loadAllGames4() throws Exception {
        Files.writeString(historyFile, "{ invalid json", StandardCharsets.UTF_8);
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;
        List<GameRecord> records;

        try (PrintStream capturedError = new PrintStream(errorOutput, true, StandardCharsets.UTF_8)) {
            System.setErr(capturedError);
            records = writer.loadAllGames();
        } finally {
            System.setErr(originalError);
        }

        assertAll(
                () -> assertTrue(records.isEmpty(),
                        "Error: expected malformed JSON to produce an empty list."),
                () -> assertTrue(errorOutput.toString(StandardCharsets.UTF_8)
                                .contains("Erro ao carregar o histórico de jogos:"),
                        "Error: expected the caught JSON read failure to be reported.")
        );
    }

    @Test
    @DisplayName("saveGame grava um registo e devolve true")
    void saveGame1() {
        GameRecord record = new GameRecord();
        record.setOutcome("Vitoria");

        boolean saved = writer.saveGame(record);

        assertAll(
                () -> assertTrue(saved,
                        "Error: expected saveGame to return true when writing succeeds."),
                () -> assertTrue(Files.exists(historyFile),
                        "Error: expected saveGame to create the history file."),
                () -> assertEquals(1, writer.loadAllGames().size(),
                        "Error: expected the saved record to be present in the history.")
        );
    }

    @Test
    @DisplayName("saveGame trata um erro de escrita e devolve false")
    void saveGame2() throws Exception {
        Files.createDirectory(historyFile);
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;
        boolean saved;

        try (PrintStream capturedError = new PrintStream(errorOutput, true, StandardCharsets.UTF_8)) {
            System.setErr(capturedError);
            saved = writer.saveGame(new GameRecord());
        } finally {
            System.setErr(originalError);
        }

        assertAll(
                () -> assertFalse(saved,
                        "Error: expected saveGame to return false when writing to a directory."),
                () -> assertTrue(errorOutput.toString(StandardCharsets.UTF_8)
                                .contains("Falha ao gravar partida em JSON:"),
                        "Error: expected the caught JSON write failure to be reported.")
        );
    }
}
