java-pubcontrol
===============

Author: Konstantin Bokarius <kon@fanout.io>

A Java convenience library for publishing messages using the EPCP protocol.

License
-------

java-pubcontrol is offered under the MIT license. See the LICENSE file.

Installation
------------

java-pubcontrol is compatible with JDK 6 and above.

Maven:

```xml
<dependency>
  <groupId>org.fanout</groupId>
  <artifactId>pubcontrol</artifactId>
  <version>1.0.7</version>
</dependency>
```

HTTPS Publishing
----------------

Note that on some operating systems Java may require you to add the root CA certificate of the publishing server to the key store. This is particularly the case with OSX. Follow the steps outlined in this article to address the issue: http://nodsw.com/blog/leeland/2006/12/06-no-more-unable-find-valid-certification-path-requested-target

Also, if using Java 6 you may run into SNI issues. If this occurs we recommend HTTP-only publishing or upgrading to Java 7 or above.

Usage
-----

```java
import org.fanout.pubcontrol.*;
import javax.xml.bind.DatatypeConverter;
import java.util.*;

public class PubControlExample {
    private static class HttpResponseFormat implements Format {
        private String body;

        public HttpResponseFormat(String body) {
            this.body = body;
        }

        public String name() {
            return "http-response";
        }

        public Map<String, Object> export() {
            Map<String, Object> export = new HashMap<String, Object>();
            export.put("body", body);
            return export;
        }
    }

    private static class Callback implements PublishCallback {
        public void completed(boolean result, String message) {
            if (result)
                System.out.println("Publish successful");
            else
                System.out.println("Publish failed with message: " + message);
        }
    }

    public static void main(String[] args) {
        // PubControl can be initialized with or without an endpoint configuration.
        // Each endpoint can include optional JWT authentication info.
        // Multiple endpoints can be included in a single configuration.

        // Initialize PubControl with a single endpoint:
        List<Map<String, Object>> config = new ArrayList<Map<String, Object>>();
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put("uri", "https://api.fanout.io/realm/<realm>");
        entry.put("iss", "<realm>");
        entry.put("key", DatatypeConverter.parseBase64Binary("<key>"));
        config.add(entry);
        PubControl pub = new PubControl(config);

        // Add new endpoints by applying an endpoint configuration:
        config = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> entry1 = new HashMap<String, Object>();
        entry1.put("uri", "<myendpoint_uri_1>");
        config.add(entry1);
        HashMap<String, Object> entry2 = new HashMap<String, Object>();
        entry2.put("uri", "<myendpoint_uri_2>");
        config.add(entry2);
        pub.applyConfig(config);

        // Remove all configured endpoints:
        pub.removeAllClients();

        // Explicitly add an endpoint as a PubControlClient instance:
        PubControlClient pubClient = new PubControlClient("<myendpoint_uri");
        // Optionally set JWT auth: pubClient.setAuthJwt(<claims>, '<key>')
        // Optionally set basic auth: pubClient.setAuthBasic('<user>', '<password>')
        pub.addClient(pubClient);

        // Publish across all configured endpoints:
        List<String> channels = new ArrayList<String>();
        channels.add("test_channel");
        List<Format> formats = new ArrayList<Format>();
        formats.add(new HttpResponseFormat("Test publish!"));
        try {
            pub.publish(channels, new Item(formats, null, null));
        } catch (PublishFailedException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        pub.publishAsync(channels, new Item(formats, null, null), new Callback());

        // Wait for all async publish calls to complete:
        pub.finish();
    }
}
```
