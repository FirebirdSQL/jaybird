/*
 SPDX-FileCopyrightText: Copyright 2004-2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2009 Thomas Steinmaurer
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
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
 * @author Steven Jardine
 * @author Mark Rotteveel
 * @deprecated Use the SQL user management statements instead, we currently do not plan to remove this API
 */
@Deprecated(since = "6")
@SuppressWarnings("DeprecatedIsStillUsed")
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
    private Map<String, User> getFBUsers(ByteArrayOutputStream logger) {
        Map<String, User> users = new TreeMap<>();
        byte[] displayBuffer = logger.toByteArray();
        var userBuilder = new FBUserBuilder();
        count = 0;
        while (count < displayBuffer.length && displayBuffer[count] != isc_info_end) {
            switch (displayBuffer[count]) {
            case isc_spb_sec_username -> {
                if (userBuilder.isInitialized()) {
                    users.put(userBuilder.getUserName(), userBuilder.build());
                }
                userBuilder.reset();
                userBuilder.setUserName(getSRBString(displayBuffer));
            }
            case isc_spb_sec_firstname -> userBuilder.setFirstName(getSRBString(displayBuffer));
            case isc_spb_sec_middlename -> userBuilder.setMiddleName(getSRBString(displayBuffer));
            case isc_spb_sec_lastname -> userBuilder.setLastName(getSRBString(displayBuffer));
            case isc_spb_sec_userid -> userBuilder.setUserId(getSRBInteger(displayBuffer));
            case isc_spb_sec_groupid -> userBuilder.setGroupId(getSRBInteger(displayBuffer));
            default -> count++;
            }
        }
        if (userBuilder.isInitialized()) {
            users.put(userBuilder.getUserName(), userBuilder.build());
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

    @Override
    public void add(User user) throws SQLException, IOException {
        requireUserName(user);
        userAction(isc_action_svc_add_user, user);
    }

    @Override
    public void delete(User user) throws SQLException, IOException {
        requireUserName(user);
        // Only parameter for delete action is username. All others should be null.
        User delUser = new FBUser();
        delUser.setUserName(user.getUserName());
        userAction(isc_action_svc_delete_user, delUser);
    }

    @Override
    public void update(User user) throws SQLException, IOException {
        requireUserName(user);
        userAction(isc_action_svc_modify_user, user);
    }

    private void requireUserName(User user) throws SQLException {
        if (user.getUserName() == null) {
            throw new SQLException("UserName is required.");
        }
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public Map<String, User> getUsers() throws SQLException, IOException {
        OutputStream savedStream = getLogger();
        try (var logger = new ByteArrayOutputStream()) {
            setLogger(logger);
            userAction(isc_action_svc_display_user, new FBUser());
            return getFBUsers(logger);
        } finally {
            setLogger(savedStream);
        }
    }

    @Override
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
    @Override
    public void setAdminRoleMapping() throws SQLException, IOException {
        adminRoleAction(isc_action_svc_set_mapping);
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void dropAdminRoleMapping() throws SQLException, IOException {
        adminRoleAction(isc_action_svc_drop_mapping);
    }

    private static final class FBUserBuilder {

        private String userName;
        private String firstName;
        private String middleName;
        private String lastName;
        private int userId = -1;
        private int groupId = -1;

        FBUser build() {
            return new FBUser(userName, null, firstName, middleName, lastName, userId, groupId);
        }

        /**
         * @return {@code true} if at least {@code userName} is set, {@code false} otherwise
         */
        boolean isInitialized() {
            return userName != null;
        }

        void reset() {
            userName = firstName = middleName = lastName = null;
            userId = groupId = -1;
        }

        void setUserName(String userName) {
            this.userName = userName;
        }

        String getUserName() {
            return userName;
        }

        void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        void setLastName(String lastName) {
            this.lastName = lastName;
        }

        void setUserId(int userId) {
            this.userId = userId;
        }

        void setGroupId(int groupId) {
            this.groupId = groupId;
        }
    }
}
