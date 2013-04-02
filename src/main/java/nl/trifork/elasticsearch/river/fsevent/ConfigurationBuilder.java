package nl.trifork.elasticsearch.river.fsevent;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;

import java.util.Map;

/**
 * Builder for the configuration of the FS Event river.
 *
 * @author Thomas Zeeman
 */
public class ConfigurationBuilder {

    private static final ESLogger logger = Loggers.getLogger(ConfigurationBuilder.class);

    private static final int DEFAULT_BULK_SIZE = 100;
    private static final String DEFAULT_TYPE = "doc";
    private static final String DEFAULT_URL = "/mnt/es";

    public static DirectoryDefinition build(RiverName riverName, RiverSettings riverSettings) {
        logger.info("build configuration for '{}'", riverName.name());

        DirectoryDefinition definition = buildDirectoryDefinition(riverSettings.settings());

        definition.setIndexConfiguration(buildIndexConfiguration(riverName.name(), riverSettings.settings()));

        return definition;
    }

    private static DirectoryDefinition buildDirectoryDefinition(Map<String, Object> settings) {
        DirectoryDefinition definition;

        if (settings.containsKey("fs_event")) {
            definition = parseDirectoryConfiguration(settings);
        } else {
            definition = generateDefaultDirectoryDefinition();
        }

        return definition;
    }

    private static DirectoryDefinition parseDirectoryConfiguration(Map<String, Object> settings) {
        @SuppressWarnings("unchecked")
        Map<String, Object> directory = (Map<String, Object>) settings.get("fs_event");

        String name = XContentMapValues.nodeStringValue(directory.get("name"), null);
        String url = XContentMapValues.nodeStringValue(directory.get("url"), null);

        return new DirectoryDefinition(name, url);
    }

    private static DirectoryDefinition generateDefaultDirectoryDefinition() {
        logger.warn("You didn't define the fs_event river. Switching to defaults; using url '{}'", DEFAULT_URL);

        return new DirectoryDefinition("default_directory", DEFAULT_URL);
    }

    private static IndexConfiguration buildIndexConfiguration(String riverName, Map<String, Object> settings) {
        IndexConfiguration indexConfiguration;

        if (settings.containsKey("index")) {
            indexConfiguration = parseIndexConfiguration(riverName, settings);
        } else {
            indexConfiguration = generateDefaultIndexConfiguration(riverName);
        }

        return indexConfiguration;
    }

    private static IndexConfiguration parseIndexConfiguration(String riverName, Map<String, Object> settings) {
        @SuppressWarnings("unchecked")
        Map<String, Object> indexSettings = (Map<String, Object>) settings.get("index");

        String indexName = XContentMapValues.nodeStringValue(indexSettings.get("index"), riverName);
        String typeName = XContentMapValues.nodeStringValue(indexSettings.get("type"), DEFAULT_TYPE);
        int bulkSize = XContentMapValues.nodeIntegerValue(indexSettings.get("bulk_size"), DEFAULT_BULK_SIZE);

        return new IndexConfiguration(indexName, typeName, bulkSize);
    }

    private static IndexConfiguration generateDefaultIndexConfiguration(String riverName) {
        logger.warn("You didn't define the index. Switching to defaults; using name '{}'", riverName);

        return new IndexConfiguration(riverName, DEFAULT_TYPE, DEFAULT_BULK_SIZE);
    }
}
