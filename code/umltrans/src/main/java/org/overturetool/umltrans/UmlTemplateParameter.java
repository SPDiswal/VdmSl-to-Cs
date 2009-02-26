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



public class UmlTemplateParameter extends IUmlTemplateParameter {

// ***** VDMTOOLS START Name=vdmComp KEEP=NO
  static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
// ***** VDMTOOLS END Name=vdmComp

// ***** VDMTOOLS START Name=ivName KEEP=NO
  private String ivName = null;
// ***** VDMTOOLS END Name=ivName


// ***** VDMTOOLS START Name=vdm_init_UmlTemplateParameter KEEP=NO
  private void vdm_init_UmlTemplateParameter () throws CGException {
    try {
      ivName = UTIL.ConvertToString(new String());
    }
    catch (Exception e){

      e.printStackTrace(System.out);
      System.out.println(e.getMessage());
    }
  }
// ***** VDMTOOLS END Name=vdm_init_UmlTemplateParameter


// ***** VDMTOOLS START Name=UmlTemplateParameter KEEP=NO
  public UmlTemplateParameter () throws CGException {
    vdm_init_UmlTemplateParameter();
  }
// ***** VDMTOOLS END Name=UmlTemplateParameter


// ***** VDMTOOLS START Name=identity KEEP=NO
  public String identity () throws CGException {
    return new String("TemplateParameter");
  }
// ***** VDMTOOLS END Name=identity


// ***** VDMTOOLS START Name=accept#1|IUmlVisitor KEEP=NO
  public void accept (final IUmlVisitor pVisitor) throws CGException {
    pVisitor.visitTemplateParameter((IUmlTemplateParameter) this);
  }
// ***** VDMTOOLS END Name=accept#1|IUmlVisitor


// ***** VDMTOOLS START Name=UmlTemplateParameter#1|String KEEP=NO
  public UmlTemplateParameter (final String p1) throws CGException {

    vdm_init_UmlTemplateParameter();
    setName(p1);
  }
// ***** VDMTOOLS END Name=UmlTemplateParameter#1|String


// ***** VDMTOOLS START Name=UmlTemplateParameter#3|String|Long|Long KEEP=NO
  public UmlTemplateParameter (final String p1, final Long line, final Long column) throws CGException {

    vdm_init_UmlTemplateParameter();
    {

      setName(p1);
      setPosition(line, column);
    }
  }
// ***** VDMTOOLS END Name=UmlTemplateParameter#3|String|Long|Long


// ***** VDMTOOLS START Name=init#1|HashMap KEEP=NO
  public void init (final HashMap data) throws CGException {

    String fname = new String("name");
    Boolean cond_4 = null;
    cond_4 = new Boolean(data.containsKey(fname));
    if (cond_4.booleanValue()) 
      setName(UTIL.ConvertToString(data.get(fname)));
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

}
;
