package com.sample.crawler.controller;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.sample.crawler.model.CrawlerResponse;
import com.sample.crawler.service.WebCrawlerService;

/**
 * This test class contains test scenarios for WebCrawlerController class.
 * 
 * @author Mahnaz
 * @Mar 01, 2020
 */

@SpringBootTest
@AutoConfigureMockMvc
public class WebCrawlerControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
    private WebCrawlerService crawlerService;
	
	@Test
    public void getTopJSLibraries_whenRequestIsValid_thenSendOkStatusCode() throws Exception {
        Mockito.when(crawlerService.getTopJSLibraries(Mockito.anyString(), Mockito.anyInt()))
        		.thenReturn(new CrawlerResponse());
        
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/crawler?searchKey=java&numberOfThreads=10")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        Assertions.assertTrue(mvcResult.getResponse().getContentAsString().contains("null"));
    }
	
	@Test
    public void getTopJSLibraries_whenRequestIsValidAndHasResult_thenReturnResult() throws Exception {
        CrawlerResponse resposne = new CrawlerResponse();
        Set<String> libs = new HashSet<String>();
        libs.add("apmeum.js");
        libs.add("s_code_remote.js");
        libs.add("subtlePager.js");
        resposne.setTopJSLibraries(libs);
        
        Mockito.when(crawlerService.getTopJSLibraries(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(resposne);
        
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/crawler?searchKey=java&numberOfThreads=10")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        Assertions.assertTrue(mvcResult.getResponse().getContentAsString().contains("apmeum.js"));
    }
}
