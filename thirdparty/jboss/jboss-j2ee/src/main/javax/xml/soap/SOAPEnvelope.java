/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** The container for the SOAPHeader and SOAPBody portions of a SOAPPart object. By default, a SOAPMessage object is created with a SOAPPart object that has a SOAPEnvelope object. The SOAPEnvelope object by default has an empty SOAPBody object and an empty SOAPHeader object. The SOAPBody object is required, and the SOAPHeader object, though optional, is used in the majority of cases. If the SOAPHeader object is not needed, it can be deleted, which is shown later. 
A client can access the SOAPHeader and SOAPBody objects by calling the methods SOAPEnvelope.getHeader and SOAPEnvelope.getBody. The following lines of code use these two methods after starting with the SOAPMessage object message to get the SOAPPart object sp, which is then used to get the SOAPEnvelope object se. 
     SOAPPart sp = message.getSOAPPart();
     SOAPEnvelope se = sp.getEnvelope();
     SOAPHeader sh = se.getHeader();
     SOAPBody sb = se.getBody();
 
It is possible to change the body or header of a SOAPEnvelope object by retrieving the current one, deleting it, and then adding a new body or header. The javax.xml.soap.Node method deleteNode deletes the XML element (node) on which it is called. For example, the following line of code deletes the SOAPBody object that is retrieved by the method getBody. 
      se.getBody().detachNode();
 
To create a SOAPHeader object to replace the one that was removed, a client uses the method SOAPEnvelope.addHeader, which creates a new header and adds it to the SOAPEnvelope object. Similarly, the method addBody creates a new SOAPBody object and adds it to the SOAPEnvelope object. The following code fragment retrieves the current header, removes it, and adds a new one. Then it retrieves the current body, removes it, and adds a new one. 
     SOAPPart sp = message.getSOAPPart();
     SOAPEnvelope se = sp.getEnvelope();
     se.getHeader().detachNode();
     SOAPHeader sh = se.addHeader();
     se.getBody().detachNode();
     SOAPBody sb = se.addBody();
 
It is an error to add a SOAPBody or SOAPHeader object if one already exists. 
The SOAPEnvelope interface provides three methods for creating Name objects. One method creates Name objects with a local name, a namespace prefix, and a namesapce URI. The second method creates Name objects with a local name and a namespace prefix, and the third creates Name objects with just a local name. The following line of code, in which se is a SOAPEnvelope object, creates a new Name object with all three. 
     Name name = se.createName("GetLastTradePrice", "WOMBAT",
                                "http://www.wombat.org/trader");

 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPEnvelope
   extends SOAPElement
{
   public abstract SOAPBody addBody() throws SOAPException;
   public abstract SOAPHeader addHeader() throws SOAPException;
   public abstract Name createName(String localName) throws SOAPException;
   public abstract Name createName(String localName, String prefix, String uri)
      throws SOAPException;
   public abstract SOAPBody getBody() throws SOAPException;
   public abstract SOAPHeader getHeader() throws SOAPException;

}
