package applied.tajfunek.alpaca;

import applied.tajfunek.alpaca.exceptions.ExchangeException;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.Bar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;

/**
 * Interface describes actions that can be executed on exchange <br>
 * Idea is to use one exchange object for every symbol <br>
 * Allows to check of exchange is open, get quotes, historical market data and
 * make basic types of orders
 */
public interface Exchange {

    default boolean isOpen() throws ExchangeException {
        return false;
    }

    Quote getLatestQuote() throws ExchangeException;

    List<? extends Bar> getMarketData(int no_points, ZonedDateTime end, BarTimePeriod period, int barDuration,
                            TemporalUnit unit) throws ExchangeException;

    Order marketOrder(Double quantity, OrderSide side) throws ExchangeException;
    Order limitOrder(OrderSide side, Double quantity, Double limitPrice,
                     OrderTimeInForce tif) throws ExchangeException;
    Order stopLimitOrder(OrderSide side, Double quantity, Double limitPrice,
                         Double stopPrice, OrderTimeInForce tif) throws ExchangeException;

    public void cancelAll() throws  ExchangeException;

    public String getSymbol();
    public String getAsset();
}
