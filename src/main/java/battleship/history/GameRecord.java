package battleship.history;

import battleship.IGame;
import battleship.IMove;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameRecord {
    private String timestamp; // tempo
    private String outcome; //vitoria, derrota, desistencia.....
    private int totalShots;
    private int totalHits;

    @JsonDeserialize(contentAs = battleship.Move.class)
    private List<IMove> moves = new ArrayList<>(); //todos os movimentos do jogo

    public GameRecord() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void addMove(IMove move) {
        this.moves.add(move); //adiciona aos movimentos
        this.totalShots += move.getShots().size(); //adiciona os shots
        if (move.getShotResults() != null) {
            for(IGame.ShotResult result : move.getShotResults()) {
                if (result.ship() != null) { this.totalHits++; }
            }
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
    public List<IMove> getMoves() { return moves; }
    public void setMoves(List<IMove> moves) { this.moves = moves; }
}
