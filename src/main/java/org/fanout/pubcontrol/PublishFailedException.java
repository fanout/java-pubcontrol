//        PublishFailedException.java
//        ~~~~~~~~~
//        This module implements the PublishFailedException exception.
//        :authors: Konstantin Bokarius.
//        :copyright: (c) 2015 by Fanout, Inc.
//        :license: MIT, see LICENSE for more details.

package org.fanout.pubcontrol;

// This exception is used to indicate that a publish failed to complete.
public class PublishFailedException extends Exception
{
    public PublishFailedException()
    {
    }

    public PublishFailedException(String message)
    {
        super(message);
    }

    public PublishFailedException(Throwable cause)
    {
        super(cause);
    }

    public PublishFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
