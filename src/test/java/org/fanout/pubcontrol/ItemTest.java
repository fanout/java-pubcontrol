import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import org.fanout.pubcontrol.*;

public class ItemTest {
    @Test
    public void testExport1() {
        List<Format> formats = new ArrayList<Format>();
        formats.add(new TestFormatSubClass1());
        Item item = new Item(formats, "id", "prev");
        Map<String, Object> out = item.export();
        assertEquals(out.get("id"), "id");
        assertEquals(out.get("prev-id"), "prev");
        assertEquals(((Map)out.get("test-name")).get("name"), "value");
    }

    @Test
    public void testExport2() {
        List<Format> formats = new ArrayList<Format>();
        formats.add(new TestFormatSubClass1());
        formats.add(new TestFormatSubClass2());
        Item item = new Item(formats, null, null);
        Map<String, Object> out = item.export();
        assertEquals(out.get("id"), null);
        assertEquals(out.get("prev-id"), null);
        assertEquals(((Map)out.get("test-name")).get("name"), "value");
        assertEquals(((Map)out.get("test-name2")).get("name2"), "value2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExportException() {
        List<Format> formats = new ArrayList<Format>();
        formats.add(new TestFormatSubClass1());
        formats.add(new TestFormatSubClass1());
        Item item = new Item(formats, null, null);
        item.export();
    }

    private class TestFormatSubClass1 implements Format {
        public String name() {
            return "test-name";
        }

        public Map<String, Object> export() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", "value");
            return map;
        }
    }

    private class TestFormatSubClass2 implements Format {
        public String name() {
            return "test-name2";
        }

        public Map<String, Object> export() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name2", "value2");
            return map;
        }
    }
}
