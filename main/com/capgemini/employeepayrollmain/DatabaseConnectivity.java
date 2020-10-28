package main.com.capgemini.employeepayrollmain;

import java.util.Enumeration;
import java.util.logging.*;
import java.sql.*;

public class DatabaseConnectivity {
	private static Logger log = Logger.getLogger(DatabaseConnectivity.class.getName());

	public static void main(String[] args) {

		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "123qwe";
		Connection con;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			log.info("Driver loaded");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot find driver in classpath", e);
		}

		listOfDrivers();

		try {
			log.info("Connecting to database : " + jdbcURL);
			con = DriverManager.getConnection(jdbcURL, userName, password);
			log.info("Connection is successful : " + con);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Printing the available classes
	private static void listOfDrivers() {
		Enumeration<Driver> driverList = DriverManager.getDrivers();
		while (driverList.hasMoreElements()) {
			Driver driverClass = (Driver) driverList.nextElement();
			log.info(" " + driverClass.getClass().getName());
		}
	}
}
