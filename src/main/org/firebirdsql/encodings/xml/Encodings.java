
package org.firebirdsql.encodings.xml;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


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
 *         &lt;element name="encodingDefinition" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="firebirdName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="javaName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="characterSetId" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="maxBytesPerCharacter" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *                 &lt;attribute name="encodingDefinitionImplementation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="firebirdOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
public class Encodings {

    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<Encodings.EncodingDefinition> encodingDefinition;

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
     * {@link Encodings.EncodingDefinition }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
    public List<Encodings.EncodingDefinition> getEncodingDefinition() {
        if (encodingDefinition == null) {
            encodingDefinition = new ArrayList<Encodings.EncodingDefinition>();
        }
        return this.encodingDefinition;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
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
    @XmlType(name = "", propOrder = {
        "value"
    })
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
    public static class EncodingDefinition {

        @XmlValue
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        protected String value;
        @XmlAttribute(name = "firebirdName", required = true)
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        protected String firebirdName;
        @XmlAttribute(name = "javaName")
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        protected String javaName;
        @XmlAttribute(name = "characterSetId", required = true)
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        protected int characterSetId;
        @XmlAttribute(name = "maxBytesPerCharacter")
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        protected Integer maxBytesPerCharacter;
        @XmlAttribute(name = "encodingDefinitionImplementation")
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        protected String encodingDefinitionImplementation;
        @XmlAttribute(name = "firebirdOnly")
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        protected Boolean firebirdOnly;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        public void setJavaName(String value) {
            this.javaName = value;
        }

        /**
         * Gets the value of the characterSetId property.
         * 
         */
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        public int getCharacterSetId() {
            return characterSetId;
        }

        /**
         * Sets the value of the characterSetId property.
         * 
         */
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
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
        @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-07-12T10:45:51+02:00", comments = "JAXB RI v2.2.4-2")
        public void setFirebirdOnly(Boolean value) {
            this.firebirdOnly = value;
        }

    }

}
