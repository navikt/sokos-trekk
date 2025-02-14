package no.nav.sokos.trekk.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

import no.nav.maskinelletrekk.trekk.v1.TrekkResponse

@Serializable
data class TrekkResponse(val trekkResponse: List<@Contextual TrekkResponse>)
