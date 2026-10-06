package battleship.history;

//imports do Jackson para escrever o JSON legível
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameHistoryWriter {
    private static final String DEFAULT_FILE_NAME= "historico_jogos.json"; //nome default para se algo der erro no nome do ficheiro
    private final File storageFile; //local onde guardar ficheiro
    private final ObjectMapper objectMapper; //guarda o json legível e lê o que estava escrito anteriormente no ficheiro

    public GameHistoryWriter() {
        this(DEFAULT_FILE_NAME);
    }
    public GameHistoryWriter(String fileName) {
        this.storageFile = new File(fileName);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); //indenta o json e deixa legível
    }

    public List<GameRecord> loadAllGames() { //carrega as partidas anteriormente guardadas no ficheiro JSON
        if (!storageFile.exists() || storageFile.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(storageFile, new TypeReference<List<GameRecord>>() {});
        } catch (IOException e) {
            System.err.println("Erro ao carregar o histórico de jogos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public synchronized boolean saveGame(GameRecord game) { //adiciona novo jogo ao histórico e grava no JSON
        List<GameRecord> lastGames = loadAllGames();
        lastGames.add(game);

        try {
            objectMapper.writeValue(storageFile, lastGames);
            System.out.println("Partida guardada com sucesso em: " + storageFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Falha ao gravar partida em JSON: " + e.getMessage());
            return false;
        }
    }
}
