/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFloating;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Calculator of decision schedule for different instruments. Used in particular for Monte Carlo pricing.
 */
public class DecisionScheduleDerivativeCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, DecisionScheduleDerivative> {

  /**
   * The cash-flow equivalent calculator.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final CashFlowEquivalentCurveSensitivityCalculator CFECSC = CashFlowEquivalentCurveSensitivityCalculator.getInstance();

  /**
   * The unique instance of the calculator.
   */
  private static final DecisionScheduleDerivativeCalculator INSTANCE = new DecisionScheduleDerivativeCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static DecisionScheduleDerivativeCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  DecisionScheduleDerivativeCalculator() {
  }

  @Override
  public DecisionScheduleDerivative visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public DecisionScheduleDerivative visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    double[] decisionTime = new double[] {swaption.getTimeToExpiry()};
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), curves);
    double[][] impactTime = new double[1][cfe.getNumberOfPayments()];
    double[][] impactAmount = new double[1][cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      impactTime[0][loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      impactAmount[0][loopcf] = cfe.getNthPayment(loopcf).getAmount();
    }
    ArrayList<Map<Double, InterestRateCurveSensitivity>> impactAmountDerivative = new ArrayList<Map<Double, InterestRateCurveSensitivity>>();
    impactAmountDerivative.add(CFECSC.visit(swaption.getUnderlyingSwap(), curves));
    DecisionScheduleDerivative decision = new DecisionScheduleDerivative(decisionTime, impactTime, impactAmount, impactAmountDerivative);
    return decision;
  }

  @Override
  public DecisionScheduleDerivative visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final YieldCurveBundle curves) {
    int nbCpn = annuity.getNumberOfPayments();
    double[] decisionTime = new double[nbCpn];
    double[][] impactTime = new double[nbCpn][];
    double[][] impactAmount = new double[nbCpn][];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      AnnuityPaymentFixed cfe = CFEC.visit(annuity.getNthPayment(loopcpn), curves);
      decisionTime[loopcpn] = annuity.isFixed()[loopcpn] ? 0.0 : ((CouponFloating) annuity.getNthPayment(loopcpn)).getFixingTime();
      impactTime[loopcpn] = new double[cfe.getNumberOfPayments()];
      impactAmount[loopcpn] = new double[cfe.getNumberOfPayments()];
      for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
        impactTime[loopcpn][loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
        impactAmount[loopcpn][loopcf] = cfe.getNthPayment(loopcf).getAmount();
      }
    }
    ArrayList<Map<Double, InterestRateCurveSensitivity>> impactAmountDerivative = new ArrayList<Map<Double, InterestRateCurveSensitivity>>();
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      impactAmountDerivative.add(CFECSC.visit(annuity.getNthPayment(loopcpn), curves));
    }
    DecisionScheduleDerivative decision = new DecisionScheduleDerivative(decisionTime, impactTime, impactAmount, impactAmountDerivative);
    return decision;
  }

}
