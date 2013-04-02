package nl.trifork.elasticsearch.river.fsevent;

import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Thomas Zeeman
 */
public class ConfigurationBuilderTest {

    @Test(expected = AssertionError.class)
    public void testBuildWithNullConfiguration() {
        ConfigurationBuilder.build(null, null);
    }

    @Test
    public void testBuildWithNoConfiguration() {
        DirectoryDefinition definition = ConfigurationBuilder.build(new RiverName("", "default_test"), new RiverSettings(null, new HashMap<String, Object>()));

        assertNotNull(definition);
        assertEquals("/mnt/es", definition.getUrl());
        assertEquals("default_directory", definition.getName());
        assertNotNull(definition.getIndexConfiguration());
        assertEquals("default_test", definition.getIndexConfiguration().getIndexName());
        assertEquals("doc", definition.getIndexConfiguration().getType());
        assertEquals(100, definition.getIndexConfiguration().getBulkSize());
    }

    @Test
    public void testBuildWithConfiguration() {
        Map<String, Object> fsEvent = new HashMap<>();
        fsEvent.put("name", "test_directory");
        fsEvent.put("url", "./target/data");
        Map<String, Object> index = new HashMap<>();
        index.put("type", "test_type");
        index.put("bulk_size", 50);
        Map<String, Object> settings = new HashMap<>();
        settings.put("fs_event", fsEvent);
        settings.put("index", index);

        DirectoryDefinition definition = ConfigurationBuilder.build(new RiverName("", "test_name"), new RiverSettings(null, settings));

        assertNotNull(definition);
        assertEquals("./target/data", definition.getUrl());
        assertEquals("test_directory", definition.getName());
        assertNotNull(definition.getIndexConfiguration());
        assertEquals("test_name", definition.getIndexConfiguration().getIndexName());
        assertEquals("test_type", definition.getIndexConfiguration().getType());
        assertEquals(50, definition.getIndexConfiguration().getBulkSize());
    }
}
