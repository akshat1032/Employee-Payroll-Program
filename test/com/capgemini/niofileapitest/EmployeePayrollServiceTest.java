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
		long noOfEntries  = employeePayrollService.countEntries(IOService.FILE_IO);
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
		employeePayrollService.updateEmployeeSalary("Terissa",300000.00);
		boolean result = employeePayrollService.checkEmployeePayrollSyncWithDB("Terissa");
		Assert.assertTrue(result);
		
	}
}
