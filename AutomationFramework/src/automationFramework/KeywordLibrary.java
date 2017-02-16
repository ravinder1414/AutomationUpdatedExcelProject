package automationFramework;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.automation.testcomplete.TestCompleteBean;

import utility.Constant;
import utility.ExcelUtils;
import utility.Log;

public class KeywordLibrary {
	static int screenshotflag = 1;
	static int devFlag;
	static int initscreen = 2;
	static String strLog; 
	// Execute test actions in one step
	public static void ReadTest(LocalTC Vars) throws Exception{
		if(Vars.getTestStep() !="")
		{   
			Vars.ExecutionStatus = "Not Run";
			String TC = Vars.getTestStep();   //Store steps from Local TC to a local variable
			String Ex = Vars.getExpected();   // Store Expected from Local TC to a local variable
			String steps[] = TC.split("\\n");  //split the steps to get all action in one step
			String expected[] = Ex.split("\\n");  //split the expected steps to get all action in one step
			Vars.act = 0;  //Reset action counter to 0 that tells the report the action count with in step
			Vars.setActualResult("");  // Reset Actual result of the test step 
			if (Vars.fscreenlock > 0 && Vars.fscreenlock != 100)
			{
				Vars.robot.keyPress(KeyEvent.VK_PRINTSCREEN);
				Vars.robot.keyRelease(KeyEvent.VK_PRINTSCREEN);
				//Vars.robot.keyPress(KeyEvent.VK_TAB);
				//Vars.robot.keyRelease(KeyEvent.VK_ALT);
				Vars.robot.mouseMove(1, 1); //Keeping system alive so it does not gets locked
			}
			if(Vars.getSampleData() != null && ! (Vars.getSampleData().contains("null"))){
				if(steps[0].toLowerCase().contains(Constant.RunTest_Case) || steps[0].toLowerCase().contains(Constant.RunTestCase) )
					Vars.RunTestCase = Vars.getSampleData();
				else
					if(Vars.getSampleData().contains("dt_"))
					{
						Vars.TestData.setExcelFile(Constant.Path_TestData, Vars.getSampleData().replace("dt_", ""));
						Vars.setObj(Vars.TestData.getRowCount()+"");
						KeywordAction.loop(Vars);
						Vars.loopcount[Vars.loopsize-1] = Vars.TestData.getRowCount()-1;
						Vars.setTestdata(Vars.getSampleData());
					}
				if(Vars.getSampleData().contains("dt_") && Vars.loopflag == 0)
				{
					Vars.TestData.setExcelFile(Constant.Path_TestData, Vars.getSampleData().replace("dt_", ""));
					Vars.sTestStep = "Loop " + Vars.TestData.getRowCount() + " times";
					execsteps(Vars,Vars.sTestStep,Vars.getSampleData());
				}
			}
			Log.info("KeywordLibrary.ReadTest - Test  step ID :: "+ Vars.getTestStepID());
			for (int i=0; i<steps.length;i++){  //loop to all actions in a step
				Vars.sTestStep = steps[i].trim();
				if (Vars.sTestStep  == null)
					Vars.sTestStep="";
				if (! Vars.sTestStep.trim().equalsIgnoreCase(""))
				{
					strLog = "KeywordLibrary.ReadTest - Test Step :: " + Vars.sTestStep;
					//Log.info("KeywordLibrary.ReadTest - Test Step :: " + Vars.getTestCaseID() + "-" + Vars.row + "_" + Vars.getTestStepID() + "-" + Vars.act + " " + steps[i]); //Print test action
					execsteps(Vars,Vars.sTestStep,Vars.getTestdata());
					if (expected.length==steps.length){
						if(expected[i].startsWith("null"))
							expected[i]="";
						if (! expected[i].trim().equalsIgnoreCase("")) {  
							strLog="KeywordLibrary.ReadTest - Test Expected :: " + expected[i];
							//Log.info("KeywordLibrary.ReadTest - Test Expected :: " + Vars.getTestCaseID() + "-" + Vars.row + "_" + Vars.getTestStepID() + "-" + Vars.act + " " + expected[i]);
							Vars.sTestStep = expected[i];
							execsteps(Vars,Vars.sTestStep,Vars.getTestdata());
							//Vars.setActualResult(Vars.getActualResult() + Vars.ExecutionResult + ";");
							Vars.setTestCaseStatus("");
						}
					}
				}
			}
			//when expected steps are not equal to test steps
			// that suggest that expected steps to be executed after completion of all steps
			if (expected.length!=steps.length){  
				for (int i=0; i<expected.length;i++){ 
					if(expected[i].startsWith("null"))
						expected[i]="";
					if (! expected[i].trim().equalsIgnoreCase("")) {
						Log.info("KeywordLibrary.ReadTest - Test expected :: " + Vars.getTestStepID() + "_" + Vars.row  + "-" + Vars.act + " " +expected[i]);
						Vars.sTestStep = expected[i];
						execsteps(Vars,expected[i],Vars.getTestdata());
						//Vars.setActualResult(Vars.getActualResult() + Vars.ExecutionResult + ";");
						Vars.setTestCaseStatus("");
					}
				}
			}
		}
	}

