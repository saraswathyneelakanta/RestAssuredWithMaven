package invitation;

import org.testng.annotations.Test;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import junit.framework.Assert;

import static io.restassured.RestAssured.given;


import com.tngtech.java.junit.dataprovider.*;
import org.junit.runner.RunWith;


@RunWith(DataProviderRunner.class)
public class InvitationTest {
	
	
	RequestSpecBuilder invitationRequBuilder;
	static RequestSpecification requestSpec;
	
	RequestSpecBuilder tokenReqBuilder;
	static RequestSpecification tokenRequestSpec;
	
	private static String appToken = null;
	private static List<Integer> invitationIdList = new ArrayList<Integer>();
	
    private static final String PASSWORD = "test123";
    private static final String APP_ID_NAME = "ApplicationID";
    private static final String APP_ID_VALUE = "1234567890";
    private static final String USER_ID_NAME = "UserID";
    private static final String USER_ID_VALUE ="user1";
    
	
	@BeforeClass
	public void setup() {
		
		tokenReqBuilder = new RequestSpecBuilder();
		tokenReqBuilder.setBaseUri("http://authenticate.testapp.com");
		tokenReqBuilder.setPort(8080);
		tokenRequestSpec = tokenReqBuilder.build();
		
		invitationRequBuilder = new RequestSpecBuilder();
		invitationRequBuilder.setBaseUri("https://testApplication.com");
		invitationRequBuilder.setBasePath("/invitation");
		invitationRequBuilder.setPort(5030);
		requestSpec = invitationRequBuilder.build().contentType(ContentType.XML)
				.accept("text/XML").header(APP_ID_NAME, APP_ID_VALUE,USER_ID_NAME,USER_ID_VALUE);
		
	}
	
	//once the token is generated, we can store in a properties file with the timestamp and fetch it when we need the token. 
	//if the token is expired we can generate using this method.
	@BeforeTest
	public void getToken() {
		Response response = given().auth()
		  .basic(USER_ID_VALUE, PASSWORD)
		  .spec(tokenRequestSpec)
		  .when()
		  .get("/Service/httpservice")
		  .then()
		  .log().body()
		  .assertThat()
		  .statusCode(200).extract().response();
		
		String responseString = response.asString();
		this.appToken = response.path("ServiceResponse.ResponseBody.token");

	}
	
    //post
		@Test
		@UseDataProvider("createInvitationData")
		public void validCreateRequest(String name, String email) {
			InvitationRequest invitationreq = new InvitationRequest();
			UserData userdata = new UserData();
			userdata.setUserName(name);
			userdata.setUserEmail(email);
			invitationreq.setUserData(userdata);
			
			Response response = given()
			 .spec(requestSpec)
			 .header("AppToken",InvitationTest.appToken)
			 .body(invitationreq) 
			.when().post("/create")
			.then()
			.assertThat()
			.statusCode(201)
			.contentType(ContentType.XML)
			.body("InvitationResponse.ResponseBody.status", hasItem("QUEUED"),
			      "InvitationResponse.ResponseBody.status", hasItem("INVITED"),
			      "InvitationResponse.ResponseBody.status", hasItem("CANCELLED")).extract().response();
			
			int id =response.path("ServiceResponse.ResponseBody.id");
			invitationIdList.add(id);
			
		
		}

	//get
	@Test
	@UseDataProvider("createdInvitationIds")
	public void validReadrequest(int id) {
		given()
		.spec(requestSpec)
		.pathParam("AppToken", InvitationTest.appToken)
		.when().get("/fetch/{id}")
		.then()
		.log().body()
		.assertThat()
        .statusCode(200)
        .contentType(ContentType.XML)
		.body("InvitationResponse.ResponseBody.status", hasItem("QUEUED"),
			      "InvitationResponse.ResponseBody.status", hasItem("INVITED"),
			      "InvitationResponse.ResponseBody.status", hasItem("CANCELLED"),
			      "InvitationResponse.ResponseBody.id",is(any(int.class)));
		}
	
	
	
	//put
	@Test
	@UseDataProvider("updateEmailAddress")
	public void validUpdateRequest(int Id, String emailId) {
	    InvitationRequest invitationreq = new InvitationRequest();
		UserData userdata = new UserData();
		invitationreq.setInvitationId(Id);
		userdata.setUserEmail(emailId);
		invitationreq.setUserData(userdata);
		
		given()
		 .spec(requestSpec)
		 .header("AppToken",InvitationTest.appToken)
		 .body(invitationreq)
		 .when().put("/update/{Id}")
		 .then()
		 .assertThat()
		 .statusCode(200)
		 .contentType(ContentType.XML)
		 .body("InvitationResponse.ResponseBody.status", hasItem("QUEUED"),
			      "InvitationResponse.ResponseBody.status", hasItem("INVITED"),
			      "InvitationResponse.ResponseBody.status", hasItem("CANCELLED"));
		
		
	}
	
	//delete
	@Test
	@UseDataProvider("createdInvitationIds")
	public void ValidDeleteRequest(int id) {
		
		InvitationRequest invitationreq = new InvitationRequest();
		invitationreq.setInvitationId(id);
		
		given()
		 .spec(requestSpec)
		 .header("AppToken",InvitationTest.appToken)
		 .body(invitationreq)
		 .when().delete("/delete")
		 .then()
		 .assertThat()
		 .statusCode(200)
		 .contentType(ContentType.XML)
		 .body("InvitationResponse.ResponseBody.status", hasItem("QUEUED"),
			      "InvitationResponse.ResponseBody.status", hasItem("INVITED"),
			      "InvitationResponse.ResponseBody.status", hasItem("CANCELLED"),
			      "InvitationResponse.ResponseBody.id",is(any(int.class)));
	}
	
	public static String getAppToken() {
		return appToken;
	}

	public static void setAppToken(String appToken) {
		InvitationTest.appToken = appToken;
	}
	
	@DataProvider
    public static Object[][] updateEmailAddress() {
        return new Object[][] {
            { 1234,  "Beverly@gmail.com" },
            { 4567,  "Schenectady@gmail.com" },
            { 2344,  "Waverley@gmail.com"}
        };

	}
	
	@DataProvider
	public static Object[][] createInvitationData(){
		return new Object[][] {
			{"amy", "amy@gmail.com"},
			{"Tristan", "Tristan@gmail.com"},
			{"Ryan", "ryan@gmail.com"}		
		};
	}
	
	@DataProvider
	public static List<Integer> createdInvitationIds(){
		return invitationIdList;
	}
}

