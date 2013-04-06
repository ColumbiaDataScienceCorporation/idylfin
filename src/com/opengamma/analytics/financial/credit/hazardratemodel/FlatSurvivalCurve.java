/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.hazardratemodel;

/**
 * Class for the construction of flat survival curves (mostly used for testing purposes - will remove this code in due course)
 */
public class FlatSurvivalCurve {

  // ------------------------------------------------------------------------

  // Private (final) member variables

  // Flat (across the term structure) hazard rate value
  private final double _flatHazardRate;

  // ------------------------------------------------------------------------

  // Default FlatSurvivalCurve constructor (not very useful)
  public FlatSurvivalCurve() {

    _flatHazardRate = 0.0;
  }

  // ------------------------------------------------------------------------

  // FlatSurvivalCurve constructor
  public FlatSurvivalCurve(final double premiumLegCoupon, final double recoveryRate) {

    _flatHazardRate = (premiumLegCoupon / 10000.0) / (1 - recoveryRate);
  }

  // ------------------------------------------------------------------------

  // Public member function to get a survival probability from a flat hazard rate curve
  public double getSurvivalProbability(double hazardRate, double t) {

    return Math.exp(-hazardRate * t);
  }

  // ------------------------------------------------------------------------

  // Public accessor method to return the flat hazard rate
  public double getFlatHazardRate() {
    return _flatHazardRate;
  }

  // ------------------------------------------------------------------------
}
