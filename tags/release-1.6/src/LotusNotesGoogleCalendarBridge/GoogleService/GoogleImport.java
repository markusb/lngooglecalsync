package LotusNotesGoogleCalendarBridge.GoogleService;

import LotusNotesGoogleCalendarBridge.LotusNotesService.NotesCalendarEntry;
import com.google.gdata.client.GoogleService.*;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.*;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.Reminder.Method;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.UUID;

public class GoogleImport {

    public GoogleImport(String accountname, String password, boolean useSSL) throws Exception {
        try {
            // Get the absolute path to this app
            String appPath = new java.io.File("").getAbsolutePath() + System.getProperty("file.separator");
            googleInRangeEntriesFullFilename = appPath = googleInRangeEntriesFilename;

            String protocol = "http:";
            if (useSSL) {
                protocol = "https:";
            }

            mainCalendarFeedUrl = new URL(protocol + "//www.google.com/calendar/feeds/" + accountname + "/owncalendars/full");
            privateCalendarFeedUrl = new URL(protocol + "//www.google.com/calendar/feeds/" + accountname + "/private/full");

            service = new CalendarService("LotusNotes-Calendar-Sync");
            if (useSSL) {
                service.useSsl();
            }

            service.setUserCredentials(accountname, password);

            createCalendar();
        } catch (InvalidCredentialsException ex) {
            throw new Exception("The username and/or password are invalid for signing into Google.", ex);
        } catch (AuthenticationException ex) {
            throw new Exception("Unable to login to Google. Perhaps you need to use a proxy server.", ex);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public GoogleImport() {
    }


    /**
     * Get the calendar feed URL for the calendar we want to update.
     * @return The calendar feed URL for the calendar we want to update.
     */
    protected URL getDestinationCalendarUrl() throws Exception {
        CalendarFeed calendars = null;
        int retryCount = 0;

        try {
            // If true, we already know our calendar URL
            if (destinationCalendarFeedUrl != null)
                return destinationCalendarFeedUrl;

            do {
                try {
                    calendars = service.getFeed(mainCalendarFeedUrl, CalendarFeed.class);
                } catch (com.google.gdata.util.ServiceException ex) {
                    calendars = null;
                    // If there is a network problem while connecting to Google, retry a few times
                    if (++retryCount > 10)
                        throw ex;
                    Thread.sleep(200);
                }
            } while (calendars == null);


            for (int i = 0; i < calendars.getEntries().size(); i++) {
                CalendarEntry calendar = calendars.getEntries().get(i);

                // If true, we've found the name of the destination calendar
                if (calendar.getTitle().getPlainText().equals(destinationCalendarName)) {
                    destinationCalendarFeedUrl = new URL(calendar.getLink("alternate", "application/atom+xml").getHref());
                }
            }
        } catch (Exception ex) {
            throw ex;
        }

        return destinationCalendarFeedUrl;
    }




    /**
     * Creates a Google calendar for the desired name (if it doesn't already exist).
     * @throws IOException
     * @throws ServiceException
     */
    public void createCalendar() throws Exception, IOException, ServiceException {
        // If true, the calendar already exists
        if (getDestinationCalendarUrl() != null) {
            return;
        }

        CalendarEntry calendar = new CalendarEntry();
        calendar.setTitle(new PlainTextConstruct(destinationCalendarName));
//        calendar.setSummary(new PlainTextConstruct("Lotus Notes Calendar"));
        // Get this machine's current time zone when creating the new Google calendar
        TimeZone localTimeZone = TimeZone.getDefault();
        calendar.setTimeZone(new TimeZoneProperty(localTimeZone.getID()));
        
        calendar.setHidden(HiddenProperty.FALSE);
        calendar.setSelected(SelectedProperty.TRUE);
        calendar.setColor(new ColorProperty(DEST_CALENDAR_COLOR));

        CalendarEntry returnedCalendar = service.insert(mainCalendarFeedUrl, calendar);
        returnedCalendar.update();

        // Get the feed url reference so that we can add events to the new calendar.
        destinationCalendarFeedUrl = new URL(returnedCalendar.getLink("alternate", "application/atom+xml").getHref());

        return;
    }


    /**
     * Delete all Google calendar entries for a specific date range.
     * @return The number of entries successfully deleted.
     */
    public int deleteCalendarEntries() throws Exception {
        try {
            URL feedUrl = getDestinationCalendarUrl();
            CalendarQuery myQuery = new CalendarQuery(feedUrl);

            // Get today - 7 days
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DATE, -7);
            // Clear out the time portion
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);

            myQuery.setMinimumStartTime(new com.google.gdata.data.DateTime(now.getTime()));
            // Make the end time far into the future so we delete everything
            myQuery.setMaximumStartTime(com.google.gdata.data.DateTime.parseDateTime("2099-12-31T23:59:59"));

            // Set the maximum number of results to return for the query.
            // Note: A GData server may choose to provide fewer results, but will never provide
            // more than the requested maximum.
            myQuery.setMaxResults(5000);
            int startIndex = 1;
            int entriesReturned;
            int retryCount = 0;

            List<CalendarEventEntry> allCalEntries = new ArrayList<CalendarEventEntry>();
            CalendarEventFeed resultFeed;

            // Run our query as many times as necessary to get all the
            // Google calendar entries we want
            while (true) {
                myQuery.setStartIndex(startIndex);

                try {
                    // Execute the query and get the response
                    resultFeed = service.query(myQuery, CalendarEventFeed.class);
                } catch (com.google.gdata.util.ServiceException ex) {
                    // If there is a network problem while connecting to Google, retry a few times
                    if (++retryCount > 10)
                        throw ex;
                    Thread.sleep(200);

                    continue;
                }

                entriesReturned = resultFeed.getEntries().size();
                if (entriesReturned == 0)
                    // We've hit the end of the list
                    break;

                // Add the returned entries to our local list
                allCalEntries.addAll(resultFeed.getEntries());

                startIndex = startIndex + entriesReturned;
            }

            // Delete all the entries as a batch delete
            CalendarEventFeed batchRequest = new CalendarEventFeed();

            for (int i = 0; i < allCalEntries.size(); i++) {
                CalendarEventEntry entry = allCalEntries.get(i);

                BatchUtils.setBatchId(entry, Integer.toString(i));
                BatchUtils.setBatchOperationType(entry, BatchOperationType.DELETE);
                batchRequest.getEntries().add(entry);
            }

            // Get the batch link URL and send the batch request
            Link batchLink = resultFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
            CalendarEventFeed batchResponse = service.batch(new URL(batchLink.getHref()), batchRequest);

            // Ensure that all the operations were successful
            boolean isSuccess = true;
            StringBuffer batchFailureMsg = new StringBuffer("These entries in the batch delete failed:");
            for (CalendarEventEntry entry : batchResponse.getEntries()) {
                String batchId = BatchUtils.getBatchId(entry);
                if (!BatchUtils.isSuccess(entry)) {
                    isSuccess = false;
                    BatchStatus status = BatchUtils.getBatchStatus(entry);
                    batchFailureMsg.append("\nID: " + batchId + "  Reason: " + status.getReason());
                }
            }

            if (!isSuccess) {
                throw new Exception(batchFailureMsg.toString());
            }

            return batchRequest.getEntries().size();
        } catch (Exception ex) {
            throw ex;
        }
    }


