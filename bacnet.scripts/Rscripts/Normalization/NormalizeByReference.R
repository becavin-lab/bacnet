## modified by Mathieu on 03/10/2007 for Listeria in order to resize subsample from 2e5 (Scerevisae) to 1e5
## Indeed Listeria analyses requires that "'subsample' must be a scalar with values between 0 and 151736."

NormalizeByReference = function(x, reference, pm, background, refSig, nrStrata=10,
  cutoffQuantile=0.05, plotFileNames, verbose=FALSE, subsample=1e5) {

  ##--------------- process and check the input arguments ---------------------------

  ## process 'x'
  if(!is(x, "ExpressionSet"))
    stop("'x' must be an object of class 'ExpressionSet'")
  n = nrow(exprs(x))
  d = ncol(exprs(x))
  if(d<1)
    stop("There is nothing to normalize in 'x'.")

  ## process 'pm' and 'background'
  checkindex = function(v) {
    nm = deparse(substitute(v))
    if(any(is.na(v)))
      stop(sprintf("'%s' must not contain NA.", nm))
    if(is.logical(v)) {
      if(length(v)!=n)
        stop(sprintf("%d must be logical vector of length %d.", nm, n))
      v = which(v)
    } else {
      if(!(is.integer(v)&&(length(v)>1)&&(min(v)>=1)&&(max(v)<=n)))
        stop(sprintf("'%s' must be an integer vector with values between 1 and %d.", nm, n))
      if(any(duplicated(v)))
        stop(sprintf("'%s' must not contain duplicated indices.", nm))
    }
    return(v)
  }

  pm = checkindex(pm)
  background = checkindex(background)
	
  mtb = match(background, pm)
  if(any(is.na(mtb)))
    stop("'background' must be a subset of 'pm'.")
  
  ## process 'reference' and 'refSig'
  if(!xor(missing(refSig), missing(reference)))
    stop("Please specify either the argument 'refSig' or 'reference' (but not both, or none of them).")
  
  if(missing(refSig)) {
    if(!is(reference, "eSet"))
      stop("'reference' must be an object of class 'eSet'")
    if(n!=nrow(exprs(reference)))
     stop("'x' and 'reference' must have 'exprs' elements in their 'assayData' slot with the same number of rows.")
    refSig = rowMeans(log2(exprs(reference)[pm,,drop=FALSE]))
  } else {
    if(!(is.numeric(refSig)&&(length(refSig)==length(pm))))
      stop("'refSig' must be a numeric vector whose length is the same as that of 'pm'.")
  }

  ##--------------- now the number crucnching starts ---------------------------
   ## quantiles of the reference intensities, to group probes into
  ## strata for the background estimations
  quants    = quantile(refSig, probs=seq(0, 1, length=nrStrata+1))
  quants[1] = quants[1]-1

  ## reference signal for the background features
  refSigBg = refSig[mtb]

  ## strata is now a factor with 'nrStrata' levels and of same length as 'background'
  strata     = cut(refSigBg, quants)

  if(any(table(strata) < 5e3))
    warning("'some strata of background contain fewer than 5000 features, are you sure this is alright?")
  
  xbg = (quants[-1]+quants[-length(quants)])/2  ## midpoint between quantiles
  ybg = matrix(as.numeric(NA), nrow=nrStrata, ncol=d)
  bgfun  = vector(mode="list", length=d)

  ## interpolate  
  for(j in 1:d) {
    ybg[, j] = tapply(log(exprs(x)[background, j], 2), strata, genefilter::shorth, tie.action="min")
    bgfun[[j]] = approxfun(xbg, ybg[,j], rule=2)
  }

  ## diagnostic plot of bgfun:
  ## if(!missing(plotFileNames)) {
  ##  if(length(plotFileNames)!=d)
  ##    stop("Please supply as many elements of 'plotFileNames' as there are arrays in 'x'")
  ##  rgx = range(refSigBg)
  ##  px  = seq(rgx[1], rgx[2], length=120)
  ##  for(j in 1:d) {
  ##    pdf(file=plotFileNames[j], width=8, height=6)
  ##    smoothScatter(refSigBg, log(exprs(x)[background, j],2),
  ##          xlab = "Reference intensity",
  ##          ylab = "Background intensity", nrpoints=0)
  ##    lines(px, bgfun[[j]](px), col="darkred")
  ##    dev.off()
  ##  }
  ##}
  
  ## apply the background and the scaling
  if(verbose) cat("Applying background and scaling\n")
  xn = matrix(as.numeric(NA), nrow=length(pm), ncol=d)
  ttrefsig = 2^refSig
  for(j in 1:d)
    xn[, j] = (exprs(x)[pm, j] - 2^bgfun[[j]](refSig)) / ttrefsig

  ## call vsn, if there are >= 2 arrays
  ##################### subsample modified here ##########################
  if(d>=2) {
    if(verbose) cat("Between array normalization and variance stabilizing transformation\n")
    #yn = exprs(vsnMatrix(xn, lts.quantile=0.95, subsample=as.integer(2e5), verbose=verbose)) # since vsnMatrix returns object of class 'vsn'
	yn = exprs(vsnMatrix(xn, lts.quantile=0.95, subsample=as.integer(subsample), verbose=verbose)) # since vsnMatrix returns object of class 'vsn'
  } else {
    yn = xn
    warning("'x' has only one column, cannot do between array normalization and variance stabilizing transformation")
  }
  
  ## throw out data from probes that have too small refSig, they are likely to
  ## be dominated by noise / unspecific signal
  throwOut = (refSig < quantile(refSig, probs=cutoffQuantile))
  yn[throwOut, ] = NA

  exprmat = matrix(as.numeric(NA), nrow=n, ncol=d)
  exprmat[pm, ] = yn

  res = x
  exprs(res) = exprmat

  return(res)  
}
