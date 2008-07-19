package org.firebirdsql.jdbc;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSHelper;


public class FBDatabaseMetaData extends AbstractDatabaseMetaData {

    public FBDatabaseMetaData(AbstractConnection c) throws GDSException {
        super(c);
        // TODO Auto-generated constructor stub
    }

    public FBDatabaseMetaData(GDSHelper gdsHelper) {
        super(gdsHelper);
        // TODO Auto-generated constructor stub
    }

    
}
