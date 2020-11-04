package test.com.capgemini.niofileapitest;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import io.restassured.*;
import io.restassured.response.*;
import io.restassured.specification.RequestSpecification;
import main.com.capgemini.employeepayrollmain.DatabaseServiceException;
import main.com.capgemini.employeepayrollmain.EmployeePayrollData;
import main.com.capgemini.employeepayrollmain.EmployeePayrollService;
import main.com.capgemini.employeepayrollmain.EmployeePayrollService.IOService;

public class EmployeePayrollServiceJSONServerTest {

	@Before
	public void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	// Getting employees from json server
	public EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		EmployeePayrollService.log.info("Employee payroll entries in JSON Server :\n" + response.asString());
		EmployeePayrollData[] arrayOfEmployees = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmployees;
	}

	public Response addEmployeeToJsonServer(EmployeePayrollData employeePayrollData) {
		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}

	// Employees retrieved from json server should match count
	@Test
	public void givenEmployeeInJSONServer_WhenRetreieved__ShouldMatchCount() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService;
		EmployeePayrollData[] arrayOfEmployees = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployees));
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(6, entries);
	}

	// Add new employee should match status code and count
	@Test
	public void givenNewEmployee_WhenAdded_ShouldMatchStatusCodeAndCount() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService;
		EmployeePayrollData[] arrayOfEmployees = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployees));
		EmployeePayrollData employeePayrollData = null;
		employeePayrollData = new EmployeePayrollData(23, "Tiger", "M", 3000000.00, LocalDate.now());
		Response response = addEmployeeToJsonServer(employeePayrollData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(201, statusCode);
		employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
		employeePayrollService.addEmployeeToPayroll(employeePayrollData, IOService.REST_IO);
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(6, entries);
	}

	// Add multiple employees to server, match status code and count
	@Test
	public void givenListOfEmployees_WhenAdded_ShouldMatchStatusCodeAndCount() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService;
		EmployeePayrollData[] arrayOfEmployeesFromServer = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployeesFromServer));
		EmployeePayrollData[] arrayOfEmployees = {
				new EmployeePayrollData(27, "Sasuke", "M", 50000.00, LocalDate.now()),
				new EmployeePayrollData(28, "Itachi", "M", 60000.00, LocalDate.now()),
				new EmployeePayrollData(29, "Sarada", "F", 45000.00, LocalDate.now()) };
		for (EmployeePayrollData employeePayrollData : arrayOfEmployees) {
			Response response = addEmployeeToJsonServer(employeePayrollData);
			int statusCode = response.getStatusCode();
			Assert.assertEquals(201, statusCode);
			employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
			employeePayrollService.addEmployeeToPayroll(employeePayrollData, IOService.REST_IO);
		}
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}
	
	// Update salary for an employee on server
	@Test
	public void givenSalaryForEmployee_WhenUpdated_ShouldMatchStatusCode() {
		EmployeePayrollService employeePayrollService;
		EmployeePayrollData[] arrayOfEmployees = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployees));
		employeePayrollService.updateEmployeeSalary("Jeff Bezos", 6000000.0, IOService.REST_IO);
		EmployeePayrollData employeePayrollData = employeePayrollService.getEmployeePayrollData("Jeff Bezos");
		String employeeJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(employeeJson);
		Response response = request.put("/employees/" + employeePayrollData.id);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
	}

}
