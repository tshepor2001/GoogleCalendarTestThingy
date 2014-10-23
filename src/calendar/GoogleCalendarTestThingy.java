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
        String clientId = "764517824387-u2fs564hm8ag8o8sj03r1l1g13di3rrj.apps.googleusercontent.com";
        String clientSecret = "kYDYMXAttTEZqjAMvdwUTD_s";

        AuthorizationCodeFlow.Builder codeFlowBuilder =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport,
                        jsonFactory,
                        clientId,
                        clientSecret,
                        scope
                );
        AuthorizationCodeFlow codeFlow = codeFlowBuilder.build();

        String redirectUri = "http://localhost:8080/callback";
        AuthorizationCodeRequestUrl authorizationUrl = codeFlow.newAuthorizationUrl();
        authorizationUrl.setRedirectUri(redirectUri);
        System.out.println("Go to the following address:");
        System.out.println(authorizationUrl);

        //use the code that is returned as a url parameter
        //to request an authorization token
        System.out.println("What is the 'code' url parameter?");
        String code = new Scanner(System.in).nextLine();
        AuthorizationCodeTokenRequest tokenRequest = codeFlow.newTokenRequest(code);
        tokenRequest.setRedirectUri(redirectUri);
        TokenResponse tokenResponse = tokenRequest.execute();

        //Now, with the token and user id, we have credentials
        Credential credential = codeFlow.createAndStoreCredential(tokenResponse, null);

        //Credentials may be used to initialize http requests
        HttpRequestInitializer initializer = credential;
        //and thus are used to initialize the calendar service
        Calendar.Builder serviceBuilder = new Calendar.Builder(
                httpTransport, jsonFactory, initializer);
        serviceBuilder.setApplicationName("My Project");
        Calendar calendar = serviceBuilder.build();

        FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
        FreeBusyRequestItem freeBusyRequestItem = new FreeBusyRequestItem();
        freeBusyRequestItem.setId("tramats@thoughtworks.com");
        List<FreeBusyRequestItem> items = new ArrayList<FreeBusyRequestItem>();
        items.add(freeBusyRequestItem);
        freeBusyRequest.setItems(items);
        freeBusyRequest.setCalendarExpansionMax(1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dtFrom = sdf.parse("2014-10-01");
        DateTime dateMin = new DateTime(dtFrom);
        freeBusyRequest.setTimeMin(dateMin);
        Date dtTo = sdf.parse("2014-10-20");
        DateTime dateMax = new DateTime(dtTo);
        freeBusyRequest.setTimeMax(dateMax);

        Calendar.Freebusy.Query query = calendar.freebusy().query(freeBusyRequest);
        FreeBusyResponse response = query.execute();
        System.out.println(response.getKind());
        for (TimePeriod period : response.getCalendars().get("tramats@thoughtworks.com").getBusy()) {
            System.out.println(period);

        }

    }
}
