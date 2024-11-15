import javax.json.JsonObject;
import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    String baseUrl = "https://setonhall.instructure.com/";
    String CanvasApiToken = System.getenv("CANVAS_API_KEY");
    String courseID = "31315";
    String openAIApiKey = System.getenv("OPENAI_API_KEY");

// Initialize Canvas and OpenAI API clients
    CanvasAPI canvasAPI = new CanvasAPI(baseUrl, CanvasApiToken);
    OpenAIAPI openAIAPI = new OpenAIAPI(openAIApiKey);

    try {
      // Retrieve the most recent file and discussion from Canvas
      JsonObject mostRecentFile = canvasAPI.getMostRecentFile(courseID);
      JsonObject mostRecentDiscussion = canvasAPI.getMostRecentDiscussion(courseID);

      if (mostRecentFile != null) {
        System.out.println("Most Recent Document:");
        System.out.println("Title: " + mostRecentFile.getString("display_name"));
        System.out.println("URL: " + mostRecentFile.getString("url"));
      } else {
        System.out.println("No recent documents found.");
      }

      if (mostRecentDiscussion != null) {
        System.out.println("Most Recent Discussion:");
        System.out.println("Discussion ID" + mostRecentDiscussion.getString("id"));
        System.out.println("Title: " + mostRecentDiscussion.getString("title"));
        System.out.println("Description: " + mostRecentDiscussion.getString("description"));
        System.out.println("URL: " + mostRecentDiscussion.getString("url"));
      } else {
        System.out.println("No discussions found.");
      }

      // Proceed only if both file and discussion are found
      if (mostRecentFile != null && mostRecentDiscussion != null) {
        // Extract text from the file (PDF)
        String fileText = canvasAPI.extractTextFromPDF(mostRecentFile.getString("url"));

        // Retrieve the discussion description for the prompt
        String discussionMessage = mostRecentDiscussion.getString("description");

        // Combine the file text and discussion message
        String prompt = "Discussion Topic: " + discussionMessage + "\n\nFile Content: " + fileText;

        // Send prompt to OpenAI and get the response
        String aiResponse = openAIAPI.generateAIResponse(prompt);
        System.out.println("AI Response: " + aiResponse);

        canvasAPI.postDiscussionReply(courseID, mostRecentDiscussion.getString("id"), aiResponse);

      } else {
        System.out.println("Unable to generate AI response due to missing file or discussion.");
      }
    } catch (IOException | InterruptedException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }
}
