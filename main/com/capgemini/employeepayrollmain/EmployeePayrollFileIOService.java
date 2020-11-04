package main.com.capgemini.employeepayrollmain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeePayrollFileIOService {
	
	public static String PAYROLL_FILE = "Payroll.txt";
	
	// Writing employee payroll data to file
	public void writeDataToFile(List<EmployeePayrollData> employeePayrollList) {
		StringBuffer employeeDetails = new StringBuffer();
		employeePayrollList.forEach(employee ->{
			String employeeData = employee.toString().concat("\n");
			employeeDetails.append(employeeData);
		});
		try {
			Files.write(Paths.get(PAYROLL_FILE), employeeDetails.toString().getBytes());
		}catch(IOException e) {}
	}
		
	//	Counting no of entries for employee payroll data 
	public long countEntries() {
		long noOfEntries = 0;
		try {
			noOfEntries = Files.lines(new File("Payroll.txt").toPath()).count();
		}catch(IOException e) {}
		return noOfEntries;
	}
	
	// Printing employee payroll data
	public void printData() {
		try {
			Files.lines(new File("Payroll.txt").toPath()).forEach(System.out::println);
		}catch(IOException e) {}
	}
	
	// Read employee payroll details
	public List<EmployeePayrollData> readData(){
		List<EmployeePayrollData> employeeList = new ArrayList<>();
		Object[] employeeData = new Object[3];
		try {
			employeeData = Files.lines(new File("Payroll.txt").toPath())
					.map(line -> line.toString().split("EMPLOYEE ID :  NAME :  SALARY : ,")).toArray();
			System.out.println(String.valueOf(employeeData[0]).toString());
			employeeList.add(new EmployeePayrollData(Integer.parseInt(String.valueOf(employeeData[0]).toString()), String.valueOf(employeeData[1]).toString(), Double.parseDouble(String.valueOf(employeeData[2]).toString())));
		}catch(IOException e) {}
		return employeeList;
	}
}
