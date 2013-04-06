/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A present value curve sensitivity calculator that convert a multi-currency rate sensitivity into the curve currency.
 */
public class PresentValueCurveSensitivityConvertedCurveCurrencyCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The method unique instance.
   */
  private static final PresentValueCurveSensitivityConvertedCurveCurrencyCalculator INSTANCE = new PresentValueCurveSensitivityConvertedCurveCurrencyCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueCurveSensitivityConvertedCurveCurrencyCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * The present value curve sensitivity calculator (with MultiCurrencyAmount output)
   */
  private final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> _pvcsCalculator;

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityConvertedCurveCurrencyCalculator() {
    _pvcsCalculator = PresentValueCurveSensitivityMCSCalculator.getInstance();
  }

  /**
   * Constructor.
   * @param pvcsc The present value curve sensitivity calculator (not converted). Not null.
   */
  public PresentValueCurveSensitivityConvertedCurveCurrencyCalculator(final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> pvcsc) {
    ArgumentChecker.notNull(pvcsc, "present value curve sensitivity calculator");
    _pvcsCalculator = pvcsc;
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(derivative, "derivative");
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMulti = _pvcsCalculator.visit(derivative, curves);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    for (final Currency ccy : pvcsMulti.getCurrencies()) {
      final InterestRateCurveSensitivity pvcs = pvcsMulti.getSensitivity(ccy);
      for (final String curve : pvcs.getCurves()) {
        if (curves.getCurrencyMap().get(curve).equals(ccy)) { // Identical currencies: no changes
          result = result.plus(curve, pvcs.getSensitivities().get(curve));
        } else { // Different currencies: exchange rate multiplication.
          final double fxRate = curves.getFxRates().getFxRate(curves.getCurrencyMap().get(curve), ccy);
          result = result.plus(curve, InterestRateCurveSensitivityUtils.multiplySensitivity(pvcs.getSensitivities().get(curve), fxRate));
        }
      }
    }
    return result;
  }

}
