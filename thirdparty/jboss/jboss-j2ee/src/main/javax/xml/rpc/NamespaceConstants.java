/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc;

/** Constants used in JAX-RPC for namespace prefixes and URIs
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class NamespaceConstants
{
   public static final String NSPREFIX_SCHEMA_XSD = "xsd"; 
    public static final String NSPREFIX_SCHEMA_XSI = "xsi"; 
    public static final String NSPREFIX_SOAP_ENCODING = "soapenc";
    public static final String NSPREFIX_SOAP_ENVELOPE = "soapenv";
    public static final String NSURI_SCHEMA_XSD =
       "http://www.w3.org/2001/XMLSchema"; 
    public static final String NSURI_SCHEMA_XSI =
       "http://www.w3.org/2001/XMLSchema-instance"; 
    public static final String NSURI_SOAP_ENCODING =
       "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String NSURI_SOAP_ENVELOPE =
       "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String NSURI_SOAP_NEXT_ACTOR =
       "http://schemas.xmlsoap.org/soap/actor/next";
}
