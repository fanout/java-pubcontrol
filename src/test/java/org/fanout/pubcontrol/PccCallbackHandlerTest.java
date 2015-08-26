import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import org.fanout.pubcontrol.*;

public class PccCallbackHandlerTest {
    @Test
    public void testHandler1() {
        Callback callback = new Callback();
        PccCallbackHandler handler = new PccCallbackHandler(1, callback);
        handler.completed(true, null);
        assertEquals(callback.callbackExecuted, true);
        assertEquals(callback.callbackResult, true);
        assertEquals(callback.callbackMessage, null);
    }

    @Test
    public void testHandler2() {
        Callback callback = new Callback();
        PccCallbackHandler handler = new PccCallbackHandler(3, callback);
        handler.completed(true, null);
        handler.completed(true, null);
        assertEquals(callback.callbackExecuted, false);
        handler.completed(true, null);
        assertEquals(callback.callbackExecuted, true);
        assertEquals(callback.callbackResult, true);
        assertEquals(callback.callbackMessage, null);
    }

    @Test
    public void testHandler3() {
        Callback callback = new Callback();
        PccCallbackHandler handler = new PccCallbackHandler(3, callback);
        handler.completed(false, "error");
        handler.completed(true, null);
        assertEquals(callback.callbackExecuted, false);
        handler.completed(true, null);
        assertEquals(callback.callbackExecuted, true);
        assertEquals(callback.callbackResult, false);
        assertEquals(callback.callbackMessage, "error");
    }

    private class Callback implements PublishCallback {
        public boolean callbackExecuted = false;
        public boolean callbackResult = false;
        public String callbackMessage = null;

        public void completed(boolean result, String errorMessage) {
            this.callbackExecuted = true;
            this.callbackResult = result;
            this.callbackMessage = errorMessage;
        }
    }
}
