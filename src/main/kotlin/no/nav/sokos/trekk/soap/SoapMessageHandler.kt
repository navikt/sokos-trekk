package no.nav.sokos.trekk.soap

import javax.xml.namespace.QName

import jakarta.xml.soap.SOAPElement
import jakarta.xml.soap.SOAPEnvelope
import jakarta.xml.soap.SOAPException
import jakarta.xml.soap.SOAPHeader
import jakarta.xml.ws.ProtocolException
import jakarta.xml.ws.handler.MessageContext
import jakarta.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY
import jakarta.xml.ws.handler.soap.SOAPHandler
import jakarta.xml.ws.handler.soap.SOAPMessageContext
import mu.KotlinLogging
import org.slf4j.MDC

import no.nav.common.log.MDCConstants

private val logger = KotlinLogging.logger { }
private val CALLID_QNAME: QName = QName("uri:no.nav.applikasjonsrammeverk", MDCConstants.MDC_CALL_ID)

class SoapMessageHandler : SOAPHandler<SOAPMessageContext> {
    override fun handleMessage(context: SOAPMessageContext): Boolean {
        val outbound = context[MESSAGE_OUTBOUND_PROPERTY] as Boolean?

        // OUTBOUND processing
        if (context[MESSAGE_OUTBOUND_PROPERTY] as Boolean) {
            val callId =
                MDC.get(MDCConstants.MDC_CALL_ID)
                    ?: throw RuntimeException(
                        "CallId skal være tilgjengelig i MDC på dette tidspunkt. Om du er en webapp, må du legge til et MDCFilter i web.xml " +
                            "(oppskrift på dette: http://confluence.adeo.no/display/Modernisering/MDCFilter). " +
                            "Om du er noe annet må du generere callId selv og legge på MDC. Hjelpemetoder finnes i no.nav.modig.common.MDCOperations.",
                    )
            logger.debug("Add the callId to the SOAP message: $callId")
            try {
                val envelope: SOAPEnvelope = context.message.soapPart.envelope
                val header: SOAPHeader = envelope.header

                val callIdElement: SOAPElement = header.addChildElement(CALLID_QNAME)
                callIdElement.value = callId
            } catch (e: SOAPException) {
                logger.error(e.message)
                throw ProtocolException(e)
            }
        }
        return true
    }

    override fun handleFault(soapMessageContext: SOAPMessageContext): Boolean {
        return true
    }

    override fun close(messageContext: MessageContext) {
    }

    override fun getHeaders(): Set<QName> {
        return object : HashSet<QName>() {
            init {
                add(CALLID_QNAME)
            }
        }
    }
}
