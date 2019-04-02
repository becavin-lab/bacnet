

library("affy")
library("tilingArray")
library("davidTiling")
library("vsn")


TilingNormalization=function(filecatsDir,celDir,grDir,probeRDA,scriptDir,tempCELFile,tempNormFile,tempStrandFile){

	ref_idx=1:3
	cond1_idx <- c(4,5,6)
	cond2_idx <- c(7,8,9)
	chr=1
	strand=c("+","-")
	nbps=750
	step=11
	seg1=FALSE
	seg2=FALSE
	subsample=1e5
	
	
	# read list of data = filecats
	filecats = read.table(filecatsDir)
	cond1_cat = levels(as.factor(as.character(filecats[cond1_idx,2])))
	
	# load annotation
	load(probeRDA)
	
	#Step1: creating .CELS data object
	print("Loading .CEL files")
	source(paste(scriptDir,"makeCels.R",sep=""))
	lmoCels=makeCels(celDir,grDir,filecats,tempCELFile)
	
	
	#Step2: launching normalization
	print("Launch Normalization")
	xn=normalizeData(probeAnno=probeAnno,expData=lmoCels,ref_idx=ref_idx,subsample=subsample,scriptDir=scriptDir)
	save(xn,file=tempNormFile,compress=TRUE)
	
	#xn=lmoCels
	#Step3: run segmentation to add annotation to the file (don't know better way for the moment)
	print("Launch segmentation to add annotation")
	
	
	# ????????????????????????????????
	y=xn[,xn$nucleicAcid==cond1_cat] 
	
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
		
		#get segmentation object: s
		segObjFile = paste(tempStrandFile,strand[j], "rda", sep = ".")
		filename = paste(grDir,"/",cond1_cat,".",strand[j],".gr", sep="")
		print(filename)
		print(paste("seqqqq ",segObjFile))
		load(segObjFile)
		# c = column ; which = condition ; s@flag = attribu flag de s
		ord  = c(which(s@flag!=0), which(s@flag==0))
		# cbin = fusion de columns
		gr = cbind(format(s@x[ord],scientific=FALSE,trim=TRUE),format(s@y[ord],scientific=FALSE,trim=TRUE))
		write.table(gr,row.names=FALSE,col.names=FALSE,sep="\t",quote=FALSE,file=filename)
		#file.copy(from=filename,to=grDir)
	}
	
	print("Finish R script for Tiling normalization")
	
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
