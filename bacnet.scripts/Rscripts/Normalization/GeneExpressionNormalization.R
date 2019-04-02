
# Chargement des packages
library(LPE)
library(affy)
library(makecdfenv)
library(limma)

GeneExpressionNormalization=function(finalFilepath,scriptDir,manipfile,tempDir){
	
	print("Read cdf package")
	# Creation du package R pour le CDF si inexistant dans BioC
	cdf="L_monocytogenes.cdf"			# cdf sur le site affy
	species="Lmo"			# Espece du cdf (MOUSE, HUMAN...)
	
	#try(make.cdf.package(cdf,cdf.path=tempDir, species="species",unlink=TRUE))
	cdf<<-make.cdf.env(cdf,cdf.path=tempDir) 
	
	# path where the annotation file is
	annotation=paste(scriptDir,"Annot Lmo with COGs.txt",sep="")
	# path where the qcontrol file is
	qcontrol=paste(scriptDir,"lmo_qcc.txt",sep="")
	
	
	
	print("Read files")
	# Chargement du fichier de manip
	targets<-readTargets(manipfile)
	
	# Chargement des fichiers Cel en Affybatch
	affybatch<-ReadAffy(filenames=targets$FileName)
	# Association du CDF à l'Affybatch
	
	affybatch@cdfName<-"cdf"
	
	
	print("RMA normalization")
	# Normalisation RMA
	exprset<-rma(affybatch)
	# Transformation de l'eset en matrice
	exprset_matrix<-exprs(exprset)
	dim(exprset_matrix)
	head(exprset_matrix)
	# Transformation de la matrice en dataframe avec une colonne probesetID
	exprset_dataframe<-data.frame(exprset_matrix, probesetID=rownames(exprset_matrix))
	dim(exprset_dataframe)
	head(exprset_dataframe)
	
	# quality control
	qcc<-read.delim(qcontrol, header = TRUE)
	dimnames(qcc)[[2]]
	exprset_dataframe<-exprset_dataframe[!exprset_dataframe$probesetID %in% qcc$ProbesetID,]
	dim(exprset_dataframe)
	
	
	print("Run statistical tests")
	### Test Statistique LPE 
	## Parametre de calcul a zero TRES IMPORTANT !
	set.seed(0)
	#
	### Var Condition 1,2. POUR RMA
	## Calcul baseline error distribution par condition
	var.cond1<-baseOlig.error(exprset_dataframe[,1:2], q=0.01) #
	var.cond2<-baseOlig.error(exprset_dataframe[,4:6],q=0.01) #
	#
	lpe<-data.frame(probesetID=exprset_dataframe[,7],lpe(exprset_dataframe[,4:6],exprset_dataframe[,1:3],var.cond2,var.cond1))
	dim(lpe)
	## ajustement des pvalues par 3 methodes
	BH_lpe<-fdr.adjust(lpe,adj="BH")
	BY_lpe<-fdr.adjust(lpe,adj="BY")
	BONF_lpe<-fdr.adjust(lpe,adj="Bonferroni")
	## binding des differentes FDR
	fdr<-cbind(BH_lpe,BY_lpe,BONF_lpe) 
	colnames(fdr)<-c("FDR BH","z.real1","FDR BY","z.real2","FDR Bonf","z.real3")
	# Assemblage des FDR avec les resultats du test LPE et sauvegarde
	lpe<-merge(lpe, fdr,  by.x = "row.names", by.y = 0, all.x=TRUE, row.name=FALSE, na="NA")
	write.table(lpe, finalFilepath, sep="\t", na="NA", quote=TRUE, row.names=F)
	
	## Chargement de l'annotation
	#annot<-read.table(annotation, header = TRUE, sep = "\t", fill=TRUE)
	#dim(annot)
	#dimnames(annot)[[2]]
	#
	## Assemblage avec annotation et sauvegarde
	#lpe_annot<-merge(lpe, annot, by.x="probesetID", by.y="probeset_id", all.x=F, row.name=F, na="NA")
	#write.table(lpe_annot, "R_BH_BY_BONF_RMA_LPE_EGDe_vs_HyperRliB_annot.txt", sep="\t", na="NA", quote=TRUE, row.names=F)
	
	print("Finish R script for RMA normalization")
	
}
