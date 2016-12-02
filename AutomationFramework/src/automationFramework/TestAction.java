package automationFramework;

import java.io.IOException;

/********************************************************************************************************
 *Project Name		: Ignite Automation framework 
 *Author		    : Bharat Sethi
 *Version	    	: V1.0
 *Date of Creation	: 04-05-2016
 *Date Last modified: 04/05/2016
 *Description       : Program to be called from command line with 6 arguments to be in the same order 	    
 *		ProjectID 	- args[0]
 *		ReleaseID 	- args[1]
 *		TestSetID 	- args[2]
 *		TestCaseID	- args[3]
 *		TestRunPath	- args[4]
 *		Integration - args[5]
 *Functions			: public static void main(String[] args) - Validates the argument count and then create a Dynamic TestNG XML 
 *					  to run the test from FrameworkDriver class 
 *1. TestAction
 *1a. Log.java
 *2. Framework Driver
 *
 * ********************************************************************************************************
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;
import org.codehaus.jettison.json.JSONException;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import utility.Constant;
import utility.ExcelUtils;
import utility.Log;

public class TestAction {
	/**
	 * @param args
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	public static void main(String[] args) throws NumberFormatException, JSONException {
		String arg;
		/*
		 * 30 1025 "C:\\temp" 100
		 * "C:\AutomationFramework\src\\testData\\Timesheet_Multiplecandidate_Updated.xlsx"
		 * true ProjectId, ReleaseID, executionStartTime, testCompleteStatus,
		 * csvFilePath, excelFilePath, integrationFlag
		 */
		if (args[3].equalsIgnoreCase("200")) {
			arg = "Reading all test arguments as ";
			for (String s : args) {
				arg = arg + " <" + s + ">";
			}
			Log.info(arg);
			Constant.Vars = new LocalTC(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
			Constant.Vars.setReportReleaseId(Integer.toString(Constant.Vars.iReleaseID));
			try {
				ExcelUtils.readCSVForTestComplete();
				Reporter.generateReport(Constant.Vars);
				Constant.Vars.bw1.close();
				Log.info("Report has been generated");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		else {
			if (args.length != 6) {
				System.err.println("Blocked: Test started with wrong input arguments");
				System.exit(1);
			}
			arg = "Reading all test arguments as ";
			for (String s : args) {
				arg = arg + " <" + s + ">";
			}
			// Provide Log4j configuration settings
			DOMConfigurator.configure("log4j.xml");
			Log.info(arg);

			/****************
			 * Create a TestNG XML at runtime by passing the command line
			 * variables to TestNG
			 **************/
			// Set Suite Name in XML
			XmlSuite suite = new XmlSuite();
			suite.setName("IgniteSuite");
			Log.info("added xml suite");
			// Set Test name in XML
			XmlTest test = new XmlTest(suite);
			test.setName("IgniteTest");
			Log.info("added xml test");
			// Add Parameters to the XML
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("ProjectID", args[0]);
			parameters.put("ReleaseID", args[1]);
			parameters.put("TestSetID", args[2]);
			parameters.put("TestCaseID", args[3]);
			parameters.put("TestRunPath", args[4]);
			parameters.put("Integration", args[5]);
			Log.info("added xml parameters");
			test.setParameters(parameters);
			Log.info("added xml parameters to xml");
			// Add Class name in xml
			List<XmlClass> myclasses = new ArrayList<XmlClass>(); // List of
																	// XMLClass
			String classname = "automationFramework.FrameworkDriver";
			XmlClass cl = new XmlClass(classname);
			myclasses.add(cl); // add new class to the list
			test.setXmlClasses(myclasses); // add class list to XML
			// Create a list of XmlTests and add the Xmltest you created earlier
			// to it.
			List<XmlTest> myTests = new ArrayList<XmlTest>();
			myTests.add(test);
			suite.setTests(myTests);

			/************************************************************************************************************/
			Log.info("Created a dynamic XML using parameters");
			/****************
			 * Calling Framework to execute test for this test case
			 ****************************************/
			List<XmlSuite> suites = new ArrayList<XmlSuite>();
			suites.add(suite);
			TestNG testng = new TestNG();
			testng.setXmlSuites(suites);
			Log.info("Starting IgniteFramework to execute test");
			testng.run();
		}

		/*************************************************************************************************************/
	}

}
