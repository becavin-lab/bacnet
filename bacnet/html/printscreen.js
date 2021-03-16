/**
 * Function for printscreen BACNET elements
 */
function printscreen(fileName, divId) {
	console.log("Printscreen "+divId+" in "+fileName)
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
			link.setAttribute("download", fileName +".jpeg");
			link.click();
		});
	console.log("Screen printed")
	});

}

html2canvas(document.querySelector("#capture")).then(canvas => {
    document.body.appendChild(canvas)
});


var fileName = "_FileName"
//var divId = "_DivName"
var divId = "body"
printscreen(fileName, divId)