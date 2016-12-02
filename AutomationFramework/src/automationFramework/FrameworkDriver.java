package automationFramework;
/********************************************************************************************************
 *Project Name		: Ignite Automation framework 
 *Author		    : Bharat Sethi
 *Version	    	: V1.0
 *Date of Creation	: 28-04-2016
 *Date Last modified: 04/05/2016
 *Description       : Getting maximum benefits with minimum effort. This Framework will give ability to increase 
 *					  the efficiency of resources, increase test coverage, and increase the quality and 
 *					  reliability of the software.
 *Functions			: 
 *
@BeforeSuite  - beforeSuite - Configures the log
@BeforeTest   - loadLocally - Load all arguments to the LocalTC object
@BeforeClass  - 
@BeforeMethod - ReadTest    - If Integration is set to true then run a loop to read each step from SpiraReader else from excel
@Test         - StartTest   - Loop through all steps in a test case and send steps string to translator  
@AfterMethod  -
@AfterClass   -
@AfterTest    -
@AfterSuite   -
@DataProvider -
 ********************************************************************************************************
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import ObjectMap.OR;
import SpiraTest.SpiraReader;
import mail.SendMail;
import utility.BrowserFactory;
import utility.Constant;
import utility.ExcelUtils;
import utility.Log;
import utility.Utils;

public class FrameworkDriver
{
	LocalTC Vars;
	SpiraReader SpiraRead;
	int rowCountBw = 0;
	int rowCount = 0;

	/****************************This function is called by TestNG at the start of the test suite
	 * @throws MalformedURLException ***********************/
	@BeforeSuite
	public void beforeSuite() throws MalformedURLException {
		Log.info("beforeSuite : Strating Project Test Execution" );
		SpiraRead = new SpiraReader();
	}

	/*************This function is called by TestNG at the start of the test by loading all arguments to object
	 * @throws Exception ********/
	@Parameters({"ProjectID","ReleaseID","TestSetID","TestCaseID","TestRunPath","Integration"})
	@BeforeTest
	public void loadLocally(String ProjectID,String ReleaseID,String TestSetID,String TestCaseID,String TestRunPath,String Integration) throws Exception
	{
		Vars = new LocalTC(ProjectID,ReleaseID,TestSetID,TestCaseID,TestRunPath,Integration);
		Log.info("loadLocally : Running test for Project ID " +  Vars.getProjectID());
		if (Vars.getIntegration() == false)
			Log.startTestSet("loadLocally : Starting to execute Test Set " + TestSetID + " and Test Case " + TestCaseID);
	}
	/****************************Initiate the 
	 * 
	 * @throws Exception
	 */
	@BeforeMethod //initiate the browser of a particular type (ie/firefox/chrome)
	public void init() throws Exception
	{		
		Vars.Translate = new TranslateEngine();
		Vars.TestRun = new ExcelUtils();
		Vars.TestData = new ExcelUtils();
		Constant.Vars = Vars;
		if (Vars.getIntegration() == false){
			//Create test run for the test set this has to be called for each set in release
			Vars.TestRun.setExcelFile(Vars.getTestRunPath(),"Test Runs");
			Log.info("init : test case excel opened");
			Log.info("init: It is start of test execution");
		}
		if (Vars.fscreenlock == 100) {
			Vars.obj = new OR("ObjectRepository/TestComplete.Properties");
		} else {
			Vars.obj = new OR("ObjectRepository/OR.Properties");
		}
		if (Vars.getIntegration() == true){
			//Create test run for the test set this has to be called for each set in release
			//SpiraRead.CreateTestRun(Vars);  
		}
		//create detail directory
		ExcelUtils.createScreenShotDirectory(Vars, "Details");
		ExcelUtils.deleteFile(Vars, null);
	}

	@Test 
	public void StartTest() throws Exception 
	{
		//if set to false open the excel file from TestRunPath sample added in TestData package
		//loop through the excel or SpiraTest for all the test cases in the given Set
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Vars.setExecutionStartTime(dateFormat.format(new Date()));
		ReporterSummaryObject reportSumObj = new ReporterSummaryObject();
		ReporterCommon reportCommonObj = new ReporterCommon();
		String strTestCaseId = "";
		String testSetBrowserName = "";
		if (Vars.getIntegration() == false) {
			int retRowCount = Vars.TestRun.getRowCount();
			// Loop through all rows test step, expected, test step id and
			// sample data from excel and
			reportCommonObj.setReportBrowserName(Vars.getbrowsername());
			reportCommonObj.setReportTestSetId(Integer.toString(99999));
			reportCommonObj.setReportTestSetName("Evo TestSet");
			if(Vars.fscreenlock == 100){
				Vars.setTestSetName("Frostnet TestSet");
			}else
				Vars.setTestSetName("Evo TestSet");
			if(null == Vars.TestRun.getCellData(2, 2) || Vars.TestRun.getCellData(2, 2).isEmpty()){
				Vars.setReportReleaseId(Integer.toString(1000));
				Vars.iReleaseID = 1000;
			}
			else{
				Vars.setReportReleaseId(Vars.TestRun.getCellData(2, 2));
				Vars.iReleaseID = Integer.parseInt(Vars.TestRun.getCellData(2, 2));
			}
			testSetBrowserName = Vars.getTestSetName() + "&#" + Vars.getbrowsername();
			for (int rowItr = 2; rowItr < retRowCount; rowItr++) {
				if (Vars.getStopExecution().equals("false")) {
					// Reset execution result
					Vars.row = rowItr - 2;
					if (Vars.testcasestart != 0 && null != Vars.TestRun.getCellData(rowItr, 1)
							&& !Vars.TestRun.getCellData(rowItr, 1).isEmpty()) {
						reportSumObj.getReportObjMap().put(Vars.getTestCaseName(),
								new ArrayList<>(Vars.reporterObjectList));
						Vars.reporterSumObjList.add(new ReporterSummaryObject(reportSumObj));
						Vars.getReportMap().put(testSetBrowserName, new ArrayList<>(Vars.reporterSumObjList));
						Vars.getReportCommonObj().add(reportCommonObj);
					}
					if (null != Vars.TestRun.getCellData(rowItr, 1) && !Vars.TestRun.getCellData(rowItr, 1).isEmpty()) {
						reportSumObj = new ReporterSummaryObject();
						Vars.reporterObjectList = new ArrayList<>();
						if (Vars.bw != null) {
							Vars.conditionSkip = false;
							if (Vars.loopflag == 1) {
								Vars.loopflag = 0;
								KeywordAction.endloop(Vars);
							}
							Vars.bw.close();
							Log.endTestCase("End of Test Case : " + Vars.getTestCaseName());
						}
						Vars.setExecutionCount(rowItr - 1);
						Vars.testcasestart = rowItr;
						strTestCaseId = Vars.TestRun.getCellData(rowItr, 0).replaceAll("[|?\\/*:<>TCtc-]*", "");
						Vars.setTestCaseID(Integer.parseInt(strTestCaseId));
						Vars.setTestCaseName(Vars.TestRun.getCellData(rowItr, 1));
						Vars.setResultStatus(Vars.TestRun.getCellData(rowItr, 9));
						Log.startTestCase("Start of Test Case " + Vars.getTestCaseName());
						Vars.setTestCaseStatus("Passed");
						if(null != Vars.TestRun.getCellData(rowItr, 3) && !Vars.TestRun.getCellData(rowItr, 3).isEmpty())
							Vars.setTestSetID(Integer.parseInt(Vars.TestRun.getCellData(rowItr, 3)));
						// storing the object of report into arraylist for
						// summary report
						reportSumObj.setReportSummaryTestCaseID(Vars.getTestCaseID());
						reportSumObj.setReportSummaryTestCaseName(Vars.getTestCaseName());
					} else {
						if (null != Vars.TestRun.getCellData(rowItr, 6)
								&& !Vars.TestRun.getCellData(rowItr, 6).isEmpty()) {
							// Reading one row of excel for Step, Expected, Test
							// StepID, Sample Data
							Vars.setTestStep(Utils.htmlToTextConvertMethod(Vars.TestRun.getCellData(rowItr, 6)));
							if (null != Vars.TestRun.getCellData(rowItr, 7)
									&& !Vars.TestRun.getCellData(rowItr, 7).isEmpty())
								Vars.setExpected(Utils.htmlToTextConvertMethod(Vars.TestRun.getCellData(rowItr, 7)));
							else
								Vars.setExpected("");
							Vars.setTestStepID(Vars.TestRun.getNumaricCellData(rowItr, 5));
							Vars.setSampleData(Vars.TestRun.getCellData(rowItr, 8));
							Vars.setExecutionResult("");
							KeywordLibrary.ReadTest(Vars); // Execute all
															// actions in a test
															// step
							reportSumObj.setReportSummaryTestCaseStatus(Vars.getTestCaseStatus());
							if (Vars.fscreenlock != 100) {
								ExcelUtils.updateExcellSheet(Constant.Vars);
							}
						}
					}
				} else
					break;

			}
			reportSumObj.getReportObjMap().put(Vars.getTestCaseName(), new ArrayList<>(Vars.reporterObjectList));
			Vars.reporterSumObjList.add(new ReporterSummaryObject(reportSumObj));
			Vars.getReportMap().put(testSetBrowserName, new ArrayList<>(Vars.reporterSumObjList));
			Vars.getReportCommonObj().add(reportCommonObj);
			Vars.reporterSumObjList = new ArrayList<>();
			if(Vars.loopflag == 1)
			{
				Vars.loopflag =0;
				KeywordAction.endloop(Vars);
			}
			DateFormat dateFormatEndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Constant.Vars.setExecutionEndTime(dateFormatEndTime.format(new Date()));
			Vars.conditionSkip = false;
			Log.endTestCase("End of Test Case : " + Vars.getTestCaseName());
		}
		if (Vars.getIntegration()==true) {
			Log.info(" StartTest : calling  getIntegration " + Vars.getIntegration());
			Log.info("StartTest : calling Reader function ");
			SpiraRead.ExtractRelease(Vars);
			DateFormat dateFormatEndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Constant.Vars.setExecutionEndTime(dateFormatEndTime.format(new Date()));
		}
	}

	@AfterMethod
	public void afterMethod() throws IOException {
		if (Vars.fscreenlock == 100) {
			Process process = null;
			try{
			ExcelUtils.writeCSVForTestComplete(Vars);
			File batchFile = new File(Constant.testCompleteBatch);
			Log.info("Automation Framework End!!");
			Log.info("CSV for TestComplete has been generated and stored in path " + Constant.tempTestReportPath + "TestComplete.csv");
			Log.info("TestComplete is called");
			//"ProjectId" “ReleaseId” “ExecutionStartTime” “csvPath” “excelPath” “booleanFlag”
			process = Runtime.getRuntime().exec("cmd /c start" + " " + batchFile.getAbsolutePath() + " " + Vars.getProjectID() + " " + Vars.getReportReleaseId() + " \"" + Vars.getExecutionStartTime() + "\" " + "\""+ Constant.tempTestReportPath + "TestComplete.csv" + "\"" + " " + "\""+ Vars.getTestRunPath() + "\"" + " " + Vars.getIntegration());
			}catch(Exception e){
				Log.error("Error in writing CSV: " + e.getMessage());
			}finally{
				Runtime.getRuntime().exec("taskkill " + process);
			}
		} else {
			Reporter.generateReport(Vars);
			Vars.bw1.close();
			Log.endTestSet("Ending TestSet " + Vars.getTestSetID());
			try {
				String[] mailObjects = Vars.obj.getPropertyMail().split(";");
				if (mailObjects.length > 0 && mailObjects.length == 5)
					SendMail.sendAttachmentEmail(mailObjects[0], mailObjects[1], mailObjects[2], mailObjects[3],
							mailObjects[4]);
			} catch (Exception e) {
				Log.error("Error in getting object for mail: " + e.getMessage());
			}
		}
	}

	@AfterSuite
	public void tearDown() throws IOException
	{
		if (Vars.fscreenlock != 100) {
			BrowserFactory.closeAllDriver();
			Log.info("Browser closed");
		}
	}
}
