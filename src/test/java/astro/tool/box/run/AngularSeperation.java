package astro.tool.box.run;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.ConversionFactors.*;
import astro.tool.box.container.Couple;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.container.catalog.NoirlabCatalogEntry;
import astro.tool.box.container.catalog.PanStarrsCatalogEntry;
import astro.tool.box.container.catalog.SdssCatalogEntry;
import astro.tool.box.container.catalog.TwoMassCatalogEntry;
import astro.tool.box.container.catalog.UnWiseCatalogEntry;
import astro.tool.box.container.catalog.VhsCatalogEntry;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.util.CSVParser;
import static astro.tool.box.util.Constants.LINE_SEP;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Test;

public class AngularSeperation {

    private final List<Couple<NumberPair, NumberPair>> pairs = new ArrayList();

    @Before
    public void populatePairList() {
        pairs.add(new Couple(new NumberPair(30.2018838, -47.1321787), new NumberPair(30.1968786, -47.1373651)));
        pairs.add(new Couple(new NumberPair(305.9567781, -48.7430579), new NumberPair(305.9533633, -48.7452881)));
        pairs.add(new Couple(new NumberPair(0, 0), new NumberPair(0, 0)));
        pairs.add(new Couple(new NumberPair(353.2604400, -63.7892751), new NumberPair(353.2612406, -63.7885657)));
        pairs.add(new Couple(new NumberPair(196.3634786, -22.7912345), new NumberPair(196.3634238, -22.7898509)));
        pairs.add(new Couple(new NumberPair(83.1346048, -51.4140969), new NumberPair(83.1359277, -51.4139545)));
        pairs.add(new Couple(new NumberPair(28.4171021, -0.2639823), new NumberPair(28.4171034, -0.2628419)));
        pairs.add(new Couple(new NumberPair(43.7084538, -8.8811802), new NumberPair(43.7088891, -8.8795421)));
        pairs.add(new Couple(new NumberPair(37.8054288, -3.1964142), new NumberPair(37.8049613, -3.1950330)));
        pairs.add(new Couple(new NumberPair(356.1829712, 0.5201146), new NumberPair(356.1806290, 0.5201466)));
        pairs.add(new Couple(new NumberPair(64.1748361, -21.8802396), new NumberPair(64.1727738, -21.8788069)));
        pairs.add(new Couple(new NumberPair(189.7530615, -0.9093664), new NumberPair(189.7546283, -0.9074848)));
        pairs.add(new Couple(new NumberPair(11.2912826, -28.9325546), new NumberPair(11.2896207, -28.9321265)));
        pairs.add(new Couple(new NumberPair(16.8236365, -26.9800885), new NumberPair(16.8228410, -26.9786380)));
        pairs.add(new Couple(new NumberPair(10.6659165, -25.1205574), new NumberPair(10.6651479, -25.1210108)));
        pairs.add(new Couple(new NumberPair(41.2802081, -12.6240460), new NumberPair(41.2788699, -12.6249969)));
        pairs.add(new Couple(new NumberPair(340.2579184, -45.0073440), new NumberPair(340.2595948, -45.0089825)));
        pairs.add(new Couple(new NumberPair(22.6007936, -4.5643123), new NumberPair(22.6004189, -4.5633730)));
        pairs.add(new Couple(new NumberPair(31.4218210, 2.0471276), new NumberPair(31.4233605, 2.0469716)));
    }

