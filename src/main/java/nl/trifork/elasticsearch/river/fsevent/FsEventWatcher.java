package nl.trifork.elasticsearch.river.fsevent;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * Watcher for file system events.
 *
 * @author Thomas Zeeman
 */
public class FsEventWatcher implements Runnable {

    private static final ESLogger logger = Loggers.getLogger(FsEventWatcher.class);

    private final EventProcessor processor;
    private final Path rootPath;

    public FsEventWatcher(BulkProcessor bulkProcessor, DirectoryDefinition directory) {
        this.rootPath = FileSystems.getDefault().getPath(directory.getUrl());
        this.processor = new EventProcessor(bulkProcessor, directory.getIndexConfiguration(), rootPath);
    }

    @Override
    public void run() {
        try {
            WatchService watchService = this.rootPath.getFileSystem().newWatchService();
            this.rootPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            while(true) {
                WatchKey watchKey = watchService.take();

                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    processor.process(event);
                }

                // if the watched directory gets deleted, get out of run method
                if (!watchKey.reset()) {
                    logger.info("Deleting FsEventWatcher for deleted path {}.", rootPath);
                    watchKey.cancel();
                    watchService.close();
                    break;
                }
            }
        } catch (InterruptedException | ClosedWatchServiceException e) {
            logger.info("We got interrupted. Goodbye.");
            logger.trace("Caught exception: {}", e);
        } catch (IOException ioe) {
            logger.info("IOException, oops.", ioe);
        }
    }
}
