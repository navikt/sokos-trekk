package no.nav.sokos.trekk

import java.io.StringReader
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.stream.StreamSource

import jakarta.xml.bind.JAXBContext

import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse

object TestUtil {
    fun String.readFromResource(): String {
        val clazz = {}::class.java.classLoader
        val resource = clazz.getResource(this)
        requireNotNull(resource) { "Resource $this not found." }
        return resource.readText()
    }

    fun String.unmarshalFinnYtelseVedtakListeResponse(): FinnYtelseVedtakListeResponse =
        JAXBContext.newInstance(FinnYtelseVedtakListeResponse::class.java)
            .createUnmarshaller()
            .unmarshal(
                XMLInputFactory.newInstance().createXMLStreamReader(StreamSource(StringReader(this))),
                FinnYtelseVedtakListeResponse::class.java,
            ).value
}
