package nl.trifork.elasticsearch.river.fsevent;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.river.River;

/**
 * @author Thomas Zeeman
 */
public class FsEventRiverModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(River.class).to(FsEventRiver.class).asEagerSingleton();
    }
}
