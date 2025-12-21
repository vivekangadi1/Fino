package com.fino.app.util

import org.junit.Assert.*
import org.junit.Test

class MerchantNormalizerTest {

    @Test
    fun `normalize - converts to uppercase`() {
        assertEquals("SWIGGY", MerchantNormalizer.normalize("swiggy"))
        assertEquals("UBER", MerchantNormalizer.normalize("uber"))
        assertEquals("AMAZON", MerchantNormalizer.normalize("amazon"))
    }

    @Test
    fun `normalize - removes UPI suffixes`() {
        assertEquals("SWIGGY", MerchantNormalizer.normalize("swiggy@paytm"))
        assertEquals("ZOMATO", MerchantNormalizer.normalize("ZOMATO@ybl"))
        assertEquals("AMAZON", MerchantNormalizer.normalize("amazon@okaxis"))
        assertEquals("UBER", MerchantNormalizer.normalize("Uber@paytm"))
    }

    @Test
    fun `normalize - removes special characters`() {
        assertEquals("HDFC BANK", MerchantNormalizer.normalize("HDFC*BANK"))
        assertEquals("SWIGGY", MerchantNormalizer.normalize("Swiggy#123"))
        assertEquals("AMAZON", MerchantNormalizer.normalize("Amazon-001"))
        assertEquals("OLA", MerchantNormalizer.normalize("Ola!"))
    }

    @Test
    fun `normalize - removes numbers`() {
        assertEquals("SWIGGY", MerchantNormalizer.normalize("Swiggy123"))
        assertEquals("HDFC BANK", MerchantNormalizer.normalize("HDFC123BANK456"))
        assertEquals("UBER", MerchantNormalizer.normalize("Uber007"))
    }

    @Test
    fun `normalize - collapses multiple spaces`() {
        assertEquals("HDFC BANK", MerchantNormalizer.normalize("HDFC  BANK"))
        assertEquals("AMAZON INDIA", MerchantNormalizer.normalize("Amazon   India"))
        assertEquals("UBER INDIA", MerchantNormalizer.normalize("Uber    India"))
    }

    @Test
    fun `normalize - trims whitespace`() {
        assertEquals("SWIGGY", MerchantNormalizer.normalize("  swiggy  "))
        assertEquals("AMAZON", MerchantNormalizer.normalize(" amazon "))
    }

    @Test
    fun `normalize - handles complex real-world cases`() {
        // Real UPI merchant names from Indian banks
        assertEquals("SWIGGY", MerchantNormalizer.normalize("swiggy@paytm"))
        assertEquals("ZOMATO", MerchantNormalizer.normalize("Zomato@ybl"))
        assertEquals("AMAZON", MerchantNormalizer.normalize("AMAZON@okaxis"))
        assertEquals("UBER INDIA", MerchantNormalizer.normalize("uber-india@paytm"))

        // Merchant names with special characters
        assertEquals("HDFC BANK SWIGGY", MerchantNormalizer.normalize("HDFC*BANK-Swiggy#001"))
        assertEquals("NETFLIX", MerchantNormalizer.normalize("Netflix.com"))
    }

    @Test
    fun `extractBaseName - removes bank prefixes`() {
        assertEquals("SWIGGY", MerchantNormalizer.extractBaseName("HDFC BANK SWIGGY"))
        assertEquals("ZOMATO", MerchantNormalizer.extractBaseName("SBI ZOMATO"))
        assertEquals("AMAZON", MerchantNormalizer.extractBaseName("ICICI AMAZON"))
        assertEquals("UBER", MerchantNormalizer.extractBaseName("AXIS BANK UBER"))
        assertEquals("OLA", MerchantNormalizer.extractBaseName("KOTAK OLA"))
    }

    @Test
    fun `extractBaseName - removes business suffixes`() {
        assertEquals("AMAZON", MerchantNormalizer.extractBaseName("AMAZON PVT LTD"))
        assertEquals("SWIGGY", MerchantNormalizer.extractBaseName("SWIGGY INDIA"))
        assertEquals("UBER", MerchantNormalizer.extractBaseName("UBER LIMITED"))
        assertEquals("ZOMATO", MerchantNormalizer.extractBaseName("ZOMATO LTD"))
    }

