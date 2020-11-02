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
	private int connectionCounter = 0;
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
		Connection connection[] = new Connection[1];
		EmployeePayrollData[] employeePayrollData = new EmployeePayrollData[1];
		Map<Integer, Boolean> payrollAdditionStatus = new HashMap<>();
		try {
			connection[0] = this.getConnection();
			connection[0].setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection[0].rollback();
			} catch (SQLException e1) {
				throw new DatabaseServiceException("Error in establishing connection to DB");
			}
		}
		int comId = this.addToCompanyTable(connection[0], companyName, companyId);
		int employeeId = this.addToEmployeePayrollTable(connection[0], name, gender, salary, start,comId);
		int depId = this.addToDepartmentTable(connection[0], departmentName,departmentId);
		
		Runnable taskAddPayrollDetails = () -> {
			payrollAdditionStatus.put(employeeId, false);
			try {
				boolean tableUpdated = this.addToPayrollDetailsTable(connection[0], employeeId, salary, name, start);
				if (tableUpdated) {
					employeePayrollData[0] = new EmployeePayrollData(employeeId, name, gender, salary, start);
				}
				payrollAdditionStatus.put(employeeId, tableUpdated);
			} catch (DatabaseServiceException e) {
			}
		};
		Thread thread = new Thread(taskAddPayrollDetails);
		thread.start();
		while (payrollAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Map<Integer,Boolean> employeeDeptAdditionStatus = new HashMap<>();
		Runnable taskAddEmployeeDeptDetails = () -> {
			payrollAdditionStatus.put(employeeId, false);
			try {
				boolean tableUpdated = this.addToEmployeeDepartmentTable(connection[0], employeeId, depId);
				payrollAdditionStatus.put(employeeId, tableUpdated);
			} catch (DatabaseServiceException e) {
			}
		};
		Thread threadEmployeeDept = new Thread(taskAddEmployeeDeptDetails);
		threadEmployeeDept.start();
		while (employeeDeptAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			connection[0].commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection[0].close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return employeePayrollData[0];
	}

	// Adding to employee-department table
	private boolean addToEmployeeDepartmentTable(Connection connection, int employeeId, int depId) throws DatabaseServiceException {
		boolean isUpdated = false;
		try {
			Statement statement = connection.createStatement();
			String query = String.format(
					"insert into employeedepartmentlist(employee_id,department_id) VALUES ('%s','%s');", employeeId,
					depId);
			int rowsAffected = statement.executeUpdate(query);
			if (rowsAffected == 1) {
				isUpdated = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e5) {
				throw new DatabaseServiceException("Error in inserting record to EmployeeDepartmentList table");
			}
		}
		return isUpdated;
	}

	private boolean addToPayrollDetailsTable(Connection connection, int employeeId, double salary, String name,
			LocalDate start) throws DatabaseServiceException {
		try {
			Statement statement = connection.createStatement();
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String query = String.format(
					"insert into payrolldetails(employee_id,basic_pay,deductions,taxable_pay,tax ,net_pay)VALUES ('%s','%s','%s','%s','%s','%s');",
					employeeId, salary, deductions, taxablePay, tax, netPay);
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e3) {
				throw new DatabaseServiceException("Error in inserting record to PayrollDetails table");
			}
		}
		return false;
	}

	// Adding to department table
	private int addToDepartmentTable(Connection connection, String departmentName, int departmentId) throws DatabaseServiceException {
		try {
			Statement statement = connection.createStatement();
			ResultSet department = statement.executeQuery("select * from departmentlist");
			List<Integer> departmentIdList = new ArrayList<>();
			while (department.next()) {
				departmentIdList.add(department.getInt("department_id"));
			}
			int departmentCounter = 0;
			for (Integer integer : departmentIdList) {
				if (integer.equals(departmentId))
					departmentCounter++;
			}
			if (departmentCounter == 0) {
				String query = String.format(
						"insert into departmentlist(Department_ID,Department_Name) VALUES ('%s','%s');", departmentId,
						departmentName);
				statement.executeUpdate(query);
				return departmentId;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e4) {
				throw new DatabaseServiceException("Error in inserting record to DepartmentList table");
			}
		}
		return -1;
	}

	// Adding to employee payroll table
	private int addToEmployeePayrollTable(Connection connection, String name, String gender, double salary,
			LocalDate start, int comId) throws DatabaseServiceException {
		int employeeId = -1;
		try {
			Statement statement = connection.createStatement();
			String query = String.format(
					"insert into employeepayroll(name,gender,salary,start,company_id) VALUES ('%s','%s','%s','%s','%s');",
					name, gender, salary, Date.valueOf(start), comId);
			int rowsAffected = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			if (rowsAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DatabaseServiceException("Error in inserting record to Employee Payroll table");
			}
		}
		return employeeId;
	}

	// Adding to company table
	private int addToCompanyTable(Connection connection, String companyName, int companyId) throws DatabaseServiceException {
		try {
			Statement statement = connection.createStatement();
			ResultSet company = statement.executeQuery("select * from companylist");
			List<Integer> companyIdList = new ArrayList<>();
			while (company.next()) {
				companyIdList.add(company.getInt("company_id"));
			}
			int companyCounter = 0;
			for (Integer integer : companyIdList) {
				if (integer == companyId)
					companyCounter++;
			}
			if (companyCounter == 0) {
				String query = String.format("insert into companylist values ('%s','%s');", companyId, companyName);
				statement.executeUpdate(query);
				return companyId;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e2) {
				throw new DatabaseServiceException("Error in inserting record to CompanyList table");
			}
		}
		return -1;
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
	private List<EmployeePayrollData> getEmployeePayrollDataUsingIsActive(String query)
			throws DatabaseServiceException {
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
	private List<EmployeePayrollData> getEmployeePayrollDataUsingIsActive(ResultSet resultSet)
			throws DatabaseServiceException {
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
				boolean is_active = resultSet.getBoolean("is_active");
				employeePayrollList.add(
						new EmployeePayrollData(id, name, gender, salary, start, companyName, companyId, is_active));
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
	private synchronized Connection getConnection() throws SQLException {
		connectionCounter++;
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
