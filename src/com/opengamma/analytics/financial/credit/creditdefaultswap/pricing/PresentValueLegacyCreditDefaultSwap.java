/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapIntegrationSchedule;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 *  Class containing methods for the valuation of a vanilla Legacy CDS
 */
public class PresentValueLegacyCreditDefaultSwap {

  // -------------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // Set the number of partitions to divide the timeline up into for the valuation of the contingent leg

  private static final int DEFAULT_N_POINTS = 30;
  private final int _numberOfIntegrationSteps;

  public PresentValueLegacyCreditDefaultSwap() {
    this(DEFAULT_N_POINTS);
  }

  public PresentValueLegacyCreditDefaultSwap(int numberOfIntegrationPoints) {
    _numberOfIntegrationSteps = numberOfIntegrationPoints;
  }

  // -------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Add a method to calc both the legs in one method (useful for performance reasons e.g. not computing survival probabilities and discount factors twice)
  // TODO : If valuationDate = adjustedMatDate - 1day have to be more careful in how the contingent leg integral is calculated
  // TODO : Fix the bug when val date is very close to mat date
  // TODO : Need to add the code for when the settlement date > 0 business days (just a discount factor)
  // TODO : Replace the while with a binary search function
  // TODO : Should build the cashflow schedules outside of the leg valuation routines to avoid repitition of calculations
  // TODO : Eventually replace the ISDACurve with a YieldCurve object (currently using ISDACurve built by RiskCare as this allows exact comparison with the ISDA model)
  // TODO : Replace the accrued schedule double with a ZonedDateTime object to make it consistent with other calculations
  // TODO : Tidy up the calculatePremiumLeg, valueFeeLegAccrualOnDefault and methods
  // TODO : Add the calculation for the settlement and stepin discount factors

