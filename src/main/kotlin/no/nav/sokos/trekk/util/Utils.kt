package no.nav.sokos.trekk.util

import java.time.LocalDate
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

object Utils {
    fun LocalDate.toXMLGregorianCalendar(): XMLGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(this.toString())

    fun XMLGregorianCalendar.toLocalDate(): LocalDate = toGregorianCalendar().toZonedDateTime().toLocalDate()
}
