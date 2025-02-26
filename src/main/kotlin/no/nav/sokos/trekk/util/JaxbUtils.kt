package no.nav.sokos.trekk.util

import java.io.StringReader
import java.io.StringWriter
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.stream.StreamSource

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.Marshaller

import no.nav.maskinelletrekk.trekk.v1.Trekk

object JaxbUtils {
    private val jaxbContextTrekk = JAXBContext.newInstance(Trekk::class.java)

    fun marshalTrekk(trekk: Trekk): String {
        val marshaller =
            jaxbContextTrekk.createMarshaller().apply {
                setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
            }
        return StringWriter().use {
            marshaller.marshal(trekk, it)
            it.toString()
        }
    }

    fun unmarshalTrekk(xmlElement: String): Trekk =
        jaxbContextTrekk
            .createUnmarshaller()
            .unmarshal(
                XMLInputFactory.newInstance().createXMLStreamReader(StreamSource(StringReader(xmlElement))),
                Trekk::class.java,
            ).value

    inline fun <reified T> T.toXml(namespace: String): String {
        val jaxbContext = JAXBContext.newInstance(T::class.java)
        val marshaller =
            jaxbContext.createMarshaller().apply {
                setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
            }

        val rootElementName = T::class.java.simpleName
        val qName = QName(namespace, rootElementName)
        val jaxbElement = JAXBElement(qName, T::class.java, this)

        return StringWriter().use {
            marshaller.marshal(jaxbElement, it)
            it.toString()
        }
    }
}
