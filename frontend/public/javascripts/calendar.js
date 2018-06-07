// Your Client ID can be retrieved from your project in the Google
// Developer Console, https://console.developers.google.com
var CLIENT_ID = '977873630238-e7af6qmovht9fmd058pn20ang0pef3l6.apps.googleusercontent.com';
var API_KEY = 'AIzaSyADFc2oxztqV1PwB6NUY1phr4XiNr-S_8o';

var SCOPES = ["https://www.googleapis.com/auth/calendar"];

/**
 * Check if current user has authorized this application.
 */
function checkAuth() {
    gapi.auth.authorize(
        {
            'client_id': CLIENT_ID,
            'scope': SCOPES.join(' '),
            'immediate': true
        }, handleAuthResult);
}

/**
 * Handle response from authorization server.
 *
 * @param {Object} authResult Authorization result.
 */
function handleAuthResult(authResult) {
    var authorizeDiv = document.getElementById('authorize-div');
    if (authResult && !authResult.error) {
        // Hide auth UI, then load client library.
        authorizeDiv.style.display = 'none';
        $("#pleaseWait").show();
        loadCalendarApi();
    } else {
        // Show auth UI, allowing the user to initiate authorization by
        // clicking authorize button.
        authorizeDiv.style.display = 'inline';
    }
}

/**
 * Initiate auth flow in response to user clicking authorize button.
 *
 * @param {Event} event Button click event.
 */
function handleAuthClick(event) {
    gapi.auth.authorize(
        {client_id: CLIENT_ID, scope: SCOPES, immediate: false},
        handleAuthResult);
    return false;
}

/**
 * Load Google Calendar client library. List calendars
 * once client library is loaded.
 */
function loadCalendarApi() {
    gapi.client.load('calendar', 'v3', listCalendars);
}

function listCalendars() {
    var request = gapi.client.calendar.calendarList.list({});

    request.execute(function (resp) {
        var calendars = resp.items;

        if (calendars.length > 0) {
            for (i = 0; i < calendars.length; i++) {
                var calendar = calendars[i];
                if (calendar.accessRole == 'writer' || calendar.accessRole == 'owner')
                    $("#calendars").append(
                        $('<option />')
                            .attr('value', calendar.id)
                            .html(calendar.summary)
                    );
            }
        }
        if (chosen_browser_is_supported()) {
            $("#calendars").chosen({width: "450px"});
        } else {
            $("#calendars").show();
        }

        $("#pleaseWait").hide();
        $("#addAllButton").parent().show();
    });
}

function listSubjects() {
    $.ajax({
        url: "api.py",
        success: function (result) {
            for (var i = 0; i < result.length; i++) {
                $("#subjects").append(
                    $('<option />')
                        .attr('value', result[i])
                        .html(result[i])
                );
            }
            if (chosen_browser_is_supported()) {
                $("#subjects").chosen({width: "450px"});
            } else {
                $("#subjects").show();
            }
        }
    });
    $("#pleaseWait").hide();
    $("#addAllButton").parent().show();
}

function addCalendar() {
    var resource = {
        "summary": "Schedule"
    };
    var request = gapi.client.calendar.calendars.insert({
        'resource': resource
    });
    request.execute(function (resp) {
        console.log(resp);
        if (resp) {
            toastr.success('Success', "Added calendar: " + resp.summary);
            $("#calendars").append(
                $('<option />')
                    .attr('value', resp.id)
                    .html(resp.summary)
            );
            $("#calendars").trigger("chosen:updated");
        } else {
            toastr.warning('Something may have gone wrong.');
        }
    });
}

function createEvent(resource, calendarId) {
    var event = {
        'summary': resource.title,
        'location': resource.location,
        'description': resource.description,
        'start': {
            'dateTime': resource.start,
            'timeZone': 'Europe/Belgrade'
        },
        'end': {
            'dateTime': resource.end,
            'timeZone': 'Europe/Belgrade'
        }
    };
    return gapi.client.calendar.events.insert({
        'calendarId': calendarId,
        'resource': event
    });
}

function addAll() {
    if ($("#calendars").val() == null) {
        alert("Please select a writeable calendar.")
        return;
    }
    var calendarId = $("#calendars").val();
    $.ajax({
        url: "get-events",
        data: {
            start: $("#start").val(),
            end: $("#end").val()
        },
        type: "GET",
        success: function (result) {

            var batch = gapi.client.newBatch();
            for (var i = 0; i < result.length; i++) {
                event = createEvent(result[i], calendarId);
                batch.add(event)
            }
            batch.then(function () {
                toastr.success('Success', "Added " + result.length + " events.");
            }, function () {
                toastr.warning('Something may have gone wrong.');
            })
        }
    });
}

chosen_browser_is_supported = function () {
    if (window.navigator.appName === "Microsoft Internet Explorer") {
        return document.documentMode >= 8;
    }
    if (/iP(od|hone)/i.test(window.navigator.userAgent)) {
        return false;
    }
    if (/Android/i.test(window.navigator.userAgent)) {
        if (/Mobile/i.test(window.navigator.userAgent)) {
            return false;
        }
    }
    return true;
};