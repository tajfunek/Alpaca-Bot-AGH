package applied.tajfunek;


import applied.tajfunek.alpaca.CryptoExchange;
import applied.tajfunek.alpaca.Exchange;
import applied.tajfunek.alpaca.exceptions.SymbolException;
import applied.tajfunek.strategy.Random;
import applied.tajfunek.strategy.SMAStrategy;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.account.Account;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import net.jacobpeterson.alpaca.websocket.AlpacaWebsocket;
import net.jacobpeterson.alpaca.websocket.AlpacaWebsocketMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;


public class Main {

    public static void main(String[] args) {
        CryptoExchange ex;
        Logger logger = LoggerFactory.getLogger("applied.tajfunek.Main");
        double startAmount;
        AlpacaAPI api;
        try {
            api = new AlpacaAPI();
            api.positions().closeAll(Boolean.TRUE);
            startAmount = Double.parseDouble(api.account().get().getCash());
            ex = new CryptoExchange(api, "BTC/USD");

        } catch (SymbolException | AlpacaClientException e) {
            throw new RuntimeException(e);
        }
        SMAStrategy str = new SMAStrategy(ex, 20);
        str.run(120, 30, ChronoUnit.SECONDS);
    }
}