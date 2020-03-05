package com.sample.crawler.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class contains response of top javaScript libraries service
 *  
 * @author Mahnaz
 * @Mar 01, 2020
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerResponse {

	@JsonProperty("topJSLibraries")
	private Set<String> topJSLibraries;
}
