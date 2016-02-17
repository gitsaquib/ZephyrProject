package com.pearson.psc.zephyr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.thed.service.soap.wsdl.ZephyrServiceException;

public class ZephyrMain {

	private static ZephyrUtil util = null;
	private static Long userId = null;
	private static String rootFolder = "D:\\Regression";
	public static void main(String[] args){ 
		try {
			util = new ZephyrUtil();
			Configuration configuration = util.readConfigFile();
			System.out.println("Start time:"+new Date());
			util.loginProcess(configuration.getUrl(), configuration.getUsername(), configuration.getPassword());
			userId = util.retrieveUserId("mohammed.saquib@pearson.com");
			updateResultsInZephyr();
			System.out.println("End time:"+new Date());
			util.logoutProcess(); 
		} catch (ZephyrServiceException e) {
			System.out.println("1. "+e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("2. "+e.getMessage());
			e.printStackTrace();
		} 
	}
	
	private static void updateResultsInZephyr() throws FileNotFoundException, ZephyrServiceException {
		File inFolder = new File(rootFolder+File.separator+"in"+File.separator);
    	FilenameFilter fileNameFilter = new FilenameFilter() {
 		   
            @Override
            public boolean accept(File dir, String name) {
               if(name.lastIndexOf('.')>0)
               {
                  int lastIndex = name.lastIndexOf('.');
                  String str = name.substring(lastIndex);
                  if(str.equals(".txt"))
                  {
                     return true;
                  }
               }
               return false;
            }
        };
    	File files[] = inFolder.listFiles(fileNameFilter);
    	for(File file:files) {
    		Scanner sc=new Scanner(new FileReader(file));
    		List<TestResult> testResults = new ArrayList<TestResult>();
    		while (sc.hasNextLine()){
    			TestResult testResult = new TestResult();
	        	String testCaseDetails = sc.nextLine();
	        	String details[] = testCaseDetails.split("\t");
	        	if(details.length < 3) {
	        		System.out.println("Three columns are not filled for "+testCaseDetails);
	        		System.exit(0);
	        	}
        		String status = "1";
        		if(null != details[1] && details[1].equalsIgnoreCase("Fail")) {
        			status = "2";
        		}
        		if(null != details[1] && details[1].equalsIgnoreCase("Blocked")) {
        			status = "4";
        		}
        		testResult.setStatus(status);
        		testResult.setTestCaseId(details[0]);
        		testResult.setToolType(details[2]);
        		testResults.add(testResult);
	        }
	        sc.close();
	        util.updateTestResult(userId, testResults);
	        file.renameTo(new File(file.getAbsolutePath().replace("txt", "done")));
    	}
	}
}
