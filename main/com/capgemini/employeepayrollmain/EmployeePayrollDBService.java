package main.com.capgemini.employeepayrollmain;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

public class EmployeePayrollDBService {

	private static Logger log = Logger.getLogger(EmployeePayrollDBService.class.getName());
	private PreparedStatement employeePayrollDataStatement;
	private static EmployeePayrollDBService employeePayrollDBService;

	private EmployeePayrollDBService() {
	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null)
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}

	// Establishing connection and getting connection object
	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "123qwe";
		Connection con;
		log.info("Connecting to database : " + jdbcURL);
		con = DriverManager.getConnection(jdbcURL, userName, password);
		log.info("Connection is successful : " + con);
		return con;
	}

	// Reading data from database
	public List<EmployeePayrollData> readData() throws DatabaseServiceException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			String query = "select * from EmployeePayroll";
			employeePayrollList = this.getEmployeePayrollDataUsingDB(query);
		} catch (Exception e) {
			throw new DatabaseServiceException("Cannot create or establish connection to database");
		}
		return employeePayrollList;
	}

	// Update employee data
	public int updateEmployeeData(String name, double salary) {
		return this.updateDataUsingPreparedStatement(name, salary);
	}

	// Updating data using statement
	private int updateDataUsingStatement(String name, double salary) {
		String query = String.format("update EmployeePayroll set salary = '%.2f' where name = '%s';", salary, name);
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Updating data using prepared statement
	private int updateDataUsingPreparedStatement(String name, Double salary) {
		String query = String.format("update EmployeePayroll set salary = %.2f where name='%s';", salary, name);
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(query);
			return prepareStatement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Getting list as per name
	public List<EmployeePayrollData> getEmployeePayrollData(String name) {
		List<EmployeePayrollData> employeePayrollList = null;
		if (this.employeePayrollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try {
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	// Getting data from result set
	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("ID");
				String name = resultSet.getString("NAME");
				String gender = resultSet.getString("GENDER");
				double salary = resultSet.getDouble("SALARY");
				LocalDate start = resultSet.getDate("START").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, gender, salary, start));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	// Execute given query
	public List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String query) throws DatabaseServiceException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			ResultSet employeePayroll = statement.executeQuery(query);
			employeePayrollList = this.getEmployeePayrollData(employeePayroll);
		} catch (Exception e) {
			throw new DatabaseServiceException("Cannot create or establish connection to database");
		}
		return employeePayrollList;
	}

	// Retrieving employees for date range
	public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate start, LocalDate end)
			throws DatabaseServiceException {
		String query = String.format("select * from employeepayroll where start between '%s' and '%s'",
				Date.valueOf(start), Date.valueOf(end));
		try {
			return this.getEmployeePayrollDataUsingDB(query);
		} catch (DatabaseServiceException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Retrieve average salary from database
	public Map<String, Double> getAverageSalaryByGender() throws DatabaseServiceException {
		String query = "select gender,avg(salary) as averageSalary from employeepayroll group by gender";
		Map<String, Double> averageSalaryByGender = new HashMap<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double avgSalary = resultSet.getDouble("averageSalary");
				averageSalaryByGender.put(gender, avgSalary);
			}
		} catch (SQLException e) {
			throw new DatabaseServiceException("Error in accessing database!");
		}
		return averageSalaryByGender;
	}

	// Preparing the statement
	private void prepareStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String query = "select * from EmployeePayroll where name = ?";
			employeePayrollDataStatement = connection.prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
