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
package de.erichseifert.gral.plots.points;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;

import de.erichseifert.gral.plots.colors.SingleColor;
import de.erichseifert.gral.plots.settings.BasicSettingsStorage;
import de.erichseifert.gral.plots.settings.Key;
import de.erichseifert.gral.plots.settings.SettingChangeEvent;
import de.erichseifert.gral.plots.settings.SettingsListener;
import de.erichseifert.gral.util.Location;


/**
 * Abstract class implementing functions for the administration of settings.
 */
public abstract class AbstractPointRenderer extends BasicSettingsStorage
		implements PointRenderer, SettingsListener {
	/** Version id for serialization. */
	private static final long serialVersionUID = -408976260196287753L;

	/**
	 * Creates a new AbstractPointRenderer object with default shape and
	 * color.
	 */
	public AbstractPointRenderer() {
		addSettingsListener(this);

		setSettingDefault(SHAPE, new Rectangle2D.Double(-2.5, -2.5, 5.0, 5.0));
		setSettingDefault(COLOR, new SingleColor(Color.BLACK));

		setSettingDefault(VALUE_DISPLAYED, Boolean.FALSE);
		setSettingDefault(VALUE_COLUMN, 1);
		setSettingDefault(VALUE_LOCATION, Location.CENTER);
		setSettingDefault(VALUE_ALIGNMENT_X, 0.5);
		setSettingDefault(VALUE_ALIGNMENT_Y, 0.5);
		setSettingDefault(VALUE_ROTATION, 0.0);
		setSettingDefault(VALUE_DISTANCE, 1.0);
		setSettingDefault(VALUE_COLOR, new SingleColor(Color.BLACK));
		setSettingDefault(VALUE_FONT, Font.decode(null));

		setSettingDefault(ERROR_DISPLAYED, Boolean.FALSE);
		setSettingDefault(ERROR_COLUMN_TOP, 2);
		setSettingDefault(ERROR_COLUMN_BOTTOM, 3);
		setSettingDefault(ERROR_COLOR, new SingleColor(Color.BLACK));
		setSettingDefault(ERROR_SHAPE, new Line2D.Double(-2.0, 0.0, 2.0, 0.0));
		setSettingDefault(ERROR_STROKE, new BasicStroke(1f));
	}

	/**
	 * Invoked if a setting has changed.
	 * @param event Event containing information about the changed setting.
	 */
	public void settingChanged(SettingChangeEvent event) {
	}

	@Override
	protected <T> void setSetting(Key key, T value, boolean isDefault) {
		// Be nice and automatically convert colors to color mappers
		if (value instanceof Paint && (COLOR.equals(key) ||
				VALUE_COLOR.equals(key) || ERROR_COLOR.equals(key))) {
			super.setSetting(key, new SingleColor((Paint) value), isDefault);
		} else {
			super.setSetting(key, value, isDefault);
		}
	};

	/**
	 * Custom deserialization method.
	 * @param in Input stream.
	 * @throws ClassNotFoundException if a serialized class doesn't exist anymore.
	 * @throws IOException if there is an error while reading data from the
	 *         input stream.
	 */
	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		// Normal deserialization
		in.defaultReadObject();

		// Restore listeners
		addSettingsListener(this);
	}
}
