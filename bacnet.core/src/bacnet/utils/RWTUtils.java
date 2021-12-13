package bacnet.utils;

import java.util.HashMap;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Tools for saving files<br>
 * Everything in bacnet.raprcp package is specific to eclipse.rap or eclipse.rcp. Be careful to
 * comment or not when changing environment
 *
 * @author Christophe Bécavin
 *
 */
public class RWTUtils {

    public static String PRELOADED_ITEMS = "org.eclipse.rap.rwt.preloadedItems"; // RWT.PRELOADED_ITEMS
    public static String MARKUP_ENABLED = "org.eclipse.rap.rwt.markupEnabled"; // RWT.MARKUP_ENABLED;

    public static void setPreloadedItems(Composite composite) {
        composite.setData(PRELOADED_ITEMS, new int[10]);
    }

    public static void setMarkup(Composite composite) {
        composite.setData(MARKUP_ENABLED, Boolean.TRUE);
    }

    public static void setMarkup(Label label) {
        label.setData(MARKUP_ENABLED, Boolean.TRUE);
    }

    // private static void execJavaScript(String... strings) {
    // /*
    // * for eclipse.rap
    // */
    // StringBuilder builder = new StringBuilder();
    // builder.append("try{");
    // for (String str : strings) {
    // builder.append(str);
    // }
    // builder.append("}catch(e){}");
    // JavaScriptExecutor executor = RWT.getClient().getService(JavaScriptExecutor.class);
    // executor.execute(builder.toString());
    //
    // /*
    // * for eclipse.rcp
    // */
    // }

    public static String setPubMedLink(String text) {
        if (text.contains("PubMed") || text.contains("Pubmed")) {
            String pubmedLink = text.substring(text.indexOf(':') + 1, text.indexOf(')'));
            return "<a href='" + getPubMedLink(pubmedLink) + "' target='_blank'>" + text + "</a>";
        } else {
            return text;
        }
    }

    public static String getPubMedLink(String pubmedID) {
        return "https://www.ncbi.nlm.nih.gov/pubmed/" + pubmedID;
    }

    public static String setArrayExpressExpLink(String expID) {
        if (expID.startsWith("E-")) {
            return "<a href='" + getArrayExpressExpLink(expID) + "' target='_blank'>" + expID + "</a>";
        } else if (expID.startsWith("PRJ")) {
            return "<a href='" + getENALink(expID) + "' target='_blank'>" + expID + "</a>";
        } else {
            return expID;
        }
    }
    
    public static String setPrideLink(String expID) {
        if (expID.startsWith("PRD")) {
            return "<a href='" + getPrideLink(expID) + "' target='_blank'>" + expID + "</a>";
        }else if (expID.startsWith("PXD")) {
            return "<a href='" + getPrideLink(expID) + "' target='_blank'>" + expID + "</a>";
        } else {
            return expID;
        }
    }

    /**
     * Get pride database link to a specific project
     * @param expID
     * @return
     */
    public static String getPrideLink(String expID) {
        return "https://www.ebi.ac.uk/pride/archive/projects/" + expID;
    }
    
    /**
     * Get ArrayExpress database link to a specific project
     * @param expID
     * @return
     */
    public static String getArrayExpressExpLink(String expID) {
        return "https://www.ebi.ac.uk/arrayexpress/experiments/" + expID;
    }

    public static String getENALink(String enaID) {
        return "http://www.ebi.ac.uk/ena/data/view/" + enaID;
    }

    public static String setArrayExpressArrayLink(String arrayID) {
        if (arrayID.startsWith("A-")) {
            return "<a href='" + getArrayExpressArrayLink(arrayID) + "' target='_blank'>" + arrayID + "</a>";
        } else {
            return arrayID;
        }
    }

    public static String getArrayExpressArrayLink(String arrayID) {
        return "https://www.ebi.ac.uk/arrayexpress/arrays/" + arrayID;
    }

    public static String setGenomeNCBILink(String text) {
        return "<a href='" + getGenomeNCBILink(text) + "' target='_blank'>" + text + "</a>";
    }

    public static String getGenomeNCBILink(String genomeID) {
        return "https://www.ncbi.nlm.nih.gov/nuccore/" + genomeID;
    }

    public static String setProteinNCBILink(String proteinID) {
        return "<a href='" + getProteinNCBILink(proteinID) + "' target='_blank'>" + proteinID + "</a>";
    }

    public static String getProteinNCBILink(String proteinID) {
        return "https://www.ncbi.nlm.nih.gov/protein/" + proteinID;
    }

    public static String setSrnaLink(String reference, HashMap<String, String> sRNARefToLink) {
        String[] allLinks = reference.split(";");
        String text = "";
        for (String link : allLinks) {
            if (sRNARefToLink.containsKey(link)) {
                String idRef = sRNARefToLink.get(link);
                if (idRef.contains("rfam")) {
                    text += "<a href='" + idRef + "' target='_blank'>" + link + "</a>;";
                } else {
                    text += "<a href='" + getPubMedLink(idRef) + "' target='_blank'>" + link + "</a>;";
                }
            }
        }
        text = text.substring(0, text.length() - 1);
        return text;
    }

}
