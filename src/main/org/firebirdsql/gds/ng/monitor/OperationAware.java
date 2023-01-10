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
package org.firebirdsql.gds.ng.monitor;

import org.firebirdsql.gds.ng.OperationMonitor;

/**
 * Allows monitoring of driver operations like the execution of statements.
 * <p>
 * The notification of {@link #startOperation(Operation)} and {@link #endOperation(Operation)} occurs on the thread
 * performing the operation. Implementations of {@code OperationAware} should complete these methods as quick as
 * possible and prevent any blocking operations to avoid excessive performance degradation of the driver.
 * </p>
 * <p>
 * <b>Note</b>: This is an experimental feature. The implementation or API may be removed or changed at any time.
 * </p>
 *
 * @author Vasiliy Yashkov
 * @see OperationMonitor
 * @since 4.0
 */
public interface OperationAware {

    /**
     * Start of operation.
     *
     * @param operation
     *         operation.
     */
    void startOperation(Operation operation);

    /**
     * End of operation.
     *
     * @param operation
     *         operation.
     */
    void endOperation(Operation operation);

}
