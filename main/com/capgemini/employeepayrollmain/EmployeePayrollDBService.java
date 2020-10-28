package main.com.capgemini.employeepayrollmain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

public class EmployeePayrollDBService {

	private Logger log = Logger.getLogger(EmployeePayrollData.class.getName());

	public List<EmployeePayrollData> readData() throws DatabaseServiceException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			String query = "select * from EmployeePayroll";
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			ResultSet employeePayroll = statement.executeQuery(query);
			while (employeePayroll.next()) {
				int id = employeePayroll.getInt("ID");
				String name = employeePayroll.getString("NAME");
				double salary = employeePayroll.getDouble("SALARY");
				LocalDate start = employeePayroll.getDate("START").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, start));
			}
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
}
