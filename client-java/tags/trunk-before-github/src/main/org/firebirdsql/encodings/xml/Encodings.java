
package org.firebirdsql.encodings.xml;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="encodingDefinition" type="{http://www.firebirdsql.org/schemas/Jaybird/encodings/1}EncodingDefinitionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "encodingDefinition"
})
@XmlRootElement(name = "encodings")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-02-07T01:31:12+01:00", comments = "JAXB RI v2.2.4-2")
public class Encodings {

    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-02-07T01:31:12+01:00", comments = "JAXB RI v2.2.4-2")
    protected List<EncodingDefinitionType> encodingDefinition;

    /**
     * Gets the value of the encodingDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the encodingDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEncodingDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EncodingDefinitionType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-02-07T01:31:12+01:00", comments = "JAXB RI v2.2.4-2")
    public List<EncodingDefinitionType> getEncodingDefinition() {
        if (encodingDefinition == null) {
            encodingDefinition = new ArrayList<EncodingDefinitionType>();
        }
        return this.encodingDefinition;
    }

}
