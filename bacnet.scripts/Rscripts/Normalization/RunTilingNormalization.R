# path for finding CEL data
celDir = "D:/BacnetSVN/TranscriptAnalysis/RawData"
# path where to save GR file
grDir = "D:/BacnetSVN/R-Normalization TilingArray/TilingExample"
# path where all scripts are
scriptDir = "D:/BacnetSVN/R-Normalization TilingArray/FinalScripts/"
# path where the annotation file is
probeRDA = paste(scriptDir,"probeAnnoAll.rda",sep="")
# path of the table containing the list of files to normalize
filecatsDir = "D:/filecat.txt"

# temp file
tempDir = "D:/BacnetSVN/TranscriptAnalysis/tempFiles/test"
tempCELFile = paste(tempDir,"TempCELfile.rda",sep="")
tempNormFile = paste(tempDir,"TempNormfile.rda",sep="")
tempStrandFile = paste(tempDir,"1",sep="")

# run the script
source(paste(scriptDir,"TilingNormalization.R",sep=""))
TilingNormalization(filecatsDir,celDir,grDir,probeRDA,scriptDir,tempCELFile,tempNormFile,tempStrandFile)


