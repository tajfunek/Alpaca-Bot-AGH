package applied.tajfunek.ui;

import net.jacobpeterson.alpaca.AlpacaAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class DefaultInterpreterTest {
    DefaultInterpreter interpreter;

    @BeforeEach
    void setUp() {
    }
    @Test
    @DisplayName("Test interpretation of empty command")
    void interpretTestEmptyCommand() {
        assertEquals("",interpreter.interpret(new String[]{}).trim());
    }

    @Test
    void interpretTest() {
        DefaultInterpreter interpreter = Mockito.mock(DefaultInterpreter.class);
        Mockito.when(interpreter.interpret(Mockito.any())).thenCallRealMethod();
        Mockito.when(interpreter.accountAction(new String[]{"account", "summary"})).thenReturn("Account Test");
        Mockito.when(interpreter.orderAction(new String[]{"order"})).thenReturn("Order Test");
        Mockito.when(interpreter.printHelp()).thenReturn("Help test");

        assertEquals("Order Test", interpreter.interpret(new String[]{"account", "summary"}).trim());
        assertEquals("Account Test", interpreter.interpret(new String[]{"order"}).trim());
        assertEquals("Help Test", interpreter.interpret(new String[]{"help"}).trim());

    }
}