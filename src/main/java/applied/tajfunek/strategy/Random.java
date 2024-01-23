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
        log.atInfo().setMessage("Starting Random trading strategy").log();
        try {
            quantity = startPrice/(exchange.getLatestQuote().getAskPrice()+exchange.getLatestQuote().getBidPrice())/2;
        } catch (AlpacaClientException e) {
            log.atError().setCause(e).log();
            return;
        }
        log.atInfo().
                setMessage("Trading {}. Calculated quantity for one trade {}.")
                .addArgument(exchange.getSymbol())
                .addArgument(quantity)
                .log();


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

                    if(e.getAPIResponseCode() != null && e.getAPIResponseCode() == 40310000) {
                        var message = e.getAPIResponseMessage();
                        double available;
                        String symbol;
                        if(message != null) {
                            available = Double.parseDouble(message.split(" ")[7].split("\\)")[0]);
                            symbol = message.split(" ")[3];
                            if(available < 1e-12) {
                                log.atInfo()
                                        .setMessage("Amount of currency owned lower than 1e-12. Sell skipped").log();
                            } else {
                                log.atInfo().
                                        setMessage("Previous sell failed due to insufficient amount of currency to sell. " +
                                                "Selling all available, {} {}")
                                        .addArgument(available).addArgument(symbol)
                                        .log();
                                try {
                                    exchange.marketOrder(OrderSide.SELL, available);
                                } catch (AlpacaClientException ex) {
                                    log.atError().setCause(e).log();
                                    throw new RuntimeException(ex);
                                }
                            }
                        } else {
                            log.atError().setMessage("Empty response code. Interrupting").log();
                            throw new RuntimeException(e);
                        }
                    } else throw new RuntimeException(e);
                }
            }

            no_trades++;
            Thread.sleep(Duration.of(interval, unit));
        }
        try {
            exchange.cancelAll();
        } catch (AlpacaClientException e) {
            log.atError().setCause(e).log();
        }
    }
}
