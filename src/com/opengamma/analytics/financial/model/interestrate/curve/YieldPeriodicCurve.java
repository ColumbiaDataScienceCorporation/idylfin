/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * The implementation of a YieldAndDiscount curve where the curve is stored with maturities and periodically-compounded rates.
 */
public class YieldPeriodicCurve extends YieldAndDiscountCurve {

  /**
   * The curve storing the required data in the periodic-compounded convention.
   */
  private final DoublesCurve _curve;
  /**
   * The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   */
  private final int _compoundingPeriodsPerYear;

  /** 
   * Constructor.
   * @param name The curve name.
   * @param compoundingPeriodsPerYear The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   * @param yieldCurve Curve containing periodically-compounded rates against maturities. Rates are unitless (eg 0.02 for two percent) and maturities are in years.
   */
  public YieldPeriodicCurve(final String name, final int compoundingPeriodsPerYear, final DoublesCurve yieldCurve) {
    super(name);
    ArgumentChecker.notNull(yieldCurve, "Curve");
    _curve = yieldCurve;
    _compoundingPeriodsPerYear = compoundingPeriodsPerYear;
  }

  /**
   * Builder from a DoublesCurve using the name of the DoublesCurve as the name of the YieldCurve.
   * @param compoundingPeriodsPerYear The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   * @param yieldCurve The underlying curve based on yields (periodically-compounded).
   * @return The yield curve.
   */
  public static YieldPeriodicCurve from(final int compoundingPeriodsPerYear, final DoublesCurve yieldCurve) {
    ArgumentChecker.notNull(yieldCurve, "Curve");
    return new YieldPeriodicCurve(yieldCurve.getName(), compoundingPeriodsPerYear, yieldCurve);
  }

  /**
   * Builder of an interpolated discount factor curve from yields (continuously compounded).
   * @param nodePoints The node points for the interpolated curve.
   * @param yields The yields (cc) at the node points.
   * @param compoundingPeriodsPerYear The number of composition periods per year for the storage curve (1 for annual, 2 for semi-annual, etc.).
   * @param interpolator The period yield interpolator.
   * @param name The curve name.
   * @return The periodic yield curve.
   */
  public static YieldPeriodicCurve fromYieldsInterpolated(final double[] nodePoints, final double[] yields, final int compoundingPeriodsPerYear, final Interpolator1D interpolator, final String name) {
    int nbYields = yields.length;
    ArgumentChecker.isTrue(nodePoints.length == nbYields, "Yields array of incorrect length");
    double[] yieldPeriodic = new double[nbYields];
    for (int loopy = 0; loopy < nbYields; loopy++) {
      InterestRate continous = new ContinuousInterestRate(yields[loopy]);
      yieldPeriodic[loopy] = continous.toPeriodic(compoundingPeriodsPerYear).getRate();
    }
    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(nodePoints, yieldPeriodic, interpolator, false);
    return new YieldPeriodicCurve(name, compoundingPeriodsPerYear, curve);
  }

  @Override
  public double getInterestRate(final Double time) {
    double rate = _curve.getYValue(time);
    return _compoundingPeriodsPerYear * Math.log(1 + rate / _compoundingPeriodsPerYear);
  }

  @Override
  public double getDiscountFactor(final double t) {
    double rate = _curve.getYValue(t);
    return Math.pow(1 + rate / _compoundingPeriodsPerYear, -_compoundingPeriodsPerYear * t);
  }

  @Override
  public double getPeriodicInterestRate(final double t, final int compoundingPeriodsPerYear) {
    if (compoundingPeriodsPerYear == _compoundingPeriodsPerYear) {
      _curve.getYValue(t);
    }
    InterestRate rc = new PeriodicInterestRate(_curve.getYValue(t), _compoundingPeriodsPerYear);
    // Implementation note: rate in the composition of the storage.
    InterestRate rq = rc.toPeriodic(compoundingPeriodsPerYear);
    return rq.getRate();
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double t) {
    double rp = _curve.getYValue(t);
    //    double rc = _compoundingPeriodsPerYear * Math.log(1 + rp / _compoundingPeriodsPerYear);
    double rcBar = 1.0;
    double rpBar = 1.0 / (1 + rp / _compoundingPeriodsPerYear) * rcBar;
    Double[] drpdp = _curve.getYValueParameterSensitivity(t);
    double[] pBar = new double[drpdp.length];
    for (int loopp = 0; loopp < drpdp.length; loopp++) {
      pBar[loopp] = drpdp[loopp] * rpBar;
    }
    return pBar;
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.size();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<String>();
  }

  /**
   * Gets the underlying curve. 
   * @return The curve.
   */
  public Curve<Double, Double> getCurve() {
    return _curve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final YieldPeriodicCurve other = (YieldPeriodicCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
