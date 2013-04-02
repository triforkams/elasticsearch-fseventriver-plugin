package nl.trifork.elasticsearch.river.fsevent;

/**
 * Placeholder for the configuration of a particular directory to be watched.
 *
 * @author Thomas Zeeman
 */
public class DirectoryDefinition {

    private final String name;
    private final String url;
    private IndexConfiguration indexConfiguration;

    /**
     * Constructor with the minimal mandatory properties.
     *
     * @param name unique name to refer to this definition
     * @param url  url of the directory                                                                                                                                                                                                             ory
     */
    public DirectoryDefinition(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public IndexConfiguration getIndexConfiguration() {
        return indexConfiguration;
    }

    public void setIndexConfiguration(IndexConfiguration indexConfiguration) {
        this.indexConfiguration = indexConfiguration;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
