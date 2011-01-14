/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A class that specifies a CSS length.
 * <p>
 * 
 * The class combines a value with a unit. There is a special value <i>auto</i>
 * which has a different meaning depending on the context.
 */
public class WLength {
	/**
	 * The unit.
	 */
	public enum Unit {
		/**
		 * The relative font size.
		 */
		FontEm,
		/**
		 * The height of an &apos;x&apos; in the font.
		 */
		FontEx,
		/**
		 * Pixel, relative to canvas resolution.
		 */
		Pixel,
		/**
		 * Inche.
		 */
		Inch,
		/**
		 * Centimeter.
		 */
		Centimeter,
		/**
		 * Millimeter.
		 */
		Millimeter,
		/**
		 * Point (1/72 Inch).
		 */
		Point,
		/**
		 * Pica (12 Point).
		 */
		Pica,
		/**
		 * Percentage (meaning context-sensitive).
		 */
		Percentage;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates an &apos;auto&apos; length.
	 * <p>
	 * Specifies an &apos;auto&apos; length.
	 * <p>
	 * 
	 * @see WLength#Auto
	 */
	public WLength() {
		this.auto_ = true;
		this.unit_ = WLength.Unit.Pixel;
		this.value_ = -1;
	}

	/**
	 * Creates a length by parsing the argument as a css length string.
	 * <p>
	 * Only a combination of a value and a unit is supported. If the string is
	 * an illegal css length an exception is thrown.
	 */
	public WLength(String s) {
		this.auto_ = false;
		String end = null;
		{
			Matcher matcher = StringUtils.FLOAT_PATTERN.matcher(s);
			this.value_ = 0.0;
			if (matcher.find()) {
				end = s.substring(matcher.end());
				this.value_ = Double.parseDouble(matcher.group().trim());
			}
		}
		;
		if (s == end) {
			throw new WtException(
					"WLength: Missing value in the css length string '" + s
							+ "'.");
		}
		String unit = end;
		unit = unit.trim();
		if (unit.equals("em")) {
			this.unit_ = WLength.Unit.FontEm;
		} else {
			if (unit.equals("ex")) {
				this.unit_ = WLength.Unit.FontEx;
			} else {
				if (unit.length() == 0 || unit.equals("px")) {
					this.unit_ = WLength.Unit.Pixel;
				} else {
					if (unit.equals("in")) {
						this.unit_ = WLength.Unit.Inch;
					} else {
						if (unit.equals("cm")) {
							this.unit_ = WLength.Unit.Centimeter;
						} else {
							if (unit.equals("mm")) {
								this.unit_ = WLength.Unit.Millimeter;
							} else {
								if (unit.equals("pt")) {
									this.unit_ = WLength.Unit.Point;
								} else {
									if (unit.equals("pc")) {
										this.unit_ = WLength.Unit.Pica;
									} else {
										if (unit.equals("%")) {
											this.unit_ = WLength.Unit.Percentage;
										} else {
											throw new WtException(
													"WLength: Illegal unit '"
															+ unit
															+ "' in the css length string.");
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a length with value and unit.
	 * <p>
	 * This constructor will also provide the implicit conversion between a
	 * double and {@link WLength}, using a pixel unit.
	 */
	public WLength(double value, WLength.Unit unit) {
		this.auto_ = false;
		this.unit_ = unit;
		this.value_ = value;
	}

	/**
	 * Creates a length with value and unit.
	 * <p>
	 * Calls {@link #WLength(double value, WLength.Unit unit) this(value,
	 * WLength.Unit.Pixel)}
	 */
	public WLength(double value) {
		this(value, WLength.Unit.Pixel);
	}

	/**
	 * Creates a length with value and unit.
	 * <p>
	 * This constructor will also provide the implicit conversion between an int
	 * and {@link WLength}, using a pixel unit.
	 */
	public WLength(int value, WLength.Unit unit) {
		this.auto_ = false;
		this.unit_ = unit;
		this.value_ = (double) value;
	}

	/**
	 * Creates a length with value and unit.
	 * <p>
	 * Calls {@link #WLength(int value, WLength.Unit unit) this(value,
	 * WLength.Unit.Pixel)}
	 */
	public WLength(int value) {
		this(value, WLength.Unit.Pixel);
	}

	/**
	 * Creates a length with value and unit.
	 * <p>
	 * This constructor will also provide the implicit conversion between a long
	 * and {@link WLength}, using a pixel unit.
	 */
	public WLength(long value, WLength.Unit unit) {
		this.auto_ = false;
		this.unit_ = unit;
		this.value_ = (double) value;
	}

	/**
	 * Creates a length with value and unit.
	 * <p>
	 * Calls {@link #WLength(long value, WLength.Unit unit) this(value,
	 * WLength.Unit.Pixel)}
	 */
	public WLength(long value) {
		this(value, WLength.Unit.Pixel);
	}

	/**
	 * Returns whether the length is &apos;auto&apos;.
	 * <p>
	 * 
	 * @see WLength#WLength()
	 * @see WLength#Auto
	 */
	public boolean isAuto() {
		return this.auto_;
	}

	/**
	 * Returns the value.
	 * <p>
	 * 
	 * @see WLength#getUnit()
	 */
	public double getValue() {
		return this.value_;
	}

	/**
	 * Returns the unit.
	 * <p>
	 * 
	 * @see WLength#getValue()
	 */
	public WLength.Unit getUnit() {
		return this.unit_;
	}

	/**
	 * Returns the CSS text.
	 */
	public String getCssText() {
		if (this.auto_) {
			return "auto";
		} else {
			return String.valueOf(this.value_)
					+ unitText[this.unit_.getValue()];
		}
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	public boolean equals(WLength other) {
		return this.auto_ == other.auto_ && this.unit_ == other.unit_
				&& this.value_ == other.value_;
	}

	/**
	 * Returns the (approximate) length in pixels.
	 * <p>
	 * When the length {@link WLength#isAuto() isAuto()}, 0 is returned,
	 * otherwise the approximate length in pixels.
	 */
	public double toPixels(double fontSize) {
		if (this.auto_) {
			return 0;
		} else {
			if (this.unit_ == WLength.Unit.FontEm) {
				return this.value_ * fontSize;
			} else {
				if (this.unit_ == WLength.Unit.FontEx) {
					return this.value_ * fontSize / 2.0;
				} else {
					if (this.unit_ == WLength.Unit.Percentage) {
						return this.value_ * fontSize / 100.0;
					} else {
						return this.value_
								* unitFactor[this.unit_.getValue() - 2];
					}
				}
			}
		}
	}

	/**
	 * Returns the (approximate) length in pixels.
	 * <p>
	 * Returns {@link #toPixels(double fontSize) toPixels(16.0)}
	 */
	public final double toPixels() {
		return toPixels(16.0);
	}

	private boolean auto_;
	private WLength.Unit unit_;
	private double value_;
	private static String[] unitText = { "em", "ex", "px", "in", "cm", "mm",
			"pt", "pc", "%" };
	private static final double pxPerPt = 4.0 / 3.0;
	private static double[] unitFactor = { 1, 72 * pxPerPt,
			72 / 2.54 * pxPerPt, 72 / 25.4 * pxPerPt, pxPerPt, 12 * pxPerPt };

	static WLength multiply(WLength l, double s) {
		return new WLength(l.getValue() * s, l.getUnit());
	}

	static WLength multiply(double s, WLength l) {
		return WLength.multiply(l, s);
	}

	static WLength divide(WLength l, double s) {
		return WLength.multiply(l, 1 / s);
	}

	/**
	 * An &apos;auto&apos; length.
	 * <p>
	 * 
	 * @see WLength#WLength()
	 */
	public static WLength Auto = new WLength();
}
