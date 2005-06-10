package org.firebirdsql.jdbc;

import java.sql.Driver;
import java.sql.SQLException;


public interface FirebirdDriver extends Driver {
    
    FirebirdConnectionProperties newConnectionProperties();
    
    FirebirdConnection connect(FirebirdConnectionProperties properties) 
        throws SQLException;
}
