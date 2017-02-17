import java.sql.*;

public class JDGBCtest 
{
	public static void main(String[] args) 
	{
		 try
		    { 
		      // Step 1: Load the JDBC ODBC driver 
		      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); 

		      // Step 2: Establish the connection to the database 
		      String url = "jdbc:odbc:contact_mgr"; 
		      Connection conn = DriverManager.getConnection(url,"user1","password");  
		    }
		    catch (Exception e)
		    { 
		      System.err.println("Got an exception! "); 
		      System.err.println(e.getMessage()); 
		    } 
	}

}
