package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.nativeoo.FbOOEmbeddedGDSFactoryPlugin;

public class TestFbOOEmbeddedServicesAPI extends TestServicesAPI {

    public TestFbOOEmbeddedServicesAPI() {
        gdsType = GDSType.getType(FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
        protocol = "jdbc:firebirdsql:fboo:embedded:";
    }
}
