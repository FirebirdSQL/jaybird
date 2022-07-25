package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.nativeoo.FbOONativeGDSFactoryPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FbOONativeServicesAPITest extends ServicesAPITest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supports(FbOONativeGDSFactoryPlugin.NATIVE_TYPE_NAME);

    @Test
    void testFbOONativeServicesAPI() {
        gdsType = GDSType.getType(FbOONativeGDSFactoryPlugin.NATIVE_TYPE_NAME);
        protocol = "jdbc:firebirdsql:fboo:native:";
        port = 5066;
    }
}
