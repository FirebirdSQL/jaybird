package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.GDSType;

public class TestNativeServicesAPI extends TestServicesAPI {

    public TestNativeServicesAPI() {
        gdsType = GDSType.getType(NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);
        protocol = "jdbc:firebirdsql:native:";
        port = 3050;
    }
}
