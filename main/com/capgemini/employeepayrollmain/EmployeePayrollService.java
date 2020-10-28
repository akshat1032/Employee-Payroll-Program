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
	private static Logger log = Logger.getLogger(EmployeePayrollService.class.getName());

	public EmployeePayrollService() {
	}

	// Initializing the field which stores employee payroll details
	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
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
	public long readEmployeePayrollData(IOService ioService) throws DatabaseServiceException {
		if (ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = new EmployeePayrollDBService().readData();
		return employeePayrollList.size();
	}
}
