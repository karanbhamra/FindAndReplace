//*******************************************************************
//  Karandeep Bhamra
//  October 9, 2018
//  COMP585
//  The Main class is the entry point of the program, it creates the
//      FindFrame object which displays the search GUI.
//*******************************************************************

package com.karanbhamra;

import org.apache.log4j.Logger;


public class Main
{

    final static Logger logger = Logger.getLogger(Main.class);

    // The logger object is passed to FindFrame and will log most interactions and results.
    public static void main(String[] args)
    {
        new FindFrame(logger);
    }


}
