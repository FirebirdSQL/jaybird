/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.management;

/**
 * A user in the Firebird Security Database.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
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

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#getUserName()
     */
    public String getUserName() {
        return userName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#setUserName(java.lang.String)
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#getFirstName()
     */
    public String getFirstName() {
        return firstName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#setFirstName(java.lang.String)
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#getMiddleName()
     */
    public String getMiddleName() {
        return middleName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#setMiddleName(java.lang.String)
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#getLastName()
     */
    public String getLastName() {
        return lastName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#setLastName(java.lang.String)
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#getUserId()
     */
    public int getUserId() {
        return userId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#setUserId(int)
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#getGroupId()
     */
    public int getGroupId() {
        return groupId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.User#setGroupId(int)
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object ob1) {
        User user = (User) ob1;
        return user.getUserName().equals(getUserName())
                && user.getFirstName().equals(getFirstName())
                && user.getMiddleName().equals(getMiddleName())
                && user.getLastName().equals(getLastName())
                && user.getUserId() == getUserId()
                && user.getGroupId() == getGroupId();

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getUserName() + " | " + getPassword() + " | " + getFirstName()
                + " | " + getMiddleName() + " | " + getLastName() + " | "
                + getUserId() + " | " + getGroupId();

    }
}
