/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Description of an bond future option with up-front margin security.
 */
public class BondFutureOptionPremiumSecurity implements InstrumentDerivative {

  /**
   * Underlying future security.
   */
  private final BondFuture _underlyingFuture;
  /**
   * Expiration date.
   */
  private final double _expirationTime;
  /**
   * Cap (true) / floor (false) flag.
   */
  private final boolean _isCall;
  /**
   * Strike price.
   */
  private final double _strike;
  /**
   * The discounting curve name.
   */
  private final String _discountingCurveName;

  /**
   * Constructor of the option future from the details.
   * @param underlyingFuture The underlying future security.
   * @param expirationTime The time (in year) to expiration.
   * @param strike The option strike.
   * @param isCall The cap (true) / floor (false) flag.
   */
  public BondFutureOptionPremiumSecurity(final BondFuture underlyingFuture, final double expirationTime, final double strike, final boolean isCall) {
    Validate.notNull(underlyingFuture, "underlying future");
    this._underlyingFuture = underlyingFuture;
    this._expirationTime = expirationTime;
    this._strike = strike;
    _isCall = isCall;
    _discountingCurveName = underlyingFuture.getDiscountingCurveName();
  }

  /**
   * Gets the underlying future security.
   * @return The underlying future security.
   */
  public BondFuture getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the expiration date.
   * @return The expiration date.
   */
  public double getExpirationTime() {
    return _expirationTime;
  }

  /**
   * Gets the cap (true) / floor (false) flag.
   * @return The cap/floor flag.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the option strike.
   * @return The option strike.
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * The future option currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingFuture.getCurrency();
  }

  /**
   * Gets the discounting curve name.
   * @return The discounting curve name.
   */
  public String getDiscountingCurveName() {
    return _discountingCurveName;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondFutureOptionPremiumSecurity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFutureOptionPremiumSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _discountingCurveName.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_expirationTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCall ? 1231 : 1237);
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingFuture.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondFutureOptionPremiumSecurity other = (BondFutureOptionPremiumSecurity) obj;
    if (!ObjectUtils.equals(_discountingCurveName, other._discountingCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_expirationTime) != Double.doubleToLongBits(other._expirationTime)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingFuture, other._underlyingFuture)) {
      return false;
    }
    return true;
  }

}
