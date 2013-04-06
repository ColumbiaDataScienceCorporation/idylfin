/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.fra;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Forward Rate Agreement (FRA). The pay-off is (Ibor fixing-FRA rate) multiplied by the notional and accrual fraction and discounted with the fixing rate to the payment date.
 * This correspond to "buy" a FRA is the same as buying the Ibor rate and paying the FRA rate for it.
 */
public class ForwardRateAgreementDefinition extends CouponFloatingDefinition {

  /**
   * Ibor-like index on which the FRA fixes. The index currency should be the same as the instrument currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * The end date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * The FRA rate.
   */
  private final double _rate;

  /**
   * Constructor of a FRA from contract details and the Ibor index. The payment currency is the index currency.
   * 
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param rate The FRA rate.
   */
  public ForwardRateAgreementDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double rate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), _index.getCalendar());
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), index.getCalendar(),
        index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate);
    _rate = rate;
  }

  /**
   * Builder of FRA from a coupon, the fixing date and the index. The fixing period dates are deduced from the index and the fixing date.
   * @param coupon Underlying coupon.
   * @param fixingDate The coupon fixing date.
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition from(final CouponDefinition coupon, final ZonedDateTime fixingDate, final IborIndex index, final double rate) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.notNull(index, "index");
    return new ForwardRateAgreementDefinition(index.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(),
        coupon.getPaymentYearFraction(), coupon.getNotional(), fixingDate, index, rate);
  }

  /**
   * Builder of FRA from a coupon, the fixing date and the index. The fixing period dates are deduced from the index and the fixing date.
   * @param tradeDate The FRA trade date.
   * @param startPeriod The period from trade to start date.
   * @param notional The notional
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition fromTrade(final ZonedDateTime tradeDate, final Period startPeriod, final double notional, final IborIndex index,
      final double rate) {
    ArgumentChecker.notNull(tradeDate, "trade date");
    ArgumentChecker.notNull(startPeriod, "start period");
    ArgumentChecker.notNull(index, "index");
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(tradeDate, index.getSpotLag(), index.getCalendar());
    final ZonedDateTime accrualStartDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, index);
    final Period endPeriod = startPeriod.plus(index.getTenor());
    final ZonedDateTime accrualEndDate = ScheduleCalculator.getAdjustedDate(spotDate, endPeriod, index);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), index.getCalendar());
    final double accrualFactor = index.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate);
    return new ForwardRateAgreementDefinition(index.getCurrency(), accrualStartDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, rate);
  }

  /**
   * Builder of FRA from the accrual start date, the accrual end date and the index.
   * @param accrualStartDate (Unadjusted) start date of the accrual period
   * @param accrualEndDate (Unadjusted) end date of the accrual period
   * @param notional The notional
   * @param index The FRA Ibor index.
   * @param rate The FRA rate.
   * @return The FRA.
   */
  public static ForwardRateAgreementDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional,
      final IborIndex index, final double rate) {
    ArgumentChecker.notNull(accrualStartDate, "accrual start date");
    ArgumentChecker.notNull(accrualEndDate, "accrual end date");
    ArgumentChecker.notNull(index, "index");
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -index.getSpotLag(), index.getCalendar());
    final double paymentAccrualFactor = index.getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate);
    return new ForwardRateAgreementDefinition(index.getCurrency(), accrualStartDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate, index,
        rate);
  }

  /**
   * Gets the Ibor index of the instrument.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the fixing period start date.
   * @return The fixing period start date.
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixing period end date field.
   * @return the fixing period end date
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixingPeriodAccrualFactor field.
   * @return the fixing period accrual factor
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the FRA rate.
   * @return The rate.
   */
  public double getRate() {
    return _rate;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitForwardRateAgreementDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitForwardRateAgreementDefinition(this);
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(date.isBefore(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " "
        + date);
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curve names are required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate());
    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodEndDate());
    return new ForwardRateAgreement(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), _index, fixingTime, fixingPeriodStartTime,
        fixingPeriodEndTime, getFixingPeriodAccrualFactor(), _rate, forwardCurveName);
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, getPaymentDate());
    if (date.isAfter(getFixingDate()) || (date.equals(getFixingDate()))) {
      Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
      if (fixedRate == null) {
        final ZonedDateTime fixingDateAtLiborFixingTime = getFixingDate().withTime(11, 0);
        fixedRate = indexFixingTimeSeries.getValue(fixingDateAtLiborFixingTime);
      }
      if (fixedRate == null) {
        final ZonedDateTime previousBusinessDay = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding").adjustDate(getIndex().getCalendar(),
            getFixingDate().minusDays(1));
        fixedRate = indexFixingTimeSeries.getValue(previousBusinessDay);
        //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
        if (fixedRate == null) {
          final ZonedDateTime previousBusinessDayAtLiborFixingTime = previousBusinessDay.withTime(11, 0);
          fixedRate = indexFixingTimeSeries.getValue(previousBusinessDayAtLiborFixingTime);
        }
        if (fixedRate == null) {
          throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
        }
      }
      return new CouponFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate - _rate);
    }

    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = actAct.getDayCountFraction(zonedDate, getFixingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, getFixingPeriodEndDate());
    return new ForwardRateAgreement(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), _index, fixingTime, fixingPeriodStartTime,
        fixingPeriodEndTime, getFixingPeriodAccrualFactor(), _rate, forwardCurveName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    result = prime * result + _index.hashCode();
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final ForwardRateAgreementDefinition other = (ForwardRateAgreementDefinition) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }
}
