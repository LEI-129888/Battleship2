package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfExporterTest {

    @Test
    @DisplayName("Garante que a exportação ocorre sem erros usando uma lista válida")
    void testExportMovesToPdfWithValidData() {
        //1. Arrange: Preparar os dados (lista vazia para não dar erro no construtor)
        List<IMove> moves = new ArrayList<>();
        String filename = "teste_exportacao.pdf";
        PdfExporter exporter = new PdfExporter();

        //2. Act: Tentar criar o PDF. Se rebentar com uma exceção, o teste falhará.
        assertDoesNotThrow(() -> exporter.exportMovesToPdf(moves, filename));

        //3. Assert: Verificar se o arquivo foi criado
        File file = new java.io.File(filename);
        assertTrue(file.exists(), "O arquivo PDF não foi criado.");

        //4. Limpeza: Remover o arquivo criado após o teste
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("Exporta várias jogadas, percorrendo a lista")
    void testExportMovesToPdfWithMultipleMoves(@TempDir Path tempDir) throws Exception {
        List<IMove> moves = List.of(
                new Move(1, List.of(new Position(1, 1)), List.of()),
                new Move(2, List.of(new Position(2, 2)), List.of())
        );
        Path pdf = tempDir.resolve("jogadas.pdf");

        assertDoesNotThrow(() -> PdfExporter.exportMovesToPdf(moves, pdf.toString()));
        assertTrue(Files.exists(pdf), "O PDF com várias jogadas não foi criado.");
        assertTrue(Files.size(pdf) > 0, "O PDF criado não deve estar vazio.");
    }

    @Test
    @DisplayName("Trata exceções ocorridas durante a exportação")
    void testExportMovesToPdfHandlesException(@TempDir Path tempDir) {
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;
        Path pdf = tempDir.resolve("jogadas-invalidas.pdf");

        try (PrintStream capturedError = new PrintStream(errorOutput)) {
            System.setErr(capturedError);
            assertDoesNotThrow(() -> PdfExporter.exportMovesToPdf(null, pdf.toString()));
        } finally {
            System.setErr(originalError);
        }

        assertTrue(errorOutput.toString().contains("Erro ao gerar PDF:"),
                "A exceção deve ser comunicada pelo catch.");
    }
}