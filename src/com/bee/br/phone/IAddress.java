package com.bee.br.phone;

public interface IAddress extends Item {	
	public String getFormattedAddress();
	public void setFormattedAddress(String address);
	
	public String getStreet();
	public void setStreet(String street);
	public String getCity();
	public void setCity(String city);
	
	public String getRegion();
	public void setRegion(String region);
	public String getCountry();
	public void setCountry(String country);
	public String getPostcode();
	public void setPostcode(String postcode);
	
	String getPobox();
	void setPobox(String pobox);
	
	String getNeighborhood();
	void setNeighborhood(String neighborhood);
}
