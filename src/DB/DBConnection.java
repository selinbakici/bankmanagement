package DB;

 import java.sql.Connection;  
 import java.sql.DriverManager;  
 import java.sql.SQLException;  
import java.util.Properties;

 
 
public class DBConnection {  
   private static Connection conn;    
   public static String url = "jdbc:mysql://localhost:3306/bank?useSSL=false";  
   private static String user = "root"; 
    
   public static Connection connect() throws SQLException{  
     try{  
       Class.forName("com.mysql.jdbc.Driver").newInstance();  
     }catch(ClassNotFoundException cnfe){  
       System.err.println("Error: "+cnfe.getMessage());  
     }catch(InstantiationException ie){  
       System.err.println("Error: "+ie.getMessage());  
     }catch(IllegalAccessException iae){  
       System.err.println("Error: "+iae.getMessage());  
     }  
     conn = DriverManager.getConnection(url,user,"");  
     return conn;  
   }  
   
   public static Connection getConnection() throws SQLException, ClassNotFoundException{  
     if(conn !=null && !conn.isClosed())  
       return conn;  
     connect();  
     return conn;  
   }  
 }   