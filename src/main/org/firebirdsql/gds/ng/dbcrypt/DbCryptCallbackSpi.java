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
package org.firebirdsql.gds.ng.dbcrypt;

/**
 * Service provider interface for database encryption callback plugins.
 * <p>
 * NOTE: This plugin is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public interface DbCryptCallbackSpi {

    /**
     * Name of the database encryption callback.
     * <p>
     * This name is for identification and selection purposes. As the name will be used in connection properties, we
     * suggest to use relatively simple/short names, but make sure it is unique enough to prevent name conflicts.
     * Consider using something like {@code <company-or-author>.<name>}.
     * </p>
     *
     * @return Name for identifying this callback within Jaybird.
     */
    String getDbCryptCallbackName();

    /**
     * Creates the database encryption callback with a configuration string.
     * <p>
     * The configuration string of the {@code dbCryptConfig} connection property is plugin specific, but we suggest the
     * following conventions:
     * </p>
     * <ul>
     * <li>For binary data, use prefix {@code base64:} to indicate the rest of the string is base64-encoded</li>
     * <li>Avoid use of {@code &}, {@code ;} or {@code :}, or 'hide' this by using base64 encoding; this is necessary to
     * avoid existing limitations in the parsing of connection properties that are added directly to the URL (we
     * hope to address this in the future), and to allow support for other prefixes similar to {@code base64:}</li>
     * </ul>
     *
     * @param dbCryptConfig
     *         Configuration string from connection properties, or {@code null} if absent
     * @return Database encryption callback
     */
    DbCryptCallback createDbCryptCallback(String dbCryptConfig);

}
