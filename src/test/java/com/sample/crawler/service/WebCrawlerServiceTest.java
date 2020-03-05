package com.sample.crawler.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This unit test class contains test scenarios for WebCrawlerService class.
 * 
 * @author Mahnaz
 * @Mar 01, 2020
 */

@SpringBootTest
public class WebCrawlerServiceTest {

	@Autowired
	private WebCrawlerService crawlerService;

	@Test
	public void processLinks_whenSendValidAndInvalidUrls_thenReturnValidUrls() {
		Elements links = new Elements();

		Attributes attrs1 = new Attributes();
		attrs1.add("href", "search?q=java&amp;num=20&amp;gbv=1&amp;sei=5d9AE");		
		links.add(new Element(Tag.valueOf("<a href=/search?q=java&amp;num=20&amp;gbv=1&amp;sei=5d9AE\"></a>"),
				"", attrs1));

		Attributes attrs2 = new Attributes();
		attrs2.add("href", "/url?q=https://www.oracle.com/&amp;sa=U&amp;");		
		links.add(new Element(Tag.valueOf("<a href=/url?q=https://www.oracle.com/&amp;sa=U&amp;\"></a>"),
				"", attrs2));

		Attributes attrs3 = new Attributes();
		attrs3.add("href", "/url?q=https://java.com/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb");		
		links.add(new Element(Tag.valueOf("<a href=/url?q=https://java.com/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb\"></a>"),
				"", attrs3));

		Attributes attrs4 = new Attributes();
		attrs4.add("href", "/url?q=https://www.java.com/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb");		
		links.add(new Element(Tag.valueOf("<a href=/url?q=https://www.java.com/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb\"></a>"),
				"", attrs4));

		WebCrawlerService crawlerServiceSpy = Mockito.spy(crawlerService);
		Mockito.doNothing().when(crawlerServiceSpy).processLinkInNewThread(Mockito.anyString());
		Set<String> pages = crawlerService.processLinks(links);
		Assertions.assertEquals(true, pages.size() == 2);
	}

	@Test
	public void getDomainNameAndValidate_whenSendValidUrl_thenReturnDomainName() {
		String link = "/url?q=https://www.java.com/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb";
		String result = crawlerService.getDomainNameAndValidate(link);
		Assertions.assertNotNull(result);
	}

	@Test
	public void getDomainNameAndValidate_whenSendValidUrlWithWWW_thenReturnDomainName() {
		String link = "/url?q=https://java.com/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb";
		String result = crawlerService.getDomainNameAndValidate(link);
		Assertions.assertEquals("www.java.com", result);
	}

	@Test
	public void getDomainNameAndValidate_whenSendInvalidUrl_thenReturnNullDomainName() {
		String link = "search?q=java&amp;num=20&amp;gbv=1&amp;sei=5d9AE";
		String result = crawlerService.getDomainNameAndValidate(link);
		Assertions.assertNull(result);
	}

	@Test
	public void getDomainName_whenSendValidUrl_thenReturnDomainName() {
		String link = "/url?q=https://java.com/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb";
		String result = crawlerService.getDomainName(link);
		Assertions.assertEquals("java.com", result);
	}

	@Test
	public void getDomainName_whenSendInValidUrl_thenReturnNullDomainName() {
		String link = "/url?q=https://wwww-java/&amp;sa=U&amp;ved=2ahAB&amp;usg=APzvKtb";
		String result = crawlerService.getDomainName(link);
		Assertions.assertNull(result);
	}

	@Test
	public void getTopLibraries_whenAddSomeLibraries_thenReturnTopLibrary() {
		ConcurrentHashMap<String, Integer> libraries = new ConcurrentHashMap<String, Integer>();
		libraries.put("odc.js", 7);
		libraries.put("s_code_remote.js", 1);
		libraries.put("subtlePager.js", 1);
		libraries.put("global.js", 2);
		libraries.put("apmeum.js", 1);
		libraries.put("odc_v1.js", 1);
		libraries.put("apmeum001.js", 1);
		Set<String> result = crawlerService.getTopLibraries(libraries);
		Assertions.assertTrue(result.contains("odc.js"));
	}
}
