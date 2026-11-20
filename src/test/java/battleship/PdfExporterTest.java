package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PdfExporter.
 * Author: alexa
 * Date: 2026-11-17 22:50
 * Cyclomatic Complexity:
 * - PdfExporter(): 1
 * - exportMovesToPdf(): 3
 */
class PdfExporterTest {

    private PdfExporter exporter;

    @BeforeEach
    @SuppressWarnings("InstantiationOfUtilityClass")
    void setUp() {
        exporter = new PdfExporter();
    }

    @AfterEach
    void tearDown() {
        exporter = null;
    }

    @Test
    @DisplayName("Construtor inicializa a classe PdfExporter")
    void constructor() {
        assertNotNull(exporter,
                "Error: expected setup to create a PdfExporter instance.");
    }

    @Test
    @DisplayName("exportMovesToPdf garante que a exportação ocorre sem erros usando uma lista válida")
    void exportMovesToPdf1(@TempDir Path tempDir) {
        List<IMove> moves = new ArrayList<>();
        Path pdf = tempDir.resolve("teste_exportacao.pdf");

        assertDoesNotThrow(() -> PdfExporter.exportMovesToPdf(moves, pdf.toString()),
                "Error: expected exportMovesToPdf to complete without throwing exceptions.");

        assertTrue(Files.exists(pdf),
                "Error: expected the PDF file to be created for an empty moves list.");
    }

    @Test
    @DisplayName("exportMovesToPdf exporta várias jogadas, percorrendo a lista")
    void exportMovesToPdf2(@TempDir Path tempDir) {
        List<IMove> moves = List.of(
                new Move(1, List.of(new Position(1, 1)), List.of()),
                new Move(2, List.of(new Position(2, 2)), List.of())
        );
        Path pdf = tempDir.resolve("jogadas.pdf");

        assertDoesNotThrow(() -> PdfExporter.exportMovesToPdf(moves, pdf.toString()),
                "Error: expected exportMovesToPdf to complete without throwing exceptions for multiple moves.");

        assertAll(
                () -> assertTrue(Files.exists(pdf),
                        "Error: expected the PDF file to be created for multiple moves."),
                () -> assertTrue(Files.size(pdf) > 0,
                        "Error: expected the created PDF file not to be empty.")
        );
    }

    @Test
    @DisplayName("exportMovesToPdf trata exceções ocorridas durante a exportação")
    void exportMovesToPdf3(@TempDir Path tempDir) {
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;
        Path pdf = tempDir.resolve("jogadas-invalidas.pdf");

        try (PrintStream capturedError = new PrintStream(errorOutput)) {
            System.setErr(capturedError);
            // Passar null vai forçar um erro dentro do método
            assertDoesNotThrow(() -> PdfExporter.exportMovesToPdf(null, pdf.toString()),
                    "Error: expected exportMovesToPdf to handle exceptions internally.");
        } finally {
            System.setErr(originalError);
        }

        assertTrue(errorOutput.toString().contains("Erro ao gerar PDF:"),
                "Error: expected the caught exception to be reported to System.err.");
    }
}