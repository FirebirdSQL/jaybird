/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.encoding;

import java.io.Serializable;

/** This defines a registry of TypeMapping instances for encoding styles.
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface TypeMappingRegistry
   extends Serializable
{
   public void clear();

   public TypeMapping getDefaultTypeMapping();
   public void registerDefault(TypeMapping mapping);

   public TypeMapping createTypeMapping();
   public TypeMapping getTypeMapping(String encodingStyleURI);

   public String[] getRegisteredEncodingStyleURIs();
   public TypeMapping register(String encodingStyleURI, TypeMapping mapping);
   public TypeMapping unregisterTypeMapping(String encodingStyleURI);

   public boolean removeTypeMapping(TypeMapping mapping);
}
