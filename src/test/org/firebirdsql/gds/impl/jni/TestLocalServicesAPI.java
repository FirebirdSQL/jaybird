package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.GDSType;

public class TestLocalServicesAPI extends TestServicesAPI {

    public TestLocalServicesAPI() {
        gdsType = GDSType.getType(LocalGDSFactoryPlugin.LOCAL_TYPE_NAME);
        protocol = "jdbc:firebirdsql:local:";
    }
}
