package bacnet.reader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.DNASequence;

import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.ChromosomeBacteriaSequence;
import bacnet.datamodel.sequence.Gene;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

public class PTTReader {

	public static String EXTENSION = ".gff";

	/**
	 * Add feature from a PTT file<br>
	 * IMPORTANT: Need to be modified!
	 * 
	 * @param root
	 * @param file
	 * @param chromosomes
	 * @return
	 * @throws Exception
	 */
	public static LinkedHashMap<String, ChromosomeBacteriaSequence> addGeneFeaturesFromPTT(File root, File file,
			LinkedHashMap<String, ChromosomeBacteriaSequence> chromosomes) throws Exception {
		// we get only .gff (i.e. annotation data) files in the folder root
		File[] files = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".ptt"))
					return true;
				return false;
			}
		});
		// we had only the features coming from the file (.gff) with the same name as
		// file (.fna)
		for (int i = 0; i < files.length; i++) {
			// the two files have the same name
			if (FileUtils.removeExtensionAndPath(files[i].getAbsolutePath())
					.equals(FileUtils.removeExtensionAndPath(file.getAbsolutePath()))) {

				// We read the GFF file and assemble all the data by Locus_tag
				String[][] listGenes = TabDelimitedTableReader.read(files[i]);
				/*
				 * We create a HashMap which will regroup features with the same locus_tag or ID
				 * than a gene
				 */
				LinkedHashMap<String, String[]> listLocusTag = new LinkedHashMap<String, String[]>();
				// we get all genes
				for (int j = 1; j < listGenes.length; j++) {
					// we can found locusTag at column 5
					listLocusTag.put(listGenes[j][5], ArrayUtils.getRow(listGenes, j));
				}

				// We try to allocate each locus_tag to a coding/non-coding region
				for (ChromosomeBacteriaSequence chromosome : chromosomes.values()) {
					for (String accession : chromosome.getCodingSequenceHashMap().keySet()) {
						// findCorrespondingCodingSequence(accession, chromosome, listLocucTag);
						DNASequence sequence = chromosome.getCodingSequenceHashMap().get(accession);
						String locusTag = sequence.getAccession().toString();
						if (listLocusTag.containsKey(locusTag)) {
							String[] infos = listLocusTag.get(locusTag);
							// chromosome.getLocusTagToKeyHashMap().put(locusTag, accession);
							// we first copy the locus_tag
							sequence.setAccession(new AccessionID(locusTag));
							// parse position
							String position = infos[0].replace('.', '-');
							int begin = Integer.parseInt(position.split("--")[0]);
							int end = Integer.parseInt(position.split("--")[1]);
							if (infos[1].equals("-")) {
								int temp = end;
								end = begin;
								begin = temp;
							}
							sequence.setBioBegin(begin);
							sequence.setBioEnd(end);
							// add Description
							sequence.setDescription(infos[8]);
							// add Note
							String ret = "CDS \t " + infos[0] + "\n";
							ret += "\t Gene : " + infos[4] + "\n";
							ret += " \t Strand : " + infos[1] + "\n";
							ret += "\t product : " + infos[8] + "\n";
							ret += "\t protein_Id : " + infos[3] + "\n";
							sequence.addNote(ret);
							listLocusTag.remove(locusTag);
						}
						// System.out.println();
					}
				}
				System.out.println(listLocusTag.size() + " sequences are not in the gene list.");
				System.out.println(
						"They might be pseudo-gene or RefSeq provisional - Consequently they are added with the AccessionID : PROV_i");
				
				/*
				 * Element in listLocusTag here are not found in gene list So we describe them
				 * has pseudo-gene or refSeq
				 */
				// for(String locusTag : listLocucTag.keySet()){
				// ChromosomeBacteriaSequence chromosome =
				// chromosomes.values().iterator().next();
				// DNASequence sequence = new DNASequence();
				// sequence.setAccession(new AccessionID(locusTag));
				// sequence.setDescription("Pseudo-gene or RefSeq provisional");
				//
				// for(FeatureI feature : listLocucTag.get(locusTag)){
				// if(!feature.type().equals("source")){
				// String ret = feature.type()+" \t "+feature.location().bioStart()+" ..
				// "+feature.location().bioEnd()+"\n";
				// ret+=" \t Strand : "+feature.location().bioStrand()+"\n";
				// if(feature.hasAttribute("note")) ret+="\t note :
				// "+feature.getAttribute("note")+"\n";
				// if(feature.hasAttribute("pseudo")){
				// ret+="\t Pseudo-Gene "+feature.getAttribute("pseudo")+"\n";
				// }else{
				// ret+="\t Provisional refSeq gene\n";
				// }
				// sequence.addNote(ret);
				// sequence.setBioBegin(feature.location().bioStart());
				// sequence.setBioEnd(feature.location().bioEnd());
				// }
				// }
				// chromosome.getNonCodingSequences().put("PROV_"+j, sequence);
				// chromosome.getLocusTagToKeyHashMap().put(locusTag, "PROV_"+j+"_NC");
				// j++;
				// }
				System.out.println("end");
			}
		}
		return chromosomes;
	}

	/**
	 * Save list of Gene in a PTT file<br>
	 * Information about the resulting protein are written into different
	 * columns:<br>
	 * "Location","Strand","Length","PID","Gene","Synonym","Code","COG","Product"
	 * 
	 * @param sequences
	 * @param fileName
	 * @param comment   Text to write at the beginning of the file: generally genome
	 *                  name
	 */
	public static void saveProteins(ArrayList<Gene> sequences, String fileName, Chromosome chromo) {
		String[] header = { "Location", "Strand", "Length", "PID", "Gene", "Synonym", "Code", "COG", "Product" };
		String[][] pttTable = new String[sequences.size() + 3][header.length];
		for (int j = 0; j < header.length; j++) {
			pttTable[0][j] = "";
			pttTable[1][j] = "";
		}
		pttTable[0][0] = chromo.getDescription() + " - 1.." + chromo.getLength();
		pttTable[1][0] = sequences.size() + " proteins";
		for (int j = 0; j < header.length; j++) {
			pttTable[2][j] = header[j];
		}
		int i = 3;
		for (Gene gene : sequences) {
			String pid = gene.getProtein_id();
			if (pid.equals(""))
				pid = "-";
			String geneName = gene.getGeneName();
			if (geneName.equals(""))
				geneName = "-";
			String[] row = { gene.getBegin() + ".." + gene.getEnd(), gene.getStrand() + "", gene.getLengthAA() + "",
					pid, geneName, gene.getName(), "-", gene.getComment(), gene.getProduct() };
			for (int j = 0; j < row.length; j++) {
				pttTable[i][j] = row[j];
			}
			i++;
		}
		TabDelimitedTableReader.save(pttTable, fileName);
	}

}
