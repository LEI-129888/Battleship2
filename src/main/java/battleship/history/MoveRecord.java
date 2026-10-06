package battleship.history;

public class MoveRecord {
    private int row;
    private int column;
    private boolean hit; //se atingiu algo
    private String shipHit; //que navio atingiu (ou água)

    public MoveRecord() {} //vazio para dar para o JSON

    public MoveRecord(int row, int column, boolean hit, String shipHit) {
        this.row = row;
        this.column = column;
        this.hit = hit;
        this.shipHit = shipHit;
    }
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getColumn() {
        return column;
    }
    public void setColumn(int column) {
        this.column = column;
    }

    public boolean isHit() {
        return hit;
    }
    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public String getShipHit() {
        return shipHit;
    }
    public void setShipHit(String shipHit) {
        this.shipHit = shipHit;
    }

    @Override
    public String toString() {
        return String.format("Posição [%d, %d] -> %s",
                row,
                column,
                hit ? "Tiro em " + shipHit : "Água");
    }
}
