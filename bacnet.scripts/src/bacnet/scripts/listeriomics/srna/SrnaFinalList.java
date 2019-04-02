package bacnet.scripts.listeriomics.srna;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.FileUtils;
import bacnet.utils.UNAfold;
import bacnet.utils.XMLUtils;

public class SrnaFinalList {

	/**
	 * Read the different :list from different publication and combine all of them into an Srna final list<br>
	 * Every table should be found in   SrnaTables.PATH
	 * @throws IOException
	 */
	public static void createFinalList() {

		ArrayList<Srna> toledo = SrnaTables.getToledoInfo();
		ArrayList<Srna> mraheil = SrnaTables.getMraheilInfo();
		ArrayList<Srna> rfam = SrnaTables.getRfamInfo();
		ArrayList<Srna> wurtzel = SrnaTables.getWurtzelInfo();
		ArrayList<Srna> johansson = SrnaTables.getJohanssonInfo();
		//		ArrayList<Srna> jeff = SrnaTables.getJeffInfo();


		// combine all the list
		HashMap<String, ArrayList<Srna>> listSrnas = new HashMap<String, ArrayList<Srna>>();
		for(Srna sRNA : toledo){
			boolean found = false;
			for(String key : listSrnas.keySet()){
				Srna sRNA1 = listSrnas.get(key).get(0); 
				if(sRNA1.isEqual(sRNA, 40)){
					listSrnas.get(key).add(sRNA);
					found = true;
				}
			}
			if(!found){
				ArrayList<Srna> sRnas = new ArrayList<Srna>();
				sRnas.add(sRNA);
				listSrnas.put(sRNA.getName()+"-"+sRNA.getId(), sRnas);
			}
		}
		combineSrnaLists(listSrnas, mraheil);
		combineSrnaLists(listSrnas, wurtzel);
		combineSrnaLists(listSrnas, rfam);
		combineSrnaLists(listSrnas, johansson);
		//		combineSrnaLists(listSrnas, jeff);
		/* 
		 * add the list of sRNA from Oliver which have been modified,
		 * for each of the rli not in the other lists we align on EGDe, and create a new table with EGDe position of each sRNA
		 * Each new Srnas strand is curated by hand directly in the table
		 * Thanks to  :     Srna10403SOliver.findEGDePosition();
		 * 
		 * sRNA Oliver-1 aligned.txt  ---->   sRNA Oliver 2009-1 EGDe position.txt
		 */
		ArrayList<Srna> oliverEGDe = SrnaTables.getOliverInfoEGDePosition();
		combineSrnaLists(listSrnas, oliverEGDe);

		displayListSrnas(listSrnas);

		// verif that every srna has been included
		int countMap = 0;
		ArrayList<String> sRNAsMap = new ArrayList<String>();
		for(String key : listSrnas.keySet()){
			for(Srna sRNA : listSrnas.get(key)){
				sRNAsMap.add(sRNA.getName());
			}
		}
		// count the total number of sRNA we have
		ArrayList<String> sRNAsrawData = new ArrayList<String>();
		for(Srna sRNA : toledo) sRNAsrawData.add(sRNA.getName());
		for(Srna sRNA : mraheil) sRNAsrawData.add(sRNA.getName());
		for(Srna sRNA : wurtzel) sRNAsrawData.add(sRNA.getName());
		for(Srna sRNA : rfam) sRNAsrawData.add(sRNA.getName());
		for(Srna sRNA : oliverEGDe) sRNAsrawData.add(sRNA.getName());
		System.err.println("c: "+sRNAsMap.size()+" "+sRNAsrawData.size());
		//		TabDelimitedTableReader.saveList(sRNAsMap, "D:/map.txt");
		//		TabDelimitedTableReader.saveList(sRNAsrawData, "D:/rawData.txt");

		//displayListSrnas(listSrnas);

		/*
		 *  curate the list using sRNA summaryTable.txt
		 */
		try {
			listSrnas = curateList(listSrnas);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(listSrnas.size());

		/*
		 *  create final list of sRNA using sRNA reference.txt and EGDe annotation.txt
		 */
		ArrayList<Srna> finalList = createTotalList(listSrnas);
		System.err.println(finalList.size()+"    "+listSrnas.size());

		// add info from Oliver (RNASeq in 10403S)
		//		ArrayList<Srna> oliver = SrnaTables.getOliverInfo();
		//		addOliverInfo(finalList,oliver);

		// save all Srna into an XML file
		File path = new File(Srna.PATHSerialize);
		path.mkdir();
		path = new File(Srna.PATHXML);
		path.mkdir();
		for(Srna sRNA : finalList){
			sRNA.save(Srna.PATHSerialize+File.separator+sRNA.getName()+".srna");
			XMLUtils.encodeToFile(sRNA, Srna.PATHXML+File.separator+sRNA.getId()+"-"+sRNA.getName()+".xml");
		}
	}

	//	/**
	//	 * Read all sRNA in Oliver table and extract all sRNA which are not present in EGDe lists
	//	 * @throws IOException
	//	 */
	//	public static void createFinalList10403S() throws IOException{
	//		ArrayList<Srna> oliver = SrnaTables.getOliverInfo();
	//		ArrayList<Srna> allSrnas = Srna.getAllSrnas();
	//		ArrayList<Srna> only10403S = new ArrayList<Srna>();
	//		for(Srna sRNA : oliver){
	//			if(sRNA.getTypeSrna()==TypeSrna.sRNA){
	//				boolean found = false;
	//				for(Srna sRNA2 : allSrnas){
	//					if(sRNA.getName().equals(sRNA2.getName()) || sRNA2.getSynonym().contains(sRNA.getName())){
	//						found = true;
	//						System.out.println(sRNA.getName()+"  "+sRNA2.getName());
	//					}
	//				}
	//				if(!found) only10403S.add(sRNA);
	//			}
	//		}
	//		// save all Srna into an XML file
	//		for(Srna sRNA : only10403S){
	//			XMLUtils.encodeToFile(sRNA, Srna.PATHXML_10403S+sRNA.getId()+"-"+sRNA.getName()+".xml");
	//		}
	//	}

	/**
	 * Add the list of Srna contained in lst2 to the HashMap listSrnas<br>
	 * These function use the method sRNA1.isEqual(sRNA2, 40) to detect if two sRNAs are the same or not
	 * @param listSrnas
	 * @param list2
	 */
	private static void combineSrnaLists(HashMap<String, ArrayList<Srna>> listSrnas, ArrayList<Srna> list2){
		for(Srna sRNA2 : list2){
			boolean found = false;
			for(String key : listSrnas.keySet()){
				for(int i=0;i<listSrnas.get(key).size() && !found;i++){
					Srna sRNA1 = listSrnas.get(key).get(i);
					if(sRNA1.isEqual(sRNA2, 40)){
						listSrnas.get(key).add(sRNA2);
						found = true;
					}
				}
			}
			if(!found){
				ArrayList<Srna> sRnas = new ArrayList<Srna>();
				sRnas.add(sRNA2);
				listSrnas.put(sRNA2.getName()+"-"+sRNA2.getId(), sRnas);
			}
		}
	}

	/**
	 * Load sRNA summaryTable and curate annotation of sRNA which are in it<br>
	 * Use sRNA summaryTable
	 * @param listSrnas
	 * @throws IOException 
	 */
	private static HashMap<String, ArrayList<Srna>> curateList(HashMap<String, ArrayList<Srna>> listSrnas) throws IOException{
		String[][] curateTable = TabDelimitedTableReader.read(SrnaTables.PATH+"InfoForTableCuration/sRNA summaryTable.txt");
		// first create new list of sRNAs
		HashMap<String, ArrayList<Srna>> listSrnasTemp = new HashMap<String, ArrayList<Srna>>();
		for(int i=0;i<curateTable.length;i++){
			ArrayList<Srna> sRNAs = new ArrayList<Srna>();
			listSrnasTemp.put(curateTable[i][1], sRNAs);
		}

		// then combine all sRNA corresponding to one name in an ArrayList
		HashMap<String,String> nameToRefandType = new HashMap<String, String>();
		for(int i=0;i<curateTable.length;i++){
			if(listSrnas.containsKey(curateTable[i][4])){
				ArrayList<Srna> sRNAs = listSrnas.get(curateTable[i][4]);
				for(Srna sRNA : sRNAs){
					listSrnasTemp.get(curateTable[i][1]).add(sRNA);
				}
				// link name of the sRNA to the reference Srna we need to use as a reference for position
				nameToRefandType.put(curateTable[i][1], curateTable[i][2]+";"+curateTable[i][3]+";"+curateTable[i][0]);
			}else{
				System.err.println(curateTable[i][4]);
			}
		}

		// final step: put the sRNA selected as a reference in the first position in the list
		HashMap<String, ArrayList<Srna>> listSrnasNew = new HashMap<String, ArrayList<Srna>>();
		for(String name : listSrnasTemp.keySet()){
			ArrayList<Srna> sRNAs = listSrnasTemp.get(name);
			ArrayList<Srna> sRNAsNew = new ArrayList<Srna>();
			String refToUse = nameToRefandType.get(name).split(";")[0];
			String typeToUse = nameToRefandType.get(name).split(";")[1];
			String idToUse = nameToRefandType.get(name).split(";")[2];
			// the first element should be the one to use for reference (position, strand, type, etc)
			for(Srna sRNA : sRNAs){
				if(sRNA.getFoundIn().get(0).equals(refToUse)){
					// set Type
					sRNA.setTypeSrna(TypeSrna.valueOf(typeToUse));
					// set ID
					sRNA.setId(idToUse);
					sRNAsNew.add(sRNA);
					//System.out.println("found "+sRNA.getFoundIn().get(0));
				}
			}
			// then add the others
			for(Srna sRNA : sRNAs){
				if(!sRNA.getFoundIn().get(0).equals(refToUse)){
					sRNAsNew.add(sRNA);
					//System.out.println("found "+sRNA.getFoundIn().get(0));
				}
			}
			listSrnasNew.put(name, sRNAsNew);
		}

		return listSrnasNew;
	}


	/**
	 * Combine all List of sRNA regrouped by "same center" into a general List of Srna <br>
	 * Use sRNA reference.txt
	 * <br>
	 * And EGDe annotation.txt
	 * @param listSrnas
	 * @return
	 */
	private static ArrayList<Srna> createTotalList(HashMap<String, ArrayList<Srna>> listSrnas){
		ArrayList<Srna> finalList = new ArrayList<Srna>();
		int found = 0;
		for(String key : listSrnas.keySet()){
			ArrayList<Srna> listRNA = listSrnas.get(key);
			Srna sRNA = createSrna(listRNA, key);
			for(Srna sRNATemp : listRNA){
				// add position information has attributes
				String foundIn = sRNATemp.getFoundIn().get(0);
				if(sRNA.getFeatures().containsKey("name ("+foundIn+")")){
					sRNA.getFeatures().put("name ("+foundIn+"-2)",sRNATemp.getName()+"");
					sRNA.getFeatures().put("from ("+foundIn+"-2)",sRNATemp.getBegin()+"");
					sRNA.getFeatures().put("to ("+foundIn+"-2)",sRNATemp.getEnd()+"");
					sRNA.getFeatures().put("length ("+foundIn+"-2)",sRNATemp.getLength()+"");
					sRNA.getFeatures().put("strand ("+foundIn+"-2)",sRNATemp.isStrand()+"");
					sRNA.getFeatures().put("type ("+foundIn+"-2)",sRNATemp.getType()+"");

					// add attributes
					for(String attribute : sRNATemp.getFeatures().keySet()){
						sRNA.getFeatures().put(attribute.replaceAll("\\)","-2\\)"), sRNATemp.getFeatures().get(attribute));
					}
					// update found in
					sRNA.getFoundIn().add(foundIn+"-2");
				}else{
					sRNA.getFeatures().put("name ("+foundIn+")",sRNATemp.getName()+"");
					sRNA.getFeatures().put("from ("+foundIn+")",sRNATemp.getBegin()+"");
					sRNA.getFeatures().put("to ("+foundIn+")",sRNATemp.getEnd()+"");
					sRNA.getFeatures().put("length ("+foundIn+")",sRNATemp.getLength()+"");
					sRNA.getFeatures().put("strand ("+foundIn+")",sRNATemp.isStrand()+"");
					sRNA.getFeatures().put("type ("+foundIn+")",sRNATemp.getType()+"");

					// add attributes
					for(String attribute : sRNATemp.getFeatures().keySet()){
						sRNA.getFeatures().put(attribute, sRNATemp.getFeatures().get(attribute));
					}
					// update found in
					sRNA.getFoundIn().add(foundIn);
				}
			}
			// create synonym list
			for(String feature : sRNA.getFeatures().keySet()){
				if(feature.indexOf("name")!=-1){
					String synonym = sRNA.getFeatures().get(feature);
					if(!sRNA.getName().equals(synonym) && !synonym.equals("")){
						sRNA.getSynonym().add(synonym);
					}
				}
			}


			// create ref name
			String[][] reference = TabDelimitedTableReader.read(SrnaTables.PATH+"InfoForTableCuration/sRNA reference.txt");
			for(int i=0;i<reference.length;i++){
				if(reference[i][1].equals(sRNA.getName())){
					sRNA.setRef(reference[i][2]);
				}
			}

			finalList.add(sRNA);
		}
		System.err.println("Found lmo surrounding for "+found+" sRNAs");
		return finalList;
	}

	/**
	 * Given a list of Srna, this function create a Srna which have the right fixe attributes
	 * @param listRNA
	 * @return
	 */
	private static Srna createSrna(ArrayList<Srna> listRNA,String name){
		// have to decipher from, to , strand and length using all the data
		// we always need to have from < to
		int from = listRNA.get(0).getBegin();
		int to = listRNA.get(0).getEnd();
		if(from==ExpressionMatrix.MISSING_VALUE){
			from = to - (Srna.DEFAULT_LENGTH-1);
		}else if(to==ExpressionMatrix.MISSING_VALUE){
			if(listRNA.get(0).isStrand()){
				to = from + (Srna.DEFAULT_LENGTH-1);
			}else{
				to = from;
				from = from - (Srna.DEFAULT_LENGTH-1);
			}
		}else if(from>to){
			int temp = from;
			from = to;
			to = temp;
		}

		Srna sRNA = new Srna(name, from, to);
		sRNA.setStrand(listRNA.get(0).getStrand());
		sRNA.setLength(Math.abs(sRNA.getBegin()-sRNA.getEnd())+1);
		sRNA.setType(listRNA.get(0).getType());
		sRNA.setTypeSrna(listRNA.get(0).getTypeSrna());
		sRNA.setId(listRNA.get(0).getId());
		return sRNA;
	}

	/**
	 * Add has a final step the information coming from oliver et al. 2009 information in 10403S strain
	 * @param finalList
	 * @param oliver
	 */
	private static void addOliverInfo(ArrayList<Srna> finalList, ArrayList<Srna> oliver){
		for(Srna sRNA : finalList){
			for(Srna sRNAOliver : oliver){
				if(sRNA.getName().equals(sRNAOliver.getName())){
					// if we found an sRNA in both list
					String foundIn = sRNAOliver.getFoundIn().get(0);
					sRNA.getFeatures().put("name ("+foundIn+")",sRNAOliver.getName()+"");
					sRNA.getFeatures().put("from ("+foundIn+")",sRNAOliver.getBegin()+"");
					sRNA.getFeatures().put("to ("+foundIn+")",sRNAOliver.getEnd()+"");
					sRNA.getFeatures().put("length ("+foundIn+")",sRNAOliver.getLength()+"");
					sRNA.getFeatures().put("strand ("+foundIn+")",sRNAOliver.isStrand()+"");
					sRNA.getFeatures().put("type ("+foundIn+")",sRNAOliver.getType()+"");

					// add attributes
					for(String attribute : sRNAOliver.getFeatures().keySet()){
						sRNA.getFeatures().put(attribute, sRNAOliver.getFeatures().get(attribute));
					}
					// update found in
					if(!sRNA.getFoundIn().contains(foundIn)) sRNA.getFoundIn().add(foundIn);
				}
			}
		}
	}

	private static void displayListSrnas(HashMap<String, ArrayList<Srna>> listSrnas){
		String text = "";
		for(String key : listSrnas.keySet()){
			String ret=key+"\t"+listSrnas.get(key).size()+"\t";
			for(Srna sRNA : listSrnas.get(key)){
				ret+=sRNA.getName()+" "+sRNA.getBegin()+" "+sRNA.getEnd()+" "+sRNA.isStrand()+" : "+sRNA.getFoundIn().get(0)+" : "+sRNA.getTypeSrna()+"\t";
			}
			text+=ret+"\n";
			System.out.println(ret);
		}
		FileUtils.saveText(text, SrnaTables.PATH+"AllTogether.txt");
	}
	
	/**
	 * Create SUmmary Tables for the website<br>
	 * WARNING: GenomeConversion has to be run before! So we can access to the Srna through <code>Genome.loadEgdeGenome()</code>;
	 */
	public static void createSummaryTables(){
		Genome genome = Genome.loadEgdeGenome();
		ArrayList<String> finalList = new ArrayList<String>();
		String[] headersVector ={"Id","Name","From","To","Strand","Length","Synonim","TSS","First described in","Also described in"};
		String header = "";
		for(String temp: headersVector){
			header+=temp+"\t";
		}
		finalList.add(header);
		for(int i=0;i<Srna.getSrnaOrder().size();i++){
			Srna sRNA = genome.getFirstChromosome().getsRNAs().get(Srna.getSrnaOrder().get(i));
			String wurtzel = "";
			if(sRNA.getFoundInText().contains("Wurtzel et al. 2012")) wurtzel = "Yes";
			String[] rows ={i+"",sRNA.getName(),sRNA.getBegin()+"",sRNA.getEnd()+"",sRNA.getStrand()+"",
					sRNA.getLength()+"",sRNA.getSynonymsText()+"",wurtzel,sRNA.getRef(),sRNA.getFoundInText()};
			String row = "";
			for(String temp: rows){
				row+=temp+"\t";
			}
			finalList.add(row);
		}
		TabDelimitedTableReader.saveList(finalList, Srna.PATHTABLE_Srna);
		
		finalList.clear();
		finalList.add(header);
		int i=1;
		for(String name : genome.getFirstChromosome().getAsRNAs().keySet()){
			Srna sRNA = genome.getFirstChromosome().getAsRNAs().get(name);
			String wurtzel = "";
			if(sRNA.getFoundInText().contains("Wurtzel et al. 2012")) wurtzel = "Yes";
			String[] rows ={i+"",sRNA.getName(),sRNA.getBegin()+"",sRNA.getEnd()+"",sRNA.getStrand()+"",
					sRNA.getLength()+"",sRNA.getSynonymsText()+"",wurtzel,sRNA.getRef(),sRNA.getFoundInText()};
			String row = "";
			for(String temp: rows){
				row+=temp+"\t";
			}
			finalList.add(row);
			i++;
		}
		TabDelimitedTableReader.saveList(finalList, Srna.PATHTABLE_ASrna);
		
		finalList.clear();
		finalList.add(header);
		i=1;
		for(String name : genome.getFirstChromosome().getCisRegs().keySet()){
			Srna sRNA = genome.getFirstChromosome().getCisRegs().get(name);
			String wurtzel = "";
			if(sRNA.getFoundInText().contains("Wurtzel et al. 2012")) wurtzel = "Yes";
			String[] rows ={i+"",sRNA.getName(),sRNA.getBegin()+"",sRNA.getEnd()+"",sRNA.getStrand()+"",
					sRNA.getLength()+"",sRNA.getSynonymsText()+"",wurtzel,sRNA.getRef(),sRNA.getFoundInText()};
			String row = "";
			for(String temp: rows){
				row+=temp+"\t";
			}
			finalList.add(row);
			i++;
		}
		TabDelimitedTableReader.saveList(finalList, Srna.PATHTABLE_CISReg);
	}
	
	/**
	 * Run UNAFold for each sRNA to calculate folding<br>
	 * Copy each .png image from UNAFold folder to <code>Srna.PATH_SEC_STRUCTURE</code>
	 */
	public static void createFoldingFigures(){
		Genome genome = Genome.loadEgdeGenome();
		for(int i=0;i<Srna.getSrnaOrder().size();i++){
			Srna sRNA = genome.getChromosomes().get(0).getsRNAs().get(Srna.getSrnaOrder().get(i));
			UNAfold.foldRNA(sRNA, sRNA.getName(),true);
		}
		
		for(String name : genome.getChromosomes().get(0).getAsRNAs().keySet()){
			Srna sRNA = genome.getChromosomes().get(0).getAsRNAs().get(name);
			UNAfold.foldRNA(sRNA, sRNA.getName(),true);
		}
		
		for(String name : genome.getChromosomes().get(0).getCisRegs().keySet()){
			Srna sRNA = genome.getChromosomes().get(0).getCisRegs().get(name);
			UNAfold.foldRNA(sRNA, sRNA.getName(),true);
		}
		
		File path = new File(Srna.PATH_SEC_STRUCTURE);

		
		if(!path.exists()) path.mkdir();
		File previousPath = new File(UNAfold.PATH_DATA);
		for(File file : previousPath.listFiles()){
			if(file.getAbsolutePath().contains(".png")){
				try {
					System.out.println(file.getAbsolutePath());
					System.out.println(path.getAbsolutePath()+File.separator+FileUtils.removePath(file.getAbsolutePath()));
					FileUtils.copy(file.getAbsolutePath(), path.getAbsolutePath()+File.separator+FileUtils.removePath(file.getAbsolutePath()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		
	}
}
