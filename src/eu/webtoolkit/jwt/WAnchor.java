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
 * A widget that represents an HTML anchor (to link to other documents).
 * <p>
 * 
 * Use an anchor to link to another web page, document, internal application
 * path or a resource. The anchor may contain a label text, an image, or any
 * other widget (as it inherits from {@link WContainerWidget}). If you do not
 * want the application to terminate when the user follows the anchor, you must
 * use {@link WAnchor#setTarget(AnchorTarget target) setTarget(TargetNewWindow)}
 * . Even for non-HTML documents, this may be important since pending AJAX
 * requests are cancelled if documents are not served within the browser window
 * in certain browsers.
 * <p>
 * WAnchor is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <p>
 * <i><b>Note: </b>If you set a text or image using one of the API methods like
 * {@link WAnchor#setText(CharSequence text) setText()} or
 * {@link WAnchor#setImage(WImage image) setImage()} or a constructor, you
 * should not attempt to remove all contents (using
 * {@link WContainerWidget#clear() WContainerWidget#clear()}, or provide a
 * layout (using {@link WContainerWidget#setLayout(WLayout layout)
 * WContainerWidget#setLayout()}), as this will result in undefined behaviour:
 * the text or image are simply inserted as widgets into the container.</i>
 * </p>
 * <h3>CSS</h3>
 * <p>
 * The widget corresponds to the HTML <code>&lt;a&gt;</code> tag and does not
 * provide styling. It can be styled using inline or external CSS as
 * appropriate.
 */
public class WAnchor extends WContainerWidget {
	/**
	 * Creates an anchor.
	 */
	public WAnchor(WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
	}

	/**
	 * Creates an anchor.
	 * <p>
	 * Calls {@link #WAnchor(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WAnchor() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates an anchor referring to a URL.
	 */
	public WAnchor(String ref, WContainerWidget parent) {
		super(parent);
		this.ref_ = ref;
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
	}

	/**
	 * Creates an anchor referring to a URL.
	 * <p>
	 * Calls {@link #WAnchor(String ref, WContainerWidget parent) this(ref,
	 * (WContainerWidget)null)}
	 */
	public WAnchor(String ref) {
		this(ref, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor referring a resource.
	 * <p>
	 * The <code>resource</code> specifies application-dependent content that
	 * may be generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of <code>resource</code>, so that
	 * you may share the same resources for several anchors.
	 * <p>
	 * 
	 * @see WResource
	 */
	public WAnchor(WResource resource, WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setResource(resource);
	}

	/**
	 * Creates an anchor referring a resource.
	 * <p>
	 * Calls {@link #WAnchor(WResource resource, WContainerWidget parent)
	 * this(resource, (WContainerWidget)null)}
	 */
	public WAnchor(WResource resource) {
		this(resource, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor referring to a URL, using a text message.
	 */
	public WAnchor(String ref, CharSequence text, WContainerWidget parent) {
		super(parent);
		this.ref_ = ref;
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.text_ = new WText(text, this);
	}

	/**
	 * Creates an anchor referring to a URL, using a text message.
	 * <p>
	 * Calls
	 * {@link #WAnchor(String ref, CharSequence text, WContainerWidget parent)
	 * this(ref, text, (WContainerWidget)null)}
	 */
	public WAnchor(String ref, CharSequence text) {
		this(ref, text, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor referring to a resource, using a text message.
	 * <p>
	 * The <code>resource</code> specifies application-dependent content that
	 * may be generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of <code>resource</code>, so that
	 * you may share the same resources for several anchors.
	 */
	public WAnchor(WResource resource, CharSequence text,
			WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.text_ = new WText(text, this);
		this.setResource(resource);
	}

	/**
	 * Creates an anchor referring to a resource, using a text message.
	 * <p>
	 * Calls
	 * {@link #WAnchor(WResource resource, CharSequence text, WContainerWidget parent)
	 * this(resource, text, (WContainerWidget)null)}
	 */
	public WAnchor(WResource resource, CharSequence text) {
		this(resource, text, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor referring to a URL, using an image.
	 * <p>
	 * Ownership of the image is transferred to the anchor.
	 */
	public WAnchor(String ref, WImage image, WContainerWidget parent) {
		super(parent);
		this.ref_ = ref;
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	/**
	 * Creates an anchor referring to a URL, using an image.
	 * <p>
	 * Calls {@link #WAnchor(String ref, WImage image, WContainerWidget parent)
	 * this(ref, image, (WContainerWidget)null)}
	 */
	public WAnchor(String ref, WImage image) {
		this(ref, image, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor referring to a resource, using an image.
	 * <p>
	 * The <code>resource</code> specifies application-dependent content that
	 * may be generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of <code>resource</code>, so that
	 * you may share the same resources for several anchors.
	 * <p>
	 * Ownership of the image is transferred to the anchor.
	 */
	public WAnchor(WResource resource, WImage image, WContainerWidget parent) {
		super(parent);
		this.ref_ = "";
		this.resource_ = null;
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
		this.setResource(resource);
	}

	/**
	 * Creates an anchor referring to a resource, using an image.
	 * <p>
	 * Calls
	 * {@link #WAnchor(WResource resource, WImage image, WContainerWidget parent)
	 * this(resource, image, (WContainerWidget)null)}
	 */
	public WAnchor(WResource resource, WImage image) {
		this(resource, image, (WContainerWidget) null);
	}

	public void remove() {
		;
		super.remove();
	}

	/**
	 * Sets the destination URL.
	 * <p>
	 * This method should not be used when the anchor has been pointed to a
	 * dynamically generated resource using
	 * {@link WAnchor#setResource(WResource resource) setResource()}.
	 * <p>
	 * 
	 * @see WAnchor#setResource(WResource resource)
	 * @see WAnchor#setRefInternalPath(String path)
	 */
	public void setRef(String url) {
		if (!this.flags_.get(BIT_REF_INTERNAL_PATH) && this.ref_.equals(url)) {
			return;
		}
		this.flags_.clear(BIT_REF_INTERNAL_PATH);
		this.ref_ = url;
		this.flags_.set(BIT_REF_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}

	/**
	 * Sets the destination URL as an internal path.
	 * <p>
	 * Sets the anchor to point to the internal path <code>path</code>. When the
	 * anchor is activated, the internal path is set to <code>path</code>, and
	 * the {@link WApplication#internalPathChanged()
	 * WApplication#internalPathChanged()} signal is emitted.
	 * <p>
	 * This is the easiest way to let the application participate in browser
	 * history, and generate URLs that are bookmarkable and search engine
	 * friendly.
	 * <p>
	 * Internally, this method sets the destination URL using: <blockquote>
	 * 
	 * <pre>
	 * setRef(WApplication::instance().bookmarkUrl(path))
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * The {@link WInteractWidget#clicked() WInteractWidget#clicked()} signal is
	 * connected to a slot that changes the internal path by calling
	 * <blockquote>
	 * 
	 * <pre>
	 * WApplication::instance().setInternalPath(ref(), true);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WAnchor#setRef(String url)
	 * @see WAnchor#setResource(WResource resource)
	 * @see WApplication#getBookmarkUrl()
	 * @see WApplication#getBookmarkUrl()
	 */
	public void setRefInternalPath(String path) {
		if (this.flags_.get(BIT_REF_INTERNAL_PATH) && path.equals(this.ref_)) {
			return;
		}
		this.flags_.set(BIT_REF_INTERNAL_PATH);
		this.ref_ = path;
		this.flags_.set(BIT_REF_CHANGED);
		WApplication.getInstance().enableInternalPaths();
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}

	/**
	 * Returns the destination URL.
	 * <p>
	 * When the anchor refers to a resource, the current resource URL is
	 * returned. When the anchor refers to an internal path, the internal path
	 * is returned. Otherwise, the URL is returned that was set using
	 * {@link WAnchor#setRef(String url) setRef()}.
	 * <p>
	 * 
	 * @see WAnchor#setRef(String url)
	 * @see WResource#getUrl()
	 */
	public String getRef() {
		return this.ref_;
	}

	/**
	 * Sets a destination resource.
	 * <p>
	 * A resource specifies application-dependent content, which may be
	 * generated by your application on demand.
	 * <p>
	 * This sets the <code>resource</code> as the destination of the anchor, and
	 * is an alternative to {@link WAnchor#setRef(String url) setRef()}. The
	 * resource may be cleared by passing <code>resource</code> =
	 * <code>null</code>.
	 * <p>
	 * The anchor does not assume ownership of the resource.
	 * <p>
	 * 
	 * @see WAnchor#setRef(String url)
	 */
	public void setResource(WResource resource) {
		this.resource_ = resource;
		if (this.resource_ != null) {
			this.resource_.dataChanged().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WAnchor.this.resourceChanged();
						}
					});
			this.resourceChanged();
		}
	}

	/**
	 * Returns the destination resource.
	 * <p>
	 * Returns <code>null</code> if no resource has been set.
	 * <p>
	 * 
	 * @see WAnchor#setResource(WResource resource)
	 */
	public WResource getResource() {
		return this.resource_;
	}

	/**
	 * Sets the label text.
	 * <p>
	 * If no text was previously set, a new {@link WText} widget is added using
	 * {@link WContainerWidget#addWidget(WWidget widget)
	 * WContainerWidget#addWidget()}.
	 */
	public void setText(CharSequence text) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(text, this);
		} else {
			if (!(text.length() == 0)) {
				this.text_.setText(text);
			} else {
				if (this.text_ != null)
					this.text_.remove();
				this.text_ = null;
			}
		}
	}

	/**
	 * Returns the label text.
	 * <p>
	 * Returns an empty string if no label was set.
	 * <p>
	 * 
	 * @see WAnchor#setText(CharSequence text)
	 */
	public WString getText() {
		if (this.text_ != null) {
			return this.text_.getText();
		} else {
			return empty;
		}
	}

	/**
	 * Configures text word wrapping.
	 * <p>
	 * When <code>wordWrap</code> is <code>true</code>, the text set with
	 * {@link WAnchor#setText(CharSequence text) setText()} may be broken up
	 * over multiple lines. When <code>wordWrap</code> is <code>false</code>,
	 * the text will displayed on a single line, unless the text contains &lt;br
	 * /&gt; tags or other block-level tags.
	 * <p>
	 * The default value is <code>true</code>.
	 * <p>
	 * 
	 * @see WAnchor#hasWordWrap()
	 */
	public void setWordWrap(boolean wordWrap) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(this);
		}
		this.text_.setWordWrap(wordWrap);
	}

	/**
	 * Returns whether the widget may break lines.
	 * <p>
	 * 
	 * @see WAnchor#setWordWrap(boolean wordWrap)
	 */
	public boolean hasWordWrap() {
		return this.text_ != null ? this.text_.isWordWrap() : true;
	}

	/**
	 * Sets an image.
	 * <p>
	 * If an image was previously set, it is deleted. The <code>image</code> is
	 * added using {@link WContainerWidget#addWidget(WWidget widget)
	 * WContainerWidget#addWidget()}.
	 * <p>
	 * Ownership of the image is transferred to the anchor.
	 */
	public void setImage(WImage image) {
		if (this.image_ != null)
			this.image_.remove();
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	/**
	 * Returns the image.
	 * <p>
	 * Returns <code>null</code> if no image is set.
	 * <p>
	 * 
	 * @see WAnchor#setImage(WImage image)
	 */
	public WImage getImage() {
		return this.image_;
	}

	/**
	 * Sets the location where the referred content should be displayed.
	 * <p>
	 * By default, the referred content is displayed in the application (
	 * {@link AnchorTarget#TargetSelf}). When the destination is an HTML
	 * document, the application is replaced with the new document. When the
	 * reference is a document that cannot be displayed in the browser, it is
	 * offered for download or opened using an external program, depending on
	 * browser settings.
	 * <p>
	 * By setting <code>target</code> to {@link AnchorTarget#TargetNewWindow},
	 * the destination is displayed in a new browser window or tab.
	 * <p>
	 * 
	 * @see WAnchor#getTarget()
	 */
	public void setTarget(AnchorTarget target) {
		if (this.target_ != target) {
			this.target_ = target;
			this.flags_.set(BIT_TARGET_CHANGED);
		}
	}

	/**
	 * Returns the location where the referred content should be displayed.
	 * <p>
	 * 
	 * @see WAnchor#setTarget(AnchorTarget target)
	 */
	public AnchorTarget getTarget() {
		return this.target_;
	}

	private static final int BIT_REF_INTERNAL_PATH = 0;
	private static final int BIT_REF_CHANGED = 1;
	private static final int BIT_TARGET_CHANGED = 2;
	private String ref_;
	private WResource resource_;
	private WText text_;
	private WImage image_;
	private AnchorTarget target_;
	BitSet flags_;
	private JSlot changeInternalPathJS_;

	private void resourceChanged() {
		this.setRef(this.resource_.getUrl());
	}

	void updateDom(DomElement element, boolean all) {
		boolean needsUrlResolution = false;
		if (this.flags_.get(BIT_REF_CHANGED) || all) {
			String url = "";
			WApplication app = WApplication.getInstance();
			if (this.flags_.get(BIT_REF_INTERNAL_PATH)) {
				if (app.getEnvironment().hasAjax()) {
					url = app.getBookmarkUrl(this.ref_);
					if (this.target_ == AnchorTarget.TargetSelf) {
						if (!(this.changeInternalPathJS_ != null)) {
							this.changeInternalPathJS_ = new JSlot();
							this.clicked().addListener(
									this.changeInternalPathJS_);
							this.clicked().preventDefaultAction();
						}
						this.changeInternalPathJS_
								.setJavaScript("function(){Wt3_1_10.history.navigate("
										+ jsStringLiteral(this.ref_)
										+ ",true);}");
						this.clicked().senderRepaint();
					}
				} else {
					if (app.getEnvironment().agentIsSpiderBot()) {
						url = app.getBookmarkUrl(this.ref_);
					} else {
						url = app.getSession().getMostRelativeUrl(this.ref_);
					}
				}
			} else {
				url = this.ref_;
				;
				this.changeInternalPathJS_ = null;
			}
			element.setAttribute("href", resolveRelativeUrl(url));
			needsUrlResolution = !app.getEnvironment().isHashInternalPaths();
			this.flags_.clear(BIT_REF_CHANGED);
		}
		if (this.flags_.get(BIT_TARGET_CHANGED) || all) {
			switch (this.target_) {
			case TargetSelf:
				if (!all) {
					element.setProperty(Property.PropertyTarget, "_self");
				}
				break;
			case TargetThisWindow:
				element.setProperty(Property.PropertyTarget, "_top");
				break;
			case TargetNewWindow:
				element.setProperty(Property.PropertyTarget, "_blank");
			}
			this.flags_.clear(BIT_TARGET_CHANGED);
		}
		super.updateDom(element, all);
		if (needsUrlResolution) {
			if (all) {
				element.setProperty(Property.PropertyClass, StringUtils
						.addWord(this.getStyleClass(), "Wt-rr"));
			} else {
				element.callJavaScript("$('#" + this.getId()
						+ "').addClass('Wt-rr');");
			}
		}
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_A;
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_REF_CHANGED);
		this.flags_.clear(BIT_TARGET_CHANGED);
		super.propagateRenderOk(deep);
	}

	protected void enableAjax() {
		if (this.flags_.get(BIT_REF_INTERNAL_PATH)) {
			this.flags_.set(BIT_REF_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		}
		super.enableAjax();
	}

	static WString empty = new WString("");
}
