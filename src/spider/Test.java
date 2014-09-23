package spider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author keyalin
 *
 */
public class Test {
	public static void main(String[] args) throws IOException, SQLException{
		String image = "http://www.whu.edu.cn/img/index_03.gif";
		byte[] bytes = GetUrlContents.getBytes(image, 0);
		String sql = "insert into images (url, contents, host, " +
		"format, download_date) values(?, ?, ?, ?, ?)";
		String host = Utility.getHost(image);
		String format = Utility.getImagFormat(image);
		String timestamp = Utility.getFormatTime(Calendar.getInstance());
		PreparedStatement statement = DatabaseManager.databaseConnection(
								"keliuspider", "root", "3125703").prepareStatement(sql);
		statement.setString(1, image);
		statement.setBytes(2, bytes);
		statement.setString(3, host);
		statement.setString(4, format);
		statement.setString(5, timestamp);
		statement.execute();
	}
}

class Task implements Runnable{
	public void run(){
		try {
			TimeUnit.SECONDS.sleep(20);
//			while(true)System.out.println("nihao");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}