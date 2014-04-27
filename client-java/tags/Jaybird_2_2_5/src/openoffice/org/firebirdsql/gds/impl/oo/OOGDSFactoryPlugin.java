package org.firebirdsql.gds.impl.oo;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.AbstractJavaGDSImpl;

public class OOGDSFactoryPlugin implements GDSFactoryPlugin {

    private static final String TYPE_NAME = "OOREMOTE";

    private static final String[] TYPE_ALIASES = new String[] {};

    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebird:oo:", "jdbc:firebirdsql:oo:"};

    private static AbstractJavaGDSImpl gdsImpl;

    public String getPluginName() {
        return "GDS implementation for OpenOffice.";
    }

    public String getTypeName() {
        return TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public Class getConnectionClass() {
        return ClassFactory.get("org.firebirdsql.jdbc.oo.OOConnection");
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public GDS getGDS() {
        if (gdsImpl == null) gdsImpl = GDSObjectFactory.createJavaGDSImpl();

        return gdsImpl;
    }

    public String getDatabasePath(String server, Integer port, String path)
            throws GDSException {
        if (server == null)
            throw new GDSException("Server name/address is required "
                    + "for pure Java implementation.");

        if (path == null)
            throw new GDSException("Database name/path is required.");

        StringBuffer sb = new StringBuffer();

        sb.append(server);
        if (port != null) sb.append("/").append(port.intValue());

        sb.append(":").append(path);

        return sb.toString();
    }

    public String getDatabasePath(String jdbcUrl) throws GDSException {

        String[] protocols = getSupportedProtocols();
        for (int i = 0; i < protocols.length; i++) {
            if (jdbcUrl.startsWith(protocols[i]))
                return jdbcUrl.substring(protocols[i].length());
        }

        throw new IllegalArgumentException("Incorrect JDBC protocol handling: "
                + jdbcUrl);
    }

    public String getDefaultProtocol() {
        return getSupportedProtocols()[0];
    }

    public int hashCode() {
        return getTypeName().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof OOGDSFactoryPlugin)) return false;

        return true;
    }
}
