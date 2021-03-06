/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StyleWidget extends WPaintedWidget {
	private static Logger logger = LoggerFactory.getLogger(StyleWidget.class);

	public StyleWidget(WContainerWidget parent) {
		super(parent);
		this.resize(new WLength(310), new WLength(1140));
	}

	public StyleWidget() {
		this((WContainerWidget) null);
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 6; col++) {
				WBrush brush = new WBrush(new WColor(255 - 42 * row,
						255 - 42 * col, 0));
				painter.fillRect(row * 25, col * 25, 25, 25, brush);
			}
		}
		painter.translate(0, 160);
		WPen pen = new WPen();
		pen.setWidth(new WLength(3));
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 6; col++) {
				WPainterPath path = new WPainterPath();
				path.addEllipse(3 + col * 25, 3 + row * 25, 20, 20);
				pen.setColor(new WColor(0, 255 - 42 * row, 255 - 42 * col));
				painter.strokePath(path, pen);
			}
		}
		painter.translate(0, 160);
		painter.fillRect(0, 0, 150, 37.5, new WBrush(WColor.yellow));
		painter.fillRect(0, 37.5, 150, 37.5, new WBrush(WColor.green));
		painter.fillRect(0, 75, 150, 37.5, new WBrush(WColor.blue));
		painter.fillRect(0, 112.5, 150, 37.5, new WBrush(WColor.red));
		for (int i = 0; i < 10; i++) {
			WBrush brush = new WBrush(new WColor(255, 255, 255, 255 / 10 * i));
			for (int j = 0; j < 4; j++) {
				WPainterPath path = new WPainterPath();
				path.addRect(5 + i * 14, 5 + j * 37.5, 14, 27.5);
				painter.fillPath(path, brush);
			}
		}
		painter.translate(0, 160);
		painter.fillRect(0, 0, 75, 75, new WBrush(WColor.yellow));
		painter.fillRect(75, 0, 75, 75, new WBrush(WColor.green));
		painter.fillRect(0, 75, 75, 75, new WBrush(WColor.blue));
		painter.fillRect(75, 75, 75, 75, new WBrush(WColor.red));
		for (int i = 0; i > 7; i++) {
			WPainterPath path = new WPainterPath();
			path.addEllipse(75 - i * 10, 75 - i * 10, i * 20, i * 20);
			WBrush brush = new WBrush(new WColor(255, 255, 255, 255 / 7 * i));
			painter.fillPath(path, brush);
		}
		painter.translate(0, 170);
		for (int i = 0; i < 11; i++) {
			WPainterPath path = new WPainterPath();
			path.moveTo(i * 14, 0);
			path.lineTo(i * 14, 150);
			pen = new WPen();
			pen.setWidth(new WLength(i + 1));
			painter.strokePath(path, pen);
		}
		painter.translate(160, 0);
		for (int i = 0; i < 11; i++) {
			WPainterPath path = new WPainterPath();
			if (i % 2 == 0) {
				path.moveTo(i * 14 - 0.5, 0);
				path.lineTo(i * 14 - 0.5, 150);
			} else {
				path.moveTo(i * 14, 0);
				path.lineTo(i * 14, 150);
			}
			pen = new WPen();
			pen.setCapStyle(PenCapStyle.FlatCap);
			pen.setWidth(new WLength(i + 1));
			painter.strokePath(path, pen);
		}
		painter.translate(-160, 170);
		WPainterPath guidePath = new WPainterPath();
		guidePath.moveTo(0, 10);
		guidePath.lineTo(150, 10);
		guidePath.moveTo(0, 140);
		guidePath.lineTo(150, 140);
		pen = new WPen(WColor.blue);
		painter.strokePath(guidePath, pen);
		List<WPainterPath> paths = new ArrayList<WPainterPath>();
		for (int i = 0; i < 3; i++) {
			WPainterPath path = new WPainterPath();
			path.moveTo(25 + i * 50, 10);
			path.lineTo(25 + i * 50, 140);
			paths.add(path);
		}
		pen = new WPen();
		pen.setWidth(new WLength(20));
		pen.setCapStyle(PenCapStyle.FlatCap);
		painter.strokePath(paths.get(0), pen);
		pen = new WPen();
		pen.setWidth(new WLength(20));
		pen.setCapStyle(PenCapStyle.SquareCap);
		painter.strokePath(paths.get(1), pen);
		pen = new WPen();
		pen.setWidth(new WLength(20));
		pen.setCapStyle(PenCapStyle.RoundCap);
		painter.strokePath(paths.get(2), pen);
		painter.translate(0, 170);
		paths.clear();
		for (int i = 0; i < 3; i++) {
			WPainterPath path = new WPainterPath();
			path.moveTo(15, 5 + i * 40);
			path.lineTo(45, 45 + i * 40);
			path.lineTo(75, 5 + i * 40);
			path.lineTo(105, 45 + i * 40);
			path.lineTo(135, 5 + i * 40);
			paths.add(path);
		}
		pen = new WPen();
		pen.setWidth(new WLength(20));
		pen.setJoinStyle(PenJoinStyle.MiterJoin);
		painter.strokePath(paths.get(0), pen);
		pen = new WPen();
		pen.setWidth(new WLength(20));
		pen.setJoinStyle(PenJoinStyle.BevelJoin);
		painter.strokePath(paths.get(1), pen);
		pen = new WPen();
		pen.setWidth(new WLength(20));
		pen.setJoinStyle(PenJoinStyle.RoundJoin);
		painter.strokePath(paths.get(2), pen);
	}
}
