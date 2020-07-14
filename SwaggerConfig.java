package com.bezalel.trading_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import static springfox.documentation.builders.PathSelectors.regex;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableSwagger2
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {
    
    @Autowired
    BuildProperties buildProperties;
    
    @Bean
    public WebMvcConfigurer configurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/*").allowedOrigins("*");		    
            }
        };
    }
    
    @Component 
    public class FinTech_Filter implements Filter {
        
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }
        
        @Override
        public void doFilter(
                ServletRequest request, 
                ServletResponse res,
                FilterChain chain) throws IOException, ServletException {
            
            HttpServletRequest req = (HttpServletRequest) request;
            System.out.println("Inside ABCFilter: "+ req.getRequestURI());
            System.out.println("Inside Filter, Header names: "+ req.getHeaderNames());
            System.out.println("Content-Type value:" + req.getHeader("Content-Type"));
                
            // Debug: see request headers
            Enumeration<String> headerNames = req.getHeaderNames();	        
            while (headerNames.hasMoreElements()) {	 
                String headerName = headerNames.nextElement();	 
                Enumeration<String> headers = req.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String headerValue = headers.nextElement();
                    System.out.println(headerName + ": " + headerValue);
                }                  
            }
            
            HttpServletResponse response = (HttpServletResponse)res;
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader(
                    "Access-Control-Allow-Headers", 
                    "Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
            chain.doFilter(request, response);
        }
        
        @Override
        public void destroy() {
        }
    }

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.bezalel.trading_api"))              
                .paths(regex("/v1.*"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        
         return new ApiInfoBuilder()
                .title("Trading API") 
                .description("FinTech Gateway")
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licesen.html")
                .version(buildProperties.getVersion())
                .build();                
    }