package nl.trifork.elasticsearch.river.fsevent;

/**
 * @author Thomas Zeeman
 */
public class IndexConfiguration {

    private final String indexName;
    private final String type;
    private final int bulkSize;

    public IndexConfiguration(String indexName, String type, int bulkSize) {
        this.indexName = indexName;
        this.type = type;
        this.bulkSize = bulkSize;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getType() {
        return type;
    }

    public int getBulkSize() {
        return bulkSize;
    }
}
