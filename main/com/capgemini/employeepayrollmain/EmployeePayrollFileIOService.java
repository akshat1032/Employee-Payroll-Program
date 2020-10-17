package main.com.capgemini.employeepayrollmain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
}
