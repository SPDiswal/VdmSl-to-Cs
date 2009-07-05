package org.overturetool.potrans.external_tools;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class UtilitiesTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetFormatedDate() throws Exception {
		Date now = new Date();
		String formatedDate = Utilities.getFormatedDate(now);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		
		Date artual = dateFormat.parse(formatedDate);
		
		assertEquals(now, artual);
	}
	
	public void testSystemProperties() throws Exception {
		assertNotNull(Utilities.FILE_SEPARATOR);
		assertNotNull(Utilities.LINE_SEPARATOR);
		assertNotNull(Utilities.PATH_SEPARATOR);
	}

}
