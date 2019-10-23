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
	private static final String appId = "1234567890";
    private static final String password = "test123";
	
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
				.accept("text/XML").header("ApplicationID", appId,"UserID","user1");
		
	}
	
	//once the token is generated, we can store in a properties file with the timestamp and fetch it when we need the token. 
	//if the token is expired we can generate using this method.
	@BeforeTest
	public void getToken() {
		Response response = given().auth()
		  .basic(appId, password)
		  .spec(tokenRequestSpec)
		  .when()
		  .get("/Service/httpservice")
		  .then()
		  .assertThat()
		  .statusCode(200).extract().response();
		
		String responseString = response.asString();
		this.appToken = response.path("ServiceResponse.ResponseBody.token");

	}

	//get
	@Test
	@UseDataProvider("invitationIdRead")
	public void validReadrequest(int id) {
		given()
		.spec(requestSpec)
		.pathParam("AppToken", InvitationTest.appToken)
		.when().get("/fetch/{id}")
		.then()
		.assertThat()
        .statusCode(200).and().contentType(ContentType.XML);
		}
	
	//post
	@Test
	public void validCreateRequest() {
		InvitationRequest invitationreq = new InvitationRequest();
		UserData userdata = new UserData();
		userdata.setUserName("Anitha");
		userdata.setUserEmail("anitha@gmail.com");
		invitationreq.setUserData(userdata);
		
		given()
		 .spec(requestSpec)
		 .header("AppToken",InvitationTest.appToken)
		 .body(invitationreq) //XML serialization and deserialization is take care of by Jackson serialization
		 //https://www.james-willett.com/rest-assured-serialization-with-json-and-xml
		.when().post("/create")
		.then()
		.assertThat()
		.statusCode(201).and().contentType(ContentType.XML);
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
		 .statusCode(200).and().contentType(ContentType.XML);
		
	}
	
	//delete
	@Test
	@UseDataProvider("invitationIdRead")
	public void ValidDeleteRequest(int id) {
		
		InvitationRequest invitationreq = new InvitationRequest();
		invitationreq.setInvitationId(id);
		
		int invitId =given()
		 .spec(requestSpec)
		 .header("AppToken",InvitationTest.appToken)
		 .body(invitationreq)
		 .when().delete("/delete")
		 .then()
		 .assertThat()
		 .statusCode(200).and().contentType(ContentType.XML).extract().path("invitationID");
		
		Assert.assertEquals(id, invitId);
		
		
	}
	
	public static String getAppToken() {
		return appToken;
	}

	public static void setAppToken(String appToken) {
		InvitationTest.appToken = appToken;
	}
	
	@DataProvider
	public static int[] invitationIdRead() {
		return new int[] {1234,3456,4678};
	}
	
	@DataProvider
	public static int[] invitationIdUpdate() {
		return new int[] {1234,3456,4678};
	}
	
	
	
	@DataProvider
    public static Object[][] updateEmaiAddress() {
        return new Object[][] {
            { 1234,  "Beverly@gmail.com" },
            { 4567,  "Schenectady@gmail.com" },
            { 2344,  "Waverley@gmail.com"}
        };

	}
	
}

