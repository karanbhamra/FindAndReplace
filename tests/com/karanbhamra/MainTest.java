//*******************************************************************
//  Karandeep Bhamra
//  October 9, 2018
//  COMP585
//  UnitTests to test out some of the string functionality used.
//*******************************************************************
package com.karanbhamra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest
{

    @Test
    public void searchMatchWordTrue()
    {

        boolean result = FindFrame.doesMatchWholeWord("This is a test.", "test");

        assertEquals(true, result);
    }

    @Test
    public void searchMatchWordFalse()
    {
        boolean result = FindFrame.doesMatchWholeWord("This is a test.", "te");
        assertEquals(false, result);
    }

    @Test
    public void searchStringContainsTrue()
    {
        boolean result = "This is a test.".contains("is");

        assertEquals(true, result);
    }

    @Test
    public void searchStringContainsFalse()
    {
        boolean result = "This is a test.".contains("iz");

        assertEquals(false, result);
    }

    @Test
    public void searchStringCaseInsensitiveTrue()
    {
        boolean result = "This IS a test.".toLowerCase().contains("is");
        assertEquals(true, result);
    }

    @Test
    public void searchStringCaseInsensitiveFalse()
    {
        boolean result = "This IS a test.".toLowerCase().contains("IS");
        assertEquals(false, result);
    }

}