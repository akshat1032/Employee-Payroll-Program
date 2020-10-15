package main.com.capgemini.employeepayrollmain;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.*;

public class EmployeePayrollService {
	
	public enum IOService{CONSOLE_IO,FILE_IO,DB_IO,REST_IO}
	private List<EmployeePayrollData> employeePayrollList;
	private Logger log = Logger.getLogger(EmployeePayrollService.class.getName());
	
	public EmployeePayrollService() {}
	
	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this.employeePayrollList = employeePayrollList;
	}

	public static void main(String[] args) {
		
		Logger log = Logger.getLogger(EmployeePayrollService.class.getName());
		//Welcome message added
		log.info("Welcome to Employee Payroll Program");
		ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeePayrollData(consoleInputReader);
		employeePayrollService.writeEmployeePayrollData();
	}

	private void readEmployeePayrollData(Scanner consoleInputReader) {
		log.info("Enter Employee ID");
		int employeeId = consoleInputReader.nextInt();
		log.info("Enter Employee Name");
		String employeeName = consoleInputReader.next();
		log.info("Enter Employee Salary");
		double salary = consoleInputReader.nextDouble();
		employeePayrollList.add(new EmployeePayrollData(employeeId, employeeName, salary));
	}
	
	private void writeEmployeePayrollData() {
		System.out.println("Writing Employee Pyroll to Console\n"+employeePayrollList);
	}
}
