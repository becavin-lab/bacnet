# Loading the library and the data
library(LPE)
data(Ley)
dim(Ley)
# Gives 12488*7
# First column is ID.
Ley[,2:7] <- preprocess(Ley[,2:7],data.type="MAS5")
# Subsetting the data
subset.Ley <- Ley[1:1000,]
# Finding the baseline distribution of condition 1 and 2.
var.1 <- baseOlig.error(subset.Ley[,2:4], q=0.01)
var.2 <- baseOlig.error(subset.Ley[,5:7], q=0.01)
# Applying LPE
lpe.result <- lpe(subset.Ley[,2:4],subset.Ley[,5:7], var.1, var.2,
probe.set.name=subset.Ley[,1])

# recupére les valeurs de x et y
x.location <- grep("^x", names(lpe.result))
y.location <- grep("^y", names(lpe.result))
x <- lpe.result[, x.location]
y <- lpe.result[, y.location]

# Recupere la distribution null pour estimer FDR,
# recupere la distribution function ayant une moyenne nulle, des quantiles egaux a median.diff, 
#  et standard deviation donné par lpe.result$pooled.std.dev = estimation de pooled error du LPE 
pnorm.diff <- pnorm(lpe.result$median.diff, mean = 0, sd = lpe.result$pooled.std.dev)

# p.out = 2 * minimum entre pnorm.diff et 1 - pnorm.diff 
# on calcul la p-value
# (i.e. on applique la fonction min sur les lignes du tableau a 2 colonnes [pnorm,1-pnorm])
p.out <- 2 * apply(cbind(pnorm.diff, 1 - pnorm.diff), 1, min)
            
p.adj <- pmin(p.out * nrow(x), 1)

            
############    mt.rawp2adjp.LPE   #############   
	# init des variables        
	rawp <- p.out
	m <- length(rawp)
    n <- length(proc)
    
    # reorder la matrice rawp
    index <- order(rawp)
    spval <- rawp[index]
    
    # cree la matrice de resultats
    adjp <- matrix(0, m, n + 1)
    proc <- "BH"
    dimnames(adjp) <- list(NULL, c("rawp", proc))
    adjp[, 1] <- spval
    
    
    #algo d'ajustement  (depend du choix de type d'ajustement "proc")
    if (is.element("Bonferroni", proc)) {
        tmp <- m * spval
        tmp[tmp > 1] <- 1
        adjp[, "Bonferroni"] <- tmp
    }
    if (is.element("BH", proc)) {
        tmp <- spval
        for (i in (m - 1):1) {
            # ajustement en prenant  min( 1 , tmp[i+1] , (m/i) * spval[i] )  ????
            tmp[i] <- min(tmp[i + 1], min((m/i) * spval[i], 1))
        }
        adjp[, "BH"] <- tmp
    }
    if (is.element("BY", proc)) {
        tmp <- spval
        a <- sum(1/(1:m))
        tmp[m] <- min(a * spval[m], 1)
        for (i in (m - 1):1) {
            tmp[i] <- min(tmp[i + 1], min((m * a/i) * spval[i], 
                1))
        }
        adjp[, "BY"] <- tmp
    }
    
    
   	# reorder the results en utilisant index
    ndx <- order(index)
    adjp <- adjp[ndx, ]
    # la liste en output
    list(adjp = adjp, index = index)
############    fin de mt.rawp2adjp.LPE   #############      
   
# assemble lpe.result et la list nouvellement crŽe
data.out <- data.frame(x = x, median.1 = lpe.result$median.1, 
            std.dev.1 = lpe.result$std.dev.1, y = y, median.2 = lpe.result$median.2, 
            std.dev.2 = lpe.result$std.dev.2, median.diff = lpe.result$median.diff, 
            pooled.std.dev = lpe.result$pooled.std.dev, abs.z.stats = abs(lpe.result$z.stats), 
            p.adj = p.adj)

# Recupere la colonne FDR et la colonne z.test         
col.id <- grep(proc, colnames(data.out))
aa <- cbind(FDR = data.out[, col.id], z.real = data.out$abs.z.stats)
rownames(aa) <- rownames(data.out)
aa <- aa[order(aa[, 2], decreasing = TRUE), ]
      
##################    FINISH:   return(aa)
