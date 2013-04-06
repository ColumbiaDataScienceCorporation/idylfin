/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.generator;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.market.description.IMarketBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of the curves 
 * (operation on the continuously-compounded zero-coupon rates)  produced by the array of generators. 
 * The generated curve is a YieldAndDiscountAddZeroSpreadCurve.
 */
public class GeneratorCurveAddYield extends GeneratorYDCurve {

  /**
   * The array of generators describing the different parts of the spread curve.
   */
  private final GeneratorYDCurve[] _generators;
  /**
   * If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   */
  private final boolean _substract;
  /**
   * The number of generators.
   */
  private final int _nbGenerators;

  /**
   * Constructor.
   * @param generators The array of constructors for the component curves.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   */
  public GeneratorCurveAddYield(GeneratorYDCurve[] generators, boolean substract) {
    ArgumentChecker.notNull(generators, "Generators");
    _generators = generators;
    _nbGenerators = generators.length;
    _substract = substract;
  }

  @Override
  public int getNumberOfParameter() {
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      nbParam += _generators[loopgen].getNumberOfParameter();
    }
    return nbParam;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] x) {
    ArgumentChecker.isTrue(x.length == getNumberOfParameter(), "Incorrect number of parameters");
    YieldAndDiscountCurve[] underlyingCurves = new YieldAndDiscountCurve[_nbGenerators];
    int index = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      double[] paramCurve = Arrays.copyOfRange(x, index, index + _generators[loopgen].getNumberOfParameter());
      index += _generators[loopgen].getNumberOfParameter();
      underlyingCurves[loopgen] = _generators[loopgen].generateCurve(name + "-" + loopgen, paramCurve);
    }
    return new YieldAndDiscountAddZeroSpreadCurve(name, _substract, underlyingCurves);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, IMarketBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

  /**
   * All generator but the last should be already in their final form and with a known number of parameters.
   * The number of data corresponding to each known generator is eliminated and only the last part is used to create the final generator version.
   * If several generators had a unknown number of parameters, it would be unclear which instrument correspond to which generator.
   * @param data The array of instrument used to construct the curve.
   * @return The final generator.
   */
  @Override
  public GeneratorYDCurve finalGenerator(Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    GeneratorYDCurve[] finalGenerator = new GeneratorYDCurve[_nbGenerators];
    int nbDataUsed = 0;
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators - 1; loopgen++) {
      finalGenerator[loopgen] = _generators[loopgen];
      nbParam = _generators[loopgen].getNumberOfParameter();
      nbDataUsed += nbParam;
    }
    InstrumentDerivative[] instrumentsLast = new InstrumentDerivative[instruments.length - nbDataUsed + 1];
    instrumentsLast[0] = instruments[nbDataUsed - (nbParam + 1) / 2]; // For the anchor.
    System.arraycopy(instruments, nbDataUsed, instrumentsLast, 1, instruments.length - nbDataUsed);
    finalGenerator[_nbGenerators - 1] = _generators[_nbGenerators - 1].finalGenerator(instrumentsLast);
    return new GeneratorCurveAddYield(finalGenerator, _substract);
  }

  @Override
  public double[] initialGuess(double[] rates) {
    double[] guess = new double[rates.length];
    int nbDataUsed = 0;
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      nbParam = _generators[loopgen].getNumberOfParameter();
      double[] tmp = new double[nbParam];
      System.arraycopy(rates, nbDataUsed, tmp, 0, nbParam);
      System.arraycopy(_generators[loopgen].initialGuess(tmp), 0, guess, nbDataUsed, nbParam);
      nbDataUsed += nbParam;
    }
    return guess;
  }

}
