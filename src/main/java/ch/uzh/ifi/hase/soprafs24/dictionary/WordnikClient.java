package ch.uzh.ifi.hase.soprafs24.dictionary;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service("dictionary")
public class WordnikClient implements Dictionary{

    private static final String BASE_URL = "https://api.wordnik.com/v4/word.json/";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();

    public HttpResponse<String> getScrabbleScore(String word) {
        URI uri = URI.create(BASE_URL + word.toLowerCase() + "/scrabbleScore?api_key=" + "5bnrdw90wu3xdr5kjx69iq53f4m2gi2ua1inzs9yol07vfgc8");
        return sendGetRequest(uri);
    }

    public HttpResponse<String> getWordDefinition(String word) {
        URI uri = URI.create(BASE_URL + word.toLowerCase() + "/definitions?limit=1&sourceDictionaries=wiktionary&api_key=" + "5bnrdw90wu3xdr5kjx69iq53f4m2gi2ua1inzs9yol07vfgc8");
        return sendGetRequest(uri);
    }

    private HttpResponse<String> sendGetRequest(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        try {
            return client.send(request, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Get request to the dictionary API failed");
        }
    }
}
