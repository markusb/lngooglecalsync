package LotusNotesGoogleCalendarBridge.GoogleService;

import LotusNotesGoogleCalendarBridge.LotusNotesService.NotesCalendarEntry;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.*;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import java.io.*;
import java.net.*;
import java.util.List;

public class GoogleImport {

    public GoogleImport(String accountname, String password) {
        try {
            calendarsFeedUrl = new URL("https://www.google.com/calendar/feeds/" + accountname + "/private/full");
            calendarFeedUrl = new URL("https://www.google.com/calendar/feeds/" + accountname + "/owncalendars/full");
            service = new CalendarService("Corporate-LotusNotes-Calendar");
            //service.useSsl();
            service.setUserCredentials(accountname, password);
        } catch (IOException e) {
            System.err.println("API Error: " + e);
        } catch (ServiceException e) {
            System.err.println("API Error: " + e);
        }
    }

    public GoogleImport() {
    }

    /*
    public static void main(String[] a) {
    try {
    ProxyConfigBean prx = new ProxyConfigBean();
    prx.setEnabled(true);
    prx.setProxyHost("10.162.32.100");
    prx.setProxyPort("3128");
    prx.activateNow();

    GoogleImport gi = new GoogleImport("", "");
    gi.deleteCalendar();
    CalendarEntry calentry = gi.createCalendar();

    } catch (IOException ex) {
    Logger.getLogger(GoogleImport.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ServiceException ex) {
    Logger.getLogger(GoogleImport.class.getName()).log(Level.SEVERE, null, ex);
    }
    }
     */
    public CalendarEntry createCalendar() throws IOException, ServiceException {

        CalendarEntry calendar = new CalendarEntry();
        calendar.setTitle(new PlainTextConstruct("Lotus Notes"));
        calendar.setContent(new PlainTextConstruct("Lotus Notes"));
        calendar.setSummary(new PlainTextConstruct("Lotus Notes Calendar"));
        calendar.setHidden(HiddenProperty.FALSE);
        calendar.setColor(new ColorProperty(BLUE));

        CalendarEntry returnedCalendar = service.insert(calendarFeedUrl, calendar);

        // this sometimes does not work with the title, so we will do it again below
        // its a bit strange as there is not real 'commit' used below but it works...
        calendar.setTitle(new PlainTextConstruct("Lotus Notes"));
        lotusNotesFeedUrl = new URL(returnedCalendar.getId());
        System.out.println(lotusNotesFeedUrl);

        return returnedCalendar;
    }

    public void deleteCalendar() {

        try {
            CalendarFeed calendars = service.getFeed(calendarFeedUrl, CalendarFeed.class);

            for (int i = 0; i < calendars.getEntries().size(); i++) {
                CalendarEntry entry = calendars.getEntries().get(i);
                if (entry.getTitle().getPlainText().equals("Lotus Notes")) {
                    String entryId = entry.getId();                    
                    entry.delete();
                }

            }
        } catch (Exception e) {
            System.err.println("API Error: " + e);
        }
    }

    public void createEvent(List cals) throws ServiceException, IOException {

        for (int i = 0; i < cals.size(); i++) {
            NotesCalendarEntry cal = (NotesCalendarEntry) cals.get(i);
            CalendarEventEntry event = new CalendarEventEntry();

            event.setTitle(new PlainTextConstruct(cal.getSubject()));
            Where location = new Where();
            location.setValueString(cal.getLocation());
            event.addLocation(location);
            DateTime startTime, endTime;
            try {
                startTime = DateTime.parseDateTime(cal.getStartDateTime());
                endTime = DateTime.parseDateTime(cal.getEndDateTime());
            } catch (Exception e) {
                System.err.println("Skipping a calendar entry as it is not supported yet!");
                continue;
            }
            When eventTimes = new When();
            eventTimes.setStartTime(startTime);
            eventTimes.setEndTime(endTime);
            event.addTime(eventTimes);
            URL test = new URL("http://www.google.com/calendar/feeds/shinsterneck%40gmail.com/owncalendars/full/e6qnlto4hfkpj13brtk4p8ci90%40group.calendar.google.com");
            try {
                service.insert(test, event);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    URL calendarsFeedUrl = null;
    URL calendarFeedUrl = null;
    URL lotusNotesFeedUrl = null;
    CalendarService service;
    String BLUE = "#2952A3";
}
