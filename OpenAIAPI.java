import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIAPI {

  private final HttpClient client;
  private final String apiKey;

  public OpenAIAPI(String apiKey) {
    this.client = HttpClient.newHttpClient();
    this.apiKey = apiKey;
  }

  // Method to generate AI response
  public String generateAIResponse(String prompt) throws IOException, InterruptedException {
    String endpoint = "https://api.openai.com/v1/completions"; // OpenAI API endpoint
    String body = "{\"model\": \"text-davinci-003\", \"prompt\": \"" + prompt + "\", \"max_tokens\": 100}";

    HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer " + this.apiKey)
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
      // Parse the response to get the AI's answer
      return response.body();
    } else {
      throw new IOException("Error calling OpenAI API: " + response.statusCode());
    }
  }
}
