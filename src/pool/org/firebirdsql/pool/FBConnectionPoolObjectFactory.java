/*
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.pool;

import javax.naming.spi.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * This is an implementation of the JNDI {@link ObjectFactory} specification 
 * for the {@link AbstractConnectionPoolDataSource} JDBC pool. This object factory
 * is responsible for resolving JNDI references into instances of expected
 * <code>javax.sql.*</code> interfaces (currently only {@link DataSource} and 
 * {@link ConnectionPoolDataSource} are supported).
 * <p>
 * Reference resolution happens when {@link AbstractConnectionPoolDataSource} 
 * instance is bound into JNDI context that supports {@link Referenceable}
 * interfaces. In this case only {@link Reference} instance returned by 
 * {@link AbstractConnectionPoolDataSource#getReference()} is stored in serialized
 * form in JNDI context. Later, during JNDI lookup each object factory registered
 * in the system is called. Object factory that is able to resolve reference 
 * into actual implementation must return not null value.
 * <p>
 * This object factory supports only references with addresses of type 
 * {@link ConnectionPoolConfiguration#REF_TYPE}.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public 
	class FBConnectionPoolObjectFactory 
	implements ObjectFactory {
        
        private static Logger LOG = 
            LoggerFactory.getLogger(XPreparedStatementCache.class, false);
            
        public static final String REF_CLASS_NAME = "";
            //FBConnectionPoolConfiguration.REF_TYPE;
        
	
		private static HashMap pools = new HashMap();
		
		/**
		 * Try to resolve <code>refObj</code> into object instance. This method
		 * requires <code>refObj</code> to contain {@link RefAddr} instances
		 * of type {@link ConnectionPoolConfiguration#REF_TYPE} to produce
		 * non-null response.
		 * 
		 * @param refObj reference stored inn JNDI. If not instance of 
		 * {@link Reference}, <code>null</code> is returned.
		 * 
		 * @param name name relative to <code>nameCtx</code> under which pool 
		 * is bind. If <code>null</code>, <code>null</code> is returned.
		 * 
		 * @param nameCtx JNDI context, possibly <code>null</code>.
		 * 
		 * @param environment environment with some parameters, ignored.
		 * 
		 * @return instance of either {@link DataSource} or 
		 * {@link ConnectionPoolDataSource} depending of type specified in
		 * {@link ConnectionPoolConfiguration} of the resolved JDBC pool 
		 * configuration or <code>null</code> if JDBC pool could not be resolved.
		 * 
		 * @throws Exception if this factory should produce some results, but
		 * parameters passed to this method contain incorrect values.
		 */
		public 
			Object getObjectInstance(Object refObj, Name name, 
				Context nameCtx, Hashtable environment) 
			throws Exception {
				
				if (!(refObj instanceof Reference)) return null;
                
                Reference ref = (Reference)refObj;
                
                if (!REF_CLASS_NAME.equals(ref.getClassName()))
                    return null;
				
				RefAddr address = ((Reference)refObj).get("");
				    //FBConnectionPoolConfiguration.REF_TYPE);
				
				if (address == null) return null;
				
				if (name == null) return null;
				
				StringBuffer fullName = new StringBuffer();
				if (nameCtx != null )
					 fullName.append(nameCtx.getNameInNamespace()).append("/");
				fullName.append(name.toString());
				
				FBConnectionPoolDataSource pool = 
					(FBConnectionPoolDataSource)pools.get(fullName.toString());
					
				if (pool == null) {
                    
//                    if (!(address.getContent() instanceof FBConnectionPoolConfiguration))
//                        return null;
                    
//					FBConnectionPoolConfiguration config = 
//						(FBConnectionPoolConfiguration)address.getContent();
						
                    if (true)
                        throw new UnsupportedOperationException("Fix this");
                         
					// pool = new FBConnectionPoolDataSource(config);
					
					pools.put(fullName, pool);
				}
				
				if (LOG.isDebugEnabled())
				    LOG.debug("Resolved name " + 
					    fullName + " to JDBC pool " + pool);
				
				return convertInterface(pool);
			}
		
		/**
		 * Convert interface of the underlying JDBC pool depending on the 
		 * type specified in the configuration of <code>pool</code>.
		 * 
		 * @param pool JDBC pool interface of which should be converted.
		 * 
		 * @return instance of {@link DataSource} or {@link ConnectionPoolDataSource}
		 * depending on the pool configuration.
		 * 
		 * @throws IllegalArgumentException if either pool is not JNDI-enabled
		 * or type specified in configuration is not known to this factory.
		 */
		private 
			Object convertInterface(FBConnectionPoolDataSource pool) 
			throws IllegalArgumentException {
				
//                FBConnectionPoolConfiguration config = 
//                    (FBConnectionPoolConfiguration)pool.getConfiguration();
                
				Class type = Object.class; //config.getJNDIType();
				
				if (ConnectionPoolDataSource.class == type)
					return pool;
				else
				if (DataSource.class == type)
					return new SimpleDataSource(pool);
				else
					throw new IllegalArgumentException(
						"Only javax.sql.DataSource and javax.sql.ConnectionPoolDataSource " + 
						"interfaces are supported. However, this should not happen " + 
						"because FBConnectionPoolDataSource.start(String) must " + 
						"complain about this.");
			}
		
	}