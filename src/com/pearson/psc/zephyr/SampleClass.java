package com.pearson.psc.zephyr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.xml.namespace.QName;

import org.apache.cxf.wsdl.http.UrlEncoded;

import com.thed.service.soap.wsdl.RemoteCriteria;
import com.thed.service.soap.wsdl.RemoteRepositoryTreeTestcase;
import com.thed.service.soap.wsdl.RemoteTestcase;
import com.thed.service.soap.wsdl.SearchOperation;
import com.thed.service.soap.wsdl.ZephyrServiceException;
import com.thed.service.soap.wsdl.ZephyrSoapService;
import com.thed.service.soap.wsdl.ZephyrSoapService_Service;

public class SampleClass {
	
	public static ZephyrSoapService client; 
	public static String token = new String();
	public static List<RemoteCriteria> rcList = new ArrayList<RemoteCriteria>();
	public static RemoteCriteria remoteCriteria = new RemoteCriteria();
	private static String rootFolder = "D:\\Regression";
	private static SampleClass util = null;
	
	public static void main(String[] args){ 
		try {
			util = new SampleClass();
			System.out.println("Start time:"+new Date());
			util.loginProcess("http://pearson.yourzephyr.com/flex/services/soap/zephyrsoapservice-v1?wsdl", 
					"vsaqumo", "shaad@10");
			RemoteRepositoryTreeTestcase remoteRepositoryTreeTestcase = util.getTestCaseCriteria("TC43861");
        	if(null != remoteRepositoryTreeTestcase) {
	    		RemoteTestcase  remoteTestcase = remoteRepositoryTreeTestcase.getTestcase();
	    		System.out.println(remoteTestcase.getId());
        	}
			System.out.println("End time:"+new Date());
			util.logoutProcess(); 
		} catch (Exception e) {
			System.out.println("1. "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void retriveTestCases() throws FileNotFoundException, ZephyrServiceException {
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
    		while (sc.hasNextLine()){
	        	String testCaseDetails = sc.nextLine();
	        	String details[] = testCaseDetails.split("\t");
	        	RemoteRepositoryTreeTestcase remoteRepositoryTreeTestcase = util.getTestCaseCriteria("TC45932");
	        	if(null != remoteRepositoryTreeTestcase) {
		    		RemoteTestcase  remoteTestcase = remoteRepositoryTreeTestcase.getTestcase();
		    		System.out.println(remoteTestcase.getId());
	        	}
	        }
	        sc.close();
	        file.renameTo(new File(file.getAbsolutePath().replace("txt", "done")));
    	}
	}
	
	private RemoteRepositoryTreeTestcase getTestCase(Long id) throws ZephyrServiceException{
		return client.getTestcaseById(id, token);
	}
	
	private RemoteRepositoryTreeTestcase getTestCaseCriteria(String altId){
		remoteCriteria.setSearchName("testcase.externalId"); 
		remoteCriteria.setSearchOperation(SearchOperation.EQUALS); 
		remoteCriteria.setSearchValue(altId); 
		rcList.add(remoteCriteria);
		List<RemoteRepositoryTreeTestcase> testcaseObj;
		try {
			testcaseObj = client.getTestcasesByCriteria(rcList, false, token);
			if(null != testcaseObj && !testcaseObj.isEmpty()) {
				return testcaseObj.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	public void logoutProcess() throws ZephyrServiceException { 
		client.logout(token); 
		System.out.println("\n" + "This session has ended."); 
	} 
	
	public void startZephyrService(String url) { 
		try{ 
			final URL WSDL_URL = new URL(url); 
			ZephyrSoapService_Service serviceWithUrl = new ZephyrSoapService_Service(WSDL_URL, new QName("http://soap.service.thed.com/", "ZephyrSoapService")); 
			client = serviceWithUrl.getZephyrSoapServiceImplPort(); 
		} catch (MalformedURLException ex) { 
			throw new RuntimeException (ex); 
		} 
	} 
	
	public void loginProcess(String url, String username, String password) throws ZephyrServiceException { 
		startZephyrService(url); 
		token = client.login(username, password); 
		if(token == null) 
			System.out.println("Login Failed!"); 
		else 
			System.out.println("Successfully Logged In. Your Token is: " + token); 
	} 
}
