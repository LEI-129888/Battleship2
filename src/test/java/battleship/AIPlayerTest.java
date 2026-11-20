package battleship;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class AIPlayer.
 * Author: 129867
 * Date: 2026-11-18 18:10
 * Cyclomatic Complexity:
 * - constructor: 1
 * - nextShot(): 4
 */
class AIPlayerTest {

    private AIPlayer aiPlayer;
    private static final java.nio.file.Path ENV_PATH = java.nio.file.Path.of(".env");
    private static boolean createdTempEnv = false;

    @org.junit.jupiter.api.BeforeAll
    static void ensureEnvFileExists() throws java.io.IOException {
        if (!java.nio.file.Files.exists(ENV_PATH)) {
            java.nio.file.Files.writeString(ENV_PATH, "GOOGLE_API=dummy-test-key\n");
            createdTempEnv = true;
        }
    }

    @org.junit.jupiter.api.AfterAll
    static void cleanUpTempEnvFile() throws java.io.IOException {
        if (createdTempEnv) {
            java.nio.file.Files.deleteIfExists(ENV_PATH);
        }
    }

    @BeforeEach
    void setUp() {
        aiPlayer = new AIPlayer();
    }

    @AfterEach
    void tearDown() {
        aiPlayer = null;
    }

    /**
     * Helper method to inject a fake ChatLanguageModel into AIPlayer
     * so tests run offline, deterministically, and cover both try and catch paths.
     */
    private void setMockAgent(ChatLanguageModel mockAgent) throws Exception {
        Field agentField = AIPlayer.class.getDeclaredField("agent");
        agentField.setAccessible(true);
        agentField.set(aiPlayer, mockAgent);
    }

    @Test
    @DisplayName("Test AIPlayer constructor initializes instance, agent, and chatMemory")
    void constructor() throws Exception {
        Field agentField = AIPlayer.class.getDeclaredField("agent");
        agentField.setAccessible(true);
        Field memoryField = AIPlayer.class.getDeclaredField("chatMemory");
        memoryField.setAccessible(true);

        assertAll("AIPlayer constructor initialization",
                () -> assertNotNull(aiPlayer, "Error: expected AIPlayer instance not to be null"),
                () -> assertNotNull(agentField.get(aiPlayer), "Error: expected agent field to be initialized"),
                () -> assertNotNull(memoryField.get(aiPlayer), "Error: expected chatMemory field to be initialized")
        );
    }

    @Test
    @DisplayName("Test nextShot with null lastShotResult returns AI response")
    void nextShot1() throws Exception {
        String expectedJson = "[{\"row\": \"A\", \"column\": 1}]";
        setMockAgent(messages -> Response.from(AiMessage.from(expectedJson)));

        String actual = aiPlayer.nextShot(null);

        assertAll("nextShot with null input",
                () -> assertNotNull(actual, "Error: expected non-null JSON response when lastShotResult is null"),
                () -> assertEquals(expectedJson, actual, "Error: expected " + expectedJson + " but got " + actual)
        );
    }

    @Test
    @DisplayName("Test nextShot with empty lastShotResult returns AI response")
    void nextShot2() throws Exception {
        String expectedJson = "[{\"row\": \"B\", \"column\": 2}]";
        setMockAgent(messages -> Response.from(AiMessage.from(expectedJson)));

        String actual = aiPlayer.nextShot("");

        assertAll("nextShot with empty string input",
                () -> assertNotNull(actual, "Error: expected non-null JSON response when lastShotResult is empty"),
                () -> assertEquals(expectedJson, actual, "Error: expected " + expectedJson + " but got " + actual)
        );
    }

    @Test
    @DisplayName("Test nextShot with valid non-empty lastShotResult returns AI response")
    void nextShot3() throws Exception {
        String lastShotResult = "{\"validShots\": 3, \"hitsOnBoats\": 1}";
        String expectedJson = "[{\"row\": \"C\", \"column\": 3}]";
        setMockAgent(messages -> Response.from(AiMessage.from(expectedJson)));

        String actual = aiPlayer.nextShot(lastShotResult);

        assertAll("nextShot with non-empty JSON input",
                () -> assertNotNull(actual, "Error: expected non-null JSON response for valid lastShotResult"),
                () -> assertEquals(expectedJson, actual, "Error: expected " + expectedJson + " but got " + actual)
        );
    }

    @Test
    @DisplayName("Test nextShot returns null when AI agent throws an exception")
    void nextShot4() throws Exception {
        setMockAgent(messages -> {
            throw new RuntimeException("Simulated API failure");
        });

        String actual = aiPlayer.nextShot("{\"validShots\": 3}");

        assertNull(actual, "Error: expected null when agent throws an exception, but got " + actual);
    }
}