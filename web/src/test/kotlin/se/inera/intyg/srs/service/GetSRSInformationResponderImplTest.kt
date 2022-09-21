package se.inera.intyg.srs.service

import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test

class GetSRSInformationResponderImplTest {

    // The entire set of care unit's postal codes (according to 1177.se) as of 2022-09-07
    val regionPostalCodes = listOf(
        Pair("63188", MITT),
        Pair("631 88", MITT),
        Pair("63231",	MITT),
        Pair("64122", MITT),
        Pair("64136", MITT),
        Pair("64122", MITT),
        Pair("64133", MITT),
        Pair("61131", MITT),
        Pair("64723", MITT),
        Pair("64731", MITT),
        Pair("61933", MITT),
        Pair("61185", MITT),
        Pair("61160", MITT),
        Pair("63221", MITT),
        Pair("63221", MITT),
        Pair("64237", MITT),
        Pair("64122", MITT),
        Pair("64430", MITT),
        Pair("64130", MITT),
        Pair("63188", MITT),
        Pair("63223", MITT),
        Pair("61161", MITT),
        Pair("61185", MITT),
        Pair("61330", MITT),
        Pair("64530", MITT),
        Pair("63219", MITT),
        Pair("61132", MITT),
        Pair("64330", MITT),
        Pair("63188", MITT),
        Pair("63188", MITT),
        Pair("64631", MITT),
        Pair("64635", MITT),
        Pair("61185", MITT),
        Pair("64260", MITT),
        Pair("63188", MITT),
        Pair("64533", MITT),
        Pair("31122", VAST),
        Pair("43244", VAST),
        Pair("30227", VAST),
        Pair("30261", VAST),
        Pair("30291", VAST),
        Pair("30255",	VAST),
        Pair("43279", VAST),
        Pair("94132", NORD),
        Pair("95222", NORD),
        Pair("95282", NORD),
        Pair("97341", NORD),
        Pair("96133", NORD)
    );

    @Test
    fun testCalculateRegion() {
        regionPostalCodes.forEach { p ->
            assertEquals(p.second, GetSRSInformationResponderImpl(mock(), mock(), mock()).calculateRegion(p.first))
        }

    }

}
