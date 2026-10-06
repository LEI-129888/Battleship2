package battleship;

import de.vandermeer.asciitable.AsciiTable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreBoardManager {
    private static final String SCOREBOARD_FILE = "scoreboard.csv";

    public static void saveScore(String playerName, int totalMoves, int hits, boolean won) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String status = won ? "Vitoria" : "Incompleto/Derrota";

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(SCOREBOARD_FILE, true)))) {
            out.println(date + ";" + playerName + ";" + totalMoves + ";" + hits + ";" + status);
        } catch (IOException e) {
            System.err.println("Erro ao guardar no scoreboard: " + e.getMessage());
        }
    }

    public static void printScoreboard() {
        File file = new File(SCOREBOARD_FILE);
        if (!file.exists()) {
            System.out.println("Ainda nao existem jogos registados no Scoreboard.");
            return;
        }

        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Data", "Jogador", "Rajadas", "Tiros Certeiros", "Resultado");
        at.addRule();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 5) {
                    at.addRow(parts[0], parts[1], parts[2], parts[3], parts[4]);
                    at.addRule();
                }
            }
            System.out.println(at.render());
        } catch (IOException e) {
            System.err.println("Erro ao ler o scoreboard: " + e.getMessage());
        }
    }
}