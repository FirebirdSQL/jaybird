package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


public class NativeGDSImpl extends JniGDSImpl {

    protected static class DbAttachInfo {
    
        private String server = "localhost";
    
        private int port = 3050;
    
        private String fileName;
    
        public String getConnectionString() {
            if (getServer().compareToIgnoreCase("loopback") == 0
                    || getServer().compareToIgnoreCase("localhost") == 0) {
                return getFileName();
            } else {
                return getServer() + "/" + getPort() + ":" + getFileName();
            }
        }
    
        public DbAttachInfo(String connectInfo) throws GDSException {
    
            if (connectInfo == null) { throw new GDSException(
                    "Connection string missing"); }
    
            // allows standard syntax //host:port/....
            // and old fb syntax host/port:....
            connectInfo = connectInfo.trim();
            char hostSepChar;
            char portSepChar;
            if (connectInfo.startsWith("//")) {
                connectInfo = connectInfo.substring(2);
                hostSepChar = '/';
                portSepChar = ':';
            } else {
                hostSepChar = ':';
                portSepChar = '/';
            }
    
            int sep = connectInfo.indexOf(hostSepChar);
            if (sep == 0 || sep == connectInfo.length() - 1) {
                throw new GDSException("Bad connection string: '" + hostSepChar
                        + "' at beginning or end of:" + connectInfo
                        + ISCConstants.isc_bad_db_format);
            } else if (sep > 0) {
                server = connectInfo.substring(0, sep);
                fileName = connectInfo.substring(sep + 1);
                int portSep = server.indexOf(portSepChar);
                if (portSep == 0 || portSep == server.length() - 1) {
                    throw new GDSException("Bad server string: '" + portSepChar
                            + "' at beginning or end of: " + server
                            + ISCConstants.isc_bad_db_format);
                } else if (portSep > 0) {
                    port = Integer.parseInt(server.substring(portSep + 1));
                    server = server.substring(0, portSep);
                }
            } else if (sep == -1) {
                fileName = connectInfo;
            } // end of if ()
    
        }
    
        public DbAttachInfo(String server, Integer port, String fileName)
                throws GDSException {
            if (fileName == null || fileName.equals("")) { throw new GDSException(
                    "null filename in DbAttachInfo"); } // end of if ()
            if (server != null) {
                this.server = server;
            } // end of if ()
            if (port != null) {
                this.port = port.intValue();
            } // end of if ()
            this.fileName = fileName;
            if (fileName == null || fileName.equals("")) { throw new GDSException(
                    "null filename in DbAttachInfo"); } // end of if ()
    
        }
    
        public String getServer() {
            return server;
        }
    
        public int getPort() {
            return port;
        }
    
        public String getFileName() {
            return fileName;
        }
    }

    private static Logger log = LoggerFactory.getLogger(NativeGDSImpl.class,
            false);
    
    /**
     * When initilzing in type2 mode this class will attempt too load the
     * following firebird native dlls in the order listed until one loads
     * sucesfully.
     */
    private static final String[] CLIENT_LIBRARIES_TO_TRY = {
            "fbclient.dll", "libfbclient.so"};

    public static final String NATIVE_TYPE_NAME = "NATIVE";

    
    public NativeGDSImpl() {
        this(GDSType.getType(NATIVE_TYPE_NAME));
    }

    public NativeGDSImpl(GDSType gdsType) {
        super(gdsType);

        final boolean logging = log != null;

        if (logging) 
            log.info("Attempting to initilize native library.");

        attemptToLoadAClientLibraryFromList(CLIENT_LIBRARIES_TO_TRY);

        if (logging) 
            log.info("Initilized native library OK.");
    }

    protected String getServerUrl(String file_name) throws GDSException {
        if (log != null) log.debug("Original file name: " + file_name);

        DbAttachInfo dbai = new DbAttachInfo(file_name);

        final String fileName;
        if (dbai.getFileName().indexOf(':') == -1
                && dbai.getFileName().startsWith("/") == false) {
            fileName = dbai.getServer() + "/" + dbai.getPort() + ":" + "/"
                    + dbai.getFileName();
        } else
            fileName = dbai.getServer() + "/" + dbai.getPort() + ":"
                    + dbai.getFileName();

        if (log != null) log.debug("File name for native code: " + fileName);

        return fileName;
    }

}
