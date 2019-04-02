package bacnet.scripts.blast;

import java.io.IOException;

import bacnet.utils.CMD;

public class BlastOutput {
	
	
	public static String[] outputName = {"pairwise","query-anchored showing identities","query-anchored no identities",
     "flat query-anchored, show identities","flat query-anchored, no identities","XML Blast output","tabular",
     "tabular with comment lines","Text ASN.1","Binary ASN.1","Comma-separated values","BLAST archive format (ASN.1)"};
	
	public enum BlastOutputTYPE {PAIRWISE,QUERY_SHOW,QUERY,FLAT_SHOW,FLAT,XML,TABLE,TABLE_COMMENT,ASN_TxT,ASN_Bin,COMMA,ASN};

	public static String getName(BlastOutputTYPE type){
		return outputName[type.ordinal()];
	}
	
	/**
	 * Convert asn given by blastResult into out
	 * using BlastOutputType
	 * @param blastResult
	 * @param out
	 * @param html
	 * @param type
	 */
	public static String convertOuput(String blastResult,String out,boolean html,BlastOutputTYPE type){
		// first if type = ASN, do nothing
		//if(type == BlastOutputTYPE.ASN) return blastResult;
		String htmlArg = "";
		//if(type == BlastOutputTYPE.XML) html=false;
		if(html){
			htmlArg="-html";
			out+=".html";
		}else{
			out+=fileExtension(type);
		}
		String[] args = {Blast.blast_formatter,"-archive","\""+blastResult+"\"","-out","\""+out+"\"","-outfmt",type.ordinal()+"",htmlArg};
		try {
			CMD.runProcess(args, true);
			
//			
//			if(type == BlastOutputTYPE.XML){
//				// we need to delete one line in the file otherwise it will not be read
//				String text = FileUtils.readText(out);
//				String[] lines = text.split("\n");
//				// delete line 2
//				lines[1]=lines[0];
//				text="";
//				for(int i=1;i<lines.length;i++){
//					text+=lines[i]+"\n";
//				}
//				FileUtils.saveText(text, out);
//			}
			
			System.out.println("Conversion finished");
			return out;
		}
		catch (IOException e) {
			System.err.println("Cannot convert into html file");
		}
		return out;
	}
	
	/**
	 * For a specific type, return the extension of the corresponding file
	 * @param type
	 * @return
	 */
	public static String fileExtension(BlastOutputTYPE type){
		if(type == BlastOutputTYPE.XML){
			return ".xml";
		}else if(type==BlastOutputTYPE.ASN || type==BlastOutputTYPE.ASN_Bin || type==BlastOutputTYPE.ASN_TxT){
			return ".asn";
		}else return ".txt";
	}
	
}
