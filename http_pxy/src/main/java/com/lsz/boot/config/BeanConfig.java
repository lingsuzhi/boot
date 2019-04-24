package com.lsz.boot.config;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BeanConfig {


	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate restTemplate = builder.build();
		ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
		if (factory != null && factory instanceof HttpComponentsClientHttpRequestFactory) {
			HttpComponentsClientHttpRequestFactory rf = (HttpComponentsClientHttpRequestFactory) factory;
			rf.setConnectTimeout(1000);
		}
		restTemplate.getMessageConverters().add(new TextMappingJackson2HttpMessageConverter());
		return restTemplate;
	}

	private class TextMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

		public TextMappingJackson2HttpMessageConverter() {
			List<MediaType> mediaTypes = new ArrayList<>();
			mediaTypes.add(MediaType.TEXT_PLAIN);
			setSupportedMediaTypes(mediaTypes);
		}
	}

}
