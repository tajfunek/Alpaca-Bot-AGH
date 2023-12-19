package applied.tajfunek.ui;

import net.jacobpeterson.alpaca.AlpacaAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class DefaultInterpreter implements Interpreter{
    private final Logger logger;
    public DefaultInterpreter() throws Exception{
        this.logger = LoggerFactory.getLogger(this.getClass());
        if (this.logger == null) {
            throw new Exception("Failed to generate logger");
        }
        logger.atInfo().setMessage("Successfully created interpreter").log();
    }

    public String interpret(String[] command) {
        logger.atInfo().setMessage("Intercepted command: {}").addArgument(String.join(" ", command)).log();
        if(command.length == 0) {
            return System.lineSeparator();
        }
        switch (command[0]) {
            case "account" -> {
                return accountAction(command);
            }
            case "order" -> {
                return orderAction(command);
            }
            case "help" -> {
                return printHelp();
            }
            default -> {
                return "Unknown command. See \"help\"" + System.lineSeparator();
            }
        }
    }

    String printHelp() {
        return "";
    }

    String orderAction(String[] command) {
        return "";
    }

    String accountAction(String[] command) {
        StringBuilder builder = new StringBuilder();
        String[] arguments;
        if (command.length == 1) {
            builder.append("Missing arguments. See \"help\". Defaulting to account summary.");
            builder.append(System.lineSeparator());
            arguments = new String[]{"summary"};
        } else {
            arguments = Arrays.copyOfRange(command,1, command.length);
        }

        return builder.toString();
    }
}
