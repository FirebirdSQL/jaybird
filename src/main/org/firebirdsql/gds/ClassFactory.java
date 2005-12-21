/**
 * 
 */
package org.firebirdsql.gds;

import java.util.HashMap;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * @author sjardine
 * 
 */
public class ClassFactory {

	private static HashMap classes = new HashMap();

	public static final String DriverConnectionPoolDataSource = "org.firebirdsql.pool.DriverConnectionPoolDataSource";

	public static final String FBCallableStatement = "org.firebirdsql.jdbc.FBCallableStatement";

	public static final String FBConnection = "org.firebirdsql.jdbc.FBConnection";

	public static final String FBConnectionPoolDataSource = "org.firebirdsql.pool.FBConnectionPoolDataSource";

	public static final String FBPreparedStatement = "org.firebirdsql.jdbc.FBPreparedStatement";

	public static final String FBSavepoint = "org.firebirdsql.jdbc.FBSavepoint";

	public static final String FBStatement = "org.firebirdsql.jdbc.FBStatement";

	public static final String JavaGDSImpl = "org.firebirdsql.gds.impl.wire.JavaGDSImpl";

	private static Logger log = LoggerFactory.getLogger(ClassFactory.class,
			false);

	public static Class get(String className) {
		Class clazz = null;
		if (!classes.containsKey(className)) {
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage(), e);
				clazz = null;
			}
		} else {
			clazz = (Class) classes.get(className);
		}
		return clazz;
	}

}
