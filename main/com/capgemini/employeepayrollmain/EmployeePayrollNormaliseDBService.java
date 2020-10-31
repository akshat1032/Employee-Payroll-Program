package main.com.capgemini.employeepayrollmain;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EmployeePayrollNormaliseDBService {
	
	private static Logger log = Logger.getLogger(EmployeePayrollNormaliseDBService.class.getName());
	private static EmployeePayrollNormaliseDBService employeePayrollNormaliseDBService;
	private PreparedStatement employeePayrollNormaliseDataStatement;

	private EmployeePayrollNormaliseDBService() {
	}

	public static EmployeePayrollNormaliseDBService getInstance() {
		if (employeePayrollNormaliseDBService == null)
			employeePayrollNormaliseDBService = new EmployeePayrollNormaliseDBService();
		return employeePayrollNormaliseDBService;
	}

	// Reading employee payroll data
	public List<EmployeePayrollData> readData() {
		String query = "select e.id,e.company_Id,e.name,e.gender,e.start,c.company_name,d.department_name,p.basic_pay"
				+ " from employeepayroll e JOIN employeedepartmentlist e2 ON e.id = e2.employee_ID"
				+ " join departmentlist d ON e2.Department_ID  = d.Department_ID"
				+ " join payrolldetails p ON e.id = p.employee_id"
				+ " join companylist c on e.company_id = c.company_id";
		return this.getEmployeePayrollDataUsingQuery(query);
	}

	// Get data from database by query
	private List<EmployeePayrollData> getEmployeePayrollDataUsingQuery(String query) {
		List<EmployeePayrollData> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(query);
			ResultSet resultSet = prepareStatement.executeQuery(query);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	// Populating the employee payroll data and returning list of employee
	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		List<String> department = new ArrayList<String>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int companyId = resultSet.getInt("company_id");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				String companyName = resultSet.getString("company_Name");
				String dept = resultSet.getString("department_name");
				double salary = resultSet.getDouble("basic_pay");
				log.info(""+dept);
				department.add(dept);
				log.info(""+id);
				String[] departmentArray = new String[department.size()];
				employeePayrollList.add(new EmployeePayrollData(id, name, gender, salary, startDate, companyName,
						companyId, department.toArray(departmentArray)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	// Getting data using name
	public List<EmployeePayrollData> getEmployeePayrollData(String name) {
		List<EmployeePayrollData> employeePayrollList = null;
		if (this.employeePayrollNormaliseDataStatement == null)
			this.preparedStatementForEmployeeData();
		try {
			employeePayrollNormaliseDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollNormaliseDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	// Get data for date range
	public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate start, LocalDate end) {
		String query = String.format("select * from employeepayroll e join companylist c on e.company_id = c.company_id where start between '%s' and '%s';", Date.valueOf(start), Date.valueOf(end));
		return this.getEmployeePayrollDataUsingDB(query);
	}

	// Retrieving data using database
	private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String query) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			Connection connection = this.getConnection();
			PreparedStatement prepareStatement = connection.prepareStatement(query);
			ResultSet resultSet = prepareStatement.executeQuery(query);
			employeePayrollList = this.getEmployeePayrollDataNormalised(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}
	
	// Retrieving list of employees from result set and populating object
	private List<EmployeePayrollData> getEmployeePayrollDataNormalised(ResultSet resultSet) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int companyId = resultSet.getInt("company_id");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate start = resultSet.getDate("start").toLocalDate();
				String companyName = resultSet.getString("company_Name");
				double salary = resultSet.getDouble("salary");
				employeePayrollList.add(new EmployeePayrollData(id, name, gender, salary, start, companyName, companyId));
				System.out.println(employeePayrollList);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	// Preparing the statement
	private void preparedStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String query = "select e.id,e.company_id,e.name,e.gender,e.start,c.company_name,d.department_name,p.basic_pay "
					+ " from employeepayroll e JOIN employeedepartmentlist e2 ON e.id = e2.employee_ID"
					+ " join departmentlist d ON e2.Department_ID  = d.Department_ID"
					+ " join payrolldetails p ON e.id = p.employee_id"
					+ " join companylist c on e.company_id = c.company_id where e.name = ?";
			employeePayrollNormaliseDataStatement = connection.prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Updating the employee data
	public int updateEmployeeData(String name, Double salary) {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}

	// updating the data using prepared statement
	private int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
		String query = String.format("update payrolldetails set basic_pay = %.2f where employee_id ="
				+ " (select id from employeepayroll where name = '%s');", salary, name);
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(query);
			return prepareStatement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Establishing connection to database and returning connection object
	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "123qwe";
		Connection connection;
		System.out.println("Connecting to database: " + jdbcURL);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("Connection successful: " + connection);
		return connection;
	}
}
