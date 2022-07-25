package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.nativeoo.FbOOEmbeddedGDSFactoryPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FbOOEmbeddedServicesAPITest extends ServicesAPITest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supports(FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @Test
    void testFbOOEmbeddedServicesAPI() {
        gdsType = GDSType.getType(FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
        protocol = "jdbc:firebirdsql:fboo:embedded:";
    }
}