  // -------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (with a hazard rate curve calibrated to market observed data)
  public double getPresentValueCreditDefaultSwap(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null

    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");

    // -------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, yieldCurve, hazardRateCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, yieldCurve, hazardRateCurve);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -(cds.getParSpread() / 10000.0) * presentValuePremiumLeg + presentValueContingentLeg;

    // -------------------------------------------------------------

    // If we require the clean price, then calculate the accrued interest and add this to the PV
    if (cds.getPriceType() == PriceType.CLEAN) {
      presentValue += calculateAccruedInterest(cds, yieldCurve, hazardRateCurve);
    }

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    // -------------------------------------------------------------

    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------  

  // Public method to calculate the par spread of a CDS at contract inception (with a hazard rate curve calibrated to market observed data)
  public double getParSpreadCreditDefaultSwap(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve field");

    // -------------------------------------------------------------

    double parSpread = 0.0;

    // -------------------------------------------------------------

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Check if the valuationDate equals the adjusted effective date (have to do this after the schedule is constructed)
    ArgumentChecker.isTrue(cds.getValuationDate().equals(cashflowSchedule.getAdjustedEffectiveDate(cds)), "Valuation Date should equal the adjusted effective date when computing par spreads");

    // -------------------------------------------------------------

    // Calculate the value of the premium leg
    double presentValuePremiumLeg = calculatePremiumLeg(cds, yieldCurve, hazardRateCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, yieldCurve, hazardRateCurve);

    // -------------------------------------------------------------

    // Calculate the par spread (NOTE : Returned value is in bps)
    if (Double.doubleToLongBits(presentValuePremiumLeg) == 0.0) {
      throw new IllegalStateException("Warning : The premium leg has a PV of zero - par spread cannot be computed");
    } else {
      parSpread = 10000.0 * presentValueContingentLeg / presentValuePremiumLeg;
    }

    // -------------------------------------------------------------

    return parSpread;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS (with a hazard rate curve calibrated to market observed data)
  // The code for the accrued calc has just been lifted from RiskCare's implementation for now because it exactly reproduces the ISDA model - will replace with a better model in due course
  private double calculatePremiumLeg(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Local variable definitions
    int startIndex = 0;
    int endIndex = 0;

    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedInterest = 0.0;

    // -------------------------------------------------------------

    // Construct a cashflow schedule object for the premium leg
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Construct a schedule object for the accrued leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the accrued payment integral)
    GenerateCreditDefaultSwapIntegrationSchedule accruedSchedule = new GenerateCreditDefaultSwapIntegrationSchedule();

    // Build the integration schedule for the calculation of the accrued leg 
    double[] accruedLegIntegrationSchedule = accruedSchedule.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, yieldCurve, hazardRateCurve);

    // Calculate the stepin time with the appropriate offset
    double offsetStepinTime = accruedSchedule.calculateCreditDefaultSwapOffsetStepinTime(cds, ACT_365);

    // -------------------------------------------------------------

    // Get the date on which we want to calculate the MtM
    ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the (adjusted) maturity date of the trade
    ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    // -------------------------------------------------------------

    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // If the valuation date is exactly the adjusted maturity date then simply return zero
    if (valuationDate.equals(adjustedMaturityDate)) {
      return 0.0;
    }

    // -------------------------------------------------------------

    // Determine where in the cashflow schedule the valuationDate is
    int startCashflowIndex = getCashflowIndex(cds, premiumLegSchedule, 1, 1);

    // -------------------------------------------------------------

    // Calculate the value of the remaining premium and accrual payments (due after valuationDate) 
    for (int i = startCashflowIndex; i < premiumLegSchedule.length; i++) {

      // Get the beginning and end dates of the current coupon
      ZonedDateTime accrualStart = premiumLegSchedule[i - 1];
      ZonedDateTime accrualEnd = premiumLegSchedule[i];

      // -------------------------------------------------------------

      // Calculate the time between the valuation date (time at which survival probability is unity) and the current cashflow
      double t = TimeCalculator.getTimeBetween(valuationDate, accrualEnd, ACT_365);

      // Calculate the discount factor at time t
      double discountFactor = yieldCurve.getDiscountFactor(t);

      // -------------------------------------------------------------

      // If protection starts at the beginning of the period ...
      if (cds.getProtectionStart()) {

        // ... Roll all but the last date back by 1/365 of a year 
        if (i < premiumLegSchedule.length - 1) {
          t -= cds.getProtectionOffset();
        }

        // This is a bit of a hack - need a more elegant way of dealing with the timing nuances
        if (i == 1) {
          accrualStart = accrualStart.minusDays(1);
        }

        // ... Roll the final maturity date forward by one day
        if (i == premiumLegSchedule.length - 1) {
          accrualEnd = accrualEnd.plusDays(1);
        }
      }

      // -------------------------------------------------------------

      // Compute the daycount fraction for the current accrual period
      double dcf = cds.getDayCountFractionConvention().getDayCountFraction(accrualStart, accrualEnd);

      // Calculate the survival probability at the modified time t
      double survivalProbability = hazardRateCurve.getSurvivalProbability(t);

      // Add this discounted cashflow to the running total for the value of the premium leg
      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;

      // -------------------------------------------------------------

      // Now calculate the accrued leg component if required (need to re-write this code)

      if (cds.getIncludeAccruedPremium()) {

        double stepinDiscountFactor = 1.0;

        startIndex = endIndex;

        while (accruedLegIntegrationSchedule[endIndex] < t) {
          ++endIndex;
        }

        presentValueAccruedInterest += valueFeeLegAccrualOnDefault(dcf, accruedLegIntegrationSchedule, yieldCurve, hazardRateCurve, startIndex, endIndex,
            offsetStepinTime, stepinDiscountFactor);
      }

      // -------------------------------------------------------------
    }

    // -------------------------------------------------------------

    return cds.getNotional() * (presentValuePremiumLeg + presentValueAccruedInterest);

    // -------------------------------------------------------------
  }

