package test.com.capgemini.niofileapitest;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import main.com.capgemini.employeepayrollmain.DatabaseServiceException;
import main.com.capgemini.employeepayrollmain.EmployeePayrollData;
import main.com.capgemini.employeepayrollmain.EmployeePayrollService;
import main.com.capgemini.employeepayrollmain.EmployeePayrollService.IOService;

public class EmployeePayrollServiceTest {

	// Testing if 3 entries are made to the file or not
	@Test
	public void testWriteEmployeeDataToFileFor3EmployeesAndPrint() {
		EmployeePayrollData[] employeeData = { new EmployeePayrollData(1, "Naruto Uzumaki", 300000),
				new EmployeePayrollData(2, "Shikamaru Nara", 290000),
				new EmployeePayrollData(3, "Hashirama Senju", 400000) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(employeeData));
		employeePayrollService.writeEmployeePayrollData(IOService.FILE_IO);
		employeePayrollService.printData(IOService.FILE_IO);
		long noOfEntries = employeePayrollService.countEntries(IOService.FILE_IO);
		Assert.assertEquals(3, noOfEntries);
	}

	// Testing reading data from database
	@Test
	public void testReadEmployeeDataFromDatabase() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		Assert.assertEquals(3, employeePayrollList.size());
	}

	// Testing update data in database
	@Test
	public void testUpdateEmployeeDataInDatabase() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Terissa", 300000.00);
		boolean result = employeePayrollService.checkEmployeePayrollSyncWithDB("Terissa");
		Assert.assertTrue(result);
	}

	// Testing retrieving data as per given query
	@Test
	public void testRetrieveDataAsPerQuery() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select * from EmployeePayroll where start between cast('2018-01-01' as date) and date(now());";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readDataByQuery(query);
		Assert.assertEquals(3, employeePayrollList.size());
	}

	// Testing to get sum of salary based on gender
	@Test
	public void testPerformOperationsMaleSum() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select sum(salary) from employeepayroll where gender = 'M' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double sum = employeePayrollService.performOperations(query);
		Assert.assertEquals(300000, sum, 0);
	}

	@Test
	public void testPerformOperationsFemaleSum() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select sum(salary) from employeepayroll where gender = 'F' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double sum = employeePayrollService.performOperations(query);
		Assert.assertEquals(300000, sum, 0);
	}

	// Testing to get avg of salary based on gender
	@Test
	public void testPerformOperationsMaleAvg() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select avg(salary) from employeepayroll where gender = 'M' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double avg = employeePayrollService.performOperations(query);
		Assert.assertEquals(150000, avg, 0);
	}

	@Test
	public void testPerformOperationsFemaleAvg() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select avg(salary) from employeepayroll where gender = 'F' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double avg = employeePayrollService.performOperations(query);
		Assert.assertEquals(300000, avg, 0);
	}

	// Testing to get minimum and maximum salary based on gender
	@Test
	public void testPerformOperationsMaleMax() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select max(salary) from employeepayroll where gender = 'M' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double max = employeePayrollService.performOperations(query);
		Assert.assertEquals(200000, max, 0);
	}

	@Test
	public void testPerformOperationsMaleMin() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select min(salary) from employeepayroll where gender = 'M' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double min = employeePayrollService.performOperations(query);
		Assert.assertEquals(100000, min, 0);
	}

	@Test
	public void testPerformOperationsFemaleMax() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select max(salary) from employeepayroll where gender = 'F' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double max = employeePayrollService.performOperations(query);
		Assert.assertEquals(300000, max, 0);
	}

	@Test
	public void testPerformOperationsFemaleMin() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select min(salary) from employeepayroll where gender = 'F' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		double min = employeePayrollService.performOperations(query);
		Assert.assertEquals(300000, min, 0);
	}

	// Get count by gender
	@Test
	public void testPerformOperationsMaleCount() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select count(name) from employeepayroll where gender = 'M' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		int count = (int) employeePayrollService.performOperations(query);
		Assert.assertEquals(2, count);
	}

	@Test
	public void testPerformOperationsFemaleCount() throws DatabaseServiceException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		String query = "select count(name) from employeepayroll where gender = 'F' group by gender";
		List<EmployeePayrollData> employeePayrollList = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		int count = (int) employeePayrollService.performOperations(query);
		Assert.assertEquals(1, count);
	}
}
