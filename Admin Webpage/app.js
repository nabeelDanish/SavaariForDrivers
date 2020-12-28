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


// ---------------------------------------------------------
// Actual API Connection and Processing
// ---------------------------------------------------------

const url = 'https://70b29666419e.ngrok.io/';

// Creating a Connection request
var connectionRequest = new XMLHttpRequest();

connectionRequest.open('GET', url + 'hello');
connectionRequest.onload = function () {
	alert("Connection Established");
}
connectionRequest.send();

var vehicleDataLoad = new XMLHttpRequest();

vehicleDataLoad.open('POST', url + 'vehicleRequests');
vehicleDataLoad.setRequestHeader('Content-Type', 'application/json');

vehicleDataLoad.onload = function () {
  alert("Data loaded!");
  // Begin accessing JSON data here
  var data = JSON.parse(this.response);

  if (vehicleDataLoad.status >= 200 && vehicleDataLoad.status < 400) {
    console.log(data);
    let table = document.querySelector("table");
    let head = Object.keys(data[0]);
    generateTableHead(table, head);
    generateTable(table, data);
  } else {
    console.log('error');
  }
}

vehicleDataLoad.send(JSON.stringify({"USER_ID":1}));


// ---------------------------------------------------------
// Table Generation Code
// ---------------------------------------------------------
function generateTableHead(table, data) {
  let thead = table.createTHead();
  let row = thead.insertRow();
  for (let key of data) {
    let th = document.createElement("th");
    let text = document.createTextNode(key);
    th.appendChild(text);
    row.appendChild(th);
  }
  let th = document.createElement("th");
  let text = document.createElement("RIDE TYPE");
  th.appendChild(text);
  row.appendChild(th);
}

function generateTable(table, data) {

  // Adding Objects to table
  for (let element of data) {
    let row = table.insertRow();
    for (key in element) {
      let cell = row.insertCell();
      let text = document.createTextNode(element[key]);
      cell.appendChild(text);
    }

    // Adding a Button to the cell for responding to request
    let cell = row.insertCell();
    var button = document.createElement("button");
    button.value= "RESPOND";
    button.innerHTML = "Respond";
    cell.appendChild(button);
  }
}