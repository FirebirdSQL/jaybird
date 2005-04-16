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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.IscSvcHandle;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.management.FBUser;

/**
 * Implements the display/add/delete/modify user functionality of the Firebird
 * Services API.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class FBUserManager extends FBServiceManager implements UserManager {

    private int count = 0;

    private GDS gds = getGds();

    /**
     * Parses the displayBuffer and creates a map of users.
     * 
     * @return a map of users parsed from the displayBuffer.
     * @throws SQLException
     * @throws IOException
     */
    private Map getFBUsers() throws SQLException, IOException {
        
        User user = null;

        Map users = new TreeMap();

        byte[] displayBuffer = ((ByteArrayOutputStream) getLogger())
                .toByteArray();
        setLogger(new ByteArrayOutputStream());

        count = 0;
        while (count < (displayBuffer.length)
                && displayBuffer[count] != ISCConstants.isc_info_end) {

            switch (displayBuffer[count]) {
            case ISCConstants.isc_spb_sec_username:
                if (user != null) {
                    users.put(user.getUserName(), user);
                }
                user = new FBUser();
                user.setUserName(getSRBString(displayBuffer));
                break;
            case ISCConstants.isc_spb_sec_firstname:
                user.setFirstName(getSRBString(displayBuffer));
                break;
            case ISCConstants.isc_spb_sec_middlename:
                user.setMiddleName(getSRBString(displayBuffer));
                break;
            case ISCConstants.isc_spb_sec_lastname:
                user.setLastName(getSRBString(displayBuffer));
                break;
            case ISCConstants.isc_spb_sec_userid:
                user.setUserId(getSRBInteger(displayBuffer));
                break;
            case ISCConstants.isc_spb_sec_groupid:
                user.setGroupId(getSRBInteger(displayBuffer));
                break;
            default:
                count++;
                break;
            }
        }
        users.put(user.getUserName(), user);
        return users;
        
    }

    /**
     * Generate the service request buffer for the specified action.
     * 
     * @param action
     * @param username
     * @param password
     * @param firstname
     * @param middlename
     * @param lastname
     * @param userid
     * @param groupid
     * @return the service request buffer for the specified action.
     */
    private ServiceRequestBuffer getUserSRB(int action, User user) {

        ServiceRequestBuffer srb = gds.createServiceRequestBuffer(action);

        if (user.getUserName() != null)
            srb.addArgument(ISCConstants.isc_spb_sec_username, user
                    .getUserName());
        if (user.getPassword() != null)
            srb.addArgument(ISCConstants.isc_spb_sec_password, user
                    .getPassword());
        if (user.getFirstName() != null)
            srb.addArgument(ISCConstants.isc_spb_sec_firstname, user
                    .getFirstName());
        if (user.getMiddleName() != null)
            srb.addArgument(ISCConstants.isc_spb_sec_middlename, user
                    .getMiddleName());
        if (user.getLastName() != null)
            srb.addArgument(ISCConstants.isc_spb_sec_lastname, user
                    .getLastName());
        if (user.getUserId() != -1)
            srb.addArgument(ISCConstants.isc_spb_sec_userid, user.getUserId());
        if (user.getGroupId() != -1)
            srb.addArgument(ISCConstants.isc_spb_sec_groupid, user
                    .getGroupId());
        return srb;
        
    }

    /**
     * Returns an integer from ther service request buffer. Integers are 4 bytes
     * in length.
     * 
     * @param displayBuffer
     * @return an integer from ther service request buffer.
     */
    private int getSRBInteger(byte[] displayBuffer) {
        
        count += 1;
        int integer = gds.iscVaxInteger(displayBuffer, count, 4);
        count += 4;
        return integer;
        
    }

    /**
     * Returns a string from ther service request buffer.
     * 
     * @param displayBuffer
     * @return an string from ther service request buffer.
     */
    private String getSRBString(byte[] displayBuffer) {
        
        count += 1;
        int length = gds.iscVaxInteger(displayBuffer, count, 2);
        count += 2;

        String string = new String(displayBuffer, count, length);
        count += length;
        return string;
        
    }

    /**
     * Perform the specified action.
     * 
     * @param action
     * @param username
     * @param password
     * @param firstname
     * @param middlename
     * @param lastname
     * @param userid
     * @param groupid
     * @param groupname
     * @param sqlrole
     * @throws SQLException
     * @throws IOException
     */
    private void userAction(int action, User user) throws SQLException,
            IOException {
        
        try {
            IscSvcHandle handle = attachServiceManager(gds);
            try {

                ServiceRequestBuffer srb = getUserSRB(action, user);
                gds.iscServiceStart(handle, srb);

                queueService(gds, handle);

            } finally {
                detachServiceManager(gds, handle);
            }
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
        
    }

    /**
     * Create and instance of this class.
     * 
     * @param gdsType
     */
    public FBUserManager(GDSType gdsType) {
        
        super(gdsType);
        setLogger(new ByteArrayOutputStream());
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.UserManager#add(org.firebirdsql.management.User)
     */
    public void add(User user) throws SQLException, IOException {

        if (user.getUserName() == null)
            throw new FBSQLException("UserName is required.");
        userAction(ISCConstants.isc_action_svc_add_user, user);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.UserManager#delete(org.firebirdsql.management.User)
     */
    public void delete(User user) throws SQLException, IOException {

        if (user.getUserName() == null)
            throw new FBSQLException("UserName is required.");
        // Only parameter for delete action is username. All others should be
        // null.
        User delUser = new FBUser();
        delUser.setUserName(user.getUserName());
        userAction(ISCConstants.isc_action_svc_delete_user, delUser);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.UserManager#update(org.firebirdsql.management.User)
     */
    public void update(User user) throws SQLException, IOException {

        if (user.getUserName() == null)
            throw new FBSQLException("UserName is required.");
        userAction(ISCConstants.isc_action_svc_modify_user, user);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.UserManager#getUsers()
     */

    public Map getUsers() throws SQLException, IOException {

        userAction(ISCConstants.isc_action_svc_display_user, new FBUser());
        return getFBUsers();

    }
}
