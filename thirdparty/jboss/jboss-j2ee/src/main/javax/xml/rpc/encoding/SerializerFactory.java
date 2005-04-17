/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.encoding;

import java.io.Serializable;
import java.util.Iterator;

/** This is a factory of the serializers. A SerializerFactory is registered
 * with a TypeMapping object as part of the TypeMappingRegistry.
 * 
 * @see Serializer
 * @see TypeMapping 
 * @see TypeMappingRegistry
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SerializerFactory
   extends Serializable
{
   public Serializer getSerializerAs(String mechanismType);
   public Iterator getSupportedMechanismTypes();
}
