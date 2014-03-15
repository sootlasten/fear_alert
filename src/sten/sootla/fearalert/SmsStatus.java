package sten.sootla.fearalert;

public enum SmsStatus {
	SENDING,
	SENT, 
	FAILED_TO_SEND,
	DELIVERED, 
	FAILED_TO_DELIVER;
	
	@Override
	public String toString() {
		switch(this) {
		case SENDING: return "Sending...";
		case SENT: return "SMS sent. Delivering...";
		case FAILED_TO_SEND: return "SMS failed to send.";
		case DELIVERED: return "SMS delivered.";
		case FAILED_TO_DELIVER: return "SMS failed to deliver."; 
		default: throw new IllegalArgumentException();
		}
	}
}
