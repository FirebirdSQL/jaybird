/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.handler;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;

/** This represents information about a handler in the HandlerChain. A
 * HandlerInfo instance is passed in the Handler.init method to initialize a
 * Handler instance.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class HandlerInfo
   implements Serializable
{
   private static final long serialVersionUID = -6735139577513563610L;

   private Class handlerClass;
   private Map configMap = new HashMap();
   private QName[] headers;

   /** Default constructor */
   public HandlerInfo()
   {
   }

   /** Constructor for HandlerInfo
    *
    * @param handlerClass Java Class for the Handler
    * @param config Handler Configuration as a java.util.Map
    * @param headers QNames for the header blocks processed by this Handler.
    * QName is the qualified name of the outermost element of a header block
    */
   public HandlerInfo(Class handlerClass, Map config, QName[] headers)
   {
      this.handlerClass = handlerClass;
      this.configMap = config;
      this.headers = headers;
   }

   /** Gets the Handler class
    *
    * @return Returns null if no Handler class has been set; otherwise the set handler class
    */
   public Class getHandlerClass()
   {
      return handlerClass;
   }

   /** Sets the Handler class
    *
    * @param handlerClass Class for the Handler
    */
   public void setHandlerClass(Class handlerClass)
   {
      this.handlerClass = handlerClass;
   }

   /** Gets the Handler configuration
    *
    * @return Returns empty Map if no configuration map has been set; otherwise returns the set configuration map
    */
   public Map getHandlerConfig()
   {
      return new HashMap(configMap);
   }

   /** Sets the Handler configuration as java.util.Map
    *
    * @param config Configuration map
    */
   public void setHandlerConfig(Map config)
   {
      configMap.clear();
      configMap.putAll(config);
   }

   /** Gets the header blocks processed by this Handler.
    *
    * @return Array of QNames for the header blocks. Returns null if no header blocks have been set using the setHeaders method.
    */
   public QName[] getHeaders()
   {
      return headers;
   }

   /** Sets the header blocks processed by this Handler.
    *
    * @param qnames QNames of the header blocks. QName is the qualified name of the outermost element of the SOAP header block
    */
   public void setHeaders(QName[] qnames)
   {
      headers = qnames;
   }

   /** Returns a string representation of the object.
    */
   public String toString()
   {
      List hlist = (headers != null ? Arrays.asList(headers) : null);
      return "[class=" + handlerClass.getName() + ",headers=" + hlist + ",config=" + configMap + "]";
   }
}
