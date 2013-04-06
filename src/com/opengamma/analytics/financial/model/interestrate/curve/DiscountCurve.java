/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DiscountCurve extends YieldAndDiscountCurve {

  /**
   * The curve storing the required data as discount factors.
   */
  private final DoublesCurve _curve;

  /**
   * Constructor from a curve containing the discount factors.
   * @param name The discount curve name.
   * @param discountFactorCurve The underlying curve.
   */
  public DiscountCurve(final String name, final DoublesCurve discountFactorCurve) {
    super(name);
    ArgumentChecker.notNull(discountFactorCurve, "Curve");
    _curve = discountFactorCurve;
  }

  /**
   * Builder from a DoublesCurve using the name of the DoublesCurve as the name of the DiscountCurve.
   * @param discountFactorCurve The underlying curve based on discount factors.
   * @return The discount curve.
   */
  public static DiscountCurve from(final DoublesCurve discountFactorCurve) {
    ArgumentChecker.notNull(discountFactorCurve, "Curve");
    return new DiscountCurve(discountFactorCurve.getName(), discountFactorCurve);
  }

  /**
   * Builder of an interpolated discount factor curve from yields (continuously compounded).
   * @param nodePoints The node points for the interpolated curve.
   * @param yields The yields (cc) at the node points.
   * @param interpolator The discount factors interpolator.
   * @param name The curve name.
   * @return The discount curve.
   */
  public static DiscountCurve fromYieldsInterpolated(final double[] nodePoints, final double[] yields, final Interpolator1D interpolator, final String name) {
    int nbYields = yields.length;
    ArgumentChecker.isTrue(nodePoints.length == nbYields, "Yields array of incorrect length");
    double[] discountFactor = new double[nbYields];
    for (int loopy = 0; loopy < nbYields; loopy++) {
      discountFactor[loopy] = Math.exp(-nodePoints[loopy] * yields[loopy]);
    }
    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(nodePoints, discountFactor, interpolator, false);
    return new DiscountCurve(name, curve);
  }

  @Override
  public double getInterestRate(final Double time) {
    double eps = 1.0E-6;
    if (Math.abs(time) > eps) {
      return -Math.log(getDiscountFactor(time)) / time;
    }
    // Implementation note: if time too close to 0, compute the limit for t->0.
    double dfP = getDiscountFactor(time + eps);
    double df = getDiscountFactor(time);
    return (df - dfP) / (eps * df);
  }

  @Override
  public double getDiscountFactor(final double t) {
    return _curve.getYValue(t);
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double time) {
    Double[] dfSensitivity = _curve.getYValueParameterSensitivity(time);
    double[] rSensitivity = new double[dfSensitivity.length];
    double df = getDiscountFactor(time);
    for (int loopp = 0; loopp < dfSensitivity.length; loopp++) {
      rSensitivity[loopp] = -dfSensitivity[loopp] / (time * df);
    }
    return rSensitivity;
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
    final DiscountCurve other = (DiscountCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

}
