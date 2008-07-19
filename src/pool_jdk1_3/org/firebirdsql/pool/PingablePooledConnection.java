package org.firebirdsql.pool;

import java.sql.Connection;
import java.sql.SQLException;


public class PingablePooledConnection extends AbstractPingablePooledConnection {

    public PingablePooledConnection(Connection connection,
            boolean statementPooling, int maxStatements, boolean keepStatements)
            throws SQLException {
        super(connection, statementPooling, maxStatements, keepStatements);
        // TODO Auto-generated constructor stub
    }

    public PingablePooledConnection(Connection connection,
            String pingStatement, int pingInterval, boolean statementPooling,
            int maxStatements, boolean keepStatements) throws SQLException {
        super(connection, pingStatement, pingInterval, statementPooling, maxStatements,
                keepStatements);
        // TODO Auto-generated constructor stub
    }

    
}
