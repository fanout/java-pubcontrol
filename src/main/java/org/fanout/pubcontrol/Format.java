//        Format.java
//        ~~~~~~~~~
//        This module implements the Format class.
//        :authors: Konstantin Bokarius.
//        :copyright: (c) 2015 by Fanout, Inc.
//        :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

import java.util.*;

// The Format interface is provided as a base class for all publishing
// formats that are included in the Item class. Examples of format
// implementations include JsonObjectFormat and HttpStreamFormat.
public interface Format {

    // The name of the format which should return a string. Examples
    // include 'json-object' and 'http-response'
    String name();

    // The export method which should return a format-specific object
    // containing the required format-specific data.
    Object export();
}
