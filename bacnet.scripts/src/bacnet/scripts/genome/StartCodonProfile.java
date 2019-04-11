package bacnet.scripts.genome;

import org.biojava3.core.sequence.Strand;
import bacnet.datamodel.dataset.OmicsData;
import bacnet.datamodel.sequence.Codon;
import bacnet.datamodel.sequence.Genome;
import bacnet.reader.TabDelimitedTableReader;

/**
 * Calculate presence of start codon on firrst chromosome of bacterial genomes
 * 
 * @author christophebecavin
 *
 */
public class StartCodonProfile {

    public static int SD_ENERGY_CUTOFF = -7;

    public static void run() {

        Genome genome = Genome.loadEgdeGenome();
        String dataName = "SDProfile_EGDe_filter";
        calculateStartCodonProfile(genome, dataName, 0);
        dataName = "SDProfile_EGDe_StartCodon";
        calculateStartCodonProfile(genome, dataName, 1);
        dataName = "SDProfile_EGDe_Potential_Start";
        calculateStartCodonProfile(genome, dataName, 2);

        Genome genome2 = Genome.loadGenome(Genome.ECOLI_NAME);
        dataName = "SDProfile_Ecoli_filter";
        calculateStartCodonProfile(genome2, dataName, 0);
        dataName = "SDProfile_Ecoli_StartCodon";
        calculateStartCodonProfile(genome2, dataName, 1);
        dataName = "SDProfile_Ecoli_Potential_Start";
        calculateStartCodonProfile(genome2, dataName, 2);

        Genome genome3 = Genome.loadGenome(Genome.BACSUBTILIS_NAME);
        dataName = "SDProfile_Bsub_filter";
        calculateStartCodonProfile(genome3, dataName, 0);
        dataName = "SDProfile_Bsub_StartCodon";
        calculateStartCodonProfile(genome3, dataName, 1);
        dataName = "SDProfile_Bsub_Potential_Start";
        calculateStartCodonProfile(genome3, dataName, 2);
    }

    /**
     * Create an NGS data where:<br>
     * <li>Start codon represent a coverage of 10
     * <li>Stop codon represent a coverage of 5
     * <li>every other codon a coverage of 0
     * 
     * @param genome
     * @param dataName
     */
    public static void calculateStartCodonProfile(Genome genome, String dataName, int typeData) {
        // Load SD Profile
        String genomeName = genome.getSpecies().replaceAll(" ", "_");
        String fileName = SDProfile.PATH + genomeName + "_+_.wig";
        String[][] arrayPlus = TabDelimitedTableReader.read(fileName);
        fileName = SDProfile.PATH + genomeName + "_-_.wig";
        String[][] arrayMinus = TabDelimitedTableReader.read(fileName);

        // Fill Start codon table
        String[][] data = new String[genome.getFirstChromosome().getLength() + 1][2];
        data[0][0] = "0";
        data[0][1] = "0";
        data[1][0] = "0";
        data[1][1] = "0";
        data[genome.getFirstChromosome().getLength()][0] = "0";
        data[genome.getFirstChromosome().getLength()][1] = "0";
        for (int i = 2; i < genome.getFirstChromosome().getLength(); i++) {
            String codon = genome.getFirstChromosome().getSequenceAsString(i - 1, i + 1, Strand.POSITIVE);
            data[i][0] = i + "";
            data[i][1] = "0";
            switch (typeData) {
                case 0:
                    Double energy = Double.parseDouble(arrayPlus[i][1]);
                    if (energy < SD_ENERGY_CUTOFF) {
                        data[i][1] = -energy + "";
                    }
                    break;
                case 1:
                    if (Codon.isStart(codon)) {
                        data[i][1] = "10";
                    }
                    break;
                case 2:
                    if (Codon.isStart(codon)) {
                        for (int j = 0; j < 20 && (i - j) > 0; j++) {
                            energy = Double.parseDouble(arrayPlus[i - j][1]);
                            if (energy < SD_ENERGY_CUTOFF) {
                                data[i][1] = "-2";
                                data[i - j][1] = -energy + "";
                            }
                        }
                    }
                    break;
            }
        }
        TabDelimitedTableReader.save(data, SDProfile.PATH + dataName + "_f.wig");
        TabDelimitedTableReader.save(data, OmicsData.PATH_NGS_RAW + dataName + "_f.wig");

        for (int i = 2; i < genome.getFirstChromosome().getLength(); i++) {
            data[i][0] = i + "";
            data[i][1] = "0";
        }
        for (int i = 2; i < genome.getFirstChromosome().getLength(); i++) {
            String codon = genome.getFirstChromosome().getSequenceAsString(i - 1, i + 1, Strand.NEGATIVE);
            switch (typeData) {
                case 0:
                    Double energy = Double.parseDouble(arrayMinus[i][1]);
                    if (energy < SD_ENERGY_CUTOFF) {
                        data[i][1] = -energy + "";
                    }
                    break;
                case 1:
                    if (Codon.isStart(codon)) {
                        data[i][1] = "10";
                    }
                    break;
                case 2:
                    if (Codon.isStart(codon)) {
                        for (int j = 0; j < 20 && (i + j) < genome.getFirstChromosome().getLength(); j++) {
                            energy = Double.parseDouble(arrayMinus[i + j][1]);
                            if (energy < SD_ENERGY_CUTOFF) {
                                data[i][1] = "-2";
                                data[i + j][1] = -energy + "";
                            }
                        }
                    }
                    break;
            }
        }
        TabDelimitedTableReader.save(data, SDProfile.PATH + dataName + "_r.wig");
        TabDelimitedTableReader.save(data, OmicsData.PATH_NGS_RAW + dataName + "_r.wig");

    }

}
