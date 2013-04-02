package nl.trifork.elasticsearch.river.fsevent;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;

import java.util.concurrent.TimeUnit;

/**
 * Entry point for the file system event based river. Instantiates watchers for the given configuration.
 *
 * @author Thomas Zeeman
 */
public class FsEventRiver extends AbstractRiverComponent implements River {

    private final DirectoryDefinition directoryDefinition;
    private final Client client;
    private final BulkProcessor bulkProcessor;

    private volatile Thread thread;

    @Inject
    public FsEventRiver(final RiverName riverName, final RiverSettings riverSettings, final Client client) {
        super(riverName, riverSettings);

        this.client = client;
        directoryDefinition = ConfigurationBuilder.build(riverName, riverSettings);
        bulkProcessor = createBulkProcessor(client);
    }

    @Override
    public void start() {
        logger.info("start() fs event watcher for {}", directoryDefinition.getName());

        createIndexIfNotExisting();

        thread = EsExecutors.daemonThreadFactory("fs_event_watcher").newThread(new FsEventWatcher(bulkProcessor, directoryDefinition));
        thread.start();
    }

    @Override
    public void close() {
        logger.info("close() fs event watcher for {}", directoryDefinition.getName());

        if (thread != null) {
            thread.interrupt();
        }
        bulkProcessor.close();
    }

    private BulkProcessor createBulkProcessor(Client client) {
        return BulkProcessor.builder(client, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                logger.info("Going to execute new bulk composed of {} actions", request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                logger.info("Executed bulk composed of {} actions", request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                logger.warn("Error executing bulk", failure);
            }
        }).setBulkActions(directoryDefinition.getIndexConfiguration().getBulkSize()).setFlushInterval(new TimeValue(30, TimeUnit.SECONDS)).build();
    }

    private void createIndexIfNotExisting() {
        String indexName = directoryDefinition.getIndexConfiguration().getIndexName();
        logger.trace("Checking if index {} exists...", indexName);

        if (!client.admin().indices().prepareExists(indexName).execute().actionGet().exists()) {
            logger.info("Index {} does not exist, creating it now.", indexName);

            CreateIndexRequestBuilder createIndexRequest = client.admin().indices()
                    .prepareCreate(indexName);

            createIndexRequest.execute().actionGet();
        }
    }
}
