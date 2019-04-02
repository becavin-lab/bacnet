# TODO: Add comment
# 
# Author: UIBC
###############################################################################


function (x, y, basevar.x, basevar.y, df = 10, array.type = "olig", 
		probe.set.name = NULL, trim.percent = 5) 
{
	n1 <- ncol(x)
	n2 <- ncol(y)
	ngenes <- nrow(x)
	if (is.null(probe.set.name)) {
		probe.set.name <- as.character(seq(nrow(x)))
	}
	if (n1 < 2 | n2 < 2) {
		stop("No replicated arrays!")
	}
	if (n1 > 2 | n2 > 2) {
		var.x <- basevar.x[, 2]
		var.y <- basevar.y[, 2]
		median.x <- basevar.x[, 1]
		median.y <- basevar.y[, 1]
		median.diff <- median.x - median.y
		std.dev <- sqrt(1.57 * ((var.x/n1) + (var.y/n2)))
		z.stats <- median.diff/std.dev
		data.out <- data.frame(x = x, median.1 = median.x, std.dev.1 = sqrt(var.x), 
				y = y, median.2 = median.y, std.dev.2 = sqrt(var.y), 
				median.diff = median.diff, pooled.std.dev = std.dev, 
				z.stats = z.stats)
		row.names(data.out) <- probe.set.name
		return(data.out)
	}
	if (n1 == 2 & n2 == 2) {
		var.x <- basevar.x[, 2]
		var.y <- basevar.y[, 2]
		median.x <- basevar.x[, 1]
		median.y <- basevar.y[, 1]
		median.diff <- median.x - median.y
		std.dev <- sqrt((var.x/n1) + (var.y/n2))
		z.stats <- median.diff/std.dev
		pnorm.diff <- pnorm(median.diff, mean = 0, sd = std.dev)
		p.out <- 2 * apply(cbind(pnorm.diff, 1 - pnorm.diff), 
				1, min)
		sf.xi <- smooth.spline(basevar.x[, 1], basevar.x[, 2], 
				df = df)
		var.x0 <- fixbounds.predict.smooth.spline(sf.xi, median.x)$y
		sf.xi <- smooth.spline(basevar.y[, 1], basevar.y[, 2], 
				df = df)
		var.y0 <- fixbounds.predict.smooth.spline(sf.xi, median.y)$y
		min.xvar <- min(basevar.x[, 2])
		min.yvar <- min(basevar.y[, 2])
		if (any(var.x0 < min.xvar)) 
			var.x0[var.x0 < min.xvar] <- min.xvar
		if (any(var.y0 < min.yvar)) 
			var.y0[var.y0 < min.yvar] <- min.yvar
		flag <- matrix(".", ngenes, 2)
		p.val <- matrix(NA, ngenes, 2)
		x.stat <- abs(x[, 1] - x[, 2])/sqrt(2 * var.x0)
		p.val[, 1] <- 2 * (1 - pnorm(x.stat))
		flag[p.val[, 1] < 0.01, 1] <- "*"
		flag[p.val[, 1] < 0.005, 1] <- "**"
		flag[p.val[, 1] < 0.001, 1] <- "***"
		y.stat <- abs(y[, 1] - y[, 2])/sqrt(2 * var.y0)
		p.val[, 2] <- 2 * (1 - pnorm(y.stat))
		flag[p.val[, 2] < 0.01, 2] <- "*"
		flag[p.val[, 2] < 0.005, 2] <- "**"
		flag[p.val[, 2] < 0.001, 2] <- "***"
		data.out <- data.frame(x = x, median.1 = median.x, std.dev.1 = sqrt(var.x), 
				p.outlier.x = p.val[, 1], flag.outlier.x = flag[, 
						1], y = y, median.2 = median.y, std.dev.2 = sqrt(var.y), 
				p.outlier.y = p.val[, 2], flag.outlier.y = flag[, 
						2], median.diff = median.diff, pooled.std.dev = std.dev, 
				z.stats = z.stats)
		row.names(data.out) <- probe.set.name
		return(data.out)
	}
}

