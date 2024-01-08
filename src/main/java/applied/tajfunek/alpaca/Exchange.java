package applied.tajfunek.alpaca;

import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.Bar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;

import java.util.List;

/**
 * Interface describes actions that can be executed on exchange <br>
 * Idea is to use one exchange object for every symbol <br>
 * Allows to check of exchange is open, get quotes, historical market data and
 * make basic types of orders
 */
public interface Exchange {
    /**@return True if it is possible to place an order,False otherwise
     * @throws AlpacaClientException if underlying API calls throw an exception
     */
    default boolean isOpen() throws AlpacaClientException {
        return false;
    }
    /** Gets a quote from exchange for symbol connected with object
     *
     * @return Alpaca Quote
     * @throws AlpacaClientException if underlying API calls throw an exception
     */
    Quote getLatestQuote() throws AlpacaClientException;

    /** Fetch bars from a time interval
     * @return List of bars for a given time interval
     * @throws AlpacaClientException if underlying API calls throw an exception
     */
    List<Bar> getMarketData();

    Order marketOrder(OrderSide side, Double quantity) throws AlpacaClientException;
    Order limitOrder(OrderSide side, Double quantity, Double limitPrice,
                     OrderTimeInForce tif) throws AlpacaClientException;
    Order stopLimitOrder(OrderSide side, Double quantity, Double limitPrice,
                         Double stopPrice, OrderTimeInForce tif) throws AlpacaClientException;


}
