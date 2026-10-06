package battleship;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExporter {

    public static void exportMovesToPdf(List<IMove> moves, String filename) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("Relatorio de Jogadas - Batalha Naval", titleFont));

            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            document.add(new Paragraph("Data da partida: " + date));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidths(new int[]{1, 4});
            table.addCell("N. Rajada");
            table.addCell("Coordenadas Disparadas");

            int moveNumber = 1;
            for (IMove move : moves) {
                table.addCell(String.valueOf(moveNumber++));
                table.addCell("Tiros: " + move.getShots().toString());
            }

            document.add(table);
            System.out.println("PDF gerado com sucesso em: " + filename);

        } catch (Exception e) {
            System.err.println("Erro ao gerar PDF: " + e.getMessage());
        } finally {
            document.close();
        }
    }
}