/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */

package me.christoffer.junitpmdtestwrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.PMD;

import org.junit.Assert;

/**
 * Contains the logic for the JUnit PMD test wrapper
 *
 * @author Christoffer Pettersson, christoffer@christoffer.me
 */

public abstract class JUnitPMDTestWrapper {

	/**
	 * Runs the PMD JUnit test wrapper on a specific folder based on a given rule set file in the same folder
	 *  
	 * @param testClassInstance The JUnit test instance
	 * @param folderToCheck The folder that should be tested
	 * @param ruleFileName Name of the PMD rule set file that should be located in the same folder
	 * @throws IOException If any error occur
	 */

	public static void run(final Object testClassInstance, final String folderToCheck, final String ruleFileName) throws IOException {

		File fileFolderToCheck = new File(folderToCheck);

		/*
		 * Start-up message 
		 */

		System.out.println("Starting PMD code analyzer test on folder '" + fileFolderToCheck.getAbsolutePath() + "'.");

		/*
		 * Validation
		 */

		if (!fileFolderToCheck.exists()) {
			throw new FileNotFoundException("The folder to check '" + fileFolderToCheck.getAbsolutePath() + "' does not exist.");
		}

		if (!fileFolderToCheck.isDirectory()) {
			throw new FileNotFoundException("The folder to check '" + fileFolderToCheck.getAbsolutePath() + "' is not a directory.");
		}

		URL ruleFileURL = testClassInstance.getClass().getResource(ruleFileName);

		if (testClassInstance.getClass().getResource(ruleFileName) == null) {
			throw new FileNotFoundException("The rule set file '" + ruleFileName + "' does not exist in the same folder as '" + testClassInstance.getClass().getSimpleName() + "'.");
		}

		/*
		 * Initialize commands
		 */

		String outputType = "text";
		String rules = URLDecoder.decode(ruleFileURL.toString(), "UTF-8");

		String[] arguments = new String[] { fileFolderToCheck.getAbsolutePath(), outputType, rules };

		/*
		 * Save the existing output streams
		 */

		PrintStream out = System.out;
		PrintStream err = System.err;

		/*
		 * Init redirects
		 */

		ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream baosErr = new ByteArrayOutputStream();

		PrintStream psOut = new PrintStream(baosOut);
		PrintStream psErr = new PrintStream(baosErr);

		/*
		 * Redirect the streams
		 */

		System.setOut(psOut);
		System.setErr(psErr);

		/*
		 * Process
		 */

		PMD.main(arguments);

		/*
		 * Put back the output streams
		 */

		System.setOut(out);
		System.setErr(err);

		/*
		 * Cleanup
		 */

		psOut.close();
		psErr.close();
		baosOut.close();
		baosErr.close();

		/*
		 * Output
		 */

		String linesOut[] = baosOut.toString().split("\\r?\\n");
		List<String> rowsOut = new ArrayList<String>();

		for (String line : linesOut) {
			if (!line.isEmpty() && line.indexOf("suppressed by Annotation") == -1 && line.indexOf("No problems found!") == -1 && line.indexOf("Error while processing") == -1) {
				rowsOut.add(line);
			}
		}

		System.out.println("Found " + rowsOut.size() + " errors");
		for (String error : rowsOut) {
			System.out.println(error + "\n");
		}

		if (!baosErr.toString().isEmpty()) {
			System.out.println("Errors:");
			System.out.println(baosErr.toString());
		}

		/*
		 * Assert
		 */

		String errorMessage = "";
		for (String row : rowsOut) {
			errorMessage += row + "\n";
		}

		Assert.assertTrue(rowsOut.size() + " errors\n" + errorMessage, rowsOut.isEmpty());
		Assert.assertTrue(baosErr.toString(), baosErr.toString().trim().length() == 0);

	}

}