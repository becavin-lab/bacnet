# TODO: Add comment
# 
# Author: Chris
###############################################################################

## set settings for proxy
setInternet2(TRUE)


source("https://bioconductor.org/biocLite.R")
biocLite()
biocLite("LPE")
biocLite("makecdfenv")
biocLite("davidTiling")  #Better to do it directly with R because it is 500Mo
biocLite("tilingArray")