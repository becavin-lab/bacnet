# TODO: Add comment
# 
# Author: UIBC
###############################################################################


error = function (y, stats = median, q = 0.01, min.genes.int = 10, div.factor = 1) 
{
	baseline.step1 <- baseOlig.error.step1(y, stats = stats)
	baseline.step2 <- baseOlig.error.step2(y, stats = stats, 
			baseline.step1, min.genes.int = min.genes.int, div.factor = div.factor)
	return(baseline.step2)
}

error.step1 = function (y, stats = median, q = 0.01, df = 10) 
{
	AM <- am.trans(y)
	A <- AM[, 1]
	M <- AM[, 2]
	median.y <- apply(y, 1, stats)
	quantile.A <- quantile(A, probs = seq(0, 1, q), na.rm = TRUE)
	quan.n <- length(quantile.A) - 1
	var.M <- rep(NA, length = quan.n)
	medianAs <- rep(NA, length = quan.n)
	if (sum(A == min(A)) > (q * length(A))) {
		tmpA <- A[!(A == min(A))]
		quantile.A <- c(min(A), quantile(tmpA, probs = seq(q, 
								1, q), na.rm = TRUE))
	}
	for (i in 2:(quan.n + 1)) {
		n.i <- length(!is.na(M[A > quantile.A[i - 1] & A <= quantile.A[i]]))
		if (n.i > 1) {
			mult.factor <- 0.5 * ((n.i - 0.5)/(n.i - 1))
			var.M[i - 1] <- mult.factor * var(M[A > quantile.A[i - 
											1] & A <= quantile.A[i]], na.rm = TRUE)
			medianAs[i - 1] <- median(A[A > quantile.A[i - 1] & 
									A <= quantile.A[i]], na.rm = TRUE)
		}
	}
	if (any(is.na(var.M))) {
		for (i in (quan.n - 1):1) {
			if (is.na(var.M[i])) {
				var.M[i] <- ifelse(!is.na(var.M[i - 1]), mean(var.M[i + 
												1], var.M[i - 1]), var.M[i + 1])
			}
		}
	}
	var.M[1:which(var.M == max(var.M))] <- max(var.M)
	base.var <- cbind(A = medianAs, var.M = var.M)
	sm.spline <- smooth.spline(base.var[, 1], base.var[, 2], 
			df = df)
	min.Var <- min(base.var[, 2])
	var.genes <- fixbounds.predict.smooth.spline(sm.spline, median.y)$y
	if (any(var.genes < min.Var)) 
		var.genes[var.genes < min.Var] <- min.Var
	basevar.step1 <- cbind(A = median.y, var.M = var.genes)
	ord.median <- order(basevar.step1[, 1])
	var.genes.ord <- basevar.step1[ord.median, ]
	return(var.genes.ord)
}

error.step2 = function (y, baseOlig.error.step1.res, df = 10, stats = median, 
		min.genes.int = 10, div.factor = 1) 
{
	AM <- am.trans(y)
	A <- AM[, 1]
	M <- AM[, 2]
	median.y <- apply(y, 1, stats)
	var.genes.ord <- baseOlig.error.step1.res
	genes.sub.int <- n.genes.adaptive.int(var.genes.ord, min.genes.int = min.genes.int, 
			div.factor = div.factor)
	j.start <- 1
	j.end <- 0
	var.M.adap <- rep(NA, length = length(genes.sub.int))
	medianAs.adap <- rep(NA, length = length(genes.sub.int))
	for (i in 2:(length(genes.sub.int) + 1)) {
		j.start <- j.end + 1
		j.end <- j.start + genes.sub.int[i - 1] - 1
		vect.temp <- (A > var.genes.ord[j.start, 1] & A <= var.genes.ord[j.end, 
							1])
		n.i <- length(!is.na(M[vect.temp]))
		if (n.i > 1) {
			mult.factor <- 0.5 * ((n.i - 0.5)/(n.i - 1))
			var.M.adap[i - 1] <- mult.factor * var(M[vect.temp], 
					na.rm = TRUE)
			medianAs.adap[i - 1] <- median(A[vect.temp], na.rm = TRUE)
		}
	}
	if (any(is.na(var.M.adap))) {
		for (i in length(genes.sub.int):1) {
			if (is.na(var.M.adap[i])) {
				var.M.adap[i] <- ifelse(!is.na(var.M.adap[i - 
												1]), mean(var.M.adap[i + 1], var.M.adap[i - 
												1]), var.M.adap[i + 1])
			}
		}
	}
	var.M.adap[1:which(var.M.adap == max(var.M.adap))] <- max(var.M.adap)
	base.var.adap <- cbind(A.adap = medianAs.adap, var.M.adap = var.M.adap)
	sm.spline.adap <- smooth.spline(base.var.adap[, 1], base.var.adap[, 
					2], df = df)
	min.Var <- min(base.var.adap[, 2])
	var.genes.adap <- fixbounds.predict.smooth.spline(sm.spline.adap, 
			median.y)$y
	if (any(var.genes.adap < min.Var)) 
		var.genes.adap[var.genes.adap < min.Var] <- min.Var
	basevar.all.adap <- cbind(A = median.y, var.M = var.genes.adap)
	return(basevar.all.adap)
}