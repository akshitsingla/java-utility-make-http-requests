package com.assl.samples.server.requests.tests;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.assl.samples.server.requests.WebApiInvoker;
import com.assl.samples.server.requests.entities.HttpMethod;
import com.assl.samples.server.requests.entities.PayloadType;
import com.assl.samples.server.requests.exceptions.ConnectionOpenException;
import com.assl.samples.server.requests.exceptions.HttpException;
import com.assl.samples.server.requests.exceptions.InvalidResponseCodeException;
import com.assl.samples.server.requests.exceptions.InvalidResponseMsgException;
import com.assl.samples.server.requests.exceptions.InvalidTLSVersionException;
import com.assl.samples.server.requests.exceptions.MissingUrlException;
import com.assl.samples.server.requests.exceptions.PayloadException;
import com.assl.samples.server.requests.exceptions.ResponseException;
import com.assl.samples.server.requests.exceptions.SSLCertificatesKeException;

public class TestHttpRequests {

  private String GETUrl = "xxx";

  private String POSTUrl = "xxx";

  @Test
  public void testGETRequests() {
    System.out.println("Loading...");

    // GET REQUEST
    WebApiInvoker webApiInvokerGET = makeWebApiInvoker(GETUrl, HttpMethod.GET);

    // GET REQUEST 1
    String response1 = makeRequest(webApiInvokerGET, null, null, null);
    System.out.println(response1);

    // GET REQUEST 2 - Contains Payload => Automatically converts into POST
    // String response2 = makeRequest(webApiInvokerGET, null, PayloadType.XML, "<hello>World!</hello>");
    // System.out.println(response2);

    Assert.assertTrue(true);
  }

  @Test
  public void testPOSTRequests() {
    System.out.println("Loading...");

    // POST REQUEST
    WebApiInvoker webApiInvokerPOST = makeWebApiInvoker(POSTUrl, HttpMethod.POST);

    // POST REQUEST 1 - JSON Payload
    String response1 = makeRequest(webApiInvokerPOST, null, PayloadType.JSON, "{\"Hello\" : \"World!\"}");
    System.out.println(response1);

    // POST REQUEST 2 - XML Payload
    String response2 = makeRequest(webApiInvokerPOST, null, PayloadType.XML, "<hello>World!</hello>");
    System.out.println(response2);

    // POST REQUEST 3 - HTML Payload
    String response3 = makeRequest(webApiInvokerPOST, null, PayloadType.HTML,
        "<html><head><head><body>Hello world!</body></html>");
    System.out.println(response3);

    // POST REQUEST 4 - EMPTY Payload
    String response4 = makeRequest(webApiInvokerPOST, null, null, "hi");
    System.out.println(response4);

    // POST REQUEST 5 - LONG Payload
    String response5 = makeRequest(
        webApiInvokerPOST,
        null,
        PayloadType.JSON,
        "{\"8d240d99fbfa8b32c7908520527769291efd3672\":\"Description about the startup will be depicted here\",\"78e3f9e5a49390dce4bc4a0c7bd5c61d59782f7a\":\"Startup\",\"67ed61547954ca338afaa172565875dd4abbd270\":\"2014\",\"name\":\"Sample Submission\",\"address_country\":\"Thailand\",\"f0faab900bc0b40b067d05d7b2a3a669b84b023c\":\"www.next.amadeus.com\"}");
    System.out.println(response5);

    Assert.assertTrue(true);
  }

  private WebApiInvoker makeWebApiInvoker(String url, HttpMethod httpMethod) {
    WebApiInvoker webApiInvoker = null;
    try {
      webApiInvoker = new WebApiInvoker(url, httpMethod);
    }
    catch (MissingUrlException e) {
      System.out.println("Invalid Request");
      Assert.fail();
    }
    if (webApiInvoker == null) {
      System.out.println("Unable to create WebApiInvoker for : " + httpMethod.name());
      Assert.fail();
    }
    return webApiInvoker;
  }

  private String makeRequest(WebApiInvoker webApiInvoker, Map<String, String> requestHeaders, PayloadType payloadType,
      String payload) {

    String response = null;
    try {
      response = webApiInvoker.execute(requestHeaders, payloadType, payload);
    }
    catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (ProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (HttpException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (ConnectionOpenException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (PayloadException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (ResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (InvalidResponseCodeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (InvalidResponseMsgException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
    catch (InvalidTLSVersionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (SSLCertificatesKeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return response;
  }

}
