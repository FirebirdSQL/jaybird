/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Roman Rokytskyy
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jdbc;

import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.gds.GDS;

import java.util.Properties;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import java.lang.reflect.*;
import java.io.*;

/**
 * This class maps the extended JDBC properties to the
 * {@link FBConnectionRequestInfo} instance. It uses
 * <code>java.lang.reflection.</code> to determine correct type of the parameter
 * passed to the {@link java.sql.Driver#connect(String, Properties)} method.
 */
public class FBConnectionHelper {

    public static final String DPB_PREFIX = "isc_dpb_";
    public static final String TPB_PREFIX = "isc_tpb_";

    public static final String ISC_ENCODINGS_RESOURCE =
        "isc_encodings.properties";

    private static final HashMap dpbTypes = new HashMap();
    private static final HashMap tpbTypes = new HashMap();

    private static final HashMap iscEncodings = new HashMap();
    private static final HashMap javaEncodings = new HashMap();

    private static boolean encodingsLoaded = false;

    /*
     * Initialize mappings between various GDS constant names and
     * their values. This operation should be executed only once.
     */
    static {
        Class gdsClass = GDS.class;

        Field[] fields = gdsClass.getFields();

        for(int i = 0; i < fields.length; i++) {
            if (!fields[i].getType().getName().equals("int"))
                continue;

            // System.out.println("bla");

            String name = fields[i].getName();
            Integer value;
            try {
                value = (Integer)fields[i].get(null);
            } catch(IllegalAccessException iaex) {
                continue;
            }

            if (name.startsWith(DPB_PREFIX)) {
                // put the correct parameter name
                dpbTypes.put(name.substring(DPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                dpbTypes.put(name, value);
            } else
            if (name.startsWith(TPB_PREFIX)) {
                // put the correct parameter name
                tpbTypes.put(name.substring(TPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                tpbTypes.put(name, value);
            } else
                continue;
        }

    }

    public static FBConnectionRequestInfo getDefaultCri() {
        FBConnectionRequestInfo result = new FBConnectionRequestInfo();

        //result.setProperty(GDS.isc_dpb_num_buffers, new byte[] {90});
        //result.setProperty(GDS.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
        //result.setProperty(GDS.isc_dpb_force_write, new byte[] {0});

        return result;
    }

    /**
     * Get the instance of {@link FBConnectionRequestInfo} containing all
     * relevant parameters from the <code>info</code> properties.
     *
     * @param info instance of {@link Properties} containing connection
     * parameters.
     *
     * @param baseCri connection request info used as the base for a newly
     * created one. All types are copied from the <code>baseCri</code> into
     * the result, but these properties can be overriden by the <code>info</code>
     * properties. Can be <code>null</code>.
     *
     * @return instance of {@link FBConnectionRequestInfo} containing all
     * relevant connection parameters.
     */
    public static FBConnectionRequestInfo getCri(Properties info,
        FBConnectionRequestInfo baseCri)
    {
        FBConnectionRequestInfo cri;

        if (baseCri != null)
            cri = new FBConnectionRequestInfo(baseCri);
        else
            cri = new FBConnectionRequestInfo(getDefaultCri());

        // process all set keys
        Iterator keys = info.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            String value = info.getProperty(key);

            Integer type = (Integer)dpbTypes.get(key);

            // if the type is unknown, continue
            if (type == null) continue;

            // set the value of the DPB
            try {

                // try to deal with a value as a byte
                byte byteValue = Byte.parseByte(value);
                cri.setProperty(type.intValue(), new byte[] {byteValue});

            } catch(NumberFormatException nfex) {

                // ok, that's not a byte, then set it as string
                cri.setProperty(type.intValue(), value);
            }
        }

        return cri;
    }

    /**
     * Get the transaction parameter buffer using the information supplied
     * with connection.
     *
     * @param info instance of {@link Properties} containing the connection
     * parameters.
     *
     * @return a TPB containing all relevant parameters extracted from
     * <code>info</code>
     */
    public static Collection getTpb(Properties info) {
        HashSet tpb = new HashSet();

        Iterator keys = info.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            Integer type = (Integer)tpbTypes.get(key);

            if (type == null) continue;

            tpb.add(type);
        }

        return tpb;
    }

    /**
     * Load mapping between Java and InterBase encodings. This method loads the
     * mapping using the classloader that loaded this class.
     */
    private static void loadEncodings() {
        ClassLoader cl = FBConnectionHelper.class.getClassLoader();

        InputStream in = null;

        // get the stream from the classloader or system classloader
        if (cl == null)
            in = ClassLoader.getSystemResourceAsStream(ISC_ENCODINGS_RESOURCE);
        else
            in = cl.getResourceAsStream(ISC_ENCODINGS_RESOURCE);

        if (in == null) return;

        // load properties
        Properties props = new Properties();
        try {
            props.load(in);
        } catch(IOException ioex) {
            ioex.printStackTrace();
            return;
        } finally {
            try {
                in.close();
            } catch(IOException ex) {
                ex.printStackTrace();
                // do nothing here
            }
        }

        // fill the direct and inversed mappings
        iscEncodings.putAll(props);

        Iterator iterator = props.keySet().iterator();
        while(iterator.hasNext()) {
            String iscEncoding = (String)iterator.next();
            String javaEncoding = (String)props.get(iscEncoding);
            javaEncodings.put(javaEncoding, iscEncoding);
        }

        encodingsLoaded = true;
    }

    /**
     * Get Java language encoding for given InterBase encoding.
     *
     * @param iscEncoding InterBase encoding
     * @return corresponding Java encoding or <code>null</code> if none found.
     */
    public static String getJavaEncoding(String iscEncoding) {
        if (!encodingsLoaded)
            loadEncodings();

        return (String)iscEncodings.get(iscEncoding);
    }

    /**
     * Get InterBase encoding for given Java language encoding.
     *
     * @param javaEncoding Java language encoding.
     * @return corresponding InterBase encoding or <code>null</code> if none
     * found.
     */
    public static String getIscEncoding(String javaEncoding) {
        if (!encodingsLoaded)
            loadEncodings();

        return (String)javaEncodings.get(javaEncoding);
    }

}