    @Test
    public void calculateAngularSeperation() {
        int pairNumber = 0;
        for (Couple<NumberPair, NumberPair> couple : pairs) {
            if (couple.getA().getX() == 0) {
                pairNumber++;
                continue;
            }
            CatalogEntry entry = new GaiaDR3CatalogEntry();
            entry.setRa(couple.getA().getX());
            entry.setDec(couple.getA().getY());
            entry.setSearchRadius(2);
            GaiaDR3CatalogEntry primaryGaia = (GaiaDR3CatalogEntry) retrieveCatalogEntry(entry, new CatalogQueryService(), null);
            entry = new NoirlabCatalogEntry();
            entry.setRa(couple.getA().getX());
            entry.setDec(couple.getA().getY());
            entry.setSearchRadius(1);
            NoirlabCatalogEntry primaryNoirlab = (NoirlabCatalogEntry) retrieveCatalogEntry(entry, new CatalogQueryService(), null);
            entry = new NoirlabCatalogEntry();
            entry.setRa(couple.getB().getX());
            entry.setDec(couple.getB().getY());
            entry.setSearchRadius(1);
            NoirlabCatalogEntry secondaryNoirlab = (NoirlabCatalogEntry) retrieveCatalogEntry(entry, new CatalogQueryService(), null);
            double numberOfYears = primaryNoirlab.getMeanEpoch() - secondaryNoirlab.getMeanEpoch();
            System.out.println("==========> pair #" + ++pairNumber);
            System.out.println("mean epoch of primary       = " + primaryNoirlab.getMeanEpoch());
            System.out.println("mean epoch of secondary     = " + secondaryNoirlab.getMeanEpoch());
            System.out.println("difference                  = " + numberOfYears);
            NumberPair correctedPrimaryPosition = calculatePositionFromProperMotion(new NumberPair(primaryNoirlab.getRa(), primaryNoirlab.getDec()),
                    new NumberPair(-numberOfYears * primaryNoirlab.getPmra() / DEG_MAS, -numberOfYears * primaryNoirlab.getPmdec() / DEG_MAS)
            );
            double angularDistance = calculateAngularDistance(new NumberPair(primaryNoirlab.getRa(), primaryNoirlab.getDec()), new NumberPair(secondaryNoirlab.getRa(), secondaryNoirlab.getDec()), DEG_ARCSEC);
            double correctedAngularSeparation = calculateAngularDistance(correctedPrimaryPosition, new NumberPair(secondaryNoirlab.getRa(), secondaryNoirlab.getDec()), DEG_ARCSEC);
            System.out.println("angularDistance             = " + roundTo3DecNZ(angularDistance));
            //System.out.println("correctedPrimaryPosition  = " + correctedPrimaryPosition);
            System.out.println("correctedAngularSeparation  = " + roundTo3DecNZ(correctedAngularSeparation));
            double projectedPhysicalSeparation = correctedAngularSeparation * primaryGaia.getParallacticDistance();
            System.out.println("projectedPhysicalSeparation = " + roundTo3DecNZ(projectedPhysicalSeparation));
            System.out.println("pair #" + pairNumber + " " + roundTo3DecNZ(correctedAngularSeparation) + " " + roundTo3DecNZ(projectedPhysicalSeparation));
            //System.out.println("pair #" + pairNumber + " " + roundTo3DecNZ(primaryGaia.getRuwe()));
        }
    }

