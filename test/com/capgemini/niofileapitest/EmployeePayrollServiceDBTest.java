package test.com.capgemini.niofileapitest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import main.com.capgemini.employeepayrollmain.EmployeePayrollService;
import main.com.capgemini.employeepayrollmain.EmployeePayrollService.IOService;
import main.com.capgemini.employeepayrollmain.DatabaseServiceException;
import main.com.capgemini.employeepayrollmain.EmployeePayrollData;

public class EmployeePayrollServiceDBTest {
	
	// Test employee count in database
	@Test
	public void testNormalisedDBReadEmployeeData() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		Assert.assertEquals(4, employeePayrollData.size());
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
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeePayrollDataForDateRange(IOService.DB_IO, start, end);
		Assert.assertEquals(3, employeePayrollData.size());
	}
	
	// Get average salary for employee
	@Test
	public void givenEmployeePayrollData_ShouldMatchAverageSalary_GroupByGender() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		Map<String, Double> employeePayrollData = employeePayrollService.readAverageSalaryByGender(IOService.DB_IO);
		Assert.assertTrue(employeePayrollData.get("M").equals(150000.00) &&
				employeePayrollData.get("F").equals(3000000.00));
	}
}
