package applied.tajfunek.alpaca.exceptions;

import net.jacobpeterson.alpaca.rest.AlpacaClientException;

public class ExchangeException extends Exception {
   ExchangeException(AlpacaClientException ace) {
       super(ace);
   }

   public int getErrorCode() {
       return 0;
   }
}