	/************************
	 * Function   : execsteps 
	 * Description: This function used to execute current step loaded in to Vars
	 * @param Vars
	 * @throws Exception 
	 */
	public static void execsteps(LocalTC Vars,String step,String TestData) throws Exception{
		String browsername = Vars.obj.getBrowser();
		if(null != browsername && !browsername.isEmpty()){
			Vars.browsername = browsername;
		}
		try{
			if(Vars.loopflag == 1 &&  ! step.contains("loop") && (Vars.runtestloopflag > 1 || Vars.runtestloopflag == 0)){
				Vars.loopTestCases = Arrays.copyOf(Vars.loopTestCases, (Vars.startrow+1));
				Vars.loopTestStepID = Arrays.copyOf(Vars.loopTestStepID, (Vars.startrow+1));
				Vars.loopTestCaseName = Arrays.copyOf(Vars.loopTestCaseName, (Vars.startrow+1));
				Vars.loopTestCaseId = Arrays.copyOf(Vars.loopTestCaseId, (Vars.startrow+1));
				Vars.loopTestStepId = Arrays.copyOf(Vars.loopTestStepId, (Vars.startrow+1));
				Vars.loopTestCases[Vars.startrow] = step;
				Vars.loopTestCaseName[Vars.startrow] = Vars.getTestCaseName();
				Vars.loopTestCaseId[Vars.startrow] = Integer.toString(Vars.getTestCaseID());
				Vars.loopTestStepId[Vars.startrow] = Integer.toString(Vars.TestStepID);
				Vars.loopTestStepID[Vars.startrow++] = Vars.TestStepID + "_" + Vars.row + "-" + Vars.startrow ;
				if (Vars.fscreenlock != 100) {
					Vars.act = Vars.startrow +1;
				}
				Log.info(Vars.getTestCaseID() + "-" + Vars.row + "_" + Vars.getTestStepID() + "-" + Vars.startrow + " " + strLog.replace("endloop", step) ); //Print test action
				Log.info("Inside loop adding steps to execution stack #" + (Vars.startrow-1) + " loop number # " + (Vars.loopnum+1));
			}
			else{
				//check if this step is with in the condition block if yes and condition is been set to skip  
				if(Vars.conditionSkip == false)
				{
					strLog = "KeywordLibrary.ReadTest - Test Step :: " + step;
					Log.info(Vars.getTestCaseID() + "-" + Vars.row + "_" + Vars.getTestStepID() + "-" + Vars.act + " " + strLog.replace("endloop", step) );
					//Vars.setTestdata(TestData);
					Vars.act++;
					Vars.Translate.FindKeyword(Vars, step);
					Vars.setbFlagSpell(false);
					if (Vars.fscreenlock == 100) {
						TestCompleteBean testCompleteObj = new TestCompleteBean();
						testCompleteObj.setAction(Vars.Action);
						testCompleteObj.setExecutionResult("");
						testCompleteObj.setEvent(Vars.Event);
						testCompleteObj.setObj(Vars.Obj);
						if(Vars.sTestStep.contains("obj=") && !Vars.ObjProp.isEmpty()){
							testCompleteObj.setObjProp(Vars.obj.getValueObjProp(Vars.ObjProp));	
						}else if(!Vars.ObjProp.isEmpty()){
							testCompleteObj.setObjProp(Vars.ObjProp);
						}
						testCompleteObj.setStatus("");
						testCompleteObj.setTestCaseId(Integer.toString(Vars.iTestCaseID));
						testCompleteObj.setTestCaseName(Vars.TestCaseName);
						testCompleteObj.setTestdata(Vars.Testdata);
						testCompleteObj.setTestSetName(Vars.TestSetName);
						testCompleteObj.setTestStep(Vars.sTestStep);
						testCompleteObj.setTestStepID(Integer.toString(Vars.TestStepID));
						testCompleteObj.setExecutionTime("");
						testCompleteObj.setScreenshotPath("");
						testCompleteObj.setReportStatus("");
						testCompleteObj.setAct(Vars.act);
						testCompleteObj.setTestSetId(Integer.toString(Vars.getTestSetID()));
						Vars.getTestCompleteList().add(testCompleteObj);

					} else {
						KeywordAction.CallAction(Vars);
						if (!Vars.isRunTestCaseFlag())
							setReportVariables(Vars);
						Vars.setResultStatus(Vars.ExecutionStatus);
						Vars.setRunTestCaseFlag(false);
						Vars.setTestCaseStatus("");
						Vars.setActualResult(Vars.getActualResult() + Vars.ExecutionResult + ";");
						Log.info(Vars.ExecutionStatus + ":" + Vars.ExecutionResult);
					}
					//if expected steps and test steps are equal
					//that suggest that we have one expected for each step	
				} else {
					if ((step.contains(Constant.End_Condition) || step.contains(Constant.Endcondition)))
						Vars.conditionSkip = false;
					else{
						Log.info("Skipped : " + step);
						//vars.setExecutionStatus("Skipped");
						Vars.setExecutionStatus(Constant.Passed);
						Vars.setExecutionResult("Step has been skipped");
					}
				}
				if (Vars.captureperform == true) {
					if (Vars.fscreenlock != 100) {
						screenShot(Vars);
					}
				}
			}
		}
		catch(Exception e){
			Log.info(e.getMessage());
		}
	}

