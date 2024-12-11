package model;

import java.util.Date;

public class Tour {
	private String tour_name;
	private String image_tour;
	private String location_name;
	private String province;
	private String vehicle;
	private String price;
	private String description;
	private String start_point;
	private String destination;
	private String duration_days;
	private String start_date;
	private String end_date;

	public Tour(String tour_name, String image_tour, String location_name, String province, String vehicle,
			String price, String description, String start_point, String destination, String duration_days,
			String start_date, String end_date) {
		this.tour_name = tour_name;
		this.image_tour = image_tour;
		this.location_name = location_name;
		this.province = province;
		this.vehicle = vehicle;
		this.price = price;
		this.description = description;
		this.start_point = start_point;
		this.destination = destination;
		this.duration_days = duration_days;
		this.start_date = start_date;
		this.end_date = end_date;
	}

	public String getTour_name() {
		return tour_name;
	}

	public void setTour_name(String tour_name) {
		this.tour_name = tour_name;
	}

	public String getImage_tour() {
		return image_tour;
	}

	public void setImage_tour(String image_tour) {
		this.image_tour = image_tour;
	}

	public String getLocation_name() {
		return location_name;
	}

	public void setLocation_name(String location_name) {
		this.location_name = location_name;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getVehicle() {
		return vehicle;
	}

	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStart_point() {
		return start_point;
	}

	public void setStart_point(String start_point) {
		this.start_point = start_point;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDuration_days() {
		return duration_days;
	}

	public void setDuration_days(String duration_days) {
		this.duration_days = duration_days;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	@Override
	public String toString() {
		return "Tour{" + "tour_name='" + tour_name + '\'' + ", image_tour='" + image_tour + '\'' + ", location_name='"
				+ location_name + '\'' + ", province='" + province + '\'' + ", vehicle='" + vehicle + '\'' + ", price="
				+ price + ", description='" + description + '\'' + ", start_point='" + start_point + '\''
				+ ", destination='" + destination + '\'' + ", duration_days=" + duration_days + ", start_date="
				+ start_date + ", end_date=" + end_date + '}';
	}
}
