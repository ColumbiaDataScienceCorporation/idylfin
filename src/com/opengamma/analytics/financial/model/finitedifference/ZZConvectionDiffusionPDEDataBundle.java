/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Describes a partial differential for a function $V(t,x)$, with the initial condition $V(0,x) = f(x)$
 *  $\frac{\partial V}{\partial t} + a(t,x) \frac{\partial^2 V}{\partial x^2} + b(t,x) \frac{\partial V}{\partial x} + c(t,x)V = 0$
 *  @deprecated this has been split out into ParabolicPDECoefficients and intital conditions (which can be a Function1D or a double[]), and these
 *  together with the boundary conditions and the grid form the ParabolicPDEDataBundleNew which is passed to the PDE solver.
 */
@Deprecated
public class ZZConvectionDiffusionPDEDataBundle {

  private final Surface<Double, Double, Double> _a;
  private final Surface<Double, Double, Double> _b;
  private final Surface<Double, Double, Double> _c;

  private final Function1D<Double, Double> _initialCondition;

  public ZZConvectionDiffusionPDEDataBundle(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b, final Surface<Double, Double, Double> c,
      final Function1D<Double, Double> initialCondition) {
    Validate.notNull(a, "null a");
    Validate.notNull(b, "null b");
    Validate.notNull(c, "null c");
    Validate.notNull(initialCondition, "null initial Condition");
    _a = a;
    _b = b;
    _c = c;
    _initialCondition = initialCondition;
  }

  public double getA(final double t, final double x) {
    return _a.getZValue(t, x);
  }

  public double getB(final double t, final double x) {
    return _b.getZValue(t, x);
  }

  public double getC(final double t, final double x) {
    return _c.getZValue(t, x);
  }

  public double getInitialValue(final double x) {
    return _initialCondition.evaluate(x);
  }

  public ConvectionDiffusionPDE1DStandardCoefficients getCoefficients() {
    return new ConvectionDiffusionPDE1DStandardCoefficients(_a, _b, _c);
  }

  public Function1D<Double, Double> getInitialCondition() {
    return _initialCondition;
  }

}
