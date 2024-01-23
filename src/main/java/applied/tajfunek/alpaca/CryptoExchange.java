package applied.tajfunek.alpaca;

import applied.tajfunek.alpaca.exceptions.*;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.Bar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderType;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.List;

public class CryptoExchange implements Exchange {

    private final String symbol;
    private final AlpacaAPI api;
    private final Logger logger;

    public CryptoExchange(AlpacaAPI api, String symbol) throws SymbolException {
        this.symbol = symbol;
        this.api = api;
        this.logger = LoggerFactory.getLogger(Exchange.class);

        try {
            api.assets().getBySymbol(String.join("", symbol.split("/")));
        } catch (AlpacaClientException e) {
            logger.atError().setCause(e).log();
            throw new SymbolException();
        }
    }

    public boolean isOpen() throws ExchangeException {
        // You can always make trades on crypto
        try {
            return api.clock().get().getIsOpen();
        } catch (AlpacaClientException e) {
            if (e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                throw new ConnectionException(e);
            } else {
                // API doesn't specify other errors
                logger.atError().setCause(e).log();
                throw new RuntimeException(e);
            }
        }
    }

    public Quote getLatestQuote() throws ExchangeException {
        try {
            logger.atDebug().setMessage("Executing API call for latest quote on {}").addArgument(symbol).log();
            return api.cryptoMarketData().
                    getLatestQuotes(Collections.singleton("BTC/USD")).
                    getQuotes().
                    get("BTC/USD");
        } catch (AlpacaClientException e) {
            if (e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                throw new ConnectionException(e);
            } else {
                // API doesn't specify other errors
                logger.atError().setCause(e).log();
                throw new RuntimeException(e);
            }
        }
    }

    public List<? extends Bar> getMarketData(int no_points, ZonedDateTime end, BarTimePeriod period, int barDuration, TemporalUnit unit) throws ExchangeException {
        try {
            var response = api.cryptoMarketData().getBars(Collections.singleton(symbol),
                    end.minus(no_points, unit), end, no_points, null, barDuration, period);

            var bars = response.getBars().get(symbol);

            while (bars.size() != no_points) {
                response = api.cryptoMarketData().getBars(Collections.singleton(symbol),
                        end.minus(no_points, unit), end, no_points, response.getNextPageToken(), barDuration, period);
                bars.addAll(response.getBars().get(symbol));
            }

            return bars;
        } catch (AlpacaClientException e) {
            if (e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                throw new ConnectionException(e);
            } else {
                // API doesn't specify other errors
                logger.atError().setCause(e).log();
                throw new RuntimeException(e);
            }
        }
    }

    public Order marketOrder(Double quantity, OrderSide side) throws ExchangeException {
        logger.atDebug()
                .setMessage("Placing MARKET SELL order: {} {} (quantity, symbol)")
                .addArgument(quantity)
                .addArgument(symbol)
                .log();

        try {
            return api.orders().requestFractionalMarketOrder(symbol, quantity, side);
        } catch (AlpacaClientException e) {
            throw parseOrderException(e);
        }
    }

    public Order limitOrder(OrderSide side, Double quantity, Double limitPrice, OrderTimeInForce tif) throws ExchangeException {
        logger.atDebug()
                .setMessage("Placing LIMIT order: {} {} {} {} (side, quantity, limit, symbol)")
                .addArgument(side)
                .addArgument(quantity)
                .addArgument(limitPrice)
                .addArgument(symbol)
                .log();
        try {
            return api.orders().requestLimitOrder(symbol, quantity, side, tif, limitPrice, false);
        } catch (AlpacaClientException e) {
            throw parseOrderException(e);
        }
    }

    public Order stopLimitOrder(OrderSide side, Double quantity, Double limitPrice, Double stopPrice, OrderTimeInForce tif) throws ExchangeException {
        logger.atDebug()
                .setMessage("Placing STOP LIMIT order: {} {} {} {} (side, quantity, stop, limit, symbol)")
                .addArgument(side)
                .addArgument(quantity)
                .addArgument(stopPrice)
                .addArgument(limitPrice)
                .addArgument(symbol)
                .log();
        try {
            return api.orders().requestOrder(symbol, quantity, null, side, OrderType.STOP_LIMIT, tif, limitPrice,
                    stopPrice, null, null, false, null, null,
                    null, null, null);
        } catch (AlpacaClientException e) {
            throw parseOrderException(e);
        }
    }

    public Order stopOrder(OrderSide side, Double quantity, Double stopPrice, OrderTimeInForce tif) throws ExchangeException {
        logger.atDebug()
                .setMessage("Placing STOP order: {} {} {} {} (side, quantity, stop, symbol)")
                .addArgument(side)
                .addArgument(quantity)
                .addArgument(stopPrice)
                .addArgument(symbol)
                .log();
        try {
            return api.orders().requestOrder(symbol, quantity, null, side, OrderType.STOP, tif, null,
                    stopPrice, null, null, false, null, null,
                    null, null, null);
        } catch (AlpacaClientException e) {
            throw parseOrderException(e);
        }
    }

    public void cancelAll() throws ExchangeException {
        try {
            api.positions().close(getAsset(), null, 100.0);
        } catch (AlpacaClientException e) {
            if (e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                throw new ConnectionException(e);
            } else if (e.getResponseStatusCode() != null && e.getResponseStatusCode() == 500) {
                throw new LiquidateFailException(e);
            }
        }
    }

    private ExchangeException parseOrderException(AlpacaClientException e) {
        if (e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
            return new ConnectionException(e);
        } else if (e.getResponseStatusCode() != null && e.getResponseStatusCode() == 403) {
            return new InsufficientException(e);
        } else if (e.getResponseStatusCode() != null && e.getResponseStatusCode() == 422) {
            return new UnprocessableRequestException(e);
        } else {
            // Other exceptions are not specified in API
            logger.atError().setCause(e).log();
            throw new RuntimeException(e);
        }
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getAsset() {
        return String.join("", this.symbol.split("/"));
    }
}

