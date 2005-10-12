/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.encoding;

import java.io.Serializable;

/** This interface defines the base interface for serializers. A Serializer
 * converts a Java object to an XML representation using a specific XML
 * processing mechanism and based on the specified type mapping and encoding
 * style.
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface Serializer
   extends Serializable
{
   public String getMechanismType();
}
