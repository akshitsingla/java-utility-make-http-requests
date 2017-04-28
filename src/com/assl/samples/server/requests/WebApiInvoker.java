package com.assl.samples.server.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import com.assl.samples.server.requests.entities.HttpMethod;
import com.assl.samples.server.requests.entities.PayloadType;
import com.assl.samples.server.requests.exceptions.ConnectionOpenException;
import com.assl.samples.server.requests.exceptions.HttpException;
import com.assl.samples.server.requests.exceptions.InvalidResponseCodeException;
import com.assl.samples.server.requests.exceptions.InvalidResponseMsgException;
import com.assl.samples.server.requests.exceptions.MissingUrlException;
import com.assl.samples.server.requests.exceptions.PayloadException;
import com.assl.samples.server.requests.exceptions.ResponseException;

public class WebApiInvoker {

  private HttpMethod httpMethod = null;

  private String targetURL = null;

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
      PayloadException, ResponseException, InvalidResponseCodeException, InvalidResponseMsgException {
    return execute(null, null, null);
  }

  public String execute(Map<String, String> requestHeaders, PayloadType payloadType, String payload)
      throws HttpException, MalformedURLException, ConnectionOpenException, ProtocolException, PayloadException,
      ResponseException, InvalidResponseCodeException, InvalidResponseMsgException {

    HttpURLConnection connection = null;
    String response = null;

    try { // To make sure connection is closed at the end

      URL url = makeURL();
      connection = openConnection(connection, url);

      setHttpMethod(connection);

      addRequestHeaders(requestHeaders, connection);

      setConnectionProperties(connection);

      setPayload(payloadType, payload, connection);

      response = receive(connection);
    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return response;
  }

  private URL makeURL() throws MalformedURLException {
    URL url;
    try {
      url = new URL(targetURL);
    }
    catch (MalformedURLException e) {
      throw e;
    }
    return url;
  }

  private HttpURLConnection openConnection(HttpURLConnection connection, URL url) throws ConnectionOpenException {
    try {
      connection = (HttpURLConnection)url.openConnection();
    }
    catch (IOException e) {
      throw new ConnectionOpenException(e);
    }
    return connection;
  }

  private void setHttpMethod(HttpURLConnection connection) throws ProtocolException {
    try {
      connection.setRequestMethod(httpMethod.toString());
    }
    catch (ProtocolException e) {
      throw e;
    }
  }

  private void addRequestHeaders(Map<String, String> requestHeaders, HttpURLConnection connection) {
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

  private void setConnectionProperties(HttpURLConnection connection) {
    connection.setUseCaches(false);
    connection.setDoOutput(true);
  }

  private void setPayload(PayloadType payloadType, String payload, HttpURLConnection connection)
      throws PayloadException {
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

  private String receive(HttpURLConnection connection) throws InvalidResponseCodeException, ResponseException,
      InvalidResponseMsgException, HttpException {
    int responseCode;
    try {
      responseCode = connection.getResponseCode();
    }
    catch (IOException e) {
      throw new InvalidResponseCodeException(e);
    }
    switch (responseCode) {
    case 200: {
      return extractResponse(connection);
    }
    default: {
      extractError(connection, responseCode); // always throws an error
      return null;
    }
    }
  }

  private String extractResponse(HttpURLConnection connection) throws ResponseException {
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

  private void extractError(HttpURLConnection connection, int responseCode) throws InvalidResponseMsgException,
      HttpException {
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
