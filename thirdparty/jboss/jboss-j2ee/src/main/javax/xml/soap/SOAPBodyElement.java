/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** A SOAPBodyElement object represents the contents in a SOAPBody object.
 * The SOAPFault interface is a SOAPBodyElement object that has been defined.
 * 
 * A new SOAPBodyElement object can be created and added to a SOAPBody object
 * with the SOAPBody method addBodyElement. In the following line of code, sb
 * is a SOAPBody object, and myName is a Name object. 

    SOAPBodyElement sbe = sb.addBodyElement(myName);
 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPBodyElement
   extends SOAPElement
{
}
