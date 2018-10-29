package org.firebirdsql.nativeoo.gds.ng;

import com.sun.jna.Pointer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.ISCConstants.isc_arg_number;

/**
 * Class for handling exceptions from native OO API.
 *
 * @since 4.0
 */
public class FbException extends SQLException {

    private static final long serialVersionUID = 1L;

    public FbException(Throwable t) {
        super(t);
    }

    public FbException(String msg) {
        super(msg);
    }

    public FbException(String msg, Throwable t) {
        super(msg, t);
    }

    public FbException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
    }

    public static void rethrow(Throwable t) throws FbException {
        throw new FbException(null, t);
    }

    public static void catchException(IStatus status, Throwable t) {

        while (t != null && t instanceof FbException && t.getMessage() == null)
            t = t.getCause();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String msg = sw.toString();

        try (CloseableMemory memory = new CloseableMemory(msg.length() + 1)) {
            memory.setString(0, msg);

            Pointer[] vector = new Pointer[]{
                    new Pointer(ISCConstants.isc_arg_gds),
                    new Pointer(ISCConstants.isc_random),
                    new Pointer(ISCConstants.isc_arg_cstring),
                    new Pointer(msg.length()),
                    memory,
                    new Pointer(ISCConstants.isc_arg_end)
            };

            status.setErrors2(vector.length, vector);
        }
    }

    public static void checkException(IStatus status) throws FbException {
        if ((status.getState() & IStatus.STATE_ERRORS) != 0) {
            FbExceptionBuilder builder = new FbExceptionBuilder();

            int offset = 0;

            processingLoop:
            while (status.getErrors().getInt(offset) != isc_arg_end) {

                int arg = status.getErrors().getInt(offset);

                switch (arg) {
                    case isc_arg_gds:
                        offset += 8;
                        int iscCode = status.getErrors().getInt(offset);
                        builder.exception(iscCode);
                        break;
                    case isc_arg_interpreted:
                    case isc_arg_string:
                    case isc_arg_sql_state:
                        offset += 8;
                        long stringPointerAddress = status.getErrors().getLong(offset);
                        if (stringPointerAddress == 0L) {
                            break processingLoop;
                        }
                        Pointer stringPointer = new Pointer(stringPointerAddress);
                        String stringValue = stringPointer.getString(0);
                        if (arg != isc_arg_sql_state) {
                            builder.messageParameter(stringValue);
                        } else {
                            builder.sqlState(stringValue);
                        }
                        break;
                    case isc_arg_cstring:
                        offset += 8;
                        int stringLength = status.getErrors().getInt(offset);
                        offset += 8;
                        long cStringPointerAddress = status.getErrors().getLong(offset);
                        Pointer cStringPointer = new Pointer(cStringPointerAddress);
                        byte[] stringData = cStringPointer.getByteArray(0, stringLength);
                        String cStringValue = new String(stringData);
                        builder.messageParameter(cStringValue);
                        break;
                    case isc_arg_number:
                        offset += 8;
                        int intValue = status.getErrors().getInt(offset);
                        builder.messageParameter(intValue);
                        break;
                }

                offset += 8;
            }


            if (!builder.isEmpty()) {
                SQLException exception = builder.toSQLException();
                throw new FbException(exception.getMessage(), exception.getSQLState(), exception.getErrorCode(),
                        exception);
            }
        }
    }
}
