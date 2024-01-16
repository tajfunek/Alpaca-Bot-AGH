package applied.tajfunek.strategy;

import applied.tajfunek.alpaca.CryptoExchange;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class Random {
    public Random(CryptoExchange exchange, int interval, ChronoUnit unit, int buy, int sell, int trades_limit, Double startPrice) throws InterruptedException {
        Logger log = LoggerFactory.getLogger(Random.class);

        RandomGenerator gen = new java.util.Random();
        double quantity = 0;
        try {
            quantity = startPrice/(exchange.getLatestQuote().getAskPrice()+exchange.getLatestQuote().getBidPrice())/2;
        } catch (AlpacaClientException e) {
            log.atError().setCause(e).log();
            return;
        }

        int no_trades = 0;
        while(no_trades < trades_limit) {
            var rnum = gen.nextInt(0, buy+sell);
            if(rnum < buy) {
                try {
                    exchange.marketOrder(OrderSide.BUY, quantity);
                } catch (AlpacaClientException e) {
                    log.atError().setCause(e).log();
                    return;
                }
            } else {
                try {
                    exchange.marketOrder(OrderSide.SELL, quantity);
                } catch (AlpacaClientException e) {
                    log.atError().setCause(e).log();
                    return;
                }
            }

            no_trades++;
            Thread.sleep(Duration.of(interval, unit));
        }
        try {
            exchange.cancelAll();
        } catch (AlpacaClientException e) {
            log.atError().setCause(e).log();
            return;
        }
    }
}
