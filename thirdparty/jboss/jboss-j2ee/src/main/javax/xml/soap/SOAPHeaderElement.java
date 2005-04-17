/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** An object representing the contents in the SOAP header part of the SOAP
 * envelope. The immediate children of a SOAPHeader object can be represented
 * only as SOAPHeaderElement objects.
 * 
 * A SOAPHeaderElement object can have other SOAPElement objects as its children. 

 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPHeaderElement
   extends SOAPElement
{
	public String getActor();
	public boolean getMustUnderstand();
	public void setActor(String actorURI);
	public void setMustUnderstand(boolean mustUnderstand);
}
