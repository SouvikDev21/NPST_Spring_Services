package com.fund_transfer.backend.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * All persisted/transmitted amounts are BigInteger paise. Convert to rupees
 * (BigDecimal, scale 2) ONLY at the point of a calculation or a
 * human-facing display — never store or compare rupees directly, and never
 * let a rupee value round-trip back into paise via anything but this class.
 */
public final class MoneyUtil {

    private static final BigDecimal PAISE_PER_RUPEE = BigDecimal.valueOf(100);

    private MoneyUtil() {
    }

    public static BigDecimal paiseToRupees(BigInteger paise) {
        if (paise == null) {
            return null;
        }
        return new BigDecimal(paise).divide(PAISE_PER_RUPEE, 2, RoundingMode.UNNECESSARY);
    }

    public static BigInteger rupeesToPaise(BigDecimal rupees) {
        if (rupees == null) {
            return null;
        }
        return rupees.multiply(PAISE_PER_RUPEE).setScale(0, RoundingMode.UNNECESSARY).toBigInteger();
    }
}