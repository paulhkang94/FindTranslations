package com.pkang.findtranslations.model;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/***
 * Asynchronous task that retrieves the crossword challenge JSON. OnPostExecute Returns an InputStream of the JSON data.
 */
public class RetrieveCrosswordTask extends AsyncTask<String, Void, List<CrosswordChallenge>> {

    private Callback _callback;

    public RetrieveCrosswordTask(Callback callback) {
        _callback = callback;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        _callback.onLoadingStarted();

    }

    @Nullable
    protected List<CrosswordChallenge> doInBackground(String... params) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream  = connection.getInputStream();

            return parseCrosswordJSON(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            _callback.onLoadingFailed(e.getLocalizedMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            _callback.onLoadingFailed(e.getLocalizedMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(@Nullable List<CrosswordChallenge> crosswordChallenges) {
        if(crosswordChallenges != null) {
            _callback.onLoadingSucceeded(crosswordChallenges);
        }
    }

    /**
     * public API to parse the crossword JSON into model objects.
     * @param inputStream   Stream of JSON (e.g. from a HTTPURLConnection)
     * @return list of of CrosswordChallenge objects
     * @throws IOException when parsing JSON fails.
     */
    public List<CrosswordChallenge> parseCrosswordJSON(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<CrosswordChallenge> crosswordChallenges = new ArrayList<>();
        JsonParser parser = mapper.getFactory().createParser(inputStream);
        while (parser.nextToken() != null) {
            CrosswordChallenge screen = mapper.readValue(parser, CrosswordChallenge.class);
            crosswordChallenges.add(screen);
        }
        return crosswordChallenges;
    }


    /**
     * Callback for this task. JSON consumers should implement this.
     */
    public interface Callback {
        void onLoadingStarted();
        void onLoadingSucceeded(List<CrosswordChallenge> crosswordChallenges);
        void onLoadingFailed(String errorMessage);
    }
}