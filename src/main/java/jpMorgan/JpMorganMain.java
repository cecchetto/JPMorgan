package jpMorgan;

import jpMorgan.thread.RealizeReportThread;


public class JpMorganMain {

	
	
	public static void main(String[] args) {

		/**
		 * RUN THREAD FOR ELABORATE THE REPORT BASED ON EXCEL INPUT**/
		RealizeReportThread reportThread = new RealizeReportThread();
		reportThread.run();
	}
}
