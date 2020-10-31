package main.com.capgemini.employeepayrollmain;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
				+ " from employeepayroll e JOIN employeedepartmentlist e2 on e.id = e2.employee_ID"
				+ " join departmentlist d ON e2.Department_ID  = d.Department_ID"
				+ " join payrolldetails p ON e.id = p.employee_id"
				+ " join companylist c on e.company_id = c.company_id";
		return this.getEmployeePayrollDataUsingQuery(query);
	}

	// Add employee to database
	public EmployeePayrollData addEmployeeToDatabase(String name, String gender, double salary, LocalDate start,
			int departmentId, String departmentName, int companyId, String companyName)
			throws DatabaseServiceException {
		int employeeId = -1;
		Connection connection = null;
		EmployeePayrollData employeePayrollData = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DatabaseServiceException("Error in establising connection for adding to database");
		}

		try {
			Statement statement = connection.createStatement();
			String query = String.format("insert into companylist values ('%s','%s');", companyId, companyName);
			int rowsAffected = statement.executeUpdate(query);
			if (rowsAffected == 1) {
				employeePayrollData = new EmployeePayrollData(companyName, companyId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e2) {
				throw new DatabaseServiceException("Error in inserting record to CompanyList table");
			}
			return employeePayrollData;
		}

		try {
			Statement statement = connection.createStatement();
			String query = String.format(
					"insert into employeepayroll(name,gender,salary,start,company_id) VALUES ('%s','%s','%s','%s','%s');",
					name, gender, salary, Date.valueOf(start), companyId);
			int rowsAffected = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			if (rowsAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(employeeId, name, gender, salary, start, companyId,
					companyName);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DatabaseServiceException("Error in inserting record to Employee Payroll table");
			}
			return employeePayrollData;
		}

		try {
			Statement statement = connection.createStatement();
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String query = String.format(
					"insert into payrolldetails(employee_id,basic_pay,deductions,taxable_pay,tax ,net_pay)VALUES ('%s','%s','%s','%s','%s','%s');",
					employeeId, salary, deductions, taxablePay, tax, netPay);
			int rowsAffected = statement.executeUpdate(query);
			if (rowsAffected == 1) {
				employeePayrollData = new EmployeePayrollData(employeeId, name, salary, start);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e3) {
				throw new DatabaseServiceException("Error in inserting record to PayrollDetails table");
			}
			System.out.println(employeePayrollData);
			return employeePayrollData;
		}
		try {
			Statement statement = connection.createStatement();
			String query = String.format(
					"insert into departmentlist(Department_ID,Department_Name) VALUES ('%s','%s');", departmentId,
					departmentName);
			int rowsAffected = statement.executeUpdate(query);
			if (rowsAffected == 1) {
				employeePayrollData = new EmployeePayrollData(departmentId, departmentName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e4) {
				throw new DatabaseServiceException("Error in inserting record to DepartmentList table");
			}
			return employeePayrollData;
		}
		try {
			Statement statement = connection.createStatement();
			String query = String.format(
					"insert into employeedepartmentlist(employee_id,department_id) VALUES ('%s','%s');", employeeId,
					departmentId);
			int rowsAffected = statement.executeUpdate(query);
			if (rowsAffected == 1) {
				employeePayrollData = new EmployeePayrollData(employeeId, departmentId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e5) {
				throw new DatabaseServiceException("Error in inserting record to EmployeeDepartmentList table");
			}
			return employeePayrollData;
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return employeePayrollData;
		}
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
				log.info("" + dept);
				department.add(dept);
				log.info("" + id);
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
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
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

	// Get active employees from DB
	public List<EmployeePayrollData> getActiveEmployeesData() throws DatabaseServiceException {
		String query = "select * from employeepayroll e join companylist c on e.company_id = c.company_id where is_active=1";
		return this.getEmployeePayrollDataUsingIsActive(query);
	}
	
	// Passing query for active employees
	private List<EmployeePayrollData> getEmployeePayrollDataUsingIsActive(String query) throws DatabaseServiceException {
		List<EmployeePayrollData> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(query);
			ResultSet resultSet = prepareStatement.executeQuery(query);
			employeePayrollList = this.getEmployeePayrollDataUsingIsActive(resultSet);
		} catch (SQLException e) {
			throw new DatabaseServiceException("Error in getting result set using is active");
		}
		return employeePayrollList;
	}
	
	// Populating the object and returning list of employees
	private List<EmployeePayrollData> getEmployeePayrollDataUsingIsActive(ResultSet resultSet) throws DatabaseServiceException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int companyId = resultSet.getInt("company_id");
				String companyName = resultSet.getString("company_name");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate start = resultSet.getDate("start").toLocalDate();
				double salary = resultSet.getDouble("salary");
				boolean is_active=resultSet.getBoolean("is_active");
				employeePayrollList
						.add(new EmployeePayrollData(id, name, gender, salary, start, companyName, companyId, is_active));
			}
		} catch (SQLException e) {
			throw new DatabaseServiceException("Error in get values from result set using is active");
		}
		return employeePayrollList;
	}

	// Get data for date range
	public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate start, LocalDate end) {
		String query = String.format(
				"select * from employeepayroll e join companylist c on e.company_id = c.company_id where start between '%s' and '%s';",
				Date.valueOf(start), Date.valueOf(end));
		return this.getEmployeePayrollDataUsingDB(query);
	}

	// Get average salary by gender
	public Map<String, Double> getAverageSalaryByGender() throws DatabaseServiceException {
		String query = "SELECT gender,avg(basic_pay) as avg_salary from employeepayroll e join payrolldetails p"
				+ " on e.id = p.employee_id group by gender;";
		Map<String, Double> genderToAverageSalaryMap = new HashMap<>();
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(query);
			ResultSet resultSet = prepareStatement.executeQuery(query);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("avg_salary");
				genderToAverageSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			throw new DatabaseServiceException("Cannot get average salary");
		}
		return genderToAverageSalaryMap;
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
				employeePayrollList
						.add(new EmployeePayrollData(id, name, gender, salary, start, companyId, companyName));
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
