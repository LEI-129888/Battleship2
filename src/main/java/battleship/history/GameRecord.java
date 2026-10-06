package battleship.history;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameRecord {
    private String timestamp; // tempo
    private String outcome; //vitoria, derrota, desistencia.....
    private int totalShots;
    private int totalHits;
    private List<MoveRecord> moves = new ArrayList<>(); //todos os movimentos do jogo

    public GameRecord() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public GameRecord(String outcome) {
        this(); //constroi: basicamente faz a atribuição do tempo, como faz com o construtor default
        this.outcome = outcome; //mas adiciona o outcome
    }

    public void addMove(MoveRecord move) {
        this.moves.add(move); //adiciona aos movimentos
        this.totalShots++; //adiciona o shot deste movimento
        if(move.isHit()) { //se este movimento atingiu algo, adiciona-se tambem
            this.totalHits++;
        }
    }

    //getters e setters:
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public int getTotalShots() { return totalShots; }
    public void setTotalShots(int totalShots) { this.totalShots = totalShots; }
    public int getTotalHits() { return totalHits; }
    public void setTotalHits(int totalHits) { this.totalHits = totalHits; }

    public List<MoveRecord> getMoves() { return moves; }
    public void setMoves(List<MoveRecord> moves) {
        this.moves = moves;
        this.totalShots = moves.size(); //aqui damos replace a toda uma lista ent o totalShots tem de ser o tamanho da lista
        this.totalHits = (int) moves.stream().filter(MoveRecord::isHit).count(); //filtrar os movimentos que são hit, desta nova lista
    }
}
