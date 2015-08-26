import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import org.fanout.pubcontrol.*;

public class FormatTest {
    @Test
    public void testInterface() {
        TestFormatSubClass testClass = new TestFormatSubClass();
    }

    private class TestFormatSubClass implements Format {
        public String name() {
            return "test-name";
        }

        public Map<String, Object> export() {
            return new HashMap<String, Object>();
        }
    }
}
