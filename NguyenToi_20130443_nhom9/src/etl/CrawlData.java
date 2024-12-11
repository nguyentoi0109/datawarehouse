package etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import db.DatabaseConn;
import model.Tour;
import util.SendMail;
public class CrawlData {
	static String location = "";
	static String name = "";
	private static final String[] fieldNames = { "tour_name", "image_tour", "location_name", "province", "vehicle",
			"price", "description", "start_point", "destination", "duration_days", "start_date", "end_date" };

	public void cralwData() throws SQLException {
		String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

		String cnError = "Cannot connected! ";
		String crawlError = "CRAWL_ERROR! ";
		String currentEmail = "nnnnn01092002@gmail.com";

		DatabaseConn connection = new DatabaseConn();
		// kết nối db control
		connection.connectToControl();

		Connection control = connection.getControlConn();

		if (control == null) {
			SendMail.sendMail(currentEmail, cnError + currentDate, crawlError, currentEmail);
			return;
		}

		String getConfig = "SELECT * FROM configs WHERE flag = 1 AND status = 'PREPARED'";
		List<Map<String, Object>> listConfig = connection.query(getConfig);

		// Chạy từng dòng config
		for (Map<String, Object> config : listConfig) {
			String id = config.get("id").toString();
			name = config.get("name").toString();

			File file = new File("C:\\Users\\DELL\\Desktop\\tour.csv");
			// Nếu file đã tồn tại thì xóa đi để crawl dữ liệu mới
			if (file.exists()) {
				file.delete();
			}

			try {
				// Cập nhật status
				connection.updateStatusConfig("CRAWLING", id);
				getData();
			} catch (Exception e) {
				// Gặp lỗi cập nhật status
				connection.updateStatusConfig("CRAWL_ERROR", id);
				connection.log(id, "Log of crawler", "CRAWL_ERROR", "Cannot crawl data! " + e.getMessage(), "Crawler");
				SendMail.sendMail(currentEmail, crawlError + currentDate,
						"Cannot crawl data! " + "\n Exception: " + e.getMessage(), "Crawl data exception");
				return;
			}

			connection.updateStatusConfig("CRAWL_COMPLETED", id);
			connection.log(id, "Log of crawler", "CRAWL_COMPLETED", "Crawl complete", "Crawler");
		}

		// Đóng kết nối control
		connection.closeConnection();
	}

