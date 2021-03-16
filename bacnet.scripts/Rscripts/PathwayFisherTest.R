#! /usr/bin/Rscript --vanilla

# Parse aguments
input=function(){
  x=commandArgs(trail=T)
  if(length(x) != 4){
    # count_db.sqlite
    print("Usage : ./correlation.r correlation.RData 
sample_metadata.csv type  output.pdf")
    q("yes",status=1)
  }
  return(x)
}

EnrichFisher <- function(nN,nK,nG,ng){
  
  if(ng>nG || ng>nK){
    stop("Error in EnrichFisher(nN=BackgroundGeneNum,nK=InterestedGeneNum,nG=FunctionalgeneNum,ng=AnnotatedGeneNum)! ng should not be greater than nG or nK!")
  }
  if(ng>nN){
    stop("Error in EnrichFisher(nN=BackgroundGeneNum,nK=InterestedGeneNum,nG=FunctionalgeneNum,ng=AnnotatedGeneNum) error! ng should not be greater than nN!")
  }
  if(nK>nN){
    stop("Error in EnrichFisher(nN=BackgroundGeneNum,nK=InterestedGeneNum,nG=FunctionalgeneNum,ng=AnnotatedGeneNum) error! nK should not be greater than nN!")
  }
  if(nG>nN){
    stop("Error in EnrichFisher(nN=BackgroundGeneNum,nK=InterestedGeneNum,nG=FunctionalgeneNum,ng=AnnotatedGeneNum) error! nG should not be greater than nN!")
  }
  
#nN
# The total number of genes in the background distribution.
#nK
# The number of selected/interesting genes following a cerain criteria (diff expressed, p-value, logFC)
    
#Selecting a functional category
#nG
# The total number of genes in the known functional gene set.
#ng
# The number of selected/interesting genes in a known functional gene set.
        
#------------------------------------------------------------------------------------------
#                   |    Focus genes      |     Non-Focus genes       |    Row totals     |
#-------------------|---------------------|---------------------------|-------------------|
#      Genes        |                     |                           |                   |  
#    associated     |       a = ng        |         b = nG-ng         |        nG         |
#  with a function  |                     |                           |                   |
#-----------------------------------------------------------------------------------------|
#     Genes not     |                     |                           |                   |                      
#  associated with  |     c = nK-ng       |       d=nN-nG-nK+ng       |       nN-nG       |  
#   that Function   |                     |                           |                   |  
#-----------------------------------------------------------------------------------------|
#                   |                     |                           |                   |  
#   Column totals   |        nK           |           nN-nK           |        nN         |  
#                   |                     |                           |                   |  
#------------------------------------------------------------------------------------------
#
    a=ng
    b=nG-ng
    c=nK-ng
    d=nN-nG-nK+ng
    
    
    P_value=fisher.test(matrix(c(a,b,c,d),2,2), alternative = "greater")$p.value;
    return(P_value);
}


x=input()
x=as.numeric(x)
#use X for example
pvalue=EnrichFisher(x[1],x[2],x[3],x[4])
pvalue