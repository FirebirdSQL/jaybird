/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** SOAPFactory is a factory for creating various objects that exist in the SOAP XML tree.
 *
 * SOAPFactory can be used to create XML fragments that will eventually end up in the SOAP part.
 * These fragments can be inserted as children of the SOAPHeaderElement or SOAPBodyElement or
 * SOAPEnvelope or other SOAPElement objects.
 *
 * SOAPFactory also has methods to create javax.xml.soap.Detail objects as well as java.xml.soap.Name objects.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public abstract class SOAPFactory
{
   private static final String DEFAULT_FACTORY = "org.apache.axis.soap.SOAPFactoryImpl";

   public SOAPFactory()
   {
   }


   /** Creates a new instance of SOAPFactory.
    *
    * @return a new instance of a SOAPFactory
    * @throws SOAPException if there was an error creating the default SOAPFactory
    */
   public static SOAPFactory newInstance() throws SOAPException
   {
      SOAPFactory factory = null;
      String factoryName = null;

      try
      {
         factoryName = System.getProperty(SOAPFactory.class.getName(), DEFAULT_FACTORY);
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class factoryClass = loader.loadClass(factoryName);
         factory = (SOAPFactory)factoryClass.newInstance();
      }
      catch (Throwable t)
      {
         throw new SOAPException("Failed to create SOAPFactory: " + factoryName, t);
      }
      return factory;
   }

   /** Creates a new Detail object which serves as a container for DetailEntry objects.
    *
    *  This factory method creates Detail objects for use in situations where it is not practical to use the SOAPFault abstraction.
    *
    * @return a Detail object
    * @throws SOAPException if there is a SOAP error
    */
   public abstract Detail createDetail() throws SOAPException;

   /** Create a SOAPElement object initialized with the given local name.
    *
    * @param localName a String giving the local name for the new element
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement createElement(String localName) throws SOAPException;

   /** Create a new SOAPElement object with the given local name, prefix and uri.
    *
    * @param localName a String giving the local name for the new element
    * @param prefix the prefix for this SOAPElement
    * @param uri a String giving the URI of the namespace to which the new element belongs
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement createElement(String localName, String prefix, String uri) throws SOAPException;

   /** Create a SOAPElement object initialized with the given Name object.
    *
    * @param name a Name object with the XML name for the new element
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement createElement(Name name) throws SOAPException;

   /** Creates a new Name object initialized with the given local name.
    *
    * This factory method creates Name objects for use in situations where it is not practical to use the
    * SOAPEnvelope abstraction.
    * @return
    * @throws SOAPException
    */
   public abstract Name createName(String localName) throws SOAPException;

   /** Creates a new Name object initialized with the given local name, namespace prefix, and namespace URI.
    *
    *  This factory method creates Name objects for use in situations where it is not practical to use the SOAPEnvelope abstraction.
    * 
    * @param localName a String giving the local name
    * @param prefix a String giving the prefix of the namespace
    * @param uri a String giving the URI of the namespace
    * @return a Name object initialized with the given local name, namespace prefix, and namespace URI
    * @throws SOAPException  if there is a SOAP error
    */
   public abstract Name createName(String localName, String prefix, String uri) throws SOAPException;
}
