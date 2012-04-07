package org.firebirdsql.pool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.StatementEventListener;

public class PingablePooledConnection extends AbstractPingablePooledConnection {

	protected PingablePooledConnection(Connection connection,
			boolean statementPooling, int maxStatements, boolean keepStatements)
			throws SQLException {
		super(connection, statementPooling, maxStatements, keepStatements);
	}

	public PingablePooledConnection(Connection connection,
			String pingStatement, int pingInterval, boolean statementPooling,
			int maxStatements, boolean keepStatements) throws SQLException {
		super(connection, pingStatement, pingInterval, statementPooling, maxStatements, keepStatements);
	}

	public void addStatementEventListener(StatementEventListener arg0) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public void removeStatementEventListener(StatementEventListener arg0) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
