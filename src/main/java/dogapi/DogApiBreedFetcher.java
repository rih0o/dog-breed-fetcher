package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        if (breed == null || breed.isEmpty()) {
            throw new BreedNotFoundException("null-or-empty");
        }

        String normalized = breed.toLowerCase().trim();
        String url = "https://dog.ceo/api/breed/" + normalized + "/list";

        Request req = new Request.Builder().url(url).get().build();

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful() || res.body() == null) {
                throw new BreedNotFoundException(normalized);
            }

            String body = res.body().string();
            JSONObject json = new JSONObject(body);

            if (!"success".equalsIgnoreCase(json.optString("status", "error"))) {
                throw new BreedNotFoundException(normalized);
            }

            JSONArray message = json.optJSONArray("message");
            List<String> subs = new ArrayList<>();
            if (message != null) {
                for (int i = 0; i < message.length(); i++) {
                    subs.add(message.getString(i));
                }
            }
            return subs;
        } catch (IOException e) {
            throw new BreedNotFoundException(normalized);
        }
    }
}