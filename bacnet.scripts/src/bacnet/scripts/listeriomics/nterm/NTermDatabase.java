package bacnet.scripts.listeriomics.nterm;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import org.biojava3.core.sequence.DNASequence;

import bacnet.datamodel.proteomics.NTermUtils;
import bacnet.datamodel.sequence.Chromosome;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;

public class NTermDatabase {

	public static String DATABASE = NTermUtils.getPATH()+"Databases"+File.separator;
	public static int LENGTH_CUTOFF = 20;
	
	/**
	 * Create:<br>
	 * <li> Protein DB
	 * <li> UTR DB
	 * <li> fusion Protein DB and Protein DB
	 * <li> Genome DB
	 * <li> TIS DB
	 */
	public static void createDataBases(){
//		createProteinDB();
//		createUTRDB();
//		fusionProteinUTRDB();
		createTISDB();
//		divideTISDB();
//		verifyTISDB();
//		createMDB();
//		createGenomeDB();
	}

	/**
	 * Fusion ProteinDB and UTRDB in one file
	 */
	public static void fusionProteinUTRDB(){
		String[][] proteinDB = TabDelimitedTableReader.read(DATABASE+"ProteinDB.fasta");
		String textUtrDB = FileUtils.readText(DATABASE+"UtrDB.fasta");

		String finalDB = "";
		for(int i=0;i<proteinDB.length;i++){
			String accession = getHeaderValues(proteinDB[i][0])[1];
			System.out.println(accession);
			if(textUtrDB.contains(accession)){
				i++;
			}else{
				finalDB+=proteinDB[i][0]+"\n";
				i++;
				finalDB+=proteinDB[i][0]+"\n";
			}
		}
		finalDB+=textUtrDB;
		FileUtils.saveText(finalDB, DATABASE+"ProteinUtrDB.fasta");
	}

