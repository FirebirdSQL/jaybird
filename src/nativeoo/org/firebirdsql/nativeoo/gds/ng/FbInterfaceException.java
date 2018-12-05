package org.firebirdsql.nativeoo.gds.ng;

/**
 * Class with for wrapping an exception thrown by the native interface.
 *
 * @since 4.0
 */
public class FbInterfaceException {
    public static void catchException(FbInterface.IStatus status, Throwable t) {
        if (t == null)
            return;
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        t.printStackTrace(pw);
        String msg = sw.toString();
        try (CloseableMemory memory = new CloseableMemory(msg.length())) {
            memory.setString(0, msg);
            com.sun.jna.Pointer[] vector = new com.sun.jna.Pointer[]{
                    new com.sun.jna.Pointer(org.firebirdsql.gds.ISCConstants.isc_arg_gds),
                    new com.sun.jna.Pointer(org.firebirdsql.gds.ISCConstants.isc_random),
                    new com.sun.jna.Pointer(org.firebirdsql.gds.ISCConstants.isc_arg_cstring),
                    new com.sun.jna.Pointer(msg.length()),
                    memory,
                    new com.sun.jna.Pointer(org.firebirdsql.gds.ISCConstants.isc_arg_end)
            };
            status.setErrors2(vector.length, vector);
        }
    }
}
