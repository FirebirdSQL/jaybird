/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.encoding;

import javax.xml.namespace.QName;

/** This is the base interface for the representation of a type mapping. A
 * TypeMapping implementation class may support one or more encoding styles.
 * 
 * For its supported encoding styles, a TypeMapping instance maintains a set of
 * tuples of the type {Java Class, SerializerFactory, DeserializerFactory,
 *    XML type-QName}. 
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface TypeMapping
{
   public DeserializerFactory getDeserializer(Class javaType, QName xmlType);
   public SerializerFactory getSerializer(Class javaType, QName xmlType);

   public String[] getSupportedEncodings();
   public void setSupportedEncodings(String[] encodingStyleURIs);

   public boolean isRegistered(Class javaType, QName xmlType);
   public void register(Class javaType, QName xmlType, SerializerFactory sf,
      DeserializerFactory dsf); 

   public void removeDeserializer(Class javaType, QName xmlType);
   public void removeSerializer(Class javaType, QName xmlType);
}
