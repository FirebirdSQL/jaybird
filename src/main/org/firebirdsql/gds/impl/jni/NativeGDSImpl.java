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

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class NativeGDSImpl extends JniGDSImpl {

    private static Logger log = LoggerFactory.getLogger(NativeGDSImpl.class, false);
    
    /**
     * When initializing in type2 mode this class will attempt too load the
     * following firebird native dlls in the order listed until one loads
     * successfully.
     */
    private static final String[] CLIENT_LIBRARIES_TO_TRY = {
            "fbclient.dll", "libfbclient.so"};

    public static final String NATIVE_TYPE_NAME = "NATIVE";

    
    public NativeGDSImpl() {
        this(GDSType.getType(NATIVE_TYPE_NAME));
    }

    public NativeGDSImpl(GDSType gdsType) {
        super(gdsType);

        final boolean logging = log != null;

        if (logging) 
            log.info("Attempting to initilize native library.");

        attemptToLoadAClientLibraryFromList(CLIENT_LIBRARIES_TO_TRY);

        if (logging) 
            log.info("Initilized native library OK.");
    }

    protected String getServerUrl(String file_name) throws GDSException {
        if (log != null) log.debug("Original file name: " + file_name);

        DbAttachInfo dbai = new DbAttachInfo(file_name);

        final String fileName;
        if (dbai.getFileName().indexOf(':') == -1
                && dbai.getFileName().startsWith("/") == false) {
            fileName = dbai.getServer() + "/" + dbai.getPort() + ":" + "/"
                    + dbai.getFileName();
        } else
            fileName = dbai.getServer() + "/" + dbai.getPort() + ":"
                    + dbai.getFileName();

        if (log != null) log.debug("File name for native code: " + fileName);

        return fileName;
    }

}
