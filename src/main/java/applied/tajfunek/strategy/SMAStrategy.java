package applied.tajfunek.strategy;

import applied.tajfunek.CONFIG;
import applied.tajfunek.alpaca.CryptoExchange;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.Bar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class SMAStrategy {
    private final CryptoExchange exchange;
    private final Logger log;
    private final int ma_len;

    public SMAStrategy(CryptoExchange exchange, int ma_len) {
        this.exchange = exchange;
        this.ma_len = ma_len;
        this.log = LoggerFactory.getLogger(SMAStrategy.class);
    }

    public void run(int timeInSeconds, int intervalLen, ChronoUnit intervalUnit) {
        log.atInfo()
                .setMessage("Starting strategy with average length parameter {}")
                .addArgument(ma_len)
                .log();
        var timeStart = Instant.now();
        double movingAverage = 0;
        int decision = 0;

        do {
            try {
                var data = exchange.getMarketData(ma_len, ZonedDateTime.now(), BarTimePeriod.DAY, 1, ChronoUnit.DAYS);

                if (data == null || data.size() != ma_len) {
                    /* Shouldn't happen without throwing AlpacaClientException */
                    log.atError()
                            .setMessage("Data length ({}) doesn't match desired length ({})")
                            .addArgument(data != null ? data.size() : "null")
                            .addArgument(ma_len)
                            .log();

                    throw new RuntimeException();
                }
                movingAverage = data.stream().mapToDouble(Bar::getClose).average().orElse(0);
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
            } catch (AlpacaClientException e) {
                // Only possible exception is SocketTimeout
                if (e.getCause().getClass() == SocketTimeoutException.class) {
                    reconnectionLoop(); // Returns if exception no longer occurs
                    continue; // Skip sleep to immediately read data again without waiting
                }
            }

            try {
                if (decision == 1) {
                    exchange.marketOrder(OrderSide.BUY, 0.1);
                } else if (decision == -1) {
                    exchange.marketOrder(OrderSide.SELL, 0.1);
                }
            } catch (AlpacaClientException e) {
                if (e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                    reconnectionLoop(); // Returns if exception no longer occurs
                    continue; // Skip sleep to immediately read data again without waiting
                } else if (e.getResponseStatusCode() != null && e.getResponseStatusCode() == 403) {
                    String msg = e.getAPIResponseMessage();
                    if (msg == null) {
                        log.atError().setMessage("403 FORBIDDEN, EMPTY API RESPONSE MESSAGE").log();
                        throw new RuntimeException();
                    }
                    double available = Double.parseDouble(msg.split(" ")[3].split("\\)")[0]);
                    try {}


                } else {
                    log.atError().setCause(e).log();
                }
            }


            try {
                log.atDebug().setMessage("Waiting.").log();
                Thread.sleep(intervalLen*intervalUnit.getDuration().toMillis());
            } catch (InterruptedException e) {
                log.atWarn()
                        .setMessage("Wait interrupted earlier than planned")
                        .log();
            }
        } while (Duration.between(timeStart, Instant.now()).getSeconds() < timeInSeconds);
    }

    private int decideBasedOnPrice(double movingAverage, Quote latestQuote) {
        return -1;
    }

    private void reconnectionLoop() {
        log.atError()
                .setMessage("Connection to server interrupted. Waiting for reconnection (60 seconds).")
                .log();

        var timeStart = Instant.now();
        while (Duration.between(timeStart, Instant.now()).getSeconds() < CONFIG.TIMEOUT) {
            try {
                Thread.sleep(60*ChronoUnit.SECONDS.getDuration().toMillis());
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
            } catch (AlpacaClientException e) {
                if (e.getCause().getClass() == SocketTimeoutException.class) {
                    log.atError()
                            .setMessage("Reconnection failed. Waiting 60 seconds.")
                            .log();
                }

                continue; // Skip return
            }

            return; // Returns if successfully reconnected
        }
        log.atError().setMessage("Failed to reconnect. Timeout: {} seconds").addArgument(CONFIG.TIMEOUT).log();
        throw new RuntimeException("Failed to reconnect");
    }
}
