## --------------------------------------------------
## Create the data set davidTiling
## --------------------------------------------------
## --------------------------------------------------
## Modified by Mathieu for as a function to be triggered by the root controller script
## Parameters:
##	- celdir
##	- filenames and corresponding categories
##	- Experiment (segmentation object)
## returns the values matrix

makeOneCel=function(celFileName,nucleicName,tempCELFile){


adfData = data.frame(
    filename = celFileName,
	nucleicAcid = nucleicName)
rownames(adfData) = adfData$filename

levels = paste(unlist(levels(as.factor(as.character(celFileName)))),collapse=",")

pd = new("AnnotatedDataFrame",data=adfData,
  varMetadata = data.frame(
    labelDescription = I(c(filename="Name of the .CEL file",
      nucleicAcid = paste("What is the sample? A factor with levels:", levels)))
	  )
	)
ed = new("MIAME", name="Christophe",
    lab="Listeria",
    contact="Christophe",
    title="Listeria"
	)


##if(!dir.create(dir)){
##	stop("similar experiment name already registered")
##}

values = readCel2eSet(adf=pd, path=celDir, rotated=TRUE, experimentData=ed)

#save as r data archive 
save(values, file=tempCELFile, compress=TRUE)

return(values)
}