package main.common;

public class Protocol {
	public static final int OK					= 10;
	public static final int KO 					= 20;
	public static final int REPLY_PORT 			= 5;
	
	public static final int REQ_TEXT			= 1001;
	public static final int REQ_PUBLIC_KEY 		= 1000;
	public static final int REPLY_PUBLIC_KEY 	= 2000;
	public static final int REPLY_TEXT			= 2001;

	public static final int REQ_CONNECT 		= 8000;
	public static final int REQ_REGISTER 		= 8001;

}
