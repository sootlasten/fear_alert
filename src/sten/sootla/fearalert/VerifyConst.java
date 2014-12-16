package sten.sootla.fearalert;

public enum VerifyConst {
	OK,
	EMPTY_NAME,
	EMPTY_NUMBER,
	BADLY_FORMATTED_NUMBER,
	NAME_TAKEN,
	NUMBER_TAKEN,
	CALL_TAKEN,
	NO_SMS_CONTENT;
	
	@Override
	public String toString() {
		switch(this) {
		case EMPTY_NAME:
			return "Please specify the name of the contact.";
		case EMPTY_NUMBER:
			return "Please specify the phone number of the contact.";
		case BADLY_FORMATTED_NUMBER:
			return "Number is incorrectly formatted, should contain only numbers.";
		case NAME_TAKEN:
			return "Name already assigned to another contact.";
		case NUMBER_TAKEN:
			return "Number already assigned to another contact.";
		case CALL_TAKEN:
			return "You have already specified another contact to call to.";
		case NO_SMS_CONTENT:
			return "Please specify the content of the SMS.";
		default: throw new IllegalArgumentException();
		}
	}
}
