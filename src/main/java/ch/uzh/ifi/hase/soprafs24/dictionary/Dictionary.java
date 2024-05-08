package ch.uzh.ifi.hase.soprafs24.dictionary;

import java.net.http.HttpResponse;

public interface Dictionary {

    HttpResponse<String> getScrabbleScore(String word);
    HttpResponse<String> getWordDefinition(String word);
}
