/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;
import java.util.Map;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 * TODO: The output is in the form of a map and not in the form of the InterestRateCurveSensitivity object. 
 * This is to help in the yield curve construction but this class should be deprecated soon.
 */
public final class ParSpreadMarketQuoteCurveMapSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCurveMapSensitivityCalculator INSTANCE = new ParSpreadMarketQuoteCurveMapSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCurveMapSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCurveMapSensitivityCalculator() {
  }

  /**
   * The methods and calculators.
   */

  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();

  @Override
  public Map<String, List<DoublesPair>> visit(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return PSMQCSC.visit(instrument, curves).getSensitivities();
  }

}
