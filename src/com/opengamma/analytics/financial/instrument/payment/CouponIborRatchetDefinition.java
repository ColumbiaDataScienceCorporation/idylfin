/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Ratchet on Ibor coupon. The coupon payment depends on the
 * previous coupon ($C_{i-1}$), the current Ibor fixing ($L_i$). The pay-off is:
 * $$
 * \begin{equation*}
 * \alpha^M_i C_{i-1} + \beta^M_i L_i + \gamma^M_i 
 * \end{equation*}
 * $$ 
 * subject to the floor :
 * $$
 * \begin{equation*}
 * \alpha^F_i C_{i-1} + \beta^F_i L_i + \gamma^F_i 
 * \end{equation*}
 * $$ 
 * and the cap:
 * $$
 * \begin{equation*}
 * \alpha^C_i C_{i-1} + \beta^C_i L_i + \gamma^C_i 
 * \end{equation*}
 * $$
 */
public class CouponIborRatchetDefinition extends CouponFloatingDefinition {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
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
   * The coefficients of the main payment (before floor and cap). Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   */
  private final double[] _mainCoefficients;
  /**
   * The coefficients of the floor. Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   */
  private final double[] _floorCoefficients;
  /**
   * The coefficients of the cap. Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   */
  private final double[] _capCoefficients;

  /**
   * Constructor from all the details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param mainCoefficients The coefficients of the main payment (before floor and cap). Array of length 3.
   * @param floorCoefficients The coefficients of the floor. Array of length 3.
   * @param capCoefficients The coefficients of the cap. Array of length 3.
   */
  public CouponIborRatchetDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double[] mainCoefficients, final double[] floorCoefficients, final double[] capCoefficients) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    Validate.notNull(index, "index");
    Validate.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), _index.getCalendar());
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate);
    Validate.notNull(mainCoefficients, "Main coefficients");
    Validate.notNull(floorCoefficients, "Floor coefficients");
    Validate.notNull(capCoefficients, "Cap coefficients");
    Validate.isTrue(mainCoefficients.length == 3, "Requires 3 main coefficients");
    Validate.isTrue(floorCoefficients.length == 3, "Requires 3 floor coefficients");
    Validate.isTrue(capCoefficients.length == 3, "Requires 3 cap coefficients");
    _mainCoefficients = mainCoefficients;
    _floorCoefficients = floorCoefficients;
    _capCoefficients = capCoefficients;
  }

  /**
   * Gets the Ibor index of the instrument.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the start date of the fixing period.
   * @return The start date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the end date of the fixing period.
   * @return The end date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @return The accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the coefficients of the main payment (before floor and cap).
   * @return The coefficients of the main payment (before floor and cap).
   */
  public double[] getMainCoefficients() {
    return _mainCoefficients;
  }

  /**
   * Gets the coefficients of the floor.
   * @return The coefficients of the floor.
   */
  public double[] getFloorCoefficients() {
    return _floorCoefficients;
  }

  /**
   * Gets the coefficients of the cap.
   * @return The coefficients of the cap.
   */
  public double[] getCapCoefficients() {
    return _capCoefficients;
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitCouponIborRatchetDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponIborRatchetDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_capCoefficients);
    result = prime * result + Arrays.hashCode(_floorCoefficients);
    result = prime * result + Arrays.hashCode(_mainCoefficients);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponIborRatchetDefinition other = (CouponIborRatchetDefinition) obj;
    if (!Arrays.equals(_capCoefficients, other._capCoefficients)) {
      return false;
    }
    if (!Arrays.equals(_floorCoefficients, other._floorCoefficients)) {
      return false;
    }
    if (!Arrays.equals(_mainCoefficients, other._mainCoefficients)) {
      return false;
    }
    return true;
  }

  @Override
  public CouponIborRatchet toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final String discountingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDate());
    return new CouponIborRatchet(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), forwardCurveName, getIndex(), _mainCoefficients, _floorCoefficients, _capCoefficients);
  }

  @Override
  public Payment toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data, String... yieldCurveNames) {
    return null;
    //TODO: coupon with fixing!
  }

}
