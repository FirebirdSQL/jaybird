/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
 * 
 * Copyright (C) All Rights Reserved.
 * 
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  
 *   - Redistributions of source code must retain the above copyright 
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above 
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   - Neither the name of the firebird development team nor the names
 *     of its contributors may be used to endorse or promote products 
 *     derived from this software without specific prior written 
 *     permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
 * SUCH DAMAGE.
 */
package org.firebirdsql.management;

import java.io.OutputStream;

/**
 * The base Firebird Service API functionality.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 */
public interface ServiceManager {

    /**
     * Sets the username for the connection to the service manager.
     * @param user for the connection to the service manager.
     */
    public void setUser(String user);

    /**
     * Returns the username for the connection to the service manager.
     * @return the username for the connection to the service manager.
     */
    public String getUser();

    /**
     * Sets the password for the connection to the service manager.
     * @param password for the connection to the service manager.
     */
    public void setPassword(String password);

    /**
     * Returns the password for the connection to the service manager.
     * @return the password for the connection to the service manager.
     */
    public String getPassword();

    /**
     * Sets the database path for the connection to the service manager.
     * @param database path for the connection to the service manager.
     */
    public void setDatabase(String database);

    /**
     * Returns the database path for the connection to the service manager.
     * @return the database path for the connection to the service manager.
     */
    public String getDatabase();

    /**
     * Returns the host for the connection to the service manager.
     * @return the host for the connection to the service manager.
     */
    public String getHost();

    /**
     * Sets the host for the connection to the service manager.
     * @param host for the connection to the service manager.
     */
    public void setHost(String host);

    /**
     * Returns the port for the connection to the service manager.
     * @return the port for the connection to the service manager.
     */
    public int getPort();

    /**
     * Sets the port for the connection to the service manager.
     * @param port for the connection to the service manager.
     */
    public void setPort(int port);

    /**
     * Returns the logger for the connection to the service manager.
     * @return the logger for the connection to the service manager.
     */
    public OutputStream getLogger();

    /**
     * Sets the logger for the connection to the service manager.
     * @param logger for the connection to the service manager.
     */
    public void setLogger(OutputStream logger);

}
