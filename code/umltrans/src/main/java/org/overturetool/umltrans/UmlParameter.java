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



public class UmlParameter extends IUmlParameter {

// ***** VDMTOOLS START Name=vdmComp KEEP=NO
  static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
// ***** VDMTOOLS END Name=vdmComp

// ***** VDMTOOLS START Name=ivName KEEP=NO
  private String ivName = null;
// ***** VDMTOOLS END Name=ivName

// ***** VDMTOOLS START Name=ivType KEEP=NO
  private IUmlType ivType = null;
// ***** VDMTOOLS END Name=ivType

// ***** VDMTOOLS START Name=ivMultiplicity KEEP=NO
  private IUmlMultiplicityElement ivMultiplicity = null;
// ***** VDMTOOLS END Name=ivMultiplicity

// ***** VDMTOOLS START Name=ivDefault KEEP=NO
  private String ivDefault = null;
// ***** VDMTOOLS END Name=ivDefault

// ***** VDMTOOLS START Name=ivDirection KEEP=NO
  private IUmlParameterDirectionKind ivDirection = null;
// ***** VDMTOOLS END Name=ivDirection


// ***** VDMTOOLS START Name=vdm_init_UmlParameter KEEP=NO
  private void vdm_init_UmlParameter () throws CGException {
    try {

      ivName = UTIL.ConvertToString(new String());
      ivType = null;
      ivMultiplicity = null;
      ivDefault = UTIL.ConvertToString(new String());
      ivDirection = null;
    }
    catch (Exception e){

      e.printStackTrace(System.out);
      System.out.println(e.getMessage());
    }
  }
// ***** VDMTOOLS END Name=vdm_init_UmlParameter


// ***** VDMTOOLS START Name=UmlParameter KEEP=NO
  public UmlParameter () throws CGException {
    vdm_init_UmlParameter();
  }
// ***** VDMTOOLS END Name=UmlParameter


// ***** VDMTOOLS START Name=identity KEEP=NO
  public String identity () throws CGException {
    return new String("Parameter");
  }
// ***** VDMTOOLS END Name=identity


// ***** VDMTOOLS START Name=accept#1|IUmlVisitor KEEP=NO
  public void accept (final IUmlVisitor pVisitor) throws CGException {
    pVisitor.visitParameter((IUmlParameter) this);
  }
// ***** VDMTOOLS END Name=accept#1|IUmlVisitor


// ***** VDMTOOLS START Name=UmlParameter#5|String|IUmlType|IUmlMultiplicityElement|String|IUmlParameterDirectionKind KEEP=NO
  public UmlParameter (final String p1, final IUmlType p2, final IUmlMultiplicityElement p3, final String p4, final IUmlParameterDirectionKind p5) throws CGException {

    vdm_init_UmlParameter();
    {

      setName(p1);
      setType((IUmlType) p2);
      setMultiplicity((IUmlMultiplicityElement) p3);
      setDefault(p4);
      setDirection((IUmlParameterDirectionKind) p5);
    }
  }
// ***** VDMTOOLS END Name=UmlParameter#5|String|IUmlType|IUmlMultiplicityElement|String|IUmlParameterDirectionKind


// ***** VDMTOOLS START Name=UmlParameter#7|String|IUmlType|IUmlMultiplicityElement|String|IUmlParameterDirectionKind|Long|Long KEEP=NO
  public UmlParameter (final String p1, final IUmlType p2, final IUmlMultiplicityElement p3, final String p4, final IUmlParameterDirectionKind p5, final Long line, final Long column) throws CGException {

    vdm_init_UmlParameter();
    {

      setName(p1);
      setType((IUmlType) p2);
      setMultiplicity((IUmlMultiplicityElement) p3);
      setDefault(p4);
      setDirection((IUmlParameterDirectionKind) p5);
      setPosition(line, column);
    }
  }
// ***** VDMTOOLS END Name=UmlParameter#7|String|IUmlType|IUmlMultiplicityElement|String|IUmlParameterDirectionKind|Long|Long


// ***** VDMTOOLS START Name=init#1|HashMap KEEP=NO
  public void init (final HashMap data) throws CGException {

    {

      String fname = new String("name");
      Boolean cond_4 = null;
      cond_4 = new Boolean(data.containsKey(fname));
      if (cond_4.booleanValue()) 
        setName(UTIL.ConvertToString(data.get(fname)));
    }
    {

      String fname = new String("type");
      Boolean cond_13 = null;
      cond_13 = new Boolean(data.containsKey(fname));
      if (cond_13.booleanValue()) 
        setType((IUmlType) data.get(fname));
    }
    {

      String fname = new String("multiplicity");
      Boolean cond_22 = null;
      cond_22 = new Boolean(data.containsKey(fname));
      if (cond_22.booleanValue()) 
        setMultiplicity((IUmlMultiplicityElement) data.get(fname));
    }
    {

      String fname = new String("default");
      Boolean cond_31 = null;
      cond_31 = new Boolean(data.containsKey(fname));
      if (cond_31.booleanValue()) 
        setDefault(UTIL.ConvertToString(data.get(fname)));
    }
    {

      String fname = new String("direction");
      Boolean cond_40 = null;
      cond_40 = new Boolean(data.containsKey(fname));
      if (cond_40.booleanValue()) 
        setDirection((IUmlParameterDirectionKind) data.get(fname));
    }
  }
// ***** VDMTOOLS END Name=init#1|HashMap


// ***** VDMTOOLS START Name=getName KEEP=NO
  public String getName () throws CGException {
    return ivName;
  }
// ***** VDMTOOLS END Name=getName


// ***** VDMTOOLS START Name=setName#1|String KEEP=NO
  public void setName (final String parg) throws CGException {
    ivName = UTIL.ConvertToString(UTIL.clone(parg));
  }
// ***** VDMTOOLS END Name=setName#1|String


// ***** VDMTOOLS START Name=getType KEEP=NO
  public IUmlType getType () throws CGException {
    return (IUmlType) ivType;
  }
// ***** VDMTOOLS END Name=getType


// ***** VDMTOOLS START Name=setType#1|IUmlType KEEP=NO
  public void setType (final IUmlType parg) throws CGException {
    ivType = (IUmlType) UTIL.clone(parg);
  }
// ***** VDMTOOLS END Name=setType#1|IUmlType


// ***** VDMTOOLS START Name=getMultiplicity KEEP=NO
  public IUmlMultiplicityElement getMultiplicity () throws CGException {
    return (IUmlMultiplicityElement) ivMultiplicity;
  }
// ***** VDMTOOLS END Name=getMultiplicity


// ***** VDMTOOLS START Name=setMultiplicity#1|IUmlMultiplicityElement KEEP=NO
  public void setMultiplicity (final IUmlMultiplicityElement parg) throws CGException {
    ivMultiplicity = (IUmlMultiplicityElement) UTIL.clone(parg);
  }
// ***** VDMTOOLS END Name=setMultiplicity#1|IUmlMultiplicityElement


// ***** VDMTOOLS START Name=getDefault KEEP=NO
  public String getDefault () throws CGException {
    return ivDefault;
  }
// ***** VDMTOOLS END Name=getDefault


// ***** VDMTOOLS START Name=setDefault#1|String KEEP=NO
  public void setDefault (final String parg) throws CGException {
    ivDefault = UTIL.ConvertToString(UTIL.clone(parg));
  }
// ***** VDMTOOLS END Name=setDefault#1|String


// ***** VDMTOOLS START Name=getDirection KEEP=NO
  public IUmlParameterDirectionKind getDirection () throws CGException {
    return (IUmlParameterDirectionKind) ivDirection;
  }
// ***** VDMTOOLS END Name=getDirection


// ***** VDMTOOLS START Name=setDirection#1|IUmlParameterDirectionKind KEEP=NO
  public void setDirection (final IUmlParameterDirectionKind parg) throws CGException {
    ivDirection = (IUmlParameterDirectionKind) UTIL.clone(parg);
  }
// ***** VDMTOOLS END Name=setDirection#1|IUmlParameterDirectionKind

}
;
