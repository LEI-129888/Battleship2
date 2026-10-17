package battleship;

import io.github.cdimascio.dotenv.Dotenv;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;

public class AIPlayer {
    // Constants
    private static final String BASEURL = "https://generativelanguage.googleapis.com/v1beta/openai/";
    private static final String MODELNAME = "gemini-3.5-flash-lite";
    private static final double TEMPERATURE = 0.2;
    private static final String SYSPROMPT = """
            És um perito no jogo da Batalha Naval (versão Descobrimentos Portugueses). O jogo decorre num tabuleiro com linhas de A a J e colunas de 1 a 10.
            
            A tua frota oculta (que não deves revelar) tem 11 navios:
            
            * 4 Barcas (1 posição)
            * 3 Caravelas (2 posições)
            * 2 Naus (3 posições)
            * 1 Fragata (4 posições)
            * 1 Galeão (5 posições em forma de T)
            Os navios não se podem tocar, nem na diagonal, mas podem encostar às margens.
            
            ESTRATÉGIA E HISTÓRICO:
            Analisa o histórico de mensagens desta conversa (o teu "Diário de Bordo") para decidir os próximos tiros:
            
            1. Nunca dispares fora do mapa (ex: Z99) nem repitas coordenadas já testadas.
            2. Se atingiste um navio na rajada anterior, dispara nas posições contíguas (Norte, Sul, Este, Oeste) na jogada seguinte para descobrir a orientação e afundá-lo.
            3. Como os navios não se tocam, as posições diagonais a um tiro certeiro são água (exceto no corpo do Galeão). Evita atirar nas diagonais.
            4. Quando o relatório indicar que um navio foi afundado, deduz a sua posição no mapa e assume que todo o halo (1 posição em redor) é água intransitável.
            
            PROTOCOLO DE COMUNICAÇÃO (ESTRITAMENTE JSON):
            Vais gerar as tuas jogadas em rajadas de 3 tiros. Tens de responder EXCLUSIVAMENTE com um array JSON válido, sem qualquer texto adicional (sem blocos de código markdown como ```json, sem saudações).
            
            Formato exato obrigatório:
            [
            {"row": "A", "column": 5},
            {"row": "C", "column": 10},
            {"row": "F", "column": 5}
            ]
            
            Eu (o sistema) irei responder-te com um JSON contendo o resultado da tua rajada (validShots, sunkBoats, repeatedShots, outsideShots, hitsOnBoats, missedShots). Usa essa minha resposta para planear o teu próximo array JSON de ataque.
            """;

    // Agent related
    private final ChatLanguageModel agent;
    private final ChatMemory chatMemory;

    public AIPlayer() {
        Dotenv dotenv = Dotenv.load();

        String apiKey = dotenv.get("GOOGLE_API");

        this.agent = OpenAiChatModel.builder()
                .baseUrl(BASEURL)
                .apiKey(apiKey)
                .modelName(MODELNAME)
                .temperature(TEMPERATURE)
                .build();

        this.chatMemory = MessageWindowChatMemory.withMaxMessages(150);
        this.chatMemory.add(SystemMessage.systemMessage(SYSPROMPT));
    }

    /**
     * Recebe o resultado da última rajada efetuada e devolve a nova rajada
     * @param lastShotResult JSON com resultado da última rajada efetuada pela IA
     * @return JSON com a nova rajada da IA
     */
    public String nextShot(String lastShotResult) {
        if (lastShotResult == null || lastShotResult.isEmpty()) {
            chatMemory.add(UserMessage.userMessage("O jogo começou. Envia a tua primeira rajada."));
        } else {
            chatMemory.add(UserMessage.from(lastShotResult));
        }

        try {
            Response<AiMessage> response = agent.generate(chatMemory.messages());
            chatMemory.add(response.content());
            System.out.println(response.content().text());
            return response.content().text();
        } catch (Exception e) {
            System.err.println("Falha na comunicação com a IA (" + e.getMessage() + ").");
            return  null;
        }
    }

}
