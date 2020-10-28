package main.com.capgemini.employeepayrollmain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

public class EmployeePayrollDBService {

	private Logger log = Logger.getLogger(EmployeePayrollData.class.getName());
	private PreparedStatement employeePayrollDataStatement;
	private static EmployeePayrollDBService employeePayrollDBService;

	private EmployeePayrollDBService() {
	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null)
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}

	// Reading data from database
	public List<EmployeePayrollData> readData() throws DatabaseServiceException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try (Connection connection = this.getConnection();) {
			String query = "select * from EmployeePayroll";
			Statement statement = connection.createStatement();
			ResultSet employeePayroll = statement.executeQuery(query);
			employeePayrollList = this.getEmployeePayrollData(employeePayroll);
		} catch (Exception e) {
			throw new DatabaseServiceException("Cannot create or establish connection to database");
		}
		return employeePayrollList;
	}

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
				double salary = resultSet.getDouble("SALARY");
				LocalDate start = resultSet.getDate("START").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, start));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	// Preparing the statement
	private void prepareStatementForEmployeeData() {
		try (Connection connection = this.getConnection();) {
			String query = "select * from EmployeePayroll where name = ?";
			employeePayrollDataStatement = connection.prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Execute given query
	public List<EmployeePayrollData> readDataByQuery(String query) throws DatabaseServiceException {
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
}
