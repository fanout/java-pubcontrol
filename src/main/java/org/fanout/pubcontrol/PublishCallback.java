//        PublishCallback.java
//        ~~~~~~~~~
//        This module implements the PublishCallback interface.
//        :authors: Konstantin Bokarius.
//        :copyright: (c) 2015 by Fanout, Inc.
//        :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

import java.util.*;

/**
 * The PublishCallback interface is used to indicate the result of a publish.
 * The single interface method is called when publishing is completed.
 */
public interface PublishCallback {
    /**
     * The event method indicating that the publish has completed.
     * Accepts a boolean result and error message if an error occured.
     */
    void completed(boolean result, String errorMessage);
}
