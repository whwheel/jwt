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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for popup widgets.
 * <p>
 * 
 * A popup widget anchors to another widget, for which it usually provides
 * additional information or assists in editing, etc...
 * <p>
 * The popup widget will position itself relative to the anchor widget by taking
 * into account available space, and switching sides if necessary to fit the
 * widget into the current window. For example, a vertically anchored widget
 * will by default be a &quot;drop-down&quot;, positioning itself under the
 * anchor widget, but it may also choose to position itself above the anchor
 * widget if space is lacking below.
 */
public class WPopupWidget extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WPopupWidget.class);

	/**
	 * Constructor.
	 * <p>
	 * You need to pass in a widget that provides the main contents of the
	 * widget (e.g. a {@link WTemplate} or {@link WContainerWidget}).
	 * <p>
	 * Unlike other widgets, a popup widget does not need a parent widget (it
	 * acts like a pseudo top-level widget), but it can be given a parent object
	 * which is used to scope its lifetime.
	 */
	public WPopupWidget(WWidget impl, WObject parent) {
		super();
		this.fakeParent_ = null;
		this.anchorWidget_ = null;
		this.orientation_ = Orientation.Vertical;
		this.transient_ = false;
		this.autoHideDelay_ = 0;
		this.hidden_ = new Signal(this);
		this.shown_ = new Signal(this);
		this.jsHidden_ = new JSignal(impl, "hidden");
		this.jsShown_ = new JSignal(impl, "shown");
		this.setImplementation(impl);
		if (parent != null) {
			parent.addChild(this);
		}
		WApplication.getInstance().addGlobalWidget(this);
		this.hide();
		this.setPopup(true);
		this.setPositionScheme(PositionScheme.Absolute);
		this.jsHidden_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WPopupWidget.this.hide();
			}
		});
		this.jsShown_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WPopupWidget.this.show();
			}
		});
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WPopupWidget(WWidget impl, WObject parent) this(impl,
	 * (WObject)null)}
	 */
	public WPopupWidget(WWidget impl) {
		this(impl, (WObject) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		if (this.fakeParent_ != null) {
			this.fakeParent_.removeChild(this);
		}
		WApplication.getInstance().removeGlobalWidget(this);
		super.remove();
	}

	/**
	 * Sets an anchor widget.
	 * <p>
	 * A vertical popup will show below (or above) the widget, while a
	 * horizontal popup will show right (or left) of the widget.
	 */
	public void setAnchorWidget(WWidget anchorWidget, Orientation orientation) {
		this.anchorWidget_ = anchorWidget;
		this.orientation_ = orientation;
	}

	/**
	 * Sets an anchor widget.
	 * <p>
	 * Calls
	 * {@link #setAnchorWidget(WWidget anchorWidget, Orientation orientation)
	 * setAnchorWidget(anchorWidget, Orientation.Vertical)}
	 */
	public final void setAnchorWidget(WWidget anchorWidget) {
		setAnchorWidget(anchorWidget, Orientation.Vertical);
	}

	public WWidget getAnchorWidget() {
		return this.anchorWidget_;
	}

	/**
	 * Returns the orientation.
	 * <p>
	 */
	public Orientation getOrientation() {
		return this.orientation_;
	}

	/**
	 * Sets transient property.
	 * <p>
	 * A transient popup will automatically hide when the user clicks outside of
	 * the popup. When <code>autoHideDelay</code> is not 0, then it will also
	 * automatically hide when the user moves the mouse outside the widget for
	 * longer than this delay.
	 */
	public void setTransient(boolean isTransient, int autoHideDelay) {
		this.transient_ = isTransient;
		this.autoHideDelay_ = autoHideDelay;
	}

	/**
	 * Sets transient property.
	 * <p>
	 * Calls {@link #setTransient(boolean isTransient, int autoHideDelay)
	 * setTransient(isTransient, 0)}
	 */
	public final void setTransient(boolean isTransient) {
		setTransient(isTransient, 0);
	}

	/**
	 * Returns whether the popup is transient.
	 * <p>
	 * 
	 * @see WPopupWidget#setTransient(boolean isTransient, int autoHideDelay)
	 */
	public boolean isTransient() {
		return this.transient_;
	}

	/**
	 * Returns the auto-hide delay.
	 * <p>
	 * 
	 * @see WPopupWidget#setTransient(boolean isTransient, int autoHideDelay)
	 */
	public int getAutoHideDelay() {
		return this.autoHideDelay_;
	}

	public void setHidden(boolean hidden, WAnimation animation) {
		if (WWebWidget.canOptimizeUpdates() && hidden == this.isHidden()) {
			return;
		}
		super.setHidden(hidden, animation);
		if (!hidden && this.anchorWidget_ != null) {
			this.positionAt(this.anchorWidget_, this.orientation_);
		}
		if (hidden) {
			this.hidden().trigger();
		} else {
			this.shown().trigger();
		}
		if (!WWebWidget.canOptimizeUpdates() || this.isRendered()) {
			if (hidden) {
				this.doJavaScript("var o = jQuery.data(" + this.getJsRef()
						+ ", 'popup');if (o) o.hidden();");
			} else {
				this.doJavaScript("var o = jQuery.data(" + this.getJsRef()
						+ ", 'popup');if (o) o.shown();");
			}
		}
	}

	/**
	 * Signal emitted when the popup is hidden.
	 * <p>
	 * This signal is emitted when the popup is being hidden because of a
	 * client-side event (not when
	 * {@link WPopupWidget#setHidden(boolean hidden, WAnimation animation)
	 * setHidden()} or {@link WWidget#hide() WWidget#hide()} is called).
	 */
	public Signal hidden() {
		return this.hidden_;
	}

	/**
	 * Signal emitted when the popup is shown.
	 * <p>
	 * This signal is emitted when the popup is being hidden because of a
	 * client-side event (not when
	 * {@link WPopupWidget#setHidden(boolean hidden, WAnimation animation)
	 * setHidden()} or {@link WWidget#show() WWidget#show()} is called).
	 */
	public Signal shown() {
		return this.shown_;
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	protected void setParent(WObject p) {
		if (!(p != null) || p == WApplication.getInstance().getDomRoot()) {
			if (!(p != null)) {
				this.fakeParent_ = null;
			}
			super.setParent(p);
		} else {
			if (p != null) {
				this.fakeParent_ = p;
			}
		}
	}

	private WObject fakeParent_;
	private WWidget anchorWidget_;
	private Orientation orientation_;
	private boolean transient_;
	private int autoHideDelay_;
	private Signal hidden_;
	private Signal shown_;
	private JSignal jsHidden_;
	private JSignal jsShown_;

	// private void create(WWidget parent) ;
	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WPopupWidget.js", wtjs1());
		StringBuilder jsObj = new StringBuilder();
		jsObj.append("new Wt3_3_0.WPopupWidget(").append(
				app.getJavaScriptClass()).append(',').append(this.getJsRef())
				.append(',').append(this.transient_).append(',').append(
						this.autoHideDelay_).append(',').append(
						!this.isHidden()).append(");");
		this.setJavaScriptMember(" WPopupWidget", jsObj.toString());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WPopupWidget",
				"function(d,a,j,k,n){function o(){clearTimeout(e);e=setTimeout(function(){f.hide()},k)}function p(){clearTimeout(e)}function l(b){function g(q,c){for(c=c.parentNode;c;c=c.parentNode)if(q==c)return true;return false}g(a,m.target(b))||f.hide()}jQuery.data(a,\"popup\",this);var f=this,m=d.WT,e=null,h=null,i=null;this.bindShow=function(b){h=b};this.bindHide=function(b){i=b};this.shown=function(){j&&setTimeout(function(){$(document).bind(\"click\", l)},0);h&&h()};this.show=function(b,g){if(a.style.display!=\"\"){a.style.display=\"\";b&&m.positionAtWidget(a.id,b.id,g);d.emit(a,\"shown\")}};this.hidden=function(){i&&i();j&&$(document).unbind(\"click\",l)};this.hide=function(){if(a.style.display!=\"none\")a.style.display=\"none\";d.emit(a,\"hidden\");f.hidden()};k>0&&$(a).mouseleave(o).mouseenter(p);n&&this.shown()}");
	}
}
