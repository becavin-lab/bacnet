package bacnet.datamodel.sequence;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import bacnet.utils.BasicColor;

public class CompoundColorization {

    /**
     * Given a Nucleotide, this method return the corresponding color
     * 
     * @param i
     * @return
     */
    public static Color getNucleotideColor(String nucl, Display display) {
        if (nucl.equals("O"))
            // WHITE
            return new Color(display, 255, 255, 255);
        else if (nucl.equals("A"))
            // BLUE
            return new Color(display, 0, 0, 255);
        else if (nucl.equals("T"))
            // GREEN
            return new Color(display, 0, 150, 150);
        else if (nucl.equals("U"))
            // GREEN
            return new Color(display, 0, 150, 150);
        else if (nucl.equals("G"))
            // RED
            return new Color(display, 255, 0, 0);
        else if (nucl.equals("C"))
            // ORANGE
            return new Color(display, 255, 127, 0);
        else if (nucl.equals("N"))
            // RED
            return new Color(display, 255, 127, 0);
        else if (nucl.equals("N"))
            // RED
            return new Color(display, 127, 255, 127);
        // BLACK
        else
            return new Color(display, 0, 0, 0);

    }

    /**
     * Given an Amino Acid, this method return the corresponding color
     * 
     * @param i
     * @return
     */
    public static Color getAAcidColor(String nucl, Display display) {
        if (nucl.equals("R") || nucl.equals("H") || nucl.equals("K")) { // positively charged AA
            return BasicColor.BLUE;
        } else if (nucl.equals("D") || nucl.equals("E")) { // negatively charged AA
            return BasicColor.RED;
        } else if (nucl.equals("S") || nucl.equals("T") || nucl.equals("N") || nucl.equals("Q")) { // polar uncharged
                                                                                                   // side chains
            return BasicColor.ORANGE;
        } else if (nucl.equals("C") || nucl.equals("U") || nucl.equals("G") || nucl.equals("P")) { // special cases
            return BasicColor.PURPLE;
        } else if (nucl.equals("-")) {
            return BasicColor.GREY;
        } else { // hydrophobic chain
            return BasicColor.GREY;
        }
    }

}
