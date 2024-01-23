package applied.tajfunek.strategy;

import applied.tajfunek.alpaca.Exchange;
import applied.tajfunek.alpaca.MyAccount;
import applied.tajfunek.alpaca.exceptions.ConnectionException;
import applied.tajfunek.alpaca.exceptions.ExchangeException;
import applied.tajfunek.alpaca.exceptions.InsufficientException;
import applied.tajfunek.alpaca.exceptions.UnprocessableRequestException;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.Bar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce;
import net.jacobpeterson.alpaca.model.endpoint.positions.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MeanReversal {
    private final Exchange exchange;
    private final Logger log;
    private final int ma_len;
    private final int timeout;
    private final boolean noShorting;
    private final MyAccount account;

    enum Decision {
        BUY,
        SELL,
        WAIT
    }

    public MeanReversal(Exchange exchange, MyAccount myAccount, int ma_len, int timeout, boolean noShorting) {
        this.exchange = exchange;
        this.ma_len = ma_len;
        this.log = LoggerFactory.getLogger(MeanReversal.class);
        this.timeout = timeout;
        this.noShorting = noShorting;
        this.account  = myAccount;
    }

    public void run(int timeInSeconds, int intervalLen, ChronoUnit intervalUnit) {
        log.atInfo()
                .setMessage("Starting strategy with average length parameter {}")
                .addArgument(ma_len)
                .log();
        var timeStart = Instant.now();

        List<Order> orders = new java.util.ArrayList<>(List.of());


        do {
            // First we check if we have a position open
            Position pos = account.getPosition(exchange.getAsset())
            if (!invested) {
                Decision decision = Decision.WAIT;
                try {
                    var data = exchange.getMarketData(ma_len, ZonedDateTime.now(), BarTimePeriod.DAY, 1, ChronoUnit.DAYS);

                    if (data == null || data.size() != ma_len) {
                        /* Shouldn't happen without throwing Exception */
                        log.atError()
                                .setMessage("Data length ({}) doesn't match desired length ({}). Requested wrong times")
                                .addArgument(data != null ? data.size() : "null")
                                .addArgument(ma_len)
                                .log();

                        throw new RuntimeException();
                    }

                    var movingAverage = data.stream().mapToDouble(Bar::getClose).average().orElse(0);
                    if (movingAverage == 0) {
                        log.atError()
                                .setMessage("Calculated moving average is 0")
                                .log();

                        throw new RuntimeException();
                    }
                    log.atDebug()
                            .setMessage("Calculated moving average is {}")
                            .addArgument(movingAverage)
                            .log();

                    Quote quote = exchange.getLatestQuote();
                    decision = decideBasedOnPrice(movingAverage, quote);
                } catch (ExchangeException e) {
                    // Only possible exception is SocketTimeout, anything else is not possible
                    if (e.getClass() == ConnectionException.class) {
                        reconnectionLoop(); // Returns if exception no longer occurs
                        continue; // Skip sleep to immediately read data again without waiting
                    }
                }

                try {
                    if (decision == Decision.BUY) {
                        Quote quote = exchange.getLatestQuote();
                        double cash = account.getCash();
                        double limit = quote.getAskPrice();
                        double quantity = cash/limit;

                        orders.add(exchange.limitOrder(OrderSide.BUY, quantity,limit, OrderTimeInForce.DAY));
                        orders.add(exchange.stopLimitOrder(OrderSide.SELL, quantity, 0.93*quote.getBidPrice(),
                                0.95 * quote.getBidPrice(), OrderTimeInForce.GOOD_UNTIL_CANCELLED));
                        invested = true;
                    }
                } catch (ExchangeException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // We open an order so we wait until price rises and sell

            }

            try {
                log.atDebug().setMessage("Waiting.").log();
                Thread.sleep(intervalLen * intervalUnit.getDuration().toMillis());
            } catch (InterruptedException e) {
                log.atWarn()
                        .setMessage("Wait interrupted earlier than planned")
                        .log();
            }
        } while (Duration.between(timeStart, Instant.now()).getSeconds() < timeInSeconds);
    }


    private Decision decideBasedOnPrice(double movingAverage, Quote latestQuote) {
        var price = (latestQuote.getAskPrice() + latestQuote.getBidPrice())/2;

        if(movingAverage - price > 0.03*price) {
            return Decision.BUY;
        } else if (price - movingAverage >0.03*price) {
            return Decision.SELL;
        } else return Decision.WAIT;
    }


    private void reconnectionLoop() {
        log.atError()
                .setMessage("Connection to server interrupted. Waiting for reconnection (60 seconds).")
                .log();

        var timeStart = Instant.now();
        while (Duration.between(timeStart, Instant.now()).getSeconds() < timeout) {
            try {
                Thread.sleep(60 * ChronoUnit.SECONDS.getDuration().toMillis());
            } catch (InterruptedException e) {
                log.atError()
                        .setMessage("Thread interrupted earlier. Trying to reconnect now.")
                        .log();
            }

            log.atInfo()
                    .setMessage("Trying to reconnect")
                    .log();

            try {
                exchange.isOpen();
            } catch (ExchangeException e) {
                if (e.getClass() == ConnectionException.class) {
                    log.atError()
                            .setMessage("Reconnection failed. Waiting 60 seconds.")
                            .log();
                }

                continue; // Skip return
            }

            return; // Returns if successfully reconnected
        }
        log.atError().setMessage("Failed to reconnect. Timeout: {} seconds").addArgument(timeout).log();
        throw new RuntimeException("Failed to reconnect");
    }
}
