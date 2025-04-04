// SPDX-FileCopyrightText: Copyright 2004 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import java.util.Objects;

/**
 * A user in the Firebird Security Database.
 *
 * @author Steven Jardine
 * @author Roman Rokytskyy
 * @deprecated Use the SQL user management statements instead, we currently do not plan to remove this API
 */
@Deprecated(since = "6")
@SuppressWarnings("DeprecatedIsStillUsed")
public class FBUser implements User {

    private String userName;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private int userId = -1;
    private int groupId = -1;

    /**
     * Create an instance of this class.
     */
    public FBUser() {
    }

    FBUser(String userName, String password, String firstName, String middleName, String lastName, int userId,
            int groupId) {
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.userId = userId;
        this.groupId = groupId;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getMiddleName() {
        return middleName;
    }

    @Override
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof User user
                && Objects.equals(getUserName(), user.getUserName())
                && Objects.equals(getFirstName(), user.getFirstName())
                && Objects.equals(getMiddleName(), user.getMiddleName())
                && Objects.equals(getLastName(), user.getLastName())
                && user.getUserId() == getUserId()
                && user.getGroupId() == getGroupId();
    }

    @Override
    public int hashCode() {
        int hashCode = Objects.hash(userName, firstName, middleName, lastName);

        hashCode ^= userId != -1 ? userId : 0;
        hashCode ^= groupId != -1 ? groupId : 0;

        return hashCode;
    }

    @Override
    public String toString() {
        return getUserName() + " | " + getPassword() + " | " + getFirstName()
                + " | " + getMiddleName() + " | " + getLastName() + " | "
                + getUserId() + " | " + getGroupId();
    }
}
