package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class EmbeddedServicesAPITest extends ServicesAPITest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supports(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @Test
    void testEmbeddedServicesAPI() {
        gdsType = GDSType.getType(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
        protocol = "jdbc:firebirdsql:embedded:";
    }
}
