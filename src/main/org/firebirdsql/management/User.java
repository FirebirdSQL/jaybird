// SPDX-FileCopyrightText: Copyright 2004 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

/**
 * A user in the Firebird Security Database.
 *
 * @author Steven Jardine
 * @deprecated Use the SQL user management statements instead, we currently do not plan to remove this API
 */
@Deprecated(since = "6")
@SuppressWarnings("DeprecatedIsStillUsed")
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
