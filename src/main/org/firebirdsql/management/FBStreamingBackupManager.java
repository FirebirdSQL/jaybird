// SPDX-FileCopyrightText: Copyright 2016-2017 Ivan Arabadzhiev
// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;

/**
 * Implements the streaming version of the backup and restore functionality of
 * Firebird Services API.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public class FBStreamingBackupManager extends FBBackupManagerBase implements BackupManager {

    private OutputStream backupOutputStream = null;
    private BufferedInputStream restoreInputStream = null;

    private int backupBufferSize = BUFFER_SIZE * 30; // 30K
    private static final int MAX_RESTORE_CHUNK = 65532;

    private static final int DATA_NOT_READY = 0;
    private static final int END_OF_STREAM = -1;

    /**
     * Set the local buffer size to be used when doing a backup. Default is
     * 30720
     *
     * @param bufferSize
     *        The buffer size to be used, a positive value
     */
    public void setBackupBufferSize(int bufferSize) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }
        this.backupBufferSize = bufferSize;
    }

    /**
     * Create a new instance of <code>FBStreamingBackupManager</code> based on
     * the default GDSType.
     */
    public FBStreamingBackupManager() {
    }

    /**
     * Create a new instance of <code>FBStreamingBackupManager</code> based on a
     * given GDSType.
     *
     * @param gdsType
     *        type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBStreamingBackupManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBStreamingBackupManager</code> based on a
     * given GDSType.
     *
     * @param gdsType
     *        type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBStreamingBackupManager(GDSType gdsType) {
        super(gdsType);
    }

    public void setBackupPath(String backupPath) {
        throw new IllegalArgumentException("You cannot use setBackupPath(String) for Streaming backups.");
    }

    public void addBackupPath(String path, int size) {
        throw new IllegalArgumentException("You cannot use setBackupPath(String) for Streaming backups.");
    }

    public void setBackupOutputStream(OutputStream backupStream) {
        backupOutputStream = backupStream;
    }

    public void setRestoreInputStream(InputStream restoreStream) {
        restoreInputStream = (restoreStream instanceof BufferedInputStream bufferedInputStream)
                ? bufferedInputStream
                : new BufferedInputStream(restoreStream, 128 * MAX_RESTORE_CHUNK);
    }

    public void clearBackupPaths() {
        backupOutputStream = null;
    }

    public void backupDatabase(int options) throws SQLException {
        if (backupOutputStream == null) {
            throw new SQLException("No output stream specified for the backup.");
        }

        try (FbService service = attachServiceManager()) {
            executeServiceBackupOperation(service, getBackupSRB(service, options));
        }
    }

    public void restoreDatabase(int options) throws SQLException {
        if (restoreInputStream == null) {
            throw new SQLException("No input stream specified for the restore.");
        }
        try (FbService service = attachServiceManager()) {
            executeServiceRestoreOperation(service, getRestoreSRB(service, options));
        }
    }

    /**
     * Streaming backups are currently not capable of verbose output
     */
    protected boolean verboseBackup() {
        return false;
    }

    /**
     * Set the page size that will be used for a restored database. The value
     * for {@code pageSize} must be one at least 4096, see als {@link PageSizeConstants}. The
     * default value depends on the Firebird version. Pages smaller than 4096
     * were dropped in 2006 and are by definition unavailable with the streaming
     * functionality of the Services API
     *
     * @param pageSize
     *        The page size to be used in a restored database, see {@link PageSizeConstants} with a minimum of 4096
     * @see PageSizeConstants
     */
    @Override
    public void setRestorePageSize(int pageSize) {
        if (pageSize < PageSizeConstants.SIZE_4K) {
            throw new IllegalArgumentException(
                    "FirebirdSQL versions with streaming restore support don't support pages below 4096");
        }
        super.setRestorePageSize(pageSize);
    }

    /**
     * Adds stdout as a source for the backup operation
     *
     * @param backupSPB
     *        The buffer to be used during the backup operation
     */
    protected void addBackupsToBackupRequestBuffer(FbService service, ServiceRequestBuffer backupSPB) {
        backupSPB.addArgument(isc_spb_bkp_file, "stdout");
    }

    /**
     * Adds stdin as a source for the restore operation
     *
     * @param restoreSPB
     *        The buffer to be used during the restore operation
     */
    protected void addBackupsToRestoreRequestBuffer(FbService service, ServiceRequestBuffer restoreSPB) {
        restoreSPB.addArgument(isc_spb_bkp_file, "stdin");
    }

    private void executeServiceBackupOperation(FbService service, ServiceRequestBuffer srb) throws SQLException {
        try {
            service.startServiceAction(srb);

            ServiceRequestBuffer infoSRB = service.createServiceRequestBuffer();
            infoSRB.addArgument(isc_info_svc_to_eof);

            int bufferSize = backupBufferSize;

            boolean processing = true;

            while (processing) {
                byte[] buffer = service.getServiceInfo(null, infoSRB, bufferSize);

                switch (buffer[0]) {
                case isc_info_svc_to_eof:
                    if (readOutput(buffer, 0, backupOutputStream) == END_OF_STREAM) {
                        processing = false;
                    }
                    break;
                case isc_info_truncated:
                    bufferSize = bufferSize * 2;
                    break;
                case isc_info_end:
                    processing = false;
                    break;
                default:
                    throw new SQLException("Unexpected response from service.");
                }
            }
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    private void executeServiceRestoreOperation(FbService service, ServiceRequestBuffer srb) throws SQLException {
        OutputStream currentLogger = getLogger();
        if (this.verbose && currentLogger == null) {
            throw new SQLException("Verbose mode was requested but there is no logger provided.");
        }
        try {
            service.startServiceAction(srb);

            ServiceRequestBuffer infoSRB = service.createServiceRequestBuffer();
            infoSRB.addArgument(isc_info_svc_stdin);
            infoSRB.addArgument(isc_info_svc_line);
            ServiceParameterBuffer infoSPB = null;

            int bufferSize = BUFFER_SIZE;
            byte[] stdinBuffer = new byte[MAX_RESTORE_CHUNK];
            byte[] newLine = System.lineSeparator().getBytes();
            boolean processing = true;
            boolean sending = true;

            byte[] buffer;

            while (processing || infoSPB != null) {
                buffer = service.getServiceInfo(infoSPB, infoSRB, bufferSize);

                if (infoSPB != null && !sending) {
                    infoSRB = service.createServiceRequestBuffer();
                    infoSRB.addArgument(isc_info_svc_line);
                }

                infoSPB = null;

                int codePos = 0;
                while (codePos < buffer.length && buffer[codePos] != isc_info_end) {
                    switch (buffer[codePos]) {
                    case isc_info_svc_stdin -> {
                        int requestedBytes = Math.min(iscVaxInteger(buffer, ++codePos, 4), stdinBuffer.length);
                        codePos += 4;
                        if (requestedBytes > 0) {
                            int actuallyReadBytes = restoreInputStream.read(stdinBuffer, 0, requestedBytes);
                            if (actuallyReadBytes > 0) {
                                infoSPB = service.createServiceParameterBuffer();
                                if (stdinBuffer.length == actuallyReadBytes) {
                                    infoSPB.addArgument(isc_info_svc_line, stdinBuffer);
                                } else {
                                    infoSPB.addArgument(isc_info_svc_line,
                                            Arrays.copyOfRange(stdinBuffer, 0, actuallyReadBytes));
                                }
                            }

                            restoreInputStream.mark(2);
                            if (restoreInputStream.read() < 0) {
                                sending = false;
                            } else {
                                restoreInputStream.reset();
                            }
                        }
                    }
                    case isc_info_truncated -> {
                        bufferSize *= 2;
                        ++codePos;
                    }
                    case isc_info_svc_line -> {
                        int bytesToLog = readOutput(buffer, codePos, currentLogger);
                        codePos += 3;
                        switch (bytesToLog) {
                        case DATA_NOT_READY -> ++codePos;
                        case END_OF_STREAM -> processing = false;
                        default -> {
                            codePos += bytesToLog;
                            if (currentLogger != null) {
                                currentLogger.write(newLine);
                            }
                        }
                        }
                    }
                    case isc_info_end -> {
                        // Shouldn't actually happen due to condition of while-loop
                    }
                    default -> throw new SQLException("Unexpected response from service.");
                    }
                }
            }
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    private int readOutput(byte[] buffer, int offset, OutputStream out) throws SQLException, IOException {
        int dataLength = iscVaxInteger2(buffer, offset + 1);
        if (dataLength == 0) {
            return switch (buffer[offset + 3]) {
                case isc_info_data_not_ready -> DATA_NOT_READY;
                case isc_info_end -> END_OF_STREAM;
                default -> throw new SQLException("Unexpected end of stream reached.");
            };
        }
        if (out != null) {
            out.write(buffer, offset + 3, dataLength);
        }
        return dataLength;
    }
}
