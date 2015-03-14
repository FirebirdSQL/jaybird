/*
 * $Id$
 * 
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

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public abstract class JniGDSImpl extends BaseGDSImpl {

    // TODO: Replace with debug logging
    private static final boolean DEVELOPMENT_DEBUG_OUTPUT = false;

    /**
     * Name of the JNI library able to communicate with the client or embedded
     * server library.
     */
    public static final String JAYBIRD_JNI_LIBRARY = "jaybird23";
    public static final String JAYBIRD_JNI_LIBRARY_X64 = "jaybird23_x64";
    
    private static Logger log = LoggerFactory.getLogger(JniGDSImpl.class
    );

    static {
        initJNIBridge();
    }
    
    /**
     * Create instance of this class. This constructor attempts to load Jaybird
     * JNI library.
     * 
     * @param gdsType type of GDS module being created.
     */
    protected JniGDSImpl(GDSType gdsType) {
        super(gdsType);
    }
    
    /**
     * Default constructor for subclasses only. If subclass uses this constructor,
     * it must ensure that correct type is returned from 
     * {@link org.firebirdsql.gds.impl.AbstractGDS#getType()} method.
     */
    public JniGDSImpl() {
        super();
    }

    /**
     * Init the JNI bridge for this class.
     * 
     * @throws UnsatisfiedLinkError if JNI bridge cannot be initialized.
     */
    protected static void initJNIBridge() throws UnsatisfiedLinkError {
//        final boolean logging = log != null;
//
//        try {
//            boolean amd64Architecture = "amd64".equals(getSystemPropertyPrivileged("os.arch"));
//
//            String jaybirdJniLibrary = amd64Architecture ? JAYBIRD_JNI_LIBRARY_X64 : JAYBIRD_JNI_LIBRARY;
//
//    		if (logging)
//                log.info("Attempting to load JNI library : [" + jaybirdJniLibrary + "]");
//
//            System.loadLibrary(jaybirdJniLibrary);
//        } catch (SecurityException ex) {
//            if (logging)
//                log.error("No permission to load JNI libraries.", ex);
//
//            throw ex;
//        } catch (UnsatisfiedLinkError ex) {
//            if (logging)
//                log.error("No JNI library was found in the path.", ex);
//
//            throw ex;
//        }
    }

    /**
     * Attempts too load a Firebird client or embedded server library. Method 
     * tries all specified libraries one by one, and when the 
     * nativeInitilize(String) method does not throw an exception, it
     * assumes that initialization was successful. 
     * 
     * @param clientLibraryList list of library names including file extension
     * that will be tried. 
     */
    protected void attemptToLoadAClientLibraryFromList(String[] clientLibraryList) {
//        final boolean logging = log != null;
//
//        for (int i = 0, n = clientLibraryList.length; i < n; i++) {
//            final String currentClientLibraryToTry = clientLibraryList[i];
//            try {
//                nativeInitilize(currentClientLibraryToTry);
//            } catch (Throwable th) {
//                if (DEVELOPMENT_DEBUG_OUTPUT)
//                	th.printStackTrace(); // Dont hide it completly
//
//                if (logging && DEVELOPMENT_DEBUG_OUTPUT)
//                    System.out.println("Failed to load client library # " + i
//                        + " - \"" + currentClientLibraryToTry + "\"."
//                        + th.toString());
//
//                // If we have just failed to load the last client library
//                // then we need to throw an exception.
//                if (i == clientLibraryList.length - 1)
//                    throw new RuntimeException(
//                            "Failed to initialize Jaybird native library. " +
//                            "This is most likely due to a failure to load the " +
//                            "firebird client library.");
//
//                // Otherwise we continue to next client library
//                continue;
//            }
//
//            if (logging)
//                log.info("Successfully loaded client library # " + i + " - \""
//                        + currentClientLibraryToTry + "\".");
//
//            // If we get here we have been loaded a client library so we stop
//            // here.
//            break;
//        }
    }

    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
           public String run() {
               return System.getProperty(propertyName);
           } 
        });
    }
}
