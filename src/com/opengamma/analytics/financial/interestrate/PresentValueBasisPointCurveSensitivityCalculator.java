/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes the sensitivity of the par spread to the curve rates. 
 */
public final class PresentValueBasisPointCurveSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBasisPointCurveSensitivityCalculator INSTANCE = new PresentValueBasisPointCurveSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBasisPointCurveSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBasisPointCurveSensitivityCalculator() {
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle data) {
    return new InterestRateCurveSensitivity();
  }

  public InterestRateCurveSensitivity visitCoupon(final Coupon coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(coupon.getFundingCurveName());
    double df = fundingCurve.getDiscountFactor(coupon.getPaymentTime());
    // Backward sweep
    double pvbpBar = 1.0;
    double dfBar = coupon.getPaymentYearFraction() * coupon.getNotional() * pvbpBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    resultMapDsc.put(coupon.getFundingCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

  @Override
  public InterestRateCurveSensitivity visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitCouponIborCompounded(final CouponIborCompounded coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public InterestRateCurveSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    InterestRateCurveSensitivity pvbpSensi = new InterestRateCurveSensitivity();
    for (final Payment p : annuity.getPayments()) {
      pvbpSensi = pvbpSensi.plus(visit(p, curves));
    }
    return pvbpSensi;
  }

  @Override
  public InterestRateCurveSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

}
