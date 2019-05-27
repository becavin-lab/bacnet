package bacnet.scripts.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import bacnet.Database;
import bacnet.datamodel.phylogeny.Phylogenomic;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.utils.ArrayUtils;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class PhylogenomicsCreation {
    
	public static String IQTREE_PATH_WIN = "C:\\Users\\ipmc\\Documents\\BACNET\\Bacnet-private\\bacnet.scripts\\external\\iqtree-1.6.10-Windows\\bin\\iqtree.exe";
	
	public static String IQTREE_PATH = IQTREE_PATH_WIN;
	
	
	// ADD MAFFT parameters and run it on genomes
	// Run IQTree
	// Run FigTree
	
	
	public static String createPhylogenomicFigure(String logs) {
		String output = "PhylogenyGenomes";
		String fastaFolder = GenomeNCBI.PATH_RAW + "Fasta/";
		
		logs = copyFastafile(fastaFolder, logs);
		logs = runJOLYTree(fastaFolder, output, logs);
		logs = runFigTree(output, logs);
		return logs;
	}
	
	
	/**
	 * Copy All 1st chromosome fasta files in fastaFolder and rename them with genome name
	 * @param fastaFolder
	 * @param logs
	 * @return
	 */
	public static String copyFastafile(String fastaFolder, String logs) {
		ArrayList<String> genomeNames = Genome.getAvailableGenomes();
		String genomeFolder = GenomeNCBI.PATH_GENOMES;
		if (!FileUtils.exists(fastaFolder)) {
            System.out.println("Create folder: " + fastaFolder);
            File file = new File(fastaFolder);
            file.mkdir();
        }
		
		for(int i=0;i<genomeNames.size();i++) {
			String genomeName = genomeNames.get(i);
			String folder = genomeFolder + genomeName;
			/*
			 * Search for all .fna files
			 */
			File file = new File(folder);
			File[] files = file.listFiles(new FilenameFilter() {
	            @Override
	            public boolean accept(File dir, String name) {
	                if (name.endsWith(".fna"))
	                    return true;
	                return false;
	            }
	        });
			/*
			 * Get biggest fasta file
			 */
			File fileMax = null;
			int fileLengthMax = -1;
	        for (int k = 0; k < files.length; k++) {
	        	File fileTemp = files[k];
	        	System.out.println(fileTemp.length());
	        	if(fileLengthMax < fileTemp.length()) {
	        		fileMax = fileTemp;
	        	}
	        }
	        /*
	         * Copy selected fasta to fastaFolder
	         */
	        System.out.println(fileMax.getAbsolutePath());
	        try {
				FileUtils.copy(fileMax.getAbsolutePath(), fastaFolder + genomeName.replace(" ", "_") + ".fna");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    }
		
		logs += "All genome fasta files were copy in :"+ fastaFolder +".\n";
		return logs;
	}
	
	/**
	 * Run Genomes alignment and tree reconstruciton with JolyTree
	 * https://gitlab.pasteur.fr/GIPhy/JolyTree
	 * @param logs
	 * @return
	 */
	public static String runJOLYTree(String fastaFolder, String output, String logs) {
		logs += "Install JolyTree on your computer : https://gitlab.pasteur.fr/GIPhy/JolyTree\n";
		logs+="Dependencies: mash (Ondov et al. 2016) version >= 1.0.2; gawk version >= 4.1.0; FastME (Lefort et al. 2015) version >= 2.1.5.1; REQ version >= 1.2\n";
		String script = "JolyTree.sh -i "+ fastaFolder +" -b " + output;
		logs+="Execute:\"" + script+"\"\n";
		System.out.println(script);
		logs+="Copy results in "+Database.getTEMP_PATH();
		return logs+"\n";
	}
	
	
	/**
	 * Run Tree figure construction
	 * @param output
	 * @param logs
	 * @return
	 */
	public static String runFigTree(String output, String logs) {
		logs += "Install FigTree on your computer : http://tree.bio.ed.ac.uk/software/figtree/\n";
		logs += "Run FigTree and load "+output+".nwk\n";
		logs+= "Select \"Align Tip Labels\" and \"Trees>Transform branches>Proportional\"\n";
		logs+="Resize FigTree to have a vertical image and save it to: "+Phylogenomic.PHYLO_GENOME_SVG+"\n"; 
		return logs;
	}
	
	
}
