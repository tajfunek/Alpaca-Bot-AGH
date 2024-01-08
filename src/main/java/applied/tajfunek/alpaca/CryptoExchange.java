package applied.tajfunek.alpaca;

import applied.tajfunek.alpaca.exceptions.SymbolException;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.Bar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderType;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class CryptoExchange implements Exchange{

    private final String symbol;
    private final AlpacaAPI api;
    private final Logger logger;

    public CryptoExchange(AlpacaAPI api, String symbol) throws SymbolException {
        this.symbol = symbol;
        this.api = api;
        this.logger = LoggerFactory.getLogger(Exchange.class);

        try {
            api.assets().getBySymbol(String.join("",symbol.split("/")));
        } catch (AlpacaClientException e) {
            logger.atError().setCause(e).log();
            throw new SymbolException();
        }
    }


    @Override
    public boolean isOpen() throws AlpacaClientException {
        // You can always make trades on crypto
        return true;
    }

    @Override
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

    @Override
    public List<Bar> getMarketData() {
        return null;
    }

    @Override
    public Order marketOrder(OrderSide side, Double quantity) throws AlpacaClientException {
        return api.orders().requestFractionalMarketOrder(symbol,quantity, side);
    }

    @Override
    public Order limitOrder(OrderSide side, Double quantity, Double limitPrice, OrderTimeInForce tif) throws AlpacaClientException {
        return api.orders().requestLimitOrder(symbol, quantity, side, tif, limitPrice, false);
    }

    @Override
    public Order stopLimitOrder(OrderSide side, Double quantity, Double limitPrice, Double stopPrice, OrderTimeInForce tif) throws AlpacaClientException {
        return api.orders().requestOrder(symbol, quantity, null ,side,OrderType.STOP_LIMIT, tif, limitPrice,
                stopPrice,null,null, false, null, null,
                null,null,null);
    }
}
