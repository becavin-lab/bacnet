package bacnet.datamodel.dataset;

import java.io.Serializable;

/**
 * OmicsData class inherited everything from ExpressionMatrix, the only difference is the TypeDate
 * value, it a Proteome data.<br>
 * It is not very useful for the moment but will be in the futur
 * 
 * @author UIBC
 *
 */
public class ProteomicsData extends ExpressionMatrix implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7625087278597156645L;

    public ProteomicsData() {
        super();
        this.setType(TypeData.Proteome);
    }

    /**
     * Read compressed, serialized data with a FileInputStream. Uncompress that data with a
     * GZIPInputStream. Deserialize the vector of lines with a ObjectInputStream. Replace current data
     * with new data, and redraw everything.
     */
    public void load() {
        System.out.println(" load proteomics data : " +OmicsData.PATH_STREAMING + this.getName() + EXTENSION);
        ExpressionMatrix matrixLoaded = ExpressionMatrix.load(OmicsData.PATH_STREAMING + this.getName() + EXTENSION);
        this.setAnnotations(matrixLoaded.getAnnotations());
        this.setBioCondName(matrixLoaded.getBioCondName());
        this.setDate(matrixLoaded.getDate());
        this.setHeaderAnnotation(matrixLoaded.getHeaderAnnotation());
        this.setHeaders(matrixLoaded.getHeaders());
        this.setFirstRowName(matrixLoaded.getFirstRowName());
        this.setSecondRowName(matrixLoaded.getSecondRowName());
        this.setName(matrixLoaded.getName());
        this.setNote(matrixLoaded.getNote());
        this.setOrdered(matrixLoaded.isOrdered(), matrixLoaded.getOrderedRowNames());
        this.setRawDatas(matrixLoaded.getRawDatas());
        this.setRowNames(matrixLoaded.getRowNames());
        this.setValues(matrixLoaded.getValues());
        this.setLoaded(true);
    }

}
