package bacnet.genomeBrowser;

import javax.inject.Inject;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import bacnet.genomeBrowser.core.Track;
import bacnet.genomeBrowser.tracksGUI.TracksComposite;
import bacnet.reader.TabDelimitedTableReader;

public class TestGenomeBrowser {

    private TracksComposite trackCompo;
    private Track track;

    @Inject
    EPartService partService;

    @Inject
    public TestGenomeBrowser() {

    }

    @Inject
    public TestGenomeBrowser(TracksComposite trackCompo, Track track) {
        this.trackCompo = trackCompo;
        this.track = track;
    }

    public void setRelativeValueDisplay() {
        System.out.println("Select relative value display");
        track.getDatas().setDisplayAbsoluteValue(false);
        track.getDatas().absoluteTOrelativeValue();
        trackCompo.setTrack(track);
        trackCompo.getComboAbsoluteRelative().select(1);
    }

    public void zoomOut() {
        track.zoom(false);
        if (track.getZoom().getZoomPosition() < 27)
            track.setDisplaySequence(false);
        trackCompo.redrawAllCanvas();
        trackCompo.setHorizontalBarProperties();
    }

    public void zoomIn() {
        track.zoom(true);
        trackCompo.redrawAllCanvas();
        trackCompo.setHorizontalBarProperties();
    }

    public void search(String text) {
        if (track.search(text)) {
            trackCompo.moveHorizontally(track.getDisplayRegion().getMiddleH());
            trackCompo.redrawAllCanvas();
            trackCompo.setHorizontalBarProperties();
        }
    }

    public static void run() {
        String[] positions = {"102555", "lmo0204", "lmo2556", "1000020"};
        for (String position : positions) {
            test1(position);
            test2(position);
            test3(position);
            compareResults(position);
        }
    }

    private static void compareResults(String position) {
        System.out.println("Compare results " + position);
        String[][] arrayTest1 = TabDelimitedTableReader.read("D:/test1.excel");
        String[][] arrayTest2 = TabDelimitedTableReader.read("D:/test2.excel");
        String[][] arrayTest3 = TabDelimitedTableReader.read("D:/test3.excel");

        String[][] arrayTest1vsTest2 = new String[arrayTest2[0].length][arrayTest1.length];
        String[][] arrayTest1vsTest3 = new String[arrayTest2[0].length][arrayTest1.length];

        boolean same1 = true;
        boolean same2 = true;
        for (int i = 0; i < arrayTest1.length; i++) {
            for (int j = 1; j < arrayTest2[0].length; j++) {
                arrayTest1vsTest2[0][i] = arrayTest1[i][0];
                arrayTest1vsTest3[0][i] = arrayTest1[i][0];
                String test1 = arrayTest1[i][j];
                String test2 = arrayTest2[i][j];
                String test3 = arrayTest3[i][j];
                if (test1.equals(test2)) {
                    arrayTest1vsTest2[j][i] = "Same:" + test1;
                } else {
                    arrayTest1vsTest2[j][i] = test1 + " vs " + test2;
                    if (same1) {
                        System.out.println("Difference found test1 vs test2:" + arrayTest1[i][0] + " " + j);
                        System.out.println(test1 + " vs " + test2);
                        same1 = false;
                    }

                }
                if (test1.equals(test3)) {
                    arrayTest1vsTest3[j][i] = "Same:" + test1;
                } else {
                    arrayTest1vsTest3[j][i] = test1 + " vs " + test3;
                    if (same2) {
                        System.out.println("Difference found test1 vs test3:" + arrayTest1[i][0] + " " + j);
                        System.out.println(test1 + " vs " + test3);
                        same2 = false;
                    }
                }
            }
        }

        System.out.println("Test1 and Test 2 are similar: " + same1);
        System.out.println("Test1 and Test 3 are similar: " + same2);

        TabDelimitedTableReader.save(arrayTest1vsTest2, "D:/test1vstest2.excel");
        TabDelimitedTableReader.save(arrayTest1vsTest3, "D:/test1vstest3.excel");
    }

    private static void test3(String position) {
        TestGenomeBrowser testBrowser = new TestGenomeBrowser();
        GenomeTranscriptomeView view = GenomeTranscriptomeView.displayTestView(testBrowser.partService);
        TestGenomeBrowser test = new TestGenomeBrowser(view.getTracksComposite(), view.getTrack());
        view.getTracksComposite().getCanvasData().setName("test1");
        // test.setRelativeValueDisplay();
        test.zoomOut();
        test.zoomOut();
        test.search(position);
        test.zoomOut();
        test.zoomIn();
        test.zoomOut();
        test.zoomOut();
        view.getTracksComposite().getCanvasData().setTestData(true);
        test.search(position);

    }

    private static void test2(String position) {
        TestGenomeBrowser testBrowser = new TestGenomeBrowser();
        GenomeTranscriptomeView view = GenomeTranscriptomeView.displayTestView(testBrowser.partService);
        TestGenomeBrowser test = new TestGenomeBrowser(view.getTracksComposite(), view.getTrack());
        view.getTracksComposite().getCanvasData().setName("test2");
        // test.setRelativeValueDisplay();
        test.search(position);
        test.zoomOut();
        test.zoomIn();
        test.zoomOut();
        test.zoomOut();
        test.zoomOut();
        test.zoomIn();
        test.zoomOut();
        test.zoomOut();
        view.getTracksComposite().getCanvasData().setTestData(true);
        test.search(position);
    }

    private static void test1(String position) {
        TestGenomeBrowser testBrowser = new TestGenomeBrowser();
        GenomeTranscriptomeView view = GenomeTranscriptomeView.displayTestView(testBrowser.partService);
        TestGenomeBrowser test = new TestGenomeBrowser(view.getTracksComposite(), view.getTrack());
        view.getTracksComposite().getCanvasData().setName("test3");
        // test.setRelativeValueDisplay();
        test.zoomOut();
        test.zoomOut();
        test.zoomOut();
        test.zoomOut();
        view.getTracksComposite().getCanvasData().setTestData(true);
        test.search(position);
    }

}
