package com.ps.DemoService;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import javax.swing.KeyStroke;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.ODataHttpHeaders;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.part.BatchProcessor;
import org.apache.tomcat.jni.File;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.Property;
import org.json.XML;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ps.DemoService.StringHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.google.common.io.Resources;

@CrossOrigin
@RestController
@RequestMapping("api")
public class DemoServiceController {

	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_PATCH = "PATCH";
	private static final String HTTP_METHOD_DELETE = "DELETE";

	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_HEADER_ACCEPT = "Accept";

	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_ATOM_XML = "application/atom+xml";
	public static final String METADATA = "$metadata";
	public static final String SEPARATOR = "/";
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
	public static final String CSRF_TOKEN_FETCH = "Fetch";
	public static final String C4C_TENANT = "C4C_TENANT";

	private static String boundary = "batch_36522ad7-fc75-4b56-8c71-56071383e77b";

	private HttpClient m_httpClient = null;
	private Edm m_edm = null;

	@GetMapping(path = "/Categories", produces = "application/json")
	public String getAll() throws IOException {

		String serviceUrl = "https://services.odata.org/Northwind/Northwind.svc/Categories";
		final HttpGet get = new HttpGet(serviceUrl);
		get.setHeader("Accept", "application/json");
		HttpResponse response = getHttpClient().execute(get);
		HttpEntity entity = response.getEntity();
		String content = EntityUtils.toString(entity);
		JSONObject json = new JSONObject(content);
		JSONArray values = json.getJSONArray("value");

		return values.toString();
	}



	@PostMapping(path = "/Categories", produces = "application/json", consumes = "application/json")
	private String getCustom(@RequestBody String str) throws IOException, ClientProtocolException {

		JSONObject payload = new JSONObject(str);
		JSONArray input = payload.getJSONArray("queries");

		List<JSONObject> result = new ArrayList<>();
		String serviceUrl;
		int id;

		for (int i = 0; i < input.length(); i++) {
			JSONObject currentQuery = input.getJSONObject(i);

			serviceUrl = "https://services.odata.org/Northwind/Northwind.svc/Categories";

			id = 0;
			for (String keyStr : currentQuery.keySet()) {
				String keyvalue = currentQuery.get(keyStr).toString();

				if (id == 0) {
					if (keyStr.contentEquals("CategoryID")) {
						String s = "?$filter=".concat(keyStr).concat("%20eq%20").concat(keyvalue);
						System.out.println(s);
						serviceUrl = serviceUrl.concat(s);
					} else {
						String s = "?$filter=".concat(keyStr).concat("%20eq%20").concat("'").concat(keyvalue)
								.concat("'");
						System.out.println(s);
						serviceUrl = serviceUrl.concat(s);
					}
				}

				else {
					if (keyStr.contentEquals("CategoryID")) {
						String s = "%20and%20".concat(keyStr).concat("%20eq%20").concat(keyvalue);
						System.out.println(s);
						serviceUrl = serviceUrl.concat(s);
					} else {
						String s = "%20and%20".concat(keyStr).concat("%20eq%20").concat("'").concat(keyvalue)
								.concat("'");
						System.out.println(s);
						serviceUrl = serviceUrl.concat(s);
					}
				}
				id++;
			}

//			System.out.println(serviceUrl);
			final HttpGet get = new HttpGet(serviceUrl);
			get.setHeader("Accept", "application/json");
			HttpResponse response = null;
			try {
				response = getHttpClient().execute(get);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Res" + response);
			HttpEntity entity = response.getEntity();
			String content = null;
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject json = new JSONObject(content);
			JSONArray values = json.getJSONArray("value");
			for (int j = 0; j < values.length(); j++) {
				JSONObject current = values.getJSONObject(j);
				result.add(current);

			}
		}

		return result.toString();
	}

	private HttpClient getHttpClient() {
		if (this.m_httpClient == null) {
			this.m_httpClient = HttpClientBuilder.create().build();
		}
		return this.m_httpClient;
	}

	private static String batchService() throws IOException,BatchException {

//		JSONObject payloadJson = new JSONObject(str);
//		JSONArray queries = payloadJson.getJSONArray("queries");
		
		List<JSONObject> result = new ArrayList<>();
		List<BatchPart> batchParts = new ArrayList<BatchPart>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		
//		for (int i = 0; i < queries.length(); i++) {
//			JSONObject currentQuery = queries.getJSONObject(i);
//			String uri=findUrl(currentQuery);
//		batchParts.add(BatchQueryPart.uri(uri).headers(headers).method(method))	
//		}
//		
//		
		BatchQueryPart batchquery = BatchQueryPart.uri("Categories").headers(headers).method(HTTP_METHOD_GET).build();
		BatchQueryPart batchquery1 = BatchQueryPart.uri("Customers").headers(headers).method(HTTP_METHOD_GET).build();
		
		batchParts.add(batchquery);
		batchParts.add(batchquery1);
		
		InputStream body = EntityProvider.writeBatchRequest(batchParts, boundary);

		String payload=StringHelper.inputStreamToString(body,true);

		System.out.println(payload);
		
		String serviceUrl = "http://services.odata.org/V3/Northwind/Northwind.svc";
		
		HttpResponse batchResponse = executeBatchCall(serviceUrl, payload);
		InputStream responseBody = batchResponse.getEntity().getContent();
		String contentType = batchResponse.getFirstHeader(
				HttpHeaders.CONTENT_TYPE).getValue();
		String response = IOUtils.toString(responseBody);
		List<BatchSingleResponse> responses = EntityProvider
				.parseBatchResponse(IOUtils.toInputStream(response),
						contentType);
		
		for (BatchSingleResponse rsp : responses) {
			
			String queryResponse=rsp.getBody();
			JSONObject json = new JSONObject(queryResponse);
			JSONArray values = json.getJSONArray("value");
			
			for (int j = 0; j < values.length(); j++) {
				JSONObject current = values.getJSONObject(j);
				result.add(current);
			}

				
		}
		
		System.out.println(result);		
		
		return null;
	}

	private static HttpResponse executeBatchCall(String serviceUrl, final String body)
			throws ClientProtocolException, IOException {
		final HttpPost post = new HttpPost(URI.create(serviceUrl + "/$batch"));
		post.setHeader("Content-Type", "multipart/mixed; boundary=" + boundary);
		post.setHeader(ODataHttpHeaders.DATASERVICEVERSION, ODataServiceVersion.V10);
		post.setHeader("Accept","application/json");

		HttpEntity entity = new StringEntity(body);

		post.setEntity(entity);


		HttpResponse response = HttpClientBuilder.create().build().execute(post);

		return response;
	}

	public static void main(String[] args) throws IOException,BatchException {
		batchService();
	}
}
