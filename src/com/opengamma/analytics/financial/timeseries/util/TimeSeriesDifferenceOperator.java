/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;

/**
 * 
 */
public class TimeSeriesDifferenceOperator extends Function1D<DoubleTimeSeries<?>, DoubleTimeSeries<?>> {

  @Override
  public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 1, "time series length must be > 1");
    final FastIntDoubleTimeSeries fastTS = ts.toFastIntDoubleTimeSeries();
    final int[] times = fastTS.timesArrayFast();
    final double[] values = fastTS.valuesArrayFast();
    final int n = times.length;
    final int[] differenceTimes = new int[n - 1];
    final double[] differenceValues = new double[n - 1];
    for (int i = 1; i < n; i++) {
      differenceTimes[i - 1] = times[i];
      differenceValues[i - 1] = values[i] - values[i - 1];
    }
    return new FastArrayIntDoubleTimeSeries(fastTS.getEncoding(), differenceTimes, differenceValues).toLocalDateDoubleTimeSeries();
  }
  
}
