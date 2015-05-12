"use strict";

var clearQueryModal = function () {
  $('#queryModalTable tbody').html("");
  $('#queryModalResults').html("");
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
};

var showQuery  = function (queryId) {
  var jqxhr = $.getJSON('/query/all');
  //var jqxhr = $.getJSON('/result.json');
  jqxhr.done(function (doc) {
    var query = $.grep(doc.list_of_queries, function (q) {
      return q.query_id == queryId;
    })[0];
    clearQueryModal();
    populateQueryModal(query);
    $('#queryModal').modal('show');
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

var addQuerySubmissionAlertMessage = function (msg) {
  var element = $('<div/>', {
    'class': 'alert alert-warning alert-dismissable fade in',
    'role': 'alert',
    'text': msg
  });
  $("#querySubmissionMessages").append(element);
};

var addQuerySubmissionSuccessMessage = function (msg) {
  var element = $('<div/>', {
    'class': 'alert alert-success alert-dismissable fade in',
    'role': 'alert',
    'text': msg
  });
  $("#querySubmissionMessages").append(element);
};

var clearQuerySubmissionMessages = function () {
  $("#querySubmissionMessages").children().each(function (idx, elem) {
    //elem.alert('close');
    $(elem).detach();
  });
};

$(document).ready(function() {
  $('#testButton').on('click', function () {
    $('#queryModal').modal('show');
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
});