	/**
	 * Parse accession of fasta headers
	 * @param header
	 * @return
	 */
	private static String[] getHeaderValues(String header) {
		String[] data = new String[0];
		ArrayList<String> values = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < header.length(); i++) {
			if (header.charAt(i) == '|') {
				values.add(sb.toString());
				sb = new StringBuffer();
			} else if (i == header.length() - 1) {
				sb.append(header.charAt(i));
				values.add(sb.toString());
			} else {
				sb.append(header.charAt(i));
			}

			data = new String[values.size()];
			values.toArray(data);
		}
		return data;
	}
	
	/**
	 * Create a fasta file which will be used has a database<br>
	 * This file contains:
	 * <li> all genes
	 * <li> all new genes described in RAST
	 * <li> all Srna on 3 frames
	 * <li> all ASrna on 3 frames (300bp long)
	 * lists of Genes, ID=lmo, directly taken from NCBI
	 * lists of "new"Gene, ID=rastlmo, from the analysis of annotation I just did using RAST software
	 * lists of Srna, ID=rli, rliM, rliP, we don't know where is the begining so we have 3 frame for each rli, we start at the first start codon, plus we cut when we found stop codon and peptide smaller than 5aa.
	 * lists of ASrna, ID=anti, antiM, antiP, we don't know where is the begining so we have 3 frame for each asRNA, we start at the first start codon, plus we cut when we found stop codon and peptide smaller than 5aa.

	 */
	public static void createProteinDB(){
		Genome genome = Genome.loadEgdeGenome();
		String ret="";
		for(Gene gene : genome.getFirstChromosome().getGenes().values()){
			String name = gene.getGeneName()+" ";
			if(name.equals("- ")) name = "";
			ret+=">protDB|"+gene.getName()+"|EGDe "+name+gene.getBegin()+".."+gene.getEnd()+"/"+gene.getStrand()+"\n";
			ret+=gene.getSequenceAA()+"\n";
		}
		ArrayList<Sequence> seqs = RASToverlap.getEGDeUnknownRAST();
		for(Sequence seq : seqs){
			ret+=">protDB|"+seq.getName()+"|EGDe "+seq.getBegin()+".."+seq.getEnd()+"/"+seq.getStrand()+"\n";
			ret+=seq.getSequenceAA()+"\n";
		}
		for(Srna sRNA : genome.getFirstChromosome().getsRNAs().values()){
			ArrayList<String> sequences = sRNA.getSequenceAACurated();
			int i=0;
			String name = sRNA.getName().replaceAll(" ", "");
			for(String sequence : sequences){
				if(sequence.length()>LENGTH_CUTOFF){
					ret+=">protDB|"+name+"_"+i+"|EGDe "+sRNA.getBegin()+".."+sRNA.getEnd()+"/"+sRNA.getStrand()+"\n";
					ret+=sequence+"\n";
				}
				i++;
			}

			Sequence seqM = new Sequence("ff", sRNA.getBegin()-1, sRNA.getEnd(),sRNA.getStrand());
			sequences = seqM.getSequenceAACurated();
			i=0;
			for(String sequence : sequences){
				if(sequence.length()>LENGTH_CUTOFF){
					ret+=">protDB|"+name+"M_"+i+"|EGDe "+seqM.getBegin()+".."+seqM.getEnd()+"/"+seqM.getStrand()+"\n";
					ret+=sequence+"\n";
				}
				i++;
			}

			Sequence seqP = new Sequence("ff", sRNA.getBegin()+1, sRNA.getEnd(),sRNA.getStrand());
			sequences = seqP.getSequenceAACurated();
			i=0;
			for(String sequence : sequences){
				if(sequence.length()>LENGTH_CUTOFF){
					ret+=">protDB|"+name+"P_"+i+"|EGDe "+seqP.getBegin()+".."+seqP.getEnd()+"/"+seqP.getStrand()+"\n";
					ret+=sequence+"\n";
				}
				i++;
			}
		}

		for(Srna sRNA : genome.getFirstChromosome().getAsRNAs().values()){
			ArrayList<String> sequences = sRNA.getSequenceAACurated();
			int i=0;
			for(String sequence : sequences){
				if(sequence.length()>LENGTH_CUTOFF){
					ret+=">protDB|"+sRNA.getName()+"_"+i+"|EGDe "+sRNA.getBegin()+".."+sRNA.getEnd()+"/"+sRNA.getStrand()+"\n";
					ret+=sequence+"\n";
				}
				i++;
			}

			Sequence seqM = new Sequence("ff", sRNA.getBegin()-1, sRNA.getEnd(),sRNA.getStrand());
			sequences = seqM.getSequenceAACurated();
			i=0;
			for(String sequence : sequences){
				if(sequence.length()>LENGTH_CUTOFF){
					ret+=">protDB|"+sRNA.getName()+"M_"+i+"|EGDe "+seqM.getBegin()+".."+seqM.getEnd()+"/"+seqM.getStrand()+"\n";
					ret+=sequence+"\n";
				}
				i++;
			}

			Sequence seqP = new Sequence("ff", sRNA.getBegin()+1, sRNA.getEnd(),sRNA.getStrand());
			sequences = seqP.getSequenceAACurated();
			i=0;
			for(String sequence : sequences){
				if(sequence.length()>LENGTH_CUTOFF){
					ret+=">protDB|"+sRNA.getName()+"P_"+i+"|EGDe "+seqP.getBegin()+".."+seqP.getEnd()+"/"+seqP.getStrand()+"\n";
					ret+=sequence+"\n";
				}
				i++;
			}
		}

		FileUtils.saveText(ret, DATABASE+"ProteinDB.fasta");


	}

	/**
	 * 
	 * Create a database with all UTR from Wurtzel et al. 2012<br>
	 * Found here : DATABASE+"table_s1_tss_table.txt"<br>
	 * We found the correct frame so we are sure to have the start codon of the gene at the exact position of NCBI annotation
	 * We start at the first start codon, then if stop codon are found in the UTR region we cut in different peptides, remove ones < 5aa, 
	 * and paste to the last part of the UTR the whole gene amino acide sequence
	 * all gene, ID=lmo_n  n being the id of the UTR peptide extracted
	 */
	public static void createUTRDB(){
		String[][] tssTable = TabDelimitedTableReader.read(DATABASE+"table_s1_tss_table.txt");
		ArrayList<DNASequence> seqs = new ArrayList<DNASequence>();

		ArrayList<String> sequencesFinal = new ArrayList<String>();
		for(int i=1;i<tssTable.length;i++){
			String name = tssTable[i][0];
			int begin = Integer.parseInt(tssTable[i][1]);
			int end = Integer.parseInt(tssTable[i][2]);
			char strand = tssTable[i][3].charAt(0);
			int utrLength = Integer.parseInt(tssTable[i][7]);

			Sequence seq = new Sequence(name, begin, end, strand);
			Sequence seqGene = new Sequence(name, begin, end, strand);
			if(utrLength%3!=0){
				if(seq.isStrand()){
					seq.setEnd(seq.getBegin());
					seq.setBegin(seq.getBegin()-utrLength-(3-utrLength%3));
				}else{
					seq.setBegin(seq.getEnd());
					seq.setEnd(seq.getEnd()+utrLength+(3-utrLength%3));
				}
			}else{
				if(seq.isStrand()){
					seq.setEnd(seq.getBegin()+3);
					seq.setBegin(seq.getBegin()-utrLength);
				}else{
					seq.setBegin(seq.getEnd()-3);
					seq.setEnd(seq.getEnd()+utrLength);
				}
			}
			seq.setLength(seq.getEnd()-seq.getBegin());
			if(seq.getLength()==0) seq.setBegin(seq.getEnd()-3);


			ArrayList<String> sequences = Sequence.getSequenceAACurated(seq.getSequenceAA());
			int k=0;
			int count = 1;
			//				sequencesFinal.add(">utrDB|"+name+"_"+k+"|"+seq.getBegin()+".."+seq.getEnd() +" utrLength "+utrLength);  // to test if we cut accordingly the UTR
			//				sequencesFinal.add(seq.getSequenceAA());
			for(String sequence : sequences){
				if(count==sequences.size()){
					sequencesFinal.add(">utrDB|"+name+"_"+k+"|EGDe "+seq.getBegin()+".."+seq.getEnd() +"/"+seq.getStrand()+" utrLength "+utrLength);
					k++;
					sequencesFinal.add(sequence+seqGene.getSequenceAA());
				}else if(sequence.length()>LENGTH_CUTOFF){
					sequencesFinal.add(">utrDB|"+name+"_"+k+"|EGDe "+seq.getBegin()+".."+seq.getEnd() +"/"+seq.getStrand()+" utrLength "+utrLength);
					k++;
					sequencesFinal.add(sequence);
				}
				count++;
			}
			//				

		}

		String[][] db = new String[(sequencesFinal.size())][1];
		int k=0;
		for(String sequence: sequencesFinal){
			db[k][0] = sequence;
			k++;
		}
		TabDelimitedTableReader.save(db, DATABASE+"UtrDB.fasta");
	}

	/**
	 * lists of small peptides contained in the genome of EGDe on 6frames.<br>
	 * The small peptides include in the list are the one only of size more than 5aa, and surrounded by at least one R.<br>
	 * example:  Start - peptide - R<br>
	 * 	  Stop - peptide - R<br>
	 * 	  <br>
	 * Element like:  Start - peptide - Stop  are rejected from the analysis<br>
	 *  <br>
	 * <li> frame 1 = EGDe genome, ID=genome
	 * <li> frame 2 = EGDe genome 1bp on the left, ID=genomeM
	 * <li> frame 3 = EGDe genome 1bp on the right, ID=genomeP
	 * <li> frame 4 = EGDe reverse complement genome, ID=genomeCompl
	 * <li> frame 5 = EGDe reverse complement genome 1bp on the left, ID=genomeComplM
	 * <li> frame 6 = EGDe reverse complement genome 1bp on the right, ID=genomeComplP
	 * <br>
	 */
	public static void createGenomeDB(){
		Chromosome chromosome = Genome.loadEgdeGenome().getFirstChromosome();
		createGenomeFasta(chromosome.getRNASequence().getProteinSequence().getSequenceAsString(),"genome", "EGDe");
		String seqComplement = chromosome.getReverseComplement().getSequenceAsString();
		DNASequence chromoComplement = new DNASequence(seqComplement);
		createGenomeFasta(chromoComplement.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeCompl", "EGDeCompl");

		String seqTemp = chromosome.getSequenceAsString();
		String seqMinus = seqTemp.charAt(seqTemp.length()-1)+seqTemp.substring(0, seqTemp.length()-1);
		String seqPlus = seqTemp.substring(1, seqTemp.length())+seqTemp.charAt(0);
		DNASequence seq = new DNASequence(seqMinus);
		createGenomeFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeM", "EGDeMinus");
		seq = new DNASequence(seqPlus);
		createGenomeFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeP", "EGDePlus");

		seqTemp = chromoComplement.getSequenceAsString();
		seqMinus = seqTemp.charAt(seqTemp.length()-1)+seqTemp.substring(0, seqTemp.length()-1);
		seqPlus = seqTemp.substring(1, seqTemp.length())+seqTemp.charAt(0);
		seq = new DNASequence(seqMinus);
		createGenomeFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeComplM", "EGDeComplMinus");
		seq = new DNASequence(seqPlus);
		createGenomeFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeComplP", "EGDeComplPlus");


		ArrayList<String> ret;
		ret = TabDelimitedTableReader.readList(DATABASE+"GenomeDB_EGDe.fasta");
		for(String line : TabDelimitedTableReader.readList(DATABASE+"GenomeDB_EGDeCompl.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"GenomeDB_EGDeMinus.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"GenomeDB_EGDePlus.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"GenomeDB_EGDeComplMinus.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"GenomeDB_EGDeComplPlus.fasta")) ret.add(line);
		TabDelimitedTableReader.saveList(ret, DATABASE+"GenomeDB.fasta");
	}

	/**
	 * From an amino acid sequence of a genome create final fasta file by cutting at each Start, Stop and R positions
	 * @param seqAA
	 * @param prefix
	 * @param fileName
	 */
	private static void createGenomeFasta(String seqAA,String prefix,String fileName){
		String seqAAComplete =seqAA;
		seqAA = seqAA.replace('*', '-');
		seqAA = seqAA.replaceAll("-", "-Stop\nStop-");
		seqAA = seqAA.replaceAll("M", "-Start\nStart-");
		seqAA = seqAA.replaceAll("R", "-R\nR-");
		FileUtils.saveText(seqAA, DATABASE+"GenomeDB_"+fileName+"_temp.txt");
		ArrayList<String> sequencesTemp;
		sequencesTemp = TabDelimitedTableReader.readList( DATABASE+"GenomeDB_"+fileName+"_temp.txt");
		System.out.println("nb element: "+sequencesTemp.size());
		ArrayList<String> sequences = new ArrayList<String>();
		int k=0;
		for(String sequence : sequencesTemp){
			// parse sequence by finding "-"
			String[] parses = sequence.split("-");
			if(parses.length==3){
				String begin = parses[0];
				String end = parses[2];
				String seq = parses[1];

				if(begin.equals("R") || end.equals("R")){
					int pos = seqAA.indexOf(seq, k);
					if(end.equals("R")){
						seq+="R";
					}
					if(seq.length()>4){
						//double weight = Gene.getMolecularWeight(seq);
						//if(weight<4000 && weight>600){

						sequences.add(">genoDB|"+prefix+"_"+k+"|EGDe "+(pos*3)+".."+((pos+seq.length())*3));
						sequences.add(seq);
						//System.out.println(seq+" "+begin+" "+end);
						//System.out.println(weight);
						//}
						k++;
					}

				}

			}
		}
		String[][] db = new String[(sequences.size())][1];
		int i=0;
		for(String sequence: sequences){
			db[i][0] = sequence;
			i++;
		}
		TabDelimitedTableReader.save(db, DATABASE+"GenomeDB_"+fileName+".fasta");
	}

	
	/**
	 *  Create database of all peptide from a start codon (M=ATG, L=TTG, V=GTG), to the next stop codon or R
	 */
	public static void createTISDB(){
		Chromosome chromosome = Genome.loadEgdeGenome().getFirstChromosome();
		ArrayList<String> peptides = new ArrayList<String>();
		String sequence = chromosome.getSequenceAsString();
		ArrayList<Integer> startOccurences = new ArrayList<Integer>();
		for(int i=0;i<Codon.startCodon.length;i++){
			startOccurences = FileUtils.searchPosition(Codon.startCodon[i][0], sequence);
			getAllPeptides(sequence, startOccurences, peptides,Codon.startCodon[i][1].charAt(0),Codon.startCodon[i][0],2944527,true,10000);
		}
				
		String seqComplement = chromosome.getReverseComplement().getSequenceAsString();
		for(int i=0;i<Codon.startCodon.length;i++){
			startOccurences = FileUtils.searchPosition(Codon.startCodon[i][0], seqComplement);
			getAllPeptides(seqComplement, startOccurences, peptides,Codon.startCodon[i][1].charAt(0),Codon.startCodon[i][0],2944527,false,10000);
		}
		
		TreeSet<String> finalPeptides = new TreeSet<String>();
		for(String peptide : peptides) finalPeptides.add(peptide);
		System.out.println("Found "+peptides.size()+" peptides with "+finalPeptides.size()+" doublon");
		
		/*
		 * Save in a file
		 */
		ArrayList<String> tisDB = new ArrayList<String>();
		int k=1;
		for(String peptide : finalPeptides){
			tisDB.add(">tisDB|peptide"+k+"|EGDe peptide"+k);
			tisDB.add(peptide);
			k++;
		}
		TabDelimitedTableReader.saveList(tisDB, DATABASE+"TISDB.fasta");
	}
	
	/**
	 * Divide TISDB into sequence starting with a M (TISMDB), and other not starting with an M (TISnoMDB).
	 */
	public static void divideTISDB(){
		ArrayList<String> list = TabDelimitedTableReader.readList(DATABASE+"TISDB.fasta");
		ArrayList<String> Mlist = new ArrayList<String>();
		ArrayList<String> noMlist = new ArrayList<String>(); 
		for(int i=0;i<(list.size()-1);i++){
			if(list.get(i+1).charAt(0)=='M'){
				Mlist.add(list.get(i).replaceFirst("tisDB", "tisMDB"));
				Mlist.add(list.get(i+1));
			}else{
				noMlist.add(list.get(i).replaceFirst("tisDB", "tisNoMDB"));
				noMlist.add(list.get(i+1));
			}
			i++;
		}
		TabDelimitedTableReader.saveList(Mlist, DATABASE+"TISMDB.fasta");
		TabDelimitedTableReader.saveList(noMlist, DATABASE+"TISnoMDB.fasta");
	}
	
	
	
	/**
	 * Given a genome sequence, and a list of position of start codon, update a list of peptides accordingly<br>
	 * The new peptide, are the peptide from a start codon to the next R or stop codon
	 * @param sequence
	 * @param startOccurences
	 * @param peptides
	 * @param codon
	 */
	public static void getAllPeptides(String sequence,ArrayList<Integer> startOccurences,ArrayList<String> peptides,char codon,String codonNucleotide, int genomeSize,boolean strand,int cutoff){
		//System.out.println("occurence: "+startOccurences.size()+" with codon: "+codon+"-"+codonNucleotide);
		for(Integer position : startOccurences){
//		for(int i=0;i<10 && i<startOccurences.size();i++){
		//	int position =  startOccurences.get(i);
			int end = position + 500;
			if(end>genomeSize) end = genomeSize;
			String subSeq = sequence.substring(position,end);
			//System.out.println(subSeq.substring(0, 20));
			DNASequence dnaSeq = new DNASequence(subSeq);
			String subSeqAA = dnaSeq.getRNASequence().getProteinSequence().getSequenceAsString();
			//int indexR = subSeqAA.indexOf('R');
			int indexStop = subSeqAA.indexOf('*');
			int indexR = indexStop;
			int index = indexR;
			String subSeqAATemp = subSeqAA.substring(0, index+1);   // keep in memeory the whole aa sequence to verify the results
			if(index==-1 && index==-1){
				//System.err.println(subSeqAA);
			}else{
				if(indexStop!=-1){
					if(indexR==-1){
						index = indexStop;
					}else{
						if(indexR>indexStop){
							index = indexStop;
						}else{
							index++;
						}
					}
				}else{
					index++;
				}
				subSeqAA = subSeqAA.substring(1, index-1);
				if(subSeqAA.length()>(LENGTH_CUTOFF-1)){
					if(subSeqAA.length()<cutoff){
						//System.out.println("M"+subSeqAA+";"+position+";"+strand+";"+codon+";"+codonNucleotide);
						/*
						 * If we are on minus strand working with reverse complement sequence the real position on the genome is : genomeLength-position
						 */
						int genomePos = position;
						char strandChar = '+';
						if(!strand){
							strandChar = '-';
							genomePos = genomeSize - position;
						}
						
						
						peptides.add("M"+subSeqAA+"\t"+genomePos+"\t"+strandChar);
					}
				}
			}
			
			//System.out.println(subSeqAATemp+"  keep: "+subSeqAA);  // verify that we really cut before stop codon
			
		}
	}
	
	/**
	 * Verify TISDB peptides:<br>
	 * <li> Each peptide might have a R at the end, or no R at all
	 * <li> Every EGD-e gene should be in this database
	 */
	public static void verifyTISDB(){
		ArrayList<String> peptides = TabDelimitedTableReader.readList(DATABASE+"TISDB.fasta");
		/*
		 * Check if every peptide has either "no R" or "an R at the end", because no R in the middle of the peptide is allowed
		 */
		ArrayList<String> results = new ArrayList<String>();
//		for(String peptide : peptides){
//			if(!peptide.contains(">")){
//				if(peptide.indexOf('R')>0 && peptide.indexOf('R')<peptide.length()-1){
//					System.err.println(peptide);
//				}
//				if(peptide.indexOf('R')!=-1){
//					results.add(peptide+"\t"+(peptide.length()-peptide.indexOf('R')));
//					System.out.println(peptide+"\t"+(peptide.length()-peptide.indexOf('R')));
//				}
//			}
//		}

		/*
		 * Check if all EGD-e gene start peptide are included in this database
		 */
//		Genome genome = Genome.loadEgdeGenome();
//		for(Gene gene : genome.getFirstChromosome().getGenes().values()){
//			String seqAA = gene.getSequenceAA();
//			if(seqAA.indexOf('R')!=-1) seqAA = seqAA.substring(0, seqAA.indexOf('R')+1);
//			if(seqAA.length()>4){
//				if(peptides.contains(seqAA)){
//					System.out.println(gene.getName()+"\t"+seqAA);
//					results.add(gene.getName()+"\t"+seqAA);
//				}else{
//					System.err.println(gene.getName()+"\tDont contain");
//					results.add(gene.getName()+"\tDont contain");
//				}
//			}
//		}
//		TabDelimitedTableReader.saveList(results, "D:/listPeptide.txt");
	}
	
	public static void createMDB(){
		Chromosome chromosome = Genome.loadEgdeGenome().getFirstChromosome();
		createMDBFasta(chromosome.getRNASequence().getProteinSequence().getSequenceAsString(),"genome", "EGDe");
		String seqComplement = chromosome.getReverseComplement().getSequenceAsString();
		DNASequence chromoComplement = new DNASequence(seqComplement);
		createMDBFasta(chromoComplement.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeCompl", "EGDeCompl");

		String seqTemp = chromosome.getSequenceAsString();
		String seqMinus = seqTemp.charAt(seqTemp.length()-1)+seqTemp.substring(0, seqTemp.length()-1);
		String seqPlus = seqTemp.substring(1, seqTemp.length())+seqTemp.charAt(0);
		DNASequence seq = new DNASequence(seqMinus);
		createMDBFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeM", "EGDeMinus");
		seq = new DNASequence(seqPlus);
		createMDBFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeP", "EGDePlus");

		seqTemp = chromoComplement.getSequenceAsString();
		seqMinus = seqTemp.charAt(seqTemp.length()-1)+seqTemp.substring(0, seqTemp.length()-1);
		seqPlus = seqTemp.substring(1, seqTemp.length())+seqTemp.charAt(0);
		seq = new DNASequence(seqMinus);
		createMDBFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeComplM", "EGDeComplMinus");
		seq = new DNASequence(seqPlus);
		createMDBFasta(seq.getRNASequence().getProteinSequence().getSequenceAsString(),"genomeComplP", "EGDeComplPlus");


		ArrayList<String> ret;
		ret = TabDelimitedTableReader.readList(DATABASE+"MDB_EGDe.fasta");
		for(String line : TabDelimitedTableReader.readList(DATABASE+"MDB_EGDeCompl.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"MDB_EGDeMinus.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"MDB_EGDePlus.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"MDB_EGDeComplMinus.fasta")) ret.add(line);
		for(String line : TabDelimitedTableReader.readList(DATABASE+"MDB_EGDeComplPlus.fasta")) ret.add(line);
		TabDelimitedTableReader.saveList(ret, DATABASE+"MDB.fasta");


	}
	
	/**
	 * From an amino acid sequence of a genome create final fasta file by cutting at each Start, Stop and R positions
	 * @param seqAA
	 * @param prefix
	 * @param fileName
	 */
	private static void createMDBFasta(String seqAA,String prefix,String fileName){
//		String seqAAComplete =seqAA;
		seqAA = seqAA.replace('*', '-');
		seqAA = seqAA.replaceAll("-", "-Stop\nStop-");
		//seqAA = seqAA.replaceAll("M", "-Start\nStart-");
		seqAA = seqAA.replaceAll("R", "-R\nR-");
		FileUtils.saveText(seqAA, DATABASE+"MDB_"+fileName+"_temp.txt");
		ArrayList<String> sequencesTemp;
		sequencesTemp = TabDelimitedTableReader.readList( DATABASE+"MDB_"+fileName+"_temp.txt");
		System.out.println("nb element: "+sequencesTemp.size());
		ArrayList<String> sequences = new ArrayList<String>();
		int k=0;
		for(String sequence : sequencesTemp){
			sequence = sequence.replaceAll("-Stop","");
			sequence = sequence.replaceAll("Stop-","");
			sequence = sequence.replaceAll("-R","R");
			sequence = sequence.replaceAll("R-","R");
			for(int i=0;i<sequence.length();i++){
				String seqTemp = sequence.substring(i);
				if(seqTemp.length()>LENGTH_CUTOFF){
					seqTemp = "M"+seqTemp.substring(1);
					sequences.add(">MDB|"+prefix+"_"+k+"|EGDe ");
					sequences.add(seqTemp);
					k++;
				}
			}
		}
		String[][] db = new String[(sequences.size())][1];
		int i=0;
		for(String sequence: sequences){
			db[i][0] = sequence;
			i++;
		}
		TabDelimitedTableReader.save(db, DATABASE+"MDB_"+fileName+".fasta");
	}
	
	/**
	 * Get all possible peptides from EGD-e: <br>
	 * <li> get all proteins sequence from Small RNAs
	 * <li> get peptide sequence from M start to first R, or first E
	 * <li> cut-off every peptide of size under 5aa
	 * <li> export in a table
	 * <li> use <code>http://web.expasy.org/compute_pi/</code> to calculate mass and isoelectric point
	 * <li> save the different mass in NTermUtils.getPATH()+EGDePeptideMass.txt
	 */
	public static void possibleSmallRNAPeptides(){
//		String[][] array = TabDelimitedTableReader.read(NTermUtils.getPATH()+"Peptide candidates - sRNAs - 20-09-11.txt");
//		String cutPeptide = "E";
		String cutPeptide = "R";
//		for(int i=1;i<array.length;i++){
//			String sequence = array[i][5];
//			if(sequence.indexOf(cutPeptide)!=-1){
//				sequence = sequence.substring(0, sequence.indexOf(cutPeptide)+1);
//			}
//			System.out.println(sequence);
//		}
		
		/*
		 * use <code>http://web.expasy.org/compute_pi/</code> to calculate mass and isoelectric point
		 */
		//String[][] array = TabDelimitedTableReader.read(NTermUtils.getPATH()+"GluC sRNA.txt");
		String[][] array = TabDelimitedTableReader.read(NTermUtils.getPATH()+"Tripsin sRNA.txt");
		ArrayList<String> predicted = new ArrayList<String>();
		String[] headers = {"Sequence","Locus","Theoretical pI","Molecular weight","Decision"};
		String header = "";
		for(String temp : headers) header +=temp+"\t";
		predicted.add(header);
		
		for(int i=1;i<array.length;i++){
			String sequence = array[i][0];
			if(sequence.indexOf(cutPeptide)!=-1){
				sequence = sequence.substring(0, sequence.indexOf(cutPeptide)+1);
			}
			boolean possiblePeptide = false;
			double mass = Double.parseDouble(array[i][2]);
			double pI = Double.parseDouble(array[i][1]);
			if(sequence.length()>5){
				if(mass>700 && mass<5700){
					possiblePeptide = true;
				}
			}
			String[] rows = {sequence,array[i][2],pI+"",mass+"",possiblePeptide+""};
			String row = "";
			for(String temp : rows) row +=temp+"\t";
			predicted.add(row);
		}
		
		TabDelimitedTableReader.saveList(predicted, NTermUtils.getPATH()+"Tripsin sRNA curate.txt");
	}
	
	/**
	 * Extract statistics from existing databases, number of lmos, sRNA, utr...
	 */
	public static void statDatabase(){
		Genome genome = Genome.loadEgdeGenome();
		TreeSet<String> missLmo = new TreeSet<>();
		for(Gene gene : genome.getFirstChromosome().getGenes().values()){
			missLmo.add(gene.getName());
		}
		ArrayList<String> list = TabDelimitedTableReader.readList(DATABASE+"Francis_ProteinUtrDB.fasta");
		ArrayList<String> filterList = new ArrayList<>();
		for(int i = 0; i< list.size();i++){
			String row = list.get(i);
			if(row.startsWith(">")){
				//System.out.println(row);
				i++;
				filterList.add(row+"\t"+list.get(i));
				String yo = row.replace("|", "_");
				
				if(yo.split("_")[0].equals(">protDB")){
					String locus = yo.split("_")[1];
					if(locus.contains("lmo")){
						if(missLmo.contains(locus)){
							missLmo.remove(locus);
						}
					}
				}
			}
		}
		
		for(String gene : missLmo){
			System.out.println(gene);
		}
		TabDelimitedTableReader.saveList(filterList, DATABASE+"Francis_ProteinUtrDB_Stat.txt");
		
	}
}
