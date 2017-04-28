package com.assl.samples.server.requests.entities;

public enum PayloadType {

  JSON("application/json"), XML("application/xml"), HTML("text/html");

  private String contentTypeHeaderValue = null;

  private PayloadType(String _value) {
    contentTypeHeaderValue = _value;
  }

  public String getContentTypeHeaderValue() {
    return contentTypeHeaderValue;
  }
}
