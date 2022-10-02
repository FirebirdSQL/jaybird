/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_dbname;

/**
 * Implements the display/add/delete/modify user functionality of the Firebird Services API.
 *
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBUserManager extends FBServiceManager implements UserManager {

    private int count = 0;
    private String securityDatabase = null;

    /**
     * Create a new instance of {@code FBMaintenanceManager} based on the default GDSType.
     */
    @SuppressWarnings("unused")
    public FBUserManager() {
        super();
    }

    /**
     * Create a new instance of {@code FBMaintenanceManager} based on
     * a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    @SuppressWarnings("unused")
    public FBUserManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of {@code FBMaintenanceManager} based on
     * a given GDSType.
     *
     * @param gdsType
     *         the GDS implementation type to use
     */
    public FBUserManager(GDSType gdsType) {
        super(gdsType);
    }

    /**
     * Parses the display buffer and creates a map of users.
     *
     * @return a map of users parsed from the display buffer.
     */
    private Map<String, User> getFBUsers() {
        User user = null;
        Map<String, User> users = new TreeMap<>();
        byte[] displayBuffer = ((ByteArrayOutputStream) getLogger()).toByteArray();
        count = 0;
        while (count < displayBuffer.length && displayBuffer[count] != isc_info_end) {
            switch (displayBuffer[count]) {
            case isc_spb_sec_username:
                if (user != null) {
                    users.put(user.getUserName(), user);
                }
                user = new FBUser();
                user.setUserName(getSRBString(displayBuffer));
                break;
            case isc_spb_sec_firstname:
                assert user != null : "Should have a non null user";
                user.setFirstName(getSRBString(displayBuffer));
                break;
            case isc_spb_sec_middlename:
                assert user != null : "Should have a non null user";
                user.setMiddleName(getSRBString(displayBuffer));
                break;
            case isc_spb_sec_lastname:
                assert user != null : "Should have a non null user";
                user.setLastName(getSRBString(displayBuffer));
                break;
            case isc_spb_sec_userid:
                assert user != null : "Should have a non null user";
                user.setUserId(getSRBInteger(displayBuffer));
                break;
            case isc_spb_sec_groupid:
                assert user != null : "Should have a non null user";
                user.setGroupId(getSRBInteger(displayBuffer));
                break;
            default:
                count++;
                break;
            }
        }
        if (user != null) {
            users.put(user.getUserName(), user);
        }
        return users;

    }

    /**
     * Generate the service request buffer for the specified action.
     *
     * @param service
     *         service handle
     * @param action
     *         action to execute
     * @param user
     *         user
     * @return the service request buffer for the specified action.
     */
    private ServiceRequestBuffer getUserSRB(FbService service, int action, User user) {
        ServiceRequestBuffer srb = service.createServiceRequestBuffer();
        srb.addArgument(action);

        if (user.getUserName() != null) {
            srb.addArgument(isc_spb_sec_username, user.getUserName());
        }
        if (user.getPassword() != null) {
            srb.addArgument(isc_spb_sec_password, user.getPassword());
        }
        if (user.getFirstName() != null) {
            srb.addArgument(isc_spb_sec_firstname, user.getFirstName());
        }
        if (user.getMiddleName() != null) {
            srb.addArgument(isc_spb_sec_middlename, user.getMiddleName());
        }
        if (user.getLastName() != null) {
            srb.addArgument(isc_spb_sec_lastname, user.getLastName());
        }
        if (user.getUserId() != -1) {
            srb.addArgument(isc_spb_sec_userid, user.getUserId());
        }
        if (user.getGroupId() != -1) {
            srb.addArgument(isc_spb_sec_groupid, user.getGroupId());
        }
        return srb;
    }

    /**
     * Returns an integer from the service request buffer. Integers are 4 bytes in length.
     *
     * @param displayBuffer
     *         display buffer
     * @return an integer from the service request buffer.
     */
    private int getSRBInteger(byte[] displayBuffer) {
        count += 1;
        int integer = iscVaxInteger(displayBuffer, count, 4);
        count += 4;
        return integer;
    }

    /**
     * Returns a string from the service request buffer.
     *
     * @param displayBuffer
     *         display buffer
     * @return a string from the service request buffer.
     */
    private String getSRBString(byte[] displayBuffer) {
        count += 1;
        int length = iscVaxInteger(displayBuffer, count, 2);
        count += 2;

        String string = new String(displayBuffer, count, length);
        count += length;
        return string;
    }

    /**
     * Sets the security database in the service request buffer, in
     * case it is provided.
     *
     * @param srb
     *         - ServiceRequestBuffer
     */
    private void setSecurityDatabaseArgument(ServiceRequestBuffer srb) {
        if (securityDatabase != null) {
            srb.addArgument(isc_spb_dbname, securityDatabase);
        }
    }

    /**
     * Perform the specified action.
     *
     * @param action
     *         action to execute
     * @param user
     *         user
     */
    private void userAction(int action, User user) throws SQLException {
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = getUserSRB(service, action, user);
            setSecurityDatabaseArgument(srb);
            executeServicesOperation(service, srb);
        }
    }

    public void add(User user) throws SQLException, IOException {
        if (user.getUserName() == null) {
            throw new SQLException("UserName is required.");
        }
        userAction(isc_action_svc_add_user, user);
    }

    public void delete(User user) throws SQLException, IOException {
        if (user.getUserName() == null) {
            throw new SQLException("UserName is required.");
        }
        // Only parameter for delete action is username. All others should be null.
        User delUser = new FBUser();
        delUser.setUserName(user.getUserName());
        userAction(isc_action_svc_delete_user, delUser);
    }

    public void update(User user) throws SQLException, IOException {
        if (user.getUserName() == null) {
            throw new SQLException("UserName is required.");
        }
        userAction(isc_action_svc_modify_user, user);
    }

    @SuppressWarnings("RedundantThrows")
    public Map<String, User> getUsers() throws SQLException, IOException {
        OutputStream savedStream = getLogger();
        setLogger(new ByteArrayOutputStream());
        try {
            userAction(isc_action_svc_display_user, new FBUser());
            return getFBUsers();
        } finally {
            setLogger(savedStream);
        }
    }

    public void setSecurityDatabase(String securityDatabase) {
        this.securityDatabase = securityDatabase;
    }

    /**
     * Services API execution for setting and dropping the auto admin role mapping
     *
     * @param action
     *         action to execute
     */
    private void adminRoleAction(int action) throws SQLException {
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = service.createServiceRequestBuffer();
            srb.addArgument(action);
            setSecurityDatabaseArgument(srb);
            executeServicesOperation(service, srb);
        }
    }

    @SuppressWarnings("RedundantThrows")
    public void setAdminRoleMapping() throws SQLException, IOException {
        adminRoleAction(isc_action_svc_set_mapping);
    }

    @SuppressWarnings("RedundantThrows")
    public void dropAdminRoleMapping() throws SQLException, IOException {
        adminRoleAction(isc_action_svc_drop_mapping);
    }
}
