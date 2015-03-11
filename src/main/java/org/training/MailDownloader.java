package org.training;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MailDownloader {
	
	private Document document;
	private String year;
	private String month;
	private int fileCount;
	private final String DESTINATION = "D:\\Crawler\\";
	private final static Logger logger = Logger.getLogger(MailDownloader.class);

	public void connectToPageAndDownloadMails(String pageUrl, String year) {
		Document document;
		Elements elementsofMonths;
		Elements elementsofMails;
		String absUrlofMonth;
		String absUrlofMail;
				
		document = connectToPage(pageUrl);
		this.year = year;
		
		elementsofMonths = document.getElementsByAttributeValueContaining("href", year);
		
		new File(DESTINATION + year).mkdir();
		
		for (Element eleofMonths : elementsofMonths) {

			absUrlofMonth = eleofMonths.absUrl("href");

			if (absUrlofMonth.contains(year) && absUrlofMonth.contains("thread")) {

				document = connectToPage(absUrlofMonth);

				month = document.getElementById("boxactive").getElementsByTag("a").text();

				new File(DESTINATION + year + "\\" + month).mkdir();

				fileCount = 1;

				for (int j = 0; j < document.getElementsByClass("pages").size(); j++) {

					document = connectToPage(absUrlofMonth + "?" + j);
					
					elementsofMails = document.getElementById("msglist").getElementsByTag("a");
					
					for (Element eleofMails : elementsofMails) {
						absUrlofMail = eleofMails.absUrl("href");
						downloadMail(absUrlofMail);
					}
				}
			}
		}
	}
	

	public Document connectToPage(String url) {
		Document document = null;
		try {
			logger.debug("Connectiong to :" + url);
			document = Jsoup.connect(url).get();
			logger.debug("Connected to :" + url);
		} catch (IOException ex) {
			logger.error("Exception occured while connecting to " + url, ex);
		}
		return document;
	}
	
	public void downloadMail(String absUrlofMail) {
		BufferedWriter writer = null;
		File file;
		Element message;
		if (absUrlofMail.contains(year) && absUrlofMail.contains("%")) {
			document = connectToPage(absUrlofMail);
			message = document.select("#msgview > tbody").get(0);
			file = new File(DESTINATION + year + "\\" + month + "\\" + "File-"+ (fileCount++) + ".txt");
			try {
				writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
				writer.write(message.text());
			} catch (IOException ex) {
				logger.error("Exception occured while writing message in to the file", ex);
			} finally {
				try {
					writer.close();
				} catch (IOException ex) {
					logger.error("Exception occured while closing the writer", ex);
				}
			}
		}
	}
}