	public static void execloopsteps(LocalTC Vars,int start,int end,int count) throws Exception{
		for (Vars.loopcnt[Vars.loopnum]=0;Vars.loopcnt[Vars.loopnum]<count;Vars.loopcnt[Vars.loopnum]++){
			for (int i=start; i<=end;i++)
			{
				if(Vars.loopsize > 1){
					if(Vars.loopstart[Vars.loopnum] < Vars.loopend[Vars.loopnum-1]){
						Vars.loopnum++;
						Vars.loopsize--;
						Vars.TestData.setExcelFile(Constant.Path_TestData, Vars.loopTestData[Vars.loopsize]);
						execloopsteps(Vars,Vars.loopstart[Vars.loopnum],Vars.loopend[Vars.loopnum],Vars.loopcount[Vars.loopnum]);
					}
					else{
						Vars.sTestStep = Vars.loopTestCases[i];
						execsteps(Vars,Vars.sTestStep,Vars.loopTestData[Vars.loopnum]);
					}
				}
				else{
					Vars.sTestStep = Vars.loopTestCases[i];
					if (Vars.fscreenlock == 100) {
						Vars.setTestCaseName(Vars.loopTestCaseName[i]);
						Vars.setTestCaseID(Integer.parseInt(Vars.loopTestCaseId[i]));
						Vars.TestStepID = Integer.parseInt(Vars.loopTestStepId[i]);
					}
					execsteps(Vars,Vars.sTestStep,Vars.loopTestData[Vars.loopnum]);
				}
			}
		}
	}