    /**
     * Delete all Google calendar entries in the provided list.
     * @return The number of entries successfully deleted.
     */
    public int deleteCalendarEntries(ArrayList<CalendarEventEntry> googleCalEntries) throws Exception {
        try {
            if (googleCalEntries.size() == 0)
                return 0;

            URL feedUrl = getDestinationCalendarUrl();

            int retryCount = 0;

            // Delete all the entries as a batch delete
            CalendarEventFeed batchRequest = new CalendarEventFeed();

            for (int i = 0; i < googleCalEntries.size(); i++) {
                CalendarEventEntry entry = googleCalEntries.get(i);

                BatchUtils.setBatchId(entry, Integer.toString(i));
                BatchUtils.setBatchOperationType(entry, BatchOperationType.DELETE);
                batchRequest.getEntries().add(entry);
            }

            CalendarEventFeed feed = service.getFeed(feedUrl, CalendarEventFeed.class);

//            CalendarEventFeed resultFeed;
//            resultFeed = service.getFeed(feedUrl, CalendarFeed.class);
  
            // Get the batch link URL and send the batch request
            Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
            CalendarEventFeed batchResponse = service.batch(new URL(batchLink.getHref()), batchRequest);

            // Ensure that all the operations were successful
            boolean isSuccess = true;
            StringBuffer batchFailureMsg = new StringBuffer("These entries in the batch delete failed:");
            for (CalendarEventEntry entry : batchResponse.getEntries()) {
                String batchId = BatchUtils.getBatchId(entry);
                if (!BatchUtils.isSuccess(entry)) {
                    isSuccess = false;
                    BatchStatus status = BatchUtils.getBatchStatus(entry);
                    batchFailureMsg.append("\nID: " + batchId + "  Reason: " + status.getReason());
                }
            }

            if (!isSuccess) {
                throw new Exception(batchFailureMsg.toString());
            }

            return batchRequest.getEntries().size();
        } catch (Exception ex) {
            throw ex;
        }
    }


