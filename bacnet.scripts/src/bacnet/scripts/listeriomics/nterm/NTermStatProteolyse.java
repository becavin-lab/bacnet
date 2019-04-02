package bacnet.scripts.listeriomics.nterm;

import java.util.ArrayList;

import bacnet.datamodel.dataset.NTermData;
import bacnet.datamodel.proteomics.NTermUtils;
import bacnet.datamodel.proteomics.TIS;
import bacnet.reader.TabDelimitedTableReader;

/**
 * A list of methods to assess the quantity of proteolysis on the peptides
 * @author UIBC
 *
 */
public class NTermStatProteolyse {

	public static String PATH = NTermUtils.getPATH()+"Proteolyse/";
	
	
	public static void run(String massSpecName){
		NTermData massSpec = NTermData.load(massSpecName);
		System.out.println("nb Nterm: "+massSpec.getElements().size());
		ArrayList<TIS> proteolyses = massSpec.getTisList();
		saveProteolyses(proteolyses,PATH+"TIS list.txt");
//		numberElements(proteolyses);
//		proteolyses = removeMultiModif(proteolyses);
//		numberElements(proteolyses);
//		ArrayList<TIS> proteolysesAtis = getaTIS(proteolyses, true);
//		numberElements(proteolysesAtis);
//		proteolyses = getaTIS(proteolyses, false);
//		numberElements(proteolyses);
//		saveProteolyses(proteolyses,PATH+"no aTIS proteolyse.txt");
//		saveProteolyses(proteolysesAtis,PATH+"aTIS proteolyse.txt");
//		cTerminalRagging(proteolyses);
	}
	
//	public static void cTerminalRagging(ArrayList<TIS> proteolyses){
//	
//		System.out.println("Nb group: "+proteolyses.size());
//		
//		ArrayList<TIS> proteolysesNew = removeMultiModif(proteolyses);
//		int countRealaTIS = 0;
//		for(TIS tis : proteolysesNew){
//			ArrayList<NTerm> nTerms = tis.getnTerms();
//			boolean found = false;
//			String peptide = tis.getnTermRef().getSequencePeptide();
//			if(peptide.startsWith("M") && peptide.endsWith("R")){
//				found = true;
//			}
//			for(int i=0;i<nTerms.size() && !found;i++){
//				peptide = nTerms.get(i).getSequencePeptide();
//				if(peptide.startsWith("M") && peptide.endsWith("R")){
//					found = true;
//				}
//			}
//			if(found){
//				countRealaTIS++;
//			}
//		}
//		System.out.println("NB of group of peptides: "+proteolyses.size()+"  nb of group of peptides containing perfect peptide (start with an M, end with an R): "+countRealaTIS);
//		
//	}
	
//	/**
//	 * Remove group of peptides containing NTerm with exactly the same sequence, but different modification
//	 * @param proteolyses
//	 * @return
//	 */
//	public static ArrayList<TIS> removeMultiModif(ArrayList<TIS> proteolyses){
//		ArrayList<TIS> proteolysesNew = new ArrayList<TIS>();
//		System.out.println("Nb group: "+proteolyses.size());
//		int multimodif = 0;
//		for(TIS tis : proteolyses){
//			ArrayList<NTerm> nTerms = tis.getnTerms();
//			boolean found = false;
//			String peptide = tis.getnTermRef().getSequencePeptide();
//			for(int i=0;i<nTerms.size() && !found;i++){
//				String peptide2 = nTerms.get(i).getSequencePeptide();
//				if(!peptide.equals(peptide2)){
//					found=true;
//				}
//			}
//			
//			if(!found){
//				multimodif++;
//			}else{
//				proteolysesNew.add(nTerms);
//			}
//		}
//		//porte306
//		System.out.println("NB of groups removed because it corresponds to same peptide but with different type of modification: "+multimodif);
//		return proteolysesNew;
//	}
//	
//	public static ArrayList<TIS> getaTIS(ArrayList<TIS> proteolyses,boolean keep){
//		ArrayList<TIS> proteolysesNew = new ArrayList<TIS>();
//		System.out.println("Nb group: "+proteolyses.size());
//		int nbaTIS = 0;;
//		for(NTerm nTermRef : proteolyses.keySet()){
//			ArrayList<NTerm> nTerms = proteolyses.get(nTermRef);
//			boolean found = false;
//			if(nTermRef.getTypeOverlap()==TypeOverlap.aTIS) found = true;
//			String peptide = nTermRef.getSequencePeptide();
//			for(int i=0;i<nTerms.size() && !found;i++){
//				if(nTerms.get(i).getTypeOverlap()==TypeOverlap.aTIS){
//					found=true;
//				}
//			}
//			
//			if(found){
//				nbaTIS++;
//				if(keep){
//					proteolysesNew.put(nTermRef, nTerms);
//				}
//			}else{
//				if(!keep){
//					proteolysesNew.put(nTermRef, nTerms);
//				}
//			}	
//		}
//		
//		System.out.println("NB of groups containing at least one aTIS: "+nbaTIS);
//		return proteolysesNew;
//	}
//	
//	public static void numberElements(ArrayList<TIS> proteolyses){
//		int number=0;
//		for(NTerm nTermRef : proteolyses.keySet()){
//			number++;
//			number+=proteolyses.get(nTermRef).size();
//		}
//		System.out.println("NB elements: "+number);
//	}
	
	/**
	 * Save list of protoelyses hashmap
	 * @param proteolyses
	 */
	public static void saveProteolyses(ArrayList<TIS> tiss,String fileName){
		ArrayList<String> results = new ArrayList<String>();
		String header = "TIS name\tRef sequence\tList of modification\tList of overlaps\tRef peptide\t# peptides\t# spectra";
		results.add(header);
		
		for(TIS tisnTerm : tiss){
			String row = tisnTerm.getName()+"\t"+tisnTerm.getRefSequence()+"\t"+tisnTerm.getModifsString()+"\t"+tisnTerm.getOverlapsString()+"\t"+tisnTerm.getnTermRef().getName()+"\t"+tisnTerm.getnTerms().size()+"\t"+tisnTerm.getnTermsString()+"\t"+tisnTerm.getTotalSpectra();
			results.add(row);
//			for(NTerm nTermTemp : tisnTerm.getnTerms()){
//				row = tisnTerm.getName()+"\t"+nTermTemp.getSequencePeptide()+"\t"+nTermTemp.getTypeModif()+"\t"+nTermTemp.getOverlap()+"\t"+nTermTemp.getSpectra()+"\t"+nTermTemp.getTypeOverlap()+"\t"+nTermTemp.getName();
//				results.add(row);
//			}
		}
		TabDelimitedTableReader.saveList(results, fileName);
	}
}
