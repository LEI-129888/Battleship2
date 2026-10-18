package battleship;

import battleship.history.GameHistoryWriter;
import battleship.history.GameRecord;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.apache.commons.lang3.time.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class BoardTUI {
    private final Game game;
    private final GameRecord current;
    private final GameHistoryWriter historyWriter;
    private final AIPlayer ai;
    private String lastAiResult = "";

    private WindowBasedTextGUI gui;
    private BasicWindow window;
    private Label myBoardLabel;
    private Label enemyBoardLabel;
    private Label statusLabel;

    public BoardTUI(Game game, GameRecord current, GameHistoryWriter historyWriter, AIPlayer ai) {
        this.game = game;
        this.current = current;
        this.historyWriter = historyWriter;
        this.ai = (ai != null) ? ai : new AIPlayer();
    }

    public BoardTUI(Game game, GameRecord current, GameHistoryWriter historyWriter) {
        this(game, current, historyWriter, new AIPlayer());
    }

    public void start() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();

        try (Terminal terminal = terminalFactory.createTerminal(); Screen screen = new TerminalScreen(terminal)) {
            screen.startScreen();
            gui = new MultiWindowTextGUI(screen);
            window = new BasicWindow("Battleship - Menu");
            window.setHints(java.util.List.of(Window.Hint.CENTERED));

            Panel main = new Panel(new LinearLayout(Direction.VERTICAL));
            Panel boardsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

            Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
            leftPanel.addComponent(new Label("--- A minha frota ---"));
            myBoardLabel = new Label("");
            leftPanel.addComponent(myBoardLabel);

            Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));
            rightPanel.addComponent(new Label("--- Frota inimiga ---"));
            enemyBoardLabel = new Label("");
            rightPanel.addComponent(enemyBoardLabel);

            boardsPanel.addComponent(leftPanel.withBorder(Borders.singleLine()));
            boardsPanel.addComponent(rightPanel.withBorder(Borders.singleLine()));
            main.addComponent(boardsPanel);

            statusLabel = new Label("Jogo pronto. Escolha uma ação abaixo.");
            main.addComponent(statusLabel);

            Panel buttonsPanel = new Panel(new GridLayout(4));
            buttonsPanel.addComponent(new Button("Rajada", this::onRajada));
            buttonsPanel.addComponent(new Button("Simula", this::onSimula));
            buttonsPanel.addComponent(new Button("Estado", this::onEstado));
            buttonsPanel.addComponent(new Button("Tiros", this::onTiros));
            buttonsPanel.addComponent(new Button("Scoreboard", this::onScoreboard));
            buttonsPanel.addComponent(new Button("Historico", this::onHistorico));
            buttonsPanel.addComponent(new Button("Tempo", this::onTempo));
            buttonsPanel.addComponent(new Button("Ajuda", this::onAjuda));
            buttonsPanel.addComponent(new Button("Sair", () -> window.close()));

            main.addComponent(buttonsPanel.withBorder(Borders.singleLine("Comandos")));

            window.setComponent(main);
            refreshDisplay();
            gui.addWindowAndWait(window);
            screen.stopScreen();
        } catch (IOException e) {
            System.err.println("Erro a mostrar o terminal TUI: " + e.getMessage());
        }
    }

    private void gameOver(String motivo, boolean playerWon) {
        game.over();
        if (current != null && historyWriter != null) {
            current.setOutcome(playerWon ? "VITÓRIA" : "DERROTA");
            for (IMove move : game.getAlienMoves()) {
                current.addMove(move);
            }
            historyWriter.saveGame(current);
        }
        String msg = motivo + "\n\n" +
                "+--------------------------------------------------------------+\n" +
                "| Maldito sejas, Java Sparrow, eu voltarei, glub glub glub ... |\n" +
                "+--------------------------------------------------------------+";
        MessageDialog.showMessageDialog(gui, "Fim de Jogo", msg, MessageDialogButton.OK);
        window.close();
    }

    private void refreshDisplay() {
        myBoardLabel.setText(renderBoardToString(game.getBoard(true)));
        enemyBoardLabel.setText(renderBoardToString(getEnemyBoard()));
        String tempoInfo = (game.getLastTime() != -1)
                ? " | Tempo da jogada: " + (game.getLastTime() / 1000.0) + "s"
                : "";
        statusLabel.setText("Meus navios: " + game.getRemainingShips() +
                " | Navios inimigos: " + game.getAlienFleet().getFloatingShips().size() +
                " | Acertos: " + game.getHits() +
                " | Inválidos: " + game.getInvalidShots() +
                tempoInfo);
    }

    private String renderBoardToString(char[][] board) {
        StringBuilder s = new StringBuilder();
        s.append("   1 2 3 4 5 6 7 8 9 10\n");
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            s.append((char) ('A' + r)).append("  ");
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                s.append(board[r][c]).append(" ");
            }
            s.append("\n");
        }
        return s.toString();
    }

    private char[][] getEnemyBoard() {
        char[][] board = new char[Game.BOARD_SIZE][Game.BOARD_SIZE];
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                board[r][c] = '.';
            }
        }
        for (IShip ship : game.getAlienFleet().getSunkShips()) {
            for (IPosition adj : ship.getAdjacentPositions()) {
                board[adj.getRow()][adj.getColumn()] = '-';
            }
        }

        for (IMove move : game.getMyMoves()) {
            for (IPosition shot : move.getShots()) {
                if (shot.isInside()) {
                    IShip hitShip = game.getAlienFleet().shipAt(shot);
                    board[shot.getRow()][shot.getColumn()] = (hitShip != null) ? '*' : 'o';
                }
            }
        }
        return board;
    }

    private void onRajada() {
        StopWatch timer = new StopWatch();
        timer.start();
        String input = TextInputDialog.showDialog(gui, "Disparar Rajada", "Indique 3 coordenadas", "");
        if (input != null && !input.trim().isEmpty()) {
            try {
                timer.stop();
                game.setLastTime(timer.getTime());

                Scanner sc = new Scanner(new ByteArrayInputStream(input.getBytes()));

                int enemyShipsBefore = game.getAlienFleet().getFloatingShips().size();
                int hitsBefore = game.getHits();

                game.readPlayerFire(sc);

                int enemyShipsAfter = game.getAlienFleet().getFloatingShips().size();
                int hitsAfter = game.getHits();
                SoundManager.play(Tasks.decideSoundToPlay(enemyShipsBefore, enemyShipsAfter, hitsBefore, hitsAfter).concat(".mp3"));

                if (game.getAlienFleet().getFloatingShips().isEmpty()) {
                    refreshDisplay();
                    MessageDialog.showMessageDialog(gui, "Fim da Jogada",
                            "Tempo da jogada: " + (game.getLastTime() / 1000.0) + "s", MessageDialogButton.OK);
                    gameOver("Afundaste todos os navios inimigos! Vitória!", true);
                    return;
                }

                statusLabel.setText("IA a processar a resposta...");
                String aiShotsJson = ai.nextShot(lastAiResult);
                if (aiShotsJson != null) {
                    try {
                        List<IPosition> aiShots = Tasks.parseAiShots(aiShotsJson);
                        game.fireShots(aiShots);
                    } catch (Exception ex) {
                        game.randomEnemyFire();
                    }
                } else {
                    game.randomEnemyFire();
                }

                IMove lastMove = game.getAlienMoves().getLast();
                lastAiResult = lastMove.processEnemyFire(false);

                refreshDisplay();
                System.out.println("Tempo da jogada: " + (game.getLastTime() / 1000.0) + "s");

                if (game.getRemainingShips() == 0) {
                    gameOver("Todos os teus navios foram afundados pela IA.", false);
                }
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Erro", e.getMessage(), MessageDialogButton.OK);
            } finally {
                timer.reset();
            }
        }
    }

    private void onSimula() {
        if (game.getRemainingShips() > 0) {
            game.randomEnemyFire();
            refreshDisplay();
            if (game.getRemainingShips() == 0) {
                gameOver("Frota destruída na simulação", false);
            }
        }
    }

    private void onEstado() {
        StringBuilder s = new StringBuilder();
        s.append("Meus navios a flutuar: ").append(game.getRemainingShips()).append("\n")
                .append("Navios inimigos a flutuar: ").append(game.getAlienFleet().getFloatingShips().size()).append("\n")
                .append("Total de acertos: ").append(game.getHits());
        MessageDialog.showMessageDialog(gui, "Estado da Frota", s.toString(), MessageDialogButton.OK);
    }

    private void onTiros() {
        MessageDialog.showMessageDialog(gui, "Legenda de Tiros", "* = Tiro em navio\no = Tiro na água\n. = Mar Aberto", MessageDialogButton.OK);
    }

    private void onScoreboard() {
        ScoreBoardManager.saveScore("Jogador", game.getAlienMoves().size(), game.getHits(), game.getAlienFleet().getFloatingShips().isEmpty());
        MessageDialog.showMessageDialog(gui, "Scoreboard", "Pontuação atual registada no scoreboard.", MessageDialogButton.OK);
    }

    private void onHistorico() {
        List<GameRecord> historico = (historyWriter != null) ? historyWriter.loadAllGames() : new GameHistoryWriter().loadAllGames();
        if (historico.isEmpty()) {
            MessageDialog.showMessageDialog(gui, "Histórico de Partidas", "Nenhuma partida guardada até ao momento.", MessageDialogButton.OK);
            return;
        }
        StringBuilder s = new StringBuilder();
        for (GameRecord r : historico) {
            s.append(String.format("[%s] Resultado: %s | Tiros: %d | Acertos: %d%n", r.getTimestamp(), r.getOutcome(), r.getTotalShots(), r.getTotalHits()));
        }
        MessageDialog.showMessageDialog(gui, "Histórico de Partidas", s.toString(), MessageDialogButton.OK);
    }

    private void onAjuda() {
        MessageDialog.showMessageDialog(gui, "Ajuda", "Clique nos botões de comando.\nRajada pede 3 coordenadas e joga contra a IA.\nSimula dispara uma jogada automática.\n", MessageDialogButton.OK);
    }

    private void onTempo() {
        long tempo = game.getLastTime();
        if (tempo == -1)
            MessageDialog.showMessageDialog(gui, "Tempo", "Ainda não foi executada nenhuma rajada.");
        else
            MessageDialog.showMessageDialog(gui, "Tempo", "Tempo da última jogada: " + game.getLastTime() / 1000.0 + "s");
    }
}