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

import org.firebirdsql.gds.GDSType;

/**
 * Implements the backup and restore functionality of Firebird Services API.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class FBDatabaseManager extends FBServiceManager implements
        DatabaseManager {

    /**
     * Create and instance of this class.
     */
    public FBDatabaseManager(GDSType gdsType) {
        super(gdsType);
    }

    /* (non-Javadoc)
     * @see org.firebirdsql.management.DatabaseManager#getProperties()
     */
    public void getProperties() {
        
        //TODO: Implement isc_action_svc_properties action

    }

    /* (non-Javadoc)
     * @see org.firebirdsql.management.DatabaseManager#repair()
     */
    public void repair() {
        
        //TODO: Implement isc_action_svc_repair action

    }

    /* (non-Javadoc)
     * @see org.firebirdsql.management.DatabaseManager#getStats()
     */
    public void getStats() {
        
        //TODO: Implement isc_action_svc_db_stats action

    }

    /* (non-Javadoc)
     * @see org.firebirdsql.management.DatabaseManager#getLog()
     */
    public void getLog() {
        
        //TODO: Implement isc_action_svc_get_ib_log action

    }

    /* (non-Javadoc)
     * @see org.firebirdsql.management.DatabaseManager#addLicense()
     */
    public void addLicense() {
        
        //TODO: Implement isc_action_svc_add_license action

    }

    /* (non-Javadoc)
     * @see org.firebirdsql.management.DatabaseManager#removeLicense()
     */
    public void removeLicense() {
        
        //TODO: Implement isc_action_svc_remove_license action

    }

}
