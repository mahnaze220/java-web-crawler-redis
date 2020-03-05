package com.sample.crawler.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.sample.crawler.model.CrawlerResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * This service uses JSoup library for fetching web page content to extract top javaScript libraries used in them. 
 * It uses the ExecutorService to create a thread pool to downloading and parsing pages in multi-threading tasks. 
 *  
 * @author Mahnaz
 * @Mar 01, 2020
 */

@Slf4j
@Service
public class WebCrawlerService {

	//the pattern for web page urls 
	private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}");

	private static final String HREF = "href";
	private static final String A_HREF = "a[href]";
	private static final String SLASH = "/";
	private static final String DOT = ".";
	private static final String SRC = "src";
	private static final String SCRIPT = "script";
	private static final String JS = ".js";

	private static final String VALID_URL = "/url?q=";
	private static final String WWW = "www.";
	private static final String GOOGLE_URL = "https://www.google.com/search?q=";
	private static final String PAGE_SIZE = "&num=20";
	private static final String BOT = "Chrome/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
	private static final String CHROME = "Chrome";	
	private static final int TIMEOUT = 500000;
	private static final int TOP_NUMBER = 5;

	// this map holds all javaScript libraries used in visited web pages
	private ConcurrentHashMap<String, Integer> javaScriptLibraries = new ConcurrentHashMap<>();

	private ExecutorService executorService;
	private final Object lock = new Object();
	private AtomicInteger pending = new AtomicInteger(0);

	/**
	 * Do web crawling and extract list of top javaScript libraries from downloaded pages 
	 * @param searchQuery
	 * @param numberOfThreads
	 * @return CrawlerResponse
	 */
	public CrawlerResponse getTopJSLibraries(String searchQuery, int numberOfThreads) {

		log.info("Getting top java script libraries in the pages fetched by google based on "
				+ "seach query: {} and number of threads: {}", searchQuery, numberOfThreads);

		//create a connection pool with the specific number of threads
		this.executorService = Executors.newFixedThreadPool(numberOfThreads);

		//get a Google result page for the input search query and extract result links 
		Elements links = getPagesFromGoogle(searchQuery);

		// process each extracted link, download the page and extract used javaScript libraries 
		processLinks(links);

		//shutdown executorService
		shutdown();

		//extract top 5 javaScript libraries from the map
		return new CrawlerResponse(getTopLibraries(javaScriptLibraries));
	}

	public Elements getPagesFromGoogle(String query) {

		//create google query for requested search query
		String request = GOOGLE_URL + query + PAGE_SIZE;
		log.info("Fetching pages... {}", request);

		try {
			// use Google bot agent
			Document doc = Jsoup.connect(request)
					.userAgent(BOT)
					.timeout(TIMEOUT).get();

			// get all main links
			return doc.select(A_HREF);
		} catch (Exception e) {
			log.error("Exception in connectiong to Google: {}", e.getMessage());
		}
		return null;
	}

	public Set<String> processLinks(Elements links) {
		Set<String> visitedSites = new HashSet<>();
		links.stream().forEach(link -> {
			String url = link.attr(HREF);

			//validate url by the pattern and extract domain name
			String domainName = getDomainNameAndValidate(url);

			if(domainName != null && !visitedSites.contains(domainName)) {    
				visitedSites.add(domainName);
				pending.incrementAndGet();

				//download each page in a new thread
				processLinkInNewThread(url);
			}
		});
		return visitedSites;
	}

	public void processLinkInNewThread(String url) {

		//get a thread from thread pool for downloading a web page
		if(executorService != null) {
			executorService.execute(new Runnable() {
				public void run() {
					downloadPage(url);
					pending.decrementAndGet();
					if (pending.get() == 0) {
						synchronized (lock) {
							lock.notify();
						}
					}
				}
			});
		}
	}

	public String getDomainNameAndValidate(String link) {

		if (link.startsWith(VALID_URL)) {			
			String domainName = getDomainName(link);
			log.info(domainName);

			//to not process duplicate pages, check the url with/without www 
			if (domainName != null && !domainName.contains(WWW)) {
				domainName = WWW + domainName;
			}
			return domainName;
		}
		else {
			return null;
		}
	}

	public String getDomainName(String url) {
		String domainName = null;

		// use regex to get domain name
		Matcher matcher = DOMAIN_NAME_PATTERN.matcher(url);
		if (matcher.find()) {
			domainName = matcher.group(0).toLowerCase().trim();
		}
		return domainName;
	}

	public void downloadPage(String str) {

		//get url from the google result link
		String url = str.substring(str.indexOf(VALID_URL) + 7);
		url = url.substring(0, url.indexOf(SLASH, url.indexOf(DOT)));

		Document doc;
		try {
			// connect to the url
			doc = Jsoup.connect(url)
					.timeout(TIMEOUT)
					.userAgent(CHROME)
					.get();

			//get script tag and it's src value with .js extension
			doc.select(SCRIPT)
			.stream()
			.map(element -> element.attr(SRC))
			.filter(src -> !StringUtil.isBlank(src) && src.contains(JS))
			.forEach(src -> {
				log.info(src);

				//extract javaScript library name from the url
				src = src.substring(src.lastIndexOf(SLASH) + 1, src.lastIndexOf(JS) + 3);

				//put javaScript library name into map or if exists, increment the count
				if (!javaScriptLibraries.containsKey(src)) {
					javaScriptLibraries.put(src, 1);
				} else {
					javaScriptLibraries.put(src, javaScriptLibraries.get(src) + 1);
				}
			});
		} catch (Exception e) {
			log.error("Exception in connecting to the url: {}, {}", url, e.getMessage());
		}
	}

	public Set<String> getTopLibraries(ConcurrentMap<String, Integer> libraries) {

		//get top 5 library based on their usages in web pages
		Map<String, Integer> result = libraries.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(TOP_NUMBER)
				.collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		return result.keySet();
	}

	public void shutdown() {
		executorService.shutdown();
		try {
			executorService.awaitTermination(4, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}
}