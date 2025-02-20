# sokos-trekk

## Innholdsoversikt

* [Innledning](#innledning)
* [Flyt](#flyt)
* [Database](#database)
* [Integrasjoner mot Arena](#integrasjon-mot-arena)
* [Integrasjoner mot OS](#integrasjon-mot-os)

---

## Innledning

`sokos-trekk` er en java-applikasjon som kjører på NAIS.

Applikasjonens oppgave er å lese inn en liste med trekkvedtak fra Oppdragssystemet og fra ARENA og sammenligne disse.

Ut fra denne sammenligningen gjøres en beslutning på hvor et trekk skal effektueres. Dette kan være OS, Abetal, begge eller ingen.

## Flyt

Flyten i `sokos-trekk` som følger:

````mermaid
flowchart LR
    osout("OS") -- TREKK_INN (MQ) --> trekk("sokos-trekk")
    trekk -- Soap --> arena("Arena")
    arena -- Soap --> trekk
    trekk -- TREKK_REPLY (MQ) --> osinn("OS")
````

* Oppdragssystemet legger en melding på TREKK_INN-kø.
* `sokos-trekk` leser meldingen på kø.
* `sokos-trekk` finner alle unike fødselsnummer i meldingen og genererer en forespørsel til Arena ytelsesvedtak-tjenesten.
* Ytelsesvedtak returnerer en liste med vedtak fra Arena.
* Trekk går gjennom ytelsene mottatt fra Oppdragssystemet og sammenligner med ytelsene i Arena. Det gjøres ut fra dette en besluttning om hvor hvert trekk skal effektueres
* Trekklisten med tilhørende beslutning returneres, via TREKK_REPLY kø, til Oppdragssystemet.

## Database

Ingen avhengigheter til database.

## Integrasjon mot Arena

Trekk kaller YtelseVedtak_v1 webservice (SOAP) hos Arena. Se definisjon av tjenesten: Trekk - Tjenester som konsumeres.
En ytelsesVedtak-request vil inneholde en ident (fødselsnummer), en periode og en temaliste.

Perioden settes slik:

* fom: dagens dato.
* tom: den siste i neste måned ut fra dagens dato.

Temalisten vil alltid inneholde AAP, DAG og IND.

Eksempel på request til Arena:

```xml

<v1:finnYtelseVedtakListe>
    <request>
        <personListe>
            <ident>12345678901</ident>
            <periode>
                <fom>01-01-2018</fom>
                <tom>31-01-2018</tom>
            </periode>
        </personListe>
        <temaListe>AAP</temaListe>
        <temaListe>DAG</temaListe>
        <temaListe>IND</temaListe>
    </request>
</v1:finnYtelseVedtakListe>
```

## Integrasjon mot OS

Trekk leser meldinger fra OS på køen TREKK_INN og skriver meldinger til OS på køen TREKK_REPLY. Disse meldingene vil valideres mot følgende XSD

<details>
<summary>Click to expand source code</summary>

```xml
<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="urn:no:nav:maskinelletrekk:trekk:v1"
            xmlns="urn:no:nav:maskinelletrekk:trekk:v1"
            attributeFormDefault="qualified"
            elementFormDefault="qualified">

    <xsd:element name="trekk">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="typeKjoring" type="TypeKjoring"/>
                <xsd:choice>
                    <xsd:element name="trekkRequest" type="TrekkRequest" maxOccurs="1000"/>
                    <xsd:element name="trekkResponse" type="TrekkResponse" maxOccurs="1000"/>
                </xsd:choice>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

    <xsd:complexType name="TrekkRequest">
        <xsd:sequence>
            <xsd:element name="offnr" type="xsd:string"/>
            <xsd:element name="trekkvedtakId" type="xsd:int"/>
            <xsd:element name="trekkalt" type="Trekkalternativ"/>
            <xsd:element name="system" type="System" minOccurs="0"/>
            <xsd:element name="trekkSats" type="xsd:decimal"/>
            <xsd:element name="totalSatsOS" type="xsd:decimal"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="TrekkResponse">
        <xsd:sequence>
            <xsd:element name="trekkvedtakId" type="xsd:int"/>
            <xsd:element name="beslutning" type="Beslutning"/>
            <xsd:element name="system" type="System" minOccurs="0"/>
            <xsd:element name="totalSatsOS" type="xsd:decimal"/>
            <xsd:element name="totalSatsArena" type="xsd:decimal"/>
            <xsd:element name="vedtak" type="ArenaVedtak" minOccurs="0" maxOccurs="1000"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="ArenaVedtak">
        <xsd:sequence>
            <xsd:element name="vedtaksperiode" type="Periode"/>
            <xsd:element name="tema" type="xsd:string"/>
            <xsd:element name="rettighetType" type="xsd:string"/>
            <xsd:element name="dagsats" type="xsd:decimal" minOccurs="0"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="Periode">
        <xsd:sequence>
            <xsd:element name="fom" type="xsd:date" minOccurs="0"/>
            <xsd:element name="tom" type="xsd:date" minOccurs="0"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:simpleType name="Beslutning">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="OS"/>
            <xsd:enumeration value="ABETAL"/>
            <xsd:enumeration value="BEGGE"/>
            <xsd:enumeration value="INGEN"/>
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="TypeKjoring">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="INNL"/><!-- Innlesing av nytt trekk -->
            <xsd:enumeration value="PERI"/><!-- Periodisk kontroll av trekk -->
            <xsd:enumeration value="REME"/><!-- Returmelding til trekkinnmelder ved ingen ytelse -->
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="System">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="J"/> <!-- Abetal  -->
            <xsd:enumeration value="N"/> <!-- OS      -->
            <xsd:enumeration value="B"/> <!-- Begge   -->
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="Trekkalternativ">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="LOPD"/>
            <xsd:enumeration value="LOPM"/>
            <xsd:enumeration value="LOPP"/>
            <xsd:enumeration value="SALD"/>
            <xsd:enumeration value="SALM"/>
            <xsd:enumeration value="SALP"/>
        </xsd:restriction>
    </xsd:simpleType>
</xsd:schema>
```

</details>

En melding må inneholde en (eller flere) trekkRequests eller en (eller flere) trekkResponse.

En melding kan ikke inneholde både trekkRequest og trekkResponse.

### Forklaring til melding fra OS på TREKK_INN kø:

Meldingen som Oppdragssystemet sender til Trekk-komponenten.

Eksempel på XML på TREKK_INN kø:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<trekk xmlns="urn:no:nav:maskinelletrekk:trekk:v1">
    <typeKjoring>INNL</typeKjoring>
    <!--1 to 1000 repetitions:-->
    <trekkRequest>
        <offnr>12345678901</offnr>
        <trekkvedtakId>3</trekkvedtakId>
        <trekkalt>LOPD</trekkalt>
        <system>J</system>
        <trekkSats>10.0</trekkSats>
        <totalSatsOS>100.00</totalSatsOS>
    </trekkRequest>
    <trekkRequest>
        <offnr>10987654321</offnr>
        <trekkvedtakId>4</trekkvedtakId>
        <trekkalt>SALM</trekkalt>
        <system>N</system>
        <trekkSats>100.0</trekkSats>
        <totalSatsOS>100.00</totalSatsOS>
    </trekkRequest>
</trekk>
```

Forklaring på feltene i XML:

<table>
    <tr>
        <th style="text-align: left" colspan="2">Trekk</th>        
    </tr>
    <tr>
        <td style="vertical-align: top">typeKjoring</td>
        <td>
            Type kjøring i OS med følgende gyldige verdier:<br><br>
            <li><b>INNL</b> - innlesing av nytt trekk.</li>
            <li><b>PERI</b> - Periodisk kontroll av trekk.</li>
            <li><b>REME</b> - Returmelding til trekkinnmelder ved ingen ytelse.</li>
        </td>
    </tr>
    <tr>
        <td style="vertical-align: top">+trekkRequest</td>
        <td>En liste med TrekkRequest fra OS. Må inneholde minst 1, kan maksimalt inneholde 1000.</td>
    </tr>
</table>

<table>
    <tr>
        <th style="text-align: left" colspan="2">TrekkRequest</th>        
    </tr>
    <tr>
        <td>offnr</td>
        <td>Fødselsnummer på bruker.</td>
    </tr>
    <tr>
        <td>trekkvedtakId</td>
        <td>Oppdragssystemets trekkvedtakId.</td>
    </tr>
    <tr>
        <td style="vertical-align: top">system</td>
        <td>
            Hvor har trekket vært effektuert tidligere. Gyldige verdier:<br><br>
            <li>J (effektuert i Abetal)</li>  
            <li>N (effektuert i OS)</li>
            <li>B (Begge (OS og Abetal)</li>
        </td>
    </tr>
    <tr>
        <td>trekkSats</td>
        <td>Sats på trekket. Max beløp det kan trekkes i en måned.</td>
    </tr>
    <tr>
        <td>totalSatsOS</td>
        <td>Totalsats for alle trekkvedtak i OS.</td>
    </tr>
</table>

### Forklaring til melding til OS på TREKK_REPLY kø:

Meldingen som Trekk-komponenten returnerer til OS.

Eksempel på XML på TREKK_REPLY kø

```xml
<?xml version="1.0" encoding="UTF-8"?>
<trekk xmlns="urn:no:nav:maskinelletrekk:trekk:v1">
    <typeKjoring>INNL</typeKjoring>
    <!--1 to 1000 repetitions:-->
    <trekkResponse>
        <trekkvedtakId>3</trekkvedtakId>
        <beslutning>ABETAL</beslutning>
        <system>J</system>
        <totalSatsOS>1199.00</totalSatsOS>
        <totalSatsArena>1200.00</totalSatsArena>
        <!--1 to 1000 repetitions:-->
        <vedtak>
            <vedtaksperiode>
                <fom>2016-08-01</fom>
                <tom>2018-05-01</tom>
            </vedtaksperiode>
            <tema>DAG</tema>
            <rettighetType>DAGO</rettighetType>
            <!--Optional:-->
            <dagsats>1000</dagsats>
        </vedtak>
        <vedtak>
            <vedtaksperiode>
                <fom>2011-08-01</fom>
                <tom>2015-08-01</tom>
            </vedtaksperiode>
            <tema>DAG</tema>
            <rettighetType>LONN</rettighetType>
            <!--Optional:-->
            <dagsats>120</dagsats>
        </vedtak>
    </trekkResponse>
    <trekkResponse>
        <trekkvedtakId>4</trekkvedtakId>
        <beslutning>OS</beslutning>
        <system>J</system>
        <totalSatsOS>42.00</totalSatsOS>
        <totalSatsArena>42.00</totalSatsArena>
        <!--1 to 1000 repetitions:-->
        <vedtak>
            <vedtaksperiode>
                <fom>2002-11-05+01:00</fom>
                <tom>2002-06-24+02:00</tom>
            </vedtaksperiode>
            <tema>AAP</tema>
            <rettighetType>AAP</rettighetType>
            <!--Optional:-->
            <dagsats>42</dagsats>
        </vedtak>
    </trekkResponse>
</trekk>
```