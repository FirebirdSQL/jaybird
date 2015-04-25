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

/**
 * A user in the Firebird Security Database.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public interface User {

    /**
     * User name to create in security database. Maximum length is 31
     * characters. Manditory for all operations. Must be unique.
     * 
     * @param username
     */
    public void setUserName(String username);

    /**
     * @return the username in the security database.
     */
    public String getUserName();

    /**
     * Password for the specified user. Maximum length is 31 characters. Only
     * first 8 characters are significant.
     * 
     * @param password
     */
    public void setPassword(String password);

    /**
     * Returns the password as set by setPassword. This will return null in most
     * cases and is only provided for add and update user functionality.
     * 
     * @return the password for the specified user.
     */
    public String getPassword();

    /**
     * Optional first name of the person using this user name.
     * 
     * @param firstName
     */
    public void setFirstName(String firstName);

    /**
     * @return first name of the person using this user name.
     */
    public String getFirstName();

    /**
     * Optional middle name of the person using this user name.
     * 
     * @param middleName
     */
    public void setMiddleName(String middleName);

    /**
     * @return middle name of the person using this user name.
     */
    public String getMiddleName();

    /**
     * Optional last name of the person using this user name.
     * 
     * @param lastName
     */
    public void setLastName(String lastName);

    /**
     * @return last name of the person using this user name.
     */
    public String getLastName();

    /**
     * Optional user ID number, defined in /etc/passwd, to assign to the user in
     * security database; reserved for future implementation
     * 
     * @param userId
     */
    public void setUserId(int userId);

    /**
     * @return user id number.
     */
    public int getUserId();

    /**
     * Optional group ID number, defined in /etc/group, to assign to the user in
     * security database; reserved for future implementation
     * 
     * @param groupId
     */
    public void setGroupId(int groupId);

    /**
     * @return group id number.
     */
    public int getGroupId();

}
