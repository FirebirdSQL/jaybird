/**
 * Created by IntelliJ IDEA.
 * User: Ryan Baldwin
 * Date: Feb 18, 2003
 * Time: 11:05:20 PM
 * To change this template use Options | File Templates.
 */
package org.firebirdsql.ngds;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLDA;


public class XSQLDAImpl extends XSQLDA
    {
    XSQLDAImpl( int sqln, int sqld,  XSQLVAR[] sqlvar)
        {
        super();

        this.sqln = sqln;
        this.sqld = sqld;
        this.sqlvar = sqlvar;
        }

	// For TestNgds
	public XSQLDAImpl(int n) {
        version = ISCConstants.SQLDA_VERSION1;
        sqln = n;
        sqld = n;
        sqlvar = new XSQLVARImpl[n];
    }
    }