  //-------------------------------------------------------------------------------------------------

  // Need to re-write this code completely!!
  private double valueFeeLegAccrualOnDefault(final double amount, final double[] timeline, final ISDACurve yieldCurve, final HazardRateCurve hazardRateCurve, final int startIndex,
      final int endIndex, final double stepinTime, final double stepinDiscountFactor) {

    final double[] timePoints = timeline; //timeline.getTimePoints();

    final double startTime = timePoints[startIndex];
    final double endTime = timePoints[endIndex];
    final double subStartTime = stepinTime > startTime ? stepinTime : startTime;
    final double accrualRate = amount / (endTime - startTime);

    double t0, t1, dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, lambdaFwdRate, valueForTimeStep, value;

    t0 = subStartTime - startTime + 0.5 * (1.0 / 365.0); //HALF_DAY_ACT_365F;
    survival0 = hazardRateCurve.getSurvivalProbability(subStartTime);

    double PRICING_TIME = 0.0;

    discount0 = startTime < stepinTime || startTime < PRICING_TIME ? stepinDiscountFactor : yieldCurve.getDiscountFactor(timePoints[startIndex]); //discountFactors[startIndex];

    value = 0.0;

    for (int i = startIndex + 1; i <= endIndex; ++i) {

      if (timePoints[i] <= stepinTime) {
        continue;
      }

      t1 = timePoints[i] - startTime + 0.5 * (1.0 / 365.0); //HALF_DAY_ACT_365F;
      dt = t1 - t0;

      survival1 = hazardRateCurve.getSurvivalProbability(timePoints[i]);
      discount1 = yieldCurve.getDiscountFactor(timePoints[i]); //discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      lambdaFwdRate = lambda + fwdRate + 1.0e-50;
      valueForTimeStep = lambda * accrualRate * survival0 * discount0
          * (((t0 + 1.0 / lambdaFwdRate) / lambdaFwdRate) - ((t1 + 1.0 / lambdaFwdRate) / lambdaFwdRate) * survival1 / survival0 * discount1 / discount0);

      value += valueForTimeStep;

      t0 = t1;
      survival0 = survival1;
      discount0 = discount1;
    }

    return value;
  }

  // -------------------------------------------------------------------------------------------------

  // If the cleanPrice flag is TRUE then this function is called to calculate the accrued interest between valuationDate and the previous coupon date 
  private double calculateAccruedInterest(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Assume the stepin date is the valuation date + 1 day (this is not business day adjusted)
    ZonedDateTime stepinDate = cds.getValuationDate().plusDays(1);

    // Determine where in the premium leg cashflow schedule the current valuation date is
    int startCashflowIndex = getCashflowIndex(cds, premiumLegSchedule, 0, 1);

    // Get the date of the last coupon before the current valuation date
    ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1];

    // Compute the amount of time between previousPeriod and stepinDate
    double dcf = cds.getDayCountFractionConvention().getDayCountFraction(previousPeriod, stepinDate);

    // Calculate the accrued interest gained in this period of time
    double accruedInterest = (cds.getParSpread() / 10000.0) * dcf * cds.getNotional();

    return accruedInterest;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to determine where in the premium leg cashflow schedule the valuation date is
  private int getCashflowIndex(LegacyCreditDefaultSwapDefinition cds, ZonedDateTime[] premiumLegSchedule, final int startIndex, final int deltaDays) {

    int counter = startIndex;

    // Determine where in the cashflow schedule the valuationDate is
    while (!cds.getValuationDate().isBefore(premiumLegSchedule[counter].minusDays(deltaDays))) {
      counter++;
    }

    return counter;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the contingent leg (replicates the calculation in the ISDA model)
  private double calculateContingentLeg(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Local variable definitions
    double presentValueContingentLeg = 0.0;

    // -------------------------------------------------------------

    // Construct an integration schedule object for the contingent leg
    GenerateCreditDefaultSwapIntegrationSchedule contingentLegSchedule = new GenerateCreditDefaultSwapIntegrationSchedule();

    // Build the integration schedule for the calculation of the contingent leg 
    double[] contingentLegIntegrationSchedule = contingentLegSchedule.constructCreditDefaultSwapContingentLegIntegrationSchedule(cds, yieldCurve, hazardRateCurve);

    // -------------------------------------------------------------

    // Get the survival probability at the first point in the integration schedule
    double survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[0]);

    // Get the discount factor at the first point in the integration schedule
    double discountFactor = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[0]);

