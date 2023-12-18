package applied.tajfunek.ui;

import net.jacobpeterson.alpaca.AlpacaAPI;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CLITest {
    PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    Interpreter interpreter;
    private CLI cli;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }
    @Test
    void InitializationTest() {
        interpreter = Mockito.mock(DefaultInterpreter.class);
        Mockito.when(interpreter.interpret(new String[]{"account","summary"})).thenReturn("Welcome message");

        try {
            cli = new CLI(interpreter);
        } catch (Exception e) {
            fail(e);
        }

        assertEquals("Welcome message", outputStreamCaptor.toString().trim());

    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }
}