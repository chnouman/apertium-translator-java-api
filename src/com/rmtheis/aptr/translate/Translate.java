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
package com.rmtheis.aptr.translate;

import com.rmtheis.aptr.language.Language;
import com.rmtheis.aptr.ApertiumTranslatorAPI;
import com.rmtheis.aptr.ApiKeys;

import java.net.URL;
import java.net.URLEncoder;

/**
 * Makes calls to the Apertium machine translation web service API
 */
public final class Translate extends ApertiumTranslatorAPI {

  private static final String SERVICE_URL = "http://api.apertium.org/json/translate?";

  private static final String RESPONSE_LABEL = "responseData";
  private static final String TRANSLATION_LABEL = "translatedText";

  //prevent instantiation
  private Translate(){};

  /**
   * Translates text from a given Language to another given Language using Apertium.
   * 
   * @param text The String to translate.
   * @param from The language code to translate from.
   * @param to The language code to translate to.
   * @return The translated String.
   * @throws Exception on error.
   */
  public static String execute(final String text, final Language from, final Language to) throws Exception {
    //Run the basic service validations first
    validateServiceState(text); 
    final String params = 
        (apiKey != null ? PARAM_API_KEY + URLEncoder.encode(apiKey,ENCODING) : "") 
        + PARAM_LANG_PAIR + URLEncoder.encode(from.toString(),ENCODING) + URLEncoder.encode("|",ENCODING) + URLEncoder.encode(to.toString(),ENCODING) 
        + PARAM_TEXT + URLEncoder.encode(text,ENCODING);
    final URL url = new URL(SERVICE_URL + params);
    final String response = retrieveSubObjString(url, RESPONSE_LABEL, TRANSLATION_LABEL);    
    return response.trim();
  }

  /**
   * Translates an array of texts from a given Language to another given Language using Apertium.
   * 
   * Source/target language arrays must be the same size as the text array. 
   * 
   * @param texts The Strings Array to translate.
   * @param from The language code to translate from.
   * @param to The language code to translate to.
   * @return The translated Strings Array[].
   * @throws Exception on error.
   */
  public static String[] execute(final String[] texts, final Language[] from, final Language[] to) throws Exception {
    //Run the basic service validations first
    validateServiceState(texts);

    if ((texts.length != from.length) || (texts.length != to.length)) {
      throw new IllegalArgumentException("Source/target language array sizes do not match text array size.");
    }

    String params = apiKey != null ? PARAM_API_KEY + URLEncoder.encode(apiKey,ENCODING) : "";
    for (int i = 0; i < texts.length; i++) {
      params += PARAM_TEXT + URLEncoder.encode(texts[i],ENCODING)
          + PARAM_LANG_PAIR + URLEncoder.encode(from[i].toString(),ENCODING) + URLEncoder.encode("|",ENCODING) + URLEncoder.encode(to[i].toString(),ENCODING);
    }
    final URL url = new URL(SERVICE_URL + params);
    final String[] response = retrieveSubObjStringArr(url, RESPONSE_LABEL, TRANSLATION_LABEL);
    return response;
  }  

  private static void validateServiceState(final String[] texts) throws Exception {
    int length = 0;
    for(String text : texts) {
      length+=text.getBytes(ENCODING).length;
    }
    if(length>10240) {
      throw new RuntimeException("TEXT_TOO_LARGE");
    }
    validateServiceState();
  }

  private static void validateServiceState(final String text) throws Exception {
    final int byteLength = text.getBytes(ENCODING).length;
    if(byteLength>10240) { // TODO What is the maximum text length allowable for Apertium?
      throw new RuntimeException("TEXT_TOO_LARGE");
    }
    validateServiceState();
  }

  public static void main(String[] args) {
    // For testing, request a single translation
    try {
      Translate.setKey(ApiKeys.APERTIUM_API_KEY);
      String translation = Translate.execute("The quick brown fox jumps over the lazy dog.", Language.ENGLISH, Language.SPANISH);
      System.out.println("Translation: " + translation);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // For testing, request an array of translations
    try {
      Translate.setKey(ApiKeys.APERTIUM_API_KEY);      
      String[] translations = Translate.execute(new String[] {"Hello", "The quick brown fox jumps over the lazy dog."}, 
          new Language[] { /* Source languages */ Language.ENGLISH, Language.ENGLISH }, 
          new Language[] { /* Target languages */ Language.SPANISH, Language.CATALAN } );
      for (String translation : translations) {
        System.out.println("Translation: " + translation);
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
