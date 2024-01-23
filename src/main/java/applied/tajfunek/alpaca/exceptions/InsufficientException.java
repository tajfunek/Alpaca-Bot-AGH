package applied.tajfunek.alpaca.exceptions;

import net.jacobpeterson.alpaca.rest.AlpacaClientException;

public class InsufficientException extends ExchangeException {
    private final double available;
    private final double requested;

    public InsufficientException(AlpacaClientException ace){
        super(ace);

        String msg = ace.getAPIResponseMessage();
        if (msg == null) {
            // I can't imagine a case where there is API error code 403, and empty message
            // Breaks API specification
            throw new RuntimeException("Critical API error, while parsing Alpaca exception");
        }
        // Message format (requested: {}, available: {})
        this.available = Double.parseDouble(msg.split(" ")[3].split("\\)")[0]);
        this.requested = Double.parseDouble(msg.split(" ")[1].split(",")[0]);
    }


    public double getAvailable() {
        return available;
    }

    public Double getRequested() {
        return requested;
    }

    @Override
    public int getErrorCode() {
        return 403;
    }
}
