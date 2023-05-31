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
package org.firebirdsql.gds;

import org.firebirdsql.jaybird.fb.constants.DpbItems;
import org.firebirdsql.jaybird.fb.constants.SpbItems;

/**
 * Mapping of connection parameter buffer item tags.
 * <p>
 * This mapping is intended to reduce code duplication with database and service parameter buffers.
 * </p>
 * <p>
 * For now this only contains authentication related tags. This may be expanded in the future.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public enum ParameterTagMapping {

    DPB {
        @Override
        public int getUserNameTag() {
            return DpbItems.isc_dpb_user_name;
        }

        @Override
        public int getPasswordTag() {
            return DpbItems.isc_dpb_password;
        }

        @Override
        public int getEncryptedPasswordTag() {
            return DpbItems.isc_dpb_password_enc;
        }

        @Override
        public int getTrustedAuthTag() {
            return DpbItems.isc_dpb_trusted_auth;
        }

        @Override
        public int getAuthPluginNameTag() {
            return DpbItems.isc_dpb_auth_plugin_name;
        }

        @Override
        public int getAuthPluginListTag() {
            return DpbItems.isc_dpb_auth_plugin_list;
        }

        @Override
        public int getSpecificAuthDataTag() {
            return DpbItems.isc_dpb_specific_auth_data;
        }

        @Override
        public int getConfigTag() {
            return DpbItems.isc_dpb_config;
        }
    },
    SPB {
        @Override
        public int getUserNameTag() {
            return SpbItems.isc_spb_user_name;
        }

        @Override
        public int getPasswordTag() {
            return SpbItems.isc_spb_password;
        }

        @Override
        public int getEncryptedPasswordTag() {
            return SpbItems.isc_spb_password_enc;
        }

        @Override
        public int getTrustedAuthTag() {
            return SpbItems.isc_spb_trusted_auth;
        }

        @Override
        public int getAuthPluginNameTag() {
            return SpbItems.isc_spb_auth_plugin_name;
        }

        @Override
        public int getAuthPluginListTag() {
            return SpbItems.isc_spb_auth_plugin_list;
        }

        @Override
        public int getSpecificAuthDataTag() {
            return SpbItems.isc_spb_specific_auth_data;
        }

        @Override
        public int getConfigTag() {
            return SpbItems.isc_spb_config;
        }
    };

    public abstract int getUserNameTag();
    public abstract int getPasswordTag();
    public abstract int getEncryptedPasswordTag();
    public abstract int getTrustedAuthTag();
    public abstract int getAuthPluginNameTag();
    public abstract int getAuthPluginListTag();
    public abstract int getSpecificAuthDataTag();
    public abstract int getConfigTag();
}
