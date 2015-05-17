"use strict";

var clearQueryModal = function () {
  clearQueryModalMessages();
  $('#queryModalTable tbody').html("");
  $('#queryModalResults').html("");
  $('#queryModalQueryString').html("");
};

var populateQueryModal = function (query) {
  var tbody = $("#queryModalTable tbody");
  query.list_of_tasks.forEach(function (t) {
    var resultsRow = $("<tr/>", {
      class: 'hidden resultsRow'
    });
    var resultsCell = $("<td/>", {
      colspan: 7
    }).appendTo(resultsRow);
    var resultsList = $("<ul/>").appendTo(resultsCell);
    t.detailed_results.forEach(function (assignment) {
      assignment.forEach(function (response) {
        response.split("\r").forEach(function (line) {
          if (line != "") {
            resultsList.append($("<li/>", {
              text: line
            }));
          }
        });
      });
    });
    var showResultsButton = $("<button/>", {
      class: 'btn btn-sm',
      type: 'button',
      text: 'Show results'
    }).on('click', function () {
      resultsRow.toggleClass('hidden');
    });
    if (t.task_results_number == "" || t.task_results_number == "0") {
      showResultsButton.addClass('hidden');
    }
    tbody.append(
      $("<tr/>").append(
        $("<td/>", {
          text: t.task_id
        }),
        $("<td/>", {
          text: t.task_status
        }),
        $("<td/>", {
          text: t.task_operator
        }),
        $("<td/>", {
          text: t.number_of_hits
        }),
        $("<td/>", {
          text: t.finished_hits
        }),
        $("<td/>", {
          text: t.task_results_number
        }),
        $("<td/>").append(showResultsButton)),
      resultsRow
    );
  });
  if (!query.detailed_query_results || query.detailed_query_results.length == 0) {
    $('#queryModalResults').append($("<em/>", {
      text: "No results yet, be patient."
    }));
  } else {
    var ul = $('#queryModalResults').append($('<ul/>'));
    query.detailed_query_results.forEach(function (r) {
      ul.append($('<li/>', {
        text: r
      }));
    });
  }
  $('#queryModalLabel').text("Query " + query.query_id);
};

var createQueryFromAssistedForm = function(){
  var select=$('input[name="Select"]').val();
  var from=$('input[name="From"]').val();
  var where=$('input[name="Where"]').val();
  var joinon=$('input[name="JoinOn"]').val();
  var select2=$('input[name="Select2"]').val();
  var from2=$('input[name="From2"]').val();
  var where2=$('input[name="Where2"]').val();
  var generatestring;
  if(joinon.length!=0) {
    generatestring =
      " (SELECT (" + select +
      ") FROM [" + from +
      "]) WHERE [" + where +
      "] JOIN (" + joinon +
      ") (SELECT (" + select2 +
      ") FROM [" + from2 +
      "]) WHERE ["+ where2+
      "]";
  } else {
   generatestring=
   " (SELECT (" + select +
   ") FROM [" + from +
   "]) WHERE [" + where+
   "]";
  }
  $('#question_input').val(generatestring);
};

var refreshQueryModal = function () {
  var queryId = $('#queryModalQueryId').text();
  var jqxhr = $.getJSON('/query/all');
  jqxhr.done(function (doc) {
    var query = $.grep(doc.list_of_queries, function (q) {
      return q.query_id == queryId;
    })[0];
    clearQueryModal();
    populateQueryModal(query);
    $('#queryModalQueryId').text(query.query_id);
    $('#queryModalQueryString').text(query.query_string);
  });
  jqxhr.fail(function (jqxhr, textStatus, error) {
    console.log("Couldn't show query: " + textStatus + ", " + error);
  });
};

var showQuery = function (queryId) {
  $('#queryModalQueryId').text(queryId);
  refreshQueryModal();
  $('#queryModal').modal('show');
};

var abortQueryAjax = function (queryId) {
  window.console.log("Aborting query " + queryId);
  var jqxhr = $.ajax({
    type: "POST",
    url: "/query/abort",
    data: {'query': queryId},
    dataType: "json"
  });
  return jqxhr;
};

var abortShownQuery = function () {
  clearQueryModalMessages();
  var queryId = $('#queryModalQueryId').text();
  var jqxhr = abortQueryAjax(queryId);
  jqxhr.done(function (doc) {
    if (doc.success) {
      update_queries();
      $('#queryModal').modal('hide');
    } else {
      clearQueryModalMessages();
      addQueryModalMessage(
        createWarningMessage("Couldn't abort query: " + doc.message)
      );
    }
  });
  jqxhr.fail(function (jqxhr, textStatus, error) {
    console.log("Couldn't update queries: " + textStatus + ", " + error);
  });
};

var timeToText = function (t) {
  var s;
  if (t == -1) {
    s = "-";
  } else {
    s = (new Date(t)).toLocaleString();
  }
  return s;
};

