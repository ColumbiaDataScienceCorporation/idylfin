/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;

/**
 * Method to compute a future's present value given the current value of its underlying asset and a cost of carry. 
 * !!! This may include a convexity adjustment for the correlation between these two factors. 
 */
public final class EquityFutureCostOfCarry implements EquityFuturesPricer {

  private EquityFutureCostOfCarry() {
  }

  private static final EquityFutureCostOfCarry INSTANCE = new EquityFutureCostOfCarry();

  /**
   * @return singleton instance of this pricing method
   */
  public static EquityFutureCostOfCarry getInstance() {
    return INSTANCE;
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The spot price of the equity or index
   */
  @Override
  public double spotPrice(EquityFuture future, SimpleFutureDataBundle dataBundle) {
    return dataBundle.getSpotValue();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The forward price of the equity or index
   */
  @Override
  public double forwardPrice(EquityFuture future, SimpleFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getCostOfCarry());
    Validate.notNull(dataBundle.getSpotValue());
    double fwdPrice = dataBundle.getSpotValue() * Math.exp(dataBundle.getCostOfCarry() * future.getTimeToSettlement());
    return fwdPrice;
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return Present value of the derivative
   */
  @Override
  public double presentValue(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    double fwdPrice = forwardPrice(future, dataBundle);
    return (fwdPrice - future.getStrike()) * future.getUnitAmount();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the underlying's spot value
   */
  @Override
  public double spotDelta(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getCostOfCarry());
    return future.getUnitAmount() * Math.exp(dataBundle.getCostOfCarry() * future.getTimeToSettlement());
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the discount rate
   */
  @Override
  public double ratesDelta(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getCostOfCarry());
    Validate.notNull(dataBundle.getSpotValue());

    double fwdPrice = dataBundle.getSpotValue() * Math.exp(dataBundle.getCostOfCarry() * future.getTimeToSettlement());
    return future.getTimeToSettlement() * fwdPrice * future.getUnitAmount();
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a basis point change in the discount rate
   */
  @Override
  public double pv01(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    return ratesDelta(future, dataBundle) / 10000;
  }
}
