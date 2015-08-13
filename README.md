java-pubcontrol
===============

Author: Konstantin Bokarius <kon@fanout.io>

A Java convenience library for publishing messages using the EPCP protocol.

License
-------

java-pubcontrol is offered under the MIT license. See the LICENSE file.

Installation
------------

```sh
```

Usage
-----

```java
require 'pubcontrol'

class HttpResponseFormat < Format
  def initialize(body)
    @body = body
  end

  def name
    return 'http-response'
  end

  def export
    return {'body' => @body}
  end
end

def callback(result, message)
  if result
    puts 'Publish successful'
  else
    puts 'Publish failed with message: ' + message.to_s
  end
end

# PubControl can be initialized with or without an endpoint configuration.
# Each endpoint can include optional JWT authentication info.
# Multiple endpoints can be included in a single configuration.

# Initialize PubControl with a single endpoint:
pub = PubControl.new({'uri' => 'https://api.fanout.io/realm/<myrealm>',
    'iss' => '<myrealm>', 'key' => Base64.decode64('<realmkey>')})

# Add new endpoints by applying an endpoint configuration:
pub.apply_config([{'uri' => '<myendpoint_uri_1>'},
    {'uri' => '<myendpoint_uri_2>'}])

# Remove all configured endpoints:
pub.remove_all_clients

# Explicitly add an endpoint as a PubControlClient instance:
pubclient = PubControlClient.new('<myendpoint_uri>')
# Optionally set JWT auth: pubclient.set_auth_jwt(<claim>, '<key>')
# Optionally set basic auth: pubclient.set_auth_basic('<user>', '<password>')
pub.add_client(pubclient)

# Publish across all configured endpoints:
pub.publish('<channel>', Item.new(HttpResponseFormat.new('Test publish!')))
pub.publish_async('<channel>', Item.new(HttpResponseFormat.new(
    'Test async publish!')), method(:callback))

# Wait for all async publish calls to complete:
pub.finish
```
