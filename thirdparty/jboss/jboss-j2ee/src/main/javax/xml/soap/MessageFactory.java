/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

import java.io.InputStream;
import java.io.IOException;

/** A factory for creating SOAPMessage objects. 

A SAAJ client can create a MessageFactory object using the method newInstance,
 as shown in the following line of code. 

       MessageFactory mf = MessageFactory.newInstance();
 
 A standalone client (a client that is not running in a container) can use the
 newInstance method to create a MessageFactory object.
 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public abstract class MessageFactory
{
   private static final String DEFAULT_MSG_FACTORY =
      "org.apache.axis.soap.MessageFactoryImpl";

   public MessageFactory()
   {
   }

   /** Creates a new MessageFactory object that is an instance of the default
    * implementation.
    *  
    * @return
    * @throws SOAPException
    */ 
   public static MessageFactory newInstance() throws SOAPException
   {
      MessageFactory factory = null;
      String factoryName = null;

      try
      {
         factoryName = System.getProperty("javax.xml.soap.MessageFactory",
            DEFAULT_MSG_FACTORY);
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class factoryClass = loader.loadClass(factoryName);
         factory = (MessageFactory) factoryClass.newInstance();
      }
      catch(Throwable t)
      {
         throw new SOAPException("Failed to create MessageFactory: "+factoryName, t);
      }
      return factory;
   }

   public abstract SOAPMessage createMessage() throws SOAPException;
   public abstract SOAPMessage createMessage(MimeHeaders headers, InputStream in)
      throws IOException, SOAPException;
}
