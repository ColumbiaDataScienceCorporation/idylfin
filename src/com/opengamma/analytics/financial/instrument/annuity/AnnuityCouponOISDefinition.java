/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * A wrapper class for a AnnuityDefinition containing CouponOISDefinition.
 */
public class AnnuityCouponOISDefinition extends AnnuityCouponDefinition<CouponOISDefinition> {

  /** Empty array for array conversion of list */
  protected static final Coupon[] EMPTY_ARRAY_COUPON = new Coupon[0];

  /**
   * Constructor from a list of OIS coupons.
   * @param payments The coupons.
   */
  public AnnuityCouponOISDefinition(final CouponOISDefinition[] payments) {
    super(payments);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The total tenor of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The OIS generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorSwapFixedON generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.isStubShort(), generator.isFromEnd(),
        generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
    return AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The total tenor of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorSwapIborON generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getIndexIbor().getTenor(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
    return AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The OIS generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapFixedON generator,
      final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getLegsPeriod(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
    return AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapIborON generator,
      final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getIndexIbor().getTenor(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
    return AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer);
  }

  private static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final GeneratorSwapFixedON generator,
      final boolean isPayer) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponOISDefinition[] coupons = new CouponOISDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponOISDefinition.from(generator.getIndex(), settlementDate, endFixingPeriodDate[0], notionalSigned, generator.getPaymentLag());
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponOISDefinition.from(generator.getIndex(), endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, generator.getPaymentLag());
    }
    return new AnnuityCouponOISDefinition(coupons);
  }

  private static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final GeneratorSwapIborON generator,
      final boolean isPayer) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponOISDefinition[] coupons = new CouponOISDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponOISDefinition.from(generator.getIndexON(), settlementDate, endFixingPeriodDate[0], notionalSigned, generator.getPaymentLag());
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponOISDefinition.from(generator.getIndexON(), endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, generator.getPaymentLag());
    }
    return new AnnuityCouponOISDefinition(coupons);
  }

  @Override
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    ArgumentChecker.notNull(valZdt, "date");
    ArgumentChecker.notNull(indexFixingTS, "index fixing time series");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final List<Coupon> resultList = new ArrayList<Coupon>();
    final CouponOISDefinition[] payments = getPayments();
    ZonedDateTime valZdtInPaymentZone = valZdt.withZoneSameInstant(payments[0].getPaymentDate().getZone());
    LocalDate valDate = valZdtInPaymentZone.toLocalDate();

    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!valDate.isAfter(payments[loopcoupon].getPaymentDate().toLocalDate())) {
        resultList.add(payments[loopcoupon].toDerivative(valZdt, indexFixingTS, yieldCurveNames));
      }
    }
    return new Annuity<Coupon>(resultList.toArray(EMPTY_ARRAY_COUPON));
  }

}
