package bacnet.genomeBrowser.core;

import java.io.Serializable;

public class Zoom implements Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 5926414787821636159L;

    // this parameter fix the number of zoom step which are allowed in the software
    public static int ZOOM_NB_ALLOWED = 20;
    // this paremeter fix the number of base pair to display when the zoom is the
    // highest
    public static int MINIMAL_BP_NB = 100;

    // total number of bp which might be displayed
    private int totalBP = 30;
    // number of zoom
    private int zoomNumber = 30;
    // ratio applyed each time we zoomUp or zoomDown
    private double zoomRatio = 1;

    // this is the actual positions of the zoom
    private int zoomPosition = 1;
    public boolean zoomChangedFlag = false;
    /**
     * Position of the zoom for height
     */
    private int zoomHeightPosition = 5;
    /**
     * Maximal value for the zoom
     */
    private int zoomHeightMax = 20;

    public Zoom(int bpNumber) {
        setTotalBP(bpNumber);
        // this.zoomNumber = calcZoomNumber();
        this.zoomRatio = calcZoomRatio();
    }

    public int calcZoomNumber() {
        int zoomNB = (int) Math.log10(totalBP);
        if (zoomNB > ZOOM_NB_ALLOWED)
            System.err.println("The data displayed are too big for this Viewer");
        return zoomNB;

    }

    /**
     * We have MINMIAL_BP_NB * zoomRatio^zoomNumber = totalBP <br>
     * So: zoomRatio = (totalBP/MIN_NB_BP)^(1/zoomNumber)
     * 
     * @return
     */
    public double calcZoomRatio() {
        // calc (nbBP/minnbBP)^(-zoomNB)
        double zoomRatio = Math.pow((double) totalBP / (double) MINIMAL_BP_NB, 1 / (double) (zoomNumber - 1));
        double verif = MINIMAL_BP_NB * Math.pow(zoomRatio, zoomNumber);
        // System.out.println("Verif: "+verif);

        return zoomRatio;
    }

    /**
     * Change the displayRegion according to zoom parameters
     * 
     * @param displayRegion
     * @param boolean indicating a zoomOut or zoomIn
     * @return
     */
    public Region zoomRegion(Region displayRegion, boolean in) {
        if (!in) {
            if (zoomPosition != 1) {
                zoomPosition--;
                zoomChangedFlag = true;
                if (zoomPosition == 1) {
                    displayRegion.zoomRegion(totalBP);
                } else {
                    int newWidth = (int) (displayRegion.getWidth() * zoomRatio);
                    displayRegion.zoomRegion(newWidth);

                }
            }
        } else {
            if (zoomPosition != zoomNumber) {
                zoomPosition++;
                zoomChangedFlag = true;
                if (zoomPosition == zoomNumber) {
                    displayRegion.zoomRegion(MINIMAL_BP_NB);
                } else {
                    int newWidth = (int) (displayRegion.getWidth() / zoomRatio);
                    displayRegion.zoomRegion(newWidth);
                }
            }
        }
        return displayRegion;
    }

    // **************************************
    // ******* Getters and Setters ******
    // **************************************
    public int getTotalBP() {
        return totalBP;
    }

    public void setTotalBP(int totalBP) {
        this.totalBP = totalBP;
    }

    public int getZoomNumber() {
        return zoomNumber;
    }

    public void setZoomNumber(int zoomNumber) {
        this.zoomNumber = zoomNumber;
    }

    public double getZoomRatio() {
        return zoomRatio;
    }

    public void setZoomRatio(double zoomRatio) {
        this.zoomRatio = zoomRatio;
    }

    public int getZoomPosition() {
        return zoomPosition;
    }

    public void setZoomPosition(int zoomPosition) {
        this.zoomPosition = zoomPosition;
    }

    /**
     * Search the zoom position which correspond the best to displayRegion DisplayRegion should be
     * inferior to display range given by the zoom
     * 
     * @param displayRegion
     */
    public void setZoomPosition(Region displayRegion) {
        int lengthDisplay = displayRegion.getWidth();
        int lengthRange = totalBP;
        zoomPosition = 1;
        while (lengthRange > lengthDisplay && zoomPosition < zoomNumber) {
            zoomPosition++;
            lengthRange = (int) (lengthRange / zoomRatio);
        }
    }

    public boolean isZoomChangedFlag() {
        return zoomChangedFlag;
    }

    public void setZoomChangedFlag(boolean zoomChangedFlag) {
        this.zoomChangedFlag = zoomChangedFlag;
    }

    /**
     * Position of the zoom for height
     */
    public int getZoomHeightPosition() {
        return zoomHeightPosition;
    }

    public void setZoomHeightPosition(int zoomHeightPosition) {
        this.zoomHeightPosition = zoomHeightPosition;
    }

    /**
     * Maximal value for the zoom
     */
    public int getZoomHeightMax() {
        return zoomHeightMax;
    }

    public void setZoomHeightMax(int zoomHeightMax) {
        this.zoomHeightMax = zoomHeightMax;
    }

    @Override
    public String toString() {
        String ret =
                "ZoomPos " + zoomPosition + " zoomNB " + zoomNumber + " zoomRatio " + zoomRatio + " totBP " + totalBP;
        return ret;
    }

    /**
     * Clone the object
     */
    @Override
    public Zoom clone() {
        Zoom o = null;
        try {
            o = (Zoom) super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }
        return o;
    }
}
