package applied.tajfunek.alpaca.exceptions;

import net.jacobpeterson.alpaca.rest.AlpacaClientException;

public class LiquidateFailException extends ExchangeException {
    public LiquidateFailException(AlpacaClientException ace) {
        super(ace);
    }
}
