/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.derivative;

import java.util.ArrayList;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;

/**
 * A wrapper class for a AnnuityDefinition containing mainly CouponIborRatchetDefinition. The first coupon should be a CouponFixed or a CouponIborGearing.
 * The other coupons should be CouponFixed or a CouponIborRatchet.
 */
public class AnnuityCouponIborRatchet extends Annuity<Coupon> {

  /**
   * List of calibration types for the Ratchet Ibor coupon annuity.
   */
  public enum RatchetIborCalibrationType {
    /**
     * The calibration instruments are caps at strike given by the coupon in the forward curve.
     */
    FORWARD_COUPON
  }

  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  /**
   * Flag indicating if a coupon is already fixed.
   */
  private final boolean[] _isFixed;

  /**
   * @param payments The payments composing the annuity.
   */
  public AnnuityCouponIborRatchet(Coupon[] payments) {
    super(payments);
    _isFixed = new boolean[payments.length];
    Validate.isTrue((payments[0] instanceof CouponFixed) || (payments[0] instanceof CouponIborGearing), "First coupon should be CouponFixed or a CouponIborGearing");
    _isFixed[0] = (payments[0] instanceof CouponFixed);
    for (int looppay = 1; looppay < payments.length; looppay++) {
      Validate.isTrue((payments[looppay] instanceof CouponFixed) || (payments[looppay] instanceof CouponIborRatchet), "Next coupons should be CouponFixed or CouponIborRatchet");
      _isFixed[looppay] = (payments[looppay] instanceof CouponFixed);
    }
  }

  /**
   * Gets the flag indicating if a coupon is already fixed.
   * @return The flag.
   */
  public boolean[] isFixed() {
    return _isFixed;
  }

  public InstrumentDerivative[] calibrationBasket(final RatchetIborCalibrationType type, final YieldCurveBundle curves) {
    ArrayList<InstrumentDerivative> calibration = new ArrayList<InstrumentDerivative>();
    switch (type) {
      case FORWARD_COUPON:
        int nbCpn = getNumberOfPayments();
        double[] cpnRate = new double[nbCpn];
        for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
          if (getNthPayment(loopcpn) instanceof CouponIborRatchet) {
            CouponIborRatchet cpn = (CouponIborRatchet) getNthPayment(loopcpn);
            double ibor = PRC.visitCouponIborSpread(cpn, curves);
            double cpnMain = cpn.getMainCoefficients()[0] * cpnRate[loopcpn - 1] + cpn.getMainCoefficients()[1] * ibor + cpn.getMainCoefficients()[2];
            double cpnFloor = cpn.getFloorCoefficients()[0] * cpnRate[loopcpn - 1] + cpn.getFloorCoefficients()[1] * ibor + cpn.getFloorCoefficients()[2];
            double cpnCap = cpn.getCapCoefficients()[0] * cpnRate[loopcpn - 1] + cpn.getCapCoefficients()[1] * ibor + cpn.getCapCoefficients()[2];
            cpnRate[loopcpn] = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
            calibration.add(new CapFloorIbor(cpn.getCurrency(), cpn.getPaymentTime(), cpn.getFundingCurveName(), cpn.getPaymentYearFraction(), cpn.getNotional(), cpn.getFixingTime(), cpn.getIndex(),
                cpn.getFixingPeriodStartTime(), cpn.getFixingPeriodEndTime(), cpn.getFixingYearFraction(), cpn.getForwardCurveName(), cpnRate[loopcpn], true));
          } else {
            if (getNthPayment(loopcpn) instanceof CouponFixed) {
              CouponFixed cpn = (CouponFixed) getNthPayment(loopcpn);
              cpnRate[loopcpn] = cpn.getFixedRate();
            } else {
              CouponIborGearing cpn = (CouponIborGearing) getNthPayment(loopcpn);
              double ibor = PRC.visitCouponIborGearing(cpn, curves);
              cpnRate[loopcpn] = cpn.getFactor() * ibor + cpn.getSpread();
            }
          }
        }
        break;

      default:
        break;
    }
    return calibration.toArray(new InstrumentDerivative[0]);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitAnnuityCouponIborRatchet(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitAnnuityCouponIborRatchet(this);
  }

}
