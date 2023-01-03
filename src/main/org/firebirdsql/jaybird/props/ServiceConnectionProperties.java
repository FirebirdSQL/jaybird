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
 * Properties for services.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public interface ServiceConnectionProperties extends AttachmentProperties {

    /**
     * Gets the service name (defaults to {@code "service_mgr"}).
     *
     * @return database name
     * @see #setServiceName(String)
     */
    default String getServiceName() {
        return getProperty(PropertyNames.serviceName, PropertyConstants.DEFAULT_SERVICE_NAME);
    }

    /**
     * Sets the service name
     * <p>
     * When {@code serverName} is {@code null}, then the value is taken as the URL of the service, and exact
     * interpretation depends on the protocol implementation ({@code type}). Examples:
     * </p>
     * <ul>
     * <li>//localhost/ &mdash; PURE_JAVA, NATIVE (for NATIVE, this format is parsed and
     * transformed to the next example; will fail on Firebird 2.5 and earlier)</li>
     * <li>localhost &mdash; NATIVE, PURE_JAVA, </li><li>//localhost:3051/ &mdash; PURE_JAVA, NATIVE (for NATIVE, this format is parsed and
     * transformed to the next example; will fail on Firebird 2.5 and earlier)</li>
     * <li>//localhost/service_mgr &mdash; PURE_JAVA, NATIVE (for NATIVE, this format is parsed and
     * transformed to the next example)</li>
     * <li>localhost:service_mgr &mdash; NATIVE, PURE_JAVA</li>
     * <li>//localhost:3051/service_mgr &mdash; PURE_JAVA, NATIVE (for NATIVE, this format is parsed and
     * transformed to the next example)</li>
     * <li>localhost/3051:service_mgr &mdash; NATIVE, PURE_JAVA</li>
     * <li>service_mgr &mdash; NATIVE, EMBEDDED, PURE_JAVA (PURE_JAVA will use localhost
     * as {@code serverName}, depending on the Firebird version and platform, NATIVE may use Firebird Embedded)</li>
     * <li>xnet://service_mgr &mdash; NATIVE (EMBEDDED will behave as NATIVE, protocols like PURE_JAVA may
     * attempt to connect to a server called {@code xnet}) TODO: Check if actually valid</li>
     * <li>other Firebird {@code fbclient} connection URLs &mdash; NATIVE, (EMBEDDED will behave as NATIVE, protocols
     * like PURE_JAVA may interpret the protocol name as a host name</li>
     * <li>Custom {@code type} implementations may support other URL formats</li>
     * </ul>
     * <p>
     * Some protocols, for example PURE_JAVA, when {@code serverName} is not set, but {@code serviceName} doesn't seem
     * to contain a host name, may default to attempting to connect to localhost with {@code serviceName} as the
     * service.
     * </p>
     * <p>
     * When {@code serverName} is set, the value is taken as the database path or alias. Examples:
     * </p>
     * <ul>
     * <li>service_mgr</li>
     * <li>empty string TODO verify if that works</li>
     * <li>{@code null}</li>
     * </ul>
     *
     * @param serviceName
     *         service name
     */
    default void setServiceName(String serviceName) {
        setProperty(PropertyNames.serviceName, serviceName);
    }

    /**
     * Gets the expected db ({@code isc_spb_expected_db}; defaults to {@code null}).
     *
     * @return expected database
     * @see #setExpectedDb(String)
     */
    default String getExpectedDb() {
        return getProperty(PropertyNames.expectedDb);
    }

    /**
     * Filename or alias of the database expected to be accessed by the service operation ({@code isc_spb_expected_db}).
     * <p>
     * For Firebird 3.0 and higher when using a non-default security database, so Firebird knows which database to use
     * to authenticate. When using the default security database, this property does not need to be set.
     * </p>
     * <p>
     * Some service implementations (e.g. {@link org.firebirdsql.management.BackupManager})
     * may explicitly set this as part of their operation when its current value is {@code null}.
     * </p>
     *
     * @param expectedDb
     *         Expected database
     */
    default void setExpectedDb(String expectedDb) {
        setProperty(PropertyNames.expectedDb, expectedDb);
    }
}
