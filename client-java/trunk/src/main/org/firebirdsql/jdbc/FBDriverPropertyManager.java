package org.firebirdsql.jdbc;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.firebirdsql.encodings.EncodingFactory;


/**
 * Manager of the DPB properties.
 */
public class FBDriverPropertyManager {

    private static final String RES = "driver_property_info";
    
    private static ResourceBundle info;
    static {
        try {
            info = ResourceBundle.getBundle(RES);
        } catch(MissingResourceException ex) {
            info = null;
        }
    }

    /**
     * Container class for the driver properties.
     */
    private static class PropertyInfo {
        private String alias;
        private String dpbName;
        private Integer dpbKey;
        private String description;
        
        private int hashCode;
        
        public PropertyInfo(String alias, String dpbName, Integer dpbKey,
                String description) {
            this.alias = alias;
            this.dpbName = dpbName;
            this.dpbKey = dpbKey;
            this.description = description;
            
            hashCode = 17;
            if (alias != null)
                hashCode ^= alias.hashCode();
            
            hashCode ^= dpbName.hashCode();
            hashCode ^= dpbKey.intValue();
        }
        
        public int hashCode() {
            return hashCode;
        }
        
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof PropertyInfo)) return false;
            
            PropertyInfo that = (PropertyInfo)obj;
            
            boolean result = true;
            
            result &= this.alias.equals(that.alias);
            result &= this.dpbName.equals(that.dpbName);
            result &= this.dpbKey.equals(that.dpbKey);
            
            return result;
        }
    }
    
    private static HashMap aliases = new HashMap();
    private static HashMap dpbMap = new HashMap();
    private static HashMap reversedDpbMap = new HashMap();

    static {
        // process aliases and descriptions first
        if (info != null) {
            for (Enumeration en = info.getKeys(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                String value = info.getString(key);
                
                int hashIndex = value.indexOf('#');
                
                String dpbName;
                String description = "";
                if (hashIndex != -1) {
                    dpbName = value.substring(0, hashIndex).trim();
                    description = value.substring(hashIndex + 1).trim();
                } else
                    dpbName = value.trim();
                
                // skip incorrect mappings
                if (!dpbName.startsWith(FBConnectionHelper.DPB_PREFIX))
                    continue;
                
                Integer dpbKey = FBConnectionHelper.getDpbKey(dpbName);
                
                // skip unknown elements
                if (dpbKey == null)
                    continue;
                
                PropertyInfo propInfo = new PropertyInfo(key, dpbName, 
                    dpbKey, description);
                
                aliases.put(propInfo.alias, propInfo);
                dpbMap.put(propInfo.dpbName, propInfo);
                reversedDpbMap.put(dpbKey, propInfo);
            }
        }
        
        // fill rest of the properties
        Map tempDpbMap = FBConnectionHelper.getDpbMap();
        for (Iterator iter = tempDpbMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String dpbName = (String)entry.getKey();
            Integer dpbKey = (Integer)entry.getValue();
            
            if (!dpbName.startsWith(FBConnectionHelper.DPB_PREFIX))
                continue;

            if (dpbMap.containsKey(dpbName))
                continue;

            PropertyInfo propInfo = new PropertyInfo(null, dpbName, dpbKey, "");
            
            dpbMap.put(dpbName, propInfo);
            reversedDpbMap.put(dpbKey, propInfo);
        }
    }
    
    /**
     * Normalize the properties. This method resolves the aliases to their
     * original names. Also it restores the short syntax for the DPB parameters.
     * 
     * @param props instance of {@link Properties} containing original properties.
     * 
     * @return instance of {@link Properties} containing the normalized ones.
     * 
     * @throws SQLException if original properties reference the same DPB 
     * parameter using both alias and original name.
     */
    public static Properties normalize(String url, Properties props) throws SQLException {
        
        Properties tempProps = new Properties();
        tempProps.putAll(props);
        
        convertUrlParams(url, tempProps);
        
        Properties result = new Properties();
        
        for (Iterator iter = tempProps.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String propName = (String)entry.getKey();
            String propValue = (String)entry.getValue();
            
            PropertyInfo propInfo = (PropertyInfo)aliases.get(propName);
            
            // check if alias is not used together with original property
            if (propInfo != null) {
                String originalName = propInfo.dpbName;
                String shortName = propInfo.dpbName.substring(
                    FBConnectionHelper.DPB_PREFIX.length());
                
                boolean hasDuplicate = tempProps.keySet().contains(originalName)
                        || tempProps.keySet().contains(shortName);
                
                if (hasDuplicate)
                    throw new FBSQLException("Specified properties contain " +
                            "reference to a DPB parameter under original and " +
                            "alias names: original name " + propInfo.dpbName + 
                            ", alias : " + propInfo.alias);
            }
            
            // if the specified property is not an alias, check
            // the full list
            if (propInfo == null) {
                String tempKey = propName;
                if (!tempKey.startsWith(FBConnectionHelper.DPB_PREFIX))
                    tempKey = FBConnectionHelper.DPB_PREFIX + tempKey;
                
                propInfo = (PropertyInfo)dpbMap.get(tempKey);
            }
            
            // skip the element if nothing if found
            if (propInfo == null)
                continue;
            
            result.put(propInfo.dpbName, propValue);
        }
        
        handleEncodings(result);
        
        return result;
    }
    
    /**
     * Extract properties specified as URL parameter into the specified list
     * of properties.
     * 
     * @param url specified URL.
     * 
     * @param info instance of {@link Properties} into which values should
     * be extracted.
     */
    private static void convertUrlParams(String url, Properties info) {
        if (url == null)
            return;
        
        int iQuestionMark = url.indexOf("?");

        if (iQuestionMark == -1) 
            return;

        String propString = url.substring(iQuestionMark+1);
        
        StringTokenizer st = new StringTokenizer(propString,"&");
        while(st.hasMoreTokens()) {
            String propertyString = st.nextToken();
            int iIs = propertyString.indexOf("=");
            if(iIs > -1) {
                String property = propertyString.substring(0, iIs);
                String value = propertyString.substring(iIs+1);
                info.setProperty(property,value);
            } else {
                info.setProperty(propertyString, "");
            }
        }
    }
    
    /**
     * Handle character encoding parameters. This method ensures that both
     * java encoding an client connection encodings are correctly set. 
     * Additionally method handles the character translation stuff.
     * 
     * @param info connection properties
     * @param cri mapping connection request info.
     * 
     * @throws SQLException if both isc_dpb_local_encoding and charSet are
     * specified.
     */
    private static void handleEncodings(Properties info) throws SQLException {
        String iscEncoding = info.getProperty("isc_dpb_lc_ctype");
        String localEncoding = info.getProperty("isc_dpb_local_encoding");
        
        if (iscEncoding != null && localEncoding == null) {
            String javaEncoding = FBConnectionHelper.getJavaEncoding(iscEncoding);
            
            if (javaEncoding != null)
                info.setProperty("isc_dpb_local_encoding", javaEncoding);
        }
        
        if (iscEncoding == null && localEncoding != null) {
            iscEncoding = FBConnectionHelper.getIscEncoding(localEncoding); 
            info.setProperty("isc_dpb_lc_ctype", iscEncoding);
        }
        
        // ensure that we fail before any connection is obtained
        // in case when incorrect mapping path is specified 
        // (note, EncodingFactory.getEncoding(String, String) throws exception)
        String mappingPath = info.getProperty("isc_dpb_mapping_path");
        if (mappingPath != null) {
            EncodingFactory.getEncoding(localEncoding, mappingPath);
        }
    }
    
    /**
     * Get property information for the specified properties.
     * 
     * @param props instance of {@link Properties}.
     * 
     * @return array of {@link DriverPropertyInfo} instances.
     */
    public static DriverPropertyInfo[] getDriverPropertyInfo(Properties props) {
        ArrayList result = new ArrayList();
       
        for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String propName = (String)entry.getKey();
            String propValue = (String)entry.getValue();
            
            PropertyInfo propInfo = (PropertyInfo)aliases.get(propName);
            
            // if the specified property is not an alias, check
            // the full list
            if (propInfo == null) {
                String tempKey = propName;
                if (!tempKey.startsWith(FBConnectionHelper.DPB_PREFIX))
                    tempKey = FBConnectionHelper.DPB_PREFIX + tempKey;
                
                propInfo = (PropertyInfo)dpbMap.get(tempKey);
            }
            
            DriverPropertyInfo driverPropInfo = 
                new DriverPropertyInfo(propName, propValue);

            if (propInfo != null)
                driverPropInfo.description = propInfo.description;
            
            result.add(driverPropInfo);
        
        }
        
        return (DriverPropertyInfo[])result.toArray(
            new DriverPropertyInfo[result.size()]);
    }
}
