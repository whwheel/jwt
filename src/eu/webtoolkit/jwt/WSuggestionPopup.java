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
 * A widget which popups to assist in editing a textarea or lineedit.
 * <p>
 * 
 * This widget may be associated with one or more {@link WFormWidget
 * WFormWidgets} (typically a {@link WLineEdit} or a {@link WTextArea}).
 * <p>
 * The popup provides the user with suggestions to enter input. The popup can be
 * used by one or more editors, using
 * {@link WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
 * forEdit()}. The popup will show when the user starts editing the edit field,
 * or when the user opens the suggestions explicitly using a drop down icon or
 * with the down key. The popup positions itself intelligently just below or
 * just on top of the edit field. It offers a list of suggestions that match in
 * some way with the current edit field, and dynamically adjusts this list. The
 * implementation for matching individual suggestions with the current text is
 * provided through a JavaScript function. This function may also highlight
 * part(s) of the suggestions to provide feed-back on how they match.
 * <p>
 * WSuggestionPopup is an MVC view class, using a simple
 * {@link WStringListModel} by default. You can set a custom model using
 * {@link WSuggestionPopup#setModel(WAbstractItemModel model) setModel()}. The
 * model can provide different text for the suggestion text (
 * {@link ItemDataRole#DisplayRole}) and value ({@link ItemDataRole#UserRole}).
 * The member methods {@link WSuggestionPopup#clearSuggestions()
 * clearSuggestions()} and
 * {@link WSuggestionPopup#addSuggestion(CharSequence suggestionText, CharSequence suggestionValue)
 * addSuggestion()} manipulate this model. Note that a {@link WStringListModel}
 * does not support {@link ItemDataRole#UserRole} data, so you may need to use a
 * {@link WStandardItemModel} instead.
 * <p>
 * By default, the popup implements all filtering client-side. To support large
 * datasets, you may enable server-side filtering of suggestions based on the
 * input. The server-side filtering may provide a coarse filtering using a fixed
 * size prefix of the entered text, and complement the client-side filtering. To
 * enable server-side filtering, use
 * {@link WSuggestionPopup#setFilterLength(int length) setFilterLength()} and
 * listen to filter notification using the modelFilter() signal. Whenever a
 * filter event is generated you can adjust the model&apos;s content according
 * to the filter (e.g. using a {@link WSortFilterProxyModel}). By using
 * {@link WCompositeWidget#setMaximumSize(WLength width, WLength height)
 * WCompositeWidget#setMaximumSize()} you can also limit the maximum height of
 * the popup, in which case scrolling is supported (similar to a combo-box).
 * <p>
 * The class is initialized with an {@link Options} struct which configures how
 * suggestion filtering and result editing is done. Alternatively, you can
 * provide two JavaScript functions, one for filtering the suggestions, and one
 * for editing the value of the textarea when a suggestion is selected.
 * <p>
 * The matcherJS function must have the following JavaScript signature:
 * <p>
 * 
 * <pre>
 * {@code
 *  function (editElement) {
 *    // fetch the location of cursor and current text in the editElement.
 * 
 *    // return a function that matches a given suggestion with the current value of the editElement.
 *    return function(suggestion) {
 * 
 *      // 1) if suggestion is null, simply return the current text 'value'
 *      // 2) check suggestion if it matches
 *      // 3) add highlighting markup to suggestion if necessary
 * 
 *      return { match : ...,      // does the suggestion match ? (boolean)
 *               suggestion : ...  // modified suggestion with highlighting
 *              };
 *    }
 *  }
 * }
 * </pre>
 * <p>
 * The replacerJS function that edits the value has the following JavaScript
 * signature.
 * <p>
 * 
 * <pre>
 * {@code
 *  function (editElement, suggestionText, suggestionValue) {
 *    // editElement is the form element which must be edited.
 *    // suggestionText is the displayed text for the matched suggestion.
 *    // suggestionValue is the stored value for the matched suggestion.
 * 
 *    // computed modifiedEditValue and modifiedPos ...
 * 
 *    editElement.value = modifiedEditValue;
 *    editElement.selectionStart = edit.selectionEnd = modifiedPos;
 *  }
 * }
 * </pre>
 * <p>
 * To style the suggestions, you should style the &lt;span&gt; element inside
 * this widget, and the &lt;span&gt;.&quot;sel&quot; element to style the
 * current selection.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	// options for email address suggestions
 * 	WSuggestionPopup.Options contactOptions = new WSuggestionPopup.Options();
 * 	contactOptions.highlightBeginTag = &quot;&lt;b&gt;&quot;;
 * 	contactOptions.highlightEndTag = &quot;&lt;/b&gt;&quot;;
 * 	contactOptions.listSeparator = ','; //for multiple addresses)
 * 	contactOptions.whitespace = &quot; \\n&quot;;
 * 	contactOptions.wordSeparators = &quot;-., \&quot;@\\n;&quot;; //within an address
 * 	contactOptions.appendReplacedText = &quot;, &quot;; //prepare next email address
 * 
 * 	WSuggestionPopup popup = new WSuggestionPopup(contactOptions, this);
 * 
 * 	WTextArea textEdit = new WTextArea(this);
 * 	popup.forEdit(textEdit);
 * 
 * 	// load popup data
 * 	for (int i = 0; i &lt; contacts.size(); ++i)
 * 		popup.addSuggestion(contacts.get(i).formatted(), contacts.get(i)
 * 				.formatted());
 * }
 * </pre>
 * <p>
 * A screenshot of this example:
 * <table border="0" align="center" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><div align="center"> <img src="doc-files//WSuggestionPopup-default-1.png"
 * alt="An example WSuggestionPopup (default)">
 * <p>
 * <strong>An example WSuggestionPopup (default)</strong>
 * </p>
 * </div></td>
 * <td><div align="center"> <img
 * src="doc-files//WSuggestionPopup-polished-1.png"
 * alt="An example WSuggestionPopup (polished)">
 * <p>
 * <strong>An example WSuggestionPopup (polished)</strong>
 * </p>
 * </div></td>
 * </tr>
 * </table>
 * <p>
 * When using the DropDownIcon trigger, an additional style class is provided
 * for the edit field: <code>Wt-suggest-dropdown</code>, which renders the icon
 * to the right inside the edit field. This class may be used to customize how
 * the drop down icon is rendered.
 * <p>
 * <p>
 * <i><b>Note: </b>This widget requires JavaScript support. </i>
 * </p>
 */
public class WSuggestionPopup extends WPopupWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WSuggestionPopup.class);

	/**
	 * Enumeration that defines a trigger for showing the popup.
	 * <p>
	 * 
	 * @see WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
	 */
	public enum PopupTrigger {
		/**
		 * Shows popup when the user starts editing.
		 * <p>
		 * The popup is shown when the currently edited text has a length longer
		 * than the {@link WSuggestionPopup#setFilterLength(int length) filter
		 * length}.
		 */
		Editing,
		/**
		 * Shows popup when user clicks a drop down icon.
		 * <p>
		 * The lineedit is modified to show a drop down icon, and clicking the
		 * icon shows the suggestions, very much like a WComboCox.
		 */
		DropDownIcon;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * A configuration object to generate a matcher and replacer JavaScript
	 * function.
	 * <p>
	 * 
	 * @see WSuggestionPopup
	 */
	public static class Options {
		private static Logger logger = LoggerFactory.getLogger(Options.class);

		/**
		 * Open tag to highlight a match in a suggestion.
		 * <p>
		 * Must be an opening markup tag, such as &lt;b&gt;.
		 * <p>
		 * Used during matching.
		 */
		public String highlightBeginTag;
		/**
		 * Close tag to highlight a match in a suggestion.
		 * <p>
		 * Must be a closing markup tag, such as &lt;/b&gt;.
		 * <p>
		 * Used during matching.
		 */
		public String highlightEndTag;
		/**
		 * When editing a list of values, the separator used for different
		 * items.
		 * <p>
		 * For example, &apos;,&apos; to separate different values on komma.
		 * Specify 0 (&apos;\0&apos;) for no list separation.
		 * <p>
		 * Used during matching and replacing.
		 */
		public char listSeparator;
		/**
		 * When editing a value, the whitespace characters ignored before the
		 * current value.
		 * <p>
		 * For example, &quot; \\n&quot; to ignore spaces and newlines.
		 * <p>
		 * Used during matching and replacing.
		 */
		public String whitespace;
		/**
		 * To show suggestions based on matches of the edited value with parts
		 * of the suggestion.
		 * <p>
		 * For example, &quot; .@&quot; will also match with suggestion text
		 * after a space, a dot (.) or an at-symbol (@).
		 * <p>
		 * Used during matching.
		 */
		public String wordSeparators;
		/**
		 * When replacing the current edited value with suggestion value, append
		 * the following string as well.
		 * <p>
		 * Used during replacing.
		 */
		public String appendReplacedText;
	}

	/**
	 * Creates a suggestion popup.
	 * <p>
	 * The popup using a standard matcher and replacer implementation that is
	 * configured using the provided <code>options</code>.
	 * <p>
	 * 
	 * @see WSuggestionPopup#generateMatcherJS(WSuggestionPopup.Options options)
	 * @see WSuggestionPopup#generateReplacerJS(WSuggestionPopup.Options
	 *      options)
	 */
	public WSuggestionPopup(WSuggestionPopup.Options options, WObject parent) {
		super(new WContainerWidget(), parent);
		this.model_ = null;
		this.modelColumn_ = 0;
		this.filterLength_ = 0;
		this.filtering_ = false;
		this.defaultValue_ = -1;
		this.matcherJS_ = generateMatcherJS(options);
		this.replacerJS_ = generateReplacerJS(options);
		this.filterModel_ = new Signal1<String>(this);
		this.activated_ = new Signal2<Integer, WFormWidget>(this);
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.getImplementation(), "filter") {
		};
		this.jactivated_ = new JSignal2<String, String>(this
				.getImplementation(), "select") {
		};
		this.edits_ = new ArrayList<WFormWidget>();
		this.init();
	}

	/**
	 * Creates a suggestion popup.
	 * <p>
	 * Calls
	 * {@link #WSuggestionPopup(WSuggestionPopup.Options options, WObject parent)
	 * this(options, (WObject)null)}
	 */
	public WSuggestionPopup(WSuggestionPopup.Options options) {
		this(options, (WObject) null);
	}

	/**
	 * Creates a suggestion popup with given matcherJS and replacerJS.
	 * <p>
	 * See supra for the expected signature of the matcher and replace
	 * JavaScript functions.
	 */
	public WSuggestionPopup(String matcherJS, String replacerJS, WObject parent) {
		super(new WContainerWidget(), parent);
		this.model_ = null;
		this.modelColumn_ = 0;
		this.filterLength_ = 0;
		this.filtering_ = false;
		this.defaultValue_ = -1;
		this.matcherJS_ = matcherJS;
		this.replacerJS_ = replacerJS;
		this.filterModel_ = new Signal1<String>();
		this.activated_ = new Signal2<Integer, WFormWidget>();
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.getImplementation(), "filter") {
		};
		this.jactivated_ = new JSignal2<String, String>(this
				.getImplementation(), "select") {
		};
		this.edits_ = new ArrayList<WFormWidget>();
		this.init();
	}

	/**
	 * Creates a suggestion popup with given matcherJS and replacerJS.
	 * <p>
	 * Calls
	 * {@link #WSuggestionPopup(String matcherJS, String replacerJS, WObject parent)
	 * this(matcherJS, replacerJS, (WObject)null)}
	 */
	public WSuggestionPopup(String matcherJS, String replacerJS) {
		this(matcherJS, replacerJS, (WObject) null);
	}

	/**
	 * Lets this suggestion popup assist in editing an edit field.
	 * <p>
	 * A single suggestion popup may assist in several edits by repeated calls
	 * of this method.
	 * <p>
	 * The <code>popupTriggers</code> control how editing is triggered (either
	 * by the user editing the field by entering keys or by an explicit drop
	 * down menu that is shown inside the edit).
	 * <p>
	 * 
	 * @see WSuggestionPopup#removeEdit(WFormWidget edit)
	 */
	public void forEdit(WFormWidget edit,
			EnumSet<WSuggestionPopup.PopupTrigger> triggers) {
		AbstractEventSignal b = edit.keyPressed();
		this.connectObjJS(b, "editKeyDown");
		this.connectObjJS(edit.keyWentDown(), "editKeyDown");
		this.connectObjJS(edit.keyWentUp(), "editKeyUp");
		this.connectObjJS(edit.blurred(), "delayHide");
		if (!EnumUtils.mask(triggers, WSuggestionPopup.PopupTrigger.Editing)
				.isEmpty()) {
			edit.addStyleClass("Wt-suggest-onedit");
		}
		if (!EnumUtils.mask(triggers,
				WSuggestionPopup.PopupTrigger.DropDownIcon).isEmpty()) {
			edit.addStyleClass("Wt-suggest-dropdown");
			AbstractEventSignal c = edit.clicked();
			this.connectObjJS(c, "editClick");
			this.connectObjJS(edit.mouseMoved(), "editMouseMove");
		}
		this.edits_.add(edit);
	}

	/**
	 * Lets this suggestion popup assist in editing an edit field.
	 * <p>
	 * Calls {@link #forEdit(WFormWidget edit, EnumSet triggers) forEdit(edit,
	 * EnumSet.of(trigger, triggers))}
	 */
	public final void forEdit(WFormWidget edit,
			WSuggestionPopup.PopupTrigger trigger,
			WSuggestionPopup.PopupTrigger... triggers) {
		forEdit(edit, EnumSet.of(trigger, triggers));
	}

	/**
	 * Lets this suggestion popup assist in editing an edit field.
	 * <p>
	 * Calls {@link #forEdit(WFormWidget edit, EnumSet triggers) forEdit(edit,
	 * EnumSet.of(WSuggestionPopup.PopupTrigger.Editing))}
	 */
	public final void forEdit(WFormWidget edit) {
		forEdit(edit, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing));
	}

	/**
	 * Removes the edit field from the list of assisted editors.
	 * <p>
	 * The editor will no longer be assisted by this popup widget.
	 * <p>
	 * 
	 * @see WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
	 */
	public void removeEdit(WFormWidget edit) {
		if (this.edits_.remove(edit)) {
			edit.removeStyleClass("Wt-suggest-onedit");
			edit.removeStyleClass("Wt-suggest-dropdown");
		}
	}

	/**
	 * Shows the suggestion popup at an edit field.
	 * <p>
	 * This is equivalent to the user triggering the suggestion popup to be
	 * shown.
	 */
	public void showAt(WFormWidget edit) {
		this.doJavaScript("jQuery.data(" + this.getJsRef() + ", 'obj').showAt("
				+ edit.getJsRef() + ");");
	}

	/**
	 * Clears the list of suggestions.
	 * <p>
	 * This clears the underlying model.
	 * <p>
	 * 
	 * @see WSuggestionPopup#addSuggestion(CharSequence suggestionText,
	 *      CharSequence suggestionValue)
	 */
	public void clearSuggestions() {
		this.model_.removeRows(0, this.model_.getRowCount());
	}

	/**
	 * Adds a new suggestion.
	 * <p>
	 * This adds an entry to the underlying model. The
	 * <code>suggestionText</code> is set as {@link ItemDataRole#DisplayRole}
	 * and the <code>suggestionValue</code> (which is inserted into the edit
	 * field on selection) is set as {@link ItemDataRole#UserRole}.
	 * <p>
	 * 
	 * @see WSuggestionPopup#clearSuggestions()
	 * @see WSuggestionPopup#setModel(WAbstractItemModel model)
	 */
	public void addSuggestion(CharSequence suggestionText,
			CharSequence suggestionValue) {
		int row = this.model_.getRowCount();
		if (this.model_.insertRow(row)) {
			this.model_.setData(row, this.modelColumn_, suggestionText,
					ItemDataRole.DisplayRole);
			if (!(suggestionValue.length() == 0)) {
				this.model_.setData(row, this.modelColumn_, suggestionValue,
						ItemDataRole.UserRole);
			}
		}
	}

	/**
	 * Adds a new suggestion.
	 * <p>
	 * Calls
	 * {@link #addSuggestion(CharSequence suggestionText, CharSequence suggestionValue)
	 * addSuggestion(suggestionText, WString.Empty)}
	 */
	public final void addSuggestion(CharSequence suggestionText) {
		addSuggestion(suggestionText, WString.Empty);
	}

	/**
	 * Sets the model to be used for the suggestions.
	 * <p>
	 * The <code>model</code> may not be <code>null</code>, and ownership of the
	 * model is not transferred.
	 * <p>
	 * The default value is a {@link WStringListModel} that is owned by the
	 * suggestion popup.
	 * <p>
	 * The {@link ItemDataRole#DisplayRole} is used for the suggestion text. The
	 * {@link ItemDataRole#UserRole} is used for the suggestion value, unless
	 * empty, in which case the suggestion text is used as value.
	 * <p>
	 * Note that since the default WStringListModel does not support UserRole
	 * data, you will want to change it to a more general model (e.g.
	 * {@link WStandardItemModel}) if you want suggestion values that are
	 * different from display values.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setModelColumn(int modelColumn)
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		this.modelConnections_.add(this.model_.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WSuggestionPopup.this.modelRowsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WSuggestionPopup.this.modelRowsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WSuggestionPopup.this.modelDataChanged(e1, e2);
					}
				}));
		this.modelConnections_.add(this.model_.layoutChanged().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WSuggestionPopup.this.modelLayoutChanged();
					}
				}));
		this.modelConnections_.add(this.model_.modelReset().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WSuggestionPopup.this.modelLayoutChanged();
					}
				}));
		this.setModelColumn(this.modelColumn_);
	}

	/**
	 * Returns the data model.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
	}

	/**
	 * Sets the column in the model to be used for the items.
	 * <p>
	 * The column <code>index</code> in the model will be used to retrieve data.
	 * <p>
	 * The default value is 0.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setModel(WAbstractItemModel model)
	 */
	public void setModelColumn(int modelColumn) {
		this.modelColumn_ = modelColumn;
		this.impl_.clear();
		this.modelRowsInserted(null, 0, this.model_.getRowCount() - 1);
	}

	/**
	 * Sets a default selected value.
	 * <p>
	 * <code>row</code> is the model row that is selected by default (only if it
	 * matches the current input).
	 * <p>
	 * The default value is -1, indicating no default.
	 */
	public void setDefaultIndex(int row) {
		if (this.defaultValue_ != row) {
			this.defaultValue_ = row;
			if (this.isRendered()) {
				this.doJavaScript("jQuery.data(" + this.getJsRef()
						+ ", 'obj').defaultValue = "
						+ String.valueOf(this.defaultValue_) + ';');
			}
		}
	}

	/**
	 * Returns the default value.
	 * <p>
	 */
	public int getDefaultIndex() {
		return this.defaultValue_;
	}

	/**
	 * Creates a standard matcher JavaScript function.
	 * <p>
	 * This returns a JavaScript function that provides a standard
	 * implementation for the matching input, based on the given
	 * <code>options</code>.
	 */
	public static String generateMatcherJS(WSuggestionPopup.Options options) {
		return instantiateStdMatcher(options) + ".match";
	}

	/**
	 * Creates a standard replacer JavaScript function.
	 * <p>
	 * This returns a JavaScript function that provides a standard
	 * implementation for the matching input, based on the given
	 * <code>options</code>.
	 */
	public static String generateReplacerJS(WSuggestionPopup.Options options) {
		return instantiateStdMatcher(options) + ".replace";
	}

	/**
	 * Sets the minimum input length before showing the popup.
	 * <p>
	 * When the user has typed this much characters,
	 * {@link WSuggestionPopup#filterModel() filterModel()} is emitted which
	 * allows you to filter the model based on the initial input.
	 * <p>
	 * The default value is 0, which has the effect of always showing the entire
	 * model.
	 * <p>
	 * A value of -1 indicates that server-side filtering is continuously
	 * applied, no matter the length of the text input.
	 * <p>
	 * 
	 * @see WSuggestionPopup#filterModel()
	 */
	public void setFilterLength(int length) {
		this.filterLength_ = length;
	}

	/**
	 * Returns the filter length.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setFilterLength(int length)
	 */
	public int getFilterLength() {
		return this.filterLength_;
	}

	/**
	 * Signal that indicates that the model should be filtered.
	 * <p>
	 * The argument is the initial input. When
	 * {@link WSuggestionPopup.PopupTrigger#Editing Editing} is used as edit
	 * trigger, its length will always equal the
	 * {@link WSuggestionPopup#getFilterLength() getFilterLength()}. When
	 * {@link WSuggestionPopup.PopupTrigger#DropDownIcon DropDownIcon} is used
	 * as edit trigger, the input length may be less than
	 * {@link WSuggestionPopup#getFilterLength() getFilterLength()}, and the the
	 * signal will be called repeatedly as the user provides more input.
	 * <p>
	 * For example, if you are using a {@link WSortFilterProxyModel}, you could
	 * react to this signal with:
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 *    public filterSuggestions(String filter) {
	 *      proxyModel.setFilterRegExp(filter + ".*");
	 *    }
	 *   }
	 * </pre>
	 */
	public Signal1<String> filterModel() {
		return this.filterModel_;
	}

	/**
	 * Signal emitted when a suggestion was selected.
	 * <p>
	 * The selected item is passed as the first argument and the editor as the
	 * second.
	 */
	public Signal2<Integer, WFormWidget> activated() {
		return this.activated_;
	}

	/**
	 * Controls how the popup is positioned (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated this option is now ignored, since the popup is automatically
	 *             positioned to behave properly.
	 */
	public void setGlobalPopup(boolean global) {
	}

	private WContainerWidget impl_;
	private WAbstractItemModel model_;
	private int modelColumn_;
	private int filterLength_;
	private boolean filtering_;
	private int defaultValue_;
	private String matcherJS_;
	private String replacerJS_;
	private Signal1<String> filterModel_;
	private Signal2<Integer, WFormWidget> activated_;
	private List<AbstractSignal.Connection> modelConnections_;
	private JSignal1<String> filter_;
	private JSignal2<String, String> jactivated_;
	private List<WFormWidget> edits_;

	private void init() {
		this.impl_ = ((this.getImplementation()) instanceof WContainerWidget ? (WContainerWidget) (this
				.getImplementation())
				: null);
		this.impl_.setList(true);
		this.impl_.setLoadLaterWhenInvisible(false);
		this.setAttributeValue("style",
				"z-index: 10000; display: none; overflow: auto");
		this.setModel(new WStringListModel(this));
		this.impl_.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WSuggestionPopup.this.hide();
			}
		});
		this.filter_.addListener(this, new Signal1.Listener<String>() {
			public void trigger(String e1) {
				WSuggestionPopup.this.doFilter(e1);
			}
		});
		this.jactivated_.addListener(this,
				new Signal2.Listener<String, String>() {
					public void trigger(String e1, String e2) {
						WSuggestionPopup.this.doActivate(e1, e2);
					}
				});
	}

	private void doFilter(String input) {
		this.filtering_ = true;
		this.filterModel_.trigger(input);
		this.filtering_ = false;
		WApplication.getInstance().doJavaScript(
				"jQuery.data(" + this.getJsRef() + ", 'obj').filtered("
						+ WWebWidget.jsStringLiteral(input) + ");");
	}

	private void doActivate(String itemId, String editId) {
		WFormWidget edit = null;
		for (int i = 0; i < this.edits_.size(); ++i) {
			if (this.edits_.get(i).getId().equals(editId)) {
				edit = this.edits_.get(i);
				break;
			}
		}
		if (edit == null) {
			logger.error(new StringWriter()
					.append("activate from bogus editor").toString());
		}
		for (int i = 0; i < this.impl_.getCount(); ++i) {
			if (this.impl_.getWidget(i).getId().equals(itemId)) {
				this.activated_.trigger(i, edit);
				return;
			}
		}
		logger.error(new StringWriter().append("activate for bogus item")
				.toString());
	}

	private void connectObjJS(AbstractEventSignal s, String methodName) {
		String jsFunction = "function(obj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'obj');if (o) o." + methodName
				+ "(obj, event);}";
		s.addListener(jsFunction);
	}

	private void modelRowsInserted(WModelIndex parent, int start, int end) {
		if (this.filterLength_ != 0 && !this.filtering_) {
			return;
		}
		if (this.modelColumn_ >= this.model_.getColumnCount()) {
			return;
		}
		if ((parent != null)) {
			return;
		}
		for (int i = start; i <= end; ++i) {
			WContainerWidget line = new WContainerWidget();
			this.impl_.insertWidget(i, line);
			WModelIndex index = this.model_.getIndex(i, this.modelColumn_);
			Object d = index.getData();
			TextFormat format = !EnumUtils.mask(index.getFlags(),
					ItemFlag.ItemIsXHTMLText).isEmpty() ? TextFormat.XHTMLText
					: TextFormat.PlainText;
			WAnchor anchor = new WAnchor(line);
			WText value = new WText(StringUtils.asString(d), format, anchor);
			Object d2 = index.getData(ItemDataRole.UserRole);
			if ((d2 == null)) {
				d2 = d;
			}
			value.setAttributeValue("sug", StringUtils.asString(d2).toString());
			Object styleclass = index.getData(ItemDataRole.StyleClassRole);
			if (!(styleclass == null)) {
				value.setAttributeValue("class", StringUtils.asString(
						styleclass).toString());
			}
		}
	}

	private void modelRowsRemoved(WModelIndex parent, int start, int end) {
		if ((parent != null)) {
			return;
		}
		for (int i = start; i <= end; ++i) {
			if (start < this.impl_.getCount()) {
				if (this.impl_.getWidget(start) != null)
					this.impl_.getWidget(start).remove();
			} else {
				break;
			}
		}
	}

	private void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		if ((topLeft.getParent() != null)) {
			return;
		}
		if (this.modelColumn_ < topLeft.getColumn()
				|| this.modelColumn_ > bottomRight.getColumn()) {
			return;
		}
		for (int i = topLeft.getRow(); i <= bottomRight.getRow(); ++i) {
			WContainerWidget w = ((this.impl_.getWidget(i)) instanceof WContainerWidget ? (WContainerWidget) (this.impl_
					.getWidget(i))
					: null);
			WAnchor anchor = ((w.getWidget(0)) instanceof WAnchor ? (WAnchor) (w
					.getWidget(0))
					: null);
			WText value = ((anchor.getWidget(0)) instanceof WText ? (WText) (anchor
					.getWidget(0))
					: null);
			WModelIndex index = this.model_.getIndex(i, this.modelColumn_);
			Object d = index.getData();
			value.setText(StringUtils.asString(d));
			TextFormat format = !EnumUtils.mask(index.getFlags(),
					ItemFlag.ItemIsXHTMLText).isEmpty() ? TextFormat.XHTMLText
					: TextFormat.PlainText;
			value.setTextFormat(format);
			Object d2 = this.model_.getData(i, this.modelColumn_,
					ItemDataRole.UserRole);
			if ((d2 == null)) {
				d2 = d;
			}
			value.setAttributeValue("sug", StringUtils.asString(d2).toString());
		}
	}

	private void modelLayoutChanged() {
		this.impl_.clear();
		this.modelRowsInserted(null, 0, this.model_.getRowCount() - 1);
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		String THIS_JS = "js/WSuggestionPopup.js";
		app.loadJavaScript(THIS_JS, wtjs1());
		app.loadJavaScript(THIS_JS, wtjs2());
		this.setJavaScriptMember(" WSuggestionPopup",
				"new Wt3_3_0.WSuggestionPopup(" + app.getJavaScriptClass()
						+ "," + this.getJsRef() + "," + this.replacerJS_ + ","
						+ this.matcherJS_ + ","
						+ String.valueOf(this.filterLength_) + ","
						+ String.valueOf(this.defaultValue_) + ");");
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WSuggestionPopup",
				"function(u,e,y,D,q,z){function r(a){return $(a).hasClass(\"Wt-suggest-onedit\")||$(a).hasClass(\"Wt-suggest-dropdown\")}function c(){return e.style.display!=\"none\"}function g(a){e.style.display=\"block\";d.positionAtWidget(e.id,a.id,d.Vertical)}function i(a){a=d.target(a||window.event);if(!d.hasTag(a,\"UL\")){for(;a&&!d.hasTag(a,\"LI\");)a=a.parentNode;a&&n(a)}}function n(a){var b=a.firstChild.firstChild,l=d.getElement(f),m=b.innerHTML;b=b.getAttribute(\"sug\"); l.focus();y(l,m,b);u.emit(e,\"select\",a.id,l.id);h();f=null}function h(){e.style.display=\"none\";if(f!=null&&A!=null){d.getElement(f).onkeydown=A;A=null}}function B(a,b){for(a=b?a.nextSibling:a.previousSibling;a;a=b?a.nextSibling:a.previousSibling)if(d.hasTag(a,\"LI\"))if(a.style.display!=\"none\")return a;return null}function v(a){var b=a.parentNode;if(a.offsetTop+a.offsetHeight>b.scrollTop+b.clientHeight)b.scrollTop=a.offsetTop+a.offsetHeight-b.clientHeight;else if(a.offsetTop<b.scrollTop)b.scrollTop= a.offsetTop}$(\".Wt-domRoot\").add(e);jQuery.data(e,\"obj\",this);var s=this,d=u.WT,o=null,f=null,H=false,I=null,J=null,C=null,E=null,t=false;this.defaultValue=z;var A=null;this.showPopup=function(a){e.style.display=\"block\";E=o=null;A=a.onkeydown;a.onkeydown=function(b){s.editKeyDown(this,b||window.event)}};this.editMouseMove=function(a,b){if(r(a))a.style.cursor=d.widgetCoordinates(a,b).x>a.offsetWidth-16?\"default\":\"\"};this.showAt=function(a){h();f=a.id;t=true;s.refilter()};this.editClick=function(a, b){if(r(a))if(d.widgetCoordinates(a,b).x>a.offsetWidth-16)if(f!=a.id||!c())s.showAt(a);else{h();f=null}};this.editKeyDown=function(a,b){if(!r(a))return true;if(f!=a.id)if($(a).hasClass(\"Wt-suggest-onedit\")){f=a.id;t=false}else if($(a).hasClass(\"Wt-suggest-dropdown\")&&b.keyCode==40){f=a.id;t=true}else{f=null;return true}var l=o?d.getElement(o):null;if(c()&&l)if(b.keyCode==13||b.keyCode==9){n(l);d.cancelEvent(b);setTimeout(function(){a.focus()},0);return false}else if(b.keyCode==40||b.keyCode==38|| b.keyCode==34||b.keyCode==33){if(b.type.toUpperCase()==\"KEYDOWN\"){H=true;d.cancelEvent(b,d.CancelDefaultAction)}if(b.type.toUpperCase()==\"KEYPRESS\"&&H==true){d.cancelEvent(b);return false}var m=l,p=b.keyCode==40||b.keyCode==34;b=b.keyCode==34||b.keyCode==33?e.clientHeight/l.offsetHeight:1;var j;for(j=0;m&&j<b;++j){var w=B(m,p);if(!w)break;m=w}if(m&&d.hasTag(m,\"LI\")){l.className=\"\";m.className=\"active\";o=m.id}return false}return b.keyCode!=13&&b.keyCode!=9};this.filtered=function(a){I=a;s.refilter()}; this.refilter=function(){var a=o?d.getElement(o):null,b=d.getElement(f),l=D(b),m=e.childNodes,p=l(null);E=b.value;if(q!=0)if(p.length<q&&!t){h();return}else{var j=q==-1?p:p.substring(0,q);if(j!=I){if(j!=J){J=j;u.emit(e,\"filter\",j)}if(!t){h();return}}}var w=j=null;p=t&&p.length==0;var x,K;x=0;for(K=m.length;x<K;++x){var k=m[x];if(d.hasTag(k,\"LI\")){var F=k.firstChild;if(k.orig==null)k.orig=F.firstChild.innerHTML;var G=l(k.orig),L=p||G.match;if(G.suggestion!=F.firstChild.innerHTML)F.firstChild.innerHTML= G.suggestion;if(L){if(k.style.display!=\"\")k.style.display=\"\";if(j==null)j=k;if(x==this.defaultValue)w=k}else if(k.style.display!=\"none\")k.style.display=\"none\";if(k.className!=\"\")k.className=\"\"}}if(j==null)h();else{if(!c()){g(b);s.showPopup(b);a=null}if(!a||a.style.display==\"none\"){a=w||j;a.parentNode.scrollTop=0;o=a.id}a.className=\"active\";v(a)}};this.editKeyUp=function(a,b){if(f!=null)if(r(a))if(!(!c()&&(b.keyCode==13||b.keyCode==9)))if(b.keyCode==27||b.keyCode==37||b.keyCode==39)h();else if(a.value!= E)s.refilter();else(a=o?d.getElement(o):null)&&v(a)};e.onclick=i;e.onscroll=function(){if(C){clearTimeout(C);var a=d.getElement(f);a&&a.focus()}};this.delayHide=function(a){C=setTimeout(function(){C=null;if(e&&(a==null||f==a.id))h()},300)}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WSuggestionPopupStdMatcher",
				"function(u,e,y,D,q,z){function r(c){var g=c.value;c=c.selectionStart?c.selectionStart:g.length;for(var i=y?g.lastIndexOf(y,c-1)+1:0;i<c&&D.indexOf(g.charAt(i))!=-1;)++i;return{start:i,end:c}}this.match=function(c){var g=r(c),i=c.value.substring(g.start,g.end),n;n=q.length!=0?\"(^|(?:[\"+q+\"]))\":\"(^)\";n+=\"(\"+i.replace(new RegExp(\"([\\\\^\\\\\\\\\\\\][\\\\-.$*+?()|{}])\",\"g\"),\"\\\\$1\")+\")\";n=new RegExp(n,\"gi\");return function(h){if(!h)return i; var B=false;if(i.length){var v=h.replace(n,\"$1\"+u+\"$2\"+e);if(v!=h){B=true;h=v}}return{match:B,suggestion:h}}};this.replace=function(c,g,i){g=r(c);var n=c.value.substring(0,g.start)+i+z;if(g.end<c.value.length)n+=c.value.substring(g.end,c.value.length);c.value=n;if(c.selectionStart){c.selectionStart=g.start+i.length+z.length;c.selectionEnd=c.selectionStart}}}");
	}

	static String instantiateStdMatcher(WSuggestionPopup.Options options) {
		StringBuilder s = new StringBuilder();
		s.append("new Wt3_3_0.WSuggestionPopupStdMatcher(").append(
				WWebWidget.jsStringLiteral(options.highlightBeginTag)).append(
				", ").append(
				WWebWidget.jsStringLiteral(options.highlightEndTag)).append(
				", ");
		if (options.listSeparator != 0) {
			s.append(WWebWidget.jsStringLiteral("" + options.listSeparator));
		} else {
			s.append("null");
		}
		s.append(", ").append(WWebWidget.jsStringLiteral(options.whitespace))
				.append(", ").append(
						WWebWidget.jsStringLiteral(options.wordSeparators))
				.append(", ").append(
						WWebWidget.jsStringLiteral(options.appendReplacedText))
				.append(")");
		return s.toString();
	}
}
