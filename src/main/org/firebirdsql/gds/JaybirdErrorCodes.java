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
 * @author Mark Rotteveel
 * @since 3.0
 */
@SuppressWarnings({ "java:S115", "java:S1214" })
public interface JaybirdErrorCodes {

    @SuppressWarnings("unused")
    int jb_range_start              = 337248256;
    int jb_blobGetSegmentNegative   = 337248257;
    int jb_blobPutSegmentEmpty      = 337248258;
    /**
     * @deprecated Jaybird no longer uses this error code
     */
    @Deprecated(since = "6")
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
    int jb_invalidGeneratedKeysOption                    = 337248288;
    int jb_generatedKeysSupportNotAvailable              = 337248289;
    int jb_generatedKeysArrayEmptyOrNull                 = 337248290;
    int jb_generatedKeysInvalidColumnPosition            = 337248291;
    int jb_generatedKeysNoColumnsFound                   = 337248292;
    int jb_explainedExecutionPlanNotSupported            = 337248293;
    int jb_failedToLoadNativeLibrary                     = 337248294;
    int jb_blobClosed               = 337248295;
    int jb_invalidTimeout           = 337248296;
    int jb_invalidExecutor          = 337248297;
    int jb_operationClosed          = 337248298;
    int jb_closeCursorBeforeCount   = 337248299;
    int jb_invalidParameterCount    = 337248300;
    int jb_parameterNotSet          = 337248301;
    int jb_stmtNotAllocated         = 337248302;
    int jb_stmtClosed               = 337248303;
    int jb_stmtInErrorRequireClose  = 337248304;
    /**
     * @deprecated use {@link #jb_stmtInErrorRequireClose}
     */
    @SuppressWarnings("java:S1845")
    @Deprecated(forRemoval = true, since = "6")
    int jb_stmtInErrorRequireCLose  = jb_stmtInErrorRequireClose;
    int jb_invalidTransactionStateTransition             = 337248305;
    int jb_unexpectedInfoResponse   = 337248306;
    int jb_infoResponseEmpty        = 337248307;
    int jb_couldNotChangeSoTimeout  = 337248308;
    int jb_localTransactionActive   = 337248309;
    int jb_invalidFetchSize         = 337248310;
    int jb_operationNotCancellable  = 337248311;
    int jb_executeQueryWithTxStmt   = 337248312;
    int jb_commitStatementNotAllowed                     = 337248313;
    int jb_rollbackStatementNotAllowed                   = 337248314;
    int jb_setTransactionStatementNotAllowed             = 337248315;
    int jb_setTransactionNotAllowedInAutoCommit          = 337248316;
    int jb_setTransactionNotAllowedActiveTx              = 337248317;
    int jb_statementNotAssociatedWithConnection          = 337248318;
    int jb_addBatchWithTxStmt       = 337248319;
    int jb_prepareCallWithTxStmt    = 337248320;
    int jb_invalidResultSetType     = 337248321;
    int jb_invalidResultSetConcurrency                   = 337248322;
    int jb_invalidResultSetHoldability                   = 337248323;
    int jb_invalidTransactionHandleType                  = 337248324;
    int jb_invalidEventHandleType                        = 337248325;
    int jb_eventHandleNotInitialized                     = 337248326;

    @SuppressWarnings("unused")
    int jb_range_end                = 337264639;
}
