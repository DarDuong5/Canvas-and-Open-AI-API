import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenAIAPI {
  private final String apiKey;

  public OpenAIAPI(String apiKey) {
    this.apiKey = apiKey;
  }

  public String generateAIResponse(String message) {
    String url = "https://api.openai.com/v1/chat/completions";
    String model = "gpt-3.5-turbo"; // The GPT model to use.

    try {

      // Create the HTTP POST request
      URL obj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Authorization", "Bearer " + this.apiKey);
      con.setRequestProperty("Content-Type", "application/json");

      // Build the request body
      String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
      con.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
      writer.write(body);
      writer.flush();
      writer.close();

      // Get the response
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      // Extract and return the response content
      return this.extractContentFromResponse(response.toString());

    } catch (IOException e) {
      throw new RuntimeException("Error with OpenAI API request: " + e.getMessage());
    }
  }

  // This method extracts the response from the OpenAI API
  private String extractContentFromResponse(String response) {
    int startMarker = response.indexOf("content") + 11; // Locate the start of content
    int endMarker = response.indexOf("\"", startMarker); // Locate the end of content
    return response.substring(startMarker, endMarker); // Return the response content
  }
}
