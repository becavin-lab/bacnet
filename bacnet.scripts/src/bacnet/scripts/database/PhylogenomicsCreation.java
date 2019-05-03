package bacnet.scripts.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;
import bacnet.Database;
import bacnet.datamodel.phylogeny.Phylogenomic;
import bacnet.datamodel.sequence.Gene;
import bacnet.datamodel.sequence.Genome;
import bacnet.datamodel.sequenceNCBI.GenomeNCBI;
import bacnet.reader.NCBIFastaHeaderParser;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.scripts.blast.BlastOutput.BlastOutputTYPE;
import bacnet.utils.ArrayUtils;
import bacnet.utils.CMD;
import bacnet.utils.FileUtils;

public class PhylogenomicsCreation {
    
	public static String IQTREE_PATH_WIN = "C:\\Users\\ipmc\\Documents\\BACNET\\Bacnet-private\\bacnet.scripts\\external\\iqtree-1.6.10-Windows\\bin\\iqtree.exe";
	public static String MAFFT_PATH_WIN = "C:\\Users\\ipmc\\Documents\\BACNET\\Bacnet-private\\bacnet.scripts\\external\\mafft-win\\mafft.bat";
	
	
	// ADD MAFFT parameters and run it on genomes
	// Run IQTree
	// Run FigTree
	
	
	public static String createPhylogenomicFigure(String logs) {
		String figure_output = Phylogenomic.PHYLO_GENOME_SVG;
		
		File file = new File(IQTREE_PATH_WIN);
		if(file.exists()) {
			String execProcess = IQTREE_PATH_WIN;
			try {
				logs += CMD.runProcess(execProcess);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {
			logs += "Cannot find IQTree software in : \n" + IQTREE_PATH_WIN + "\n- Install IQTree from : http://www.iqtree.org/#download\n- Modify IQTREE_PATH variable in bacnet.scripts.database.PhylogenomicsCreation\n\n";
		}
		return logs;
	}
	
}
