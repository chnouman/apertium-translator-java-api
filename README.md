#apertium-translator-java-api
* * *

Provides a Java wrapper around the Apertium machine translation web service API. 

The services for translating a single string and an array of strings have been implemented in this API.

This project was forked from the microsoft-translator-java-api project by Jonathan Griggs.

## Requires

* An Apertium API Key - [Sign Up Here](http://api.apertium.org/register.jsp)

Quickstart
==========

```java
    import com.robtheis.aptr.language.Language;
    import com.robtheis.aptr.translate.Translate;

    public class Main {
      public static void main(String[] args) throws Exception {
        // Set the Apertium API Key - Get yours at http://api.apertium.org/register.jsp
        Translate.setKey(/* Put your Apertium API Key here */);

        String translatedText = Translate.execute("Hola, mundo!", Language.SPANISH, Language.ENGLISH);

        System.out.println(translatedText);
      }
    }
```

License
=======

The apertium-translator-java-api is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

