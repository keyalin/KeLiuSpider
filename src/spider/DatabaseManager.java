/**
 * ���ļ�������keliuspider�����ݿ��ѯ�����������룬ɾ���Ȳ���
 */
package spider;

import java.sql.*;
import com.mysql.jdbc.Driver;
/**
 * �������ݿ������
 * �������ݿ��Ĵ�������ѯ�� ɾ���� ����Ȳ���
 * @author keyalin
 *
 */
public class DatabaseManager {
	
	/**
	 * ��ĳ�����ݿ⽨������,��ʧ�ܷ���null
	 */
	public static Connection databaseConnection(String database, String user, String password){
		try{
			String url = "jdbc:MySQL://localhost:3306/" + database + 
					"?useUnicode=true&amp;characterEncoding=UTF-8&amp";
			String driver = "com.mysql.jdbc.Driver";
			Class.forName(driver);
			DriverManager.setLoginTimeout(2);
			Connection conn = DriverManager.getConnection(url, user, password);
			return conn;
		}catch(Exception e){
			return null;
		}
		
	}
	
	/**
	 * ��һ�����ݿ��д���һ���б�, ��ʧ�ܷ���-1���ɹ�����0
	 * @param database
	 * @param sql
	 * @param user
	 * @param password
	 * @return
	 */
	public static int createTable(String database, String sql, String user, String password){
		return excute(database, sql, user, password);
	}
	
	/**
	 * �ڱ��в�����Ϣ, ��ʧ�ܷ���-1���ɹ�����0
	 * @param database
	 * @param sql
	 * @param user
	 * @param password
	 * @return
	 */
	public static int insert(String database, String sql, String user, String password){
		return excute(database, sql, user, password);
	}
	
	/**
	 * �ڱ����޸���Ϣ, ��ʧ�ܷ���-1���ɹ�����0
	 * @param database
	 * @param sql
	 * @param user
	 * @param password
	 * @return
	 */
	public static int update(String database, String sql, String user, String password){
		return excute(database, sql, user, password);
	}
	
	/**
	 * 
	 * @param database
	 * @param sql
	 * @param user
	 * @param password
	 * @return
	 */
	public static int delete(String database, String sql, String user, String password){
		return excute(database, sql, user, password);
	}
	
	private static int excute(String database, String sql, String user, String password){
		Connection conn = databaseConnection(database, user, password);
		if(conn == null){
			return -1;
		}
		else{
			try{
				Statement state = conn.createStatement();
				int b = state.executeUpdate(sql);
				if(b < 0) try{
					conn.close();
					return -1;
				}catch(Exception e1){
					return -1;
				}
				else {
					try{
						conn.close();
						return b;
					}catch(Exception e1){
						return -1;
					}
				}
			}catch(Exception e){
				try{
					conn.close();
					return -1;
				}catch(Exception e1){
					return -1;
				}
			}	
		}
	}
	
	public static ResultSet query(String database, String sql, String user, String password){
		Connection conn = databaseConnection(database, user, password);
		try{
			if(conn == null) {
				return null;
			}
			else {
				Statement state = conn.createStatement();
				ResultSet set = state.executeQuery(sql);
				try{
					while(set.next()){
						String url = set.getString(1);
						int id = set.getInt(2);
						System.out.println("url: " + url + "id: " + id);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				return set;
			}
		}catch(Exception e){
			return null;
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}
	
	public static void main(String[] args) throws SQLException{
//		Date datetime = new Date();
//		System.out.println(datetime.toString());
//		String create = "create table images (url varchar(200), contents MEDIUMBLOB, host varchar(150), " +
//				"format varchar(20), download_date varchar(50))default charset utf8";
		String create = 	"create table pages (url varchar(200), " +
							"host varchar(100), " +
							"contents MEDIUMTEXT, " +
							"download_date varchar(50), " +
							"keywords varchar(30)) default charset utf8;";
		int i = createTable("KeLiuSpider", create, "root", "3125703");
		System.out.println(i);
//		String str = GetUrlContents.getHtml("file:///C:/Users/keyalin/Desktop/nihao.mht", 3000);
//		System.out.println(str);
//		Connection conn = databaseConnection("KeLiuSpider", "root", "3125703");
////		String str = "���";
//		String sql = "select * from pages where url = 'whu'";
//		PreparedStatement  statement = conn.prepareStatement(sql);
//		ResultSet set = statement.executeQuery();
//		while(set.next()){
//			System.out.println(set.getString(2));
//		}
//		statement.close();
//		conn.close();
		
	}
}
