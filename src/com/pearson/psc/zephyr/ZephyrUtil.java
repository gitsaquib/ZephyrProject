package com.pearson.psc.zephyr;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.thed.service.soap.wsdl.RemoteCriteria;
import com.thed.service.soap.wsdl.RemoteFieldValue;
import com.thed.service.soap.wsdl.RemoteReleaseTestSchedule;
import com.thed.service.soap.wsdl.RemoteRepositoryTree;
import com.thed.service.soap.wsdl.RemoteRepositoryTreeTestcase;
import com.thed.service.soap.wsdl.RemoteRequirement;
import com.thed.service.soap.wsdl.RemoteTestResult;
import com.thed.service.soap.wsdl.RemoteTestcase;
import com.thed.service.soap.wsdl.SearchOperation;
import com.thed.service.soap.wsdl.ZephyrServiceException;
import com.thed.service.soap.wsdl.ZephyrSoapService;
import com.thed.service.soap.wsdl.ZephyrSoapService_Service;

public class ZephyrUtil { 

	private static String strURL = "http://pearson.yourzephyr.com/flex/services/soap/zephyrsoapservice-v1?wsdl";
	public static ZephyrSoapService client; 
	private static String username = "vsaqumo"; 
	private static String password = "shaad@10"; 
	public static String token = new String(); 
	static String tcReleaseId = "5"; 
	static String tcPhaseFolder = "DevZone"; 
	public static RemoteFieldValue fieldValue = new RemoteFieldValue();
	public static List<RemoteFieldValue> values = new ArrayList<RemoteFieldValue>();
	public static List<RemoteCriteria> rcList = new ArrayList<RemoteCriteria>(); 
	public static RemoteCriteria remoteCriteria = new RemoteCriteria(); 
	public static RemoteCriteria remoteCriteria2 = new RemoteCriteria(); 
	public static RemoteRepositoryTreeTestcase treeTestcase = new RemoteRepositoryTreeTestcase(); 
	public static RemoteTestcase testcase = new RemoteTestcase(); 
	public static List<RemoteRepositoryTree> treeList = new ArrayList<RemoteRepositoryTree>(); 
	public static RemoteRepositoryTree tree = new RemoteRepositoryTree(); 
	List<RemoteFieldValue> response = new ArrayList<RemoteFieldValue>(); 

	public static void startZephyrService() { 
		try{ 
			final URL WSDL_URL = new URL(strURL); 
			ZephyrSoapService_Service serviceWithUrl = new ZephyrSoapService_Service(WSDL_URL, new QName("http://soap.service.thed.com/", "ZephyrSoapService")); 
			client = serviceWithUrl.getZephyrSoapServiceImplPort(); 
		} catch (MalformedURLException ex) { 
			throw new RuntimeException (ex); 
		} 
	} 

	public static void loginProcess() throws ZephyrServiceException { 
		startZephyrService(); 
		token = client.login(username, password); 
		if(token == null) 
			System.out.println("Login Failed!"); 
		else 
			System.out.println("Successfully Logged In. Your Token is: " + token); 
	} 

	public static void logoutProcess() throws ZephyrServiceException { 
		client.logout(token); 
		System.out.println("\n" + "This session has ended."); 
	} 

	
	public static void main(String[] args){ 
		try {
			loginProcess();
			//updateTestResult(1L, "TC21956");
			updateTestRequirement("TC53107");
			logoutProcess(); 
		} catch (ZephyrServiceException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} 
		
	}
	
	private static String getTestSchedulesByCriteria(String testcaseId) throws ZephyrServiceException {
		remoteCriteria.setSearchName("tcrTreeTestcase.testcase.id"); 
		remoteCriteria.setSearchOperation(SearchOperation.EQUALS); 
		remoteCriteria.setSearchValue(testcaseId); 
		rcList.add(remoteCriteria);
		List<RemoteReleaseTestSchedule> scheduleList = client.getTestSchedulesByCriteria(rcList, false, token);
		RemoteReleaseTestSchedule releaseTestSchedule = scheduleList.get(0);
		return Long.toString(releaseTestSchedule.getTestScheduleId());
	}
	
	private static RemoteRepositoryTreeTestcase getTestCaseCriteria(String rallyId) throws ZephyrServiceException {
		remoteCriteria.setSearchName("testcase.externalId"); 
		remoteCriteria.setSearchOperation(SearchOperation.EQUALS); 
		remoteCriteria.setSearchValue(rallyId); 
		rcList.add(remoteCriteria);
		List<RemoteRepositoryTreeTestcase> testcaseObj = client.getTestcasesByCriteria(rcList, false, token);
		if(null != testcaseObj && !testcaseObj.isEmpty()) {
			return testcaseObj.get(0);
		} else {
			return null;
		}
	}
	
