/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;

/**
 * Interpolates, for interest rate instruments using Black model, and returns the implied volatility required.
 */
public final class BlackPriceCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /** The method unique instance.*/
  private static final BlackPriceCalculator INSTANCE = new BlackPriceCalculator();

  /** @return the unique instance of the class. */
  public static BlackPriceCalculator getInstance() {
    return INSTANCE;
  }

  /** Constructor. */
  BlackPriceCalculator() {
  }

  /** The methods used in the calculator. */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod METHOD_MARGINED_FUTUREOPTION = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    return METHOD_SWAPTION_PHYSICAL.presentValue(swaption, curves).getAmount(); // TODO Confirm this is the output the user would expect, wrt scaling of Annuity/PVBP
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final YieldCurveBundle curves) {
    return METHOD_MARGINED_FUTUREOPTION.optionPrice(option, curves);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    return METHOD_MARGINED_FUTUREOPTION.optionPrice(option.getUnderlyingOption(), curves);
  }

}
