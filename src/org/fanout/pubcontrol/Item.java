//        Item.java
//        ~~~~~~~~~
//        This module implements the Item class.
//        :authors: Konstantin Bokarius.
//        :copyright: (c) 2015 by Fanout, Inc.
//        :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

import java.util.*;

// The Item class is a container used to contain one or more format
// implementation instances where each implementation instance is of a
// different type of format. An Item instance may not contain multiple
// implementations of the same type of format. An Item instance is then
// serialized into a hash that is used for publishing to clients.
public class Item {
    private List<Format> formats;
    private String id;
    private String prevId;

    // The initialize method can accept either a single Format implementation
    // instance or an array of Format implementation instances. Optionally
    // specify an ID and/or previous ID to be sent as part of the message
    // published to the client.
    public Item(List<Format> formats, String id, String prevId) {
        this.id = id;
        this.prevId = prevId;
        this.formats = formats;
    }

    // The export method serializes all of the formats, ID, and previous ID
    // into a hash that is used for publishing to clients. If more than one
    // instance of the same type of Format implementation was specified then
    // an error will be raised.
    public Map<String, Object> export() {
        List<Class> formatTypes = new ArrayList<Class>();
        for (Format format : this.formats) {
            if (formatTypes.contains(format.getClass()))
                throw new IllegalArgumentException(
                        "more than one instance of " +
                        format.getClass().getSimpleName() +
                        " specified");
            formatTypes.add(format.getClass());
        }

        Map<String, Object> out = new HashMap<String, Object>();
        if (this.id != null && !this.id.isEmpty())
            out.put("id", this.id);
        if (this.prevId != null && !this.prevId.isEmpty())
            out.put("prev-id", this.prevId);

        for (Format format : this.formats)
            out.put(format.name(), format.export());

        return out;
    }
}
