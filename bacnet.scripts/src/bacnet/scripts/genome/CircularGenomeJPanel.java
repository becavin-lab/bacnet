package bacnet.scripts.genome;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JPanel;

import org.biojava3.core.sequence.DNASequence;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequence.GenomeNCBI;
import bacnet.datamodel.sequence.NcRNA;
import bacnet.datamodel.sequence.Operon;
import bacnet.datamodel.sequence.Sequence;
import bacnet.datamodel.sequence.Srna;
import bacnet.datamodel.sequence.Srna.TypeSrna;
import bacnet.datamodel.sequenceNCBI.GeneNCBITools;
import bacnet.utils.BasicColor;
import ca.ualberta.stothard.cgview.Cgview;
import ca.ualberta.stothard.cgview.CgviewConstants;
import ca.ualberta.stothard.cgview.Feature;
import ca.ualberta.stothard.cgview.FeatureRange;
import ca.ualberta.stothard.cgview.FeatureSlot;

/**
 * Circular Genome Panel created for displaying Small RNAs
 * 
 * @author UIBC
 *
 */
public class CircularGenomeJPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Cgview cgview;

	public Genome genome;
	public LinkedHashMap<String, Srna> sRNAs;
	public LinkedHashMap<String, Operon> operons;

	public CircularGenomeJPanel(int width, int height, GenomeNCBI genome, String title) {

		int length = genome.getChromosomes().get(0).getLength();
		cgview = new Cgview(length);

		// some optional settings
		cgview.setWidth(height);
		cgview.setHeight(width);
		cgview.setBackboneRadius(200f);
		// cgview.setBackgroundColor(Color.BLACK);
		// cgview.setTitle(title);
		cgview.setLabelPlacementQuality(100);
		cgview.setShowWarning(true);
		cgview.setLabelLineLength(8.0d);
		cgview.setLabelLineThickness(0.5f);
		cgview.setDrawTickMarks(false);
		cgview.setDrawLegends(false);
		// cgview.setIsLinear(true);
		cgview.setTitleFont(new Font(getFont().getFontName(), Font.ITALIC, 30));
		// cgview.setLabelFont(new Font(getFont().getFontName(), Font.PLAIN, 12));

		// create a FeatureSlot to hold sequence features
		// FeatureSlot featureSlotOperon = new FeatureSlot(cgview,
		// Cgview.REVERSE_STRAND);
		FeatureSlot featureSlotPlusGene = new FeatureSlot(cgview, CgviewConstants.REVERSE_STRAND);
		FeatureSlot featureSlotMinusGene = new FeatureSlot(cgview, CgviewConstants.REVERSE_STRAND);

		for (DNASequence gene : genome.getCodingSequencesList(false)) {
			String strand = GeneNCBITools.getStrand(gene);
			if (strand.equals("-")) {
				Feature feature = new Feature(featureSlotMinusGene, gene.getAccession().toString());
				feature.setColor(Color.BLUE);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				// a single FeatureRange to add the Feature
				int begin = gene.getBioBegin();
				int end = gene.getBioEnd();
				if (begin > end) {
					int temp = end;
					end = begin;
					begin = temp;
				}
				FeatureRange featureRange = new FeatureRange(feature, begin, end);
				featureRange.setDecoration(CgviewConstants.DECORATION_COUNTERCLOCKWISE_ARROW);
			} else {
				Feature feature = new Feature(featureSlotPlusGene, gene.getAccession().toString());
				feature.setColor(Color.RED);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				// a single FeatureRange to add the Feature
				int begin = gene.getBioBegin();
				int end = gene.getBioEnd();
				FeatureRange featureRange = new FeatureRange(feature, begin, end);
				featureRange.setDecoration(CgviewConstants.DECORATION_CLOCKWISE_ARROW);
			}
		}
	}

	/**
	 * Run circularGenome for Listeriomics Circos network graph
	 * 
	 * @param width
	 * @param height
	 * @param genome
	 * @param title
	 */
	public CircularGenomeJPanel(int width, int height, Genome genome, String title) {

		// sRNACircularGenome(width,height,genome,title);
		RNABindingCircularGenome(width, height, genome, title);
	}

	/**
	 * Create the panel for displaying Srna.
	 */
	public CircularGenomeJPanel(int width, int height, TypeSrna typeSrna) {

		try {
			genome = Genome.loadEgdeGenome();
			if (typeSrna == TypeSrna.Srna) {
				sRNAs = genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getsRNAs();
			} else if (typeSrna == TypeSrna.ASrna) {
				sRNAs = genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getAsRNAs();
			} else {
				sRNAs = genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getCisRegs();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int length = genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getLength();
		cgview = new Cgview(length);

		// some optional settings
		cgview.setWidth(height);
		cgview.setHeight(width);
		cgview.setBackboneRadius(150f);
		// cgview.setBackboneRadius(120f);
		cgview.setDrawTickMarks(false);
		// cgview.setBackgroundColor(Color.BLACK);
		// cgview.setTitle("Listeria Monocytogenes EGD-e");
		// cgview.setLabelPlacementQuality(100);
		cgview.setShowWarning(true);
		// cgview.setLabelLineLength(3.0d);
		// cgview.setLabelLineThickness(0.1f);
		// cgview.setDrawLegends(true);
		// cgview.setIsLinear(true);
		// cgview.setTitleFont(new Font(getFont().getFontName(), Font.ITALIC, 12));
		// cgview.setLabelFont(new Font(getFont().getFontName(), Font.PLAIN, 12));

		// create a FeatureSlot to hold sequence features
		// FeatureSlot featureSlotOperon = new FeatureSlot(cgview,
		// Cgview.REVERSE_STRAND);
		FeatureSlot featureSlotPlusGene = new FeatureSlot(cgview, CgviewConstants.REVERSE_STRAND);
		FeatureSlot featureSlotMinusGene = new FeatureSlot(cgview, CgviewConstants.DIRECT_STRAND);

		FeatureSlot featureSlotPlusSrna = new FeatureSlot(cgview, CgviewConstants.DIRECT_STRAND);
		FeatureSlot featureSlotMinusSrna = new FeatureSlot(cgview, CgviewConstants.REVERSE_STRAND);

		for (Gene gene : genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getGenes().values()) {
			char strand = gene.getStrand();
			if (strand == '-') {
				Feature feature = new Feature(featureSlotMinusGene, gene.getName().toString());
				feature.setColor(Color.BLUE);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				// a single FeatureRange to add the Feature
				int begin = gene.getBegin();
				int end = gene.getEnd();
				if (begin > end) {
					int temp = end;
					end = begin;
					begin = temp;
				}
				FeatureRange featureRange = new FeatureRange(feature, begin, end);
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
			} else {
				Feature feature = new Feature(featureSlotPlusGene, gene.getName().toString());
				feature.setColor(Color.RED);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				// a single FeatureRange to add the Feature
				int begin = gene.getBegin();
				int end = gene.getEnd();
				FeatureRange featureRange = new FeatureRange(feature, begin, end);
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
			}

		}

		for (Srna sRNA : sRNAs.values()) {
			// a Feature to add to our FeatureSlot
			if (sRNA.isStrand()) {
				Feature feature = new Feature(featureSlotPlusSrna, sRNA.getName());
				feature.setColor(Color.RED);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				// a single FeatureRange to add the Feature
				int begin = sRNA.getBegin();
				int end = sRNA.getEnd();

				FeatureRange featureRange = new FeatureRange(feature, begin, end);
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);

				// featureRange.setHyperlink("sRNAs"+File.separator+sRNA.getName());
			} else {
				Feature feature = new Feature(featureSlotMinusSrna, sRNA.getName());
				feature.setColor(Color.BLUE);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);

				// a single FeatureRange to add the Feature
				int begin = sRNA.getBegin();
				int end = sRNA.getEnd();

				FeatureRange featureRange = new FeatureRange(feature, begin, end);
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
				// featureRange.setHyperlink("sRNAs"+File.separator+sRNA.getName());
			}
		}

	}

	public void sRNACircularGenome(int width, int height, Genome genome, String title) {
		int length = genome.getChromosomes().get(0).getLength();
		cgview = new Cgview(length);

		// some optional settings
		cgview.setWidth(height);
		cgview.setHeight(width);
		cgview.setBackboneRadius(350f);
		// cgview.setBackgroundColor(Color.BLACK);
		cgview.setTitle(title);
		cgview.setLabelPlacementQuality(100);
		cgview.setShowWarning(true);
		cgview.setLabelLineLength(8.0d);
		cgview.setLabelLineThickness(0.5f);
		cgview.setDrawTickMarks(true);
		cgview.setDesiredNumberOfTicks(5);

		cgview.setDrawLegends(true);
		cgview.setIsLinear(true);
		cgview.setTitleFont(new Font(getFont().getFontName(), Font.ITALIC, 30));
		// cgview.setLabelFont(new Font(getFont().getFontName(), Font.PLAIN, 12));

		// create a FeatureSlot to hold sequence features
		// FeatureSlot featureSlotOperon = new FeatureSlot(cgview,
		// Cgview.REVERSE_STRAND);
		FeatureSlot featureSlotPlusGene2 = new FeatureSlot(cgview, CgviewConstants.DIRECT_STRAND);
		FeatureSlot featureSlotPlusGene = new FeatureSlot(cgview, CgviewConstants.DIRECT_STRAND);
		// FeatureSlot featureSlotMinusGene = new FeatureSlot(cgview,
		// Cgview.REVERSE_STRAND);
		ArrayList<Sequence> sequences = new ArrayList<>();

		for (Sequence seq : genome.getChromosomes().get(0).getGenes().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(0).getsRNAs().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(0).getAsRNAs().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(0).getNcRNAs().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(0).getCisRegs().values())
			sequences.add(seq);

		for (Sequence sequence : sequences) {
			char strand = sequence.getStrand();
			Color color = Color.black;
			if (sequence instanceof Srna) {
				Srna sRNA = (Srna) sequence;
				if (sRNA.getTypeSrna() == TypeSrna.Srna)
					color = BasicColor.getAWTColor(BasicColor.LIGHT_SRNA);
				if (sRNA.getTypeSrna() == TypeSrna.ASrna)
					color = BasicColor.getAWTColor(BasicColor.LIGHT_ASRNA);
				if (sRNA.getTypeSrna() == TypeSrna.CisReg)
					color = BasicColor.getAWTColor(BasicColor.LIGHT_CISREG);
			}
			if (sequence instanceof NcRNA)
				color = BasicColor.getAWTColor(BasicColor.LIGHT_NCRNA);
			if (sequence instanceof Gene) {
				if (sequence.isStrand())
					color = BasicColor.getAWTColor(BasicColor.REDLIGHT_GENE);
				else
					color = BasicColor.getAWTColor(BasicColor.BLUELIGHT_GENE);
			}

			if (!sequence.isStrand()) {
				Feature feature = new Feature(featureSlotPlusGene2, sequence.getName());
				feature.setColor(color);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				FeatureRange featureRange = new FeatureRange(feature, sequence.getBegin(), sequence.getEnd());
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
			} else {
				Feature feature = new Feature(featureSlotPlusGene, sequence.getName());
				feature.setColor(color);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				FeatureRange featureRange = new FeatureRange(feature, sequence.getBegin(), sequence.getEnd());
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
			}
		}
		System.out.println();
	}

	/**
	 * Function to create a circular genome view of rnas binding to protein lom2686
	 * 
	 * @param width
	 * @param height
	 * @param genome
	 * @param title
	 */
	public void RNABindingCircularGenome(int width, int height, Genome genome, String title) {
		int length = genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getLength();
		cgview = new Cgview(length);

		// some optional settings
		cgview.setWidth(height);
		cgview.setHeight(width);
		cgview.setBackboneRadius(300f);
		// cgview.setBackgroundColor(Color.BLACK);
		cgview.setTitle("");
		cgview.setLabelPlacementQuality(100);
		cgview.setShowWarning(true);
		cgview.setLabelLineLength(18.0d);
		cgview.setLabelLineThickness(0.5f);
		cgview.setDrawTickMarks(true);
		cgview.setDesiredNumberOfTicks(5);

		cgview.setDrawLegends(false);
		cgview.setIsLinear(true);
		cgview.setTitleFont(new Font(getFont().getFontName(), Font.ITALIC, 30));
		// cgview.setLabelFont(new Font(getFont().getFontName(), Font.PLAIN, 12));

		/*
		 * Display genome
		 */

		// create a FeatureSlot to hold sequence features
		// FeatureSlot featureSlotOperon = new FeatureSlot(cgview,
		// Cgview.REVERSE_STRAND);
		FeatureSlot featureSlotPlusGene2 = new FeatureSlot(cgview, CgviewConstants.REVERSE_STRAND);
		FeatureSlot featureSlotPlusGene = new FeatureSlot(cgview, CgviewConstants.REVERSE_STRAND);
		// FeatureSlot featureSlotMinusGene = new FeatureSlot(cgview,
		// Cgview.REVERSE_STRAND);
		ArrayList<Sequence> sequences = new ArrayList<>();
		for (Sequence seq : genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getGenes().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getsRNAs().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getAsRNAs().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getNcRNAs().values())
			sequences.add(seq);
		for (Sequence seq : genome.getChromosomes().get(Genome.EGDE_CHROMO_NAME).getCisRegs().values())
			sequences.add(seq);

		for (Sequence sequence : sequences) {
			char strand = sequence.getStrand();
			Color color = Color.GRAY;
			// if(sequence instanceof Srna){
			// Srna sRNA = (Srna) sequence;
			// if(sRNA.getTypeSrna()==TypeSrna.Srna) color =
			// BasicColor.getAWTColor(BasicColor.LIGHT_SRNA);
			// if(sRNA.getTypeSrna()==TypeSrna.ASrna) color =
			// BasicColor.getAWTColor(BasicColor.LIGHT_ASRNA);
			// if(sRNA.getTypeSrna()==TypeSrna.CisReg) color =
			// BasicColor.getAWTColor(BasicColor.LIGHT_CISREG);
			// }
			// if(sequence instanceof NcRNA) color =
			// BasicColor.getAWTColor(BasicColor.LIGHT_NCRNA);
			// if(sequence instanceof Gene){
			// if(sequence.isStrand()) color =
			// BasicColor.getAWTColor(BasicColor.REDLIGHT_GENE);
			// else color = BasicColor.getAWTColor(BasicColor.BLUELIGHT_GENE);
			// }

			if (!sequence.isStrand()) {
				Feature feature = new Feature(featureSlotPlusGene2, sequence.getName());
				feature.setColor(color);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				FeatureRange featureRange = new FeatureRange(feature, sequence.getBegin(), sequence.getEnd());
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
			} else {
				Feature feature = new Feature(featureSlotPlusGene, sequence.getName());
				feature.setColor(color);
				feature.setShowLabel(CgviewConstants.LABEL_NONE);
				FeatureRange featureRange = new FeatureRange(feature, sequence.getBegin(), sequence.getEnd());
				featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
			}
		}

		/*
		 * Draw heatmap
		 */
		ExpressionMatrix matrix = ExpressionMatrix
				.loadTab("/Users/cbecavin/Documents/RNABindingProtein/Stat_Expression/RNABinding_All.txt", true);

		for (String header : matrix.getHeaders()) {
			int k = 0;
			if (header.equals("IP_LMO_BHI_LogFC") || header.equals("IP_LMO_Extract_FC")) {
				// if(!header.equals("IP_LMO_BHI_LogFC")) {
				FeatureSlot featureSlotDataPlus = new FeatureSlot(cgview, CgviewConstants.DIRECT_STRAND);
				FeatureSlot featureSlotDataMinus = new FeatureSlot(cgview, CgviewConstants.DIRECT_STRAND);

				for (String rowName : matrix.getRowNames().keySet()) {
					Sequence sequence = genome.getElement(rowName);
					double value = matrix.getValue(rowName, header);
					if (value > 1.5) {
						k++;
						Color color = Color.red;
						if (header.equals("IP_LMO_Extract_FC")) {
							color = Color.BLUE;
						} else if (header.equals("IP_LMO_Extract_FC")) {
							color = Color.GREEN;
						}
						if (sequence.isStrand()) {
							Feature feature = new Feature(featureSlotDataPlus, rowName);
							feature.setColor(color);
							feature.setShowLabel(CgviewConstants.LABEL_NONE);
							FeatureRange featureRange = new FeatureRange(feature, sequence.getBegin(),
									sequence.getEnd());
							featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
						} else {
							Feature feature = new Feature(featureSlotDataMinus, rowName);
							feature.setColor(color);
							feature.setShowLabel(CgviewConstants.LABEL_NONE);
							FeatureRange featureRange = new FeatureRange(feature, sequence.getBegin(),
									sequence.getEnd());

							featureRange.setDecoration(CgviewConstants.DECORATION_STANDARD);
						}
					}
				}
			}
			System.out.println(header + " : " + k);

		}

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		BufferedImage buffImage = new BufferedImage(cgview.getWidth(), cgview.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics2D graphics2D = buffImage.createGraphics();
		if (cgview.getDesiredZoom() > 1.0d) {
			cgview.drawZoomed(graphics2D, cgview.getDesiredZoom(), cgview.getDesiredZoomCenter(), false);
		} else {
			cgview.draw(graphics2D, false);
		}
		g.drawImage(buffImage, 0, 0, cgview.getWidth(), cgview.getHeight(), null);

	}

	public Cgview getCgview() {
		return cgview;
	}

	public void setCgview(Cgview cgview) {
		this.cgview = cgview;
	}

}
