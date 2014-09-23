/**
 * 
 */
package spider;

/**
 *  �����û����õ����ݿ����ƣ� �û����� ������, ����setting�������õ����ݣ��߳����� timeout�� saveperiod��
 * @author keyalin
 */
public class GetSettingValue {
    private static String databaseName;
    private static String userName;
    private static String password;
    private static int pageThreadCount = 1;
    private static int imageThreadCount = 1;
    private static int timeout = 0;
    private static long savePeriod = 3600000;
    
    public static void setDatabaseName(String database){
        databaseName = database;
    }
 
    public static void setUserName(String user){
        userName = user;
    }
    
    public static void setPassword(String value){
        password = value;
    }
    
    public static String getDatabaseName(){
        return databaseName;
    }
    
    public static String getUserName(){
        return userName;
    }
    
    public static String getPassword(){
        return password;
    }
    
    public static void setPageThreadCount(int value){
    	pageThreadCount = value;
    }
    
    public static int getPageThreadCount(){
    	return pageThreadCount;
    }
    
    public static void setImageThreadCount(int value){
    	imageThreadCount = value;
    }
    
    public static int getImageThreadCount(){
    	return imageThreadCount;
    }
    
    public static void setTimeout(int value){
    	timeout = value;
    }
    
    public static int getTimeout(){
    	return timeout;
    }
    
    public static void setSavePeriod(long value){
    	savePeriod = value;
    }
    
    public static long getSavePeriod(){
    	return savePeriod;
    }
}
