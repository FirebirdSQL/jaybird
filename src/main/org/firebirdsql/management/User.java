/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
     * characters. Mandatory for all operations. Must be unique.
     *
     * @param username User name
     */
    void setUserName(String username);

    /**
     * @return the username in the security database.
     */
    String getUserName();

    /**
     * Password for the specified user. Maximum length is 31 characters. Only
     * first 8 characters are significant.
     *
     * @param password Password
     */
    void setPassword(String password);

    /**
     * Returns the password as set by setPassword. This will return null in most
     * cases and is only provided for add and update user functionality.
     *
     * @return the password for the specified user.
     */
    String getPassword();

    /**
     * Optional first name of the person using this user name.
     *
     * @param firstName First name
     */
    void setFirstName(String firstName);

    /**
     * @return first name of the person using this user name.
     */
    String getFirstName();

    /**
     * Optional middle name of the person using this user name.
     *
     * @param middleName Middle name
     */
    void setMiddleName(String middleName);

    /**
     * @return middle name of the person using this user name.
     */
    String getMiddleName();

    /**
     * Optional last name of the person using this user name.
     *
     * @param lastName Last name
     */
    void setLastName(String lastName);

    /**
     * @return last name of the person using this user name.
     */
    String getLastName();

    /**
     * Optional user ID number, defined in /etc/passwd, to assign to the user in
     * security database; reserved for future implementation
     *
     * @param userId Id of the user
     */
    void setUserId(int userId);

    /**
     * @return user id number.
     */
    int getUserId();

    /**
     * Optional group ID number, defined in /etc/group, to assign to the user in
     * security database; reserved for future implementation
     *
     * @param groupId Id of the group
     */
    void setGroupId(int groupId);

    /**
     * @return group id number.
     */
    int getGroupId();

}
