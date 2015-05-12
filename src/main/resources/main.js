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
    tbody.append($("<tr/>").append(
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
        text: t.task_result_number
      })
    ));
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

var showQuery = function (queryId) {
  var jqxhr = $.getJSON('/query/all');
  //var jqxhr = $.getJSON('/result.json');
  jqxhr.done(function (doc) {
    var query = $.grep(doc.list_of_queries, function (q) {
      return q.query_id == queryId;
    })[0];
    clearQueryModal();
    populateQueryModal(query);
    $('#queryModal').modal('show');
    $('#queryModalQueryId').text(query.query_id);
    $('#queryModalQueryString').text(query.query_string);
  });
  jqxhr.fail(function (jqxhr, textStatus, error) {
    console.log("Couldn't update queries: " + textStatus + ", " + error);
  });
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
    window.console.log(doc);
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

var addQueryFromJSONDoc = function (parentElement, doc) {
  var row = $("<tr/>")
  var start_time = (new Date(doc.start_time)).toLocaleString();
  var end_time;
  if (doc.end_time == -1) {
    end_time = (new Date(doc.end_time)).toLocaleString();
  } else {
    end_time = "-";
  }
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
        text: start_time
      })[0],
      $("<td/>", {
        text: end_time
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
  //var jqxhr = $.getJSON('/result.json');
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
  var element = createWarningMessage(message);
  $("#querySubmissionMessages").append(element);
};

var clearMessages = function (element) {
  element.children().each(function (idx, elem) {
    //elem.alert('close');
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
              "Query " + doc.queryId + "created.");
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
    $('#question_input').val('Query generation not implemented yet.');
    $('#assistedQueryModal').modal('hide');
    event.preventDefault();
  });
});
