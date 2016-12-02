package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Utils {


	public static String getTestCaseName(String sTestCase)throws Exception{

		String value = sTestCase;

		try{

			int posi = value.indexOf("@");
			value = value.substring(0, posi);
			posi = value.lastIndexOf(".");	
			value = value.substring(posi + 1);
			return value;
				}catch (Exception e){
			Log.error("Class Utils | Method getTestCaseName | Exception desc : "+e.getMessage());
			throw (e);
					}
			}

	 public static void mouseHoverAction(WebElement mainElement, String subElement){
		 //Actions action = new Actions(driver);
        // action.moveToElement(mainElement).perform();
         if(subElement.equals("Accessories")){
        	 Log.info("Accessories link is found under Product Category");
         }
         if(subElement.equals("iMacs")){
        	 Log.info("iMacs link is found under Product Category");
         }
         if(subElement.equals("iPads")){
        	 Log.info("iPads link is found under Product Category");
         }
         if(subElement.equals("iPhones")){
        	 Log.info("iPhones link is found under Product Category");
         }
         Log.info("Click action is performed on the selected Product Type");
	 }

	 public static void waitForElement(WebElement element){
		 WebDriverWait wait = new WebDriverWait(Constant.driver, 10);
	     wait.until(ExpectedConditions.elementToBeClickable(element));
	 	}

	 public static void takeScreenshot(WebDriver driver, String sTestCaseName) throws Exception{

			try{

				File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);

				FileUtils.copyFile(scrFile, new File(Constant.Path_ScreenShot + sTestCaseName +".jpg"));	

			} catch (Exception e){

				Log.error("Class Utils | Method takeScreenshot | Exception occured while capturing ScreenShot : "+e.getMessage());

				throw new Exception();

			}

		}
	 
	 public static String htmlToTextConvertMethod(String readLine){
		 StringBuilder strBuilder = new StringBuilder();
		 String strInLine[] = readLine.split("\n");
		 for(int i=0; i<strInLine.length; i++){
			 strInLine[i] = strInLine[i].replaceAll("&nbsp;","");
			 String temptext = Jsoup.parse(strInLine[i].trim()).text();
			 strBuilder.append(temptext+"\n");
		 }
			return strBuilder.toString();
		}
	 
		public static boolean isDate(String input) {
			try {
				long datedata = Date.parse(input);
				return true;
			} catch (Exception e) {
				//Log.error(Level.SEVERE, " Exception Occured in isInteger- " +e.getMessage()); 
				return false;
			}
		}
		public static boolean isInteger(String input) {
			try {
				Integer.parseInt(input);
				return true;
			} catch (Exception e) {
				//Log.error(Level.SEVERE, " Exception Occured in isInteger- " +e.getMessage()); 
				return false;
			}
		}
		
		
		/**
		 * @param object
		 * @param param
		 * @return
		 * @throws IOException
		 * Method for autocorrecting spelling mistake of test step and object in object repository
		 */
	public static String spellCheckAndCorrect(String object, String param) throws IOException {
		String[] strTestStep = new String[30];
		String strTestStepSpell = "";
		String line = "";
		int i = 0;
		BufferedReader br = null;
		Process process = null;
		try {

			if (param.equalsIgnoreCase(Constant.Keyword)) {
				strTestStep = object.split(" ");

				for (String strTestStepSplit : strTestStep) {
					process = Runtime.getRuntime().exec(Constant.auroCorrect + " " + strTestStepSplit);
					br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					while ((line = br.readLine()) != null) {

						if (i == 0)
							strTestStepSpell = line;

						else
							strTestStepSpell = strTestStepSpell + " " + line;
						i++;
					}
				}
				Log.info("Corrected Sentence: " + strTestStepSpell);

			} else if (param.equalsIgnoreCase("Execute")) {
				process = Runtime.getRuntime().exec(Constant.auroCorrect + " " + object);
				br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = br.readLine()) != null) {
					strTestStepSpell = line;
				}
				Log.info("Corrected Sentence: " + strTestStepSpell);
			}

		} catch (Exception e) {
			Log.info("Correction is failed: " + e.getMessage());
		} finally {
			Runtime.getRuntime().exec("taskkill " + process);
			if (br != null)
				br.close();
		}
		return strTestStepSpell;
	}
	
	public static boolean isAlertPresent()
	{
		try{
			Constant.driver.switchTo().alert();
			return true;
		}
		catch(NoAlertPresentException ex){
			return false;
		}
		
	}
		
	}