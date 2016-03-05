/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.management;

import java.util.Objects;

/**
 * A user in the Firebird Security Database.
 *
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBUser implements User {

    private String userName = null;

    private String password = null;

    private String firstName = null;

    private String middleName = null;

    private String lastName = null;

    private int userId = -1;

    private int groupId = -1;

    /**
     * Create an instance of this class.
     */
    public FBUser() {
        super();
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

        if (!(obj instanceof User))
            return false;

        User user = (User) obj;
        return Objects.equals(getUserName(), user.getUserName())
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
