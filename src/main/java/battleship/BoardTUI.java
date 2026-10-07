package battleship;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class BoardTUI {
    public static void displayBoard(char[][] board) {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        try (Terminal terminal = terminalFactory.createTerminal(); Screen screen = new TerminalScreen(terminal)) {
            screen.startScreen();
            screen.clear();

            TextGraphics graphics = screen.newTextGraphics();
            graphics.setForegroundColor(TextColor.ANSI.CYAN);
            graphics.putString(2,1,"=== TABULEIRO BATALHA NAVAL ===");
            graphics.setForegroundColor(TextColor.ANSI.WHITE);

            //cabeçalho das colunas
            for (int c = 0; c < 10; c++) {
                graphics.putString(6+(c*3), 3, String.valueOf(c+1));
            }
            //desenho das linhas
            for (int r = 0; r < 10; r++) {
                char rowLabel = (char) ('A' + r);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);
                graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
                graphics.putString(3, 4+r, String.valueOf(rowLabel)); //letra da linha

                for (int c = 0; c < 10; c++) {
                    char celula = board[r][c];
                    int colPosition = 6 + (c * 3);
                    int rowPosition = 4 + r;

                    switch(celula) {
                        case '#' -> {
                            graphics.setBackgroundColor(TextColor.ANSI.WHITE);
                            graphics.setForegroundColor(TextColor.ANSI.BLACK);
                            graphics.putString(colPosition,rowPosition, " # ");
                        }
                        case '*' -> {
                            graphics.setBackgroundColor(TextColor.ANSI.RED);
                            graphics.setForegroundColor(TextColor.ANSI.YELLOW);
                            graphics.putString(colPosition,rowPosition, " * ");
                        }
                        case 'o' -> {
                            graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                            graphics.setForegroundColor(TextColor.ANSI.WHITE);
                            graphics.putString(colPosition, rowPosition, " o ");
                        }
                        default -> {
                            graphics.setBackgroundColor(TextColor.ANSI.CYAN_BRIGHT);
                            graphics.setForegroundColor(TextColor.ANSI.BLUE);
                            graphics.putString(colPosition,rowPosition," . ");
                        }
                    }
                }
            }
            graphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
            graphics.setForegroundColor(TextColor.ANSI.YELLOW);
            graphics.putString(2, 16, "Pressione qualquer tecla no terminal para voltar...");

            screen.refresh();
            screen.readInput();
            screen.stopScreen();
        } catch (IOException e) {
            System.err.println("Erro a mostrar o terminal TUI: " + e.getMessage());
        }
    }
}
