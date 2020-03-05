package com.sample.crawler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sample.crawler.model.CrawlerResponse;
import com.sample.crawler.service.WebCrawlerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This controller provides a web crawler service for fetching 5 top JavaScript libraries used in
 * the web pages found on Google base on an input search query.
 *
 * @author Mahnaz
 * @Mar 01, 2020
 */

@Controller
@Api(value = "webCrawler")
public class WebCrawlerController {

	@Autowired
    private WebCrawlerService webCrawlerService;
	
	/**
	 * Fetch top javaScript libraries in the web pages on google
	 * @param searchKey
	 * @param numberOfThreads
	 * @return list of javaScript libraries
	 */
	
	@GetMapping(value = "/crawler")
	@ApiOperation(value = "Get the top java script libraries", response = CrawlerResponse.class)
    public ResponseEntity<CrawlerResponse> getTopJSLibraries(@RequestParam final String searchKey, 
    		@RequestParam final int numberOfThreads) {
        return new ResponseEntity<>(webCrawlerService.getTopJSLibraries(searchKey, numberOfThreads), HttpStatus.OK);
    }
}
