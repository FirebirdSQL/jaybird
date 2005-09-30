package org.firebirdsql.util;

import java.io.FileOutputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.jdbc.FirebirdConnection;


/**
 * 
 */
public class MessageDump {

    private static final int ISC_CODE = 0x14000000;
    
    private static FirebirdConnection getConnection(String database) throws Exception {
        Class.forName(FBDriver.class.getName());
        String url = "jdbc:firebirdsql:" + database;
        return (FirebirdConnection)DriverManager.getConnection(url, "SYSDBA", "masterkey");
    }
    
    private static int getErrorCode(int code, int number) {
        return ISC_CODE | ((code & 0x1F) << 16) | (number & 0x3FFF);
    }
    
    private static String extractMessage(String fbMessage) {
        char[] chars = fbMessage.toCharArray();
        
        StringBuffer sb = new StringBuffer();
        int counter = 0;
        
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
                i++;
                
                if (chars[i] == 's') {
                    sb.append('{').append(counter++).append('}');
                } else
                if (chars[i] == 'd') {
                    sb.append('{').append(counter++).append('}');
                } else
                if (chars[i] == 'l') {
                    i++;
                    if (chars[i] == 'd') {
                        sb.append('{').append(counter++).append('}');
                    } else
                        sb.append("%ld");
                } else
                    sb.append('%').append(chars[i]);
            } else
                sb.append(chars[i]);
        }
        
        return sb.toString();
    }
    
    private static Properties extractProperties(FirebirdConnection connection) throws Exception {
        Properties result = new Properties();
        
        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT fac_code, number, text FROM messages");
            
            while(rs.next()) {
                int code = rs.getInt(1);
                int number = rs.getInt(2);
                String message = rs.getString(3);
                
                result.setProperty(
                    Integer.toString(getErrorCode(code, number)), 
                    extractMessage(message)
                );
            }
            
        } finally {
            stmt.close();
        }
        
        return result;
    }

    public static void main(String[] args) throws Exception {
        
        if (args.length == 0)
            args = new String[]{"localhost:c:/database/msg.fdb"};
        
        FirebirdConnection connection = getConnection(args[0]);
        try {
            Properties props = extractProperties(connection);
            
            props.store(new FileOutputStream("c:/projects/client-java.head/src/resources/error.properties"), "");
        } finally {
            connection.close();
        }
    }
}
