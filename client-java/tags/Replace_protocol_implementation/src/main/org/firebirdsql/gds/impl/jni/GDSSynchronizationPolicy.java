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
package org.firebirdsql.gds.impl.jni;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.util.ReflectionHelper;

/**
 * GDS library synchronization policy.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class GDSSynchronizationPolicy {
    
    /**
     * Apply the synchronization policy if the current platform is not Windows.
     * 
     * @param tempGds instance if {@link GDS} to which policy should be applied.
     */
    static GDS applyClientSyncPolicyNonWindows(GDS tempGds) {
        GDSSynchronizationPolicy.AbstractSynchronizationPolicy syncPolicy = null;

        String osName = getSystemPropertyPrivileged("os.name");
        if (osName != null && osName.indexOf("Windows") == -1)
            syncPolicy = new GDSSynchronizationPolicy.ClientLibrarySyncPolicy(tempGds);

        if (syncPolicy != null)
            return GDSSynchronizationPolicy.applySyncronizationPolicy(tempGds, syncPolicy);
        else
            return tempGds;
    }
    
    /**
     * Apply synchronization policy on the specfied instance of {@link GDS}.
     * 
     * @param gds instance of {@link GDS} to wrap.
     * @param syncPolicy Synchronization policy to apply
     * @return instance {@link GDS} to which synchronization policy was applied.
     */
    public static GDS applySyncronizationPolicy(GDS gds, AbstractSynchronizationPolicy syncPolicy) {
        
        // no policy specified, use default sync policy (thread-per-connection)
        if (syncPolicy == null) 
            return gds;
        
        GDS wrappedObject = (GDS)Proxy.newProxyInstance(
                gds.getClass().getClassLoader(),
                ReflectionHelper.getAllInterfaces(gds.getClass()),
                syncPolicy);
        
        return wrappedObject;
    }
  
    /**
     * Abstract synchronization policy. This class should be used as invocation
     * handler for dynamic proxy that will wrap corresponding {@link GDS} 
     * implementation.
     */
    public abstract static class AbstractSynchronizationPolicy 
        implements InvocationHandler, Serializable
    {
       
        private GDS gds;
        
        protected AbstractSynchronizationPolicy(GDS gds) {
            this.gds = gds;
        }

        protected GDS getGds() {
            return gds;
        }
        
        protected abstract Object getSynchronizationObject();
        
        /**
         * Invoke some method. This method uses synchronization object returned
         * by {@link #getSynchronizationObject()} method to ensure appropriate
         * synchronization policy when accessing the {@link GDS} implementation. 
         * 
         * @param proxy proxy object, ignored.
         * @param method method to invoke on wrapped object.
         * @param args arguments to pass.
         * 
         * @return value returned by the wrapped object. 
         * 
         * @throws java.lang.Throwable if exception is raised during execution.
         */
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            Object syncObject = getSynchronizationObject();
            synchronized(syncObject) {
                try {
                    return method.invoke(gds, args);
                } catch(InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        }
    }
    
    /**
     * Synchronization policy that ensures one thread per library.
     */
    public static class ClientLibrarySyncPolicy extends AbstractSynchronizationPolicy {
        
        public static final Object SYNC_OBJECT = new Object();
        
        public ClientLibrarySyncPolicy(GDS gds) {
            super(gds);
        }
        
        /**
         * Get synchronization object. Object returned by this method checks
         * that only one thread accesses Firebird client library.
         * 
         * @return synchronization object. 
         */
        protected Object getSynchronizationObject() {
            return SYNC_OBJECT;
        }
    }
    
    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
           public String run() {
               return System.getProperty(propertyName);
           } 
        });
    }

}
