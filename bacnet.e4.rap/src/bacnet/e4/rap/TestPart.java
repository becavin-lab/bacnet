package bacnet.e4.rap;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.widgets.Composite;

public class TestPart {

	public TestPart() {
	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite parent) {

		/*
		 * Run Scripts here!
		 */
		// BioCondition bioCond = BioCondition.getBioCondition("EGDe_TIS_Final");

		System.out.println("Finished script!");

	}

	@PreDestroy
	public void dispose() {
	}

	@Focus
	public void setFocus() {
		// TODO Set the focus to control
	}

	/**
	 * Ran when the BannerView is opened
	 */
	public static void runTests() {

		/*
		 * Run Scripts here!
		 */
		/*
		 * Run Scripts here!
		 */
		// BioCondition bioCond = BioCondition.getBioCondition("EGDe_TIS_Final");

		// ExpressionMatrix matrix =
		// ExpressionMatrix.loadTab("/Users/cbecavin/Documents/RNABindingProtein/DiffExpression/RNABinding_All.txt",
		// true);
		// matrix = Annotation.addAnnotation(matrix, Genome.loadEgdeGenome());
		// matrix.saveTab("/Users/cbecavin/Documents/RNABindingProtein/DiffExpression/RNABinding_All_Annot.txt","Locustag");

		ArrayList<String> finalList = new ArrayList<>();
		// Genome genome = Genome.loadEgdeGenome();
		// for(Gene gene : genome.getFirstChromosome().getGenes().values()){
		// String row ="";
		// row="NC_003210.1\tRefSeq\texon\t"+gene.getBegin()+"\t"+gene.getEnd()+"\t.\t"+gene.getStrand()+"\t.\tgene_id="+
		// gene.getName()+";\t";
		// System.out.println(row);
		// finalList.add(row);
		// }
		// for(NcRNA gene : genome.getFirstChromosome().getNcRNAs().values()){
		// String row ="";
		// row="NC_003210.1\tRefSeq\texon\t"+gene.getBegin()+"\t"+gene.getEnd()+"\t.\t"+gene.getStrand()+"\t.\tgene_id="+
		// gene.getName()+";\t";
		// System.out.println(row);
		// finalList.add(row);
		// }
		// for(Srna gene : genome.getFirstChromosome().getsRNAs().values()){
		// String row ="";
		// row="NC_003210.1\tRefSeq\texon\t"+gene.getBegin()+"\t"+gene.getEnd()+"\t.\t"+gene.getStrand()+"\t.\tgene_id="+
		// gene.getName()+";\t";
		// System.out.println(row);
		// finalList.add(row);
		// }
		// for(Srna gene : genome.getFirstChromosome().getAsRNAs().values()){
		// String row ="";
		// row="NC_003210.1\tRefSeq\texon\t"+gene.getBegin()+"\t"+gene.getEnd()+"\t.\t"+gene.getStrand()+"\t.\tgene_id="+
		// gene.getName()+";\t";
		// System.out.println(row);
		// finalList.add(row);
		// }
		// for(Srna gene : genome.getFirstChromosome().getCisRegs().values()){
		// String row ="";
		// row="NC_003210.1\tRefSeq\texon\t"+gene.getBegin()+"\t"+gene.getEnd()+"\t.\t"+gene.getStrand()+"\t.\tgene_id="+
		// gene.getName()+";\t";
		// System.out.println(row);
		// finalList.add(row);
		// }
		// TabDelimitedTableReader.saveList(finalList,"/Users/cbecavin/Documents/RNABindingProtein/NC_003210.gff");
		//
		// finalList.clear();
		// finalList.add("Locustag\tInfo\tCOG");
		// for(Gene gene : genome.getFirstChromosome().getGenes().values()){
		// String row ="";
		// // add phage information in the gene name
		// String geneName = gene.getGeneName();
		// int geneId = Integer.parseInt(gene.getName().replaceAll("lmo",""));
		// if(113 <= geneId && geneId <= 123) {
		// geneName = "lma operon - " +geneName;
		// }
		// if(2270 <= geneId && geneId <= 2333) {
		// geneName = "A118 phage - " +geneName;
		// }
		// row=gene.getName()+"\t"+geneName+"\t"+gene.getCog()+"";
		// finalList.add(row);
		// }
		// for(NcRNA gene : genome.getFirstChromosome().getNcRNAs().values()){
		// String row ="";
		// row=gene.getName()+"\t"+gene.getType()+"\t";
		// finalList.add(row);
		// }
		// for(Srna gene : genome.getFirstChromosome().getsRNAs().values()){
		// String row ="";
		// row=gene.getName()+"\t"+gene.getTypeSrna()+"\t";
		// finalList.add(row);
		// }
		// for(Srna gene : genome.getFirstChromosome().getAsRNAs().values()){
		// String row ="";
		// row=gene.getName()+"\t"+gene.getTypeSrna()+"\t";
		// finalList.add(row);
		// }
		// for(Srna gene : genome.getFirstChromosome().getCisRegs().values()){
		// String row ="";
		// row=gene.getName()+"\t"+gene.getTypeSrna()+"\t";
		// finalList.add(row);
		// }
		// TabDelimitedTableReader.saveList(finalList,"/Users/cbecavin/Documents/RNABindingProtein/NC_003210.cog");
		////
		// System.out.println("Finished init script!");

	}

}