    /**
     * Get all the Google calendar entries for a specific date range.
     * @return The found entries.
     */
    public ArrayList<CalendarEventEntry> getCalendarEntries() throws Exception {
        try {
            ArrayList<CalendarEventEntry> allCalEntries = new ArrayList<CalendarEventEntry>();

            URL feedUrl = getDestinationCalendarUrl();
            CalendarQuery myQuery = new CalendarQuery(feedUrl);

            // Get today - 7 days
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DATE, -7);
            // Clear out the time portion
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);

            myQuery.setMinimumStartTime(new com.google.gdata.data.DateTime(now.getTime()));
            // Make the end time far into the future so we delete everything
            myQuery.setMaximumStartTime(com.google.gdata.data.DateTime.parseDateTime("2099-12-31T23:59:59"));

            // Set the maximum number of results to return for the query.
            // Note: A GData server may choose to provide fewer results, but will never provide
            // more than the requested maximum.
            myQuery.setMaxResults(5000);
            int startIndex = 1;
            int entriesReturned;
            int retryCount = 0;

            CalendarEventFeed resultFeed;

            // Run our query as many times as necessary to get all the
            // Google calendar entries we want
            while (true) {
                myQuery.setStartIndex(startIndex);

                try {
                    // Execute the query and get the response
                    resultFeed = service.query(myQuery, CalendarEventFeed.class);
                } catch (com.google.gdata.util.ServiceException ex) {
                    // If there is a network problem while connecting to Google, retry a few times
                    if (++retryCount > 10)
                        throw ex;
                    Thread.sleep(200);
                    
                    continue;
                }

                entriesReturned = resultFeed.getEntries().size();
                if (entriesReturned == 0)
                    // We've hit the end of the list
                    break;

                // Add the returned entries to our local list
                allCalEntries.addAll(resultFeed.getEntries());

                startIndex = startIndex + entriesReturned;
            }

            if (diagnosticMode)
                writeInRangeEntriesToFile(allCalEntries);

