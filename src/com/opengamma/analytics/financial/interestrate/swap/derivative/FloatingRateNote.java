/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.interestrate.swap.derivative;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Replaced by a proper bond instrument.
 */
@Deprecated
public class FloatingRateNote extends Swap<PaymentFixed, CouponIborSpread> {

  public FloatingRateNote(final Annuity<CouponIborSpread> forwardLiborAnnuity, final PaymentFixed initalPayment, final PaymentFixed finalPayment) {
    super(setUpFixedLeg(forwardLiborAnnuity, initalPayment, finalPayment), forwardLiborAnnuity);
  }

  private static Annuity<PaymentFixed> setUpFixedLeg(final Annuity<CouponIborSpread> annuity, final PaymentFixed initalPayment, final PaymentFixed finalPayment) {

    final String curveName = annuity.getDiscountCurve();

    //consistency checks on the inputs

    Validate.isTrue(initalPayment.getCurrency() == finalPayment.getCurrency(), "initial and final payments in different currencies");

    Validate.isTrue(initalPayment.getCurrency() == annuity.getCurrency(), "flaoting and fixed payments in different currencies");

    Validate.isTrue(initalPayment.getPaymentTime() < finalPayment.getPaymentTime(), "initial payment after final payment");

    Validate.isTrue(initalPayment.getPaymentTime() <= annuity.getNthPayment(0).getPaymentTime(), "initial payment after first floating payments");

    Validate.isTrue(curveName.equals(initalPayment.getFundingCurveName()), "inital payment discounted off different curve to floating payments");

    Validate.isTrue(curveName.equals(finalPayment.getFundingCurveName()), "final payment discounted off different curve to floating payments");

    Validate.isTrue(initalPayment.getAmount() * finalPayment.getAmount() < 0, "inital payment should be oposite sign to final");

    Validate.isTrue((annuity.isPayer() && initalPayment.getAmount() > 0.0) || (!annuity.isPayer() && initalPayment.getAmount() < 0.0), "initial payment should be oposite sign to Ibor coupons");

    final PaymentFixed[] fixedPayments = new PaymentFixed[2];

    fixedPayments[0] = initalPayment;

    fixedPayments[1] = finalPayment;

    return new Annuity<PaymentFixed>(fixedPayments);

  }

  public AnnuityCouponIborSpread getFloatingLeg() {
    return (AnnuityCouponIborSpread) getSecondLeg();
  }

  /**

    * Return the currency of the annuity. 

    * @return The currency

    */

  public Currency getCurrency() {

    return getFirstLeg().getCurrency();

  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {

    return visitor.visitFloatingRateNote(this, data);

  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {

    return visitor.visitFloatingRateNote(this);

  }

}
