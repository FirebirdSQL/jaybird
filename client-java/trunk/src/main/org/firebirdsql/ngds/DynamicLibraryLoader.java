package org.firebirdsql.ngds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 
 */
public class DynamicLibraryLoader {

    private static final String TEMP_SUFFIX = "jlib";
    private static final String TEMP_PREFIX = "jaybird";
    
    /**
     * Load the library from the classpath. This method will check whether
     * the classpath contains the specified library. If no library can be found,
     * <code>false</code> is returned. However, when classpath contains the 
     * requested library, its content is copied into the temporary file and 
     * this file is later loaded using the {@link Runtime#load(java.lang.String)}
     * method.
     * 
     * @param libraryName name of the library to load. This name is translated 
     * into the platform-depended name using the 
     * {@link System#mapLibraryName(java.lang.String)} method. The new name is
     * used to search the library in the path.
     * 
     * @return <code>true</code> if library was found in the classpath and
     * was successfully loaded into the JVM.
     * 
     * @throws IOException If I/O error occured during the operation.
     */
    public static boolean loadLibraryFromClassPath(String libraryName) throws IOException {
        String platformLibName = System.mapLibraryName(libraryName);
        
        InputStream in = DynamicLibraryLoader.class.getResourceAsStream(platformLibName);
        
        if (in == null)
            return false;
        
        try {
            File tempFile = File.createTempFile(TEMP_PREFIX, TEMP_SUFFIX);
            FileOutputStream out = new FileOutputStream(tempFile);
            
            int counter = 0;
            byte[] buffer = new byte[8192];
            while((counter = in.read(buffer)) != -1)
                out.write(buffer, 0, counter);
            
            out.flush();
            out.close();
            
            tempFile.deleteOnExit();
            
            String canonicalFileName = tempFile.getCanonicalPath();
            
            Runtime.getRuntime().load(canonicalFileName);
            
            return true;
            
        } finally {
            in.close();
        }
    }
}
