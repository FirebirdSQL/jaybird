/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** A representation of the contents in a SOAPFault object. The Detail
 * interface is a SOAPFaultElement. 
 * 
 * Content is added to a SOAPFaultElement using the SOAPElement method
 * addTextNode.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPFaultElement
   extends SOAPElement
{
}
