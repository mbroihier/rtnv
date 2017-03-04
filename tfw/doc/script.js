var myRequest;

if (window.XMLHttpRequest) {  // does it exist? we're in Firefox, Safari etc.
    myRequest = new XMLHttpRequest();
} else if (window.ActiveXObject) { // if not, we're in IE
    myRequest = new ActiveXObject("Microsoft.XMLHTTP");
}

myRequest.onreadystatechange = function(){
     console.log(myRequest.readyState);
     if (myRequest.readyState === 4) {
        var entries = myRequest.responseText.split("ID:");
        var headings = ["Test Start Time:","Log data:","Results:","Test End Time:","Tester:"];
	var tableElement = document.createElement("table");
        var column1Setting = document.createElement("col");
        var testCaseTitles = [];
        var failedTestCaseTitles = [];

        column1Setting.setAttribute("style","width:15%");
        tableElement.appendChild(column1Setting);
        var column2Setting = document.createElement("col");
        column2Setting.setAttribute("style","width:85%");
        tableElement.appendChild(column2Setting);
	entries.shift(); 
	var expectedProgressInfo = entries.shift(); // remove null entry and EXPECTED PROGRESS entries
        var dateLines = expectedProgressInfo.split("\n");
        dateLines.shift();
        var eLine = [];
        var aLine = [];
        var actualExecuted = 0;
	for (var index in dateLines) {
          if (dateLines[index] == "") continue;
          eLine[index] = [new Date(dateLines[index].split(" ")[0]).getTime(), dateLines[index].split(" ")[1] ];
          console.log(" eLine["+index+"] = " + eLine[index] + " dateLines: " + dateLines[index]);
	}
        for ( var index in entries ) {
          if (entries[index] =="") { 
	    console.log("Nothing at index: "+index);
            continue;
	  } else {
	    console.log("current index defined");
	  }
	  var firstRowElement = document.createElement("tr");
          var testId = (entries[index].split("\n")[0]).trim();
          var remainder = String(entries[index].split(testId+"\n")[1]);
          var testIdText = document.createTextNode(testId);
          var testCaseTitle = document.createElement("th");

          var addToList = true; // this is a unique test case
          for (var checkTitle in testCaseTitles) {
            console.log("testId is: "+testId+" checkTitle is: "+testCaseTitles[checkTitle]);
            if (testId == testCaseTitles[checkTitle]) {
	      addToList = false;
              break;
	    }
	  }
          if (addToList) {
            testCaseTitles.push(testId);
            console.log("adding " + testId + " to list");
          }

          testCaseTitle.appendChild(testIdText);
          firstRowElement.appendChild(testCaseTitle);
          var filler = document.createElement("th");
          firstRowElement.appendChild(filler);
          tableElement.appendChild(firstRowElement);
          console.log("remainder: "+remainder);
	  for (var heading in headings) {
	    var rowElement = document.createElement("tr");
            var indexStart = remainder.indexOf(headings[heading]) + headings[heading].length;
            var indexEnd = remainder.indexOf(headings[parseInt(heading)+1]);
            var sectionContent = remainder.slice(indexStart,indexEnd);
            var labelCellElement = document.createElement("td");
	    var labelText = document.createTextNode(headings[heading].slice(0,-1));
	    labelCellElement.appendChild(labelText);
	    rowElement.appendChild(labelCellElement);
            console.log(sectionContent);
	    var contextText;
	    contentText = document.createTextNode(sectionContent);
	    var contentCellElement;
            if (sectionContent.indexOf("FAILED") >= 0) {
              contentCellElement = document.createElement("td");
	      contentCellElement.setAttribute("class","failed");
              var addToList = true;
              for (var checkTitle in failedTestCaseTitles) {
                if (testId == failedTestCaseTitles[checkTitle]) {
	          addToList = false;
                  break;
	        }
	      }
              if (addToList) {
                failedTestCaseTitles.push(testId);
                console.log("adding " + testId + " to failed list");
              }
	    } else {
              contentCellElement = document.createElement("td");
              var removeIndex = -1;
              if (headings[heading] == "Results:") {
                for (var checkTitle in failedTestCaseTitles) {
                  if (testId == failedTestCaseTitles[checkTitle]) {
	            removeIndex = checkTitle;
                    break;
	          }
	        }
                if (removeIndex >= 0) {
                  failedTestCaseTitles.splice(removeIndex,1)
                  console.log("removing a failed test case " + testId + " because a more recent run passed");
                }
	      }
	    }
            if (addToList && headings[heading] == "Test End Time:") { // if we have increased the number of unique new tests that have been run
              var regularExpression = / *(\d+\/\d+\/\d+) *.*/;
              var date = sectionContent.match(regularExpression);
              console.log("date: " + date + ", original: " + sectionContent + ", -- " + date[1]);
	      actualExecuted++;
              if (aLine.length > 0 && aLine[aLine.length - 1][0] == new Date(date[1]).getTime()) {
                aLine[aLine.length - 1] = [new Date(date[1]).getTime(), actualExecuted];
	      } else {
                aLine[aLine.length] = [new Date(date[1]).getTime(), actualExecuted];
	      }
            }
            contentCellElement.appendChild(contentText);
	    rowElement.appendChild(contentCellElement);
	    tableElement.appendChild(rowElement);
	  }
	}
        document.getElementById("detailedContent").appendChild(tableElement);
        var summaryParagraph = document.createElement("p");
        if (failedTestCaseTitles.length == 0) {
          var summaryText = document.createTextNode("" + testCaseTitles.length + " unique tests have been executed and there are no currently failing test cases.");
          summaryParagraph.appendChild(summaryText);
          document.getElementById("summaryContent").appendChild(summaryParagraph);
	} else {
          var summaryText = document.createTextNode("" + testCaseTitles.length + " unique tests have been executed and " + failedTestCaseTitles.length + " have failed - see log details below");
          summaryParagraph.appendChild(summaryText);
          document.getElementById("summaryContent").appendChild(summaryParagraph);
	}
        var options = { //set plot options
	  series: { 
	    points: {show: true},
            lines: {show: true}
          },
	  xaxis: {
	    show: true,
            mode: "time",
	    timeformat: "%m/%d/%y"
          },
          legend: {
	    show: true,
	    position: "se",
            container: "#legend"
	  }
        }; 
        // plot expected vs actual tests run
        $.plot($("#placeholder"),[{data:aLine,label:"Actual Number Of Test Cases Run"},{data:eLine,label:"Expected Number Of Test Cases Run"}],options);
        for (var index in eLine) {
          console.log("eline: " + index + " " + eLine[index]);
        }
        for (var index in aLine) {
          console.log("aline: " + index + " " + aLine[index]);
        }

     }
};

// open and send it
myRequest.open('GET', 'TEST.LOG', true);
// any parameters?
myRequest.send(null);

//....
