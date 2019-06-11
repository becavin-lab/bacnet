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
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
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
import bacnet.utils.VectorUtils;

public class PhylogenomicsCreation {
    
	/**
	 * Method to create a Phylogenomic figure using:<br>
	 * JolyTree - https://gitlab.pasteur.fr/GIPhy/JolyTree for alignment and tree construction<br>
	 * FigTree for Drawing Phylogenomic tree
	 * @param logs
	 * @return
	 */
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
	
	public static String organizePhyloTable(String logs) {
		String message = "Load genome table: "+Database.getInstance().getGenomeArrayPath()+"\n";
		ExpressionMatrix genomesMatrix = ExpressionMatrix.loadTab(Database.getInstance().getGenomeArrayPath(),true);
		message += "Load phylogenomic tree: "+Phylogenomic.PHYLO_GENOME_SVG+"\n";
		ArrayList<String> phyloTree = TabDelimitedTableReader.readList(Phylogenomic.PHYLO_GENOME_SVG,true);
		LinkedHashMap<String, Integer> strainToPyloId = new LinkedHashMap<String, Integer>();
		TreeMap<Double, String> heightToStrain = new TreeMap<Double, String>();
		// Organize strain by phyloId
		for(String line : phyloTree) {
			for(String genome : Genome.getAvailableGenomes()) {
				if(line.contains(">"+genome+"<")) {
					// Find strain position in the tree
					String[] linesTemp = line.split("=");
					for(String lineTemp : linesTemp){
						if(lineTemp.startsWith("\"matrix(")) {
							String[] parseLine = lineTemp.split(" ");
							//VectorUtils.displayVector("yo", parseLine);
							String heigth = parseLine[parseLine.length-2];
							double value = Double.valueOf(heigth.substring(0, heigth.length()-2));
							heightToStrain.put(value,  genome);
						}
					}
				}
			}
		}
		
		int phyloId = 1;
		for(Double value : heightToStrain.keySet()) {
			strainToPyloId.put(heightToStrain.get(value), phyloId);
			phyloId++;
		}
		
		// reorganize rownames
		message += "Reorganize genome table with PhyloId = Tree architecture\n";
		TreeMap<String,Integer> newRowNames = new TreeMap<String, Integer>();
		for(String key : genomesMatrix.getRowNames().keySet()) {
			Integer value = genomesMatrix.getRowNames().get(key);
			String strain = genomesMatrix.getValueAnnotation(key, "Name");
			String phyloIdStrain = String.valueOf(strainToPyloId.get(strain));
			if(phyloIdStrain.length()==1) {
				phyloIdStrain = "0"+phyloIdStrain;
			}
			newRowNames.put(phyloIdStrain, value);
		}
		
		genomesMatrix.setRowNames(newRowNames);
		genomesMatrix.setOrdered(false);
		genomesMatrix.saveTab(Database.getInstance().getGenomeArrayPath(), "PhyloID");
		message += "Saved: "+Database.getInstance().getGenomeArrayPath();
		logs += message+"\n";
		return logs;
	}
	
	
}
