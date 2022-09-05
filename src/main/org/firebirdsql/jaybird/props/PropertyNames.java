/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jaybird.props;

/**
 * Property names and aliases used by Jaybird.
 * <p>
 * In defiance of normal style rules for Java, the constants defined in this class use the same name as their value
 * (if syntactically valid).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class PropertyNames {

    // attachment
    public static final String serverName = "serverName";
    public static final String portNumber = "portNumber";
    public static final String attachObjectName = "attachObjectName";
    // NOTE: alias for attachObjectName
    public static final String databaseName = "databaseName";
    // NOTE: alias for attachObjectName
    public static final String serviceName = "serviceName";
    public static final String type = "type";
    public static final String user = "user";
    public static final String password = "password";
    public static final String roleName = "roleName";
    public static final String processId = "processId";
    public static final String processName = "processName";
    public static final String charSet = "charSet";
    public static final String encoding = "encoding";
    public static final String socketBufferSize = "socketBufferSize";
    public static final String soTimeout = "soTimeout";
    public static final String connectTimeout = "connectTimeout";
    public static final String wireCrypt = "wireCrypt";
    public static final String dbCryptConfig = "dbCryptConfig";
    public static final String authPlugins = "authPlugins";
    public static final String wireCompression = "wireCompression";

    // database connection
    public static final String sqlDialect = "sqlDialect";
    public static final String blobBufferSize = "blobBufferSize";
    public static final String useStreamBlobs = "useStreamBlobs";
    public static final String pageCacheSize = "pageCacheSize";
    public static final String defaultResultSetHoldable = "defaultResultSetHoldable";
    public static final String useFirebirdAutocommit = "useFirebirdAutocommit";
    public static final String generatedKeysEnabled = "generatedKeysEnabled";
    public static final String dataTypeBind = "dataTypeBind";
    public static final String sessionTimeZone = "sessionTimeZone";
    public static final String ignoreProcedureType = "ignoreProcedureType";
    public static final String columnLabelForName = "columnLabelForName";
    public static final String decfloatRound = "decfloatRound";
    public static final String decfloatTraps = "decfloatTraps";
    public static final String tpbMapping = "tpbMapping";
    public static final String defaultIsolation = "defaultIsolation";
    public static final String scrollableCursor = "scrollableCursor";
    public static final String useServerBatch = "useServerBatch";
    public static final String serverBatchBufferSize = "serverBatchBufferSize";

    /**
     * @deprecated This property has unclear semantics and will be removed in a future version (Jaybird 6 or later)
     */
    @Deprecated
    public static final String timestampUsesLocalTimezone = "timestampUsesLocalTimezone";

    private PropertyNames() {
        // no instances
    }
}
