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

	// @return the id
	public int getId() {
		return id;
	}

	// @return the name
	public String getName() {
		return name;
	}

	// @return the salary
	public double getSalary() {
		return salary;
	}
	
	@Override
	public String toString() {
		return "EMPLOYEE ID : "+this.id+" NAME : "+this.name+" SALARY : "+this.salary;
	}

}
