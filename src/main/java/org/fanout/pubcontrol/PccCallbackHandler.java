//    PccCallbackHandler.rb
//    ~~~~~~~~~
//    This module implements the PccCallbackHandler class.
//    :authors: Konstantin Bokarius.
//    :copyright: (c) 2015 by Fanout, Inc.
//    :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

// The PccCallbackHandler class is used internally for allowing
// an async publish call made from the PubControl class to execute a callback
// method only a single time. A PubControl instance can potentially contain
// many PubControlClient instances in which case this class tracks the number
// of successful publishes relative to the total number of PubControlClient
// instances. A failure to publish in any of the PubControlClient instances
// will result in a failed result passed to the callback method and the error
// from the first encountered failure.
public class PccCallbackHandler implements PublishCallback {
    private int numCalls;
    private PublishCallback callback;
    private boolean success;
    private String firstErrorMessage;

    // The initialize method accepts: a num_calls parameter which is an integer
    // representing the number of PubControlClient instances, and a callback
    // method to be executed after all publishing is complete.
    public PccCallbackHandler(int numCalls, PublishCallback callback) {
        this.numCalls = numCalls;
        this.callback = callback;
        this.success = true;
        this.firstErrorMessage = null;
    }

    // The completed method which is executed by PubControlClient when publishing
    // is complete. This method tracks the number of publishes performed and
    // when all publishes are complete it will call the callback method
    // originally specified by the consumer. If publishing failures are
    // encountered only the first error is saved and reported to the callback
    // method.
    public void completed(boolean success, String message) {
        if (!success && this.success) {
            this.success = false;
            this.firstErrorMessage = message;
        }
        this.numCalls -= 1;
        if (this.numCalls <= 0) {
            this.callback.completed(this.success, this.firstErrorMessage);
        }
    }
}