	public static void execTestCaseloopsteps(LocalTC Vars,int start,int end,int count) throws Exception{
		for (Vars.loopcnt[Vars.loopnum]=0;Vars.loopcnt[Vars.loopnum]<count;Vars.loopcnt[Vars.loopnum]++){
			for (int i=start; i<=end;i++)
			{
				if(Vars.loopsize > 1){
					if(Vars.loopstart[Vars.loopnum] < Vars.loopend[Vars.loopnum-1]){
						Vars.loopnum++;
						Vars.loopsize--;
						Vars.TestData.setExcelFile(Constant.Path_TestData, Vars.loopTestData[Vars.loopsize]);
						execloopsteps(Vars,Vars.loopstart[Vars.loopnum],Vars.loopend[Vars.loopnum],Vars.loopcount[Vars.loopnum]);
					}
					else{
						Vars.sTestStep = Vars.loopTestCases[i];
						execsteps(Vars,Vars.sTestStep,Vars.loopTestData[Vars.loopnum]);
					}
				}
				else{
					Vars.sTestStep = Vars.loopTestCases[i];
					execsteps(Vars,Vars.sTestStep,Vars.loopTestData[Vars.loopnum]);
				}
			}
		}
		Vars.loopnum--;
	}

	/*********************************************************************************************************
	 * Function:createConnection
	 * Description: This function is used to create connection with DataBase
	 /*********************************************************************************************************** 
	 */
	protected static String createConnection(LocalTC Vars) 
	{
		Log.info(" I am in createconnection");
		String url1=null;
		try {
			if(Vars.database.equalsIgnoreCase("mysql")){
				//String hostadd=  Vars.host_name.trim();
				//InetAddress ipaddr =InetAddress.getByName(Vars.Varshost_name.trim());
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://"+Vars.Varshost_name+":"+Vars.portnumber+"/"+Vars.schemaname;
				return url;
			}
			else if(Vars.database.equalsIgnoreCase("mssql")){
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				url1 = "jdbc:sqlserver://";
				String db = "databasename="+Vars.schemaname;
				String user = "user="+Vars.username;
				String pass  ="password="+Vars.password;
				String url =url1+Vars.host_name+";"+db+";"+user+";"+pass+";";
				Vars.con = DriverManager.getConnection(url);
				return url;
			}
			else{
				Log.info(Vars.database+" is not handeled as database in this version of  framework");
				Log.info("This framework in not handeled database ");
				return url1;
			}
		} catch (Exception e) {
			Log.info("Error Occured in update_Report Function "+e.getMessage());
		}
		return url1;
	}



	/**************************************************************************************************************
	 * Function=executeQuery
	 * Description=This function is used executeQuery from data base
	 /
	 * @throws IOException ****************************************************************************************************************
	 */

	protected static void executeQuery(ResultSet rs) throws IOException{
		String resultfile =null;
		File file = new File("Temp");
		boolean result=false;
		FileOutputStream fileOut = null;
		FileOutputStream fo = null;
		XSSFWorkbook workbook = null;
		if(!file.exists()){
			Log.info("Creating Directory - " + file);;
			try{
				file.mkdir();
				result = true;
			}catch(SecurityException se){
				Log.info("SecurityException Error Occured in executeQuery  " + se.getMessage());
			}
		}
		if(result){
			Log.info("Direcotry is already Created");
		}
		String workingDir = System.getProperty("user.dir");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		Date date = new Date();
		String CurrentDate= dateFormat.format(date);
		String filepath=workingDir+"\\"+"Temp"+"\\"+CurrentDate+"."+"xlsx";
		resultfile = filepath;
		String SheetName ="dbData";
		XSSFSheet ws; 
		try {
			fo = new FileOutputStream(filepath);
			workbook = new XSSFWorkbook(); 
			ws = workbook.createSheet(SheetName);
			FormulaEvaluator wbevaluator = workbook.getCreationHelper().createFormulaEvaluator();
			//if(update.equalsIgnoreCase("yes")){
			wbevaluator.evaluateAll();
			//}
			if (!resultfile.endsWith(".xlsx")){
				Log.info("Please correct Excel file extension, Data can be exported to only .xlsx file");
				Log.info("nofetchdata");
			}else{
				rs.beforeFirst(); 
				XSSFCell cell =null;
				ResultSetMetaData rsmd= (ResultSetMetaData) rs.getMetaData();
				int column = rsmd.getColumnCount();
				String columnname = rsmd.getColumnLabel(1);
				int rowcount=0;
				while(rs.next()){
					rowcount++;
				}
				rs.beforeFirst();
				int tempj=0;
				while(rs.next()){                      //Writing Column Name in excel
					XSSFRow row = ws.createRow(rowcount);
					for(int c =0;c<column;c++){
						cell = row.createCell(c);
						cell.setCellValue(rsmd.getColumnLabel(c+1)+0+" "+c);								
					}
				}
				rs.beforeFirst();
				int i=0;
				tempj=1;
				while(rs.next()){
					for(int c =0;c<column;c++){
						cell.setCellValue(rs.getString(c+1)+tempj+" "+c);
					}
					tempj++;
				}
				try {
					fileOut = new FileOutputStream(filepath);
					workbook.write(fileOut);
					workbook.close();
					fo.close();
				} catch (IOException e) {
					Log.info("IO Exception Occured in executeQuery -  while Writing data into Excel"+e.getMessage());
				}finally {
					if(null != fileOut){
						fileOut.close();
					}
				}
			}	
		} catch (SQLException e) {
			Log.info(" SQLException occured in executeQuery -  while connection with DB, check your credentials are correct "+e.getMessage());
		} catch (IOException e) {
			Log.info("IO Exception Occured in executeQuery -   "+e.getMessage());
		}finally {
			if(null != fo){
				fo.close();
			}if(workbook != null){
				workbook.close();
			}
		}
	}



