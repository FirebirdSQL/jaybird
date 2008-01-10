package org.firebirdsql.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;

import javax.sql.StatementEventListener;


public class PingablePooledConnection extends AbstractPingablePooledConnection {

    private HashSet statementEventListeners = new HashSet();

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

    public void addStatementEventListener(StatementEventListener listener) {
        statementEventListeners.add(listener);
    }

    public void removeStatementEventListener(StatementEventListener listener) {
        statementEventListeners.remove(listener);
    }

}
