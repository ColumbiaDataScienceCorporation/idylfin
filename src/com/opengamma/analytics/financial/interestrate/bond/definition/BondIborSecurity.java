/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;

/**
 * Describes a Ibor floating coupon bond (Floating Rate Note) issue.
 */
public class BondIborSecurity extends BondSecurity<PaymentFixed, Coupon> {

  /**
   * Ibor floating bond constructor from the nominal and the coupons.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond Ibor coupons. Can be Ibor coupons or fixed coupons (if the fixing is already known). The coupons notional should be in line with the bond nominal.
   * @param settlementTime The time (in years) to settlement date. 
   * @param discountCurveName The name of the curve used for settlement amount discounting.
   */
  public BondIborSecurity(AnnuityPaymentFixed nominal, Annuity<Coupon> coupon, double settlementTime, String discountCurveName) {
    super(nominal, coupon, settlementTime, discountCurveName, "");
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondIborSecurity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondIborSecurity(this);
  }

}
