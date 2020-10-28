package main.com.capgemini.employeepayrollmain;

import java.time.LocalDate;

public class EmployeePayrollData {

	private int id;
	private String name;
	private double salary;
	private LocalDate start;

	// Parameterized constructor to initialize instance members
	public EmployeePayrollData(int id, String name, double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	public EmployeePayrollData(int id, String name, double salary, LocalDate start) {
		this(id,name,salary);
		this.start = start;
	}

	@Override
	public String toString() {
		return "EMPLOYEE ID : " + this.id + " NAME : " + this.name + " SALARY : " + this.salary+" START : "+this.start;
	}

}
