BACNET V2 - December 2021

Bacnet is a Java based platform for fast development of ‘personalized’ multiomics website. Bacnet is easy to import, modify and deploy on web server. Bacnet was used already for the development of [four websites](http://www.genomique.info:10080/Bacnet/), two of them being already published [Listeriomics](https://listeriomics.pasteur.fr/Listeriomics/#bacnet.Listeria) ([Bécavin et al., 2017](https://msystems.asm.org/content/2/2/e00186-16)), [CrisprGo](http://hub13.hosting.pasteur.fr:8080/CRISPRBrowser/) ([Rousset et al., 2018](https://journals.plos.org/plosgenetics/article?id=10.1371/journal.pgen.1007749)). We believe Bacnet is the only platform available which allows to easily create website for sharing and publishing scientific results of specific multiomics analysis, opening the era of ‘personalized omics’.

Our platform integrates different type of tools, the three most important being: 
1.  A table tools for Heatmap drawing and manipulation. 
2.  Genome viewer for displaying gene expression array (<a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/DNA_microarray">DNA microarray</a>), tiling array, and <a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/RNA-Seq">RNA-Seq</a> data along with <a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/Proteomics">proteomics</a> and <a rel="nofollow" class="external text" href="http://en.wikipedia.org/wiki/Genomics">genomics</a> data. This genome viewer is able to load quickly every data using streaming techniques, to zoom through the data from a nucleotide scale to a genome-wide scale, and overlay heterogeneous “omics” data.  
3.  An expression atlas, inspired from <a rel="nofollow" class="external text" href="http://www.ebi.ac.uk/gxa/">EBI Expression Atlas</a>, for easily design a query based tool which connects every genomics elements (genes, smallRNAs, antisenseRNAs) to the most relevant “omics” data.

All these tools are already implemented in _Listeriomics_ website. See the [Listeriomics wiki](https://listeriomics.pasteur.fr/WikiListeriomics/index.php/Summary) to understand how they work.

![Bacnet Ad](https://github.com/becavin-lab/bacnet/blob/master/wiki/BACNET%20Ad.png)

Bacnet includes four plug-ins which should be imported from the github repository: 
* _**bacnet.core**_ package for all the necessary tools of the platform; 
* _**bacnet.scripts**_ for all methods allowing creation of the database_
* _**bacnet.e4.rap.setup**_ a web interface for multi-omics database creation
* _**bacnet.e4.rap**_ for creating the mult-iomics website

It includes also 2 features for the deployement of bacnet websites on Apache Tomcat server:
* _**bacnet.setup.feature**_ a web interface for multi-omics database creation
* _**bacnet.webapp.feature**_ for creating the mult-iomics website

BACNET allows to develop websites using <a rel="nofollow" class="external text" href="http://eclipse.org/rap/">Eclipse RAP</a>. The choice of Eclipse RAP (based on Eclipse RCP) was made because of its easy to use interface named [WindowBuilder](https://www.eclipse.org/windowbuilder/). It allows the user to quickly design its own view by adding panels, buttons, canvas in a “one click” fashion. One can also add existing graphical API based on Javascript. This capability is of particular interest when creating multi-omics website for specific organism, paving the way for ‘personalized omics’ platforms.

See the Bacnet Javadoc for more information: [Bacnet Javadoc](https://becavin-lab.github.io/bacnet/index.html)

The following tutorials will show you how to build such a website using BACNET platform.


# Tutorials

## First-Steps

[Install BACNET](https://github.com/becavin-lab/bacnet/wiki/Install-bacnet) - Install BACNET platform

[Eclipse RAP architecture](https://github.com/becavin-lab/bacnet/wiki/Eclipse-RAP-and-RCP-architecture) - Some tutorials to get familiar with Eclipse RAP  architecture

## Deploy ListeriomicsSample multi-omics website

[Develop your first multi omics website](https://github.com/becavin-lab/bacnet/wiki/Develop-your-first-multi-omics-website) - Quickly develop a multi-omics website using sample datasets

[Add Genomes](https://github.com/becavin-lab/bacnet/wiki/Add-Genomes) - Add Genomes to your website

[Add Phylogenomics data](https://github.com/becavin-lab/bacnet/wiki/Add-Phylogenomics-data) - Add Phylogenomics information to your website

[Add Homologs](https://github.com/becavin-lab/bacnet/wiki/Add-Homologs) - Add Homologs of each protein to your website

[Add Biological conditions](https://github.com/becavin-lab/bacnet/wiki/Add-Biological-conditions) - Add Biological Conditions to your website

[Add Comparisons](https://github.com/becavin-lab/bacnet/wiki/Add-Comparisons) - Add dataset comparisons to your website

[Add Transcriptomics](https://github.com/becavin-lab/bacnet/wiki/Add-Transcriptomics) - Add Transcriptomics datasets to your website

[Add Proteomics](https://github.com/becavin-lab/bacnet/wiki/Add-Proteomics) - Add Proteomics dataset to your website

[Add Co-Expression Networks](https://github.com/becavin-lab/bacnet/wiki/Add-Co-Expression-Networks) - Add Co Expression Networks to your website

[Deploy your website](https://github.com/becavin-lab/bacnet/wiki/Deploy-multi-omics-website) - Deploy on server your website

[Run query-based multi-omics analysis](https://github.com/becavin-lab/bacnet/wiki/Query-based-tools-in-BACNET) - Create a script to query multi-omics datasets


# Additional information

[Bacnet Architecture](https://github.com/becavin-lab/bacnet/wiki/Bacnet-Architecture) - We describe more in detail the architecture of Bacnet

[Bacnet Javadoc](https://becavin-lab.github.io/bacnet/index.html) - Get JavaDoc of all Bacnet API


## Support

![Bacnet Ad](https://github.com/becavin-lab/bacnet/blob/master/wiki/logos.png)
