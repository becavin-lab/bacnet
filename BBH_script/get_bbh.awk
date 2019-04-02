{
	#This scripts reads the blast output. For the record, I format the blast output in 9 columns. These columns contain :
	# Query_sequence_id ## Subject_sequence_id ## Query_length ## Subject_length ## Alignment_length ## number_of_identical_sites ## number_of_positive_sites ## e-value ## bit-score

	#ppos is the number of positives sites (7th column in the blast output - so in awk, $7)
	#it is divided by the length of the query sequence (3rd column, so $3)
	#So my approach is to consider the total length of the sequence.
	#Using the blast output, you can change to a more conventional choice, e.g. calculate the percentage of similarity or identity over the length of the alignment
	#and use a coverage threshold which is the percentage of the longest sequence covered by the alignment
	#In this case you need to define two variables instead of one here (ppos)
	#ppos=$7/$3
	#$3<$4 ? coverage=$5/$4 : coverage=$5/$3
	#You also need here to define a coverage threshold
	#cov_threshold = 0.8
	
	ppos=$7/$3
	pair=$1"\t"$2
	revpair=$2"\t"$1
	if(pair in revtable) {
		if (ppos > revtable[pair]) {
			revtable[pair]=ppos
		}
	}
	else {
		if(revpair in table) {
			revtable[pair]=ppos
		}
		else {
			if(pair in table) {
				if (ppos > table[pair]) {
					table[pair]=ppos
				}
			}
			else {
				table[pair]=ppos
			}
		}
	}
}

END {
	for(i in table) {
		split(i,a,"\t")
		rev=a[2]"\t"a[1]
		if(rev in revtable) {
			#You can change the rules to accept a BBH or not within this if line.
			#You can change the || to a && if you want both reciprocal scores to be above the threshold
			#You can add the coverage criterion, e.g.
			#if(table[i] >= similarity && revtable[rev] >= similarity && coverage >= cov_threshold) {
			if (table[i] >= similarity || revtable[rev] >= similarity) {
				if(match(a[1],pivot)) {
					print i"\t"table[i]"\t"revtable[rev]
				}
				else{
					print rev"\t"revtable[rev]"\t"table[i]
				}
			}
		}
		else {
			if(table[i] >= similarity) {
				if(match(a[1],pivot)) {
					print i"\t"table[i]
				}
				else{
					print rev"\t"table[i]
				}
			}
		}
	}
}