    @Test
    public void addPhotometry() throws Exception {
        StringBuilder results = new StringBuilder();
        try (Scanner fileScanner = new Scanner(new File("C:/Users/wcq637/Documents/Private/BYW/Binary systems/Research note/Co-moving pairs.csv"))) {
            String headerLine = fileScanner.nextLine();
            results.append(headerLine).append(",");
            results.append("Gaia eDR3 ID,G,BP,RP,NSC DR2 ID,u,u err,g,g err,r,r err,i,i err,z,z err,Y,Y err,");
            results.append("PS1 DR2 ID,g,g err,r,r err,i,i err,z,z err,y,y err,SDSS DR16 ID,u,u err,g,g err,r,r err,i,i err,z,z err,");
            results.append("2MASS ID,J,J err,H,H err,K,K err,VHS DR5 ID,Y,Y err,J,J err,H,H err,K,K err,");
            results.append("AllWISE ID,W1,W1 err,W2,W2 err,W3,W3 err,W4,W4 err,CatWISE ID,W1,W1 err,W2,W2 err,unWISE ID,W1,W2");
            results.append(LINE_SEP);
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            String separators = "";
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
                separators += ",";
            }
            while (fileScanner.hasNextLine()) {
                String bodyLine = fileScanner.nextLine();
                results.append(bodyLine).append(",");
                String[] values = CSVParser.parseLine(bodyLine);
                String objectNumber = values[columns.get("Object #")];
                String catalog = values[columns.get("Catalog")];
                double ra = toDouble(values[columns.get("RA")]);
                double dec = toDouble(values[columns.get("Dec")]);
                if ("NSC DR2".equals(catalog) || "5".equals(objectNumber) || "6".equals(objectNumber)) {
                    CatalogEntry gaiaEntry = fetchCatalogEntry(ra, dec, 5, new GaiaDR3CatalogEntry());
                    CatalogEntry noirlabEntry = fetchCatalogEntry(ra, dec, 5, new NoirlabCatalogEntry());
                    CatalogEntry panstarrsEntry = fetchCatalogEntry(ra, dec, 5, new PanStarrsCatalogEntry());
                    CatalogEntry sdssEntry = fetchCatalogEntry(ra, dec, 5, new SdssCatalogEntry());
                    CatalogEntry twomassEntry = fetchCatalogEntry(ra, dec, 10, new TwoMassCatalogEntry());
                    CatalogEntry vhsEntry = fetchCatalogEntry(ra, dec, 5, new VhsCatalogEntry());
                    CatalogEntry allwiseEntry = fetchCatalogEntry(ra, dec, 5, new AllWiseCatalogEntry());
                    CatalogEntry catwiseEntry = fetchCatalogEntry(ra, dec, 5, new CatWiseCatalogEntry());
                    CatalogEntry unwiseEntry = fetchCatalogEntry(ra, dec, 5, new UnWiseCatalogEntry());
                    if (gaiaEntry == null) {
                        results.append(",,,,");
                    } else {
                        results.append(gaiaEntry.getSourceId()).append(",").append(gaiaEntry.getPhotometry());
                    }
                    if (noirlabEntry == null) {
                        results.append(",,,,,,,,,,,,,");
                    } else {
                        results.append(noirlabEntry.getSourceId()).append(",").append(noirlabEntry.getPhotometry());
                    }
                    if (panstarrsEntry == null) {
                        results.append(",,,,,,,,,,,");
                    } else {
                        results.append(panstarrsEntry.getSourceId()).append(",").append(panstarrsEntry.getPhotometry());
                    }
                    if (sdssEntry == null) {
                        results.append(",,,,,,,,,,,");
                    } else {
                        results.append(sdssEntry.getSourceId()).append(",").append(sdssEntry.getPhotometry());
                    }
                    if (twomassEntry == null) {
                        results.append(",,,,,,,");
                    } else {
                        results.append(twomassEntry.getSourceId()).append(",").append(twomassEntry.getPhotometry());
                    }
                    if (vhsEntry == null) {
                        results.append(",,,,,,,,,");
                    } else {
                        results.append(vhsEntry.getSourceId()).append(",").append(vhsEntry.getPhotometry());
                    }
                    if (allwiseEntry == null) {
                        results.append(",,,,,,,,,");
                    } else {
                        results.append(allwiseEntry.getSourceId()).append(",").append(allwiseEntry.getPhotometry());
                    }
                    if (catwiseEntry == null) {
                        results.append(",,,,,");
                    } else {
                        results.append(catwiseEntry.getSourceId()).append(",").append(catwiseEntry.getPhotometry());
                    }
                    if (unwiseEntry == null) {
                        results.append(",,,");
                    } else {
                        results.append(unwiseEntry.getSourceId()).append(",").append(unwiseEntry.getPhotometry());
                    }
                } else {
                    results.append(separators).append(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
                }
                results.append(LINE_SEP);
            }
        }
        File resultFile = new File("C:/Users/wcq637/Documents/Private/BYW/Binary systems/Research note/Co-moving pairs photometry.csv");
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write(results.toString());
        }
    }

    private CatalogEntry fetchCatalogEntry(double ra, double dec, double radius, CatalogEntry entry) {
        entry.setRa(ra);
        entry.setDec(dec);
        entry.setSearchRadius(radius);
        return retrieveCatalogEntry(entry, new CatalogQueryService(), null);
    }

}
