package applied.tajfunek.alpaca.exceptions;

import net.jacobpeterson.alpaca.rest.AlpacaClientException;

public class UnprocessableRequestException extends ExchangeException {
    public UnprocessableRequestException(AlpacaClientException e) {
        super(e);
    }
}
