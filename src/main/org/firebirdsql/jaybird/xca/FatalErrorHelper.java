// SPDX-FileCopyrightText: Copyright 2005-2007 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.gds.ISCConstants;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;

import static org.firebirdsql.jdbc.SQLStateConstants.SQLSTATE_CLASS_CONNECTION_ERROR;

/**
 * Helper class for the exception handling in XCA framework. The JCA specification
 * required a resource adapter to report an error if it is certain that no other
 * operations can be executed over that particular managed connection.
 * <p>
 * In case of Firebird, few errors belong to the so-called "fatal errors", after
 * which client application cannot continue its job. For example, when a socket
 * connection to the server is broken, any subsequent operation will fail. The XCA
 * container should remove the connection from the pool in order to allow process
 * to recover (when Firebird server is restarted).
 * </p>
 * <p>
 * NOTE: Although these methods are intended for use within XCA, they can be used for other parts of Jaybird which have
 * similar needs for connection error evaluation.
 * </p>
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public final class FatalErrorHelper {

    /**
     * Check whether the specified exception is fatal from the XCA point of view.
     *
     * @param exception
     *         exception to check.
     * @return {@code true} if the exception that happened is fatal
     */
    public static boolean isFatal(SQLException exception) {
        return exception != null && (isFatal(exception.getErrorCode()) || isFatal(exception.getSQLState()));
    }

    /**
     * Checks whether {@code errorCode} is fatal from the XCA point of view.
     *
     * @param errorCode
     *         ISC error code
     * @return {@code true} if the error code is (considered) fatal
     */
    private static boolean isFatal(int errorCode) {
        return Arrays.binarySearch(FATAL_ERRORS, errorCode) >= 0;
    }

    /**
     * Checks whether {@code sqlState} is fatal from the XCA point of view.
     *
     * @param sqlState
     *         SQLSTATE value
     * @return {@code true} if the SQLSTATE is (considered) fatal
     */
    private static boolean isFatal(String sqlState) {
        // Don't consider absence of SQLSTATE as a sign of fatal error (we did in the past for FBPooledConnection),
        // there are exceptions in Jaybird without SQLSTATE, and most of them are not fatal.
        if (sqlState == null) return false;
        // Invalid SQLSTATE specified, assume it's fatal
        if (sqlState.length() != 5) return true;
        return FATAL_SQL_STATES.contains(sqlState)
               || FATAL_SQL_STATE_CLASSES.contains(sqlState.substring(0, 2));
    }

    /**
     * Checks whether {@code errorCode} indicates a broken connection. The broken error codes are a subset of the fatal
     * error codes, and generally mean that attempts to send network IO will not work.
     *
     * @param errorCode
     *         ISC error code
     * @return {@code true} if the error code is signals a (possibly) broken connection
     */
    private static boolean isBrokenConnectionErrorCode(int errorCode) {
        return Arrays.binarySearch(BROKEN_CONNECTION_ERRORS, errorCode) >= 0;
    }

    private static boolean isBrokenConnectionSqlState(String sqlState) {
        return sqlState != null && sqlState.startsWith(SQLSTATE_CLASS_CONNECTION_ERROR);
    }

    /**
     * Checks whether {@code exception} indicates a broken connection. There is overlap with
     * {@link #isFatal(SQLException)}, but neither is a subset of the other.
     * <p>
     * Specifically, this method will check if the first {@code SQLException} in the cause-chain of {@code exception}
     * (including {@code exception} itself) has a "broken connection error code" (a proper subset of "fatal error
     * codes"), or otherwise of there is a {@code SocketTimeoutException} or {@code SocketException} in the cause-chain.
     * </p>
     * <p>
     * NOTE: Exact checks done by this method may be revised in any point release, and above documentation should be
     * considered illustrative, and not prescriptive.
     * </p>
     *
     * @param exception
     *         exception to check
     * @return {@code true} if the error code is signals a (possibly) broken connection
     */
    @SuppressWarnings({ "RedundantIfStatement", "java:S1126" })
    public static boolean isBrokenConnection(Exception exception) {
        if (exception == null) {
            return false;
        }

        SQLException firstSqlException = findException(exception, SQLException.class);
        if (firstSqlException != null && (
                isBrokenConnectionErrorCode(firstSqlException.getErrorCode())
                || isBrokenConnectionSqlState(firstSqlException.getSQLState()))) {
            return true;
        }

        if (findException(exception, SocketTimeoutException.class) != null
                || findException(exception, SocketException.class) != null) {
            return true;
        }

        return false;
    }

    private static <T extends Exception> T findException(Exception root, Class<T> exceptionType) {
        Throwable current = root;
        while (current != null) {
            if (exceptionType.isInstance(current)) {
                return exceptionType.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * The constant array {@code FATAL_ERRORS} holds an ORDERED list of isc error codes that indicate that the
     * connection is no longer usable. This is used in the XCA framework to determine if a SQLException should result
     * in a ConnectionErrorOccurred notification to the Connection Manager to destroy the connection. It is essential
     * that this list be ordered so determining if a code is in it can proceed reliably.
     */
    private static final int[] FATAL_ERRORS = new int[] {
// @formatter:off
            ISCConstants.isc_network_error,
            ISCConstants.isc_net_read_err,
            ISCConstants.isc_net_write_err,
//        ISCConstants.isc_bad_db_format,   //probably not a firebird db
//        ISCConstants.isc_bad_db_handle,   //couldn't get a connection
//        ISCConstants.isc_bad_dpb_content, //couldn't get a connection
//        ISCConstants.isc_bad_dpb_form,    //couldn't get a connection
//        ISCConstants.isc_bug_check,
//        ISCConstants.isc_db_corrupt,
            ISCConstants.isc_io_error,
//        ISCConstants.isc_metadata_corrupt,
//
            ISCConstants.isc_open_trans,  //could not forcibly close tx on connection close

//        ISCConstants.isc_port_len,    //user sent buffer too short or long for data
//                                      //expected.  Should never occur
//
            ISCConstants.isc_req_sync,  //client asked for data when server expected
                                        //data or vice versa. Should never happen
//
//        ISCConstants.isc_req_wrong_db,//In a multi-database application, a prepared
//                                      //request has been opened against the wrong
//                                      //database.  Not fatal, but also very
//                                      //unlikely. I'm leaving it in because if we
//                                      //get this, something is horribly wrong.
//
//        ISCConstants.isc_sys_request, //A system service call failed.  Probably fatal.
//                                      //isc_stream_eof, Part of the scrolling cursors stuff, not
//                                      //fatal, simply indicates that you've got to the end of the
//                                      //cursor.
//
            ISCConstants.isc_unavailable,
//        ISCConstants.isc_wrong_ods,
//        ISCConstants.isc_badblk,
//        ISCConstants.isc_relbadblk,
//        ISCConstants.isc_blktoobig,
//        ISCConstants.isc_bufexh,
//        ISCConstants.isc_bufinuse,
//        ISCConstants.isc_bdbincon,
//        ISCConstants.isc_badodsver,
//        ISCConstants.isc_dirtypage,
//        ISCConstants.isc_doubleloc,
//        ISCConstants.isc_nodnotfnd,
//        ISCConstants.isc_dupnodfnd,
//        ISCConstants.isc_locnotmar,
//        ISCConstants.isc_badpagtyp,
//        ISCConstants.isc_corrupt,
//        ISCConstants.isc_badpage,
//        ISCConstants.isc_badindex,
//        ISCConstants.isc_badhndcnt,
//        ISCConstants.isc_connect_reject, //no connection to close
//        ISCConstants.isc_no_lock_mgr,    //no connection to close
//        ISCConstants.isc_blocking_signal,
//        ISCConstants.isc_lockmanerr,
            ISCConstants.isc_bad_detach,     //detach failed...fatal, but there's nothing we can do.
//        ISCConstants.isc_buf_invalid,
//        ISCConstants.isc_bad_lock_level,  //PC_ENGINE only, handles record locking
//                                          //issues from the attempt to make
//                                          //InterBase just like Dbase.
//
            ISCConstants.isc_shutdown,
            ISCConstants.isc_exception_stack_overflow,
            ISCConstants.isc_att_shutdown,
//        ISCConstants.isc_io_create_err,
//        ISCConstants.isc_io_open_err,
//        ISCConstants.isc_io_close_err,
//        ISCConstants.isc_io_read_err,
//        ISCConstants.isc_io_write_err,
//        ISCConstants.isc_io_delete_err,
//        ISCConstants.isc_io_access_err,
            ISCConstants.isc_lost_db_connection,
//        ISCConstants.isc_bad_protocol,
//        ISCConstants.isc_file_in_use
    };
// @formatter:on

    /**
     * Error codes which indicate a broken connection. See also comments in {@link #isBrokenConnection(Exception)}.
     * <p>
     * These error codes should be a subset of {@link #FATAL_ERRORS}.
     * </p>
     */
    private static final int[] BROKEN_CONNECTION_ERRORS = new int[] {
            ISCConstants.isc_network_error,
            ISCConstants.isc_net_read_err,
            ISCConstants.isc_net_write_err,
            ISCConstants.isc_unavailable,
            ISCConstants.isc_shutdown,
            ISCConstants.isc_exception_stack_overflow,
            ISCConstants.isc_att_shutdown,
    };

    // TODO double check firebird and Jaybird implementation for other states or state classes
    private static final Set<String> FATAL_SQL_STATE_CLASSES = Set.of(SQLSTATE_CLASS_CONNECTION_ERROR);

    private static final Set<String> FATAL_SQL_STATES = Set.of(
            "2E000", // Invalid connection name
            "HY001", // Memory allocation error
            "HYT00", // Timeout expired
            "HYT01"  // Connection timeout expired
    );

    private FatalErrorHelper() {
        // no instances
    }

    static {
        Arrays.sort(FATAL_ERRORS);
        Arrays.sort(BROKEN_CONNECTION_ERRORS);
    }
}
