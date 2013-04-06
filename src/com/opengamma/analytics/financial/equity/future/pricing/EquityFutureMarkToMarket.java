/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.pricing;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;

import org.apache.commons.lang.Validate;

/**
 * Method to compute a future's present value given market price.
 * TODO Review margining behaviour and impact
 */
public final class EquityFutureMarkToMarket implements EquityFuturesPricer {

  private EquityFutureMarkToMarket() {
  }

  private static final EquityFutureMarkToMarket INSTANCE = new EquityFutureMarkToMarket();

  /**
   * @return singleton instance of this pricing method
   */
  public static EquityFutureMarkToMarket getInstance() {
    return INSTANCE;
  }

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return Present value of the derivative
   */
  @Override
  public double presentValue(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getMarketPrice());
    return (dataBundle.getMarketPrice() - future.getStrike()) * future.getUnitAmount();
  }

  /**
   * Computes the ValueRequirement, ValueDelta
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the underlying's spot value
   */
  @Override
  public double spotDelta(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    return future.getUnitAmount();
  }

  /**
   * Computes the ValueRequirement, ValueRho. <p>
   * In this, we model the futuresPrice ~ exp(r*T-costOfCarry)*S => dV/dr = T * futuresPrice
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the discount rate
   */
  @Override
  public double ratesDelta(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    Validate.notNull(future, "Future");
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getMarketPrice(), "_marketPrice must not be null");
    return future.getTimeToSettlement() * dataBundle.getMarketPrice() * future.getUnitAmount();
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

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The spot price of the equity or index
   */
  @Override
  public double spotPrice(EquityFuture future, SimpleFutureDataBundle dataBundle) {
    Validate.notNull(dataBundle.getSpotValue(), "Spot value has not been set in dataBundle of EquityFuture");
    return dataBundle.getSpotValue();
  }

  @Override
  public double forwardPrice(EquityFuture future, SimpleFutureDataBundle dataBundle) {
    return dataBundle.getMarketPrice();
  }
}
