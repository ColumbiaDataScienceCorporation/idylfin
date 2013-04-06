/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculator of the gamma (second order derivative with respect to the spot rate) for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class GammaValueBlackForexCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, CurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final GammaValueBlackForexCalculator INSTANCE = new GammaValueBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static GammaValueBlackForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  GammaValueBlackForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXOPTIONBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();

  @Override
  public CurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONVANILLA.gamma(derivative, data, true);
  }

  @Override
  public CurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONBARRIER.gammaFd(derivative, data);
  }

}
