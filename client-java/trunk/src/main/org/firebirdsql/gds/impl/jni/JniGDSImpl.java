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
     * JNI library. Subclasses are responsible for initializing JNI layer by 
     * calling {@link #nativeInitilize(String)} method.
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
        final boolean logging = log != null;
        
        try {
            boolean amd64Architecture = "amd64".equals(getSystemPropertyPrivileged("os.arch"));
    
            String jaybirdJniLibrary = amd64Architecture ? JAYBIRD_JNI_LIBRARY_X64 : JAYBIRD_JNI_LIBRARY;
            
    		if (logging)
                log.info("Attempting to load JNI library : [" + jaybirdJniLibrary + "]");
        
            System.loadLibrary(jaybirdJniLibrary);
        } catch (SecurityException ex) {
            if (logging)
                log.error("No permission to load JNI libraries.", ex);
            
            throw ex;
        } catch (UnsatisfiedLinkError ex) {
            if (logging)
                log.error("No JNI library was found in the path.", ex);
            
            throw ex;
        }
    }

    /**
     * Attempts too load a Firebird client or embedded server library. Method 
     * tries all specified libraries one by one, and when the 
     * {@link #nativeInitilize(String)} method does not throw an exception, it 
     * assumes that initialization was successful. 
     * 
     * @param clientLibraryList list of library names including file extension
     * that will be tried. 
     */
    protected void attemptToLoadAClientLibraryFromList(String[] clientLibraryList) {
        final boolean logging = log != null;

        for (int i = 0, n = clientLibraryList.length; i < n; i++) {
            final String currentClientLibraryToTry = clientLibraryList[i];
            try {
                nativeInitilize(currentClientLibraryToTry);
            } catch (Throwable th) {
                if (DEVELOPMENT_DEBUG_OUTPUT)
                	th.printStackTrace(); // Dont hide it completly

                if (logging && DEVELOPMENT_DEBUG_OUTPUT)
                    System.out.println("Failed to load client library # " + i
                        + " - \"" + currentClientLibraryToTry + "\"."
                        + th.toString());

                // If we have just failed to load the last client library
                // then we need to throw an exception.
                if (i == clientLibraryList.length - 1)
                    throw new RuntimeException(
                            "Failed to initialize Jaybird native library. " +
                            "This is most likely due to a failure to load the " +
                            "firebird client library.");

                // Otherwise we continue to next client library
                continue;
            }

            if (logging)
                log.info("Successfully loaded client library # " + i + " - \""
                        + currentClientLibraryToTry + "\".");

            // If we get here we have been loaded a client library so we stop
            // here.
            break;
        }
    }
    
    /**
     * Attempt to load a specified library. JNI layer tries to load the 
     * specified library and to resolve the needed entry points. If this fails,
     * and exception is thrown (instance of <code>java.lang.Throwable</code>).
     * If no exception was thrown, we assume that initialization succeeded. 
     * 
     * @param sharedLibraryName name of the shared library including file
     * extension.
     */
    public native void nativeInitilize(String sharedLibraryName);

    
    /*
     * Methods below must have corresponding implementations in the Jaybird JNI
     * layer (see code for jaybird2 shared library).
     */

    public native void native_isc_attach_database(byte[] file_name,
            IscDbHandle db_handle, byte[] dpbBytes);

    public native void native_isc_create_blob2(IscDbHandle db, IscTrHandle tr,
            IscBlobHandle blob, byte[] dpbBytes);

    public native void native_isc_database_info(IscDbHandle db_handle,
            int item_length, byte[] items, int buffer_length, byte[] buffer)
            throws GDSException;

    public native void native_isc_detach_database(IscDbHandle db_handle)
            throws GDSException;

    public native void native_isc_open_blob2(IscDbHandle db, IscTrHandle tr,
            IscBlobHandle blob, byte[] dpbBytes);

    public native void native_isc_seek_blob(isc_blob_handle_impl handle,
            int position, int mode) throws GDSException;

    public native void native_isc_service_attach(String service,
            IscSvcHandle serviceHandle, byte[] serviceParameterBuffer)
            throws GDSException;

    public native void native_isc_service_detach(IscSvcHandle serviceHandle)
            throws GDSException;

    public native void native_isc_service_query(IscSvcHandle serviceHandle,
            byte[] sendServiceParameterBuffer,
            byte[] requestServiceParameterBuffer, byte[] resultBuffer)
            throws GDSException;

    public native void native_isc_service_start(IscSvcHandle serviceHandle,
            byte[] serviceParameterBuffer) throws GDSException;

    public native int native_isc_que_events(IscDbHandle db_handle,
            EventHandleImp eventHandle, EventHandler handler) 
            throws GDSException;

    public native long native_isc_event_block(
            EventHandleImp eventHandle,
            String eventName) throws GDSException;

    public native void native_isc_event_counts(EventHandleImp eventHandle)
            throws GDSException;

    public native void native_isc_cancel_events(IscDbHandle db_handle,
            EventHandleImp eventHandle) throws GDSException;

    protected native void native_isc_finalize(int isc_api_handle)
        throws GDSException;

    protected void finalize() throws Throwable {
        native_isc_finalize(isc_api_handle);
    }
    
    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
           public String run() {
               return System.getProperty(propertyName);
           } 
        });
    }
}
