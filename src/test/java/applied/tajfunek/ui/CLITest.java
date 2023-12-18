package applied.tajfunek.ui;

import net.jacobpeterson.alpaca.AlpacaAPI;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;


class CLITest {
    PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }
    @Test
    void InitializationTest() {
        try (CLI cli = new CLI(new AlpacaAPI())) {
        } catch (Exception e) {
            fail();
        }
        Scanner scan = new Scanner(System.in);
        assertEquals("Welcome!", outputStreamCaptor.toString().trim());
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }
}