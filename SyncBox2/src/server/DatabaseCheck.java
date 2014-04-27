package server;

import server.security.jBCrypt.BCrypt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DatabaseCheck {
	private static String userName = "user1";
	private static String password = "password";
	private static String dbName = "SyncBox2Users";

	private static Connection getConnection() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
		return  DriverManager.getConnection("jdbc:derby://localhost/"+dbName, userName, password);
	}

	public static boolean isUser(String email, String pass) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		PreparedStatement preparedStatement = null;
		Connection dbConnection = null;
		
		try {
			dbConnection = getConnection();
			String selectSQL = "SELECT * FROM SYNCBOXUSER WHERE EMAIL='"+email+"'";			
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			ResultSet rs = preparedStatement.executeQuery();			
			if (!rs.next()){	//if database does not contain email
				return false;
				}
			else{
				String hashedPass = rs.getString("PASSWORD");			
				return (BCrypt.checkpw(pass, hashedPass));	
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
