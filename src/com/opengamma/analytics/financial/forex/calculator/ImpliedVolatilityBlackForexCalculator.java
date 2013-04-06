/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the implied volatility for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class ImpliedVolatilityBlackForexCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ImpliedVolatilityBlackForexCalculator INSTANCE = new ImpliedVolatilityBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ImpliedVolatilityBlackForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ImpliedVolatilityBlackForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONVANILLA.impliedVolatility(derivative, data);
  }

}
