package main.com.capgemini.employeepayrollmain;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.*;

public class EmployeePayrollService {

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	private List<EmployeePayrollData> employeePayrollList;
	private EmployeePayrollDBService employeePayrollDBService;
	private EmployeePayrollNormaliseDBService employeePayrollNormaliseDBService;
	static Logger log = Logger.getLogger(EmployeePayrollService.class.getName());

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
		employeePayrollNormaliseDBService = EmployeePayrollNormaliseDBService.getInstance();
	}

	// Initializing the field which stores employee payroll details
	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = employeePayrollList;
	}

	// Taking details for employee from console
	private void readEmployeePayrollData(Scanner consoleInputReader) {
		log.info("Enter Employee ID");
		int employeeId = consoleInputReader.nextInt();
		log.info("Enter Employee Name");
		String employeeName = consoleInputReader.next();
		log.info("Enter Employee Salary");
		double salary = consoleInputReader.nextDouble();
		employeePayrollList.add(new EmployeePayrollData(employeeId, employeeName, salary));
	}

	// Writing details for employee to file or console depending on user choice
	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("Writing Employee Pyroll to Console\n" + employeePayrollList);
		if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().writeDataToFile(employeePayrollList);
	}

	// Counting number of entries made to the file
	public long countEntries(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			return new EmployeePayrollFileIOService().countEntries();
		return employeePayrollList.size();
	}

	// Printing the employee payroll data
	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().printData();
	}

	// Reading employee payroll data from file
	public List<EmployeePayrollData> readEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollNormaliseDBService.readData();
		return employeePayrollList;
	}
	
	// Reading data for active employees
	public List<EmployeePayrollData> readEmployeePayrollDataForActiveEmployees(IOService ioService) throws DatabaseServiceException {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollNormaliseDBService.getActiveEmployeesData();
		return employeePayrollList;
	}

	// Reading employee data for date range
	public List<EmployeePayrollData> readEmployeePayrollDataForDateRange(IOService ioService, LocalDate start,
			LocalDate end) {
		if (ioService.equals(IOService.DB_IO))
			return employeePayrollNormaliseDBService.getEmployeeForDateRange(start, end);
		return null;
	}

	// Reading average salary from database
	public Map<String, Double> readAverageSalaryByGender(IOService ioService) throws DatabaseServiceException {
		if (ioService.equals(IOService.DB_IO))
			try {
				return employeePayrollNormaliseDBService.getAverageSalaryByGender();
			} catch (DatabaseServiceException e) {
				e.printStackTrace();
			}
		return null;
	}

	// Inserting employee to database
	public void addEmployeeToDatabase(String name, String gender, double salary, LocalDate start) {
		employeePayrollList
				.add(employeePayrollDBService.addEmployeeToDatabaseWithPayrollDetails(name, gender, salary, start));
	}

	// Inserting record to multiple tables in database
	public void addEmployeeToPayroll(String name, String gender, double salary, LocalDate start, int departmentId,
			String departmentName, int companyId, String companyName) throws DatabaseServiceException {
		employeePayrollList.add(employeePayrollNormaliseDBService.addEmployeeToDatabase(name, gender, salary, start,
				departmentId, departmentName, companyId, companyName));
	}
	
	// Adding employee to payroll without using thread
	public void addEmployeeToPayroll(List<EmployeePayrollData> employeeList) throws DatabaseServiceException{
		employeeList.forEach(employeePayrollData ->{
			this.addEmployeeToDatabase(employeePayrollData.name,employeePayrollData.gender, employeePayrollData.salary, employeePayrollData.start);
		});
	}

	// Adding employee to payroll using thread
	public void addEmployeeToPayrollWithThreads(List<EmployeePayrollData> employeeList) {
		Map<Integer,Boolean> employeeAdditionStatus = new HashMap<>();
		employeeList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
				this.addEmployeeToDatabase(employeePayrollData.name, employeePayrollData.gender, employeePayrollData.salary, employeePayrollData.start);
				employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
				};
				Thread thread = new Thread(task, employeePayrollData.name);
				thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			}catch(InterruptedException e) {}
		}
	}
	// Updating the data
	public void updateEmployeeSalary(String name, double salary) {
		int result = employeePayrollNormaliseDBService.updateEmployeeData(name, salary);
		if (result == 0)
			return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	// Returning employee payroll data object
	private EmployeePayrollData getEmployeePayrollData(String name) {
		return this.employeePayrollList.stream().filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name)).findFirst().orElse(null);
	}

	public boolean checkEmployeePayrollSyncWithDB(String name) {
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollNormaliseDBService.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}

	public static void main(String[] args) {

		// Welcome message added
		log.info("Welcome to Employee Payroll Program");
		ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeePayrollData(consoleInputReader);
		employeePayrollService.writeEmployeePayrollData(IOService.FILE_IO);
	}
}
