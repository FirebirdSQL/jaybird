package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class LocalGDSImpl extends JniGDSImpl {

    private static Logger log = LoggerFactory.getLogger(LocalGDSImpl.class,
            false);

    private static final String[] LIST_OF_CLIENT_LIBRARIES_TO_TRY = {
            "fbclient.dll", "libfbclient.so"};

    public static final String LOCAL_TYPE_NAME = "LOCAL";

    public LocalGDSImpl() {
        this(GDSType.getType(LOCAL_TYPE_NAME));
    }

    public LocalGDSImpl(GDSType gdsType) {
        super(gdsType);

        final boolean logging = log != null;

        if (logging) log.info("Attempting to initilize native library.");

        attemptToLoadAClientLibraryFromList(LIST_OF_CLIENT_LIBRARIES_TO_TRY);

        if (logging) log.info("Initilized native library OK.");
    }

    protected String getServerUrl(String file_name) throws GDSException {
        return file_name;
    }

}
