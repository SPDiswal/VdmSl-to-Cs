package org.overture.tools.astcreator.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.overture.tools.astcreator.AstCreatorException;
import org.overture.tools.astcreator.Main;

public class SimpleTest extends TestCase {
	private static final String TESTDATA_BASE = "src\\test\\resources\\";

	public void test() throws IOException, InstantiationException,
			IllegalAccessException, AstCreatorException {
		System.out.println(new File(".").getAbsolutePath());
		File output = new File(
				FilePathUtil.getPlatformPath("target/testData/simple"));
		String inputFile = TESTDATA_BASE + "/extonly1.ast";
		Main.create(
				null,
				new FileInputStream(new File(new File(".").getParentFile(),
						FilePathUtil.getPlatformPath(inputFile))
						.getAbsolutePath()), output, true, false);
	}
}