            return allCalEntries;
        } catch (Exception ex) {
            throw ex;
        }
    }


    /**
     * Write key parts of the Google calendar entries to a text file.
     * @param calendarEntries - The calendar entries to process.
     */
    public void writeInRangeEntriesToFile(ArrayList<CalendarEventEntry> calendarEntries) throws Exception {
        try {
            // Open the output file if it is not open
            if (googleInRangeEntriesWriter == null) {
                googleInRangeEntriesFile = new File(googleInRangeEntriesFullFilename);
                googleInRangeEntriesWriter = new BufferedWriter(new FileWriter(googleInRangeEntriesFile));
            }

            if (calendarEntries == null)
                googleInRangeEntriesWriter.write("The calendar entries list is null.\n");
            else
                for (CalendarEventEntry calEntry : calendarEntries) {
                    googleInRangeEntriesWriter.write("=== Calendar Entry ===\n");

                    googleInRangeEntriesWriter.write("  Title: " + calEntry.getTitle().getPlainText() + "\n");
                    googleInRangeEntriesWriter.write("  IcalUID: " + calEntry.getIcalUID() + "\n");
                    for (When eventTime : calEntry.getTimes()) {
                        googleInRangeEntriesWriter.write("  Start Date: " + new Date(eventTime.getStartTime().getValue()) + "\n");
                        googleInRangeEntriesWriter.write("  End Date: " + new Date(eventTime.getEndTime().getValue()) + "\n");
                    }
                    googleInRangeEntriesWriter.write("  Edited Date: " + new Date(calEntry.getEdited().getValue()) + "\n");
                    googleInRangeEntriesWriter.write("  Updated Date: " + new Date(calEntry.getUpdated().getValue()) + "\n");

                    googleInRangeEntriesWriter.write("\n\n");
                }
        } catch (Exception ex) {
            throw ex;
        }
        finally {
            if (googleInRangeEntriesWriter != null) {
                googleInRangeEntriesWriter.close();
                googleInRangeEntriesWriter = null;
            }
        }
    }

    // Delete the destination calendar.
    //
    // TODO What if entries were created in the main calendar (instead of the Lotus Notes
    // calendar)? This code only deletes from the Lotus Notes calendar.
    // Because this isn't working quite yet, I've disabled the "sync to main calendar"
    // checkbox on the Advanced tab.
    public void deleteCalendar() throws Exception {

        try {
            CalendarFeed calendars = service.getFeed(mainCalendarFeedUrl, CalendarFeed.class);

            // Loop through each calendar
            for (int i = 0; i < calendars.getEntries().size(); i++) {
                CalendarEntry calendar = calendars.getEntries().get(i);

                // If this is the Lotus Notes calendar, delete it
                if (calendar.getTitle().getPlainText().equals(destinationCalendarName)) {
                    calendar.delete();
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }


    /**
     * Compare the Lotus and Google entries based on the Lotus modified timestamp.
     * On exit, lotusCalEntries will only contain the entries we want created and
     * googleCalEntries will only contain the entries we want deleted.
     */
    public void compareCalendarEntries(ArrayList<NotesCalendarEntry> lotusCalEntries, ArrayList<CalendarEventEntry> googleCalEntries) {
        // Loop through all Lotus entries
        for (int i = 0; i < lotusCalEntries.size(); i++) {
            NotesCalendarEntry lotusEntry = lotusCalEntries.get(i);
            String syncUID = lotusEntry.getSyncUID();

            // Loop through all Google entries for each Lotus entry.  This isn't
            // normally efficient, but we have small lists (probably less than 300).
            for (int j = 0; j < googleCalEntries.size(); j++) {
                // The Google IcalUID has the format: GoogleUID:SyncUID.
                // Strip off the "GoogleUID:" and do a compare.
                if (googleCalEntries.get(j).getIcalUID().substring(33).equals(syncUID)) {
                    // The Lotus and Google entries are identical, so remove them from out lists.
                    // They don't need created or deleted.
                    lotusCalEntries.remove(i--);
                    googleCalEntries.remove(j--);
                    break;
                }
            }
        }
    }


    // This method is for testing purposes.
    public void createSampleCalEntry() {
        NotesCalendarEntry cal = new NotesCalendarEntry();
        cal.setSubject("DeanRepeatTest");
        cal.setEntryType(NotesCalendarEntry.EntryType.APPOINTMENT);
        cal.setAppointmentType("3");
        cal.setLocation("nowhere");

        Date dstartDate, dendDate;
        Calendar now = Calendar.getInstance();
        now.set(Calendar.YEAR, 2010);
        now.set(Calendar.MONTH, 7);  // Month is relative zero
        now.set(Calendar.DAY_OF_MONTH, 2);
        now.set(Calendar.HOUR_OF_DAY, 10);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        dstartDate = now.getTime();
        cal.setStartDateTime(dstartDate);

        now.set(Calendar.HOUR_OF_DAY, 11);
        dendDate = now.getTime();
        cal.setEndDateTime(dendDate);

        DateTime startTime, endTime;

        CalendarEventEntry event = new CalendarEventEntry();
        event.setTitle(new PlainTextConstruct(cal.getSubject()));

        String locationStr = cal.getLocation();
        if (locationStr != null && !locationStr.isEmpty()) {
            Where location = new Where();
            location.setValueString(locationStr);
            event.addLocation(location);
        }


        try {
            When eventTime = new When();
            eventTime.setStartTime(DateTime.parseDateTime(cal.getStartDateTimeGoogle()));
            eventTime.setEndTime(DateTime.parseDateTime(cal.getEndDateTimeGoogle()));
            event.addTime(eventTime);

            Date dstartDate2, dendDate2;
            now.set(Calendar.DAY_OF_MONTH, 3);
            now.set(Calendar.HOUR_OF_DAY, 10);
            dstartDate2 = now.getTime();
            cal.setStartDateTime(dstartDate2);
            now.set(Calendar.HOUR_OF_DAY, 11);
            dendDate2 = now.getTime();
            cal.setEndDateTime(dendDate2);

            eventTime = new When();
            eventTime.setStartTime(DateTime.parseDateTime(cal.getStartDateTimeGoogle()));
            eventTime.setEndTime(DateTime.parseDateTime(cal.getEndDateTimeGoogle()));
            event.addTime(eventTime);
            int j = event.getTimes().size();
            j++;

            service.insert(getDestinationCalendarUrl(), event);
        } catch (Exception e) {
        }
    }


    /**
     * Create Lotus Notes calendar entries in the Google calendar.
     * @param lotusCalEntries - The list of Lotus Notes calendar entries.
     * @return The number of Google calendar entries successfully created.
     * @throws ServiceException
     * @throws IOException
     */
    public int createCalendarEntries(ArrayList<NotesCalendarEntry> lotusCalEntries) throws Exception, ServiceException, IOException {
        int retryCount = 0;
        int createdCount = 0;

        for (int i = 0; i < lotusCalEntries.size(); i++) {
            NotesCalendarEntry lotusEntry = lotusCalEntries.get(i);
            CalendarEventEntry event = new CalendarEventEntry();
            event.setTitle(new PlainTextConstruct(lotusEntry.getSubject()));

            // The Google IcalUID must be unique for all eternity or we'll get a
            // VersionConflictException during the insert. So start the IcalUID string
            // with a newly generate UUID (with the '-' chars removed).  Then add the values
            // we really want to remember (referred to as the SyncUID).
            event.setIcalUID(UUID.randomUUID().toString().replaceAll("-", "") + ":" + lotusEntry.getSyncUID());

            if (syncDescription && lotusEntry.getBody() != null)
                event.setContent(new PlainTextConstruct(lotusEntry.getBody()));

            String locationStr = lotusEntry.getLocation();
            if (locationStr != null && !locationStr.isEmpty()) {
                Where location = new Where();
                location.setValueString(locationStr);
                event.addLocation(location);
            }

            DateTime startTime, endTime;
            if (lotusEntry.getEntryType() == NotesCalendarEntry.EntryType.TASK ||
                    lotusEntry.getAppointmentType() == NotesCalendarEntry.AppointmentType.ALL_DAY_EVENT ||
                    lotusEntry.getAppointmentType() == NotesCalendarEntry.AppointmentType.ANNIVERSARY)
            {
                // Create an all-day event by setting start/end dates with no time portion
                startTime = DateTime.parseDate(lotusEntry.getStartDateGoogle());
                // IMPORTANT: For Google to properly create an all-day event, we must add
                // one day to the end date
                if (lotusEntry.getEndDateTime() == null)
                    // Use start date since the end date is null
                    endTime = DateTime.parseDate(lotusEntry.getStartDateGoogle(1));
                else
                    endTime = DateTime.parseDate(lotusEntry.getEndDateGoogle(1));
            }
            else if (lotusEntry.getAppointmentType() == NotesCalendarEntry.AppointmentType.APPOINTMENT ||
                    lotusEntry.getAppointmentType() == NotesCalendarEntry.AppointmentType.MEETING)
            {
                // Create a standard event
                startTime = DateTime.parseDateTime(lotusEntry.getStartDateTimeGoogle());
                if (lotusEntry.getEndDateTime() == null)
                    // Use start date since the end date is null
                    endTime = DateTime.parseDateTime(lotusEntry.getStartDateTimeGoogle());
                else
                    endTime = DateTime.parseDateTime(lotusEntry.getEndDateTimeGoogle());
            }
            else if (lotusEntry.getAppointmentType() == NotesCalendarEntry.AppointmentType.REMINDER)
            {
                // Create a standard event with the start and end times the same
                startTime = DateTime.parseDateTime(lotusEntry.getStartDateTimeGoogle());
                endTime = DateTime.parseDateTime(lotusEntry.getStartDateTimeGoogle());
            }
            else
            {
                throw new Exception("Couldn't determine Lotus Notes event type.\nEvent subject: " + lotusEntry.getSubject() +
                        "\nEntry Type: " + lotusEntry.getEntryType() +
                        "\nAppointment Type: " + lotusEntry.getAppointmentType());
            }

            When eventTime = new When();
            eventTime.setStartTime(startTime);
            eventTime.setEndTime(endTime);
            event.addTime(eventTime);

            if (syncAlarms && lotusEntry.getAlarm()) {
                Reminder reminder = new Reminder();

                // Lotus Notes alarms can be before (negative value) or after (positive value)
                // the event.  Google only supports alarms before the event and the number
                // is then positive.
                // So, convert to Google as follows: alarms after (positive) are made 0,
                // alarms before (negative) are made positive.
                int alarmMins = 0;
                if (lotusEntry.getAlarmOffsetMins() < 0)
                    alarmMins = Math.abs(lotusEntry.getAlarmOffsetMins());

                reminder.setMinutes(alarmMins);
                reminder.setMethod(Method.ALERT);
                event.getReminder().add(reminder);
            }

            retryCount = 0;
            do {
                try {
                    service.insert(getDestinationCalendarUrl(), event);
                    createdCount++;
                    break;
                } catch (com.google.gdata.util.ServiceException ex) {
                    // If there is a network problem while connecting to Google, retry a few times
                    if (++retryCount > 10)
                        throw ex;
                    Thread.sleep(200);
                }
            } while (true);
        }

        return createdCount;
    }

    public void setSyncDescription(boolean value) {
        syncDescription = value;
    }

    public void setSyncAlarms(boolean value) {
        syncAlarms = value;
    }

    public void setDiagnosticMode(boolean value) {
        diagnosticMode = value;
    }

    URL mainCalendarFeedUrl = null;
    URL privateCalendarFeedUrl = null;
    URL destinationCalendarFeedUrl = null;
    CalendarService service;

    BufferedWriter googleInRangeEntriesWriter = null;
    File googleInRangeEntriesFile = null;

    boolean diagnosticMode = false;

    boolean syncDescription = false;
    boolean syncAlarms = false;

    final String googleInRangeEntriesFilename = "GoogleInRangeEntries.txt";
    // Filename with full path
    String googleInRangeEntriesFullFilename;

    String destinationCalendarName = "Lotus Notes";
    String DEST_CALENDAR_COLOR = "#F2A640";
}
