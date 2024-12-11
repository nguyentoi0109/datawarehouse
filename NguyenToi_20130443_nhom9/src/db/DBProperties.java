package db;

import java.io.IOException;
import java.util.Properties;

public class DBProperties {
	private static final Properties properties = new Properties();

	// Static block để load cấu hình
	static {
		try {
			// Đọc file cấu hình "db.properties"
			properties.load(DBProperties.class.getClassLoader().getResourceAsStream("db.properties"));
		} catch (IOException ioException) {
			throw new RuntimeException("Unable to load database properties file", ioException);
		}
	}

	// Getter cho các thông tin cấu hình
	public static String getControlURL() {
		return properties.getProperty("control.url");
	}

	public static String getControlUsername() {
		return properties.getProperty("control.username");
	}

	public static String getControlPassword() {
		return properties.getProperty("control.password");
	}

	public static String getStagingURL() {
		return properties.get("staging.url").toString();
	};

	public static String getStagingUsername() {
		return properties.get("staging.username").toString();
	};

	public static String getStagingPassword() {
		return properties.get("staging.password").toString();
	};
}
