package bacnet.scripts.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;
import bacnet.reader.TabDelimitedTableReader;

/***************************************************************************
 *
 * PREDicting poly-A region and TERMinal splice acceptor for Leishmania Please read manuscript for
 * details : Smith, M., Blanchette, M., Papadopoulou, B. Improving the prediction of mRNA
 * extremities in the protozoan parasite Leishmania. BMC Bioinformatics, 2008.
 *
 * INPUT : .FASTA formatted intergenic sequence, with 800 nt of coding sequence at each extremity
 * (i.e. last 800 nt of upstream gene and first 800 of dowsntream gene) *must have 500A800.pssm in
 * same directory as this script
 *
 * Copyright (C) - Martin.Smith@umontreal.ca - 2007
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 **************************************************************************/

public class PredaTerm {
    // the following variables are for limiting the Splice-Junction prediction range
    // changing variables within reasonable limitis has little effect on p(A)
    // prediction
    // defaults : upstream CoDing Sequence = 500, downstream CDS = 200
    static int upCDS = 500, downCDS = 200;
    // these suckers are program variables, do not touch !
    static boolean SJcutoff;
    static int SJthreshold;

    /**
     * Please enter fasta file as argument, with 800 nucleotides flanking the intergenic sequences") ;
     * 
     * @param fastaFileName
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static void run(String fastaFileName, String pssmFileName, String finalGTFFile) throws IOException {
        System.out.println("\n" + "============================================================================\n"
                + "    ___     ___     ____    __      _    _____   ____   ____    __   __\n"
                + "   / _  7\\ / _ \\   / __/\\  /  \\    / \\  /\\_  _\\ /\\  _| /\\  _ \\ /\\  \\/  \\\n"
                + "  / ,__/ //   _/| / __/\\/ / / /|  / L \\ \\//\\ \\' \\ \\  _\\\\ \\   / \\ \\ \\_/\\ \\\n"
                + " /_/\\__.'/_/\\_\\.'/___/\\/ /__,/ / /_,^\\_\\  \\ \\_\\  \\ \\___|\\ \\_\\_\\ \\ \\_\\| \\_\\\n"
                + " \\_\\/    \\_\\^\\_\\  \\__\\/   \\__.'  |_|^|_|   \\/_/   \\/__/  \\/./_/  \\/_/ \\/_/\n"
                + "														\n"
                + "       PREDicting poly-A region and TERMinal splice acceptor of 	\n"
                + "           non-coding RNA transcripts in Leishmania sp.\n"
                + "                   Martin.Smith@umontreal.ca - 2007\n"
                + "============================================================================");
        // Variables which can be customized (see manuscript for details)
        int pssmsize = 0, // Counter, do not touch !
                upSJ = 1000, // Distance to scan upstream of predicted splice acceptor. Default 1000
                upYY = 75, // Distance for the small matrix to scan from larger matrix center. Default 75
                up1 = 75, // Large poly(A) PSSM size, upstream. Default 75
                down1 = 600, // Large poly(A) PSSM size, downstream. Default 600
                up2 = 25, // Small poly(A) PSSM size, upstream. Default 25
                down2 = 25, // Small poly(A) PSSM size, downstream. Default 25
                pAthreshold = 34; // Statistical cutoff for limiting false poly(A) positives to < 5%. Default 34
        SJthreshold = 1506; // Same threshold for linear splice junction scoring scheme. Default 1506
        String CurrLine = "", SeqLocation = "";
        @SuppressWarnings("unused")
        boolean wholeIR = false, pAcutoff = false, SJcutoff = false, alldone = false, noSJ = false;
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        // Import sequences
        SeqLocation = fastaFileName;
        ArrayList<String> finalTransSpliceList = new ArrayList<>();
        ArrayList<String> finalPolyAList = new ArrayList<>();

        input: while (!alldone) {
            System.out.print(" Scan whole IR (last 5000nt ) instead of upstream putative Trans-Splice Juntion ?"
                    + "\n (y/n, default n) ");
            String IR = keyboard.readLine();
            if (IR.equals("y"))
                wholeIR = true;
            else if (!IR.equals("n")) {
                System.out.println("Please enter simple, lowercase 'y' or 'n'...");
                continue input;
            }
            System.out.print(" Limit false-positives for poly(A) prediction "
                    + "(might not emit predictions for all sequences) ?\n (y/n, default n) ");
            String pAt = keyboard.readLine();
            if (pAt.equals("y"))
                pAcutoff = true;
            else if (!pAt.equals("n")) {
                System.out.println("Please enter simple, lowercase 'y' or 'n'...");
                continue input;
            }
            System.out.print(" Limit false-positives for splice junction prediction "
                    + "(might not emit predictions for all sequences) ?\n (y/n, default n) ");
            String SJt = keyboard.readLine();
            if (SJt.equals("y"))
                SJcutoff = true;
            else if (!SJt.equals("n")) {
                System.out.println("Please enter simple, lowercase 'y' or 'n'...");
                continue input;
            }
            alldone = true;
        }
        System.out.println(SeqLocation);
        String[][] SeqTAB = ImportFasta(SeqLocation);

        // Predict Splice Junctions
        int[] SJposTAB = PredictSJ(SeqTAB);
        // Build PSSM
        Scanner Line = new Scanner(new BufferedReader(new FileReader(pssmFileName))).useDelimiter("\n");
        while (Line.hasNext()) {
            pssmsize++;
            CurrLine = Line.next();
        }
        float[][] PSSM = new float[pssmsize][4];
        Line = new Scanner(new BufferedReader(new FileReader(pssmFileName))).useDelimiter("\n");
        int line = 0;
        while (Line.hasNext()) {
            CurrLine = Line.next();
            Scanner Tab = new Scanner(new BufferedReader(new StringReader(CurrLine))).useDelimiter("\t");
            for (int i = 0; i != 4; i++) {
                PSSM[line][i] = Float.parseFloat(Tab.next());
            }
            line++;
            Tab.close();
        }
        Line.close();
        // Let's scan some sequences !!!
        for (int i = 0; i != SeqTAB.length; i++) {
            System.out.println("Sequence \"" + SeqTAB[i][0].substring(1) + "\"");
            String[] infoSeq = SeqTAB[i][0].substring(1).split("__");
            String gene1 = infoSeq[0];
            String gene2 = infoSeq[1];
            String chromo = infoSeq[2];
            int begin = Integer.parseInt(infoSeq[3]);
            int end = Integer.parseInt(infoSeq[4]);
            @SuppressWarnings("unused")
            int length = Integer.parseInt(infoSeq[5]);
            char strand = infoSeq[6].charAt(0);

            boolean passTest = false;
            if (SJposTAB[i] == -1) {
                System.out.println("  No splice junction prediction above threshold, enforcing whole IR scanning");
                wholeIR = true;
                noSJ = true;
            } else {
                System.out.print("  Putative trans-splicing site : " + (SJposTAB[i] - 800 + 1) + ", ");
                if (SeqTAB[i][1].length() - 800 > SJposTAB[i]) {
                    System.out.println((SeqTAB[i][1].length() - 800 - SJposTAB[i] - 1) + " bases upstream of ATG");
                    String row = chromo + "\tPredTerm\ttrans-splice\t";
                    if (strand == '+') {
                        int beginSite = begin + SJposTAB[i] - 25;
                        int endSite = beginSite + 50;
                        row += beginSite + "\t" + endSite + "\t";
                        row += "0\t" + strand + "\t.\tgene_id \"" + gene1 + "\";";
                    } else {
                        int beginSite = end - SJposTAB[i] - 25;
                        int endSite = beginSite + 50;
                        row += beginSite + "\t" + endSite + "\t";
                        row += "0\t" + strand + "\t.\tgene_id \"" + gene2 + "\";";
                    }
                    finalTransSpliceList.add(row);
                    passTest = true;
                } else {
                    System.out.println((SeqTAB[i][1].length() - 800 - SJposTAB[i] - 1) + " bases downstream of ATG"
                            + "\n     ** Possible Faulty ATG Annotation **");
                }
            }

            if (passTest) {
                double score = 0, highest = 0;
                double[] big = new double[SeqTAB[i][1].length() - 1600 + downCDS],
                        small = new double[SeqTAB[i][1].length() - 1600 + downCDS];
                int matrix = 0, position = 0;
                for (int p = 800 - up1; p < SeqTAB[i][1].length() - 800 - up1; p++) {
                    score = 0;
                    for (int l = 0; l < up1 + down1; l++) {
                        switch (SeqTAB[i][1].charAt(p + l)) {
                            case 'A': {
                                score = score + PSSM[l + (500 - up1)][0];
                                break;
                            }
                            case 'T': {
                                score = score + PSSM[l + (500 - up1)][1];
                                break;
                            }
                            case 'C': {
                                score = score + PSSM[l + (500 - up1)][2];
                                break;
                            }
                            case 'G': {
                                score = score + PSSM[l + (500 - up1)][3];
                                break;
                            }
                            // uncomment this line to CONSIDER AMBIGUITY CHARACTERS as position mean
                            // case 'N': { score = (double) score +
                            // (pssm1[l][0]+pssm1[l][1]+pssm1[l][2]+pssm1[l][3])/4
                            // ;
                        }
                    }
                    big[matrix] = score;
                    score = 0;
                    for (int l = 0; l < up2 + down2; l++) {
                        switch (SeqTAB[i][1].charAt(p + l)) {
                            case 'A': {
                                score = score + PSSM[l + (500 - up2)][0];
                                break;
                            }
                            case 'T': {
                                score = score + PSSM[l + (500 - up2)][1];
                                break;
                            }
                            case 'C': {
                                score = score + PSSM[l + (500 - up2)][2];
                                break;
                            }
                            case 'G': {
                                score = score + PSSM[l + (500 - up2)][3];
                                break;
                            }
                            // uncomment this line to CONSIDER AMBIGUITY CHARACTERS as position mean
                            // case 'N': { score = (double) score +
                            // (pssm1[l][0]+pssm1[l][1]+pssm1[l][2]+pssm1[l][3])/4
                            // ;
                        }
                    }
                    small[matrix] = score;
                    matrix++;
                }
                // WHOLE INTERGENIC SCANNING
                if (wholeIR) {
                    for (int b = 0; b != big.length; b++) {
                        for (int s = Math.max(0, b - upYY); s < Math.min(b + upYY,
                                SeqTAB[i][1].length() - 1600 + downCDS); s++) {
                            if (big[b] + small[s] >= highest) {
                                highest = big[b] + small[s];
                                position = s;
                            }
                        }
                    }
                    if (pAcutoff && highest < pAthreshold) {
                        System.out.println("  Highest scoring Poly(A) position, " + position
                                + ", under relevant threshold" + "\n==================================");
                    } else {
                        System.out.println("  Putative Poly(A) site : " + position + " from STOP codon"
                                + "\n==================================");
                        String row = chromo + "\tPredTerm\tPolyA\t";
                        if (strand == '+') {
                            int beginSite = begin + position + 800 - 25;
                            int endSite = beginSite + 50;
                            row += beginSite + "\t" + endSite + "\t";
                            row += "0\t" + strand + "\t.\tgene_id \"" + gene1 + "\";";
                        } else {
                            int beginSite = end - position + 800 - 25;
                            int endSite = beginSite + 50;
                            row += beginSite + "\t" + endSite + "\t";
                            row += "0\t" + strand + "\t.\tgene_id \"" + gene2 + "\";";
                        }
                        finalPolyAList.add(row);

                    }
                } else // Default Splice-Junction distance bounding PSSM scanning
                {
                    for (int b = Math.max(0, SJposTAB[i] - upSJ - 800); b <= SJposTAB[i] - 800; b++) {
                        for (int s = Math.max(0, b - upYY); s < Math.min(b + upYY,
                                SeqTAB[i][1].length() - 1600 + downCDS); s++) {
                            if (big[b] + small[s] >= highest) {
                                highest = big[b] + small[s];
                                position = s;
                            }
                        }
                    }
                    if (pAcutoff && highest < pAthreshold) {
                        System.out.println("  Highest scoring Poly(A) position, " + position
                                + ", under relevant threshold" + "\n==================================");
                    } else {
                        System.out.println("  Putative Poly(A) site : " + position + " from STOP codon"
                                + "\n==================================");
                        String row = chromo + "\tPredTerm\tPolyA\t";
                        if (strand == '+') {
                            int beginSite = begin + position + 800 - 25;
                            int endSite = beginSite + 50;
                            row += beginSite + "\t" + endSite + "\t";
                            row += "0\t" + strand + "\t.\tgene_id \"" + gene1 + "\";";
                        } else {
                            int beginSite = end - position + 800 - 25;
                            int endSite = beginSite + 50;
                            row += beginSite + "\t" + endSite + "\t";
                            row += "0\t" + strand + "\t.\tgene_id \"" + gene2 + "\";";
                        }
                        finalPolyAList.add(row);
                    }
                }
                if (noSJ) {
                    noSJ = false;
                    wholeIR = false;
                }
            }
            TabDelimitedTableReader.saveList(finalTransSpliceList, finalGTFFile + "_TransSplice.gtf");
            TabDelimitedTableReader.saveList(finalPolyAList, finalGTFFile + "_PolySite.gtf");
        }
    }

    // This method predicts the TRANS-SPLICING acceptor site
    private static int[] PredictSJ(String[][] SequenceTAB) throws IOException {
        int[] SJ = new int[SequenceTAB.length];
        for (int i = 0; i != SequenceTAB.length; i++) {
            String Segment = "", Region = "";
            int seg_len = 0, char_counter = 0, endpos = 0,
                    // for testing optimal function coefficient
                    constant = 150;
            double best = 0, score = 0, best_end = 0;
            Region = SequenceTAB[i][1].substring(SequenceTAB[i][1].length() - 800 - upCDS,
                    SequenceTAB[i][1].length() - 800 + downCDS);
            @SuppressWarnings("resource")
            Scanner AG = new Scanner(new BufferedReader(new StringReader(Region))).useDelimiter("AG");
            while (AG.hasNext()) {
                Segment = AG.next();
                seg_len = Segment.length() + 2; // +2 for the AG excluded from sequence by Scanner
                char_counter = 0;
                endpos = endpos + seg_len;
                char curr_char;
                for (int z = 0; z < seg_len - 2; z++) // includes final AG
                {
                    curr_char = Segment.charAt(z);
                    if (z == Segment.length() - 1)
                        break; // neglect final segment (no AG!)
                    else if (curr_char == 'C' || curr_char == 'T')
                        char_counter++;
                }
                // linear scoring function
                score = seg_len + seg_len * constant * ((double) char_counter / seg_len - 0.55);
                // POLYNOMIAL scoring function (comment-out previous line if used and change
                // SJthreshold to 125)
                // score = seg_len + seg_len* constant *Math.pow(((double)char_counter/seg_len -
                // 0.55), 3) ;
                if (score > best) {
                    best_end = endpos;
                    best = score;
                }
            }
            AG.close();
            if (SJcutoff && best < SJthreshold)
                SJ[i] = -1;
            else
                SJ[i] = (int) best_end + SequenceTAB[i][1].length() - 800 - upCDS;
        }
        return SJ;
    }

    // This method processes a .fasta formatted sequence file input into predaterm
    private static String[][] ImportFasta(String File_location) throws IOException {
        int sequences = 0;
        @SuppressWarnings("unused")
        String Sequence = "", TempScan = "";
        String[][] SeqTAB;
        // create String array
        @SuppressWarnings("resource")
        Scanner Seq = new Scanner(new BufferedReader(new FileReader(File_location))).useDelimiter(">");
        while (Seq.hasNext()) {
            sequences++;
            Sequence = Seq.next();
        }
        Seq.close();
        SeqTAB = new String[sequences][2];
        // import sequences
        @SuppressWarnings("resource")
        Scanner X = new Scanner(new BufferedReader(new FileReader(File_location))).useDelimiter("\n");
        int i = -1;
        boolean IsFirst = true;
        while (X.hasNext()) {
            TempScan = X.next();
            if (TempScan.charAt(0) == '>') {
                i++;
                SeqTAB[i][0] = TempScan;
                IsFirst = true;
            } else if (IsFirst) {
                IsFirst = false;
                SeqTAB[i][1] = TempScan.toUpperCase();
            } else
                SeqTAB[i][1] = SeqTAB[i][1] + TempScan.toUpperCase();
        }
        X.close();
        return SeqTAB;
    }

}
