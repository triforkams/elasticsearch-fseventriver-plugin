package nl.trifork.elasticsearch.river.fsevent;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.Base64;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.File;
import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Class handling the indexing of files.
 *
 * @author Thomas Zeeman
 */
public class Indexer {

    private static final ESLogger logger = Loggers.getLogger(Indexer.class);

    private final BulkProcessor bulkProcessor;
    private final IndexConfiguration indexConfiguration;

    /**
     * Constructor with the minimal necessary parameters.
     *
     * @param bulkProcessor the bulk processor
     */
    public Indexer(BulkProcessor bulkProcessor, IndexConfiguration indexConfiguration) {
        this.bulkProcessor = bulkProcessor;
        this.indexConfiguration = indexConfiguration;
    }

    /**
     * Index a given file.
     *
     * @param file the file to index
     */
    public void indexFile(File file) {
        logger.info("Indexing file {}", file.getName());
        assert(file.isFile());

        // TODO: generate a proper id
        IndexRequest indexRequest = Requests.indexRequest(indexConfiguration.getIndexName()).type(indexConfiguration.getType()).id(file.getAbsolutePath());
        indexRequest.source(buildSource(file));

        bulkProcessor.add(indexRequest);
    }

    /**
     * Remove a given file from the index.
     *
     * @param file the file to remove
     */
    public void removeFileFromIndex(File file) {
        logger.info("Removing file {} from index.", file.getName());
        assert(file.isFile());

        // TODO: generate a proper id
        DeleteRequest deleteRequest = Requests.deleteRequest(indexConfiguration.getIndexName()).type(indexConfiguration.getType()).id(file.getAbsolutePath());
        bulkProcessor.add(deleteRequest);
    }

    private XContentBuilder buildSource(File file) {
        try {
            byte[] data = FileUtils.readFileToByteArray(file);

            return jsonBuilder()
                .startObject()
                    .field("name", file.getName())
                    .field("lastModified", file.lastModified())
                    .startObject("file")
                        .field("_name", file.getAbsolutePath())
                        .field("content", Base64.encodeBytes(data))
                    .endObject()
                .endObject();
        } catch (IOException e) {
            logger.info("Exception while trying to read file {}", file.getName(), e);
            return null;
        }
    }
}
