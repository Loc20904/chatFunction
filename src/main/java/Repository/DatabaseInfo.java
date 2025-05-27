package Repository;


public interface DatabaseInfo {
    // WINHTUAN\SQLEXPRESS sa --- TUAN
    // LAPTOP-0L8RQE0C loc    --- LOC  
    public static String DRIVERNAME="com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static String DBURL="jdbc:sqlserver://WINHTUAN\\SQLEXPRESS:1433;databaseName=chatFunction;encrypt=false;trustServerCertificate=true;loginTimeout=30;";
    public static String USERDB="sa";
    public static String PASSDB="123";
   
}