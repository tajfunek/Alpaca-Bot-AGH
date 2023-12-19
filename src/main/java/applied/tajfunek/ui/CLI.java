package applied.tajfunek.ui;

import net.jacobpeterson.alpaca.AlpacaAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Scanner;

public class CLI implements Closeable{
    private final Logger logger;
    private final Writer consoleWriter;
    private final Scanner consoleScanner;
    private final Interpreter interpreter;

    public CLI(Interpreter interpreter) throws Exception {
        this.logger = LoggerFactory.getLogger(CLI.class);
        if (logger == null) throw new Exception("Failed to create a logger in CLI");

        this.interpreter = interpreter;

        this.consoleScanner = new Scanner(System.in);
        this.consoleWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        logger.atInfo().setMessage("Initialized CLI object").log();

        welcomeMessage();
    }

    private void welcomeMessage() throws IOException {
        String welcome = interpreter.interpret(new String[]{"account", "summary"});
        try {
            consoleWriter.write(welcome);
            consoleWriter.flush();
        } catch (IOException e) {
            logger.atError().setMessage("Failed to write to console").log();
            throw new IOException();
        }
    }

    void waitForCommand() {
    }

    @Override
    public void close() throws IOException {
        this.consoleWriter.close();
        this.consoleScanner.close();
        logger.atInfo().log("Closed CLI interface.");
    }
}

;
