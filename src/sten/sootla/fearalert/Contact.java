package sten.sootla.fearalert;

public class Contact implements Comparable<Contact> {

	private String name;
	private String number;
	private boolean call;
	private boolean sms;
	private String smsContent;
	private boolean sendLocation;
	
	public int compareTo(Contact c) {
		if (call) {
			return -1;
		} else if (c.isCall()) {
			return 1;
		} else if (sms && !c.isSMS()) {
			return -1;
		} else if (!sms && c.isSMS()) {
			return 1;
		} else {
			return name.compareTo(c.getName());
		}
	}
	
	public Contact (String name, String number, boolean call, boolean sms, String smsContent, boolean sendLocation) {
		this.name = name;
		this.number = number;
		this.call = call;
		this.sms = sms;
		this.smsContent = smsContent;
		this.sendLocation = sendLocation;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNumber() {
		return number;
	}
	
	public boolean isCall() {
		return call;
	}
	
	public boolean isSMS() {
		return sms;
	}
	
	public String getSMSContent() {
		return smsContent;
	}
	
	public boolean isSendLocation() {
		return sendLocation;
	}
	
	public String toString() {
		return this.name;
	}
	
	public String getFormatedNumber() {
		return number.replaceAll("\\s+","");
	}
}
