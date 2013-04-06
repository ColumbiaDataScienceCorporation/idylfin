/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Constant Maturity Swap coupon.
 */
public class CouponCMSDefinition extends CouponFloatingDefinition {

  /**
   * The swap underlying the CMS coupon.
   */
  private final SwapFixedIborDefinition _underlyingSwap;
  /**
   * The CMS index associated to the coupon.
   */
  private final IndexSwap _cmsIndex;

  /**
   * Constructor of a CMS coupon from all the details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   * @param cmsIndex The CMS index associated to the coupon.
   */
  public CouponCMSDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final SwapFixedIborDefinition underlyingSwap, final IndexSwap cmsIndex) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.notNull(cmsIndex, "CMS index");
    _underlyingSwap = underlyingSwap;
    _cmsIndex = cmsIndex;
  }

  /**
   * Constructor of a CMS coupon from all the details.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   * @param cmsIndex The CMS index associated to the coupon.
   * @return The CMS coupon.
   */
  public static CouponCMSDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional,
      final ZonedDateTime fixingDate, final SwapFixedIborDefinition underlyingSwap, final IndexSwap cmsIndex) {
    Validate.notNull(underlyingSwap, "underlying swap");
    return new CouponCMSDefinition(underlyingSwap.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap, cmsIndex);
  }

  /**
   * Builder of a CMS coupon. The fixing date is computed from the start accrual date with the Ibor index spot lag. The underlying swap is computed from that date and the CMS index.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param cmsIndex The CMS index associated to the coupon.
   * @return The CMS coupon.
   */
  public static CouponCMSDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional,
      final IndexSwap cmsIndex) {
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -cmsIndex.getIborIndex().getSpotLag(), cmsIndex.getIborIndex().getCalendar());
    // Implementation comment: the underlying swap is used for forward. The notional, rate and payer flag are irrelevant.
    final SwapFixedIborDefinition underlyingSwap = SwapFixedIborDefinition.from(accrualStartDate, cmsIndex, 1.0, 1.0, true);
    return new CouponCMSDefinition(underlyingSwap.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap, cmsIndex);
  }

  /**
   * Builder from a floating coupon and an underlying swap.
   * @param coupon A floating coupon with the details of the coupon to construct.
   * @param underlyingSwap A swap describing the CMS underlying. The rate and notional are not used.
   * @param cmsIndex The CMS index associated to the coupon.
   * @return The constructed CMS coupon.
   */
  public static CouponCMSDefinition from(final CouponFloatingDefinition coupon, final SwapFixedIborDefinition underlyingSwap, final IndexSwap cmsIndex) {
    Validate.notNull(coupon, "floating coupon");
    Validate.notNull(underlyingSwap, "underlying swap");
    return new CouponCMSDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), underlyingSwap, cmsIndex);
  }

  /**
   * Builder from a floating coupon and a CMS Index.
   * @param coupon A floating coupon with the details of the coupon to construct.
   * @param cmsIndex The CMS index associated to the coupon.
   * @return The constructed CMS coupon.
   */
  public static CouponCMSDefinition from(final CouponFloatingDefinition coupon, final IndexSwap cmsIndex) {
    Validate.notNull(coupon, "floating coupon");
    Validate.notNull(cmsIndex, "CMS index");
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(coupon.getFixingDate(), cmsIndex.getIborIndex().getSpotLag(), cmsIndex.getIborIndex().getCalendar());
    // Implementation comment: the underlying swap is used for forward. The notional, rate and payer flag are irrelevant.
    final SwapFixedIborDefinition underlyingSwap = SwapFixedIborDefinition.from(settlementDate, cmsIndex, 1.0, 1.0, true);
    return new CouponCMSDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), underlyingSwap, cmsIndex);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap
   */
  public SwapFixedIborDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the CMS index associated to the coupon.
   * @return The CMS index.
   */
  public IndexSwap getCMSIndex() {
    return _cmsIndex;
  }

  @Override
  public String toString() {
    return super.toString() + ", Swap = " + _underlyingSwap.toString();
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(date.isBefore(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    // CMS is not fixed yet, all the details are required.
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double settlementTime = TimeCalculator.getTimeBetween(date, _underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<Coupon> swap = _underlyingSwap.toDerivative(date, yieldCurveNames);
    //Implementation remark: SwapFixedIbor can not be used as the first coupon may have fixed already and one CouponIbor is now fixed.
    return new CouponCMS(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap, settlementTime);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    Validate.notNull(dateTime, "date");
    LocalDate dayConversion = dateTime.toLocalDate();
    Validate.notNull(indexFixingTimeSeries, "Index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final String fundingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    LocalDate dayFixing = getFixingDate().toLocalDate();
    if (dayConversion.equals(dayFixing)) { // The fixing is on the reference date; if known the fixing is used and if not, the floating coupon is created.
      Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      if (fixedRate != null) {
        return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate);
      }
    }
    if (dayConversion.isAfter(dayFixing)) { // The fixing is required
      Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate().withHourOfDay(0)); // TODO: remove time from fixing date.
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + dayFixing);
      }
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate);
    }
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double settlementTime = TimeCalculator.getTimeBetween(dateTime, _underlyingSwap.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<Coupon> swap = _underlyingSwap.toDerivative(dateTime, yieldCurveNames);
    //Implementation remark: SwapFixedIbor can not be used as the first coupon may have fixed already and one CouponIbor is now fixed.
    return new CouponCMS(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, swap, settlementTime);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitCouponCMSDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponCMSDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _underlyingSwap.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponCMSDefinition other = (CouponCMSDefinition) obj;
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
