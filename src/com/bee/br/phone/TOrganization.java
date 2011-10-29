package com.bee.br.phone;

/**
 * @author xiangyuan
 *
 */
public class TOrganization extends TItem implements IOrganization {
	private String company;
	private String title, department;
	private String jobDescription, symbol, phoneticName, officeLocation;
	
	@Override
	public String getCompany() {
		return company;
	}
	@Override
	public void setCompany(String company) {
		this.company = company;
	}
	@Override
	public String getTitle() {
		return title;
	}
	@Override
	public void setTitle(String title) {
		this.title = title;		
	}
	
	@Override
	public String getDepartment() {
		return department;
	}
	@Override
	public void setDepartment(String department) {
		this.department = department;
	}
	@Override
	public String getJobDescription() {
		return jobDescription;
	}
	@Override
	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}
	@Override
	public String getSymbol() {
		return symbol;
	}
	@Override
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	@Override
	public String getPhoneticName() {
		return phoneticName;
	}
	@Override
	public void setPhoneticName(String phoneticName) {
		this.phoneticName = phoneticName;
	}
	@Override
	public String getOfficeLocation() {
		return officeLocation;
	}
	@Override
	public void setOfficeLocation(String officeLocation) {
		this.officeLocation = officeLocation;		
	}
}