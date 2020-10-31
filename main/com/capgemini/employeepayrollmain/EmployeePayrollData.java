package main.com.capgemini.employeepayrollmain;

import java.time.LocalDate;

public class EmployeePayrollData {

	private int id;
	public String name;
	private String gender;
	public double salary;
	private LocalDate start;
	public String companyName;
	public int companyId;
	public String department[];

	// Parameterized constructor to initialize instance members
	public EmployeePayrollData(int id, String name, double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	public EmployeePayrollData(int id, String name, double salary, LocalDate start) {
		this(id, name, salary);
		this.start = start;
	}

	public EmployeePayrollData(int id, String name, String gender, double salary, LocalDate start) {
		this(id, name, salary, start);
		this.gender = gender;
	}

	public EmployeePayrollData(int id, String name, String gender, double salary, LocalDate start, String companyName,
			int companyId, String[] department) {
		this(id, name, gender, salary, start);
		this.companyName = companyName;
		this.companyId = companyId;
		this.department = department;
	}

	@Override
	public String toString() {
		return "EMPLOYEE ID : " + this.id + " NAME : " + this.name + " GENDER : " + this.gender + " SALARY : "
				+ this.salary + " START : " + this.start;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmployeePayrollData other = (EmployeePayrollData) obj;
		return id == other.id && Double.compare(other.salary, salary) == 0 && name.equals(other.name);
	}

	// Printing the departments
	public void printDepartments() {
		String departments[] = this.department;
		for (String s : departments) {
			System.out.println("id: " + this.id + ":" + s);
		}
	}
}
