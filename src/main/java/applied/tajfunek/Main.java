package applied.tajfunek;


import applied.tajfunek.alpaca.CryptoExchange;
import applied.tajfunek.alpaca.Exchange;
import applied.tajfunek.alpaca.exceptions.SymbolException;
import applied.tajfunek.strategy.Random;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.account.Account;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import net.jacobpeterson.alpaca.websocket.AlpacaWebsocket;
import net.jacobpeterson.alpaca.websocket.AlpacaWebsocketMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        CryptoExchange ex;
        try {
            ex = new CryptoExchange(new AlpacaAPI(), "BTC/USD");
        } catch (SymbolException e) {
            throw new RuntimeException(e);
        }
        try {
            Random r = new Random(ex, 30, ChronoUnit.SECONDS, 1,1,10, 1000.0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}