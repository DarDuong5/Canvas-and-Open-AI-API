import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CanvasAPI {
  private final String baseUrl;
  private final String apiToken;
  private final HttpClient client;

  public CanvasAPI(String baseUrl, String apiToken) {
    this.baseUrl = baseUrl;
    this.apiToken = apiToken;
    this.client = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.ALWAYS) // Allow automatic redirects
      .build();
  }

  private HttpRequest request(String endpoint) {
    return HttpRequest.newBuilder(URI.create(this.baseUrl + endpoint))
      .header("Authorization", "Bearer " + this.apiToken)
      .GET()
      .build();
  }

  public JsonObject getMostRecentFile(String courseID) throws IOException, InterruptedException {
    String endpoint = "/api/v1/courses/" + courseID + "/files?sort=created_at&order=desc";
    HttpRequest request = this.request(endpoint);

    HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

    try (JsonReader jsonReader = Json.createReader(new StringReader(response.body()))) {
      JsonArray files = jsonReader.readArray();

      if (!files.isEmpty()) {
        return files.getJsonObject(0);
      } else {
        return null;
      }
    }
  }

  public JsonObject getMostRecentDiscussion(String courseID) throws IOException, InterruptedException {
    String endpoint = "/api/v1/courses/" + courseID + "/discussion_topics?sort=created_at&order=desc";
    HttpRequest request = this.request(endpoint);

    HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

    try (JsonReader jsonReader = Json.createReader(new StringReader(response.body()))) {
      JsonArray discussions = jsonReader.readArray();

      if (!discussions.isEmpty()) {
        JsonObject discussion = discussions.getJsonObject(0);
        String title = discussion.getString("title");
        String url = discussion.getString("url");

        String description = discussion.containsKey("message")
          ? discussion.getString("message")
          : "No description available";

        description = description.replaceAll("<[^>]*>", "");  // Remove HTML tags

        return Json.createObjectBuilder()
          .add("title", title)
          .add("url", url)
          .add("description", description)
          .build();
      } else {
        return null;
      }
    }
  }

  public String extractTextFromPDF(String fileUrl) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(URI.create(fileUrl))
      .header("Authorization", "Bearer " + this.apiToken)
      .GET()
      .build();

    HttpResponse<byte[]> response = this.client.send(request, HttpResponse.BodyHandlers.ofByteArray());

    String contentType = response.headers().firstValue("Content-Type").orElse("");

    if (!contentType.equals("application/pdf")) {
      String htmlResponse = new String(response.body());
      System.out.println("HTML Response: " + htmlResponse);  // Debugging: print HTML response
      throw new IOException("Unexpected content type: " + contentType);
    }

    try (PDDocument document = PDDocument.load(response.body())) {
      PDFTextStripper stripper = new PDFTextStripper();
      return stripper.getText(document);
    } catch (IOException e) {
      throw new IOException("Error extracting text from PDF: " + e.getMessage());
    }
  }
}