var addQueryFromJSONDoc = function (parentElement, doc) {
  var row = $("<tr/>")
  var content = [
      $("<td/>", {
        text: doc.query_id
      })[0],
      $("<td/>", {
        text: doc.query_status
      })[0],
      $("<td/>", {
        text: doc.query_results_number
      })[0],
      $("<td/>", {
        text: timeToText(doc.start_time)
      })[0],
      $("<td/>", {
        text: timeToText(doc.end_time)
      })[0]
  ];
  $(content).on('click', function () {
    showQuery(doc.query_id);
  });
  row.append(content);
  parentElement.append(row);
};

var update_queries = function () {
  var jqxhr = $.getJSON('/query/all');
  jqxhr.done(function (doc) {
    var tbody = $("#queriesTable tbody");
    tbody.html("");
    doc.list_of_queries.forEach(function (e) {
      addQueryFromJSONDoc(tbody, e);
    });
  });
  jqxhr.fail(function (jqxhr, textStatus, error) {
    console.log("Couldn't update queries: " + textStatus + ", " + error);
  });
};

var addMessage = function (element, message) {
  var div = $('<div/>', {
    'class': 'alert alert-success alert-dismissable fade in',
    'role': 'alert',
    'text': message
  });
  element.append(div);
};

var createSuccessMessage = function (message) {
  var div = $('<div/>', {
    'class': 'alert alert-success alert-dismissable fade in',
    'role': 'alert',
    'text': message
  });
  return div;
};

var createWarningMessage = function (message) {
  var div = $('<div/>', {
    'class': 'alert alert-warning alert-dismissable fade in',
    'role': 'alert',
    'text': message
  });
  return div;
};

var addQueryModalMessage = function (element) {
  $("#queryModalMessages").append(element);
};

var addQuerySubmissionSuccessMessage = function (message) {
  var element = createSuccessMessage(message);
  $("#querySubmissionMessages").append(element);
};

var addQuerySubmissionAlertMessage = function (message) {
  var div = createWarningMessage("Creation of query failed:");
  div.append($('<pre/>').text(message));
  $("#querySubmissionMessages").append(div);
};

var clearMessages = function (element) {
  element.children().each(function (idx, elem) {
    $(elem).detach();
  });
};

var clearQuerySubmissionMessages = function () {
  clearMessages($('#querySubmissionMessages'));
};

var clearQueryModalMessages = function () {
  clearMessages($('#queryModalMessages'));
};

$(document).ready(function() {
  $('#refreshQuerriesButton').on('click', function () {
    update_queries();
  });

  $('#assistedQueryButton').on('click', function () {
    $('#assistedQueryModal').modal('show');
  });

  $('#queryModalAbortButton').on('click', function () {
    clearQueryModalMessages();
    abortShownQuery();
  });

  $('#queryModalRefreshButton').on('click', function () {
    refreshQueryModal();
  });

  update_queries();

  $("#queryForm").on('submit', function(event){
      window.console.log("Submitting query: " + $("#question_input").val());
      clearQuerySubmissionMessages();
      var jqxhr = $.ajax({
        type: "POST",
        url: "/query/new",
        data: {question: $("#question_input").val()},
        dataType: "json"
      });
      jqxhr.done(function (doc) {
        window.console.log(doc);
        if (doc.success == true) {
          window.console.log("Query successfully created: " + doc.queryId);
          addQuerySubmissionSuccessMessage(
              "Query " + doc.queryId + " created.");
          update_queries();
          $('#question_input').val('');
        } else {
          window.console.log("Creation of query failed: " + doc.message);
          addQuerySubmissionAlertMessage(doc.message);
        }
      });
      jqxhr.always(function (doc) {
        window.console.log("Response:");
        window.console.log(doc);
      });
      event.preventDefault();
  });

  $('#assistedQueryForm').on('submit', function(event) {
    window.console.log("Generating query string from assisted form");
    createQueryFromAssistedForm();
    $('#assistedQueryModal').modal('hide');
    event.preventDefault();
  });

  $('#autorefreshQueriesCheckbox').on('change', function () {
    window.console.log("checkbox changed");
    queriesAutorefresh.setAutorefreshFromCheckbox();
  });
  queriesAutorefresh.setAutorefreshFromCheckbox();
});

var queriesAutorefresh = new function () {
  this.autorefresh = false;
  this.intervalID = undefined;
  this.refreshInterval = 15000;

  this.autorefreshFunc = function () {
    update_queries();
  };

  this.setAutorefresh = function (bool) {
    if (bool && (!this.autorefresh)) {
      window.console.log("Autorefresh enabled");
      update_queries();
      this.intervalID = window.setInterval(this.autorefreshFunc, this.refreshInterval);
    } else if ((!bool) && (this.intervalID != undefined)) {
      window.console.log("Autorefresh disabled");
      window.clearTimeout(this.intervalID);
    }
    this.autorefresh = bool;
  };

  this.setAutorefreshFromCheckbox = function () {
    this.setAutorefresh($('#autorefreshQueriesCheckbox').prop('checked'));
  };
};
