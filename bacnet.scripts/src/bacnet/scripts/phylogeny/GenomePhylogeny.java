package bacnet.scripts.phylogeny;

import java.io.File;
import java.io.FilenameFilter;
import java.util.TreeMap;

import bacnet.Database;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

/**
 * Manage phylogeny from MLST scheme : Jolley and others
 * @author christophebecavin
 *
 */
public class GenomePhylogeny {

	/**
	 * This table taken from Jolley et al. Microb. 2012, gives info on all bacterial
	 * genomes <br>
	 * It allows to make link between refSeq or GenBank IDs of genome to different
	 * information <br>
	 * More specifically it gives the id of each genome in the phylogeny tree of
	 * Jolley et al. Microb. 2012
	 */
	public static String PATH_INFOTABLE = Database.getANALYSIS_PATH()
			+ "/Egd-e Annotation/BIGSdb_10772_1336754328_25031.txt";

	/**
	 * Map Jolley ID to corresponding row in PATH_INFOTABLE
	 */
	public static TreeMap<Integer, String[]> getJolleyIDtoInfo() {
		TreeMap<Integer, String[]> jolleyIDtoInfo = new TreeMap<Integer, String[]>();
		String[][] infos = TabDelimitedTableReader.read(PATH_INFOTABLE);
		for (int i = 1; i < infos.length; i++) {
			int id = Integer.parseInt(infos[i][0]);
			jolleyIDtoInfo.put(id, ArrayUtils.getRow(infos, i));
		}

		return jolleyIDtoInfo;
	}

	/**
	 * Create list of genomes which contains all genome available <br>
	 * 
	 * This genome list contains all the genomes both from GenomeFolder and
	 * GenomePhylogeny.PATH_TABLE (=All bacteria phylogeny)
	 */
	public static TreeMap<String, String[]> getGenomesAvalaible() {
		TreeMap<Integer, String[]> jolleyIDtoInfo = getJolleyIDtoInfo();
		TreeMap<String, String[]> listGenomes = new TreeMap<String, String[]>();

		// create a map linking Accession (NC_00...) to genome name (Listeria_mono...)
		TreeMap<String, String> accessionToName = new TreeMap<String, String>();
		String[] genomes = GenomeNCBITools.getAvailableGenome();
		System.err.println(genomes.length + " genomes available");
		for (int i = 0; i < genomes.length; i++) {
			// get all FNA files contained in the genome path
			String genomeName = genomes[i];
			String path = GenomeNCBITools.getPATH() + genomeName;
			File file = new File(path);
			File[] files = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".fna"))
						return true;
					return false;
				}
			});
			for (File fileTemp : files) {
				String genomeId = FileUtils.removeExtensionAndPath(fileTemp.getAbsolutePath());
				accessionToName.put(genomeId, genomeName);
			}
		}

		// search the accession and name of each id
		for (Integer id : jolleyIDtoInfo.keySet()) {
			String aliases = jolleyIDtoInfo.get(id)[2];
			// see if the aliases contain genomeID
			for (String accession : accessionToName.keySet()) {
				if (aliases.indexOf(accession) != -1) {
					String[] info = { accessionToName.get(accession), aliases };
					listGenomes.put(id + "", info);
				}
			}
		}
		System.err.println(listGenomes.size() + " genomes will be used");
		for (String genome : genomes) {
			if (!listGenomes.containsKey(genome)) {
				System.err.println("not included: " + genome);
			}
		}
		return listGenomes;
	}
}
