package applied.tajfunek;

import applied.tajfunek.ui.CLI;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.account.Account;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        AlpacaAPI api = new AlpacaAPI();
        try(CLI cli = new CLI(api)) {}
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}