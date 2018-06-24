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
package org.firebirdsql.gds;

/**
 * Constants for Jaybird specific error codes.
 * <p>
 * The error code of Jaybird use the same scheme as the rest of Firebird error codes. The error codes are not
 * maintained within Firebird, but here and in associated files {@code jaybird_error_msg.properties}
 * and {@code jaybird_error_sqlstates.properties}
 * </p>
 * <p>
 * For error codes, Firebird has reserved facility code {@code 26} for Jaybird. Facility code 26 has error codes in
 * range 337248256 - 337264639. See Firebird {@code src\common\msg_encode.h} for calculation of this range.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface JaybirdErrorCodes {

    @SuppressWarnings("unused")
    int jb_range_start              = 337248256;
    int jb_blobGetSegmentNegative   = 337248257;
    int jb_blobPutSegmentEmpty      = 337248258;
    int jb_blobPutSegmentTooLong    = 337248259;
    int jb_blobIdAlreadySet         = 337248260;
    int jb_invalidClumpletStructure = 337248261;
    int jb_clumpletReaderUsageError = 337248262;
    int jb_invalidConnectionString  = 337248263;
    int jb_concurrencyResetReadOnlyReasonNotUpdatable    = 337248264;
    int jb_resultSetTypeUpgradeReasonHoldability         = 337248265;
    int jb_resultSetTypeDowngradeReasonScrollSensitive   = 337248266;
    int jb_concurrencyResetReadOnlyReasonStoredProcedure = 337248267;
    int jb_errorAsynchronousEventChannelClose            = 337248268;
    int jb_unableToCancelEventReasonNotConnected         = 337248269;
    int jb_executeImmediateRequiresTransactionAttached   = 337248270;
    int jb_executeImmediateRequiresNoTransactionDetached = 337248271;
    int jb_receiveTrustedAuth_NotSupported               = 337248272;
    int jb_notConnectedToServer     = 337248273;
    int jb_notAttachedToDatabase    = 337248274;
    int jb_maxStatementLengthExceeded                    = 337248275;
    int jb_unexpectedOperationCode  = 337248276;
    int jb_unsupportedFieldType     = 337248277;
    int jb_invalidFetchDirection    = 337248278;
    int jb_operationNotAllowedOnForwardOnly              = 337248279;
    int jb_cryptNoCryptKeyAvailable = 337248280;
    int jb_cryptAlgorithmNotAvailable                    = 337248281;
    int jb_cryptInvalidKey          = 337248282;
    int jb_invalidConnectionPropertyValue                = 337248283;
    int jb_dbCryptCallbackInitError = 337248284;
    int jb_dbCryptDataError         = 337248285;
    int jb_hashAlgorithmNotAvailable                     = 337248286;
    int jb_noKnownAuthPlugins       = 337248287;

    @SuppressWarnings("unused")
    int jb_range_end                = 337264639;
}
