# path where to save GR file
finalFilepath = "D:/BacnetSVN/R-Normalization TilingArray/GeneExprExample"
# path where all scripts are
scriptDir = "D:/BacnetSVN/R-Normalization TilingArray/FinalScripts/"
# path of the table containing the list of files to normalize
manipfile="D:/BacnetSVN/TranscriptAnalysis/tempFiles/test/targets_Dagr.txt"
# temp file
tempDir = "D:/BacnetSVN/TranscriptAnalysis/tempFiles/test"
# run the script
source(paste(scriptDir,"GeneExpressionNormalization.R",sep=""))
GeneExpressionNormalization(finalFilepath,scriptDir,manipfile,tempDir)

