// ----------------------------------------------------------
//                  ADMIN PAGE 
// ----------------------------------------------------------
// Nabeel Danish

// ---------------------------------------------------------
// Actual API Connection and Processing
// ---------------------------------------------------------

var url = window.prompt("Copy the URL Link (make sure you include '/' at the end)");

// Creating a Connection request
var connectionRequest = new XMLHttpRequest();

connectionRequest.open('GET', url + 'hello');
connectionRequest.onload = function () {
	alert("Connection Established");
}
connectionRequest.send();

function login(form) {

	// Getting Form Data
	var username = form.uname.value;
	var password = form.psw.value;

	// Calling Login
	var loginRequest = new XMLHttpRequest();
	loginRequest.open("POST", url + 'login_admin');
	loginRequest.setRequestHeader('Content-Type', 'application/json');

	loginRequest.onload = function() {
		var data = this.response;
		console.log(data);
		if (data != null) {
			alert("Login Successful!");
			sessionStorage.setItem("url", url);
			window.location.replace("index.html");
		}
	}

	loginRequest.send(JSON.stringify({"username":username, "password":password}));
}