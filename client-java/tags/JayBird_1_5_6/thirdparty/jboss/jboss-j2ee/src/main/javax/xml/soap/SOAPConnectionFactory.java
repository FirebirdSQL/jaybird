/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** A factory for creating SOAPConnection objects. Implementation of this class
 * is optional. If SOAPConnectionFactory.newInstance() throws an
 * UnsupportedOperationException then the implementation does not support the
 * SAAJ communication infrastructure. Otherwise SOAPConnection objects can be
 * created by calling createConnection() on the newly created
 * SOAPConnectionFactory object.
 *  
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.org
 * @version $Revision$
 */
public abstract class SOAPConnectionFactory
{
   private static final String JBOSS_SOAP_CONN_FACTORY =
           "org.jboss.webservice.soap.SOAPConnectionFactoryImpl";

   private static final String AXIS_SOAP_CONN_FACTORY =
           "org.apache.axis.soap.SOAPConnectionFactoryImpl";

   public SOAPConnectionFactory()
   {
   }

   /** Creates an instance of the default SOAPConnectionFactory object. 
    * 
    * @return
    * @throws SOAPException
    * @throws UnsupportedOperationException
    */
   public static SOAPConnectionFactory newInstance()
           throws SOAPException, UnsupportedOperationException
   {
      SOAPConnectionFactory factory = null;
      String factoryName = null;

      try
      {
         factoryName = System.getProperty("javax.xml.soap.SOAPConnectionFactory", JBOSS_SOAP_CONN_FACTORY);
         ClassLoader loader = Thread.currentThread().getContextClassLoader();

         Class factoryClass = null;
         try
         {
            factoryClass = loader.loadClass(factoryName);
         }
         catch (ClassNotFoundException ignore)
         {
            factoryName = AXIS_SOAP_CONN_FACTORY;
            factoryClass = loader.loadClass(factoryName);
         }

         factory = (SOAPConnectionFactory)factoryClass.newInstance();
      }
      catch (Throwable t)
      {
         throw new SOAPException("Failed to create SOAPConnectionFactory: " + factoryName, t);
      }
      return factory;
   }

   public abstract SOAPConnection createConnection() throws SOAPException;
}
