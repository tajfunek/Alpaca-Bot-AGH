package applied.tajfunek.alpaca;

import applied.tajfunek.alpaca.exceptions.ConnectionException;
import applied.tajfunek.alpaca.exceptions.ExchangeException;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.positions.Position;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;

public class MyAccount {
    private final AlpacaAPI api;
    private final Logger logger;

    public MyAccount(AlpacaAPI api) {
        this.api = api;
        this.logger = LoggerFactory.getLogger(MyAccount.class);
    }

    public Position getPosition(String symbol) throws ExchangeException {
        try {
            return api.positions().getBySymbol(symbol);
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

    public Double getCash() throws ExchangeException {
        try {
            return Double.parseDouble(api.account().get().getCash());
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

    public Order getOrder(String orderNumber) throws ExchangeException {
        try {
            return api.orders().get(orderNumber, true);
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
}
