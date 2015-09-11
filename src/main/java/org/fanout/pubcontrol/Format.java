//        Format.java
//        ~~~~~~~~~
//        This module implements the Format class.
//        :authors: Konstantin Bokarius.
//        :copyright: (c) 2015 by Fanout, Inc.
//        :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

import java.util.*;

/**
 * Vase class for all publishing formats that are included in the Item class.
 * Examples of format implementations include JsonObjectFormat and HttpStreamFormat.
 */
public interface Format {

    /**
     * The name of the format which should return a string.
     * Examples include 'json-object' and 'http-response'.
     */
    String name();

    /**
     * Returns a format-specific object containing the required format-specific data.
     */
    Object export();
}
