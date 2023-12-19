package applied.tajfunek;

import applied.tajfunek.ui.*;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.account.Account;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger("applied.tajfunek");
        if (logger == null) throw new RuntimeException("Cannot create a logger");

        Interpreter interpreter;
        try {
            interpreter = new DefaultInterpreter();
        } catch (Exception e) {
            logger.atError().setCause(e).log();
            throw new RuntimeException("Cannot create a logger");
        }

        try (CLI cli = new CLI(interpreter)) {
        } catch (Exception e) {
            logger.atError().setCause(e).log();
            throw new RuntimeException(e);
        }
    }
}