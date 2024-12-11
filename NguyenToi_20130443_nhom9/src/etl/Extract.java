package etl;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import db.DatabaseConn;
import util.SendMail;
public class Extract {
	public void extract() {
		final String cnError = "Cannot connected! ";
		final String extractError = "EXTRACT_ERROR! ";
		String currentEmail = "nnnnn01092002@gmail.com";
		LocalDate currentDate = LocalDate.now();

		try {
			DatabaseConn connection = new DatabaseConn();
			connection.connectToControl();
			// kết nối db control
			Connection control = connection.getControlConn();
			// Không kết nối được thì gửi mail
			if (control == null) {
				SendMail.sendMail(currentEmail, cnError + currentDate, extractError, currentEmail);
				return;
			}
			String getConfig = "SELECT * FROM configs WHERE flag = 1 AND status = 'CRAWL_COMPLETED'";
			List<Map<String, Object>> listConfig = connection.query(getConfig);

			for (Map<String, Object> config : listConfig) {
				String id = config.get("id").toString();
				// Cập nhật status
				connection.updateStatusConfig("EXTRACT_START", id);

				// Kết nối db staging
				connection.connectToStaging();
				Connection staging = connection.getStagingConn();

				if (staging == null) {
					// Gửi mail và báo lỗi vào Log
					connection.updateStatusConfig("EXTRACT_ERROR", id);
					SendMail.sendMail(currentEmail, extractError, currentEmail, getConfig);
					connection.log(id, "Log of Extract", "EXTRACT_ERROR", "Cannot connect Staging db",
							"extract_script");
					continue;
				}

				String name = config.get("name").toString();
				File file = new File("C:\\Users\\DELL\\Desktop\\tour.csv");
				try (FileInputStream excelFile = new FileInputStream(file);
						Workbook workbook = new XSSFWorkbook(excelFile)) {

					Sheet sheet = workbook.getSheetAt(0);
					Iterator<Row> iterator = sheet.iterator();
					iterator.next();
					// Truncate table staging
					connection.Truncate_staging();

					while (iterator.hasNext()) {
						Row currentRow = iterator.next();
						String tour_name = currentRow.getCell(0).getStringCellValue();
						String image_tour = currentRow.getCell(1).getStringCellValue();
						String location_name = currentRow.getCell(2).getStringCellValue();
						String province = currentRow.getCell(3).getStringCellValue();
						String vehicle = currentRow.getCell(4).getStringCellValue();
						String price = currentRow.getCell(5).getStringCellValue();
						String description = currentRow.getCell(6).getStringCellValue();
						String start_point = currentRow.getCell(7).getStringCellValue();
						String destination = currentRow.getCell(8).getStringCellValue();
						String duration_days = currentRow.getCell(9).getStringCellValue();
						String start_date = currentRow.getCell(10).getStringCellValue();
						String end_date = currentRow.getCell(11).getStringCellValue();

						// Load dữ liệu từ excel vào staging.db
						connection.LoadStaging(tour_name, image_tour, location_name, province, vehicle, price,
								description, start_point, destination, duration_days, start_date, end_date);
					}
				} catch (Exception e) {
					// Gặp lỗi cập nhật status
					connection.updateStatusConfig("EXTRACT_ERROR", id);
					connection.log(id, "Log of extract", "EXTRACT_ERROR", "Cannot extract data " + e.getMessage(),
							"extract_script");
					SendMail.sendMail(currentEmail, extractError + currentDate, "Cannot extract data " + e.getMessage(),
							null);

					continue;
				}
				connection.updateStatusConfig("EXTRACT_COMPLETED", id);
				connection.log(id, "Log of extract", "EXTRACT_COMPLETED", "EXTRACT COMPLETED!", "extract_script");
				file.deleteOnExit();
				connection.closeStaging();

			}
			connection.closeConnection();

		} catch (RuntimeException | SQLException e) {
			e.getMessage();
		}
	}

	public static void main(String[] args) {
		Extract extract = new Extract();
		extract.extract();
	}
}

