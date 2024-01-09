package applied.tajfunek.alpaca;

import applied.tajfunek.alpaca.exceptions.SymbolException;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.accountconfiguration.AccountConfiguration;
import net.jacobpeterson.alpaca.model.endpoint.clock.Clock;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.quote.Quote;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.crypto.historical.quote.CryptoQuote;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.crypto.historical.quote.LatestCryptoQuotesResponse;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoExchangeTest {
    private AlpacaAPI api;
    private CryptoExchange exchange;
    private final Logger logger;

    CryptoExchangeTest() {
        this.logger = LoggerFactory.getLogger(CryptoExchangeTest.class);
    }

    @BeforeEach
    void setUp() {
        api = new AlpacaAPI();
        try {
            api.positions().closeAll(Boolean.TRUE);
            exchange = new CryptoExchange(api, "BTC/USD");
        } catch (SymbolException e) {
            throw new RuntimeException(e);
        } catch (AlpacaClientException e) {
            fail(e);
        }


    }
    @Test
    void isOpen() {
        try {
            assertTrue(exchange.isOpen());
        } catch (AlpacaClientException e) {
            logAlpacaException(e);
            fail();
        }
    }

    @Test
    void getLatestQuote() {
        try {
            assertEquals(api.cryptoMarketData().getLatestQuotes(Collections.singleton("BTC/USD")).getQuotes().get("BTC/USD"), exchange.getLatestQuote());
        } catch (AlpacaClientException e) {
            fail(e);
        }
    }

    @Test
    void getMarketData() {
        try {
            var bars = exchange.getMarketData(10, ZonedDateTime.now(), BarTimePeriod.MINUTE, 15, ChronoUnit.DAYS);
            assertEquals(10, bars.size());
        } catch (AlpacaClientException e) {
            fail(e);
            logAlpacaException(e);
        }
    }

    private void logAlpacaException(AlpacaClientException e) {
        logger.atError().setCause(e).log();
    }

    @Test
    void marketOrder() {
        try {
            exchange.marketOrder(OrderSide.BUY, 1.0);
        } catch (AlpacaClientException e) {
            fail(e);
        }

        try {
            var openPositions = api.positions().get();
            var pos = openPositions.get(0);
            assertEquals("BTCUSD", pos.getSymbol());
        } catch (AlpacaClientException e) {
            fail(e);
        }
    }

    @Test
    void limitOrder() {
    }

    @Test
    void stopLimitOrder() {
    }
}