package org.training;

import java.io.IOException;
import java.util.Scanner;

public class WebCrawler {

	private static final String URL = "http://mail-archives.apache.org/mod_mbox/maven-users/";
	private static Scanner sc;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String args[]) {
		sc = new Scanner(System.in);
		System.out.println("Enter the year :");
		String year = sc.next();
		MailDownloader mailDownloader = new MailDownloader();
		mailDownloader.connectToPageAndDownloadMails(URL,year);
	}
}
