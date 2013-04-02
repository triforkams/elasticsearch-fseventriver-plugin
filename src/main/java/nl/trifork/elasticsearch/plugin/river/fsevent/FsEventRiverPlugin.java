package nl.trifork.elasticsearch.plugin.river.fsevent;

import nl.trifork.elasticsearch.river.fsevent.FsEventRiverModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.river.RiversModule;

/**
 * @author Thomas Zeeman
 */
public class FsEventRiverPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "river-fsevent";
    }

    @Override
    public String description() {
        return "River FileSystem Event Plugin";
    }

    public void onModule(RiversModule module) {
        module.registerRiver("fsevent", FsEventRiverModule.class);
    }
}
