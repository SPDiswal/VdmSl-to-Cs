//
// THIS FILE IS AUTOMATICALLY GENERATED!!
//
// Generated at 2009-02-15 by the VDM++ to JAVA Code Generator
// (v8.2b - Tue 03-Feb-2009 11:50:55)
//
// Supported compilers: jdk 1.4/1.5/1.6
//

// ***** VDMTOOLS START Name=HeaderComment KEEP=NO
// ***** VDMTOOLS END Name=HeaderComment

// ***** VDMTOOLS START Name=package KEEP=NO
package org.overturetool.traces;

// ***** VDMTOOLS END Name=package

// ***** VDMTOOLS START Name=imports KEEP=YES

import jp.co.csk.vdm.toolbox.VDM.*;
import java.util.*;
@SuppressWarnings("unchecked")
// ***** VDMTOOLS END Name=imports



public abstract class ToolBox {


// ***** VDMTOOLS START Name=InterpreterResult KEEP=NO
  public static class InterpreterResult implements Record {

    public Boolean successfull;

    public String output;


    public InterpreterResult () {}


    public InterpreterResult (Boolean p1, String p2) {

      successfull = p1;
      output = p2;
    }


    public Object clone () {
      return new InterpreterResult(successfull, output);
    }


    public String toString () {
      return "mk_ToolBox`InterpreterResult(" + UTIL.toString(successfull) + "," + UTIL.toString(output) + ")";
    }


    public boolean equals (Object obj) {
      if (!(obj instanceof InterpreterResult)) 
        return false;
      else {

        InterpreterResult temp = (InterpreterResult) obj;
        return UTIL.equals(successfull, temp.successfull) && UTIL.equals(output, temp.output);
      }
    }


    public int hashCode () {
      return (successfull == null ? 0 : successfull.hashCode()) + (output == null ? 0 : output.hashCode());
    }

  }
// ***** VDMTOOLS END Name=InterpreterResult
;

// ***** VDMTOOLS START Name=vdmComp KEEP=NO
  static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
// ***** VDMTOOLS END Name=vdmComp

// ***** VDMTOOLS START Name=isInilized KEEP=NO
  protected Boolean isInilized = null;
// ***** VDMTOOLS END Name=isInilized

// ***** VDMTOOLS START Name=specsFiles KEEP=NO
  protected HashSet specsFiles = new HashSet();
// ***** VDMTOOLS END Name=specsFiles


// ***** VDMTOOLS START Name=vdm_init_ToolBox KEEP=NO
  private void vdm_init_ToolBox () throws CGException {
    try {
      isInilized = new Boolean(false);
    }
    catch (Exception e){

      e.printStackTrace(System.out);
      System.out.println(e.getMessage());
    }
  }
// ***** VDMTOOLS END Name=vdm_init_ToolBox


// ***** VDMTOOLS START Name=ToolBox KEEP=NO
  public ToolBox () throws CGException {
    vdm_init_ToolBox();
  }
// ***** VDMTOOLS END Name=ToolBox


// ***** VDMTOOLS START Name=ToolBox#1|HashSet KEEP=NO
  public ToolBox (final HashSet sp) throws CGException {

    vdm_init_ToolBox();
    specsFiles = (HashSet) UTIL.clone(sp);
  }
// ***** VDMTOOLS END Name=ToolBox#1|HashSet


// ***** VDMTOOLS START Name=InitToolbox KEEP=NO
  abstract public void InitToolbox () throws CGException ;
// ***** VDMTOOLS END Name=InitToolbox


// ***** VDMTOOLS START Name=IsPreError#1|InterpreterResult KEEP=NO
  abstract public Boolean IsPreError (final InterpreterResult res) throws CGException ;
// ***** VDMTOOLS END Name=IsPreError#1|InterpreterResult


// ***** VDMTOOLS START Name=runTraceTestCase#2|String|Vector KEEP=NO
  public Vector runTraceTestCase (final String className, final Vector expressions) throws CGException {

    if (new Boolean(!isInilized.booleanValue()).booleanValue()) {

      InitToolbox();
      isInilized = (Boolean) UTIL.clone(new Boolean(true));
    }
    return runTestCase(className, expressions);
  }
// ***** VDMTOOLS END Name=runTraceTestCase#2|String|Vector


// ***** VDMTOOLS START Name=runTestCase#2|String|Vector KEEP=NO
  abstract protected Vector runTestCase (final String className, final Vector expressions) throws CGException ;
// ***** VDMTOOLS END Name=runTestCase#2|String|Vector


// ***** VDMTOOLS START Name=ResultsToStrings#1|Vector KEEP=NO
  static public Vector ResultsToStrings (final Vector res) throws CGException {

    Vector rexpr_2 = null;
    {

      Vector res_l_3 = new Vector();
      HashSet resBind_s_5 = new HashSet();
      HashSet riseq_9 = new HashSet();
      int max_10 = res.size();
      for (int i_11 = 1; i_11 <= max_10; i_11++) 
        riseq_9.add(new Long(i_11));
      resBind_s_5 = riseq_9;
      Vector bind_l_4 = null;
      bind_l_4 = UTIL.Sort(resBind_s_5);
      Long i = null;
      for (Iterator enm_17 = bind_l_4.iterator(); enm_17.hasNext(); ) {

        Long e_7 = UTIL.NumberToLong(enm_17.next());
        i = e_7;
        String reselem_12 = null;
        InterpreterResult tmpRec_13 = null;
        if ((1 <= i.intValue()) && (i.intValue() <= res.size())) 
          tmpRec_13 = (InterpreterResult) res.get(i.intValue() - 1);
        else 
          UTIL.RunTime("Run-Time Error:Illegal index");
        reselem_12 = (tmpRec_13).output;
        res_l_3.add(reselem_12);
      }
      rexpr_2 = res_l_3;
    }
    return rexpr_2;
  }
// ***** VDMTOOLS END Name=ResultsToStrings#1|Vector

}
;
