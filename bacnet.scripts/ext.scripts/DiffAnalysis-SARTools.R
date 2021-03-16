################################################################################
### R script to compare several conditions with the SARTools and edgeR packages
### Hugo Varet
### March 20th, 2018
### designed to be executed with SARTools 1.6.9
################################################################################

################################################################################
###                parameters: to be modified by the user                    ###
################################################################################
rm(list=ls())                                        # remove all the objects from the R session

workDir <- "C:/Users/ipmc/OneDrive/Yersiniomics/RawData/NGS/"      # working directory for the R session

projectName <- "E-GEOD-66516"                         # name of the project
author <- "Christophe Bécavin"                                # author of the statistical analysis/report

targetFile <- "target_E-GEOD-66516.txt"                           # path to the design/target file
rawDir <- "FCount"                                      # path to the directory containing raw counts files
featuresToRemove <- c("alignment_not_unique",        # names of the features to be removed
                      "ambiguous", "no_feature",     # (specific HTSeq-count information and rRNA for example)
                      "not_aligned", "too_low_aQual")# NULL if no feature to remove

varInt <- "BioCond"                                    # factor of interest
condRef <- "22EnterocoliticaY11IHS20002015cDNA"                                      # reference biological condition
batch <- NULL                                        # blocking factor: NULL (default) or "batch" for example

alpha <- 0.05                                        # threshold of statistical significance
pAdjustMethod <- "BH"                                # p-value adjustment method: "BH" (default) or "BY"

cpmCutoff <- 1                                       # counts-per-million cut-off to filter low counts
gene.selection <- "pairwise"                         # selection of the features in MDSPlot
normalizationMethod <- "TMM"                         # normalization method: "TMM" (default), "RLE" (DESeq) or "upperquartile"

colors <- c("dodgerblue","firebrick1",               # vector of colors of each biological condition on the plots
            "MediumVioletRed","SpringGreen")

forceCairoGraph <- FALSE



################################################################################
###                             running script                               ###
################################################################################
setwd(workDir)
library(SARTools)
if (forceCairoGraph) options(bitmapType="cairo")
library(corrplot)

# checking parameters
checkParameters.edgeR(projectName=projectName,author=author,targetFile=targetFile,
                      rawDir=rawDir,featuresToRemove=featuresToRemove,varInt=varInt,
                      condRef=condRef,batch=batch,alpha=alpha,pAdjustMethod=pAdjustMethod,
                      cpmCutoff=cpmCutoff,gene.selection=gene.selection,
                      normalizationMethod=normalizationMethod,colors=colors)

# loading target file
target <- loadTargetFile(targetFile=targetFile, varInt=varInt, condRef=condRef, batch=batch)

# loading counts
counts <- loadCountData(target=target, rawDir=rawDir, featuresToRemove=featuresToRemove)

# description plots
majSequences <- descriptionPlots(counts=counts, group=target[,varInt], col=colors)

# edgeR analysis
out.edgeR <- run.edgeR(counts=counts, target=target, varInt=varInt, condRef=condRef,
                       batch=batch, cpmCutoff=cpmCutoff, normalizationMethod=normalizationMethod,
                       pAdjustMethod=pAdjustMethod)

# MDS + clustering
exploreCounts(object=out.edgeR$dge, group=target[,varInt], gene.selection=gene.selection, col=colors)

# summary of the analysis (boxplots, dispersions, export table, nDiffTotal, histograms, MA plot)
summaryResults <- summarizeResults.edgeR(out.edgeR, group=target[,varInt], counts=counts, alpha=alpha, col=colors)

# save image of the R session
save.image(file=paste0(projectName, ".RData"))

# generating HTML report
writeReport.edgeR(target=target, counts=counts, out.edgeR=out.edgeR, summaryResults=summaryResults,
                  majSequences=majSequences, workDir=workDir, projectName=projectName, author=author,
                  targetFile=targetFile, rawDir=rawDir, featuresToRemove=featuresToRemove, varInt=varInt,
                  condRef=condRef, batch=batch, alpha=alpha, pAdjustMethod=pAdjustMethod, cpmCutoff=cpmCutoff,
                  colors=colors, gene.selection=gene.selection, normalizationMethod=normalizationMethod)



# Generate all Yersiniomics diff tables
for(i in 1:length(target$BioCond)){
  bioCond = target$BioCond[i]
  bioCondName = target$Yersi[i]
  diffFiles <- paste("tables/",levels(target$BioCond),"vs",bioCond,".complete.txt",sep="")
  diffNames <- paste("../../NormData/NGS/",levels(target$Yersi),"_vs_",bioCondName,".rnaseq",sep="")
  
  for(i in 1:length(diffFiles)){
    diffFile = diffFiles[i]
    diffName = diffNames[i]
    if(file.exists(diffFile)){
      print(diffName)
      print(i)
      complete.file <- read.delim(file=diffFile, sep = "\t", stringsAsFactors = FALSE, header =  TRUE)
      yersi.file = complete.file[,c("Id","log2FoldChange","padj")]
      colnames(yersi.file) = c("gene","LOGFC","p-value")
      write.table(yersi.file, file=diffName, quote=FALSE, sep = ",", row.names = FALSE)
    }
  }
}


# create count files for Yersiniomics
diffFiles <- paste("tables/",levels(target$BioCond),"vs",condRef,".complete.txt",sep="")
diffFile = diffFiles[2]
complete.file <- read.delim(file=diffFile, sep = "\t", stringsAsFactors = FALSE, header =  TRUE)
rownames(complete.file) = complete.file$Id
biocondList = colnames(complete.file)[grep("norm",colnames(complete.file))]

for(colname in biocondList){
  dataName = sub("norm.","",colname)
  print(dataName)
  yersi.file = complete.file[,c("Id",colname)]
  colnames(yersi.file) = c("ID","VALUE")
  write.table(yersi.file, file=paste("../../NormData/NGS/",dataName,".rnaseq",sep=""), quote=FALSE, sep = ",", row.names = FALSE)
}

