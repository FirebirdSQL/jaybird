
package org.firebirdsql.encodings.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * Definition of the mapping of a Firebird encoding for use in Jaybird
 * 
 * <p>Java class for EncodingDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EncodingDefinitionType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="firebirdName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="javaName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="characterSetId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="maxBytesPerCharacter" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *       &lt;attribute name="encodingDefinitionImplementation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="firebirdOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EncodingDefinitionType", propOrder = {
    "value"
})
public class EncodingDefinitionType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "firebirdName", required = true)
    protected String firebirdName;
    @XmlAttribute(name = "javaName")
    protected String javaName;
    @XmlAttribute(name = "characterSetId", required = true)
    protected int characterSetId;
    @XmlAttribute(name = "maxBytesPerCharacter")
    protected Integer maxBytesPerCharacter;
    @XmlAttribute(name = "encodingDefinitionImplementation")
    protected String encodingDefinitionImplementation;
    @XmlAttribute(name = "firebirdOnly")
    protected Boolean firebirdOnly;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the firebirdName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirebirdName() {
        return firebirdName;
    }

    /**
     * Sets the value of the firebirdName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirebirdName(String value) {
        this.firebirdName = value;
    }

    /**
     * Gets the value of the javaName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJavaName() {
        return javaName;
    }

    /**
     * Sets the value of the javaName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJavaName(String value) {
        this.javaName = value;
    }

    /**
     * Gets the value of the characterSetId property.
     * 
     */
    public int getCharacterSetId() {
        return characterSetId;
    }

    /**
     * Sets the value of the characterSetId property.
     * 
     */
    public void setCharacterSetId(int value) {
        this.characterSetId = value;
    }

    /**
     * Gets the value of the maxBytesPerCharacter property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getMaxBytesPerCharacter() {
        if (maxBytesPerCharacter == null) {
            return  1;
        } else {
            return maxBytesPerCharacter;
        }
    }

    /**
     * Sets the value of the maxBytesPerCharacter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxBytesPerCharacter(Integer value) {
        this.maxBytesPerCharacter = value;
    }

    /**
     * Gets the value of the encodingDefinitionImplementation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncodingDefinitionImplementation() {
        return encodingDefinitionImplementation;
    }

    /**
     * Sets the value of the encodingDefinitionImplementation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncodingDefinitionImplementation(String value) {
        this.encodingDefinitionImplementation = value;
    }

    /**
     * Gets the value of the firebirdOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFirebirdOnly() {
        if (firebirdOnly == null) {
            return false;
        } else {
            return firebirdOnly;
        }
    }

    /**
     * Sets the value of the firebirdOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFirebirdOnly(Boolean value) {
        this.firebirdOnly = value;
    }

}
