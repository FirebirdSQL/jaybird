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
package org.firebirdsql.gds;

import org.firebirdsql.gds.impl.wire.AbstractJavaGDSImpl;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Creates gds objects.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class GDSObjectFactory {

    private static Logger log = LoggerFactory.getLogger(GDSObjectFactory.class, false);

    /**
     * @return a new JavaGDSImpl object.
     */
    public static AbstractJavaGDSImpl createJavaGDSImpl() {

        try {

            return (AbstractJavaGDSImpl) ClassFactory.get(ClassFactory.JavaGDSImpl).newInstance();

        } catch (Exception e) {

            log.error(e.getMessage(), e);

        }

        return null;

    }

}
