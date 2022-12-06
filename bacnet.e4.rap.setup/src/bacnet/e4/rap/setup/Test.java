package bacnet.e4.rap.setup;

import java.io.IOException;

import bacnet.scripts.listeriomics.Peptidomics;
import bacnet.scripts.database.GenomesCreation;
/**
 * All methods which will be executed before or after interface run
 * @author ipmc
 *
 */
public class Test {
	
	/**
	 * Run pre-test methods before interface creation
	 */
	public static void runPreTest() {
		System.out.println("Run Pre-test");
		
//		try {
//			Peptidomics.runEpitopes();
//			Peptidomics.runAntigen();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		//bacnet.scripts.database.GenomesCreation.correctKIM5OldLocusTag();
		
		/*
		String[][] array = TabDelimitedTableReader.read("D:\\Yersiniomics\\Yersiniomics\\ArrayExpress\\probes.txt");
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
            	Agilent.extractProbesComparison(array[i][1], array[i][0]);
            }
        }
        */
    	//ArrayExpressTechnology.findProbes();
		//ArrayExpress.run();
		//ArrayExpressTechnology.downloadAllTechno();
    	//Agilent.extractProbesComparison("A-GEOD-9009", "E-GEOD-30634");


//		System.out.println("Run Pre-test");
//		 RASTEGDeGenome.run();
//		Genome genome = Genome.loadEgdeGenome();
//		System.out.println(genome.getSpecies());
//		
//		TabDelimitedTableReader.save(Annotation.getAnnotationGenes(genome,genome.getFirstChromosome().getGeneNameList()),"C:\\Users\\ipmc\\Desktop\\EGDe.txt");
		
		
	}
	
	/**
	 * Run post--test after interface creation
	 */
	public static void runPostTest() {
		System.out.println("Run Post-test");
		
//		for(String genome : Genome.getAvailableGenomes()) {
//			Genome genomeTemp = Genome.loadGenome(genome);
//        	genomeTemp.saveProteinIdToLocusTag();
//		}
	}

}
