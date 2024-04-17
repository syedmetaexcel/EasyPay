package Practise.Automation;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.RestAssured;
import org.json.JSONObject;

public class ExampleAPITest {
    public String getToken() {
        // Define base URI
        String baseURI = "https://auth-api-staging.easypayfinance.com";

        // Perform POST request and validate response
        Response response = RestAssured.given()
            .baseUri(baseURI)
            .contentType(ContentType.JSON)
            .body("{\r\n" +
                    "  \"email\": \"jose.corral@easypayfinance.com\",\r\n" +
                    "  \"password\": \"EasyPay123!\"\r\n" +
                    "}")
            .post("/api/Authentication/authenticate/email")
            .then()
            .statusCode(200)
            .extract().response(); // Extracting the response

        // Convert response body to JSON object
        JSONObject jsonResponse = new JSONObject(response.getBody().asString());
        
        // Extract access token
        String accessToken = jsonResponse.getString("accessToken");
        System.out.println(accessToken);
        return accessToken;
    }
    
  @Test
    public void testGetAccountDetails() {
        // Define base URI for account details API
        String accountBaseURI = "https://my-api-staging.easypayfinance.com";

        // Fetch access token from the getToken() method
        String accessToken = getToken();

        // Perform GET request to fetch account details
        Response accountResponse = RestAssured.given()
            .baseUri(accountBaseURI)
            .header("Authorization", "Bearer " + accessToken) // Include access token in the request header
            .get("/api/Accounts/balance/4061271")
            .then()
            .statusCode(200)
            .extract().response(); // Extracting the response

        // Convert account details response body to JSON object
        JSONObject accountJsonResponse = new JSONObject(accountResponse.getBody().asString());
        
        // Print account details
        System.out.println("Account Details: " + accountJsonResponse.toString());
    }
  @Test
  public int testAddCard() {
      // Define base URI for the add card API
      String addCardBaseURI = "https://my-api-staging.easypayfinance.com";

      // Fetch access token from the getToken() method
      String accessToken = getToken();

      // Define the payload for adding the card
      JSONObject requestParams = new JSONObject();
      requestParams.put("loanId", 1145688);
      requestParams.put("type", "Credit");
      requestParams.put("nickname", "");
      requestParams.put("cardNumber", "4444441111111111");
      requestParams.put("expirationDate", "10/26");
      requestParams.put("note", "Note");
      requestParams.put("zipCode", "91911");

      // Perform POST request to add the card
      Response addCardResponse = RestAssured.given()
          .baseUri(addCardBaseURI)
          .header("Authorization", "Bearer " + accessToken) // Include access token in the request header
          .contentType(ContentType.JSON)
          .body(requestParams.toString())
          .post("/api/PaymentMethods/card")
          .then()
          .statusCode(200)
          .extract().response(); // Extracting the response

      // Validate the response
      JSONObject addCardJsonResponse = new JSONObject(addCardResponse.getBody().asString());
      System.out.println("Response: " + addCardJsonResponse.toString());
      
      // Assert or validate the response as per your requirement
      Assert.assertEquals(addCardJsonResponse.getString("expirationDate"), "10/26");
      Assert.assertEquals(addCardJsonResponse.getString("paymentNetwork"), "Visa");
      Assert.assertEquals(addCardJsonResponse.getInt("loanId"), 1145688);
      return addCardJsonResponse.getInt("loanId");
      // For example, you can check if the card details in the response match the ones you sent in the request.
  }
  public void testSchedulePayment() {
      // Define base URI for the schedule payment API
      String schedulePaymentBaseURI = "https://my-api-staging.easypayfinance.com";

      // Fetch access token from the getToken() method
      String accessToken = getToken();

      // Define the payload for scheduling the payment
      JSONObject requestParams = new JSONObject();
      requestParams.put("loanId", testAddCard()); // Use the loan ID obtained from the previous response
      requestParams.put("paymentAmount", 100);
      requestParams.put("runDate", "2024-04-17");
      requestParams.put("paymentMethodId", 2990026);
      requestParams.put("isTodayPayoff", false);
      requestParams.put("paymentMethodType", "Credit");

      // Perform POST request to schedule the payment
      Response schedulePaymentResponse = RestAssured.given()
          .baseUri(schedulePaymentBaseURI)
          .header("Authorization", "Bearer " + accessToken) // Include access token in the request header
          .contentType(ContentType.JSON)
          .body(requestParams.toString())
          .post("/api/Accounts/payments/schedule")
          .then()
          .statusCode(200)
          .extract().response(); // Extracting the response

      // Validate the response
      JSONObject schedulePaymentJsonResponse = new JSONObject(schedulePaymentResponse.getBody().asString());
      System.out.println("Response: " + schedulePaymentJsonResponse.toString());
      
      // Assert the response fields
      Assert.assertEquals(schedulePaymentJsonResponse.getBoolean("success"), true);
      Assert.assertEquals(schedulePaymentJsonResponse.getString("message"), "Payment scheduled successfully");
      Assert.assertEquals(schedulePaymentJsonResponse.getInt("loanId"), 1145688);
      Assert.assertEquals(schedulePaymentJsonResponse.getJSONObject("paymentAmount").getInt("cents"), 100);
      Assert.assertEquals(schedulePaymentJsonResponse.getJSONObject("paymentAmount").getInt("units"), 1);
      Assert.assertEquals(schedulePaymentJsonResponse.getString("runDate"), "2024-04-17T00:00:00");
      Assert.assertEquals(schedulePaymentJsonResponse.getString("status"), "Pending");
      Assert.assertEquals(schedulePaymentJsonResponse.getBoolean("isTodayPayoff"), false);
  }
  
  
}
