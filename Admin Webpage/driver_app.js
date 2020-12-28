// ----------------------------------------------------------
//                  ADMIN PAGE 
// ----------------------------------------------------------
// Nabeel Danish

// ---------------------------------------------------------
// Scripts for Navbar
// ---------------------------------------------------------
const navToggle = document.querySelector(".nav-toggle");
const links = document.querySelector(".links");
navToggle.addEventListener("click", function () {
	links.classList.toggle("show-links");
});

const progressText = document.getElementById("progress");

// ---------------------------------------------------------
// Actual API Connection and Processing
// ---------------------------------------------------------

const url = sessionStorage.getItem("url");

// Creating a Connection request
var connectionRequest = new XMLHttpRequest();

connectionRequest.open('GET', url + 'hello');
connectionRequest.onload = function () {
  // alert("Connection Established");
  progressText.innerHTML = "Connection Established";
}
connectionRequest.send();

var driverDataLoad = new XMLHttpRequest();

driverDataLoad.open('POST', url + 'driverRequests');
driverDataLoad.setRequestHeader('Content-Type', 'application/json');

driverDataLoad.onload = function () {
  // alert("Data loaded!");
  // console.log(this.response);
  // Begin accessing JSON data here
  var data = JSON.parse(this.response);

  if (driverDataLoad.status >= 200 && driverDataLoad.status < 400) {
    // console.log(data);
    let table = document.querySelector("table");
    let head = ["ID", "FIRST NAME", "LAST NAME", "PHONE NO","CNIC", "LICENSE NUMBER", "ACCEPT", "REJECT"];
    progressText.innerHTML = "Data Loaded!";
    generateTableHead(table, head);
    generateTable(table, data);
  } else {
    console.log('error');
  }
}

driverDataLoad.send(JSON.stringify({"USER_ID":1}));


// ---------------------------------------------------------
// Table Generation Code
// ---------------------------------------------------------

// Generating Table Heads
function generateTableHead(table, data) {
  let thead = table.createTHead();
  let row = thead.insertRow();
  for (let key of data) {
    let th = document.createElement("th");
    let text = document.createTextNode(key);
    th.appendChild(text);
    row.appendChild(th);
  }
}

// Generating Table with Buttons
var acceptButtonIDs = [];
var rejectButtonIDs = [];
var userIDS = [];

function generateTable(table, data) {

  // Adding Objects to table
  let i = 0;
  for (let element of data) {
    let row = table.insertRow();
    let cell = row.insertCell();

    // Adding Data
    var userID = element['userID'];
    var firstname = element['firstName'];
    var lastname = element['lastName'];
    var phoneNo = element['phoneNo'];
    var cnic = element['CNIC'];
    var licenseNumber = element['licenseNumber'];

    // Displaying Data
    let text = document.createTextNode(userID);
    cell.appendChild(text);
    userIDS.push(userID);

    cell = row.insertCell();
    text = document.createTextNode(firstname);
    cell.appendChild(text);

    cell = row.insertCell();
    text = document.createTextNode(lastname);
    cell.appendChild(text);

    cell = row.insertCell();
    text = document.createTextNode(phoneNo);
    cell.appendChild(text);

    cell = row.insertCell();
    text = document.createTextNode(cnic);
    cell.appendChild(text);

    cell = row.insertCell();
    text = document.createTextNode(licenseNumber);
    cell.appendChild(text);

    // Accept Button
    cell = row.insertCell();

    var buttonAccept = document.createElement("button");
    buttonAccept.id = "ACCPET" + i;
    acceptButtonIDs.push(buttonAccept.id);
    buttonAccept.value = "ACCEPT";
    buttonAccept.innerHTML = "Accept Request";

    buttonAccept.onclick = function() {
      progressText.innerHTML = "Sending Request";
      var i = parseInt(acceptButtonIDs.indexOf(this.id));
      var userID = userIDS[i];
      respondToDriverRequest(userID, 3);
    };

    cell.appendChild(buttonAccept);

    // Adding Reject Button with listener
    cell = row.insertCell();

    var buttonReject = document.createElement("button");
    buttonReject.id = "REJECT" + i;
    rejectButtonIDs.push(buttonReject.id);
    buttonReject.value = "REJECT";
    buttonReject.innerHTML = "Reject Request";

    buttonReject.onclick = function() {
      progressText.innerHTML = "Sending Request";
      var i = parseInt(acceptButtonIDs.indexOf(this.id));
      var driver_ID = userIDS.find(i);
      respondToDriverRequest(driver_ID, 2);
    };
    cell.appendChild(buttonReject);

    // Iterating
    ++i;

  } // End of for loop
}

// API Connection Methods
function respondToDriverRequest(driverID, status) {
  var sendDriverRequest = new XMLHttpRequest();

  sendDriverRequest.open('POST', url + 'respondToDriverRequest');
  sendDriverRequest.setRequestHeader('Content-Type', 'application/json');

  sendDriverRequest.onload = function() {
    var data = JSON.parse(this.response);
    if (data.STATUS == 200) {
      // alert("Sent Successful!");
      progressText.innerHTML = "Request Succesful!";
      location.reload();
    } else {
      progressText.innerHTML = "Error: Request Failed!";
    }
  }
  var json = JSON.stringify({
    "DRIVER_ID" : driverID,
    "STATUS" : status
  });
  console.log("JSON = " + json);
  sendDriverRequest.send(json);
}