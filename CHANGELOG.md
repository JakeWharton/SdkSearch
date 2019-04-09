Change Log
==========

Version 1.4.1.0
---------------

*2019-04-09*

* Fix: Purge existing items on update so that only those from the proxy are cached. This is required
  because the format of a storage column changed and so existing installs still saw old data in
  search results which then crashed when clicked on.


Version 1.4.0.0
---------------

*2019-04-04*

* Fix: Deploy a proxy server to host the class list since developer.android.com keeps changing
  the format. This should allow future changes in the documentation site to be handled without
  requiring an app/extension update.

### Android

* Fix: Correctly report number of classes available. Previously the count was for packages and
  classes, despite search only working on classes.

### Chrome Extension

* Fix: Add 'view source' link asynchronously when the correct DOM element appears. The new
  documentation site loads new pages as partial HTML which it then inserts into the DOM to be
  faster (and completely fails at this and feels much slower).


Version 1.3.4.0
---------------

*2018-07-16*

### Chrome Extension

* Fix: Update general search URL to the new location after the DAC redesign.


Version 1.3.3.2
---------------

*2018-07-14*

### Chrome Extension

 * Fix: Omnibox and toolbar icon on 2x displays.
