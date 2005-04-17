/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.encoding;

import javax.xml.namespace.QName;

/** Constants for common XML Schema and SOAP 1.1 types.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class XMLType
{
   public static final QName SOAP_ARRAY = QName.valueOf("{SOAP-ENC}Array"); 
   public static final QName SOAP_BASE64 = QName.valueOf("{SOAP-ENC}base64");
   public static final QName SOAP_BOOLEAN = QName.valueOf("{SOAP-ENC}boolean");
   public static final QName SOAP_BYTE = QName.valueOf("{SOAP-ENC}byte");
   public static final QName SOAP_DOUBLE = QName.valueOf("{SOAP-ENC}double");
   public static final QName SOAP_FLOAT = QName.valueOf("{SOAP-ENC}float");
   public static final QName SOAP_INT = QName.valueOf("{SOAP-ENC}int");
   public static final QName SOAP_LONG = QName.valueOf("{SOAP-ENC}long");
   public static final QName SOAP_SHORT = QName.valueOf("{SOAP-ENC}short");
   public static final QName SOAP_STRING = QName.valueOf("{SOAP-ENC}string");
   public static final QName XSD_BASE64 = QName.valueOf("{SOAP-ENC}base64Binary");
   public static final QName XSD_BOOLEAN = QName.valueOf("{SOAP-ENC}boolean");
   public static final QName XSD_BYTE = QName.valueOf("{xsd}byte");
   public static final QName XSD_DATETIME = QName.valueOf("{xsd}dateTime");
   public static final QName XSD_DECIMAL = QName.valueOf("{xsd}decimal");
   public static final QName XSD_DOUBLE = QName.valueOf("{xsd}double");
   public static final QName XSD_FLOAT = QName.valueOf("{xsd}float");
   public static final QName XSD_HEXBINARY = QName.valueOf("{xsd}hexBinary");
   public static final QName XSD_INT = QName.valueOf("{xsd}int");
   public static final QName XSD_INTEGER = QName.valueOf("{xsd}integer");
   public static final QName XSD_LONG = QName.valueOf("{xsd}long");
   public static final QName XSD_QNAME = QName.valueOf("{xsd}QName");
   public static final QName XSD_SHORT = QName.valueOf("{xsd}short");
   public static final QName XSD_STRING = QName.valueOf("{xsd}string");
}
