package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class NativeServicesAPITest extends ServicesAPITest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supports(NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);

    @Test
    void testNativeServicesAPI() {
        gdsType = GDSType.getType(NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);
        protocol = "jdbc:firebirdsql:native:";
        port = 5066;
    }
}
