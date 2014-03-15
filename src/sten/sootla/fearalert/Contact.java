package sten.sootla.fearalert;

public class Contact implements Comparable<Contact> {

	private String name;
	private String number;
	private boolean call;
	private boolean sms;
	private String smsContent;
	
	public int compareTo(Contact c) {
		if (call) {
			return -1;
		} else if (c.getCall()) {
			return 1;
		} else if (sms && !c.getSMS()) {
			return -1;
		} else if (!sms && c.getSMS()) {
			return 1;
		} else {
			return name.compareTo(c.getName());
		}
	}
	
	public Contact (String name, String number, boolean call, boolean sms, String smsContent) {
		this.name = name;
		this.number = number;
		this.call = call;
		this.sms = sms;
		this.smsContent = smsContent;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNumber() {
		return number;
	}
	
	public boolean getCall() {
		return call;
	}
	
	public boolean getSMS() {
		return sms;
	}
	
	public String getSMSContent() {
		return smsContent;
	}
	
	public String toString() {
		return this.name;
	}
	
	public String getFormatedNumber() {
		return number.replaceAll("\\s+","");
	}
}
