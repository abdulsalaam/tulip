package tulip.manageStockExchange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tulip.manageOrder.*;
import tulip.sockets.messages.ContentType;
import tulip.sockets.messages.Message;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestStockExchange implements Serializable {
	ArrayList<Company> companies ; 
	StockExchange testBourse = new  StockExchange();
	
	ObjectMapper mapper = new ObjectMapper();
    ArrayNode arrayNode = mapper.createArrayNode();

	
	public TestStockExchange() {
		companies = new ArrayList<Company>() ; 
		Company company1 = new Company("EDF", 100, 23.45, testBourse) ; 
		Company company2 = new Company("MICROSOFT", 120, 52.56, testBourse) ;
		companies.add(company1) ; 
		companies.add(company2) ;
	}
	
	public static void main(String[] args) {
		
		//test of the stockExchange : opening ONLY when the word "OPEN" is manually entered in the console
		
		 StockExchange testBourse = new  StockExchange();
		 Thread t = new Thread(testBourse);
		 t.start();

		 System.out.println("ON LANCE LE SCANNER ");
		 Scanner s = new Scanner(System.in);
		 while (!s.next().equals("OPEN"));
		 
		 System.out.println("ON A ENTRE LE MOT MAGIQUE - C'EST BON ON FAIT LA SUITE ");
		 testBourse.notStarted = false;
		 testBourse.closed = false ; 
		 t.interrupt();  //end of the thread
		 
		
		
		
		
		/**
		//test of the generation of the JSONArray (for marketState)
		 
		 TestStockExchange test = new TestStockExchange() ; 
		 
		 for (Company company : test.companies) {
        		ObjectNode objectNode = test.mapper.createObjectNode();
        		objectNode.put("nameCompany", company.name); 
        		objectNode.put("price", "100");
        		
        		test.arrayNode.add(objectNode);
        }
        
        
        try {
        		System.out.println(test.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(test.arrayNode));
			System.out.println(test.mapper.writeValueAsString(test.arrayNode));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		*/
 
	}

	
}
