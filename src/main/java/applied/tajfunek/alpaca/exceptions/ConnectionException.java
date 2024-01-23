package applied.tajfunek.alpaca.exceptions;

import net.jacobpeterson.alpaca.rest.AlpacaClientException;

public class ConnectionException extends ExchangeException {
    public ConnectionException(AlpacaClientException ace){
        super(ace);
    }
}
