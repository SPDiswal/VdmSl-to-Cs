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



public class UmlInteraction extends IUmlInteraction {

// ***** VDMTOOLS START Name=vdmComp KEEP=NO
  static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
// ***** VDMTOOLS END Name=vdmComp

// ***** VDMTOOLS START Name=ivName KEEP=NO
  private String ivName = null;
// ***** VDMTOOLS END Name=ivName

// ***** VDMTOOLS START Name=ivLifeLines KEEP=NO
  private HashSet ivLifeLines = new HashSet();
// ***** VDMTOOLS END Name=ivLifeLines

// ***** VDMTOOLS START Name=ivFragments KEEP=NO
  private HashSet ivFragments = new HashSet();
// ***** VDMTOOLS END Name=ivFragments

// ***** VDMTOOLS START Name=ivMessages KEEP=NO
  private Vector ivMessages = null;
// ***** VDMTOOLS END Name=ivMessages


// ***** VDMTOOLS START Name=vdm_init_UmlInteraction KEEP=NO
  private void vdm_init_UmlInteraction () throws CGException {
    try {

      ivName = UTIL.ConvertToString(new String());
      ivLifeLines = new HashSet();
      ivFragments = new HashSet();
      ivMessages = new Vector();
    }
    catch (Exception e){

      e.printStackTrace(System.out);
      System.out.println(e.getMessage());
    }
  }
// ***** VDMTOOLS END Name=vdm_init_UmlInteraction


// ***** VDMTOOLS START Name=UmlInteraction KEEP=NO
  public UmlInteraction () throws CGException {
    vdm_init_UmlInteraction();
  }
// ***** VDMTOOLS END Name=UmlInteraction


// ***** VDMTOOLS START Name=identity KEEP=NO
  public String identity () throws CGException {
    return new String("Interaction");
  }
// ***** VDMTOOLS END Name=identity


// ***** VDMTOOLS START Name=accept#1|IUmlVisitor KEEP=NO
  public void accept (final IUmlVisitor pVisitor) throws CGException {
    pVisitor.visitInteraction((IUmlInteraction) this);
  }
// ***** VDMTOOLS END Name=accept#1|IUmlVisitor


// ***** VDMTOOLS START Name=UmlInteraction#4|String|HashSet|HashSet|Vector KEEP=NO
  public UmlInteraction (final String p1, final HashSet p2, final HashSet p3, final Vector p4) throws CGException {

    vdm_init_UmlInteraction();
    {

      setName(p1);
      setLifeLines(p2);
      setFragments(p3);
      setMessages(p4);
    }
  }
// ***** VDMTOOLS END Name=UmlInteraction#4|String|HashSet|HashSet|Vector


// ***** VDMTOOLS START Name=UmlInteraction#6|String|HashSet|HashSet|Vector|Long|Long KEEP=NO
  public UmlInteraction (final String p1, final HashSet p2, final HashSet p3, final Vector p4, final Long line, final Long column) throws CGException {

    vdm_init_UmlInteraction();
    {

      setName(p1);
      setLifeLines(p2);
      setFragments(p3);
      setMessages(p4);
      setPosition(line, column);
    }
  }
// ***** VDMTOOLS END Name=UmlInteraction#6|String|HashSet|HashSet|Vector|Long|Long


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

      String fname = new String("lifeLines");
      Boolean cond_13 = null;
      cond_13 = new Boolean(data.containsKey(fname));
      if (cond_13.booleanValue()) 
        setLifeLines((HashSet) data.get(fname));
    }
    {

      String fname = new String("fragments");
      Boolean cond_22 = null;
      cond_22 = new Boolean(data.containsKey(fname));
      if (cond_22.booleanValue()) 
        setFragments((HashSet) data.get(fname));
    }
    {

      String fname = new String("messages");
      Boolean cond_31 = null;
      cond_31 = new Boolean(data.containsKey(fname));
      if (cond_31.booleanValue()) 
        setMessages((Vector) data.get(fname));
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


// ***** VDMTOOLS START Name=getLifeLines KEEP=NO
  public HashSet getLifeLines () throws CGException {
    return ivLifeLines;
  }
// ***** VDMTOOLS END Name=getLifeLines


// ***** VDMTOOLS START Name=setLifeLines#1|HashSet KEEP=NO
  public void setLifeLines (final HashSet parg) throws CGException {
    ivLifeLines = (HashSet) UTIL.clone(parg);
  }
// ***** VDMTOOLS END Name=setLifeLines#1|HashSet


// ***** VDMTOOLS START Name=addLifeLines#1|IUmlNode KEEP=NO
  public void addLifeLines (final IUmlNode parg) throws CGException {
    ivLifeLines.add(parg);
  }
// ***** VDMTOOLS END Name=addLifeLines#1|IUmlNode


// ***** VDMTOOLS START Name=getFragments KEEP=NO
  public HashSet getFragments () throws CGException {
    return ivFragments;
  }
// ***** VDMTOOLS END Name=getFragments


// ***** VDMTOOLS START Name=setFragments#1|HashSet KEEP=NO
  public void setFragments (final HashSet parg) throws CGException {
    ivFragments = (HashSet) UTIL.clone(parg);
  }
// ***** VDMTOOLS END Name=setFragments#1|HashSet


// ***** VDMTOOLS START Name=addFragments#1|IUmlNode KEEP=NO
  public void addFragments (final IUmlNode parg) throws CGException {
    ivFragments.add(parg);
  }
// ***** VDMTOOLS END Name=addFragments#1|IUmlNode


// ***** VDMTOOLS START Name=getMessages KEEP=NO
  public Vector getMessages () throws CGException {
    return ivMessages;
  }
// ***** VDMTOOLS END Name=getMessages


// ***** VDMTOOLS START Name=setMessages#1|Vector KEEP=NO
  public void setMessages (final Vector parg) throws CGException {
    ivMessages = (Vector) UTIL.ConvertToList(UTIL.clone(parg));
  }
// ***** VDMTOOLS END Name=setMessages#1|Vector


// ***** VDMTOOLS START Name=addMessages#1|IUmlNode KEEP=NO
  public void addMessages (final IUmlNode parg) throws CGException {
    ivMessages.add(parg);
  }
// ***** VDMTOOLS END Name=addMessages#1|IUmlNode

}
;
