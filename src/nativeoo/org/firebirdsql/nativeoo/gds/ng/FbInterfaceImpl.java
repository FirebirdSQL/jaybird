package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.nativeoo.gds.ng.FbInterface.IMaster;

public class FbInterfaceImpl {
    public static IMaster getMasterInterface() {
        return FbOOClientDatabaseFactory.getInstance().getClientLibrary().fb_get_master_interface();
    }
}
