package no.nav.sokos.trekk.soap

import javax.xml.namespace.QName

import mu.KotlinLogging
import org.apache.cxf.binding.soap.SoapHeader
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.jaxb.JAXBDataBinding
import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase

private val logger = KotlinLogging.logger {}

class CallIdInterceptor(
    private val callIdGenerator: () -> String,
) : AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM) {
    @Throws(Fault::class)
    override fun handleMessage(message: Message) {
        if (message is SoapMessage) {
            runCatching {
                val qName = QName("uri:no.nav.applikasjonsrammeverk", "callId")
                val header = SoapHeader(qName, callIdGenerator(), JAXBDataBinding(String::class.java))
                message.headers.add(header)
            }.onFailure { exception ->
                logger.warn(exception) { "Error while setting CallId header" }
            }
        }
    }
}
