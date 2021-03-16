# TODO: Add comment
# 
# Author: UIBC
###############################################################################



library(LPE)

exprset_dataframe<-read.table(file="CristelExpr.txt",header=TRUE)


print("Run statistical tests")
### Test Statistique LPE 
## Parametre de calcul a zero TRES IMPORTANT !
set.seed(0)
#
### Var Condition 1,2. POUR RMA
## Calcul baseline error distribution par condition
var.cond1<-baseOlig.error(exprset_dataframe[,2:4], q=0.01) #
var.cond2<-baseOlig.error(exprset_dataframe[,5:7],q=0.01) #
#
lpe<-data.frame(probesetID=exprset_dataframe[,1],lpe(exprset_dataframe[,5:7],exprset_dataframe[,2:4],var.cond2,var.cond1))
#dim(lpe)
### ajustement des pvalues par 3 methodes
#BH_lpe<-fdr.adjust(lpe,adj="BH")
#BY_lpe<-fdr.adjust(lpe,adj="BY")
#BONF_lpe<-fdr.adjust(lpe,adj="Bonferroni")
### binding des differentes FDR
#fdr<-cbind(BH_lpe,BY_lpe,BONF_lpe) 
#colnames(fdr)<-c("FDR BH","z.real1","FDR BY","z.real2","FDR Bonf","z.real3")
## Assemblage des FDR avec les resultats du test LPE et sauvegarde
#lpe<-merge(lpe, fdr,  by.x = "row.names", by.y = 0, all.x=TRUE, row.name=FALSE, na="NA")
write.table(lpe, "CristelLPE.txt", sep="\t", na="NA", quote=TRUE, row.names=F)
