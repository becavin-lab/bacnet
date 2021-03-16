// package bacnet.datamodel.dataset;
//
// import java.util.ArrayList;
// import java.util.TreeSet;
// import bacnet.datamodel.expdesign.BioCondition;
// import bacnet.datamodel.expdesign.Experiment;
// import bacnet.datamodel.sequence.Genome;
// import bacnet.reader.TabDelimitedTableReader;
//
/// **
// * Class for manipulating various RNASeq data, herited from ExpressionData <br>
// * double[] value is of genome size, when ones want to see RNAseq expression at a certain bp
// * position, just extract values[bpposition]<br>
// * Need to be loaded first, and unlog has RNASeq will be logged for display when convert in the
// * platform
// *
// * @author UIBC
// *
// */
// public class TSS2 extends ExpressionData {
//
// /**
// *
// */
// private static final long serialVersionUID = -6196236897771961044L;
//
// public static String PATH_TSSasRNA = ExpressionData.PATH_NGS_RAW + "asRNA-TSS-EGD-e/";
// public static String PATH_TSS_EGDe = ExpressionData.PATH_NGS_RAW + "TSS_data_EGDe.txt";
// public static String PATH_TSS_Innocua = ExpressionData.PATH_NGS_RAW + "TSS_data_Innocua.txt";
// public static String[] TSS_ARRAY = {"37C", "sigB", "prfA", "30C", "minusO2", "Stat"};
//
// private boolean alreadyRead = false;
//
// public static String EXTENSION = ".tss";
//
// public TSS2() {
// setType(TypeData.TSS);
// }
//
// public TSS2(String name) {
// super(name);
// setType(TypeData.TSS);
// }
//
// /**
// * Get value at a certain base pair position and unlog it by calculating 2^value<br>
// * It works faster if data has been already loaded by this.read() or this.read(from,to)
// */
// public double getUnlogValue(int bpPosition) {
// double value = getValue(bpPosition);
// value = Math.pow(2, value);
// if (value == 1)
// value = 0;
// return value;
// }
//
// public static void createTSS(Experiment exp) {
//
// /*
// * EGD-e TSS
// */
// String[][] tssArray = TabDelimitedTableReader.read(PATH_TSS_EGDe);
// Genome genome = Genome.loadEgdeGenome();
// double[] valuesPlus = new double[genome.getFirstChromosome().getLength()];
// double[] valuesMinus = new double[genome.getFirstChromosome().getLength()];
//
// for (int j = 5; j < tssArray[0].length; j++) {
// String bioCondName = tssArray[0][j];
// if (exp.getBioConds().containsKey(bioCondName)) {
// for (int i = 0; i < valuesPlus.length; i++)
// valuesPlus[i] = 0;
// for (int i = 0; i < valuesMinus.length; i++)
// valuesMinus[i] = 0;
//
// for (int i = 1; i < tssArray.length; i++) {
// int position = Integer.parseInt(tssArray[i][0]);
// boolean strand = true;
// int value = Integer.parseInt(tssArray[i][j]);
// if (tssArray[i][1].contains("-"))
// strand = false;
// if (value != 0) {
// if (strand) {
// valuesPlus[position - 1] = Math.log(value) / Math.log(2);
// } else {
// valuesMinus[position - 1] = -Math.log(value) / Math.log(2);
// }
// }
// }
//
// BioCondition bioCond = BioCondition.getBioCondition(tssArray[0][j]);
// TSS2 plusNGS = bioCond.getTsss().get(0);
// TSS2 minusNGS = bioCond.getTsss().get(1);
// plusNGS.setBioCondName(bioCond.getName());
// plusNGS.setType(TypeData.TSS);
// plusNGS.setValues(valuesPlus);
// plusNGS.setLength(valuesPlus.length);
// plusNGS.setGenomeName(genome.getSpecies());
// plusNGS.setChromosomeID(genome.getFirstChromosome().getAccession().toString());
// plusNGS.setStat();
// plusNGS.save();
// minusNGS.setBioCondName(bioCond.getName());
// minusNGS.setType(TypeData.TSS);
// minusNGS.setValues(valuesMinus);
// minusNGS.setLength(valuesMinus.length);
// minusNGS.setGenomeName(genome.getSpecies());
// minusNGS.setChromosomeID(genome.getFirstChromosome().getAccession().toString());
// minusNGS.setStat();
// minusNGS.save();
// }
// }
//
// /*
// * InnocuaEGD-e TSS
// */
// if (exp.getBioConds().containsKey("Innocua_37C_TSS")) {
// tssArray = TabDelimitedTableReader.read(PATH_TSS_Innocua);
// genome = Genome.loadGenome("Listeria innocua Clip11262");
// valuesPlus = new double[genome.getFirstChromosome().getLength()];
// valuesMinus = new double[genome.getFirstChromosome().getLength()];
//
// int j = 2;
// for (int i = 0; i < valuesPlus.length; i++)
// valuesPlus[i] = 0;
// for (int i = 0; i < valuesMinus.length; i++)
// valuesMinus[i] = 0;
//
// for (int i = 1; i < tssArray.length; i++) {
// int position = Integer.parseInt(tssArray[i][0]);
// boolean strand = true;
// int value = Integer.parseInt(tssArray[i][j]);
// if (tssArray[i][1].contains("-"))
// strand = false;
// if (value != 0) {
// if (strand) {
// valuesPlus[position - 1] = Math.log(value) / Math.log(2);
// } else {
// valuesMinus[position - 1] = -Math.log(value) / Math.log(2);
// }
// }
// }
// BioCondition bioCond = BioCondition.getBioCondition(tssArray[0][j]);
// TSS2 plusNGS = bioCond.getTsss().get(0);
// TSS2 minusNGS = bioCond.getTsss().get(1);
// plusNGS.setBioCondName(bioCond.getName());
// plusNGS.setType(TypeData.TSS);
// plusNGS.setValues(valuesPlus);
// plusNGS.setLength(valuesPlus.length);
// plusNGS.setGenomeName(genome.getSpecies());
// plusNGS.setChromosomeID(genome.getFirstChromosome().getAccession().toString());
// plusNGS.setStat();
// plusNGS.save();
// minusNGS.setBioCondName(bioCond.getName());
// minusNGS.setType(TypeData.TSS);
// minusNGS.setValues(valuesMinus);
// minusNGS.setLength(valuesMinus.length);
// minusNGS.setGenomeName(genome.getSpecies());
// minusNGS.setChromosomeID(genome.getFirstChromosome().getAccession().toString());
// minusNGS.setStat();
// minusNGS.save();
// }
// }
//
// /**
// * Run different function to curate the data given by Omri 15/11/2012
// */
// public static void createASrnaTSStable() {
// for (String tss : TSS_ARRAY) {
// parseList(tss);
// }
// combineAllLists();
// addToFinalData();
//
// }
//
// /**
// * Add ASrna TSS information at the end of TSS_expression_acrosse_conditions file
// */
// private static void addToFinalData() {
// ArrayList<String> tssList = TabDelimitedTableReader
// .readList(ExpressionData.PATH_NGS_RAW + "TSS_expression_across_conditions.txt", true);
// ArrayList<String> tssListASrna = TabDelimitedTableReader.readList(PATH_TSSasRNA +
// "all_TSS_ASrna.txt", true);
// for (String line : tssListASrna) {
// tssList.add(line);
// }
// TabDelimitedTableReader.saveList(tssList, ExpressionData.PATH_NGS_RAW +
// "TSS_expression_across_conditions.txt");
// }
//
// /**
// * Read all list of ASrnaTSS and combine them into one table
// */
// private static void combineAllLists() {
// /*
// * Create a list of all positions
// */
// TreeSet<String> allTSS = new TreeSet<String>();
// for (String tss : TSS_ARRAY) {
// String[][] data = TabDelimitedTableReader.read(PATH_TSSasRNA + tss + "_parsed.txt");
// for (String[] line : data) {
// allTSS.add(line[0] + "\t" + line[1]);
// }
// }
// System.out.println("found " + allTSS.size() + " positions");
// /*
// * go through that list and create the final table
// */
// String[][] allTssArray = TabDelimitedTableReader.read(PATH_TSSasRNA + "all_asRNA.pos.txt");
// ArrayList<String> finalTssList = new ArrayList<String>();
// for (String tss : allTSS) {
// String position = tss.split("\t")[0];
// /*
// * Find information about this TSS
// */
// int index = -1;
// for (int i = 0; i < allTssArray.length; i++) {
// for (String element : allTssArray[i][4].split(",")) {
// String positionTemp = element.split("\\(")[0].trim();
// if (position.equals(positionTemp))
// index = i;
// }
// }
//
// String ret = tss + "\tASrna\tNA\tNA";
// if (index != -1)
// ret = tss + "\tASrna\t" + allTssArray[index][7] + "\t" + allTssArray[index][6];
//
// /*
// * Fill nbReads with the different value found in different bioCond
// */
// int[] nbReads = new int[6];
// for (int j = 0; j < TSS_ARRAY.length; j++) {
// String[][] tssArray = TabDelimitedTableReader.read(PATH_TSSasRNA + TSS_ARRAY[j] + "_parsed.txt");
// int k = -1;
// for (int i = 0; i < tssArray.length; i++) {
// if (position.equals(tssArray[i][0]))
// k = i;
// }
// if (k != -1)
// nbReads[j] = Integer.parseInt(tssArray[k][2]);
// }
// int count = 0;
// for (int nbRead : nbReads) {
// ret += "\t" + nbRead;
// count += nbRead;
// }
// ret += "\t" + count;
// finalTssList.add(ret);
// }
// TabDelimitedTableReader.saveList(finalTssList, PATH_TSSasRNA + "all_TSS_ASrna.txt");
// }
//
// /**
// * Curate list given by Omri, by parsing each element
// *
// * @param fileName
// */
// private static void parseList(String fileName) {
// ArrayList<String> wtNew = new ArrayList<String>();
// ArrayList<String> wt = TabDelimitedTableReader.readList(PATH_TSSasRNA + fileName + ".txt");
// for (String line : wt) {
//
// String[] elements = line.split(",");
// for (String element : elements) {
// int index = -1;
// String pos = element.split("\\(")[0].trim();
// String subElement = element.substring(element.indexOf("str") + 3, element.indexOf(')'));
// String strand = subElement.split("\\#")[0];
// String nbReads = subElement.split("\\#")[1];
// String ret = pos + "\t" + strand + "\t" + nbReads;
// wtNew.add(ret);
// // System.out.println(ret);
// }
// }
// TabDelimitedTableReader.saveList(wtNew, PATH_TSSasRNA + fileName + "_parsed.txt");
// }
//
// public boolean isAlreadyRead() {
// return alreadyRead;
// }
//
// public void setAlreadyRead(boolean alreadyRead) {
// this.alreadyRead = alreadyRead;
// }
//
// }
