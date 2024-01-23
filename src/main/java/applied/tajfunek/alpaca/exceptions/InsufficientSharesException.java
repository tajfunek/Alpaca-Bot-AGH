package applied.tajfunek.alpaca.exceptions;

import net.jacobpeterson.alpaca.rest.AlpacaClientException;

public class InsufficientSharesException extends ExchangeException {
    private final double availableShares;
    private final double requestedShares;

    public InsufficientSharesException(AlpacaClientException ace){
        super(ace);

        String msg = ace.getAPIResponseMessage();
        if (msg == null) {
            // I can't imagine a case where there is API error code 403, and empty message
            // Breaks API specification
            throw new RuntimeException("Critical API error, while parsing Alpaca exception");
        }
        // Message format (requested: {}, available: {})
        this.availableShares = Double.parseDouble(msg.split(" ")[3].split("\\)")[0]);
        this.requestedShares = Double.parseDouble(msg.split(" ")[1].split(",")[0]);
    }


    public double getAvailableShares() {
        return availableShares;
    }

    public Double getRequestedShares() {
        return requestedShares;
    }

    @Override
    public int getErrorCode() {
        return 403;
    }
}
