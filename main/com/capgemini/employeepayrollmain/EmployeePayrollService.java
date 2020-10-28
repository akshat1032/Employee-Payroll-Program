package main.com.capgemini.employeepayrollmain;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.*;

public class EmployeePayrollService {

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	private List<EmployeePayrollData> employeePayrollList;
	private EmployeePayrollDBService employeePayrollDBService;
	private static Logger log = Logger.getLogger(EmployeePayrollService.class.getName());

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	// Initializing the field which stores employee payroll details
	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = employeePayrollList;
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
		return 0;
	}

	// Printing the employee payroll data
	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().printData();
	}

	// Reading employee payroll data from file
	public List<EmployeePayrollData> readEmployeePayrollData(IOService ioService) throws DatabaseServiceException {
		if (ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBService.readData();
		return employeePayrollList;
	}

	// Updating the data
	public void updateEmployeeSalary(String name, double salary) {
		int result = employeePayrollDBService.updateEmployeeData(name, salary);
		if (result == 0) return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	// Returning employee payroll data object
	private EmployeePayrollData getEmployeePayrollData(String name) {
		return this.employeePayrollList.stream()
				.filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name))
				.findFirst()
				.orElse(null);
	}

	public boolean checkEmployeePayrollSyncWithDB(String name) {
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}
}
