//
// THIS FILE IS AUTOMATICALLY GENERATED!!
//
// Generated at 2009-01-24 by the VDM++ to JAVA Code Generator
// (v8.2b - Fri 23-Jan-2009 13:05:50)
//
// Supported compilers: jdk 1.4/1.5/1.6
//

// ***** VDMTOOLS START Name=HeaderComment KEEP=NO
// ***** VDMTOOLS END Name=HeaderComment

// ***** VDMTOOLS START Name=package KEEP=NO
package org.overturetool.umltrans;

// ***** VDMTOOLS END Name=package

// ***** VDMTOOLS START Name=imports KEEP=NO

import jp.co.csk.vdm.toolbox.VDM.*;
import java.util.*;
// ***** VDMTOOLS END Name=imports



public class UmlParameterDirectionKindQuotes {

// ***** VDMTOOLS START Name=vdmComp KEEP=NO
  static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
// ***** VDMTOOLS END Name=vdmComp

// ***** VDMTOOLS START Name=IQIN KEEP=NO
  public static Long IQIN = new Long(0);
// ***** VDMTOOLS END Name=IQIN

// ***** VDMTOOLS START Name=IQOUT KEEP=NO
  public static Long IQOUT = new Long(1);
// ***** VDMTOOLS END Name=IQOUT

// ***** VDMTOOLS START Name=IQRETURN KEEP=NO
  public static Long IQRETURN = new Long(2);
// ***** VDMTOOLS END Name=IQRETURN

// ***** VDMTOOLS START Name=IQINOUT KEEP=NO
  public static Long IQINOUT = new Long(3);
// ***** VDMTOOLS END Name=IQINOUT

// ***** VDMTOOLS START Name=qmap KEEP=NO
  private static HashMap qmap = new HashMap();
// ***** VDMTOOLS END Name=qmap


// ***** VDMTOOLS START Name=static KEEP=NO
  static {
    try {

      UmlParameterDirectionKindQuotes.qmap = new HashMap();
      UmlParameterDirectionKindQuotes.qmap.put(IQIN, new String("<IN>"));
      UmlParameterDirectionKindQuotes.qmap.put(IQOUT, new String("<OUT>"));
      UmlParameterDirectionKindQuotes.qmap.put(IQRETURN, new String("<RETURN>"));
      UmlParameterDirectionKindQuotes.qmap.put(IQINOUT, new String("<INOUT>"));
    }
    catch (Throwable e){

      e.printStackTrace(System.out);
      System.out.println(e.getMessage());
    }
  }
// ***** VDMTOOLS END Name=static


// ***** VDMTOOLS START Name=vdm_init_UmlParameterDirectionKindQuotes KEEP=NO
  private void vdm_init_UmlParameterDirectionKindQuotes () throws CGException {}
// ***** VDMTOOLS END Name=vdm_init_UmlParameterDirectionKindQuotes


// ***** VDMTOOLS START Name=UmlParameterDirectionKindQuotes KEEP=NO
  public UmlParameterDirectionKindQuotes () throws CGException {
    vdm_init_UmlParameterDirectionKindQuotes();
  }
// ***** VDMTOOLS END Name=UmlParameterDirectionKindQuotes


// ***** VDMTOOLS START Name=getQuoteName#1|Long KEEP=NO
  static public String getQuoteName (final Long pid) throws CGException {
    return UTIL.ConvertToString(qmap.get(pid));
  }
// ***** VDMTOOLS END Name=getQuoteName#1|Long


// ***** VDMTOOLS START Name=validQuote#1|Long KEEP=NO
  static public Boolean validQuote (final Long pid) throws CGException {

    Boolean rexpr_2 = null;
    rexpr_2 = new Boolean(qmap.containsKey(pid));
    return rexpr_2;
  }
// ***** VDMTOOLS END Name=validQuote#1|Long

}
;
