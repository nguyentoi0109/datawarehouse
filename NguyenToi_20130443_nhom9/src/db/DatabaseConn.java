package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DatabaseConn {
	private Connection controlConn;
	private Connection stagingConn;

	public DatabaseConn() {
	}

	// Kết nối đến database Control
	public void connectToControl() {
		try {
			// Lấy thông tin từ DBProperties
			String jdbcUrl = DBProperties.getControlURL();
			String user = DBProperties.getControlUsername();
			String pass = DBProperties.getControlPassword();

			// Tạo kết nối với DriverManager
			Class.forName("com.mysql.cj.jdbc.Driver");
			controlConn = DriverManager.getConnection(jdbcUrl, user, pass);
			System.out.println("Connected to Control database successfully!");
		} catch (SQLException e) {
			throw new RuntimeException("Failed to connect to Control database", e);
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
	}

	public void connectToStaging() {
		try {
			String jdbcUrl = DBProperties.getStagingURL();
			String user = DBProperties.getStagingUsername();
			String pass = DBProperties.getStagingPassword();
			// Tạo kết nối với DriverManager
			Class.forName("com.mysql.cj.jdbc.Driver");
			stagingConn = DriverManager.getConnection(jdbcUrl, user, pass);
			System.out.println("Connected to staging database successfully!");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Getter để lấy Connection
	public Connection getStagingConn() {
		return stagingConn;
	}

	// Getter để lấy Connection
	public Connection getControlConn() {
		return controlConn;
	}

	// Đóng kết nối
	public void closeConnection() {
		try {
			if (controlConn != null && !controlConn.isClosed()) {
				controlConn.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to close Control database connection", e);
		}
	}

	public void closeStaging() throws SQLException {
		if (stagingConn != null) {
			stagingConn.close();
		}
	}

	public List<Map<String, Object>> query(String sql) throws SQLException {
		ResultSet rs = null;
		List<Map<String, Object>> results = new ArrayList<>();
		Connection conn = this.controlConn;
		Statement stmt = conn.createStatement();
		rs = stmt.executeQuery(sql);
		results = rsToList(rs);
		rs.close();
		return results;
	}

	public static List<Map<String, Object>> rsToList(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		while (rs.next()) {
			Map<String, Object> row = new HashMap<>();
			for (int i = 1; i <= columns; i++) {
				row.put(md.getColumnLabel(i), rs.getObject(i));
			}
			results.add(row);
		}
		return results;
	}

	public void updateStatusConfig(String status, String id) throws SQLException {
		String sql = "UPDATE control.configs SET status = ? WHERE id = ?";

		PreparedStatement ps = this.getControlConn().prepareStatement(sql);
		ps.setString(1, status);
		ps.setString(2, id);
		ps.executeUpdate();

	}

	public void log(String config_id, String name, String status, String note, String created_by) throws SQLException {
		Timestamp fileTimestamp = new Timestamp(System.currentTimeMillis());
		String sql = "INSERT INTO log (config_id, `name`, `status`, file_timestamp, note, created_at, created_by) "
				+ "VALUES (?, ?, ?, ?, ?, NOW(), ?)";

		Connection control = this.getControlConn();
		PreparedStatement ps = control.prepareStatement(sql);
		ps.setString(1, config_id);
		ps.setString(2, name);
		ps.setString(3, status);
		ps.setTimestamp(4, fileTimestamp);
		ps.setString(5, note);
		ps.setString(6, created_by);
		ps.executeUpdate();
	}

	public void Truncate_staging() throws SQLException {
		String sql = "TRUNCATE TABLE staging";
		Connection staging = this.getStagingConn();
		PreparedStatement ps = staging.prepareStatement(sql);
		ps.executeUpdate();
	}

	public void LoadStaging(String tour_name, String image_tour, String location_name, String province, String vehicle,
			String price, String description, String start_point, String destination, String duration_days,
			String start_date, String end_date) throws SQLException {
		String sql = "INSERT INTO staging (tour_name, image_tour, location_name, province, vehicle, price, description, start_point, destination, duration_days, start_date, end_date) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection staging = this.getStagingConn();
		PreparedStatement ps = staging.prepareStatement(sql);
		ps.setString(1, tour_name);
		ps.setString(2, image_tour);
		ps.setString(3, location_name);
		ps.setString(4, province);
		ps.setString(5, vehicle);
		ps.setString(6, price);
		ps.setString(7, description);
		ps.setString(8, start_point);
		ps.setString(9, destination);
		ps.setString(10, duration_days);
		ps.setString(11, start_date);
		ps.setString(12, end_date);
		ps.executeUpdate();
	}

	public static void main(String[] args) throws SQLException {
		DatabaseConn dbConnect = new DatabaseConn();
		if (dbConnect != null) {
			dbConnect.connectToStaging();
			dbConnect.LoadStaging("Test Tour", "test_image.jpg", "Test Location", "Test Province", "Bus", "1000.00", "This is a test description.", "Test Start", "Test Destination", "5", "2024-12-10", "2024-12-15");
			System.out.println("completed");
		}
		dbConnect.closeStaging();

	}

}
