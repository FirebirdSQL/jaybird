package org.firebirdsql.gds;

import org.firebirdsql.gds.impl.wire.AbstractJavaGDSImpl;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class GDSObjectFactory {

    private static Logger log = LoggerFactory.getLogger(GDSObjectFactory.class, false);

    public static AbstractJavaGDSImpl createJavaGDSImpl() {
        try {
            return (AbstractJavaGDSImpl) ClassFactory.get(ClassFactory.JavaGDSImpl).newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
