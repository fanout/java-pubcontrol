//        PubControlClient.java
//        ~~~~~~~~~
//        This module implements the PubControlClient class.
//        :authors: Konstantin Bokarius.
//        :copyright: (c) 2015 by Fanout, Inc.
//        :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

import java.util.concurrent.locks.*;
import java.util.*;

import java.security.Key;
import javax.xml.bind.DatatypeConverter;
import javax.crypto.spec.SecretKeySpec;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.impl.crypto.MacProvider;

// The PubControlClient class allows consumers to publish either synchronously
// or asynchronously to an endpoint of their choice. The consumer wraps a Format
// class instance in an Item class instance and passes that to the publish
// methods. The async publish method has an optional callback parameter that
// is called after the publishing is complete to notify the consumer of the
// result.
public class PubControlClient {
    private String uri;
    private final Lock lock = new ReentrantLock();
    private final Lock pubWorkerLock = new ReentrantLock();
    private final Condition pubWorkerCond = lock.newCondition();
    private PubWorker pubWorker;
    private Deque<Object[]> reqQueue = new LinkedList<Object[]>();
    private String authBasicUser;
    private String authBasicPass;;
    private Map<String, Object> authJwtClaim;
    private byte[] authJwtKey;

    // Initialize this class with a URL representing the publishing endpoint.
    public PubControlClient(String uri) {
        this.uri = uri;
    }

    // Call this method and pass a username and password to use basic
    // authentication with the configured endpoint.
    public void setAuthBasic(String userName, String password) {
        this.lock.lock();
        this.authBasicUser = userName;
        this.authBasicPass = password;
        this.lock.unlock();
    }

    // Call this method and pass a claim and key to use JWT authentication
    // with the configured endpoint.
    public void setAuthJwt(Map<String, Object> claims, byte[] key) {
        this.lock.lock();
        this.authJwtClaim = claims;
        this.authJwtKey = key;
        this.lock.unlock();
    }

    // The synchronous publish method for publishing the specified item to the
    // specified channel on the configured endpoint.
    public void publish(List<String> channels, Item item) {
        List<Map<String, Object>> exports = new ArrayList<Map<String, Object>>();
        for (String channel : channels) {
            Map<String, Object> export = item.export();
            export.put("channel", channel);
            exports.add(export);
        }
        String uri = null;
        String auth = null;
        this.lock.lock();
        uri = this.uri;
        auth = this.genAuthHeader();
        this.lock.unlock();
        //this.pubcall(uri, auth, exports);
    }

    // The asynchronous publish method for publishing the specified item to the
    // specified channel on the configured endpoint. The callback method is
    // optional and will be passed the publishing results after publishing is
    // complete.
    public void publishAsync(List<String> channels, Item item, PublishCallback callback) {
        List<Map<String, Object>> exports = new ArrayList<Map<String, Object>>();
        for (String channel : channels) {
            Map<String, Object> export = item.export();
            export.put("channel", channel);
            exports.add(export);
        }
        String uri = null;
        String auth = null;
        this.lock.lock();
        uri = this.uri;
        auth = this.genAuthHeader();
        this.ensureThread();
        this.lock.unlock();
        Object[] req = {"pub", uri, auth, exports, callback};
        this.queueReq(req);
    }

    // The finish method is a blocking method that ensures that all asynchronous
    // publishing is complete prior to returning and allowing the consumer to
    // proceed.
    public void finish() throws InterruptedException {
        this.lock.lock();
        if (this.pubWorker != null) {
            Object[] req = {"stop"};
            this.queueReq(req);
            this.pubWorker.join();
            this.pubWorker = null;
        }
        this.lock.unlock();
    }

    // An internal method that ensures that asynchronous publish calls are
    // properly processed. This method initializes the required class fields,
    // starts the pubworker worker thread, and is meant to execute only when
    // the consumer makes an asynchronous publish call.
    private void ensureThread() {
        if (this.pubWorker == null) {
            this.pubWorker = new PubWorker();
            this.pubWorker.run();
        }
    }

