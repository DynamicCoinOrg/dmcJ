package org.bitcoinj.utils;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Usd;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * An exchange rate is expressed as a ratio of a {@link Coin} and a {@link Coin} amount.
 */
public class UsdExchangeRate implements Serializable {

    public final Coin coin;
    public final Usd usd;

    /** Construct exchange rate. This amount of coin is worth that amount of usd. */
    public UsdExchangeRate(Coin coin, Usd usd) {
        this.coin = coin;
        this.usd = usd;
    }

    /** Construct exchange rate. One coin is worth this amount of usd. */
    public UsdExchangeRate(Usd usd) {
        this.coin = Coin.COIN;
        this.usd = usd;
    }

    /**
     * Convert a coin amount to a usd amount using this exchange rate.
     * @throws ArithmeticException if the converted usd amount is too high or too low.
     */
    public Usd coinToUsd(Coin convertCoin) {
        // Use BigInteger because it's much easier to maintain full precision without overflowing.
        final BigInteger converted = BigInteger.valueOf(convertCoin.value).multiply(BigInteger.valueOf(usd.value))
                .divide(BigInteger.valueOf(coin.value));
        if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || converted.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
            throw new ArithmeticException("Overflow");
        return Usd.valueOf(converted.longValue());
    }

    /**
     * Convert a usd amount to a coin amount using this exchange rate.
     * @throws ArithmeticException if the converted coin amount is too high or too low.
     */
    public Coin usdToCoin(Usd convertUsd) {
        // Use BigInteger because it's much easier to maintain full precision without overflowing.
        final BigInteger converted = BigInteger.valueOf(convertUsd.value).multiply(BigInteger.valueOf(coin.value))
                .divide(BigInteger.valueOf(usd.value));
        if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || converted.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
            throw new ArithmeticException("Overflow");
        return Coin.valueOf(converted.longValue());
    }
}
