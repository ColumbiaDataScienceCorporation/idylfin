/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2012 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <michael[at]erichseifert.de>
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erichseifert.gral.plots.lines;

import java.awt.Shape;
import java.util.List;

import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.plots.DataPoint;
import de.erichseifert.gral.plots.settings.Key;
import de.erichseifert.gral.plots.settings.SettingsStorage;


/**
 * <p>Interface that provides functions for rendering a line in two dimensional
 * space.</p>
 * <p>Functionality includes:<p>
 * <ul>
 *   <li>Punching data points out of the line's shape</li>
 *   <li>Administration of settings</li>
 * </ul>
 */
public interface LineRenderer extends SettingsStorage {
	/** Key for specifying the {@link java.awt.Stroke} instance to be used to
	define the line shape. */
	Key STROKE = new Key("line.stroke"); //$NON-NLS-1$
	/** Key for specifying a {@link Number} value for the gap between the line
	and a point. If the gap value is equal to or smaller than 0 no gap will be
	used. */
	Key GAP = new Key("line.gap.size"); //$NON-NLS-1$
	/** Key for specifying a {@link Boolean} value which decides whether the
	gaps should have rounded corners. */
	Key GAP_ROUNDED = new Key("line.gap.rounded"); //$NON-NLS-1$
	/** Key for specifying the {@link java.awt.Paint} instance to be used to
	paint the line shape. */
	Key COLOR = new Key("line.color"); //$NON-NLS-1$

	/**
	 * Returns the geometric shape for this line.
	 * @param points Points used for creating the line.
	 * @return Geometric shape for this line.
	 */
	Shape getLineShape(List<DataPoint> points);

	/**
	 * Returns a graphical representation for the line defined by
	 * {@code points}.
	 * @param points Points to be used for creating the line.
	 * @param shape Geometric shape for this line.
	 * @return Representation of the line.
	 */
	Drawable getLine(List<DataPoint> points, Shape shape);
}
