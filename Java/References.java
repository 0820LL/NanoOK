package nanotools;

import java.io.*;
import java.util.*;

public class References {
    private NanotoolsOptions options;
    private File sizesFile;
    private Hashtable<String,ReferenceContig> refContigs = new Hashtable();
    private int longestId = 0;
        
    public References(NanotoolsOptions o)
    {
        options = o;
        getSizesFile();
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(sizesFile));
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split("\\t");
                ReferenceContig c = refContigs.get(values[0]);
                if (c != null) {
                    System.out.println("Error: reference contig ID "+values[0]+" occurs more than once.");
                    System.exit(1);
                } else {
                    refContigs.put(values[0], new ReferenceContig(values[0], Integer.parseInt(values[1]), values[2]));
                }
                
                if (values[0].length() > longestId) {
                    longestId = values[0].length();
                }
                
                line = br.readLine();
            }
        } catch (Exception ioe) {
            System.out.println("NanotoolsReferences Exception:");
            System.out.println(ioe);
        }
    }    

    public ReferenceContig getReferenceById(String id) {
        return refContigs.get(id);
    }
    
    public void clearReferenceStats() {
        Set<String> keys = refContigs.keySet();
        for(String id : keys) {
            refContigs.get(id).clearStats();
        }        
    }
    
    public Set<String> getAllIds() {
        return refContigs.keySet();
    }
    
    public void writeReferenceStatFiles(int type) {
        Set<String> keys = refContigs.keySet();
        String analysisDir = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "analysis";
        
        for(String id : keys) {
            ReferenceContig ref = refContigs.get(id);
            ref.writeCoverageData(analysisDir + options.getSeparator() + ref.getName() + "_" + options.getTypeFromInt(type) + "_coverage.txt", options.getCoverageBinSize());
            ref.writePerfectKmerHist(analysisDir + options.getSeparator() + ref.getName() + "_" + options.getTypeFromInt(type) + "_all_perfect_kmers.txt");
            ref.writeBestPerfectKmerHist(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_read_best_perfect_kmers.txt");
            ref.writeBestPerfectKmerHistCumulative(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_read_best_cumulative_perfect_kmers.txt");
        }        
    }
    
    public int getLongestIdLength() {
        return longestId;
    }
    
    private void getSizesFile()
    {
        sizesFile = new File(options.getReference()+".sizes");
        if (! sizesFile.exists()) {
            sizesFile = new File(options.getReference()+".fasta.sizes");
            if (!sizesFile.exists()) {
                sizesFile = new File(options.getReference()+".fa.sizes");
            }
        }
        
        if (!sizesFile.exists()) {
            System.out.println("Error: can't read sizes file.");
            System.exit(1);
        }
    }
    
    public void writeReferenceSummary() {
       try {
            PrintWriter pw = new PrintWriter(new FileWriter(options.getAlignmentSummaryFilename(), true));
            String formatString = "%-"+longestId+"s %-12s %-10s %-10s\n";
            pw.println("");
            pw.printf(formatString, "Id", "Size", "ReadsAlign", "LongPerfKm");        
            Set<String> keys = refContigs.keySet();
            for(String id : keys) {
                refContigs.get(id).writeSummary(pw, "%-"+longestId+"s %-12d %-10d %-10d\n");
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("writeReferenceSummary exception:");
            System.out.println(e);
        }
    }
}
