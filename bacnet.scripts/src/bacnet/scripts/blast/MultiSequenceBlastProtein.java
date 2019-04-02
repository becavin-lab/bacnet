package bacnet.scripts.blast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.BufferedReaderBytesRead;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import bacnet.Database;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;
import bacnet.utils.VectorUtils;

public class MultiSequenceBlastProtein {

    public static String PATH_GENOMES_LIST = "";
    
	public static void run(String fileName, boolean smallORF, double evalueCutoff,String pathGenome, String listBacteriaFileName,boolean best){
		
//		/*
//		 * Set-up variables
//		 */
		String suffix = "";
		if(smallORF) suffix = ".SmallORF";
		ArrayList<String> orfs = new ArrayList<String>();
		if(FileUtils.getExtension(fileName).equals(".txt")){
			HashMap<String, String> nameToAminoAcidSequence = TabDelimitedTableReader.readHashMap(fileName);
			for(String key : nameToAminoAcidSequence.keySet()){
				orfs.add(key+";"+nameToAminoAcidSequence.get(key));
				System.out.println(key+";"+nameToAminoAcidSequence.get(key));
				File file = new File(Database.getTEMP_PATH()+key+"/");
				file.mkdir();
			}
		}else if(FileUtils.getExtension(fileName).equals(".fasta")){
			try {
				FileInputStream inStream = new FileInputStream( fileName );
				FastaReader<ProteinSequence,AminoAcidCompound> fastaReader = 
				new FastaReader<ProteinSequence,AminoAcidCompound>(
						inStream, 
						new NCBIFastaHeaderParser<ProteinSequence,AminoAcidCompound>(), 
						new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
				LinkedHashMap<String, ProteinSequence> genomeSequences = fastaReader.process();
				for(String key : genomeSequences.keySet()){
					orfs.add(key+";"+genomeSequences.get(key).getSequenceAsString());
					System.out.println(key+";"+genomeSequences.get(key).getSequenceAsString());
					File file = new File(Database.getTEMP_PATH()+key+"/");
					file.mkdir();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		 * Create Blast database
		 */
		BlastDatabase.createDatabaseForORF(Database.getInstance().getGenomeArrayPath());
		
		
		/*
		 * Blast sequences
		 */
		blastORF(orfs,suffix,pathGenome,listBacteriaFileName,evalueCutoff);
		// blastGenome
		
		/*
		 * Extract info and summarize in one table
		 */
		extractInfoORF(orfs, evalueCutoff, suffix, listBacteriaFileName, smallORF,best);
		//extractLiteInfoORF(orfs, evalueCutoff, suffix, listBacteriaFileName);
		
		
	}
	
	/**
	 * On each genome, blastP ORF
	 * @param smallORF
	 * @param geneLeftEGDe
	 * @param geneRightEGDe
	 */
	private static void blastORF(ArrayList<String> oRFs,String suffix,String pathGenome, String listBacteriaFileName,double evalueCutoff){
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(listBacteriaFileName);
//		final ArrayList<String> listBacteria = new ArrayList<>();
//		listBacteria.add("Vibrio_brasiliensis_LMG_20546");
		
		ExecutorService executor = Executors.newFixedThreadPool(2*Runtime.getRuntime().availableProcessors());
		for(String oRFTemp : oRFs){
			//for(int k=1;k<10;k++){
			for(String genomeNameTemp : listBacteria){
				final String genomeName = genomeNameTemp;
				final String orf = oRFTemp;
				final String suffixFinal = suffix;
				
				/*
				 * Create thread
				 */
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							String database = pathGenome+genomeName+File.separator+genomeName+suffixFinal;
							//File file = new File(GenomeNCBITools.PATH_NCBI_WIN+genomeName+File.separator+genomeName+".phr");
							//if(file.length()>10){  // Database exists
							String name = orf.split(";")[0];
							String seq = orf.split(";")[1];
							//System.out.println(name+" "+seq);
							Blast.alignProtein(name,seq,database,evalueCutoff);
							//}
						}catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				};
				executor.execute(runnable);
			}
		}

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception");
		}
		System.err.println("All Threads done");
	}
	
	/**
	 * Create a text file with all results for each genome and orf
	 * 
	 * @param oRFs
	 * @param evalueCutoff
	 * @param suffix
	 * @param smallORF
	 * @param best true when only the best alignment is extracted
	 */
	private static void extractInfoORF(ArrayList<String> oRFs,double evalueCutoff,String suffix,String listBacteriaFileName,boolean smallORF,boolean best){
		//final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(listBacteriaFileName);
		HashMap<String, String> listFastaFiles = new HashMap<String, String>();
//		if(!smallORF){
//			for(String genomeName : listBacteria){
//				File file = new File(GenomeNCBITools.PATH_NCBI_WIN+genomeName+"/"+genomeName+suffix+".faa");
//				String fasta = "";
//				try {
//					fasta = org.apache.commons.io.FileUtils.readFileToString(file);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.out.println("Loaded "+genomeName);
//				listFastaFiles.put(genomeName, fasta);
//					
//			}
//		}
		
		String[] headers = {"ORF","Strain","hit_accession","eValue","identities","ident","bitScore","hit_id","blt.hit_def","hit_seq","ORF_seq"};
		String header = "";
		for(String headerTemp : headers) header+=headerTemp+"\t"; 
		
		for(String orf : oRFs){
			String orfName = orf.split(";")[0];
			ArrayList<String> finalResult = new ArrayList<>();
			finalResult.add(header);
			for(String genomeName : listBacteria){
				/*
				 * Summarize in a tablefor every genome
				 */
				ArrayList<String> seqNames = new ArrayList<>();
				seqNames.add(orfName);
				Blast.extractInfo(seqNames,genomeName,suffix,evalueCutoff,best);
				
				/*
				 * Regroup all genome blast and get Gene sequence
				 */
				ArrayList<String> blastHit = TabDelimitedTableReader.readList(Database.getTEMP_PATH()+orfName+"/Blast/resultBlast_"+orfName+"_"+genomeName+suffix+".txt",true);
				
				System.out.println(orfName+"  "+genomeName);
				int length = blastHit.size();
				if(best && length>2) length = 2;
				for(int i=1;i<length;i++){
					String row = orfName +"\t" +genomeName+"\t"+blastHit.get(i);
					System.out.println(row);
					String hitName = row.split("\t")[7];
					if(smallORF){
						/*
						 * Find sequence of the sORF
						 */
						String sequence = " ";
						if(!orfName.equals("")){
							System.out.println(genomeName+" "+orfName);
							sequence = findSmallORFSequence(hitName,genomeName);
							System.out.println("Found ORF:"+hitName+" seq:"+sequence);
							row += "\t"+sequence;
						}
						finalResult.add(row);
						
					}else{
						if(listFastaFiles.containsKey(genomeName)){
							boolean found = false;
							for(String rowFasta : listFastaFiles.get(genomeName).split(">")){
								if(rowFasta.contains(hitName) && !found){
									String sequence = rowFasta.substring(hitName.length()).trim().replaceAll("\n","").replaceAll("\r","");
									System.out.println("seq:"+sequence);
									row += "\t"+sequence;
									found = true;
								}
							}
						}else row += "\t"+" ";
						finalResult.add(row);
					}
				}
				
				if(blastHit.size()==1){
					String[] rows = {orfName,genomeName,"","","","","","","","",""};
					String row = "";
					for(String rowTemp : rows) row+=rowTemp+"\t"; 
					finalResult.add(row);
				}
			}
			/*
			 * Save table with all blast results
			 */
			String finalName = Database.getTEMP_PATH()+"/"+orfName+"/"+orfName+"_BlastResult.excel";
			if(!best){
				finalName = Database.getTEMP_PATH()+"/"+orfName+"/"+orfName+"_BlastResult_All.excel";
			}
			TabDelimitedTableReader.saveList(finalResult, finalName);
		}
		//TabDelimitedTableReader.saveList(finalResult, Database.getTEMP_PATH()+/fileName+"_BlastResult.excel");
	}
	
	/**
	 * Go through fasta file and extract sequence
	 * @param hitName
	 * @param genomeName
	 * @return
	 */
	public static String findSmallORFSequence(String hitName, String genomeName){
		try {
			File fileFaa = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa");
			FileInputStream fi = new FileInputStream(fileFaa);
			InputStreamReader isr = new InputStreamReader(fi);
			BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

			String line = br.readLine();
			// Read the lines and put them in ArrayList
			while(line!=null){
				if(line.startsWith(">")){
					if(line.contains(hitName)){
						String sequence = br.readLine();
						br.close();
						isr.close();
						//If stream was created from File object then we need to close it
						if (fi != null) {
							fi.close();
						}
						return sequence;
					}
				}
				line = br.readLine();
			}
			br.close();
			isr.close();
			//If stream was created from File object then we need to close it
			if (fi != null) {
				fi.close();
			}
			return " ";
		} catch (Exception e) {
			System.err.println("Cannot read:"+GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".SmallORF.faa");
			return " ";
		}
	}
	
	/**
	 * Go through fasta file and extract sequence
	 * @param hitName
	 * @param genomeName
	 * @return
	 */
	public static String findORFSequence(String hitName, String genomeName){
		try {
			File fileFaa = new File(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".ORF.faa");
			FileInputStream fi = new FileInputStream(fileFaa);
			InputStreamReader isr = new InputStreamReader(fi);
			BufferedReaderBytesRead br = new BufferedReaderBytesRead(isr);

			String line = br.readLine();
			// Read the lines and put them in ArrayList
			while(line!=null){
				if(line.startsWith(">")){
					if(line.contains(hitName)){
						String sequence = br.readLine();
						String seqline = br.readLine();
						while(!seqline.contains(">")){
							sequence+=seqline;
							seqline = br.readLine();
						}
						br.close();
						isr.close();
						//If stream was created from File object then we need to close it
						if (fi != null) {
							fi.close();
						}
						return sequence;
					}
				}
				line = br.readLine();
			}
			br.close();
			isr.close();
			//If stream was created from File object then we need to close it
			if (fi != null) {
				fi.close();
			}
			return " ";
		} catch (Exception e) {
			System.err.println("Cannot read:"+GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+".ORF.faa");
			return " ";
		}
	}
	
	/**
	 * Extract very few info about the hits
	 * @param oRFs
	 * @param evalueCutoff
	 * @param suffix
	 * @param listBacteriaFileName
	 * @param smallORF
	 * @param best
	 */
	private static void extractLiteInfoORF(ArrayList<String> oRFs,double evalueCutoff,String suffix,String listBacteriaFileName){
		//final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(GenomeNCBIFolderTools.PATH_GENOMES_LIST);
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(listBacteriaFileName);
		HashMap<String, String> listFastaFiles = new HashMap<String, String>();
//		if(!smallORF){
//			for(String genomeName : listBacteria){
//				File file = new File(GenomeNCBITools.PATH_NCBI_WIN+genomeName+"/"+genomeName+suffix+".faa");
//				String fasta = "";
//				try {
//					fasta = org.apache.commons.io.FileUtils.readFileToString(file);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.out.println("Loaded "+genomeName);
//				listFastaFiles.put(genomeName, fasta);
//					
//			}
//		}
		
		String[] headers = {"ORF","Strain","hit_accession","eValue","identities","blt.hit_def"};
		String header = "";
		for(String headerTemp : headers) header+=headerTemp+"\t"; 
		
		for(String orf : oRFs){
			String orfName = orf.split(";")[0];
			ArrayList<String> finalResult = new ArrayList<>();
			finalResult.add(header);
			for(String genomeName : listBacteria){
				/*
				 * Summarize in a tablefor every genome
				 */
				ArrayList<String> seqNames = new ArrayList<>();
				seqNames.add(orfName);
				//Blast.extractInfo(seqNames,genomeName,suffix,evalueCutoff,true);
				
				/*
				 * Regroup all genome blast and get Gene sequence
				 */
				ArrayList<String> blastHit = TabDelimitedTableReader.readList(Database.getTEMP_PATH()+orfName+"/Blast/resultBlast_"+orfName+"_"+genomeName+suffix+".txt",true);
				
				String[][] orfList = new String[0][0];
				System.out.println(orfName+"  "+genomeName);
				
				int length = blastHit.size();
				for(int i=1;i<length;i++){
					String[] hits = blastHit.get(i).split("\t");
					String row = orfName +"\t" +genomeName+"\t"+hits[5]+"\t"+hits[1]+"\t"+hits[2]+"\t"+hits[6];
					System.out.println(row);
					finalResult.add(row);
				}				
				if(blastHit.size()==1){
					String[] rows = {orfName,genomeName,"","","",""};
					String row = rows[0]+"\t"+rows[1]+"\t"+rows[4]+"\t"+rows[5];
					finalResult.add(row);
				}
			}
			/*
			 * Save table with all blast results
			 */
			TabDelimitedTableReader.saveList(finalResult, Database.getTEMP_PATH()+"/"+orfName+"/"+orfName+"_BlastResult_Lite.excel");
		}
		//TabDelimitedTableReader.saveList(finalResult, Database.getTEMP_PATH()+/fileName+"_BlastResult.excel");
	}
	
	/**
	 * Filtering table of results by e-value
	 */
	public static void filterSmallORF(ArrayList<String> smallORFs,double eValueCutoff){
		for(String smallORF : smallORFs){
			String name = smallORF.split(";")[0];
			String seq = smallORF.split(";")[1];
				String[][] summary = TabDelimitedTableReader.read(Database.getTEMP_PATH()+"/"+name+"/Summary-"+name+".excel");
				ArrayList<String> hitList = new ArrayList();
				ArrayList<String> summaryLite = new ArrayList();
				for(int i=1;i<summary.length;i++){
					if(!summary[i][1].equals("")){
						String genomeNCBI = summary[i][0];
						double eValue = Double.parseDouble(summary[i][2]);
						if(eValue<eValueCutoff){
							hitList.add(">"+genomeNCBI);
							hitList.add(summary[i][6]);
							
							String row = "";
							for(int j=0;j<summary[0].length;j++) row+=summary[i][j]+"\t";
							row+=genomeNCBI.split("_")[0]+"_"+genomeNCBI.split("_")[1];
							summaryLite.add(row);
						}
					}
				}
				TabDelimitedTableReader.saveList(hitList, Database.getTEMP_PATH()+"/"+name+"/Summary-"+name+"-Sequences.fasta");
				TabDelimitedTableReader.saveList(summaryLite, Database.getTEMP_PATH()+"/"+name+"/Summary-"+name+"-Lite.excel");
		}
	}
	
	/**
	 * Summarize blast result in a table
	 * @param smallORFs
	 * @param evalueCutoff
	 * @param bestHit
	 */
	private static void extractInfoSmallORF(ArrayList<String> smallORFs,double evalueCutoff,boolean bestHit){
		final ArrayList<String> listBacteria = TabDelimitedTableReader.readList(PATH_GENOMES_LIST);
		//for(int i=1;i<11;i++){
		ArrayList<String> hitList = new ArrayList();
		ArrayList<String> firstResult = new ArrayList<>();
		String[] headers = {"Strain","Hit id","evalue","identities","bitscore","hit seq","orfSequence"};
		String header = "";
		for(String headerTemp : headers) header+=headerTemp+"\t"; 
		firstResult.add(header);
		for(String orf : smallORFs){
			String name = orf.split(";")[0];
			String seq = orf.split(";")[1];
			for(String genomeName : listBacteria){
				ArrayList<String> seqNames = new ArrayList<>();
				seqNames.add(name);
				Blast.extractInfo(seqNames,genomeName,".SmallORF",evalueCutoff,bestHit);
				String[] bestHitSmallORFs = extractMinimumInfo(name,genomeName,".SmallORF");
				if(!bestHitSmallORFs[0].equals("")){
					/*
					 * Find sequence of the sORF
					 */
					String[][] orfList = TabDelimitedTableReader.read(GenomeNCBITools.PATH_NCBI_BacGenome+genomeName+File.separator+genomeName+"_Info.excel");
					String row = genomeName+"\t"+VectorUtils.toString(bestHitSmallORFs);
					String orfName = bestHitSmallORFs[0];
					String sequence = "";
					if(!orfName.equals("")){
						System.out.println(genomeName+" "+orfName);
						for(int k=1;k<orfList.length;k++){
							if(orfList[k][4].equals(orfName)){
								sequence = orfList[k][0];
								System.out.println("Found ORF:"+orfName+" seq:"+sequence);
							}
						}
						//if(!hitList.contains(sequence)){
						hitList.add(">"+genomeName+"|"+bestHitSmallORFs[0]);
						hitList.add(sequence);
						//}
					}
					firstResult.add(row+sequence);
				}
				
			}
			TabDelimitedTableReader.saveList(hitList, Database.getTEMP_PATH()+"/"+name+"/Summary-"+name+"-Sequences.txt");
			TabDelimitedTableReader.saveList(firstResult, Database.getTEMP_PATH()+"/"+name+"/Summary-"+name+".excel");
		}
	}
	
	/**
	 * Extract only the result with best evalue
	 * @param seqNames
	 * @return
	 */
	private static String[] extractMinimumInfo(String seqNames,String genomeName,String suffix){
		double evalueMin = 1000000;
		int indexMinEvalue = -1;
		String[][] bltResults = TabDelimitedTableReader.read(Database.getTEMP_PATH()+seqNames+"/Blast/resultBlast_"+seqNames+"_"+genomeName+suffix+".txt");
		for(int k=1;k<bltResults.length;k++){
			double evalue = Double.parseDouble(bltResults[k][1]);
			if(evalue<evalueMin){
				evalueMin = evalue;
				indexMinEvalue = k;
			}
		}
		if(indexMinEvalue!=-1){
			String[] rowTemp = ArrayUtils.getRow(bltResults, indexMinEvalue);
			String[] row = {rowTemp[6],rowTemp[1],rowTemp[2],rowTemp[4],rowTemp[7]};
			return row;
		}else{
			String[] result = {"","","","",""};
			return result;
		}
	}
	
		
}
