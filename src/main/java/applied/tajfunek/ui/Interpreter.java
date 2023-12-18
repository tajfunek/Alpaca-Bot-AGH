package applied.tajfunek.ui;

import net.jacobpeterson.alpaca.AlpacaAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

class Interpreter {

    private final AlpacaAPI api;
    private final Logger logger;
    public Interpreter(AlpacaAPI api) {
        this.logger = LoggerFactory.getLogger(Interpreter.class);
        this.api = api;
    }

    public String interpret(String[] command) {
        logger.atInfo().setMessage("Intercepted command: {}").addArgument(String.join(" ", command)).log();
        if(command.length == 0) {
            return System.lineSeparator();
        }
        switch (command[0]) {
            case "account" -> {
                StringBuilder builder = new StringBuilder();
                String[] arguments = (String[]) Arrays.stream(command).skip(1).toArray();
                if(arguments.length == 0) {
                    builder.append("Missing arguments. See \"help\". Defaulting to account summary.");
                    builder.append(System.lineSeparator());
                    arguments = new String[]{"summary"};
                }
                builder.append(accountAction(arguments));
                builder.append(System.lineSeparator());

                return builder.toString();
            }

            default -> {
                return "Unknown command." + System.lineSeparator();
            }
        }
    }

    private String accountAction(String[] arguments) {
        return "summary";
    }
}
