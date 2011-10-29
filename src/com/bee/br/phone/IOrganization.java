package com.bee.br.phone;

public interface IOrganization extends Item {	
	/**
	 * e.g. the school name or the company name
	 */
	public String getCompany();
	public void setCompany(String company);
	
	String getTitle();
	void setTitle(String title);
	
	String getDepartment();
	void setDepartment(String department);
	
	String getJobDescription();
	void setJobDescription(String jobDescription);
	
	String getSymbol();
	void setSymbol(String symbol);
	
	String getPhoneticName();
	void setPhoneticName(String phoneticName);
	
	String getOfficeLocation();
	void setOfficeLocation(String officeLocation);
}
