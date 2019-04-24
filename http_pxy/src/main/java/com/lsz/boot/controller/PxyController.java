package com.lsz.boot.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Enumeration;

@RestController
public class PxyController {

    private static final String CHARSET = "utf-8";

    private static final String BASE_PATH = "/";

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/pxy/{apiUrl}/**")
    public Object invoke(@PathVariable String apiUrl, HttpServletRequest request, HttpServletResponse response) throws Exception {
        apiUrl = new String(Base64.getDecoder().decode(apiUrl));
        String method = request.getMethod();
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.add(header, request.getHeader(header));
        }

        Object body = null;
        try (InputStream inputStream = request.getInputStream()) {
            String txt = StreamUtils.copyToString(inputStream, Charset.forName(CHARSET));
            if (StringUtils.isNotBlank(txt))
                body = txt;
        }

        if (body == null) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            Enumeration<String> formNames = request.getParameterNames();
            while (formNames.hasMoreElements()) {
                String name = formNames.nextElement();
                if (StringUtils.isNotBlank(request.getQueryString()) && request.getQueryString().contains(name + "="))
                    continue;
                map.add(name, request.getParameter(name));
            }
            if (map.size() > 0)
                body = map;
        }

        String requestURI = request.getRequestURI();
        int indexOf = requestURI.indexOf(BASE_PATH, 6);
        if (indexOf == -1){
            return null;
        }
        String url = apiUrl + requestURI.substring(indexOf);
        if (StringUtils.isNotBlank(request.getQueryString()))
            url += request.getQueryString();

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> result = restTemplate.exchange(url,
                    "GET".equals(method) ? HttpMethod.GET : HttpMethod.POST, entity, String.class);
            response.setCharacterEncoding(CHARSET);
            String responseBody = result.getBody();
            return responseBody;
        } catch (HttpClientErrorException e) {
            response.setStatus(e.getStatusCode().value());
            return e.getResponseBodyAsString();
        }
    }
}
