package applied.tajfunek;


import applied.tajfunek.alpaca.CryptoExchange;
import applied.tajfunek.alpaca.MyAccount;
import applied.tajfunek.alpaca.exceptions.SymbolException;
import applied.tajfunek.strategy.MeanReversal;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;


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
        MeanReversal str = new MeanReversal(ex, new MyAccount(api), 20, 120, false);
        str.run(120, 30, ChronoUnit.SECONDS);
    }
}