    @Test
    fun `extractBaseName - handles combined prefixes and suffixes`() {
        assertEquals("AMAZON", MerchantNormalizer.extractBaseName("HDFC BANK AMAZON INDIA PVT LTD"))
        assertEquals("SWIGGY", MerchantNormalizer.extractBaseName("SBI SWIGGY LIMITED"))
        assertEquals("UBER", MerchantNormalizer.extractBaseName("ICICI UBER INDIA"))
    }

    @Test
    fun `extractBaseName - handles merchants without prefixes or suffixes`() {
        assertEquals("NETFLIX", MerchantNormalizer.extractBaseName("NETFLIX"))
        assertEquals("SPOTIFY", MerchantNormalizer.extractBaseName("SPOTIFY"))
        assertEquals("MYNTRA", MerchantNormalizer.extractBaseName("MYNTRA"))
    }

    @Test
    fun `areLikelySame - returns true for identical normalized names`() {
        assertTrue(MerchantNormalizer.areLikelySame("swiggy", "SWIGGY"))
        assertTrue(MerchantNormalizer.areLikelySame("Swiggy@paytm", "swiggy@ybl"))
        assertTrue(MerchantNormalizer.areLikelySame("AMAZON123", "amazon456"))
    }

    @Test
    fun `areLikelySame - returns true when one contains the other`() {
        assertTrue(MerchantNormalizer.areLikelySame("SWIGGY", "SWIGGY INDIA"))
        assertTrue(MerchantNormalizer.areLikelySame("AMAZON", "AMAZON PVT LTD"))
        assertTrue(MerchantNormalizer.areLikelySame("UBER INDIA", "UBER"))
    }

    @Test
    fun `areLikelySame - returns false for different merchants`() {
        assertFalse(MerchantNormalizer.areLikelySame("SWIGGY", "ZOMATO"))
        assertFalse(MerchantNormalizer.areLikelySame("UBER", "OLA"))
        assertFalse(MerchantNormalizer.areLikelySame("AMAZON", "FLIPKART"))
    }

    @Test
    fun `normalize - handles empty and whitespace-only strings`() {
        assertEquals("", MerchantNormalizer.normalize(""))
        assertEquals("", MerchantNormalizer.normalize("   "))
        assertEquals("", MerchantNormalizer.normalize("123"))
        assertEquals("", MerchantNormalizer.normalize("@@@"))
    }

    @Test
    fun `real-world Indian merchant names`() {
        // Food delivery
        assertEquals("SWIGGY", MerchantNormalizer.normalize("swiggy@paytm"))
        assertEquals("ZOMATO", MerchantNormalizer.normalize("Zomato@ybl"))
        assertEquals("DOMINOS", MerchantNormalizer.normalize("Domino's Pizza"))

        // Transport
        assertEquals("UBER", MerchantNormalizer.normalize("Uber@paytm"))
        assertEquals("OLA", MerchantNormalizer.normalize("Ola Cabs"))
        assertEquals("RAPIDO", MerchantNormalizer.normalize("Rapido@ybl"))

        // Shopping
        assertEquals("AMAZON", MerchantNormalizer.normalize("Amazon.in"))
        assertEquals("FLIPKART", MerchantNormalizer.normalize("Flipkart@paytm"))
        assertEquals("MYNTRA", MerchantNormalizer.normalize("MYNTRA-001"))

        // Entertainment
        assertEquals("NETFLIX", MerchantNormalizer.normalize("Netflix.com"))
        assertEquals("SPOTIFY", MerchantNormalizer.normalize("Spotify Premium"))
        assertEquals("AMAZON PRIME", MerchantNormalizer.normalize("Amazon Prime Video"))

        // Bills
        assertEquals("JIO", MerchantNormalizer.normalize("Jio Recharge"))
        assertEquals("AIRTEL", MerchantNormalizer.normalize("Airtel Prepaid"))
        assertEquals("TATA SKY", MerchantNormalizer.normalize("Tata Sky DTH"))
    }
}
