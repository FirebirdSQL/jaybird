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
package org.firebirdsql.management;

import java.io.IOException;
import java.sql.SQLException;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.GDSType;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.isc_svc_handle;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Uses the Services API to display, add, delete, and modify users. This
 * corresponds to the functionality of the command-line tool gsec.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class UserManager extends ServiceManager {

    /**
     * Create instance of this class.
     * 
     * @param gdsType
     */
    public UserManager(GDSType gdsType) {
        super(gdsType);
    }

    /**
     * Create a new user in the security database.
     * 
     * @param username
     * @param password
     */
    public void addUser(String username, String password) throws SQLException,
            IOException {
        addUser(username, password, null, null, null, -1, -1, null, null);
    }

    /**
     * Create a new user in the security database.
     * 
     * @param username
     * @param password
     * @param firstname
     * @param middlename
     * @param lastname
     */
    public void addUser(String username, String password, String firstname,
            String middlename, String lastname) throws SQLException,
            IOException {

        addUser(username, password, firstname, middlename, lastname, -1, -1,
                null, null);
    }

    /**
     * Create a new user in the security database.
     * 
     * @param username
     * @param password
     * @param firstname
     * @param middlename
     * @param lastname
     * @param userid
     * @param groupid
     * @param groupname
     * @param sqlrole
     */
    protected void addUser(String username, String password, String firstname,
            String middlename, String lastname, int userid, int groupid,
            String groupname, String sqlrole) throws SQLException, IOException {

        userAction(ISCConstants.isc_action_svc_add_user, username, password,
                firstname, middlename, lastname, userid, groupid, groupname,
                sqlrole);
    }

    /**
     * Delete a user from the security database.
     * 
     * @param username
     */
    public void deleteUser(String username) throws SQLException, IOException {

        deleteUser(username, null);

    }

    /**
     * Delete a user from the security database.
     * 
     * @param username
     * @param sqlrole
     */
    protected void deleteUser(String username, String sqlrole)
            throws SQLException, IOException {

        userAction(ISCConstants.isc_action_svc_delete_user, username, null,
                null, null, null, -1, -1, null, sqlrole);

    }

    /**
     * Display's all users in the security database.
     */
    public void displayAllUsers() throws SQLException, IOException {

        displayUser(null);

    }

    /**
     * Display a single user in the security database.
     * 
     * @param username
     */
    protected void displayUser(String username) throws SQLException,
            IOException {

        userAction(ISCConstants.isc_action_svc_display_user, username, null,
                null, null, null, -1, -1, null, null);

    }

    /**
     * Create a new user in the security database.
     * 
     * @param username
     * @param password
     */
    public void modifyUser(String username, String password)
            throws SQLException, IOException {

        modifyUser(username, password, null, null, null, -1, -1, null, null);

    }

    /**
     * Create a new user in the security database.
     * 
     * @param username
     * @param password
     * @param firstname
     * @param middlename
     * @param lastname
     */
    public void modifyUser(String username, String password, String firstname,
            String middlename, String lastname) throws SQLException,
            IOException {

        modifyUser(username, password, firstname, middlename, lastname, -1, -1,
                null, null);

    }

    /**
     * Create a new user in the security database.
     * 
     * @param username
     * @param password
     * @param firstname
     * @param middlename
     * @param lastname
     * @param userid
     * @param groupid
     * @param groupname
     * @param sqlrole
     */
    protected void modifyUser(String username, String password,
            String firstname, String middlename, String lastname, int userid,
            int groupid, String groupname, String sqlrole) throws SQLException,
            IOException {

        userAction(ISCConstants.isc_action_svc_modify_user, username, password,
                firstname, middlename, lastname, userid, groupid, groupname,
                sqlrole);

    }

    private void userAction(int action, String username, String password,
            String firstname, String middlename, String lastname, int userid,
            int groupid, String groupname, String sqlrole) throws SQLException,
            IOException {

        GDS gds = getGds();

        try {
            isc_svc_handle handle = attachServiceManager(gds);
            try {

                ServiceRequestBuffer srb = getUserSRB(gds, action, username,
                        password, firstname, middlename, lastname, userid,
                        groupid, groupname, sqlrole);
                gds.isc_service_start(handle, srb);

                queueService(gds, handle);

            } finally {
                detachServiceManager(gds, handle);
            }
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    private ServiceRequestBuffer getUserSRB(GDS gds, int action,
            String username, String password, String firstname,
            String middlename, String lastname, int userid, int groupid,
            String groupname, String sqlrole) {

        ServiceRequestBuffer srb = gds.newServiceRequestBuffer(action);

        if (username != null)
            srb.addArgument(ISCConstants.isc_spb_sec_username, username);
        if (password != null)
            srb.addArgument(ISCConstants.isc_spb_sec_password, password);
        if (firstname != null)
            srb.addArgument(ISCConstants.isc_spb_sec_firstname, firstname);
        if (middlename != null)
            srb.addArgument(ISCConstants.isc_spb_sec_middlename, middlename);
        if (lastname != null)
            srb.addArgument(ISCConstants.isc_spb_sec_lastname, lastname);
        if (userid != -1)
            srb.addArgument(ISCConstants.isc_spb_sec_userid, userid);
        if (groupid != -1)
            srb.addArgument(ISCConstants.isc_spb_sec_groupid, groupid);
        if (groupname != null)
            srb.addArgument(ISCConstants.isc_spb_sec_groupname, groupname);
        if (sqlrole != null)
            srb.addArgument(ISCConstants.isc_spb_sql_role_name, sqlrole);

        return srb;
    }

}
