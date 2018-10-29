package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

public class FbOOLocalDatabaseFactory extends AbstractNativeOODatabaseFactory {

    private static final FbOOLocalDatabaseFactory INSTANCE = new FbOOLocalDatabaseFactory();

    @Override
    protected FbClientLibrary getClientLibrary() {
        return FbOOClientDatabaseFactory.getInstance().getClientLibrary();
    }

    @Override
    protected <T extends IAttachProperties<T>> T filterProperties(T attachProperties) {
        T attachPropertiesCopy = attachProperties.asNewMutable();
        // Clear server name
        attachPropertiesCopy.setServerName(null);
        return attachPropertiesCopy;
    }

    public static FbOOLocalDatabaseFactory getInstance() {
        return INSTANCE;
    }
}
