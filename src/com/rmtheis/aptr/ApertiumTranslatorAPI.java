/*
 * apertium-translator-java-api
 * 
 * Copyright 2011 Jonathan Griggs <jonathan.griggs at gmail.com>.
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rmtheis.aptr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Makes the generic Apertium API calls. Different service classes can then
 * extend this to make the specific service calls.
 */
public abstract class ApertiumTranslatorAPI {
  //Encoding type
  protected static final String ENCODING = "UTF-8";

  protected static String apiKey;
  private static String referrer;

  protected static final String PARAM_API_KEY = "key=",
                                PARAM_LANG_PAIR = "&langpair=",
                                PARAM_TEXT = "&q=";

  /**
   * Sets the API key.
   * @param pKey The API key.
   */
  public static void setKey(final String pKey) {
    apiKey = pKey;
  }

  /**
   * Sets the HTTP referrer field.
   * @param pKey The referrer.
   */
  public static void setHttpReferrer(final String pReferrer) {
    referrer = pReferrer;
  }

  /**
   * Forms an HTTP request, sends it using GET method and returns the result of the request as a String.
   * 
   * @param url The URL to query for a String response.
   * @return The translated String.
   * @throws Exception on error.
   */
  private static String retrieveResponse(final URL url) throws Exception {
    final HttpURLConnection uc = (HttpURLConnection) url.openConnection();
    if(referrer!=null)
      uc.setRequestProperty("referer", referrer);
    uc.setRequestProperty("Content-Type","text/plain; charset=" + ENCODING);
    uc.setRequestProperty("Accept-Charset",ENCODING);
    uc.setRequestMethod("GET");

    try {
      final int responseCode = uc.getResponseCode();
      final String result = inputStreamToString(uc.getInputStream());
      if(responseCode!=200) {
        throw new Exception("Error from Apertium API: " + result);
      }
      return result;
    } finally { 
      if(uc!=null) {
        uc.disconnect();
      }
    }
  }

  /**
   * Fetches the JSON response, parses the JSON Response an an Array of JSONObjects,
   * parses the specified JSON Property as a JSON Object, and returns the String result
   * of the value for the given JSON Property in the nested object.
   * 
   * @param url The URL to query for a String response.
   * @param jsonProperty The JSON Property (key) indicating the object we want to parse.
   * @param jsonSubObjProperty The JSON Property, in the nested object, that we want the value of.
   * @return The translated String.
   * @throws Exception on error.
   */
  protected static String retrieveSubObjString(final URL url, final String jsonProperty, final String jsonSubObjProperty) throws Exception {
    try {
      final String response = retrieveResponse(url);
      return jsonSubObjToString(response, jsonProperty, jsonSubObjProperty);
    } catch (Exception ex) {
      throw new Exception("[apertium-translator-api] Error retrieving translation.", ex);
    }    
  }

  /**
   * Fetches the JSON response, parses the JSON Response an an Array of JSONObjects,
   * parses the specified JSON Property as a JSON Array, and returns all the String 
   * results for the value for the given JSON Property in the nested objects.
   * 
   * @param url The URL to query for a String response.
   * @param jsonProperty The JSON Property (key) indicating the object we want to parse.
   * @param jsonSubObjProperty The JSON Property, in the nested object, that we want the value of.
   * @return The translated String.
   * @throws Exception on error.
   */
  protected static String[] retrieveSubObjStringArr(final URL url, final String jsonProperty, final String jsonSubObjProperty) throws Exception {
    try {
      final String response = retrieveResponse(url);
      return jsonArrSubObjToString(response, jsonProperty, jsonSubObjProperty);
    } catch (Exception ex) {
      throw new Exception("[apertium-translator-api] Error retrieving translation.", ex);
    }    
  }

  // Helper method to parse a JSONObject with nested JSONObjects.
  // Retrieves the object with the given propertyName, then the value for the given propertyName within that object.
  private static String jsonSubObjToString(final String inputString, final String propertyName, final String subObjPropertyName) throws Exception {
    JSONObject jsonObj = (JSONObject)JSONValue.parse(inputString);   
    JSONObject dataObj = (JSONObject)JSONValue.parse(jsonObj.get(propertyName).toString());
    return dataObj.get(subObjPropertyName).toString();
  }

  // Helper method to parse a JSON object continaing a JSON array as the value for the given property 
  private static String[] jsonArrSubObjToString(final String inputString, final String propertyName, final String subObjPropertyName) throws Exception {
    JSONObject jsonObj = (JSONObject)JSONValue.parse(inputString);   
    JSONArray jsonArr = (JSONArray)JSONValue.parse(jsonObj.get(propertyName).toString());
    String[] translations = new String[jsonArr.size()];
    Iterator<?> i = jsonArr.iterator();
    int c = 0;
    while (i.hasNext()) {
      String t = jsonSubObjToString(i.next().toString(), propertyName, subObjPropertyName);
      translations[c] = t;
      c++;
    }
    return translations;
  }
  
  /**
   * Reads an InputStream and returns its contents as a String.
   * Also effects rate control.
   * @param inputStream The InputStream to read from.
   * @return The contents of the InputStream as a String.
   * @throws Exception on error.
   */
  private static String inputStreamToString(final InputStream inputStream) throws Exception {
    final StringBuilder outputBuilder = new StringBuilder();

    try {
      String string;
      if (inputStream != null) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
        while (null != (string = reader.readLine())) {
          // Need to strip the Unicode Zero-width Non-breaking Space. For some reason, the Microsoft AJAX
          // services prepend this to every response
          outputBuilder.append(string.replaceAll("\uFEFF", "")); // TODO Is it safe to remove this?
        }
      }
    } catch (Exception ex) {
      throw new Exception("[apertium-translator-api] Error reading translation stream.", ex);
    }
    return outputBuilder.toString();
  }

  //Check if ready to make request, if not, throw a RuntimeException
  protected static void validateServiceState() throws Exception {
    if(apiKey==null||apiKey.length()<27) {
      throw new RuntimeException("INVALID_API_KEY - Please set the API Key with your Apertium API Key");
    }
  }

  protected static String buildStringdArrayParam(Object[] values) {
    StringBuilder targetString = new StringBuilder("[\""); 
    String value;
    for(Object obj : values) {
      if(obj!=null) {
        value = obj.toString();
        if(value.length()!=0) {
          if(targetString.length()>2)
            targetString.append(",\"");
          targetString.append(value);
          targetString.append("\"");
        }
      }
    }
    targetString.append("]");
    return targetString.toString();
  }
}
