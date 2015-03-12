package org.training;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
	private final static Logger logger = Logger.getLogger(MailDownloader.class);
	private int tryCount = 0;
	private List<String> listOfUrls; 
	private File file;
	private Writer writer;
	private int index;
	private int count;
	private WebCrawlerPropertiesBn webCrawlerPropBn;
	
	public WebCrawlerPropertiesBn getWebCrawlerPropBn() {
		return webCrawlerPropBn;
	}

	public void setWebCrawlerPropBn(WebCrawlerPropertiesBn webCrawlerPropBn) {
		this.webCrawlerPropBn = webCrawlerPropBn;
	}

	public void connectToPageAndDownloadMails(String year) throws IOException {
		Document document;
		Elements elementsofMonths;
		Elements elementsofMails;
		String absUrlofMonth;
		String absUrlofMail;
		this.year = year;
				
		document = connectToPage(webCrawlerPropBn.getPageUrl());
		elementsofMonths = document.getElementsByAttributeValueContaining("href", year);
		new File(webCrawlerPropBn.getDestination() + year).mkdir();
		
		for (Element eleofMonths : elementsofMonths) {
			absUrlofMonth = eleofMonths.absUrl("href");
			if (absUrlofMonth.contains(year) && absUrlofMonth.contains("thread")) {
				document = connectToPage(absUrlofMonth);
				month = document.getElementById("boxactive").getElementsByTag("a").text();
				new File(webCrawlerPropBn.getDestination() + year + "\\" + month).mkdir();
				fileCount = 1;
				index = 0;
				listOfUrls = new ArrayList<String>();
				for (int j = 0; j < document.getElementsByClass("pages").size(); j++) {
					document = connectToPage(absUrlofMonth + "?" + j);
					elementsofMails = document.select("#msglist > tbody").get(0).getElementsByTag("a");
					for (Element eleofMails : elementsofMails) {
						absUrlofMail = eleofMails.absUrl("href");
						listOfUrls.add(index++,absUrlofMail);
					}
				}
				if(isFileExist(webCrawlerPropBn.getResumeFileName())) {
					String downloadUrl = readContentFromFile(webCrawlerPropBn.getResumeFileName());
					resumeDownload(downloadUrl);
				} else {
				for(count = 0; count < index; count++) {
					downloadMail(listOfUrls.get(count));
				}
			  }
			}
		}
	}
	
	public Document connectToPage(String url) throws IOException {
		Document document = null;
		try {
			logger.debug("Connectiong to :" + url);
			document = Jsoup.connect(url).get();
			logger.debug("Connected to :" + url);
		} catch (IOException ex) {
			logger.error("Exception occured while connecting to " + url, ex);
			while(tryCount < webCrawlerPropBn.getNumberOfRetries()) {
				logger.debug("Retrying to connect to: "+ url);
				tryCount++;
				connectToPage(url);
			}
			if(tryCount == webCrawlerPropBn.getNumberOfRetries()) {
				logger.debug("Retried to connect "+ tryCount + "no.of times. But not able to connect.");
				try {
				file = new File(webCrawlerPropBn.getResumeFileName());
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(url);
				} catch(IOException io) {
					logger.error("Exception occured while writing the content in to file", io);
				} finally {
					writer.close();
				}
	            throw ex;
			}
		}
		return document;
	}
	
	public void downloadMail(String absUrlofMail) throws IOException {
		Element message = null;
		document = connectToPage(absUrlofMail);
		message = document.select("#msgview > tbody").get(0);
		file = new File(webCrawlerPropBn.getDestination() + year + "\\" + month + "\\" + "File-"+ (fileCount++) + ".txt");
		try {
			writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			writer.write(message.text());
		} catch (IOException ex) {
			logger.error("Exception occured while writing message in to the file",ex);
		} finally {
			try {
				writer.close();
			} catch (IOException ex) {
				logger.error("Exception occured while closing the writer", ex);
			}
		}
	}
	
	public boolean isFileExist(String fileName) {
		file = new File(fileName);
		if(file.exists()) {
			return true;
		}
		return false;
	}
	
	public String readContentFromFile(String fileName) {
		file = new File(fileName);
		BufferedReader reader = null;
		String resumedUrl = null;
		if(file.exists()) {
			try{
			reader = new BufferedReader( new FileReader (file));
			resumedUrl = reader.readLine();
			} catch(IOException ex) {
				
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			file.delete();
		}
		return resumedUrl;
	}
	
	public void resumeDownload(String downloadUrl) throws IOException {
		int startPointOfDownload = -1;
		for(count = 0; count < index; count++) {
			if(downloadUrl.equals(listOfUrls.get(count))) {
				startPointOfDownload = count;
				file = new File(webCrawlerPropBn.getResumeFileName());
				file.delete();
			}
		}
		if(startPointOfDownload != -1) {
			for(count = startPointOfDownload; count < index; count++) {
				downloadMail(listOfUrls.get(count));
			}
		}
	}
}
