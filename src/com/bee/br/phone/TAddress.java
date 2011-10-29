package com.bee.br.phone;

public class TAddress extends TItem implements IAddress {
	private String street, city, region, country;
	private String formattedAddress;
	private String postcode, pobox, neighborhood;
	
	@Override
	public String getStreet() {
		return street;
	}
	
	@Override
	public void setStreet(String street) {
		this.street = street;
	}

	@Override
	public String getCity() {
		return city;
	}

	@Override
	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
	}
	
	@Override
	public String getPostcode() {
		return postcode;
	}

	@Override
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	@Override
	public String getFormattedAddress() {
		return formattedAddress;
	}

	@Override
	public void setFormattedAddress(String address) {
		this.formattedAddress = address;
	}

	@Override
	public String getRegion() {
		return region;
	}

	@Override
	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public String getPobox() {
		return pobox;
	}

	@Override
	public void setPobox(String pobox) {
		this.pobox = pobox;
	}

	@Override
	public String getNeighborhood() {
		return neighborhood;
	}

	@Override
	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}
}