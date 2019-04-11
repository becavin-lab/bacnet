package bacnet.scripts.core.stat;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math.stat.inference.TestUtils;
import bacnet.datamodel.dataset.OmicsData;
import jsc.independentsamples.MannWhitneyTest;
import jsc.independentsamples.PitmanTwoSampleTest;
import jsc.independentsamples.SmirnovTest;
import jsc.independentsamples.TwoSampleBootstrapMeansTest;

public class StatTest {

    public enum TypeStat {
        LOGFC, FC, TSTUDENTTILING, TSTUDENT, LNFCWT, STDEVWT, MANNWHIT, PERMUT, SMIRNOV, BOOTSTRAP, CORR, SPEARCORR, LPE, FDRBH, FDRBY, FDRBONF, AUTO_BOTH, AUTO_GENEXPR, AUTO_TILING, ARRAYSCORR
    };

    private TypeStat type = TypeStat.FC;

    public StatTest() {

    }

    public StatTest(TypeStat type) {
        this.setType(type);
    }

    public double run(double[] vector1, double[] vector2) {
        try {
            switch (type) {
                case TSTUDENT:
                    return TestUtils.tTest(vector1, vector2);
                case TSTUDENTTILING:
                    return TestUtils.tTest(vector1, vector2);
                case MANNWHIT:
                    MannWhitneyTest test1 = new MannWhitneyTest(vector1, vector2);
                    return test1.getSP();
                case PERMUT:
                    PitmanTwoSampleTest test2 = new PitmanTwoSampleTest(vector1, vector2);
                    return test2.getSP();
                case SMIRNOV:
                    SmirnovTest test3 = new SmirnovTest(vector1, vector2);
                    return test3.getSP();
                case BOOTSTRAP:
                    TwoSampleBootstrapMeansTest test4 =
                            new TwoSampleBootstrapMeansTest(vector1, vector2, vector1.length / 2);
                    return test4.getSP();
                case CORR:
                    return new PearsonsCorrelation().correlation(vector1, vector2);
                case SPEARCORR:
                    return new SpearmansCorrelation().correlation(vector1, vector2);
                default:
                    return 1;
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return OmicsData.MISSING_VALUE;
    }

    /*
     * *************************************************** Getters and Setters
     * 
     * ***************************************************
     */
    public TypeStat getType() {
        return type;
    }

    public void setType(TypeStat type) {
        this.type = type;
    }

}
