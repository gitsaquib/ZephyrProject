package com.pearson.psc.zephyr;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.thed.service.soap.wsdl.RemoteCriteria;
import com.thed.service.soap.wsdl.RemoteReleaseTestSchedule;
import com.thed.service.soap.wsdl.RemoteRepositoryTreeTestcase;
import com.thed.service.soap.wsdl.RemoteTestResult;
import com.thed.service.soap.wsdl.RemoteTestcase;
import com.thed.service.soap.wsdl.RemoteUser;
import com.thed.service.soap.wsdl.SearchOperation;
import com.thed.service.soap.wsdl.ZephyrServiceException;
import com.thed.service.soap.wsdl.ZephyrSoapService;
import com.thed.service.soap.wsdl.ZephyrSoapService_Service;

public class ZephyrUtil { 

	public static ZephyrSoapService client; 
	public static String token = new String(); 
	public static List<RemoteCriteria> rcList = new ArrayList<RemoteCriteria>(); 
	public static RemoteCriteria remoteCriteria = new RemoteCriteria(); 

	public void startZephyrService(String url) { 
		try{ 
			final URL WSDL_URL = new URL(url); 
			ZephyrSoapService_Service serviceWithUrl = new ZephyrSoapService_Service(WSDL_URL, new QName("http://soap.service.thed.com/", "ZephyrSoapService")); 
			client = serviceWithUrl.getZephyrSoapServiceImplPort(); 
		} catch (MalformedURLException ex) { 
			throw new RuntimeException (ex); 
		} 
	} 
	
	public Long retrieveUserId(String username) throws ZephyrServiceException {
		List<RemoteUser> users = client.getUsersByCriteria(rcList, false, token);
		for(RemoteUser user:users) {
			if(user.getEmail().equalsIgnoreCase(username)) {
				return user.getId();	
			}
		}
		return null;
	}

	public void loginProcess(String url, String username, String password) throws ZephyrServiceException { 
		startZephyrService(url); 
		token = client.login(username, password); 
		if(token == null) 
			System.out.println("Login Failed!"); 
		else 
			System.out.println("Successfully Logged In. Your Token is: " + token); 
	} 

	public void logoutProcess() throws ZephyrServiceException { 
		client.logout(token); 
		System.out.println("\n" + "This session has ended."); 
	} 
	
	private String getTestSchedulesByCriteria(String testcaseId) {
		remoteCriteria.setSearchName("tcrTreeTestcase.testcase.id"); 
		remoteCriteria.setSearchOperation(SearchOperation.EQUALS); 
		remoteCriteria.setSearchValue(testcaseId); 
		rcList.add(remoteCriteria);
		List<RemoteReleaseTestSchedule> scheduleList;
		try {
			scheduleList = client.getTestSchedulesByCriteria(rcList, false, token);
			RemoteReleaseTestSchedule releaseTestSchedule = scheduleList.get(scheduleList.size()-1);
			return Long.toString(releaseTestSchedule.getTestScheduleId());
		} catch (Exception e) {
			return null;
		}
	}
	
	private RemoteRepositoryTreeTestcase getTestCaseCriteria(String rallyId){
		remoteCriteria.setSearchName("testcase.externalId"); 
		remoteCriteria.setSearchOperation(SearchOperation.EQUALS); 
		remoteCriteria.setSearchValue(rallyId); 
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
	
	public Configuration readConfigFile(){
    	Properties prop = new Properties();
    	File file = new File("config.properties");
    	InputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			System.out.println("1. Unable to load config properties");
		}
    	try {
    		Configuration configuration = new Configuration();
			prop.load(stream);
			configuration.setUrl(prop.getProperty("URL"));
			configuration.setUsername(prop.getProperty("USERNAME"));
			configuration.setPassword(prop.getProperty("PASSWORD"));
			return configuration;
    	} catch(Exception e) {
    		System.out.println("2. Unable to load config properties");
    	}
    	return null;
	}
	
	public void updateTestResult(long userId, List<TestResult> testResults) throws ZephyrServiceException {
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date2 = null;
		try {
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		List<RemoteTestResult> list = new ArrayList<RemoteTestResult>();
		for(TestResult testResult:testResults) {
			System.out.println(testResult.getTestCaseId()+" - "+testResult.getToolType());
			if(testResult.getToolType().equalsIgnoreCase("Rally")) {
				RemoteRepositoryTreeTestcase testCase = getTestCaseCriteria(testResult.getTestCaseId());
				if(null != testCase) {
					RemoteTestResult result = new RemoteTestResult();
					RemoteTestcase remoteTestcase = testCase.getTestcase();
					result.setExecutionDate(date2);
					result.setId(remoteTestcase.getId());
					result.setTesterId(userId);
					result.setExecutionStatus(testResult.getStatus());
					String schId = "6970";// getTestSchedulesByCriteria(remoteTestcase.getId().toString());
					if(null != schId) {
						result.setReleaseTestScheduleId(schId);
						list.add(result);	
					}
				}
			} else if(testResult.getToolType().equalsIgnoreCase("Zephyr")) {
				RemoteTestResult result = new RemoteTestResult();
				result.setExecutionDate(date2);
				result.setId(Long.parseLong(testResult.getStatus()));
				result.setTesterId(userId);
				result.setExecutionStatus(testResult.getStatus());
				String schId = getTestSchedulesByCriteria(testResult.getTestCaseId());
				if(null != schId) {
					result.setReleaseTestScheduleId(schId);
					list.add(result);	
				}
			}
		}
		if(list.size() > 0) {
			client.updateTestStatus(list, token);
		}
	}
}