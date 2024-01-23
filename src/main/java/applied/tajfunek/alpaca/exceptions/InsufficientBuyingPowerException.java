package applied.tajfunek.alpaca.exceptions;

import applied.tajfunek.alpaca.Exchange;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;

public class InsufficientBuyingPowerException extends ExchangeException {
    private final double availableBuyingPower;
    private final double requestedBuyingPower;

    public InsufficientBuyingPowerException(AlpacaClientException ace) {
        super(ace);

        String msg = ace.getAPIResponseMessage();
        if (msg == null) {
            // I can't imagine a case where there is API error code 403, and empty message
            // Breaks API specification
            throw new RuntimeException("Critical API error, while parsing Alpaca exception");
        }
        // Message format (requested: {}, available: {})
        this.availableBuyingPower = Double.parseDouble(msg.split(" ")[3].split("\\)")[0]);
        this.requestedBuyingPower = Double.parseDouble(msg.split(" ")[1].split(",")[0]);
    }

    public double getAvailableBuyingPower() {
        return availableBuyingPower;
    }

    public double getRequestedBuyingPower() {
        return requestedBuyingPower;
    }

    @Override
    public int getErrorCode() {
        return 403;
    }
}
