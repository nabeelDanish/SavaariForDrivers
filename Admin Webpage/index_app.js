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

const url = sessionStorage.getItem("url");

// Creating a Connection request
var connectionRequest = new XMLHttpRequest();

connectionRequest.open('GET', url + 'hello');
connectionRequest.onload = function () {
	alert("Connection Established");
}
connectionRequest.send();