    // An internal method for adding an asynchronous publish request to the
    // publishing queue. This method will also activate the pubworker worker
    // thread to make sure that it process any and all requests added to
    // the queue.
    public void queueReq(Object[] req) {
        this.pubWorkerLock.lock();
        this.reqQueue.addLast(req);
        this.pubWorkerCond.signal();
        this.pubWorkerLock.unlock();
    }

    // An internal method used to generate an authorization header. The
    // authorization header is generated based on whether basic or JWT
    // authorization information was provided via the publicly accessible
    // 'set_*_auth' methods defined above.
    private String genAuthHeader() {
        return "";
    }

    /*
        def gen_auth_header
            if !@auth_basic_user.nil?
                return 'Basic ' + Base64.encode64(
                        "//{@auth_basic_user}://{@auth_basic_pass}")
            elsif !@auth_jwt_claim.nil?
                if !@auth_jwt_claim.key?('exp')
                    claim = @auth_jwt_claim.clone
                    claim['exp'] = PubControlClient.timestamp_utcnow + 3600
                else
                    claim = @auth_jwt_claim
                end
                return 'Bearer ' + JWT.encode(claim, @auth_jwt_key)
            else
                return nil
            end
        end
    end
    // An internal method for preparing the HTTP POST request for publishing
    // data to the endpoint. This method accepts the URI endpoint, authorization
    // header, and a list of items to publish.
    def pubcall(uri, auth_header, items)
        uri = URI(uri + '/publish/')
        content = Hash.new
        content['items'] = items
        request = Net::HTTP::Post.new(uri.request_uri)
        request.body = content.to_json
        if !auth_header.nil?
            request['Authorization'] = auth_header
        end
        request['Content-Type'] = 'application/json'
        use_ssl = uri.scheme == 'https'
        response = make_http_request(uri, use_ssl, request)
        if !response.kind_of? Net::HTTPSuccess
            raise 'failed to publish: ' + response.class.to_s + ' ' +
                    response.message
        end
    end

    // An internal method for making the specified HTTP request to the
    // specified URI with an option that determines whether to use
    // SSL.
    def make_http_request(uri, use_ssl, request)
        response = Net::HTTP.start(uri.host, uri.port, use_ssl: use_ssl) do |http|
            http.request(request)
        end
        return response
    end

    // An internal method for publishing a batch of requests. The requests are
    // parsed for the URI, authorization header, and each request is published
    // to the endpoint. After all publishing is complete, each callback
    // corresponding to each request is called (if a callback was originally
    // provided for that request) and passed a result indicating whether that
    // request was successfully published.
    def pubbatch(reqs)
        raise 'reqs length == 0' unless reqs.length > 0
        uri = reqs[0][0]
        auth_header = reqs[0][1]
        items = Array.new
        callbacks = Array.new
        reqs.each do |req|
            if req[2].is_a? Array
                items = items + req[2]
            else
                items.push(req[2])
            end
            callbacks.push(req[3])
        end
        begin
            pubcall(uri, auth_header, items)
            result = [true, '']
        rescue => e
            result = [false, e.message]
        end
        callbacks.each do |callback|
            if !callback.nil?
                callback.call(result[0], result[1])
            end
        end
    end

*/

    // An internal class that is meant to run as a separate thread and process
    // asynchronous publishing requests. The method runs continously and
    // publishes requests in batches containing a maximum of 10 requests. The
    // method completes and the thread is terminated only when a 'stop' command
    // is provided in the request queue.
    private class PubWorker extends Thread {
        public void run() {
            /*
            def pubworker
                quit = false
                while !quit do
                    @thread_mutex.lock
                    if @req_queue.length == 0
                        @thread_cond.wait(@thread_mutex)
                        if @req_queue.length == 0
                            @thread_mutex.unlock
                            next
                        end
                    end
                    reqs = Array.new
                    while @req_queue.length > 0 and reqs.length < 10 do
                        m = @req_queue.pop_front
                        if m[0] == 'stop'
                            quit = true
                            break
                        end
                        reqs.push([m[1], m[2], m[3], m[4]])
                    end
                    @thread_mutex.unlock
                    if reqs.length > 0
                        pubbatch(reqs)
                    end
                end
            end
            */
        }
    }
}
