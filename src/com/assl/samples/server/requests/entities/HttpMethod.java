package com.assl.samples.server.requests.entities;

public enum HttpMethod {

  GET, POST, DELETE, PUT;

  @Override
  public String toString() {
    return this.name();
  }
}
