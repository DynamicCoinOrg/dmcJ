package org.bitcoinj.core;

import com.google.common.math.LongMath;
import org.bitcoinj.utils.MonetaryFormat;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a monetary USD value. This class is immutable.
 */
public final class Usd implements Monetary, Comparable<Usd>, Serializable {

    /**
     * Number of decimals for one USD. This constant is useful for quick adapting to other coins because a lot of
     * constants derive from it.
     */
    public static final int SMALLEST_UNIT_EXPONENT = 3;

    /**
     * The number of units equal to one USD (1/1000).
     */
    private static final long COIN_VALUE = LongMath.pow(10, SMALLEST_UNIT_EXPONENT);

    /**
     * Zero USD.
     */
    public static final Usd ZERO = Usd.valueOf(0);

    /**
     * One USD.
     */
    public static final Usd COIN = Usd.valueOf(COIN_VALUE);

    /**
     * 0.01 USD.
     */
    public static final Usd CENT = COIN.divide(100);

    /**
     * 0.001 USD.
     */
    public static final Usd MILLICENT = COIN.divide(1000);

    /**
     * The number of units of this monetary value.
     */
    public final long value;

    private Usd(final long units) {
        this.value = units;
    }

    public static Usd valueOf(final long units) {
        return new Usd(units);
    }

    @Override
    public int smallestUnitExponent() {
        return SMALLEST_UNIT_EXPONENT;
    }

    /**
     * Returns the number of satoshis of this monetary value.
     */
    @Override
    public long getValue() {
        return value;
    }

    /**
     * Convert an amount expressed in the way humans are used to into units.
     */
    public static Usd valueOf(final int coins, final int cents) {
        checkArgument(cents < 100);
        checkArgument(cents >= 0);
        checkArgument(coins >= 0);
        final Usd coin = COIN.multiply(coins).add(CENT.multiply(cents));
        return coin;
    }

    /**
     * Parses an amount expressed in the way humans are used to.<p>
     * <p/>
     * This takes string in a format understood by {@link BigDecimal#BigDecimal(String)},
     * for example "0", "1", "0.10", "1.23E3", "1234.5E-5".
     *
     * @throws IllegalArgumentException if you try to specify fractional satoshis, or a value out of range.
     */
    public static Usd parseCoin(final String str) {
        return Usd.valueOf(new BigDecimal(str).movePointRight(SMALLEST_UNIT_EXPONENT).toBigIntegerExact().longValue());
    }

    public Usd add(final Usd value) {
        return new Usd(LongMath.checkedAdd(this.value, value.value));
    }

    public Usd subtract(final Usd value) {
        return new Usd(LongMath.checkedSubtract(this.value, value.value));
    }

    public Usd multiply(final long factor) {
        return new Usd(LongMath.checkedMultiply(this.value, factor));
    }

    public Usd divide(final long divisor) {
        return new Usd(this.value / divisor);
    }

    public Usd[] divideAndRemainder(final long divisor) {
        return new Usd[] { new Usd(this.value / divisor), new Usd(this.value % divisor) };
    }

    public long divide(final Usd divisor) {
        return this.value / divisor.value;
    }

    /**
     * Returns true if and only if this instance represents a monetary value greater than zero,
     * otherwise false.
     */
    public boolean isPositive() {
        return signum() == 1;
    }

    /**
     * Returns true if and only if this instance represents a monetary value less than zero,
     * otherwise false.
     */
    public boolean isNegative() {
        return signum() == -1;
    }

    /**
     * Returns true if and only if this instance represents zero monetary value,
     * otherwise false.
     */
    public boolean isZero() {
        return signum() == 0;
    }

    /**
     * Returns true if the monetary value represented by this instance is greater than that
     * of the given other Coin, otherwise false.
     */
    public boolean isGreaterThan(Usd other) {
        return compareTo(other) > 0;
    }

    /**
     * Returns true if the monetary value represented by this instance is less than that
     * of the given other Coin, otherwise false.
     */
    public boolean isLessThan(Usd other) {
        return compareTo(other) < 0;
    }

    public Usd shiftLeft(final int n) {
        return new Usd(this.value << n);
    }

    public Usd shiftRight(final int n) {
        return new Usd(this.value >> n);
    }

    @Override
    public int signum() {
        if (this.value == 0)
            return 0;
        return this.value < 0 ? -1 : 1;
    }

    public Usd negate() {
        return new Usd(-this.value);
    }

    /**
     * Returns the number of satoshis of this monetary value. It's deprecated in favour of accessing {@link #value}
     * directly.
     */
    public long longValue() {
        return this.value;
    }

    private static final MonetaryFormat FRIENDLY_FORMAT = MonetaryFormat.USD.minDecimals(2).repeatOptionalDecimals(1, 6).postfixCode();

    /**
     * Returns the value as a 0.12 type string. More digits after the decimal place will be used
     * if necessary, but two will always be present.
     */
    public String toFriendlyString() {
        return FRIENDLY_FORMAT.format(this).toString();
    }

    private static final MonetaryFormat PLAIN_FORMAT = MonetaryFormat.USD.minDecimals(0).repeatOptionalDecimals(1, 8).noCode();

    /**
     * <p>
     * Returns the value as a plain string denominated in BTC.
     * The result is unformatted with no trailing zeroes.
     * For instance, a value of 150000 satoshis gives an output string of "0.0015" BTC
     * </p>
     */
    public String toPlainString() {
        return PLAIN_FORMAT.format(this).toString();
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (o == null || o.getClass() != getClass())
            return false;
        final Usd other = (Usd) o;
        if (this.value != other.value)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int) this.value;
    }

    @Override
    public int compareTo(final Usd other) {
        if (this.value == other.value)
            return 0;
        return this.value > other.value ? 1 : -1;
    }
}

