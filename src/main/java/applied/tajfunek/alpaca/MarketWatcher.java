package applied.tajfunek.alpaca;

import net.jacobpeterson.alpaca.AlpacaAPI;

public class MarketWatcher {
    final private AlpacaAPI api;

    public MarketWatcher(AlpacaAPI api) {
        this.api = api;
    }
}
