package com.assl.samples.server.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;

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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WebApiInvoker {

  private HttpMethod httpMethod = null;

  private HttpURLConnection connection = null;

  private String targetURL = null;

  public WebApiInvoker(String _targetURL) throws MissingUrlException {
    this(_targetURL, null);
  }

  public WebApiInvoker(String _targetURL, HttpMethod _httpMethod) throws MissingUrlException {

    // check mandatory parameters
    if (_targetURL == null) {
      throw new MissingUrlException();
    }
    targetURL = _targetURL;

    // set default or overridden value of HttpMethod
    httpMethod = (_httpMethod == null ? HttpMethod.GET : _httpMethod);

  }

  public String execute() throws HttpException, MalformedURLException, ProtocolException, ConnectionOpenException,
      PayloadException, ResponseException, InvalidResponseCodeException, InvalidResponseMsgException,
      InvalidTLSVersionException, SSLCertificatesKeException {
    return execute(null, null, null);
  }

  public String execute(Map<String, String> requestHeaders, PayloadType payloadType, String payload)
      throws HttpException, MalformedURLException, ConnectionOpenException, ProtocolException, PayloadException,
      ResponseException, InvalidResponseCodeException, InvalidResponseMsgException, InvalidTLSVersionException,
      SSLCertificatesKeException {

    String response = null;

    try { // To make sure connection is closed at the end

      URL url = new URL(targetURL);

      openConnection(url);

      if (url.getProtocol().equals("https")) {
        trustAllCertificates((HttpsURLConnection)connection);
      }

      // Set request method
      connection.setRequestMethod(httpMethod.toString());

      addRequestHeaders(requestHeaders);

      setConnectionProperties(false, true);

      setPayload(payloadType, payload);

      response = receive();
    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return response;
  }

  private void trustAllCertificates(HttpsURLConnection connection) throws InvalidTLSVersionException,
      SSLCertificatesKeException {

    // Trust manager that does not validate
    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }

      public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }
    } };

    try {

      // SSLContext for the new TrustManager
      SSLContext sc = SSLContext.getInstance("TLSv1.1");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());

      // Setting the new SSLContext with current connection
      connection.setSSLSocketFactory(sc.getSocketFactory());
    }
    catch (NoSuchAlgorithmException e) {
      throw new InvalidTLSVersionException(e);
    }
    catch (KeyManagementException e) {
      throw new SSLCertificatesKeException(e);
    }
  }

  private void openConnection(URL url) throws ConnectionOpenException {
    try {
      connection = (HttpURLConnection)url.openConnection();
    }
    catch (IOException e) {
      throw new ConnectionOpenException(e);
    }
  }

  private void addRequestHeaders(Map<String, String> requestHeaders) {
    if (requestHeaders != null) {
      Set<String> requestHeaderProperties = requestHeaders.keySet();
      for (String requestHeaderProperty : requestHeaderProperties) {
        if (requestHeaders.get(requestHeaderProperty) != null) {
          connection.setRequestProperty(requestHeaderProperty, requestHeaders.get(requestHeaderProperty));
        }
      }
    }
    connection.setRequestProperty("User-Agent", "*");
  }

  private void setConnectionProperties(boolean useCaches, boolean doOutput) {
    connection.setUseCaches(useCaches);
    connection.setDoOutput(doOutput);
  }

  private void setPayload(PayloadType payloadType, String payload) throws PayloadException {
    if (payloadType != null) {
      try {
        connection.setRequestProperty("Content-Type", payloadType.getContentTypeHeaderValue());

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(payload);
        wr.close();
      }
      catch (IOException e) {
        throw new PayloadException(e);
      }
    }
  }

  private String receive() throws InvalidResponseCodeException, ResponseException, InvalidResponseMsgException,
      HttpException {
    int responseCode;
    try {
      responseCode = connection.getResponseCode();
    }
    catch (IOException e) {
      throw new InvalidResponseCodeException(e);
    }
    if (responseCode >= 200 && responseCode < 300) {
      return extractResponse();
    }
    else {
      extractError(responseCode); // always throws an error
      return null;
    }
  }

  private String extractResponse() throws ResponseException {
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuffer response = new StringBuffer();
      String line;

      while ((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }

      rd.close();
      return response.toString();
    }
    catch (IOException e) {
      throw new ResponseException(e);
    }
  }

  private void extractError(int responseCode) throws InvalidResponseMsgException, HttpException {
    String responseMsg;
    try {
      responseMsg = connection.getResponseMessage();
    }
    catch (IOException e) {
      throw new InvalidResponseMsgException(e);
    }
    throw new HttpException(responseCode, responseMsg);
  }

}
