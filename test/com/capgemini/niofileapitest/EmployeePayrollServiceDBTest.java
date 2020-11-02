package test.com.capgemini.niofileapitest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import main.com.capgemini.employeepayrollmain.EmployeePayrollService;
import main.com.capgemini.employeepayrollmain.EmployeePayrollService.IOService;
import main.com.capgemini.employeepayrollmain.DatabaseServiceException;
import main.com.capgemini.employeepayrollmain.EmployeePayrollData;

public class EmployeePayrollServiceDBTest {
	
	private static Logger log = Logger.getLogger(EmployeePayrollServiceDBTest.class.getName());

	// Test employee count in database
	@Test
	public void testNormalisedDBReadEmployeeData() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		Assert.assertEquals(6, employeePayrollData.size());
	}

	// Test update salary
	@Test
	public void testNormaliseDBUpdateSalary() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Terissa", 3000000.00);
		boolean result = employeePayrollService.checkEmployeePayrollSyncWithDB("Terissa");
		Assert.assertTrue(result);
	}

	// Get employee for given date range
	@Test
	public void testEmployeeCountNormaliseDBForDateRange() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		LocalDate start = LocalDate.of(2018, 01, 01);
		LocalDate end = LocalDate.now();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService
				.readEmployeePayrollDataForDateRange(IOService.DB_IO, start, end);
		Assert.assertEquals(5, employeePayrollData.size());
	}

	// Get average salary for employee
	@Test
	public void testAverageSalaryByGenderNormaliseDB() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		Map<String, Double> employeePayrollData = employeePayrollService.readAverageSalaryByGender(IOService.DB_IO);
		Assert.assertTrue(
				employeePayrollData.get("M").equals(1350000.00) && employeePayrollData.get("F").equals(3000000.00));
	}

	// Inserting employee to DB
	@Test
	public void testAddEmployeeToDatabase() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayroll("Mark", "M", 5000000.00, LocalDate.now(), 106, "Dep4", 1004,
				"Company4");
		boolean result = employeePayrollService.checkEmployeePayrollSyncWithDB("Mark");
		Assert.assertTrue(result);
	}

	// Removing employee from DB
	@Test
	public void testRemoveEmployeeFromPayroll() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		List<EmployeePayrollData> employeePayrollData = employeePayrollService
				.readEmployeePayrollDataForActiveEmployees(IOService.DB_IO);
		Assert.assertEquals(5, employeePayrollData.size());
	}

	// Adding multiple employees to DB
	@Test
	public void testAddMultipleEmployeeToDB() throws DatabaseServiceException {
		EmployeePayrollData[] arrayOfEmployee = {
				new EmployeePayrollData(0, "Jeff","M",100000.0,LocalDate.now()),
				new EmployeePayrollData(0, "Bill", "M", 200000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Mark", "M", 300000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Mukesh", "M", 400000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Suresh", "M", 500000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Ram", "M", 600000.0, LocalDate.now())
		};
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.addEmployeeToPayroll(Arrays.asList(arrayOfEmployee));
		Instant end = Instant.now();
		log.info("Duration without thread : "+Duration.between(start, end));
		Assert.assertEquals(12, employeePayrollService.countEntries(IOService.DB_IO));
	}
}
