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
 * An abstract base class for an MVC view that is rendered using a widget.
 * <p>
 * 
 * In principle, JWt widgets are self-contained and manage both their content,
 * behavior and layout. From the point of view of a Model-View-Controller (MVC)
 * design pattern, they implement each of these, except for the view widgets
 * that work in conjunction with {@link WAbstractItemModel}. As a developer you
 * can chose to keep Model, View and Controller together or separate them as you
 * wish.
 * <p>
 * This widget facilitates separation of the View from the Model and Controller
 * in a particular way. The View is rendered as a JWt widget. The use of this
 * widget provides two benefits. The classic MVC benefit is a decoupling between
 * view and model, which may allow easier maintainance of code. In addition,
 * this widget enforces the View to be stateless, as it is only created
 * transiently on the server. Therefore the View does not require session
 * resources. This may increase scalability for Internet-deployments.
 * <p>
 * The rendered View widget returned by {@link WViewWidget#getRenderView()
 * getRenderView()} should reflect the current model state. Whenever the model
 * changes, rerendering can be triggered by calling {@link WViewWidget#update()
 * update()}.
 * <p>
 * Currently, the View cannot enclose {@link WFormWidget WFormWidgets} which
 * would allow direct manipulation of the model (but we are working to remove
 * this limitation in the future, and let the Model/Controller handle editing
 * changes) and the View may only be updated by a complete rerendering of the
 * entire view.
 * <p>
 * The View widget may contain event handling code, but only in one of the
 * following ways:
 * <ul>
 * <li>
 * event handling implemented directly in JavaScript code</li>
 * <li>
 * event handling implemented in pre-learned stateless slot implementations</li>
 * </ul>
 * Thus, currently, event handling code related to the View cannot be
 * implemented at server-side (but we are thinking about a solution for this as
 * well...).
 * <p>
 * <h3>CSS</h3>
 * <p>
 * This widget does not provide styling, and can be styled using inline or
 * external CSS as appropriate.
 */
public abstract class WViewWidget extends WWebWidget {
	private static Logger logger = LoggerFactory.getLogger(WViewWidget.class);

	/**
	 * Creates a new view widget.
	 */
	public WViewWidget(WContainerWidget parent) {
		super(parent);
		this.contents_ = null;
	}

	/**
	 * Creates a new view widget.
	 * <p>
	 * Calls {@link #WViewWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WViewWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Updates the view.
	 * <p>
	 * Typically, the model will want to update the view when the model has
	 * changed.
	 * <p>
	 * This will trigger a call to {@link WViewWidget#getRenderView()
	 * getRenderView()} to ask for a new rendering of the view.
	 */
	public void update() {
		this.needContentsUpdate_ = true;
		if (this.isRendered()) {
			this.scheduleRender();
		}
	}

	public void load() {
		this.update();
		super.load();
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (this.needContentsUpdate_
				|| !EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			if (this.contents_ != null)
				this.contents_.remove();
			WApplication.getInstance().setExposeSignals(false);
			this.contents_ = this.getRenderView();
			WApplication.getInstance().setExposeSignals(true);
			this.addChild(this.contents_);
			this.contents_.render(flags);
			this.setInline(this.contents_.isInline());
			this.needContentsUpdate_ = false;
		}
		super.render(flags);
	}

	public void refresh() {
		if (!(this.contents_ != null)) {
			this.update();
		}
	}

	/**
	 * Creates a widget that renders the View.
	 * <p>
	 * This method must be reimplemented to return a widget that renders the
	 * view. The returned widget will be deleted by WViewWidget.
	 */
	protected abstract WWidget getRenderView();

	void updateDom(DomElement element, boolean all) {
		WApplication app = WApplication.getInstance();
		if (!app.getSession().getRenderer().isPreLearning()) {
			if (all && !(this.contents_ != null)) {
				this.needContentsUpdate_ = true;
				this.render(EnumSet.of(RenderFlag.RenderFull));
			}
			if (this.contents_ != null) {
				boolean savedVisibleOnly = app.getSession().getRenderer()
						.isVisibleOnly();
				WApplication.getInstance().getSession().getRenderer()
						.setVisibleOnly(false);
				DomElement e = this.contents_.createSDomElement(WApplication
						.getInstance());
				if (!all) {
					element.setWasEmpty(true);
				}
				element.addChild(e);
				WApplication.getInstance().getSession().getRenderer()
						.setVisibleOnly(savedVisibleOnly);
				this.needContentsUpdate_ = false;
			}
		}
		super.updateDom(element, all);
	}

	void propagateRenderOk(boolean deep) {
		this.needContentsUpdate_ = false;
		super.propagateRenderOk(deep);
	}

	DomElementType getDomElementType() {
		return this.isInline() ? DomElementType.DomElement_SPAN
				: DomElementType.DomElement_DIV;
	}

	void doneRerender() {
		this.setIgnoreChildRemoves(true);
		if (this.contents_ != null)
			this.contents_.remove();
		this.contents_ = null;
		this.setIgnoreChildRemoves(false);
	}

	private WWidget contents_;
	private boolean needContentsUpdate_;
}
