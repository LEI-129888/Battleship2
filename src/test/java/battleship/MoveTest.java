package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Move.
 * Author: alexa
 * Date: 2026-11-17 22:50
 * Cyclomatic Complexity:
 * - Move(int, List, List): 1
 * - Move(int, List): 1
 * - toString(): 1
 * - getNumber(): 1
 * - getShots(): 1
 * - getShotResults(): 1
 * - processEnemyFire(boolean): 38
 */
class MoveTest {

    private Move move;

    @BeforeEach
    void setUp() {
        move = new Move(1, new ArrayList<>(), new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        move = null;
    }

    @Test
    @DisplayName("O construtor com resultados guarda todos os argumentos")
    void constructor() {
        List<IPosition> shots = List.of(new Position(0, 0));
        List<IGame.ShotResult> results = List.of(new IGame.ShotResult(true, false, null, false));
        move = new Move(4, shots, results);

        assertAll(
                () -> assertEquals(4, move.getNumber(),
                        "Error: expected the constructor to store move number 4."),
                () -> assertSame(shots, move.getShots(),
                        "Error: expected the constructor to retain the supplied shots list."),
                () -> assertSame(results, move.getShotResults(),
                        "Error: expected the constructor to retain the supplied results list.")
        );
    }

    @Test
    @DisplayName("O construtor de desserialização inicializa a lista de resultados")
    void constructor1() {
        List<IPosition> shots = List.of(new Position(1, 2));
        move = new Move(7, shots);

        assertAll(
                () -> assertEquals(7, move.getNumber(),
                        "Error: expected the deserialization constructor to store move number 7."),
                () -> assertSame(shots, move.getShots(),
                        "Error: expected the deserialization constructor to retain the supplied shots."),
                () -> assertNotNull(move.getShotResults(),
                        "Error: expected the deserialization constructor to initialize shot results."),
                () -> assertTrue(move.getShotResults().isEmpty(),
                        "Error: expected deserialized moves to start without shot results.")
        );
    }

    @Test
    @DisplayName("toString inclui número, tiros e resultados")
    void toStringTest() {
        move = new Move(3, List.of(new Position(0, 0), new Position(0, 1)),
                List.of(new IGame.ShotResult(true, false, null, false)));

        assertEquals("Move{number=3, shots=2, results=1}", move.toString(),
                "Error: expected toString to report the move number and collection sizes.");
    }

    @Test
    @DisplayName("getNumber devolve o número da jogada")
    void getNumber() {
        move = new Move(9, List.of(), List.of());

        assertEquals(9, move.getNumber(),
                "Error: expected getNumber to return the number supplied to the constructor.");
    }

    @Test
    @DisplayName("getShots devolve a lista de tiros")
    void getShots() {
        List<IPosition> shots = List.of(new Position(2, 4));
        move = new Move(1, shots, List.of());

        assertSame(shots, move.getShots(),
                "Error: expected getShots to return the original shots list.");
    }

    @Test
    @DisplayName("getShotResults devolve a lista de resultados")
    void getShotResults() {
        List<IGame.ShotResult> results = List.of(new IGame.ShotResult(true, false, null, false));
        move = new Move(1, List.of(), results);

        assertSame(results, move.getShotResults(),
                "Error: expected getShotResults to return the original results list.");
    }

    @Test
    @DisplayName("processEnemyFire ignora resultados inválidos")
    void processEnemyFire1() throws Exception {
        move = new Move(2, List.of(), List.of(
                new IGame.ShotResult(false, false, null, false)));

        JsonNode response = json(move.processEnemyFire(false));

        assertAll(
                () -> assertEquals(0, response.get("validShots").asInt(),
                        "Error: expected invalid shots not to count as valid."),
                () -> assertEquals(0, response.get("repeatedShots").asInt(),
                        "Error: expected invalid shots not to count as repeated."),
                () -> assertEquals(Game.NUMBER_SHOTS, response.get("outsideShots").asInt(),
                        "Error: expected an ignored invalid result to leave all shots outside.")
        );
    }

    @Test
    @DisplayName("processEnemyFire conta um tiro repetido")
    void processEnemyFire2() throws Exception {
        move = new Move(2, List.of(), List.of(
                new IGame.ShotResult(true, true, null, false)));

        JsonNode response = json(move.processEnemyFire(false));

        assertAll(
                () -> assertEquals(1, response.get("repeatedShots").asInt(),
                        "Error: expected the repeated shot to be counted."),
                () -> assertEquals(0, response.get("validShots").asInt(),
                        "Error: expected repeated shots not to count as valid shots."),
                () -> assertEquals(Game.NUMBER_SHOTS - 1, response.get("outsideShots").asInt(),
                        "Error: expected outside shots to exclude the repeated shot.")
        );
    }

    @Test
    @DisplayName("processEnemyFire conta um tiro válido na água")
    void processEnemyFire3() throws Exception {
        move = new Move(2, List.of(), List.of(
                new IGame.ShotResult(true, false, null, false)));

        JsonNode response = json(move.processEnemyFire(false));

        assertAll(
                () -> assertEquals(1, response.get("validShots").asInt(),
                        "Error: expected the valid shot to be counted."),
                () -> assertEquals(1, response.get("missedShots").asInt(),
                        "Error: expected a valid shot without a ship to count as a miss."),
                () -> assertEquals(Game.NUMBER_SHOTS - 1, response.get("outsideShots").asInt(),
                        "Error: expected outside shots to exclude the valid shot.")
        );
    }

    @Test
    @DisplayName("processEnemyFire conta acertos e agrupa barcos afundados e não afundados")
    void processEnemyFire4() throws Exception {
        IShip afloatShip = ship("Fragata");
        IShip sunkShip = ship("Barca");
        move = new Move(2, List.of(), List.of(
                new IGame.ShotResult(true, false, afloatShip, false),
                new IGame.ShotResult(true, false, afloatShip, false),
                new IGame.ShotResult(true, false, sunkShip, true)));

        JsonNode response = json(move.processEnemyFire(false));

        assertAll(
                () -> assertEquals(3, response.get("validShots").asInt(),
                        "Error: expected all three non-repeated shots to count as valid."),
                () -> assertEquals(0, response.get("missedShots").asInt(),
                        "Error: expected ship hits not to count as misses."),
                () -> assertEquals(1, response.get("sunkBoats").size(),
                        "Error: expected one boat type to be recorded as sunk."),
                () -> assertEquals("Barca", response.get("sunkBoats").get(0).get("type").asText(),
                        "Error: expected the sunk boat category to be preserved."),
                () -> assertEquals(1, response.get("hitsOnBoats").size(),
                        "Error: expected non-sunk hits to be reported separately."),
                () -> assertEquals("Fragata", response.get("hitsOnBoats").get(0).get("type").asText(),
                        "Error: expected the afloat ship's category to appear in hitsOnBoats."),
                () -> assertEquals(2, response.get("hitsOnBoats").get(0).get("hits").asInt(),
                        "Error: expected both hits on the afloat ship to be counted.")
        );
    }

    @Test
    @DisplayName("processEnemyFire mantém o mesmo tipo de barco fora dos acertos quando afundado")
    void processEnemyFire5() throws Exception {
        IShip ship = ship("Barca");
        move = new Move(2, List.of(), List.of(
                new IGame.ShotResult(true, false, ship, false),
                new IGame.ShotResult(true, false, ship, true)));

        JsonNode response = json(move.processEnemyFire(false));

        assertAll(
                () -> assertEquals(1, response.get("sunkBoats").size(),
                        "Error: expected the sunk category to appear in sunkBoats."),
                () -> assertTrue(response.get("hitsOnBoats").isEmpty(),
                        "Error: expected a sunk category not to appear in hitsOnBoats.")
        );
    }

    @Test
    @DisplayName("processEnemyFire serializa listas vazias quando não há resultados")
    void processEnemyFire6() throws Exception {
        move = new Move(2, List.of(), List.of());

        JsonNode response = json(move.processEnemyFire(false));

        assertAll(
                () -> assertTrue(response.get("sunkBoats").isEmpty(),
                        "Error: expected no sunk boats when there are no shot results."),
                () -> assertTrue(response.get("hitsOnBoats").isEmpty(),
                        "Error: expected no boat hits when there are no shot results."),
                () -> assertEquals(Game.NUMBER_SHOTS, response.get("outsideShots").asInt(),
                        "Error: expected all shots to be outside when there are no results.")
        );
    }

    @Test
    @DisplayName("processEnemyFire rejeita resultados nulos")
    void processEnemyFire7() {
        move = new Move(2, List.of(), null);

        assertThrows(NullPointerException.class, () -> move.processEnemyFire(false),
                "Error: expected a null shot result list to throw NullPointerException.");
    }

    @Test
    @DisplayName("processEnemyFire verbose descreve apenas tiros repetidos e tiros exteriores")
    void processEnemyFire8() throws Exception {
        move = new Move(5, List.of(), List.of(
                new IGame.ShotResult(true, true, null, false)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("Jogada nº5"),
                        "Error: expected verbose output to include the move number."),
                () -> assertTrue(text.contains("1 tiro repetido"),
                        "Error: expected a lone repeated shot to use singular wording."),
                () -> assertTrue(text.contains((Game.NUMBER_SHOTS - 1) + " tiros exteriores"),
                        "Error: expected remaining outside shots to be printed in plural."),
                () -> assertTrue(text.contains("\"repeatedShots\""),
                        "Error: expected verbose output to include the serialized JSON.")
        );
    }

    @Test
    @DisplayName("processEnemyFire verbose formata um resumo misto de tiros")
    void processEnemyFire9() throws Exception {
        move = new Move(6, List.of(), List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, true, null, false)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("2 tiros válidos"),
                        "Error: expected multiple valid shots to use plural wording."),
                () -> assertTrue(text.contains("2 tiros na água"),
                        "Error: expected multiple misses to use plural wording."),
                () -> assertTrue(text.contains(", 1 tiro repetido"),
                        "Error: expected repeated shots to follow valid shots with a comma.")
        );
    }

    @Test
    @DisplayName("processEnemyFire verbose trata corretamente um resultado sem resumo")
    void processEnemyFire10() throws Exception {
        move = new Move(7, List.of(), List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).contains(Game.NUMBER_SHOTS + " tiros exteriores"),
                "Error: expected an empty summary to print the outside-shot count without a leading comma.");
    }

    @Test
    @DisplayName("processEnemyFire verbose descreve barcos afundados, atingidos e tiros na água")
    void processEnemyFireVerboseComNavios() {
        IShip sunkShip = ship("FragataAfundada");
        IShip hitShip = ship("CaravelaAtingida");
        move = new Move(8, List.of(), List.of(
                new IGame.ShotResult(true, false, sunkShip, true),
                new IGame.ShotResult(true, false, hitShip, false),
                new IGame.ShotResult(true, false, null, false)));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("FragataAfundada"),
                        "Error: expected verbose output to include the sunk ship category."),
                () -> assertTrue(text.contains("CaravelaAtingida"),
                        "Error: expected verbose output to include the hit ship category."),
                () -> assertTrue(text.contains("1 tiro na água"),
                        "Error: expected verbose output to describe the shot that missed.")
        );
    }

    @Test
    @DisplayName("processEnemyFire verbose formata tiros exteriores sem resultados")
    void processEnemyFireVerboseSemBarcos() {
        move = new Move(9, List.of(), List.of());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("-> " + Game.NUMBER_SHOTS + " tiros exteriores"),
                "Error: expected empty results to render only the outside-shot count.");
    }
    @Test
    @DisplayName("processEnemyFire verbose formata plurais de múltiplos barcos afundados/atingidos sem tiros na água")
    void processEnemyFireVerbosePluraisESemAgua() throws Exception {
        //Simula a situação onde a string "+ " no final tem de ser removida (porque missedShots == 0)
        // e garante a passagem no "count > 1 ? s : "
        IShip barca = ship("Barca");
        IShip nau = ship("Nau");
        move = new Move(10, List.of(), List.of(
                new IGame.ShotResult(true, false, barca, true),
                new IGame.ShotResult(true, false, barca, true),
                new IGame.ShotResult(true, false, nau, false),
                new IGame.ShotResult(true, false, nau, false)
        ));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("2 Barcas ao fundo"),
                        "Error: expected verbose output to correctly pluralize multiple sunk boats of the same type."),
                () -> assertTrue(text.contains("2 tiros num(a) Nau"),
                        "Error: expected verbose output to correctly pluralize multiple hits on the same type of boat."),
                () -> assertFalse(text.contains("+ \r\n") || text.contains("+ \n"),
                        "Error: expected the trailing '+ ' to be removed when there are no missed shots, leaving a clean line break.")
        );
    }

    @Test
    @DisplayName("processEnemyFire verbose formata vários tiros repetidos no plural e 1 tiro exterior no singular")
    void processEnemyFireVerboseVariosRepetidosEUmExterior() {
        // 0 válidos, 2 repetidos -> sobra 1 tiro exterior (assumindo NUMBER_SHOTS = 3)
        move = new Move(11, List.of(), List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        ));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("2 tiros repetidos"),
                        "Error: expected multiple repeated shots to use plural wording."),
                () -> assertTrue(text.contains("1 tiro exterior"),
                        "Error: expected a single outside shot to use singular wording.")
        );
    }

    @Test
    @DisplayName("processEnemyFire verbose formata 1 tiro válido num barco sem afundar e vários repetidos")
    void processEnemyFireVerboseUmValidoSemAfundarEVariosRepetidos() {
        // 1 válido (acerto sem afundar, 0 água, 0 afundados) + 2 repetidos
        IShip hitShip = ship("Galeão");
        move = new Move(12, List.of(), List.of(
                new IGame.ShotResult(true, false, hitShip, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        ));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            move.processEnemyFire(true);
        } finally {
            System.setOut(originalOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("1 tiro válido"),
                        "Error: expected a single valid shot to use singular wording."),
                () -> assertTrue(text.contains("1 tiro num(a) Galeão"),
                        "Error: expected hit on non-sunk ship to be printed."),
                () -> assertTrue(text.contains(", 2 tiros repetidos"),
                        "Error: expected multiple repeated shots after valid shots to use plural wording.")
        );
    }

    private static JsonNode json(String text) throws Exception {
        return new ObjectMapper().readTree(text);
    }

    private static IShip ship(String category) {
        return (IShip) java.lang.reflect.Proxy.newProxyInstance(
                IShip.class.getClassLoader(),
                new Class<?>[]{IShip.class},
                (proxy, method, args) -> method.getName().equals("getCategory") ? category : null
        );
    }
}
