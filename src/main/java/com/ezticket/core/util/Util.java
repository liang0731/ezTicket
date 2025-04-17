package com.ezticket.core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Util {
	
	public static final String Driver = "com.mysql.cj.jdbc.Driver";
	public static final String URL = System.getenv("SPRING_DATASOURCE_URL") + "?serverTimezone=Asia/Taipei";
	public static final String USER = "root";
	public static final String PASSWORD = "root";
	
	public static Connection getConnection() throws SQLException {
		Connection connection =DriverManager.getConnection(Util.URL,Util.USER,Util.PASSWORD);
		return connection;
	}
}