	/*********************************************************************************************************
	 * Function:screenShot
	 * Description: This function is used to take the screen short.
	 /*********************************************************************************************************** 
	 */

	public static  void screenShot(LocalTC Vars) throws Exception {
		try {
			if(Vars.getScreenTestCaseID() != Vars.getTestCaseID()){
				Vars.setScreenTestCaseID(Vars.getTestCaseID());
				Vars.setScreenShotReport(null);
			}
			//int loopn =	Vars.getTestCaseID();
			int initscreenFlag = 1;
			String rown = Vars.getTestStepID() + "-" + Vars.row + "-" + Vars.act;
			String Sname = Vars.getTestCaseName().replaceAll("[|?\\/*:<>]", " ").trim();
			//if (!( Vars.getObj() != "alert" || Vars.getObj() != "dialog"))
			RemoteWebDriver driver = (RemoteWebDriver) Constant.driver;
			if (devFlag == 0) {
				screenshotflag = screenshotflag + 1;
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
				Date date = new Date();
				String strTime = dateFormat.format(date);
				if(Sname.contains("-"))
					Sname = Sname.substring(0, Sname.indexOf("-")).trim();
				else if(Sname.contains("."))
					Sname = Sname.substring(0, Sname.indexOf(".")).trim();
				//String ScreenFolder = Sname;
				//String screenReport = "";// = Constant.Path_ScreenShot;
				//String ScreenshotPath = "";// = screenReport  + Sname;

				if(null != Vars.getScreenShotReport())
					Vars.setScreenShotReport(Vars.getScreenShotReport().toLowerCase());
				if (null == Vars.getScreenShotReport()) {
					Vars.setScreenShotReport(Constant.Path_ScreenShot);
					initscreenFlag = 2;
				}
				if (Vars.getScreenShotReport().endsWith("\\") && !Vars.getScreenShotReport().contains(Vars.getScreenshotTimeStamp())) {
					Vars.setScreenShotReport(Vars.getScreenShotReport() + Vars.getScreenshotTimeStamp() + "\\");
					//Vars.setScreenShotReport(Vars.getScreenShotReport() + "\\" + Vars.getScreenshotTimeStamp() + "\\" + Vars.getScreenTestCaseID() + "\\");
				}
				if (!Vars.getScreenShotReport().endsWith("\\") && !Vars.getScreenShotReport().contains(Vars.getScreenshotTimeStamp())) {
					Vars.setScreenShotReport(Vars.getScreenShotReport() + "\\" + Vars.getScreenshotTimeStamp() + "\\");
					//Vars.setScreenShotReport(Vars.getScreenShotReport() + "\\" + Vars.getScreenshotTimeStamp() + "\\" + Vars.getScreenTestCaseID() + "\\");
				}
				if (!(Vars.getScreenShotReport().contains("screen"))) {
					Vars.setScreenShotReport(Vars.getScreenShotReport() + "Screenshot\\");
				}
				initscreen = initscreen + 1;
				if (initscreen == 2 && initscreenFlag == 2) {
					Log.info("Screenshots will be captured in the following path since the SCREENSHOTS PATH IS NOT GIVEN    " + Vars.getScreenShotReport()  );
				}

				else {
					if (initscreen == 2) {
						File f1s = new File(Vars.getScreenShotReport());
						if (!Vars.getScreenShotReport().substring(0,Vars.getScreenShotReport().length() - 1).
								equalsIgnoreCase(f1s.getAbsolutePath().toString()))
						{
							Log.info("Screenshots will be captured in the following path since the given SCREENSHOTS PATH IS NOT AVAILABLE   " +f1s.getAbsolutePath().toString() );
						}
						f1s.delete();
					}
				}
				Vars.filenamer = Vars.getScreenShotReport() + Sname + "_"
						+ screenshotflag + "_rowno_" + rown + "_"
						+ strTime + ".png";
				if (Vars.getScreenshotTypeFlag() == 0) {
					File screenshot = driver.getScreenshotAs(OutputType.FILE);
					FileUtils.copyFile(screenshot, new File(Vars.filenamer));

				} else {
					BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit
							.getDefaultToolkit().getScreenSize()));
					File outputfile = new File(Vars.filenamer);
					outputfile.mkdirs();
					ImageIO.write(image, "png", outputfile);
					Thread.sleep(3000);
				}
			}
			else { 
				Log.info("It is not for device..");
			}
		}
		catch (Exception e) {
			Log.info("Screenshot was not printed. Check the file path " + e.getMessage());
		}
	}

	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception e) {
			Log.info(" Exception Occured in isInteger- " +e.getMessage()); 
			return false;
		}
	}
	// Single Argument
	public static void Display(String Name)
	{
		Log.info(Name);
	}
	// three
	public static void Display(String Name,String Name2,String Name3)
	{
		Log.info(Name+ " "+Name2+ " "+Name3+ " ");
	}

	/*
	 * Method for getting connection with database
	 */
	protected static boolean getConnection(String url) {
		boolean parameter = false;
		SQLException errorString = null;
		try {
			if (Constant.Vars.database.equalsIgnoreCase("mssql")) {
				Constant.Vars.con = DriverManager.getConnection(url);
				Constant.Vars.stmt = Constant.Vars.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				Constant.Vars.rs = Constant.Vars.stmt.executeQuery(Constant.Vars.getObj());
				Constant.Vars.isconnected = true;
				return Constant.Vars.isconnected;
			} else if (Constant.Vars.database.equalsIgnoreCase("mysql")) {
				Log.info("In get connection part of MSSQL");
				Constant.Vars.con = DriverManager.getConnection(url, Constant.Vars.username, Constant.Vars.password);
				Constant.Vars.stmt = Constant.Vars.con.createStatement();
				Constant.Vars.rs = Constant.Vars.stmt.executeQuery(Constant.Vars.getObj());
				Constant.Vars.isconnected = true;
				return Constant.Vars.isconnected;
			}
			return Constant.Vars.isconnected;
		} catch (SQLException e) {
			errorString = e;
			Log.info(
					"Connection to Database is failed, please verify connection URL parameters which are set in Selenium Utility File  "
							+ e.getMessage());
			Log.info(
					"Connection to Database is failed, please verify connection URL parameters which are set in Selenium Utility File  "
							+ e.getMessage());
			return Constant.Vars.isconnected;
		}
	}
	/**
	 * @param Vars
	 * Method for setting Object for the test steps, which is use to create report for test steps
	 */
	public static void setReportVariables(LocalTC Vars){
		ReporterObject reporterObj = new ReporterObject();
		reporterObj.setReporterObjectTestCaseId(Vars.getTestCaseID());
		reporterObj.setReportObjectTestStepId(Vars.getTestStepID());
		reporterObj.setReportTestStepID(Vars.getTestStepID() + "_" + Vars.row  + "-" + Vars.act);
		if (!Vars.sTestStep.contains("crypted"))
			reporterObj.setReportAction(Vars.getAction() + " " + Vars.getEvent() + " " + Vars.getTestdata());
		else
			reporterObj.setReportAction(Vars.getAction() + " " + Vars.getEvent());
		reporterObj.setReportCCellData(Vars.getObj() + " " + Vars.getObjProp());
		reporterObj.setReportDCellData(Vars.ExecutionResult);
		reporterObj.setReportComments(Vars.sTestStep);
		Date exec_time = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		reporterObj.setReportStrTime(dateFormat.format(exec_time));
		reporterObj.setReportRes_type(Vars.getRes_type());
		reporterObj.setReportException(Vars.getExceptionVar());
		reporterObj.setReportObjectEvent(Vars.getEvent().toLowerCase());
		reporterObj.setReportObjectTestData(Vars.getTestdata().replace("\"", ""));
		reporterObj.setReporterScreenFile(Vars.filenamer);
		Vars.reporterObjectList.add(reporterObj);
	}
	/**
	 * @param csvValues
	 */
	public static void setReportVarForTestComplete(String[] csvValues){
		ReporterObject reporterObj = new ReporterObject();
		if(csvValues[0].equalsIgnoreCase("null")){
			csvValues[0] = "0";
		}else if(csvValues[1].equalsIgnoreCase("null")){
			csvValues[1] = "0";
		}else if(csvValues[2].equalsIgnoreCase("null")){
			csvValues[2] = "";
		}else if(csvValues[3].equalsIgnoreCase("null")){
			csvValues[3] = "";
		}else if(csvValues[4].equalsIgnoreCase("null")){
			csvValues[4] = "";
		}else if(csvValues[5].equalsIgnoreCase("null")){
			csvValues[5] = "";
		}else if(csvValues[6].equalsIgnoreCase("null")){
			csvValues[6] = "";
		}else if(csvValues[7].equalsIgnoreCase("null")){
			csvValues[7] = "";
		}else if(csvValues[8].equalsIgnoreCase("null")){
			csvValues[8] = "";
		}else if(csvValues[9].equalsIgnoreCase("null")){
			csvValues[9] = "";
		}else if(csvValues[10].equalsIgnoreCase("null")){
			csvValues[10] = "";
		}else if(csvValues[11].equalsIgnoreCase("null")){
			csvValues[11] = "";
		}else if(csvValues[12].equalsIgnoreCase("null")){
			csvValues[12] = "";
		}else if(csvValues[13].equalsIgnoreCase("null")){
			csvValues[13] = "";
		}else if(csvValues[14].equalsIgnoreCase("null")){
			csvValues[14] = "";
		}else if(csvValues[15].equalsIgnoreCase("null")){
			csvValues[15] = "";
		}
		reporterObj.setReporterObjectTestCaseId(Integer.parseInt(csvValues[1]));
		reporterObj.setReportTestStepID(csvValues[2] + "_" +csvValues[16]);
		reporterObj.setReportObjectTestStepId(Integer.parseInt(csvValues[2]));
		if (!csvValues[2].contains("crypted"))
			reporterObj.setReportAction(csvValues[6] + " " + csvValues[9] + " " + csvValues[10]);
		else
			reporterObj.setReportAction(csvValues[6] + " " + csvValues[9]);
		reporterObj.setReportCCellData(csvValues[7] + " " + csvValues[8]);
		reporterObj.setReportDCellData(csvValues[11]);
		reporterObj.setReportComments(csvValues[5]);
		reporterObj.setReportStrTime(csvValues[13]);
		reporterObj.setReportRes_type(csvValues[15]);
		reporterObj.setReportObjectEvent(csvValues[9].toLowerCase());
		reporterObj.setReportObjectTestData(csvValues[10].replace("\"", ""));
		reporterObj.setReporterScreenFile(csvValues[14]);
		Constant.Vars.reporterObjectList.add(reporterObj);
		Constant.Vars.ExecutionStatus = Constant.Vars.ExecutionStatus + csvValues[12] + ",";
		Constant.Vars.setTestSetName(csvValues[3]);
		Constant.Vars.setTestCaseName(csvValues[4]);
		Constant.Vars.setTestCaseID(Integer.parseInt(csvValues[1]));
		Constant.Vars.setSpiraTestSetId(Integer.parseInt(csvValues[0]));
	}
}