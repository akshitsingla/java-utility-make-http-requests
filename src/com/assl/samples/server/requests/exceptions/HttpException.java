package com.assl.samples.server.requests.exceptions;

public class HttpException extends Exception {

  private int responseCode;

  private String responseMsg;

  public HttpException(int responseCode, String responseMsg) {
    super();
    this.responseCode = responseCode;
    this.responseMsg = responseMsg;
  }

  public void printMsg() {
    System.out.println(responseCode + " : " + responseMsg);
  }

}
