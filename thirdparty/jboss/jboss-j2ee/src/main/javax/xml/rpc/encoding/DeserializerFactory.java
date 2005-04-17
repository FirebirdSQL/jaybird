/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.encoding;

import java.io.Serializable;
import java.util.Iterator;

/** A factory of deserializers. A DeserializerFactory is registered with a
 * TypeMapping instance as part of the TypeMappingRegistry.
 *
 * @see Deserializer
 * @see TypeMapping 
 * @see TypeMappingRegistry
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface DeserializerFactory
   extends Serializable
{
   public Deserializer getDeserializerAs(String mechanismType);
   public Iterator getSupportedMechanismTypes();
}
