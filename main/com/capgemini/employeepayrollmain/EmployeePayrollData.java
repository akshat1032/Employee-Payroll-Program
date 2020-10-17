package main.com.capgemini.employeepayrollmain;

public class EmployeePayrollData {

	private int id;
	private String name;
	private double salary;

	// Parameterized constructor to initialize instance members
	public EmployeePayrollData(int id, String name, double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	@Override
	public String toString() {
		return "EMPLOYEE ID : " + this.id + " NAME : " + this.name + " SALARY : " + this.salary;
	}

}
