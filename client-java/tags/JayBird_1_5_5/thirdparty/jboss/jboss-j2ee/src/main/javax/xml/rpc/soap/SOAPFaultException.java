/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.soap;

import javax.xml.soap.Detail;
import javax.xml.namespace.QName;

/** The SOAPFaultException exception represents a SOAP fault.
 * 
 * The message part in the SOAP fault maps to the contents of faultdetail
 * element accessible through the getDetail method on the SOAPFaultException.
 * The method createDetail on the javax.xml.soap.SOAPFactory creates an instance
 * of the javax.xml.soap.Detail.
 * 
 * The faultstring provides a human-readable description of the SOAP fault. The
 * faultcode element provides an algorithmic mapping of the SOAP fault.
 *  
 * Refer to SOAP 1.1 and WSDL 1.1 specifications for more details of the SOAP
 * faults. 
 * 
 * @author Scott.Stark@jboss.org
 * @author Rahul Sharma (javadoc)
 * @version $Revision$
 */
public class SOAPFaultException extends RuntimeException
{
   private QName faultCode;
   private String faultString;
   private String faultActor;
   private Detail faultDetail;

   public SOAPFaultException(QName faultCode, String faultString,
      String faultActor, Detail faultDetail)
   {
      this.faultCode = faultCode;
      this.faultString = faultString;
      this.faultActor = faultActor;
      this.faultDetail = faultDetail;
   }

   public String getFaultActor()
   {
      return faultActor;
   }
   public QName getFaultCode()
   {
      return faultCode;
   }
   public Detail getDetail()
   {
      return faultDetail;
   }
   public String getFaultString()
   {
      return faultString;
   }
}
