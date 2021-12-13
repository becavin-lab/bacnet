package bacnet.scripts.database;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import bacnet.Database;
import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.phylogeny.Phylogenomic;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.utils.FileUtils;

public class PhylogenomicsCreation {

	/**
	 * Method to create a Phylogenomic figure using:<br>
	 * JolyTree - https://gitlab.pasteur.fr/GIPhy/JolyTree for alignment and tree
	 * construction<br>
	 * FigTree for Drawing Phylogenomic tree
	 * 
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
	 * Copy All 1st chromosome fasta files in fastaFolder and rename them with
	 * genome name
	 * 
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
		for (int i = 0; i < genomeNames.size(); i++) {
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
				if (fileLengthMax < fileTemp.length()) {
					fileMax = fileTemp;
				}
			}
			/*
			 * Copy selected fasta to fastaFolder
			 */
			System.out.println(fileMax.getAbsolutePath());
			try {
				FileUtils.copy(fileMax.getAbsolutePath(),
						fastaFolder + GenomeNCBI.processGenomeName(genomeName) + ".fna");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		logs += "All genome fasta files were copy in :" + fastaFolder + ".\n";
		return logs;
	}

	/**
	 * Run Genomes alignment and tree reconstruciton with JolyTree
	 * https://gitlab.pasteur.fr/GIPhy/JolyTree
	 * 
	 * @param logs
	 * @return
	 */
	public static String runJOLYTree(String fastaFolder, String output, String logs) {
		logs += "Install JolyTree on your computer : https://gitlab.pasteur.fr/GIPhy/JolyTree\nOr use the one at : bacnet.e4.rap.setup/JolyTree.sh\n";
		logs += "Dependencies: mash (Ondov et al. 2016) version >= 1.0.2; gawk version >= 4.1.0; FastME (Lefort et al. 2015) version >= 2.1.5.1; REQ version >= 1.2 - Use Anaconda to install them easily\n";
		String script = "JolyTree.sh -i " + fastaFolder + " -b " + output;
		logs += "Execute:\"" + script + "\"\n";
		System.out.println(script);
		logs += "Copy results in " + Database.getTEMP_PATH();
		return logs + "\n";
	}

	/**
	 * Run Tree figure construction
	 * 
	 * @param output
	 * @param logs
	 * @return
	 */
	public static String runFigTree(String output, String logs) {
		logs += "Install FigTree on your computer : http://tree.bio.ed.ac.uk/software/figtree/\n";
		logs += "Run FigTree and load " + output + ".nwk\n";
		logs += "Select \"Align Tip Labels\" and \"Trees>Transform branches>Proportional\"\nResize labels in \"Tip Labels>Font Size\"\n";
		logs += "Resize FigTree to have a vertical image and save it to: " + Phylogenomic.PHYLO_GENOME_SVG + "\n";
		return logs;
	}

	public static String organizePhyloTable(String logs) {
		String message = "Load genome table: " + Database.getInstance().getGenomeArrayPath() + "\n";
		ExpressionMatrix genomesMatrix = ExpressionMatrix.loadTab(Database.getInstance().getGenomeArrayPath(), true);
		message += "Load phylogenomic tree: " + Phylogenomic.PHYLO_GENOME_SVG + "\n";
		
		String phyloTreeTemp = FileUtils.readText(Phylogenomic.PHYLO_GENOME_SVG);
		HashMap<String, String> genomeToAttribute = Phylogenomic.parsePhylogenomicFigure(phyloTreeTemp);

		LinkedHashMap<String, Integer> strainToPyloId = new LinkedHashMap<String, Integer>();
		TreeMap<Double, String> heightToStrain = new TreeMap<Double, String>();
		// Organize strain by phyloId
		for (String genome : genomeToAttribute.keySet()) {
			String matrixInfo = genomeToAttribute.get(genome);
			if(matrixInfo.contains("matrix")) {
				System.out.println(matrixInfo.substring(matrixInfo.indexOf("matrix("), matrixInfo.indexOf(")")));
				String[] parseLine = matrixInfo.substring(matrixInfo.indexOf("matrix("), matrixInfo.indexOf(")")).split(",");
				String heigth = parseLine[parseLine.length - 1];
				double value = Double.valueOf(heigth);
				heightToStrain.put(value, GenomeNCBI.unprocessGenomeName(genome));
				System.out.println(genome + " " + GenomeNCBI.unprocessGenomeName(genome) + " " + heigth);
			}
		}

		int phyloId = 1;
		for (Double value : heightToStrain.keySet()) {
			strainToPyloId.put(heightToStrain.get(value), phyloId);
			phyloId++;
		}

		// reorganize rownames
		message += "Reorganize genome table with PhyloId = Tree architecture\n";
		TreeMap<String, Integer> newRowNames = new TreeMap<String, Integer>();
		for (String key : genomesMatrix.getRowNames().keySet()) {
			Integer value = genomesMatrix.getRowNames().get(key);
			String strain = genomesMatrix.getValueAnnotation(key, "Name");
			String phyloIdStrain = String.valueOf(strainToPyloId.get(strain));
			if (phyloIdStrain.length() == 1) {
				phyloIdStrain = "0" + phyloIdStrain;
			}
			newRowNames.put(phyloIdStrain, value);
		}

		genomesMatrix.setRowNames(newRowNames);
		genomesMatrix.setOrdered(false);
		genomesMatrix.saveTab(Database.getInstance().getGenomeArrayPath() + ".txt", "PhyloID");
		message += "Saved: " + Database.getInstance().getGenomeArrayPath();
		logs += message + "\n";
		return logs;
	}

}
