package applied.tajfunek.alpaca;

import applied.tajfunek.alpaca.exceptions.SymbolException;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.crypto.historical.bar.CryptoBar;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderType;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class CryptoExchange {

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

    public boolean isOpen() throws AlpacaClientException {
        // You can always make trades on crypto
        return api.clock().get().getIsOpen();
    }

    public Quote getLatestQuote() throws AlpacaClientException {
        try {
            logger.atInfo().setMessage("Executing API call for latest quote on {}").addArgument(symbol).log();
            return api.cryptoMarketData().
                    getLatestQuotes(Collections.singleton("BTC/USD")).
                    getQuotes().
                    get("BTC/USD");
        } catch (AlpacaClientException e) {
            logger.atError().setCause(e).log();
            throw e;
        }
    }

    public ArrayList<CryptoBar> getMarketData(int no_points, ZonedDateTime end, BarTimePeriod period, int barDuration, TemporalUnit unit) throws AlpacaClientException {
        var response = api.cryptoMarketData().getBars(Collections.singleton(symbol),
                end.minus(no_points, unit), end, no_points, null, barDuration, period);

        var bars = response.getBars().get(symbol);

        while (bars.size() != no_points) {
            response = api.cryptoMarketData().getBars(Collections.singleton(symbol),
                    end.minus(no_points, unit), end, no_points, response.getNextPageToken(), barDuration, period);
            bars.addAll(response.getBars().get(symbol));
        }

        return bars;
    }

    public Order marketOrder(OrderSide side, Double quantity) throws AlpacaClientException {
        return api.orders().requestFractionalMarketOrder(symbol, quantity, side);
    }

    public Order limitOrder(OrderSide side, Double quantity, Double limitPrice, OrderTimeInForce tif) throws AlpacaClientException {
        return api.orders().requestLimitOrder(symbol, quantity, side, tif, limitPrice, false);
    }

    public Order stopLimitOrder(OrderSide side, Double quantity, Double limitPrice, Double stopPrice, OrderTimeInForce tif) throws AlpacaClientException {
        return api.orders().requestOrder(symbol, quantity, null, side, OrderType.STOP_LIMIT, tif, limitPrice,
                stopPrice, null, null, false, null, null,
                null, null, null);
    }

    public Order stopOrder(OrderSide side, Double quantity, Double stopPrice, OrderTimeInForce tif) throws AlpacaClientException {
        return api.orders().requestOrder(symbol, quantity, null, side, OrderType.STOP, tif, null,
                stopPrice, null, null, false, null, null,
                null, null, null);
    }

    public void cancelAll() throws  AlpacaClientException {
        api.positions().close(this.symbol,null, 100.0);
    }
}

