package bacnet.scripts.blast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class BlastResult{
	
	public static int CUTOFF_EVALUE = 1;
	public static int CUTOFF_INTENSITIES = 80;
	
	
	public int begin;
	public int end;
	public int beginQuery;
	public int endQuery;
	public boolean strand = true;
	public String midline = "";
	public int alignLength;
	public int ident;
	
	public int diffSize;
	public double identities;
	public double bitScore;
	public double eValue;
	public String hit_def = "";
	public String hit_id = "";
	public String hit_accession = "";
	public String queryDef = "";
	public int queryLength;
	public int gaps;
	
	public String qSequence ="";
	public String hSequence = "";
	
	public BlastResult(Element hsp){
		begin = Integer.parseInt(hsp.getChildText("Hsp_hit-from"));
		end = Integer.parseInt(hsp.getChildText("Hsp_hit-to"));
		beginQuery = Integer.parseInt(hsp.getChildText("Hsp_query-from"));
		endQuery = Integer.parseInt(hsp.getChildText("Hsp_query-to"));
		qSequence = hsp.getChildText("Hsp_qseq");
		hSequence = hsp.getChildText("Hsp_hseq");
		midline = hsp.getChildText("Hsp_midline");
		alignLength = Integer.parseInt(hsp.getChildText("Hsp_align-len"));
		ident = Integer.parseInt(hsp.getChildText("Hsp_identity"));
		diffSize = alignLength-ident;
		gaps = Integer.parseInt(hsp.getChildText("Hsp_gaps"));
		bitScore = Double.parseDouble(hsp.getChildText("Hsp_bit-score"));
		eValue = Double.parseDouble(hsp.getChildText("Hsp_evalue"));
		int hitFrame = Integer.parseInt(hsp.getChildText("Hsp_hit-frame"));
		if(hitFrame==-1) strand = false;
	}
	
	
	public void calculateIdentities(){
		identities = ((double)ident*100)/((double)queryLength);
	}
	
	/**
	 * From an XML blast file, extract blast results
	 * @param fileName
	 * @return
	 */
	public static ArrayList<BlastResult> getResultsFromXML(String fileName){
		ArrayList<BlastResult> blastResults = new ArrayList<BlastResult>();
		try
		{	
			System.err.println("Read Blast result from: "+fileName);
			SAXBuilder sxb = new SAXBuilder();
			Document document = sxb.build(fileName);
			Element racine = document.getRootElement();
			Element child = (Element) racine.getChild("BlastOutput_iterations",racine.getNamespace());
		     List<Element> iterations = child.getChildren();
		     System.out.println("number of blast results: "+iterations.size());
		     for(Element iteration : iterations){
		     	child = (Element) iteration.getChild("Iteration_hits",racine.getNamespace());
		     	List<Element> hits = child.getChildren();
		     	int countHypothetical = 0;
		     	for(Element hit : hits){
//			     	System.out.println("Species: "+hit.getChildText("Hit_def"));
//			     	System.out.println("id: "+hit.getChildText("Hit_id"));
//			     	System.out.println("Accession: "+hit.getChildText("Hit_accession"));
			     	Element hitHsp = hit.getChild("Hit_hsps");
			     	List<Element> hsps = hitHsp.getChildren();
			     	// fill the list of results
		     		for(Element hsp : hsps){
		     			BlastResult bltResult = new BlastResult(hsp);
		     			bltResult.hit_accession = hit.getChildText("Hit_accession");
		     			bltResult.hit_def = hit.getChildText("Hit_def");
		     			//System.out.println(hit.getChildText("Hit_def").contains("hypothetical protein"));
		     			if(bltResult.hit_def.contains("hypothetical protein") || bltResult.hit_def.contains("predicted protein")){
		     				countHypothetical++;
		     			}
		     			bltResult.hit_id = hit.getChildText("Hit_id");
		     			bltResult.queryDef = iteration.getChildText("Iteration_query-def");
		     			bltResult.queryLength = Integer.parseInt(iteration.getChildText("Iteration_query-len"));
		     			bltResult.calculateIdentities();
		     			blastResults.add(bltResult);
		     		}
		     	}
		     	//System.err.println("ResultsName: "+iteration.getChildText("Iteration_query-def")+"  number: "+hits.size() + " hypotethical: "+countHypothetical);
		     	System.out.println(iteration.getChildText("Iteration_query-def")+"\t"+hits.size() + "\t"+countHypothetical);
		     	
		     }
		
		}catch(Exception e){
			System.out.println("Cannot read Blast XML file ");
		}
		return blastResults;	
	}
//	
//	/**
//	 * Given two BlastResult which should have the same strand and should have been odered properly using BlastResultComparator
//	 * this methode extract the sequence between this two results in the appropriate chromosome
//	 * @param bltResult1
//	 * @param bltResult2
//	 * @param chromosome
//	 * @return
//	 */
//	public static String getSequenceBetweenResults(BlastResult bltResult1, BlastResult bltResult2,DNASequence chromosome){
//		int end1 = bltResult1.end;
//		int begin2 = bltResult2.begin;
//		if(bltResult1.strand != bltResult2.strand){
//			System.err.println("These results do not have the same strand");
//			return "";
//		}
//		String sequence = "";
//		if(bltResult1.strand){
//			sequence = chromosome.getSequenceAsString(end1+1, begin2-1, Strand.POSITIVE);
//		}else{
//			// if we are in the minus strand, results are classify by descending order
//			end1 = bltResult2.begin;
//			begin2 = bltResult1.end;
//			sequence = chromosome.getSequenceAsString(end1+1, begin2-1, Strand.NEGATIVE);
//		}
//		return sequence;
//	}
//	/**
//	 * Use accession to filter a list of BlastResult
//	 * @param blastResults
//	 * @param accessionID
//	 * @return
//	 */
//	public static ArrayList<BlastResult> filterAccession(ArrayList<BlastResult> blastResults,String accessionID){
//		ArrayList<BlastResult> blastResultFiltered = new ArrayList<BlastResult>();
//		for(BlastResult bltResult:blastResults){
//	     	if(bltResult.hit_accession.equals(accessionID)){
//	     		blastResultFiltered.add(bltResult);
//	     	}
//		}
//		return blastResultFiltered;
//	}
//	
//	/**
//	 * Use eValue to filter a list of BlastResult
//	 * @param blastResults
//	 * @param eValueCutOff
//	 * @return
//	 */
//	public static ArrayList<BlastResult> filterEvalue(ArrayList<BlastResult> blastResults,int eValueCutOff){
//		ArrayList<BlastResult> blastResultFiltered = new ArrayList<BlastResult>();
//		for(BlastResult bltResult:blastResults){
//	     	if(bltResult.eValue<eValueCutOff){
//	     		blastResultFiltered.add(bltResult);
//	     	}
//		}
//		return blastResultFiltered;
//	}
//	
	/**
	 * Use identities to filter a list of BlastResult
	 * @param blastResults
	 * @param identitiesCutOff
	 * @return
	 */
	public static ArrayList<BlastResult> filterIdentities(ArrayList<BlastResult> blastResults,int identitiesCutOff){
		ArrayList<BlastResult> blastResultFiltered = new ArrayList<BlastResult>();
		for(BlastResult bltResult:blastResults){
	     	if(bltResult.identities>identitiesCutOff){
	     		blastResultFiltered.add(bltResult);
	     	}
		}
		return blastResultFiltered;
	}
	
	public static ArrayList<BlastResult> filterLength(ArrayList<BlastResult> blastResults,int distance){
		ArrayList<BlastResult> blastResultFiltered = new ArrayList<BlastResult>();
		for(BlastResult bltResult:blastResults){
	     	if(Math.abs(bltResult.alignLength-bltResult.queryLength)<distance){
	     		blastResultFiltered.add(bltResult);
	     	}
		}
		return blastResultFiltered;
	}
	
	public static ArrayList<BlastResult> filterDiffSize(ArrayList<BlastResult> blastResults,int diff){
		ArrayList<BlastResult> blastResultFiltered = new ArrayList<BlastResult>();
		for(BlastResult bltResult:blastResults){
	     	if(bltResult.diffSize<diff){
	     		blastResultFiltered.add(bltResult);
	     	}
		}
		return blastResultFiltered;
	}
	
	/**
	 * Return a list of all accessions contain in this list of results
	 * @param blastResults
	 * @return
	 */
	public static ArrayList<String> getAllAccession(ArrayList<BlastResult> blastResults){
		TreeSet<String> accessionSet = new TreeSet<String>();
		for(BlastResult bltResult:blastResults){
			accessionSet.add(bltResult.hit_accession);
		}
		ArrayList<String> accessions = new ArrayList<String>();
		for(String access:accessionSet){
			accessions.add(access);
		}
		return accessions;
	}

	
	@Override
	public String toString(){
		
		String ret="Hit: "+hit_def+"\n";
		ret+="  e ="+eValue+" bits , Identities = "+alignLength+"/"+queryLength+" ("+identities+"\\%)"+"\n";
		ret+=" ident = "+ident+" , diffSize = "+diffSize+"\n";
		ret+="\n";
		ret+=" Query  "+beginQuery+" -- "+endQuery+"\n";
		ret+=" Sbjct  "+begin+" -- "+end+"\n";
		ret+=" "+qSequence+"\n";
		ret+=" "+midline+"\n";
		ret+=" "+hSequence+"\n";
		return ret;
	}
	
	
	/**
	 * This comparator classify the BlastResult by their position on the genome
	 * @author Chris
	 *
	 */
	public static class BlastResultCompare implements Comparator<BlastResult>{
		public int compare(BlastResult o1, BlastResult o2) {
			// the blastresult should be from the same Strand
			if(o1.strand!=o2.strand){
				System.err.println("Error: Blastresults are not from the same strand");return 0;
			}else if(o1.strand){
				// classify by ascending order
				int posO1 = o1.begin;
				int posO2 = o2.begin;
				return (posO1-posO2);
			}else{
				// classify by descending order
				// strand =false so   end<begin
				int posO1 = o1.end;
				int posO2 = o2.end;
				return (posO2-posO1);
			}			
		}
	}
	

}
