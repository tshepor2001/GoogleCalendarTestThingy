package calendar;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;


import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GoogleCalendarTestThingy {

    public static void main(String[] args) throws IOException, ParseException {

        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        Set<String> scope = Collections.singleton(CalendarScopes.CALENDAR);
        String clientId = "801893048902-q8b4mujpfkj31ac6uq37m3p4ms0e9l4s.apps.googleusercontent.com";
        String clientSecret = "uVpOFGwgbFV59U9GSi74gBZF";

        AuthorizationCodeFlow.Builder codeFlowBuilder =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport,
                        jsonFactory,
                        clientId,
                        clientSecret,
                        scope
                );
        AuthorizationCodeFlow codeFlow = codeFlowBuilder.build();

        String redirectUri = "http://localhost:8080/oauth2callback";
        AuthorizationCodeRequestUrl authorizationUrl = codeFlow.newAuthorizationUrl();
        authorizationUrl.setRedirectUri(redirectUri);
        System.out.println("Go to the following address:");
        System.out.println(authorizationUrl);

        //use the code that is returned as a url parameter
        //to request an authorization token
        System.out.println("What is the 'code' url parameter?");
        String code = new Scanner(System.in).nextLine();
        TokenResponse tokenResponse = getTokenResponse(codeFlow, redirectUri, code);

        //Now, with the token and user id, we have credentials
        Credential credential = codeFlow.createAndStoreCredential(tokenResponse, null);

        //Credentials may be used to initialize http requests
        Calendar calendar = getCalendar(httpTransport, jsonFactory, credential);

        FreeBusyRequest freeBusyRequest = getFreeBusyRequest();

        Calendar.Freebusy.Query query = calendar.freebusy().query(freeBusyRequest);
        FreeBusyResponse response = query.execute();
        System.out.println(response.getKind());
        for (TimePeriod period : response.getCalendars().get("tshepo.ramatsui@gmail.com").getBusy()) {
            System.out.println(period);

        }
    }

    private static Calendar getCalendar(HttpTransport httpTransport, JsonFactory jsonFactory, Credential credential) {
        HttpRequestInitializer initializer = credential;
        //and thus are used to initialize the calendar service
        Calendar.Builder serviceBuilder = new Calendar.Builder(
                httpTransport, jsonFactory, initializer);
        serviceBuilder.setApplicationName("Discovery OBS");
        return serviceBuilder.build();
    }

    private static TokenResponse getTokenResponse(AuthorizationCodeFlow codeFlow, String redirectUri, String code) throws IOException {
        AuthorizationCodeTokenRequest tokenRequest = codeFlow.newTokenRequest(code);
        tokenRequest.setRedirectUri(redirectUri);
        return tokenRequest.execute();
    }

    private static FreeBusyRequest getFreeBusyRequest() throws ParseException {
        FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
        FreeBusyRequestItem freeBusyRequestItem = new FreeBusyRequestItem();
        freeBusyRequestItem.setId("tshepo.ramatsui@gmail.com");
        List<FreeBusyRequestItem> items = new ArrayList<FreeBusyRequestItem>();
        items.add(freeBusyRequestItem);
        freeBusyRequest.setItems(items);
        freeBusyRequest.setCalendarExpansionMax(1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dtFrom = sdf.parse("2014-10-01");
        DateTime dateMin = new DateTime(dtFrom);
        freeBusyRequest.setTimeMin(dateMin);
        Date dtTo = sdf.parse("2014-11-05");
        DateTime dateMax = new DateTime(dtTo);
        freeBusyRequest.setTimeMax(dateMax);
        return freeBusyRequest;
    }
}
