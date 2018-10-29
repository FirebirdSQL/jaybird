package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.GDSType;

public class TestEmbeddedServicesAPI extends TestServicesAPI {

    public TestEmbeddedServicesAPI() {
        gdsType = GDSType.getType(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
        protocol = "jdbc:firebirdsql:embedded:";
    }
}
