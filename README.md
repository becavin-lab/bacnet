# Summary

BACNET is a Java based platform to develop website for multi-omics analysis. Our platform integrates different type of tools, the three most important being: 
1.  A table tools for Heatmap drawing and manipulation. 
2.  Genome viewer for displaying gene expression array (<a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/DNA_microarray">DNA microarray</a>), tiling array, and <a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/RNA-Seq">RNA-Seq</a> data along with <a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/Proteomics">proteomics</a> and <a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/Genomics">genomics</a> data. This genome viewer is able to load quickly every data using streaming techniques, to zoom through the data from a nucleotide scale to a genome-wide scale, and overlay heterogeneous “omics” data.  
3.  An expression atlas, inspired from <a rel="nofollow" class="external text" href="http://www.ebi.ac.uk/gxa/">EBI Expression Atlas</a>, for easily design a query based tool which connects every genomics elements (genes, smallRNAs, antisenseRNAs) to the most relevant “omics” data.


BACNET allows also to develop local software and websites with the same code. To reach this goal we used two highly connected APis , <a rel="nofollow" class="external text" href="http://wiki.eclipse.org/index.php/Rich_Client_Platform">Eclipse RCP</a> and <a rel="nofollow" class="external text" href="http://eclipse.org/rap/">Eclipse RAP</a> (<a rel="nofollow" class="external text" href="http://fr.slideshare.net/caniszczyk/single-sourcing-rcp-and-rap">single sourcing</a>).
A referent website for *Listeria* species called <a rel="nofollow" class="external text" href="https://listeriomics.pasteur.fr/Listeriomics/#bacnet.Listeria">Listeriomics</a> has been developed using BACNET platform. The main purpose of the Listeriomics website is to give scientists a quick and easy access to tools created for answering the four questions one has when starting a study on a specific genomic element:

*  What are the known functions of a genomic feature in a given strain and homologies in closely related strains?
*  What are the biological conditions in which a genomic feature is transcribed?
*  What are the biological conditions in which a genomic feature is translated?
*  What is the regulation network involved with a genomic feature of interest?

You will find in the <a rel="nofollow" class="external text" href="https://github.com/drbecavin/bacnet-private/wiki">project wikis</a> different tutorials showing you how to build such a website using BACNET platform.

Access the BACNET javadoc here `https://becavin-lab.github.io/bacnet/`