	public void getData() {
		try {
			int page = 1;
			while (true) {
				String url = "https://saigontourist.net/vi/tour-trong-nuoc?limit=20&page=" + page;
				Document doc = Jsoup.connect(url).get();
				Elements tours = doc.select(".box-search-tour");

				if (tours.isEmpty()) {
					System.out.println("No more data. Stopping at page " + page);
					break;
				}

				// Xử lý và in dữ liệu cho mỗi tour
				for (Element tour : tours) {
					String tourName = tour.select("a").attr("title");
					String tourLink = tour.select("a").attr("href");
					String detailLink = "https://saigontourist.net" + tourLink;
					String tourPrice = tour.select("span.price").text();

					String imageSrc = tour.select(".tour-image").attr("src");
					if (!imageSrc.startsWith("http")) {
						imageSrc = "https://saigontourist.net" + imageSrc;
					}

					String location_name = tour.select(".destination-tour").text();

					// In thông tin cơ bản của tour
					System.out.println("Tour: " + tourName);
					System.out.println("Link: " + tourLink);
					System.out.println("Price: " + tourPrice);
					System.out.println("Image: " + imageSrc);
					System.out.println("Location Name: " + location_name);

					// Lấy thông tin chi tiết của tour
					try {
						// Kết nối đến link chi tiết của tour
						Document detailDocument = Jsoup.connect(detailLink).get();

						// Lấy mã tour
						String tourCode = "";
						Elements maTourElement = detailDocument.select(".col-md-2.col-sm-3.col-xs-12");
						for (Element element : maTourElement) {
							String label = element.select("label").text();
							if (label.equals("Mã Tour")) {
								tourCode = element.select("strong").text();
								break;
							}
						}

						// Lấy mô tả
						String description = detailDocument.select(".content-description").text();

						// Lấy tỉnh thành (Điểm đến)
						String provine = "";
						Element provines = detailDocument.selectFirst(
								"div.col-md-6.col-sm-6.col-xs-12:has(span.text-uppercase:contains(Điểm đến:))");
						if (provines != null) {
							Element strongSpan = provines.selectFirst("span.text-strong");
							if (strongSpan != null) {
								provine = strongSpan.text().trim();
							}
						}

						// Lấy phương tiện
						String vehicle = "";
						Element vehicleElement = detailDocument.selectFirst(
								"div.col-md-6.col-sm-6.col-xs-12:has(span.text-uppercase:contains(Phương tiện:))");
						if (vehicleElement != null) {
							Element strongSpan = vehicleElement.selectFirst("span.text-strong");
							if (strongSpan != null) {
								vehicle = strongSpan.text().trim();
							}
						}

						// Lấy ngày khởi hành
						String startDate = "";
						for (Element element : maTourElement) {
							String label = element.select("label").text();
							if (label.equals("Ngày khởi hành")) {
								startDate = element.select("strong").text();
								break;
							}
						}

						// Lấy điểm xuất phát
						String startLocation = "";
						Element startLocationElement = detailDocument.selectFirst(
								"div.col-md-6.col-sm-6.col-xs-12:has(span.text-uppercase:contains(Điểm xuất phát:))");
						if (startLocationElement != null) {
							startLocation = startLocationElement.selectFirst("span.text-strong").text().trim();
						}

						// Lấy điểm đến
						String destination = "";
						Element destinationElement = detailDocument.selectFirst(
								"div.col-md-6.col-sm-6.col-xs-12:has(span.text-uppercase:contains(Điểm đến:))");
						if (destinationElement != null) {
							Element strongSpan = destinationElement.selectFirst("span.text-strong");
							if (strongSpan != null) {
								destination = strongSpan.text().trim();
							}
						}

						// Lấy số ngày của tour
						String durationDays = "";
						Element numDate = detailDocument.selectFirst(
								"div.col-md-6.col-sm-6.col-xs-12:has(span.text-uppercase:contains(Thời gian:))");
						if (numDate != null) {
							String timeText = numDate.selectFirst("span.text-strong").text();
							String number = timeText.replaceAll("\\D+", "").substring(0, 1);
							durationDays = number + " day";
						}

						// Tính toán ngày về
						LocalDate startLocalDate = null;
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						try {
							startLocalDate = LocalDate.parse(startDate, formatter);
						} catch (Exception e) {
							System.out.println("Error parsing start date: " + startDate);
						}

						String returnDate = "";
						if (startLocalDate != null && !durationDays.isEmpty()) {
							int days = Integer.parseInt(durationDays.replaceAll("\\D+", ""));
							LocalDate returnLocalDate = startLocalDate.plusDays(days);
							returnDate = returnLocalDate.format(formatter);
						}

						// In thông tin chi tiết
						System.out.println("Mã Tour: " + tourCode);
						System.out.println("Mô tả: " + description);
						System.out.println("Province: " + provine);
						System.out.println("Vehicle: " + vehicle);
						System.out.println("Ngày khởi hành: " + startDate);
						System.out.println("Ngày về: " + returnDate);
						System.out.println("Điểm xuất phát: " + startLocation);
						System.out.println("Destination: " + destination);
						System.out.println("Duration: " + durationDays);

						SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
						// Định dạng đầu ra
						SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
						// Chuyển chuỗi thành đối tượng Date
						Date start_date = inputFormat.parse(startDate);
						Date return_date = inputFormat.parse(returnDate);

						// Chuyển Date thành chuỗi định dạng mới
						String formattedStart = outputFormat.format(start_date);
						String formattedReturn = outputFormat.format(return_date);

						// Tạo đối tượng Tour chứa tất cả thông tin
						Tour fullTourDetails = new Tour(tourName, imageSrc, location_name, provine, vehicle, tourPrice,
								description, startLocation, destination, durationDays, formattedStart, formattedReturn);

						// Lưu thông tin tour vào tệp (sử dụng saveToFile)
						saveToFile(fullTourDetails);

						System.out.println("==========================");

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				page++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Tạo file
	public void saveToFile(Tour tour) throws IOException {
		try {
			String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			String excelFilePath = "C:\\Users\\DELL\\Desktop\\tour.csv";

			Workbook workbook = getWorkbook(excelFilePath);
			Sheet sheet = (Sheet) workbook.getSheetAt(0);

			int lastRowIndex = sheet.getLastRowNum();

			Row rowToInsert = sheet.getRow(lastRowIndex + 1);
			if (rowToInsert == null)
				rowToInsert = sheet.createRow(lastRowIndex + 1);
			Field[] fields = tour.getClass().getDeclaredFields();

			for (int i = 0; i < fields.length; i++) {
				Cell cell1 = rowToInsert.createCell(i);
				Field field = fields[i];
				field.setAccessible(true);
				cell1.setCellValue(field.get(tour).toString());
			}

			for (int i = 0; i < 16; i++) {
				sheet.autoSizeColumn(i);
			}

			FileOutputStream outputStream = new FileOutputStream(excelFilePath);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

			System.out.println("Success");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Tạo file
	private static Workbook getWorkbook(String excelFilePath) throws IOException {
		File file = new File(excelFilePath);
		boolean fileExists = file.exists();

		if (!fileExists) {
			XSSFWorkbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Data sheet");

			Row headerRow = sheet.createRow(0);

			for (int i = 0; i < fieldNames.length; i++)
				headerRow.createCell(i).setCellValue(fieldNames[i]);
			FileOutputStream outputStream = new FileOutputStream(excelFilePath);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		}
		FileInputStream inputStream = new FileInputStream(excelFilePath);
		return new XSSFWorkbook(inputStream);
	}

	public static void main(String[] args) throws SQLException {
		CrawlData crawlData = new CrawlData();
		crawlData.cralwData();
	}
}

