//        PubControl.java
//        ~~~~~~~~~
//        This module implements the PubControl class.
//        :authors: Konstantin Bokarius.
//        :copyright: (c) 2015 by Fanout, Inc.
//        :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

import java.util.*;

// The PubControl class allows a consumer to manage a set of publishing
// endpoints and to publish to all of those endpoints via a single publish
// or publish_async method call. A PubControl instance can be configured
// either using a hash or array of hashes containing configuration information
// or by manually adding PubControlClient instances.
public class PubControl {
    private List<PubControlClient> clients;

    // Initialize with or without a configuration. A configuration can be applied
    // after initialization via the apply_config method.
    public PubControl(List<Map<String, Object>> config) {
        this.clients = new ArrayList<PubControlClient>();
        if (config != null)
            applyConfig(config);
    }

    // Remove all of the configured PubControlClient instances.
    public void removeAllClients() {
        this.clients.clear();
    }

    // Add the specified PubControlClient instance.
    public void addClient(PubControlClient client) {
        this.clients.add(client);
    }

    // Apply the specified configuration to this PubControl instance. The
    // configuration object can either be a hash or an array of hashes where
    // each hash corresponds to a single PubControlClient instance. Each hash
    // will be parsed and a PubControlClient will be created either using just
    // a URI or a URI and JWT authentication information.
    @SuppressWarnings({"unchecked"})
    public void applyConfig(List<Map<String, Object>> config) {
        for (Map<String, Object> entry : config) {
            String uri = null;
            if (entry.get("uri") != null)
                uri = (String)entry.get("uri");
            PubControlClient client = new PubControlClient(uri);
            Object iss = entry.get("iss");
            Object key = entry.get("key");
            if (iss != null && key != null)
                client.setAuthJwt((Map<String, Object>)iss, (byte[])key);
            this.clients.add(client);
        }
    }

    // The finish method is a blocking method that ensures that all asynchronous
    // publishing is complete for all of the configured PubControlClient
    // instances prior to returning and allowing the consumer to proceed.
    public void finish() {
        for (PubControlClient client : this.clients) {
            client.finish();
        }
    }

    // The synchronous publish method for publishing the specified item to the
    // specified channels for all of the configured PubControlClient instances.
    public void publish(List<String> channels, Item item)
            throws PublishFailedException {
        for (PubControlClient client : this.clients) {
            client.publish(channels, item);
        }
    }

    // The asynchronous publish method for publishing the specified item to the
    // specified channels on the configured endpoint. The callback method is
    // optional and will be passed the publishing results after publishing is
    // complete. Note that a failure to publish in any of the configured
    // PubControlClient instances will result in a failure result being passed
    // to the callback method along with the first encountered error message.
    public void publishAsync(List<String> channels, Item item,
            PublishCallback callback) {
        PublishCallback cb = null;
        if (callback != null)
            cb = new PccCallbackHandler(this.clients.size(), callback);
        for (PubControlClient client : this.clients)
            client.publishAsync(channels, item, cb);
    }
}
