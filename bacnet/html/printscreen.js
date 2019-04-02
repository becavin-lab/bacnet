/**
 * Function for printscreen BACNET elements
 */
function printscreen(fileName, divId) {
	console.log("Printscreen "+divId+" in "+fileName)
	//var div = document.getElementById(divId) // not working with Eclipse.RAP
	//if(divId == "body"){
	//	var div = document.body
	//}else{
	//	var div = document.getElementById(divId)
	//}
	//console.log(div)
	html2canvas(document.body).then(
		function(canvas) {
			canvas.toBlob(function(blob) {
			var newImg = document.createElement("img"), 
			url = URL.createObjectURL(blob);
			newImg.onload = function() {
				URL.revokeObjectURL(url);
			};
			var link = document.createElement("a");
			link.setAttribute("href", url);
			link.setAttribute("download", fileName+".png");
			link.click();
		});
	console.log("Screen printed")
	});
}

var fileName = "_FileName"
//var divId = "_DivName"
var divId = "body"
printscreen(fileName, divId)