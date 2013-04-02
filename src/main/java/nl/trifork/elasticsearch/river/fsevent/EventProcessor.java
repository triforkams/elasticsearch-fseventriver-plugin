package nl.trifork.elasticsearch.river.fsevent;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Handler for events from the file system.
 *
 * @author Thomas Zeeman
 */
public class EventProcessor {

    private static final ESLogger logger = Loggers.getLogger(EventProcessor.class);

    private final Indexer indexer;
    private final Path root;

    /**
     * Constructor with the minimal necessary parameters.
     *
     * @param bulkProcessor the bulk processor
     * @param indexConfiguration the index configuration
     */
    public EventProcessor(BulkProcessor bulkProcessor, IndexConfiguration indexConfiguration, Path root) {
        this.indexer = new Indexer(bulkProcessor, indexConfiguration);
        this.root = root;
    }

    /**
     * Handle events from a WatchService instance. Silently ignores any event kinds it does not know how to handle.
     *
     * @param event the event to handle; create, delete and modify are understood
     */
    public void process(WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            processCreateEntry((Path) event.context());
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            processModifyEntry((Path) event.context());
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            processDeleteEntry((Path) event.context());
        } else {
            logger.debug("Ignoring unknown event of type '{}'", kind.name());
        }
    }

    private void processCreateEntry(Path pathCreated) {
        logger.info("Entry created:" + pathCreated);

        File file = new File(root.toFile(), pathCreated.toString());

        if(file.isFile()) {
            indexer.indexFile(file);
        } else {
            logger.debug("path '{}' is not a file, ignoring it", pathCreated);
        }
    }

    private void processModifyEntry(Path pathModified) {
        logger.info("Entry modified:" + pathModified);

        File file = new File(root.toFile(), pathModified.toString());

        if(file.isFile()) {
            indexer.indexFile(file);
        } else {
            logger.debug("path '{}' is not a file, ignoring it", pathModified);
        }
    }

    private void processDeleteEntry(Path pathDeleted) {
        logger.info("Entry deleted:" + pathDeleted);

        File file = new File(root.toFile(), pathDeleted.toString());

        if(file.isFile()) {
            indexer.removeFileFromIndex(file);
        } else {
            logger.debug("path '{}' is not a file, ignoring it", pathDeleted);
        }
    }
}
