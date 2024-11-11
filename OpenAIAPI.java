import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIAPI {
  private final String apiKey;
  private static final int MAX_TOKENS = 100;
  private static final int TOKEN_APPROX_CHAR_LIMIT = MAX_TOKENS * 4; // Approximate character count

  public OpenAIAPI(String apiKey) {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalArgumentException("API key is missing or empty.");
    }
    this.apiKey = apiKey;
  }

  public String generateAIResponse(String message) {
    String url = "https://api.openai.com/v1/chat/completions";
    String model = "gpt-3.5-turbo"; // The GPT model to use.

    // Truncate the message if it exceeds the token limit (approximate by character count)
    if (message.length() > TOKEN_APPROX_CHAR_LIMIT) {
      message = message.substring(0, TOKEN_APPROX_CHAR_LIMIT);
    }

    try {
      // Create the HTTP POST request
      URL obj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Authorization", "Bearer " + this.apiKey);
      con.setRequestProperty("Content-Type", "application/json");

      // Build the request body
      JSONObject jsonBody = new JSONObject();
      jsonBody.put("model", model);
      jsonBody.put("messages", new JSONArray().put(new JSONObject()
        .put("role", "user")
        .put("content", message)
      ));
      String body = jsonBody.toString();

      // Send the request
      con.setDoOutput(true);
      try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream())) {
        writer.write(body);
        writer.flush();
      }

      // Check the response code
      int responseCode = con.getResponseCode();
      if (responseCode != 200) {
        // Read the error stream to get more details about the error
        StringBuilder errorResponse = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            errorResponse.append(inputLine);
          }
        }
        System.out.println("Error Response: " + errorResponse.toString()); // Print error details
        throw new IOException("API request failed with response code: " + responseCode + "\nDetails: " + errorResponse.toString());
      }

      // Read the successful response
      StringBuilder response = new StringBuilder();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
      }

      // Extract and return the response content
      return this.extractContentFromResponse(response.toString());

    } catch (IOException e) {
      throw new RuntimeException("Error with OpenAI API request: " + e.getMessage(), e);
    }
  }

  // This method extracts the content from the OpenAI API response
  private String extractContentFromResponse(String response) {
    JSONObject jsonResponse = new JSONObject(response);
    JSONArray choices = jsonResponse.getJSONArray("choices");
    JSONObject firstChoice = choices.getJSONObject(0);
    JSONObject messageObject = firstChoice.getJSONObject("message");
    return messageObject.getString("content");
  }
}