	public static void retrieveCustomFieldsTestCase(long testcaseId) throws ZephyrServiceException {
		RemoteRepositoryTreeTestcase testCase = client.getTestcaseById(testcaseId, token);
		System.out.println(testCase.getTestcase().getExternalId());
	}
	
	private static void updateTestResult(long userId, String testcaseId) throws ZephyrServiceException {
		RemoteTestResult result = new RemoteTestResult();
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date2 = null;
		try {
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		RemoteRepositoryTreeTestcase testCase = getTestCaseCriteria(testcaseId);
		if(null != testCase) {
			RemoteTestcase remoteTestcase = testCase.getTestcase();
			result.setExecutionDate(date2);
			result.setId(remoteTestcase.getId());
			result.setTesterId(userId);
			result.setExecutionStatus("1");
			result.setReleaseTestScheduleId(getTestSchedulesByCriteria(remoteTestcase.getId().toString()));
			List<RemoteTestResult> list = new ArrayList<RemoteTestResult>();
			list.add(result);
			client.updateTestStatus(list, token);
		}
	}
	
	private static void updateTestRequirement(String testcaseId) throws ZephyrServiceException {
		RemoteRepositoryTreeTestcase testCase = getTestCaseCriteria(testcaseId);
		if(null != testCase) {
			RemoteRequirement requirement = client.getRequirementById(77L, token);
			List<Long> testCases = requirement.getTestcaseIds();
			testCases.add(testCase.getId());
			fieldValue.setKey("testcases");
			fieldValue.setValue((java.util.ArrayList<java.lang.Long>)testCases);
			values.add(fieldValue);
			client.updateRequirement(77L, values, token);
		}
	}
	
	/* 
	* Create New Testcase In Zephyr via API 
	*/ 
	private  void createTestCase() throws ZephyrServiceException {
		//Search criteria to look for all repository trees//phases in release 2 
		remoteCriteria.setSearchName("releaseId"); 
		remoteCriteria.setSearchOperation(SearchOperation.EQUALS); 
		remoteCriteria.setSearchValue(tcReleaseId); 
		//Search criteria to additionally look for all trees//phases named DevZone (in the above release 2) 
		remoteCriteria2.setSearchName("name"); 
		remoteCriteria2.setSearchOperation(SearchOperation.EQUALS); 
		remoteCriteria2.setSearchValue(tcPhaseFolder); 

		//Adding the search criteria to the Remote Criteria list 
		rcList.add(remoteCriteria); 
		rcList.add(remoteCriteria2); 


		treeList = client.getTestCaseTreesByCriteria(rcList, false, token); 
		//If none are found or more than one tree is returned, you will need to write code to handle that 
		tree = treeList.get(0); 

		//Enter field information for the testcase 
		testcase.setName("This Testcase was created via API"); 
		testcase.setComments("Created via SOAP API!"); 
		testcase.setAutomated(false); 
		testcase.setExternalId("99999"); 
		testcase.setPriority("1"); 
		testcase.setTag("API"); 
		//Set release the testcase will be located at 
		testcase.setReleaseId(tree.getReleaseId()); 

		//Repository trees//phases Id used to link the new testcase to a tree//phase 
		treeTestcase.setRemoteRepositoryId(tree.getId()); 
		//Adding test steps to the testcase 
		treeTestcase.setTestSteps("<steps maxId=\"3\"><step id=\"1\" orderId=\"1\" detail=\"Test Step 1\" data=\"Test Data \" result=\"Excepted Results \" />" + 
		"<step id=\"2\" orderId=\"2\" detail=\"Test Step T2 \" data=\"\" result=\"\" />" + 
		"<step id=\"3\" orderId=\"3\" detail=\"Test Step T3 -Test\" data=\"\" result=\"\" /></steps>"); 
		//Loading the testcase object I changed just above into the remoteRepositoryTreeTestcase object 
		treeTestcase.setTestcase(testcase); 

		//Actually creating the new TC here 
		//With bulk testcases you may want to use loops and list arrays 
		//Response from Zephyr will give a list of 2 values; (0)-TCR Tree Testcase ID, (1)-Testcase ID (The one you see in the UI) 
		List<RemoteFieldValue> response = client.createNewTestcase(treeTestcase, token); 

		System.out.println("\n" + "Testcase Created!"); 
		System.out.println(response.get(0).getKey() + ": " + response.get(0).getValue()); 
		System.out.println(response.get(1).getKey() + ": " + response.get(1).getValue());
	} 
}