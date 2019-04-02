

library("affy")
library("tilingArray")
library("davidTiling")
library("vsn")


TilingNormalization=function(){

	library("affy")
	library("tilingArray")
	library("davidTiling")
	
	chr=1
	strand=c("+","-")
	nbps=750
	step=11
	seg1=FALSE
	seg2=FALSE
	subsample=1e5
	
	# path for finding CEL data
	celDir = "D:/BacnetSVN/TranscriptAnalysis/RawData/"
	celFileName = "T_RNA3_e1.CEL"
	
	nucleicAcid = "Rna3"
	
	# path where to save GR file
	grDir = "D:/BacnetSVN/TranscriptAnalysis/tempFiles/"
	# path where all scripts are
	scriptDir = "D:/BacnetSVN/ListTranscript/R-scripts/Normalization/"
	# temp file
	tempDir = "D:/BacnetSVN/TranscriptAnalysis/tempFiles/"
	tempCELFile = paste(tempDir,"EGDe_270407-CEL.rda",sep="")
	tempNormFile = paste(tempDir,"EGDe_270407-Norm.rda",sep="")
	tempStrandFile = paste(tempDir,"EGDe_270407",sep="")
	
	
	# path where the annotation file is
	probeRDA = paste(scriptDir,"probeAnnoAll.rda",sep="")
	# load annotation
	load(probeRDA)
	
	
	
	#Step1: creating .CELS data object
	print("Loading .CEL files")
	source(paste(scriptDir,"makeOneCel.R",sep=""))
	xn=makeOneCel(celFileName,nucleicAcid,tempCELFile)
	save(xn,file=tempNormFile,compress=TRUE)
	
	
	#Step3: run segmentation to add annotation to the file (don't know better way for the moment)
	print("Launch segmentation to add annotation")
	
	
	y=xn
		
	# different variable for segmentation
	strands=strand 								 
	nrBasesPerSegment = 1500
	maxk = 10
	step = 7
	confint = FALSE
	confintLevel = 0.95
	useLocks = TRUE
	verbose = TRUE
	
	segObj = new.env(parent = baseenv())
	chrstrd = 	paste(rep(chr, each = length(strands)), rep(strands,times = length(chr)), sep = ".")
	
	for (j in seq(along = strand)) {
	
			
			# read annotation
			df_colnames = c("start", "end", "index", "unique")
			pa_elements = paste(chrstrd[j], df_colnames, sep = ".")
			prbs = mget(pa_elements, probeAnno)
			prbs = do.call(data.frame, prbs)
			colnames(prbs) = df_colnames
			prbs$mid = (prbs$start + prbs$end)/2
			prbs = prbs[order(prbs$mid), ]
			numna = rowSums(is.na((if (is.matrix(y)) y else exprs(y))[prbs$index, , drop = FALSE]))
			stopifnot(all(numna %in% c(0, ncol(y))))
			prbs = prbs[numna == 0, ]
			sprb = prbs[sampleStep(prbs$mid, step = step), ]
	
			#create segment
			#		nsegs = as.integer(round(sprb$end[nrow(sprb)]/nrBasesPerSegment))
			nsegs = as.integer(10)
			ychr = (if (is.matrix(y)) 
					y
				else exprs(y))[sprb$index, , drop = FALSE]
			
			s = segment(ychr, maxseg = nsegs, maxk = maxk)
			s@x = sprb$mid
			s@flag = as.integer(sprb$unique)
			s@nrSegments = nsegs
			assign(chrstrd[j], s, segObj)
			
			# save s object
			filename = file.path(paste(tempStrandFile,strand[j], "rda", sep = "."))
			save(s, file = filename)
			
			cat(" ... complete\n")
		
	}
	
	#step 4: Save into GR files
	print("creating GR files")
	for (j in seq(along = chrstrd)) {
		
		#make plus strand
		segObjFile = paste(tempStrandFile,strand[j], "rda", sep = ".")
		filename = paste(grDir,"/",cond1_cat,".",strand[j],".gr", sep="")
		print(filename)
		load(segObjFile)
		# c = column ; which = condition ; s@flag = attribu flag de s
		ord  = c(which(s@flag!=0), which(s@flag==0))
		# cbin = fusion de columns
		gr = cbind(format(s@x[ord],scientific=FALSE,trim=TRUE),format(s@y[ord],scientific=FALSE,trim=TRUE))
		write.table(gr,row.names=FALSE,col.names=FALSE,sep="\t",quote=FALSE,file=filename)
		#file.copy(from=filename,to=grDir)
	}
	
	print("Finieggfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdsh R script for Tiling normalization")
	
}

PMindex <- function(probeAnno){
	isPM = logical(length(probeAnno$probeReverse$no_feature))
	for (j in probeAnno$probeReverse)
		isPM[as.character(j) != ""] = TRUE
	which(isPM)
}

BGindex <- function(probeAnno) {
	which(probeAnno$probeReverse$no_feature == "no")
}

normalizeData = function(probeAnno,expData,ref_idx,subsample=1e5,plotFileNames,scriptDir){
	
	whPM = PMindex(probeAnno)
	whBG = BGindex(probeAnno)
	stopifnot(all(whBG %in% whPM))
	
	source(paste(scriptDir,"NormalizeByReference.R",sep=""))
	
	all_idx=seq(from=1,to=length(expData$nucleicAcid),by=1)
	x_idx=setdiff(all_idx,ref_idx)
	
	xn=NormalizeByReference(expData[,x_idx],expData[,ref_idx],pm=whPM,background=whBG,cutoffQuantile=0,subsample=subsample,plotFileNames=plotFileNames)
	
	return(xn)
}
