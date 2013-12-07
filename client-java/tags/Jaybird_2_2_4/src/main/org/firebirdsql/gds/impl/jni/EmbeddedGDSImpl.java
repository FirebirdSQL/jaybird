package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class EmbeddedGDSImpl extends JniGDSImpl {

    private static Logger log = LoggerFactory.getLogger(EmbeddedGDSImpl.class,
            false);

    private static final String[] EMBEDDED_LIBRARIES_TO_TRY = { 
        "fbembed.dll",
        "libfbembed.so"
    };

    public static final String EMBEDDED_TYPE_NAME = "EMBEDDED";

    public EmbeddedGDSImpl() {
        this(GDSType.getType(EMBEDDED_TYPE_NAME));
    }

    public EmbeddedGDSImpl(GDSType gdsType) {
        super(gdsType);

        final boolean logging = log != null;

        if (logging) log.info("Attempting to initilize native library.");

        attemptToLoadAClientLibraryFromList(EMBEDDED_LIBRARIES_TO_TRY);

        if (logging) log.info("Initilized native library OK.");
    }

    protected String getServerUrl(String file_name) throws GDSException {
        return file_name;
    }

}
