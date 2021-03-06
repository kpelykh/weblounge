/*
 * jQuery UI Tabs @VERSION
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Tabs
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.widget.js
 */
(function( $, undefined ) {

var tabId = 0,
	listId = 0;

function getNextTabId() {
	return ++tabId;
}

function getNextListId() {
	return ++listId;
}

$.widget( "ui.tabs", {
	options: {
		activate: null,
		beforeload: null,
		beforeActivate: null,
		collapsible: false,
		disabled: false,
		event: "click",
		fx: null, // e.g. { height: 'toggle', opacity: 'toggle', duration: 200 }
		load: null
	},

	_create: function() {
		var self = this,
			o = this.options;

		this.running = false;

		this.element.addClass( "ui-tabs ui-widget ui-widget-content ui-corner-all" );

		this._processTabs();

		// Selected tab
		// use "selected" option or try to retrieve:
		// 1. from fragment identifier in url
		// 2. from selected class attribute on <li>
		if ( o.active === undefined ) {
			if ( location.hash ) {
				this.anchors.each(function( i, a ) {
					if ( a.hash == location.hash ) {
						o.active = i;
						return false;
					}
				});
			}
			if ( typeof o.active !== "number" && this.lis.filter( ".ui-tabs-active" ).length ) {
				o.active = this.lis.index( this.lis.filter( ".ui-tabs-active" ) );
			}
			o.active = o.active || ( this.lis.length ? 0 : -1 );
		} else if ( o.active === null ) { // usage of null is deprecated, TODO remove in next release
			o.active = -1;
		}

		// sanity check - default to first tab...
		o.active = ( ( o.active >= 0 && this.anchors[ o.active ] ) || o.active < 0 )
			? o.active
			: 0;

		// Take disabling tabs via class attribute from HTML
		// into account and update option properly.
		if ( $.isArray( o.disabled ) ) {
			o.disabled = $.unique( o.disabled.concat(
				$.map( this.lis.filter( ".ui-state-disabled" ), function( n, i ) {
					return self.lis.index( n );
				})
			) ).sort();
		}

		this._setupFx( o.fx );

		this._refresh();

		// highlight selected tab
		this.panels.hide();
		this.lis.removeClass( "ui-tabs-active ui-state-active" );
		// check for length avoids error when initializing empty list
		if ( o.active >= 0 && this.anchors.length ) {
			this.active = this._findActive( o.active );
			var panel = self.element.find( self._sanitizeSelector( this.active.attr( "aria-controls" ) ) );

			panel.show();

			this.lis.eq( o.active ).addClass( "ui-tabs-active ui-state-active" );

			// seems to be expected behavior that the activate callback is fired
			self.element.queue( "tabs", function() {
				self._trigger( "activate", null, self._ui( self.active[ 0 ], panel[ 0 ] ) );
			});

			this.load( o.active );
		}

		// clean up to avoid memory leaks in certain versions of IE 6
		$( window ).bind( "unload.tabs", function() {
			self.lis.add( self.anchors ).unbind( ".tabs" );
			self.lis = self.anchors = self.panels = null;
		});
	},

	_setOption: function( key, value ) {
		if ( key == "active" ) {
			// _activate() will handle invalid values and update this.option
			this._activate( value );
			return
		} else {
			this.options[ key ] = value;
			this.refresh();
		}
	},

	_tabId: function( a ) {
		return ( $( a ).attr( "aria-controls" ) || "" ).replace( /^#/ , "" ) ||
			"ui-tabs-" + getNextTabId();
	},

	_sanitizeSelector: function( hash ) {
		// we need this because an id may contain a ":"
		return hash ? hash.replace( /:/g, "\\:" ) : "";
	},

	_ui: function( tab, panel ) {
		return {
			tab: tab,
			panel: panel,
			index: this.anchors.index( tab )
		};
	},

	refresh: function() {
		var self = this;

		this._processTabs();

		this._refresh();

		// Remove panels that we created that are missing their tab
		this.element.find(".ui-tabs-panel:data(destroy.tabs)").each( function( index, panel ) {
			var anchor = self.anchors.filter( "[aria-controls='#" + panel.id + "']");
			if ( !anchor.length ) {
				$( panel ).remove();
			}
		});
	},

	_refresh: function() {
		var self = this,
			o = this.options;

		this.element
			.toggleClass( "ui-tabs-collapsible", o.collapsible );

		this.list
			.addClass( "ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all" );

		this.lis
			.addClass( "ui-state-default ui-corner-top" );

		this.panels
			.addClass( "ui-tabs-panel ui-widget-content ui-corner-bottom" );

		if ( !o.disabled.length ) {
			o.disabled = false;
		}

		// disable tabs
		for ( var i = 0, li; ( li = this.lis[ i ] ); i++ ) {
			$( li ).toggleClass( "ui-state-disabled", $.inArray( i, o.disabled ) != -1 );
		}

		this._setupEvents( o.event );

		// remove all handlers, may run on existing tabs
		this.lis.unbind( ".tabs" );


		this._focusable( this.lis );
		this._hoverable( this.lis );
	},

	_processTabs: function() {
		var self = this,
			fragmentId = /^#.+/; // Safari 2 reports '#' for an empty hash

		this.list = this.element.find( "ol,ul" ).eq( 0 );
		this.lis = $( " > li:has(a[href])", this.list );
		this.anchors = this.lis.map(function() {
			return $( "a", this )[ 0 ];
		});
		this.panels = $( [] );

		this.anchors.each(function( i, a ) {
			var href = $( a ).attr( "href" ),
				hrefBase = href.split( "#" )[ 0 ],
				selector,
				panel,
				baseEl;

			// For dynamically created HTML that contains a hash as href IE < 8 expands
			// such href to the full page url with hash and then misinterprets tab as ajax.
			// Same consideration applies for an added tab with a fragment identifier
			// since a[href=#fragment-identifier] does unexpectedly not match.
			// Thus normalize href attribute...
			if ( hrefBase && ( hrefBase === location.toString().split( "#" )[ 0 ] ||
					( baseEl = $( "base" )[ 0 ]) && hrefBase === baseEl.href ) ) {
				href = a.hash;
				a.href = href;
			}

			// inline tab
			if ( fragmentId.test( href ) ) {
				selector = href;
				panel = self.element.find( self._sanitizeSelector( selector ) );
			// remote tab
			// prevent loading the page itself if href is just "#"
			} else if ( href && href !== "#" ) {
				var id = self._tabId( a );
				selector = "#" + id;
				panel = self.element.find( selector );
				if ( !panel.length ) {
					panel = self._createPanel( id );
					panel.insertAfter( self.panels[ i - 1 ] || self.list );
				}
			// invalid tab href
			} else {
				self.options.disabled.push( i );
			}

			if ( panel.length) {
				self.panels = self.panels.add( panel );
			}
			$( a ).attr( "aria-controls", selector );
		});
	},

	_createPanel: function( id ) {
		return $( "<div></div>" )
					.attr( "id", id )
					.addClass( "ui-tabs-panel ui-widget-content ui-corner-bottom" )
					.data( "destroy.tabs", true );
	},

	_setupFx: function( fx ) {
		// set up animations
		if ( fx ) {
			if ( $.isArray( fx ) ) {
				this.hideFx = fx[ 0 ];
				this.showFx = fx[ 1 ];
			} else {
				this.hideFx = this.showFx = fx;
			}
		}
	},

	// Reset certain styles left over from animation
	// and prevent IE's ClearType bug...
	_resetStyle: function ( $el, fx ) {
		$el.css( "display", "" );
		if ( !$.support.opacity && fx.opacity ) {
			$el[ 0 ].style.removeAttribute( "filter" );
		}
	},

	_showTab: function( clicked, show, event ) {
		var self = this;

		$( clicked ).closest( "li" ).addClass( "ui-tabs-active ui-state-active" );

		if ( this.showFx ) {
			self.running = true;
			show.hide()
				.animate( showFx, showFx.duration || "normal", function() {
					self._resetStyle( show, showFx );
					self.running = false;
					self._trigger( "activate", event, self._ui( clicked, show[ 0 ] ) );
				});
		} else {
			show.show();
			self._trigger( "activate", event, self._ui( clicked, show[ 0 ] ) );
		}
	},

	_hideTab: function( clicked, $hide ) {
		var self = this;

		if ( this.hideFx ) {
			self.running = true;
			$hide.animate( hideFx, hideFx.duration || "normal", function() {
				self.running = false;
				self.lis.removeClass( "ui-tabs-active ui-state-active" );
				self._resetStyle( $hide, hideFx );
				self.element.dequeue( "tabs" );
			});
		} else {
			self.lis.removeClass( "ui-tabs-active ui-state-active" );
			$hide.hide();
			self.element.dequeue( "tabs" );
		}
	},

	_setupEvents: function( event ) {
		// attach tab event handler, unbind to avoid duplicates from former tabifying...
		this.anchors.unbind( ".tabs" );

		if ( event ) {
			this.anchors.bind( event.split( " " ).join( ".tabs " ) + ".tabs",
				$.proxy( this, "_eventHandler" ) );
		}

		// disable click in any case
		this.anchors.bind( "click.tabs", function( event ){
			event.preventDefault();
		});
	},

	_eventHandler: function( event ) {
		event.preventDefault();
		var self = this,
			o = this.options,
			clicked = $( event.currentTarget ),
			$li = clicked.closest( "li" ),
			$hide = self.element.find( self._sanitizeSelector( $( this.active ).attr( "aria-controls" ) ) ),
			$show = self.element.find( self._sanitizeSelector( clicked.attr( "aria-controls" ) ) );

		// tab is already selected, but not collapsible
		if ( ( $li.hasClass( "ui-tabs-active" ) && !o.collapsible ) ||
			// can't switch durning an animation
			self.running ||
			// tab is disabled
			$li.hasClass( "ui-state-disabled" ) ||
			// tab is already loading
			$li.hasClass( "ui-tabs-loading" ) ||
			// allow canceling by beforeActivate event
			self._trigger( "beforeActivate", event, self._ui( clicked[ 0 ], $show[ 0 ] ) ) === false ) {
			clicked[ 0 ].blur();
			return;
		}

		o.active = self.anchors.index( clicked );

		self.active = clicked;

		if ( self.xhr ) {
			self.xhr.abort();
		}

		// if tab may be closed
		if ( o.collapsible ) {
			if ( $li.hasClass( "ui-tabs-active" ) ) {
				o.active = -1;
				self.active = null;

				self.element.queue( "tabs", function() {
					self._hideTab( clicked, $hide );
				}).dequeue( "tabs" );

				clicked[ 0 ].blur();
				return;
			} else if ( !$hide.length ) {
				self.element.queue( "tabs", function() {
					self._showTab( clicked, $show, event );
				});

				// TODO make passing in node possible, see also http://dev.jqueryui.com/ticket/3171
				self.load( self.anchors.index( clicked ) );

				clicked[ 0 ].blur();
				return;
			}
		}

		// show new tab
		if ( $show.length ) {
			if ( $hide.length ) {
				self.element.queue( "tabs", function() {
					self._hideTab( clicked, $hide );
				});
			}
			self.element.queue( "tabs", function() {
				self._showTab( clicked, $show, event );
			});

			self.load( self.anchors.index( clicked ) );
		} else {
			throw "jQuery UI Tabs: Mismatching fragment identifier.";
		}

		// Prevent IE from keeping other link focussed when using the back button
		// and remove dotted border from clicked link. This is controlled via CSS
		// in modern browsers; blur() removes focus from address bar in Firefox
		// which can become a usability
		if ( $.browser.msie ) {
			clicked[ 0 ].blur();
		}
	},

	_activate: function( index ) {
		var active = this._findActive( index )[ 0 ];

		// trying to activate the already active panel
		if ( this.active && active === this.active[ 0 ] ) {
			return;
		}

		// trying to collapse, simulate a click on the current active header
		active = active || this.active;

		this._eventHandler({
			target: active,
			currentTarget: active,
			preventDefault: $.noop
		});
	},

	_findActive: function( selector ) {
		return typeof selector === "number" ? this.anchors.eq( selector ) :
				typeof selector === "string" ? this.anchors.filter( "[href$='" + selector + "']" ) : $();
	},

    _getIndex: function( index ) {
		// meta-function to give users option to provide a href string instead of a numerical index.
		// also sanitizes numerical indexes to valid values.
		if ( typeof index == "string" ) {
			index = this.anchors.index( this.anchors.filter( "[href$=" + index + "]" ) );
		}

		return index;
	},

	_destroy: function() {
		var o = this.options;

		if ( this.xhr ) {
			this.xhr.abort();
		}

		this.element.removeClass( "ui-tabs ui-widget ui-widget-content ui-corner-all ui-tabs-collapsible" );

		this.list.removeClass( "ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all" );

		this.anchors.each(function() {
			var $this = $( this ).unbind( ".tabs" );
			$.each( [ "href", "load" ], function( i, prefix ) {
				$this.removeData( prefix + ".tabs" );
			});
		});

		this.lis.unbind( ".tabs" ).add( this.panels ).each(function() {
			if ( $.data( this, "destroy.tabs" ) ) {
				$( this ).remove();
			} else {
				$( this ).removeClass([
					"ui-state-default",
					"ui-corner-top",
					"ui-tabs-active",
					"ui-state-active",
					"ui-state-disabled",
					"ui-tabs-panel",
					"ui-widget-content",
					"ui-corner-bottom"
				].join( " " ) );
			}
		});

		return this;
	},

	enable: function( index ) {
		if ( index === undefined ) {
			for ( var i = 0, len = this.lis.length; i < len; i++ ) {
				this.enable( i );
			}
			return this;
		}
		index = this._getIndex( index );
		var o = this.options;
		if ( !o.disabled || ($.isArray( o.disabled ) && $.inArray( index, o.disabled ) == -1 ) ) {
			return;
		}

		this.lis.eq( index ).removeClass( "ui-state-disabled" );
		o.disabled = this.lis.map( function( i ) {
			return $(this).is( ".ui-state-disabled" ) ? i : null;
		}).get();

		if ( !o.disabled.length ) {
			o.disabled = false;
		}

		return this;
	},

	disable: function( index ) {
		if ( index === undefined ) {
			for ( var i = 0, len = this.lis.length; i < len; i++ ) {
				this.disable( i );
			}
			return this;
		}
		index = this._getIndex( index );
		var o = this.options;
		if ( !o.disabled || ($.isArray( o.disabled ) && $.inArray( index, o.disabled ) == -1 ) ) {
			this.lis.eq( index ).addClass( "ui-state-disabled" );

			o.disabled = this.lis.map( function( i ) {
				return $(this).is( ".ui-state-disabled" ) ? i : null;
			}).get();
			
			if ( o.disabled.length === this.anchors.length ) {
				o.disabled = true;
			}

		}

		return this;
	},

	load: function( index ) {
		index = this._getIndex( index );
		var self = this,
			o = this.options,
			a = this.anchors.eq( index )[ 0 ],
			panel = self.element.find( self._sanitizeSelector( $( a ).attr( "aria-controls" ) ) ),
			// TODO until #3808 is fixed strip fragment identifier from url
			// (IE fails to load from such url)
			url = $( a ).attr( "href" ).replace( /#.*$/, "" ),
			eventData = self._ui( a, panel[ 0 ] );

		if ( this.xhr ) {
			this.xhr.abort();
		}

		// not remote
		if ( !url ) {
			this.element.dequeue( "tabs" );
			return;
		}

		this.xhr = $.ajax({
			url: url,
			beforeSend: function( jqXHR, settings ) {
				return self._trigger( "beforeload", null,
						$.extend( { jqXHR: jqXHR, settings: settings }, eventData ) );
			}
		});

		if ( this.xhr ) {
			// load remote from here on
			this.lis.eq( index ).addClass( "ui-tabs-loading" );

			this.xhr
				.success( function( response ) {
					panel.html( response );
				})
				.complete( function( jqXHR, status ) {
					if ( status === "abort" ) {
						// stop possibly running animations
						self.element.queue( [] );
						self.panels.stop( false, true );

						// "tabs" queue must not contain more than two elements,
						// which are the callbacks for the latest clicked tab...
						self.element.queue( "tabs", self.element.queue( "tabs" ).splice( -2, 2 ) );

						delete this.xhr;
					}

					self.lis.eq( index ).removeClass( "ui-tabs-loading" );

					self._trigger( "load", null, eventData );
				});
		}

		// last, so that load event is fired before show...
		self.element.dequeue( "tabs" );

		return this;
	}

});

$.extend( $.ui.tabs, {
	version: "@VERSION"
});

// DEPRECATED
if ( $.uiBackCompat !== false ) {

	// ajaxOptions and cache options
	(function( $, prototype ) {
		$.extend( prototype.options, {
			ajaxOptions: null,
			cache: false
		});

		var _create = prototype._create,
			_setOption = prototype._setOption,
			_destroy = prototype._destroy,
			oldurl = prototype._url;

		$.extend( prototype, {
			_create: function() {
				_create.call( this );

				var self = this;

				this.element.bind( "tabsbeforeload", function( event, ui ) {
					// tab is already cached
					if ( $.data( ui.tab, "cache.tabs" ) ) {
						event.preventDefault();
						return;
					}

					$.extend( ui.settings, self.options.ajaxOptions, {
						error: function( xhr, s, e ) {
							try {
								// Passing index avoid a race condition when this method is
								// called after the user has selected another tab.
								// Pass the anchor that initiated this request allows
								// loadError to manipulate the tab content panel via $(a.hash)
								self.options.ajaxOptions.error( xhr, s, ui.index, ui.tab );
							}
							catch ( e ) {}
						}
					});

					ui.jqXHR.success( function() {
						if ( self.options.cache ) {
							$.data( ui.tab, "cache.tabs", true );
						}
					});
				});
			},

			_setOption: function( key, value ) {
				// reset cache if switching from cached to not cached
				if ( key === "cache" && value === false ) {
					this.anchors.removeData( "cache.tabs" );
				}
				_setOption.apply( this, arguments );
			},

			_destroy: function() {
				this.anchors.removeData( "cache.tabs" );
				_destroy.call( this );
			},

			url: function( index, url ){
				this.anchors.eq( index ).removeData( "cache.tabs" );
				oldurl.apply( this, arguments );
			}
		});
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// abort method
	(function( $, prototype ) {
		prototype.abort = function() {
			if ( this.xhr ) {
				this.xhr.abort();
			}
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// spinner
	(function( $, prototype ) {
		$.extend( prototype.options, {
			spinner: "<em>Loading&#8230;</em>"
		});

		var _create = prototype._create;
		prototype._create = function() {
			_create.call( this );
			var self = this;

			this.element.bind( "tabsbeforeload", function( event, ui ) {
				if ( self.options.spinner ) {
					var span = $( "span", ui.tab );
					if ( span.length ) {
						span.data( "label.tabs", span.html() ).html( self.options.spinner );
					}
				}
				ui.jqXHR.complete( function() {
					if ( self.options.spinner ) {
						var span = $( "span", ui.tab );
						if ( span.length ) {
							span.html( span.data( "label.tabs" ) ).removeData( "label.tabs" );
						}
					}
				});
			});
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// enable/disable events
	(function( $, prototype ) {
		$.extend( prototype.options, {
			enable: null,
			disable: null
		});

		var enable = prototype.enable,
			disable = prototype.disable;

		prototype.enable = function( index ) {
			var o = this.options,
				trigger;

			if ( index && o.disabled || ($.isArray( o.disabled ) && $.inArray( index, o.disabled ) !== -1 ) ) {
				trigger = true;
			}

			enable.apply( this, arguments );

			if ( trigger ) {
				this._trigger( "enable", null, this._ui( this.anchors[ index ], this.panels[ index ] ) );
			}
		};

		prototype.disable = function( index ) {
			var o = this.options,
				trigger;

			if ( index && !o.disabled || ($.isArray( o.disabled ) && $.inArray( index, o.disabled ) == -1 ) ) {
				trigger = true;
			}

			disable.apply( this, arguments );

			if ( trigger ) {
				this._trigger( "disable", null, this._ui( this.anchors[ index ], this.panels[ index ] ) );
			}
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// add/remove methods and events
	(function( $, prototype ) {
		$.extend( prototype.options, {
			add: null,
			remove: null,
			tabTemplate: "<li><a href='#{href}'><span>#{label}</span></a></li>"
		});

		prototype.add = function( url, label, index ) {
			if ( index === undefined ) {
				index = this.anchors.length;
			}

			var self = this,
				o = this.options,
				$li = $( o.tabTemplate.replace( /#\{href\}/g, url ).replace( /#\{label\}/g, label ) ),
				id = !url.indexOf( "#" ) ? url.replace( "#", "" ) : this._tabId( $( "a", $li )[ 0 ] );

			$li.addClass( "ui-state-default ui-corner-top" ).data( "destroy.tabs", true );

			// try to find an existing element before creating a new one
			var $panel = self.element.find( "#" + id );
			if ( !$panel.length ) {
				$panel = self._createPanel( id );
			}
			$panel.addClass( "ui-tabs-panel ui-widget-content ui-corner-bottom" ).hide();

			if ( index >= this.lis.length ) {
				$li.appendTo( this.list );
				$panel.appendTo( this.list[ 0 ].parentNode );
			} else {
				$li.insertBefore( this.lis[ index ] );
				$panel.insertBefore( this.panels[ index ] );
			}

			o.disabled = $.map( o.disabled, function( n, i ) {
				return n >= index ? ++n : n;
			});

			this.refresh();

			if ( this.anchors.length == 1 ) {
				o.active = o.selected = 0;
				$li.addClass( "ui-tabs-active ui-state-active" );
				$panel.show();
				this.element.queue( "tabs", function() {
					self._trigger( "activate", null, self._ui( self.anchors[ 0 ], self.panels[ 0 ] ) );
				});

				this.load( 0 );
			}

			this._trigger( "add", null, this._ui( this.anchors[ index ], this.panels[ index ] ) );
			return this;
		};

		prototype.remove = function( index ) {
			index = this._getIndex( index );
			var o = this.options,
				$li = this.lis.eq( index ).remove(),
				$panel = this.panels.eq( index ).remove();

			// If selected tab was removed focus tab to the right or
			// in case the last tab was removed the tab to the left.
			if ( $li.hasClass( "ui-tabs-active" ) && this.anchors.length > 1) {
				this._activate( index + ( index + 1 < this.anchors.length ? 1 : -1 ) );
			}

			o.disabled = $.map(
				$.grep( o.disabled, function(n, i) {
					return n != index;
				}),
				function( n, i ) {
					return n >= index ? --n : n;
				});

			this.refresh();

			this._trigger( "remove", null, this._ui( $li.find( "a" )[ 0 ], $panel[ 0 ] ) );
			return this;
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// length method
	(function( $, prototype ) {
		prototype.length = function() {
			return this.anchors.length;
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// url method
	(function( $, prototype ) {
		prototype.url = function( index, url ) {
			this.anchors.eq( index ).attr( "href", url );
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// _tabId method
	(function( $, prototype ) {
		$.extend( prototype.options, {
			idPrefix: "ui-tabs-"
		});

		var _tabId = prototype._tabId;
		prototype._tabId = function( a ) {
			return ( $( a ).attr( "aria-controls" ) || "" ).replace( /^#/ , "" ) ||
				a.title && a.title.replace( /\s/g, "_" ).replace( /[^\w\u00c0-\uFFFF-]/g, "" ) ||
				this.options.idPrefix + getNextTabId();
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// _tabId method
	(function( $, prototype ) {
		$.extend( prototype.options, {
			panelTemplate: "<div></div>"
		});

		var _createPanel = prototype._createPanel;
		prototype._createPanel = function( id ) {
			return $( this.options.panelTemplate )
					.attr( "id", id )
					.addClass( "ui-tabs-panel ui-widget-content ui-corner-bottom" )
					.data( "destroy.tabs", true );
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// selected option
	(function( $, prototype ) {
		var _create = prototype._create,
			_setOption = prototype._setOption,
			_eventHandler = prototype._eventHandler;

		prototype._create = function() {
			var options = this.options;
			if ( options.active === undefined && options.selected !== undefined ) {
				options.active = options.selected;
			}
			_create.call( this );
			options.selected = options.active;
		};

		prototype._setOption = function( key, value ) {
			var options = this.options;
			if ( key === "selected" ) {
				key = "active";
			}
			_setOption.apply( this, arguments );
			if ( key === "active" ) {
				options.selected = options.active ;
			}
		};

		prototype._eventHandler = function( event ) {
			_eventHandler.apply( this, arguments );
			this.options.selected = this.options.active ;
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// show and select event
	(function( $, prototype ) {
		$.extend( prototype.options, {
			show: null,
			select: null
		});
		var _trigger = prototype._trigger;

		prototype._trigger = function( type, event, data ) {
			var ret = _trigger.apply( this, arguments );
			if ( !ret ) {
				return false;
			}
			if ( type === "beforeActivate" ) {
				ret = _trigger.call( this, "select", event, data );
			} else if ( type === "activate" ) {
				ret = _trigger.call( this, "show", event, data );
			}
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// select method
	(function( $, prototype ) {
		prototype.select = function( index ) {
			index = this._getIndex( index );
			if ( index == -1 ) {
				if ( this.options.collapsible && this.options.selected != -1 ) {
					index = this.options.selected;
				} else {
					return;
				}
			}
			this.anchors.eq( index ).trigger( this.options.event + ".tabs" );
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );

	// cookie option
	(function( $, prototype ) {
		$.extend( prototype.options, {
			cookie: null // e.g. { expires: 7, path: '/', domain: 'jquery.com', secure: true }
		});

		var _create = prototype._create,
			_refresh = prototype._refresh,
			_eventHandler = prototype._eventHandler,
			_destroy = prototype._destroy;

		prototype._create = function() {
			var o = this.options;
			if ( o.active === undefined ) {
				if ( typeof o.active !== "number" && o.cookie ) {
					o.active = parseInt( this._cookie(), 10 );
				}
			}
			_create.call( this );
		};

		prototype._cookie = function() {
			var cookie = this.cookie ||
				( this.cookie = this.options.cookie.name || "ui-tabs-" + getNextListId() );
			return $.cookie.apply( null, [ cookie ].concat( $.makeArray( arguments ) ) );
		};

		prototype._refresh = function() {
			_refresh.call( this );

			// set or update cookie after init and add/remove respectively
			if ( this.options.cookie ) {
				this._cookie( this.options.active, this.options.cookie );
			}
		};

		prototype._eventHandler = function( event ) {
			_eventHandler.apply( this, arguments );

			if ( this.options.cookie ) {
				this._cookie( this.options.active, this.options.cookie );
			}
		};

		prototype._destroy = function() {
			_destroy.call( this );

			if ( this.options.cookie ) {
				this._cookie( null, this.options.cookie );
			}
		};
	}( jQuery, jQuery.ui.tabs.prototype ) );
}

})( jQuery );
