/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc;

import java.net.URL;
import java.util.Properties;
import java.security.PrivilegedAction;
import java.security.AccessController;
import javax.xml.namespace.QName;

/** The javax.xml.rpc.ServiceFactory is an abstract class that provides a
 * factory for the creation of instances of the type javax.xml.rpc.Service.
 * This abstract class follows the abstract static factory design pattern.
 * This enables a J2SE based client to create a Service instance in a portable
 * manner without using the constructor of the Service implementation class.
 * 
 * The ServiceFactory implementation class is set using the
 * javax.xml.rpc.ServiceFactory System property.
 *
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.org
 * @version 1.1
 */
public abstract class ServiceFactory
{
   private static ServiceFactory factory;

   /** A constant representing the property used to lookup the name of a ServiceFactory implementation class. */
   public static final String SERVICEFACTORY_PROPERTY = "javax.xml.rpc.ServiceFactory";

   private static final String JBOSS_SERVICE_FACTORY = "org.jboss.webservice.client.ServiceFactoryImpl";
   private static final String AXIS_SERVICE_FACTORY = "org.apache.axis.client.ServiceFactory";

   protected ServiceFactory()
   {
   }

   /** Gets an instance of the ServiceFactory
    * Only one copy of a factory exists and is returned to the application each time this method is called.
    *
    * The implementation class to be used can be overridden by setting the javax.xml.rpc.ServiceFactory system property.
    *
    * @return The ServiceFactory singleton
    * @throws ServiceException on failure to instantiate the ServiceFactory impl
    */
   public static synchronized ServiceFactory newInstance()
           throws ServiceException
   {
      // Create the factory singleton if needed
      if (factory == null)
      {
         PrivilegedAction action = new PropertyAccessAction(SERVICEFACTORY_PROPERTY, JBOSS_SERVICE_FACTORY);
         String factoryName = (String)AccessController.doPrivileged(action);


         ClassLoader loader = Thread.currentThread().getContextClassLoader();

         try
         {
            Class factoryClass = null;
            try
            {
               factoryClass = loader.loadClass(factoryName);
               factory = (ServiceFactory)factoryClass.newInstance();
            }
            catch (ClassNotFoundException ignore)
            {
               factoryName = AXIS_SERVICE_FACTORY;
               factoryClass = loader.loadClass(factoryName);
               factory = (ServiceFactory)factoryClass.newInstance();
            }
         }
         catch (Throwable e)
         {
            throw new ServiceException("Failed to create factory: " + factoryName, e);
         }
      }

      return factory;
   }

   /** Create an instance of the generated service implementation class for a given service interface, if available.
    * @param serviceInterface Service interface
    * @return A Service
    * @throws ServiceException If there is any error while creating the specified service, including the case where a
    * generated service implementation class cannot be located
    */
   public abstract Service loadService(Class serviceInterface)
           throws ServiceException;

   /**
    * Create an instance of the generated service implementation class for a given service interface, if available.
    * An implementation may use the provided wsdlDocumentLocation and properties to help locate the generated implementation class.
    * If no such class is present, a ServiceException will be thrown.
    *
    * @param wsdlDocumentLocation URL for the WSDL document location for the service or null
    * @param serviceInterface Service interface
    * @param props A set of implementation-specific properties to help locate the generated service implementation class
    * @return A Service
    * @throws ServiceException If there is any error while creating the specified service, including the case where a
    * generated service implementation class cannot be located
    */
   public abstract Service loadService(URL wsdlDocumentLocation, Class serviceInterface, Properties props)
           throws ServiceException;

   /**
    * Create an instance of the generated service implementation class for a given service, if available.
    * The service is uniquely identified by the wsdlDocumentLocation and serviceName arguments.
    * An implementation may use the provided properties to help locate the generated implementation class.
    * If no such class is present, a ServiceException will be thrown.
    *
    * @param wsdlDocumentLocation URL for the WSDL document location for the service or null
    * @param serviceName Qualified name for the service
    * @param props A set of implementation-specific properties to help locate the generated service implementation class
    * @return A Service
    * @throws ServiceException If there is any error while creating the specified service, including the case where a generated service implementation class cannot be located
    */
   public abstract Service loadService(URL wsdlDocumentLocation, QName serviceName, Properties props)
           throws ServiceException;

   /**
    * Create a <code>Service</code> instance.
    *
    * @param   serviceName QName for the service
    * @return  Service.
    * @throws  ServiceException If any error in creation of the specified service
    */
   public abstract Service createService(QName serviceName)
           throws ServiceException;

   /**
    * Create a <code>Service</code> instance.
    *
    * @param   wsdlDocumentLocation URL for the WSDL document location
    * @param   serviceName  QName for the service.
    * @return  Service.
    * @throws  ServiceException If any error in creation of the
    *                specified service
    */
   public abstract Service createService(URL wsdlDocumentLocation, QName serviceName)
           throws ServiceException;

   private static class PropertyAccessAction implements PrivilegedAction
   {
      private String name;
      private String defaultValue;

      PropertyAccessAction(String name, String defaultValue)
      {
         this.name = name;
         this.defaultValue = defaultValue;
      }

      public Object run()
      {
         return System.getProperty(name, defaultValue);
      }
   }
}