    // -------------------------------------------------------------

    // Loop over each of the points in the integration schedule
    for (int i = 1; i < contingentLegIntegrationSchedule.length; ++i) {

      // Calculate the time between adjacent points in the integration schedule
      double deltat = contingentLegIntegrationSchedule[i] - contingentLegIntegrationSchedule[i - 1];

      // Set the probability of survival up to the previous point in the integration schedule
      double survivalProbabilityPrevious = survivalProbability;

      // Set the discount factor up to the previous point in the integration schedule
      double discountFactorPrevious = discountFactor;

      // Get the survival probability at this point in the integration schedule
      survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[i]);

      // Get the discount factor at this point in the integration schedule
      discountFactor = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[i]);

      // Calculate the forward hazard rate over the interval deltat (assumes the hazard rate is constant over this period)
      double hazardRate = Math.log(survivalProbabilityPrevious / survivalProbability) / deltat;

      // Calculate the forward interest rate over the interval deltat (assumes the interest rate is constant over this period)
      double interestRate = Math.log(discountFactorPrevious / discountFactor) / deltat;

      // Calculate the contribution of the interval deltat to the overall contingent leg integral
      presentValueContingentLeg += (hazardRate / (hazardRate + interestRate)) * (1.0 - Math.exp(-(hazardRate + interestRate) * deltat)) * survivalProbabilityPrevious * discountFactorPrevious;
    }

    // -------------------------------------------------------------

    return cds.getNotional() * (1 - cds.getRecoveryRate()) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the contingent leg of a CDS (with a hazard rate curve calibrated to market observed data) - Currently not used but this is a more elegant calc than ISDA
  private double calculateContingentLegOld(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Construct a schedule generation object (to access the adjusted maturity date method)
    GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Get the date when protection begins
    ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the date when protection ends
    ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    // -------------------------------------------------------------

    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // If the valuation date is exactly the adjusted maturity date then simply return zero
    if (valuationDate.equals(adjustedMaturityDate)) {
      return 0.0;
    }

    // -------------------------------------------------------------

    double presentValueContingentLeg = 0.0;

    // -------------------------------------------------------------

    // Calculate the partition of the time axis for the calculation of the integral in the contingent leg

    // The period of time for which protection is provided
    double protectionPeriod = TimeCalculator.getTimeBetween(valuationDate, adjustedMaturityDate.plusDays(1), /*cds.getDayCountFractionConvention()*/ACT_365);

    // Given the protection period, how many partitions should it be divided into
    int numberOfPartitions = (int) (_numberOfIntegrationSteps * protectionPeriod + 0.5);

    // The size of the time increments in the calculation of the integral
    double epsilon = protectionPeriod / numberOfPartitions;

    // -------------------------------------------------------------

    // Calculate the integral for the contingent leg (note the limits of the loop)
    for (int k = 1; k <= numberOfPartitions; k++) {

      double t = k * epsilon;
      double tPrevious = (k - 1) * epsilon;

      double discountFactor = yieldCurve.getDiscountFactor(t);

      double survivalProbability = hazardRateCurve.getSurvivalProbability(t);
      double survivalProbabilityPrevious = hazardRateCurve.getSurvivalProbability(tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    // -------------------------------------------------------------

    return cds.getNotional() * (1.0 - cds.getRecoveryRate()) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------
}
