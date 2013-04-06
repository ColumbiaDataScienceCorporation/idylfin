/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureOptionPremiumTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value calculator for interest rate instruments using Black model with implied volatilities.
 */
public final class PresentValueBlackCalculator extends PresentValueCalculator {

  /**
   * The method unique instance.
   */
  private static final PresentValueBlackCalculator INSTANCE = new PresentValueBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueBlackCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod PHYSICAL_SWAPTION = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final SwaptionCashFixedIborBlackMethod CASH_SWAPTION = SwaptionCashFixedIborBlackMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod MARGINNED_IR_FUTURE_OPTION = InterestRateFutureOptionMarginTransactionBlackSurfaceMethod.getInstance();
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod PREMIUM_BOND_FUTURE_OPTION = BondFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return CASH_SWAPTION.presentValue(swaption, curvesBlack).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return PHYSICAL_SWAPTION.presentValue(swaption, curvesBlack).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      return MARGINNED_IR_FUTURE_OPTION.presentValue(option, curves).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    if (curves instanceof YieldCurveWithBlackCubeBundle) {
      final InterestRateFutureOptionPremiumSecurity underlyingOption = option.getUnderlyingOption();
      final InterestRateFutureOptionMarginSecurity underlyingMarginedOption = new InterestRateFutureOptionMarginSecurity(underlyingOption.getUnderlyingFuture(), underlyingOption.getExpirationTime(),
          underlyingOption.getStrike(), underlyingOption.isCall());
      final InterestRateFutureOptionMarginTransaction margined = new InterestRateFutureOptionMarginTransaction(underlyingMarginedOption, option.getQuantity(), option.getTradePrice());
      return MARGINNED_IR_FUTURE_OPTION.presentValue(margined, curves).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueBlackCalculator visitor visitInterestRateFutureOptionPremiumTransaction requires a YieldCurveWithBlackCubeBundle as data.");
  }

  @Override
  public Double visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(option, "option");
    return PREMIUM_BOND_FUTURE_OPTION.presentValue(option, curves).getAmount();
    //    if (curves instanceof YieldCurveWithBlackCubeBundle) {
    //      return PREMIUM_BOND_FUTURE_OPTION.presentValue(option, curves).getAmount();
    //    } else if (curves instanceof YieldCurveWithBlackCubeAndForwardBundle) {
    //      return PREMIUM_BOND_FUTURE_OPTION.presentValueFromPrice(option, curves, ((YieldCurveWithBlackCubeAndForwardBundle) curves).getForward()).getAmount();
    //    }
    //    throw new UnsupportedOperationException(
    //        "The PresentValueBlackCalculator visitor visitBondFutureOptionPremiumTransaction requires a YieldCurveWithBlackCubeBundle as data.");
  }

}
