package applied.tajfunek.ui;

import net.jacobpeterson.alpaca.AlpacaAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class DefaultInterpreter implements Interpreter{

    private final AlpacaAPI api;
    private final Logger logger;
    public DefaultInterpreter(AlpacaAPI api) {
        this.logger = LoggerFactory.getLogger(DefaultInterpreter.class);
        this.api = api;
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

    private String printHelp() {
        return "";
    }

    private String orderAction(String[] command) {
        return "";
    }

    private String accountAction(String[] command) {
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
